package com.yanwq.nws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yanweiqiang on 2017/2/23 0023.
 */
public class Sender {
    private Logger logger = Logger.getLogger(Sender.class.getName());
    private ChannelGroup channels;
    private HashMap<String, ChannelId> uuidChannel;
    private HashMap<ChannelId, String> channelUuid;

    private static Sender instance;

    public static Sender getInstance() {
        syncInit();
        return instance;
    }

    private static void syncInit() {
        if (instance == null) {
            instance = new Sender();
        }
    }

    private Sender() {
        channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        uuidChannel = new HashMap<String, ChannelId>();
        channelUuid = new HashMap<ChannelId, String>();
    }

    public void addChannel(Channel channel) {
        channels.add(channel);
    }

    public void removeChannel(Channel channe) {
        channels.remove(channe);
        uuidChannel.remove(channelUuid.get(channe.id()));
        channelUuid.remove(channe.id());
        logger.info("channels:" + uuidChannel.values());
    }

    public void putUuidChannel(String uuid, Channel channel) {
        uuidChannel.put(uuid, channel.id());
        channelUuid.put(channel.id(), uuid);
    }

    public void removeUuidChannel(String uuid) {
        channelUuid.remove(uuidChannel.get(uuid));
        uuidChannel.remove(uuid);
    }

    public HashMap<String, ChannelId> getUuidChannel() {
        return uuidChannel;
    }

    public void send(String uuid, String message) {
        logger.debug(uuidChannel.keySet().toString());

        if (!uuidChannel.containsKey(uuid)) {
            logger.debug("No user");
            return;
        }

        Channel channel = channels.find(uuidChannel.get(uuid));

        if (channel == null) {
            removeUuidChannel(uuid);
            logger.debug("No user");
            return;
        }

        channel.writeAndFlush(new TextWebSocketFrame(message));
        logger.debug("Send success");
    }

    public void broadcast(String msg) {
        logger.debug(uuidChannel.keySet().toString());
        channels.writeAndFlush(new TextWebSocketFrame(msg));
    }

}
