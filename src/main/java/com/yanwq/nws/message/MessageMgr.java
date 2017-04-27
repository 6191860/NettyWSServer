package com.yanwq.nws.message;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Author: yanweiqiang.
 * Date:　 2017/2/23 0023.
 */
public class MessageMgr {
    private Logger logger = Logger.getLogger(MessageMgr.class.getName());
    private ChannelGroup channels;
    private Map<String, ChannelId> channelIdMap;
    //For remove channel avoid loop.
    private Map<ChannelId, String> uuidMap;
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
        channelIdMap = new HashMap<String, ChannelId>();
        uuidMap = new HashMap<ChannelId, String>();
        messageCallbackMap = new HashMap<String, MessageCallback>();
    }

    public void addChannel(Channel channel) {
        channels.add(channel);
    }

    public void removeChannel(Channel channel) {
        channels.remove(channel);
        String uuid = uuidMap.get(channel.id());
        channelIdMap.remove(uuid);
        uuidMap.remove(channel.id());
        logger.info("Remove channel uuid:" + uuid);
    }

    public void putChannelId(String uuid, ChannelId channelId) {
        channelIdMap.put(uuid, channelId);
        uuidMap.put(channelId, uuid);
    }

    public void removeChannelId(String uuid) {
        uuidMap.remove(channelIdMap.get(uuid));
        channelIdMap.remove(uuid);
    }

    public Map<String, ChannelId> getChannelIdMap() {
        return channelIdMap;
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
        String msgId = UUID.randomUUID().toString();
        String wrappedMsg = msgId + MessageConst.SEPARATOR + message;

        if (!channelIdMap.containsKey(uuid)) {
            logger.debug("Unknown user");
            if (callback != null) {
                callback.onFailure(message, MessageCallback.FailureType.UNKNOWN_USER);
            }
            return false;
        }

        Channel channel = channels.find(channelIdMap.get(uuid));

        if (channel == null) {
            logger.debug("Unknown channel");
            removeChannelId(uuid);
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
        logger.debug(uuidMap.keySet().toString());
        channels.writeAndFlush(new TextWebSocketFrame(msg));
    }
}
