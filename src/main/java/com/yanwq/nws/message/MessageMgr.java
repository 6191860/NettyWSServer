package com.yanwq.nws.message;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.Logger;
import sun.plugin2.message.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yanweiqiang on 2017/2/23 0023.
 */
public class MessageMgr {
    private Logger logger = Logger.getLogger(MessageMgr.class.getName());
    private ChannelGroup channels;
    private Map<String, ChannelId> uuidChannel;
    //For remove channel avoid loop.
    private Map<ChannelId, String> channelUuid;
    private Map<String, MessageCallback> messageCallbackMap;

    private static MessageMgr instance;

    public static MessageMgr getInstance() {
        syncInit();
        return instance;
    }

    private static void syncInit() {
        if (instance == null) {
            instance = new MessageMgr();
        }
    }

    private MessageMgr() {
        channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        uuidChannel = new HashMap<String, ChannelId>();
        channelUuid = new HashMap<ChannelId, String>();
        messageCallbackMap = new HashMap<String, MessageCallback>();
    }

    public void addChannel(Channel channel) {
        channels.add(channel);
    }

    public void removeChannel(Channel channel) {
        channels.remove(channel);
        String uuid = channelUuid.get(channel.id());
        uuidChannel.remove(uuid);
        channelUuid.remove(channel.id());
        logger.info("remove channel uuid:" + uuid);
    }

    public void putUuidChannel(String uuid, Channel channel) {
        uuidChannel.put(uuid, channel.id());
        channelUuid.put(channel.id(), uuid);
    }

    public void removeUuidChannel(String uuid) {
        channelUuid.remove(uuidChannel.get(uuid));
        uuidChannel.remove(uuid);
    }

    public Map<String, ChannelId> getUuidChannel() {
        return uuidChannel;
    }

    public void callSuccess(String msgId, String message) {
        MessageCallback messageCallback = messageCallbackMap.get(msgId);
        if (messageCallback == null) {
            return;
        }
        messageCallback.onSuccess(message);
    }

    public void callFailure(String msgId, String message, MessageCallback.FailureType type) {
        MessageCallback messageCallback = messageCallbackMap.get(msgId);
        if (messageCallback == null) {
            return;
        }
        messageCallback.onFailure(message, type);
    }


    public boolean send(String uuid, String message) {
        return send(uuid, message, null);
    }

    public boolean send(String uuid, String message, MessageCallback callback) {
        logger.debug(uuidChannel.keySet().toString());
        String msgId = UUID.randomUUID().toString();
        String wrappedMsg = msgId + MessageConst.SEPARATOR + message;

        if (!uuidChannel.containsKey(uuid)) {
            logger.debug("Unknown user");
            if (callback != null) {
                callback.onFailure(message, MessageCallback.FailureType.UNKNOWN_USER);
            }
            return false;
        }

        Channel channel = channels.find(uuidChannel.get(uuid));

        if (channel == null) {
            logger.debug("Unknown channel");
            removeUuidChannel(uuid);
            if (callback != null) {
                callback.onFailure(message, MessageCallback.FailureType.UNKNOWN_CHANNEL);
            }
            return false;
        }

        messageCallbackMap.put(msgId, callback);
        channel.writeAndFlush(new TextWebSocketFrame(wrappedMsg));
        return true;
    }

    public void broadcast(String msg) {
        logger.debug(uuidChannel.keySet().toString());
        channels.writeAndFlush(new TextWebSocketFrame(msg));
    }
}
