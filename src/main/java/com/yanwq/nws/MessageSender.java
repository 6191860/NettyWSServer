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

import java.util.HashMap;

/**
 * Created by yanweiqiang on 2017/2/23 0023.
 */
public class MessageSender {
    private Logger logger = Logger.getLogger(MessageSender.class.getName());
    private ChannelGroup channels;
    private HashMap<String, ChannelId> hashMap;

    private static MessageSender instance;

    public static MessageSender getInstance() {
        syncInit();
        return instance;
    }

    private static void syncInit() {
        if (instance == null) {
            instance = new MessageSender();
        }
    }

    private MessageSender() {
        channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        hashMap = new HashMap<String, ChannelId>();
    }

    public void addChannel(Channel channel) {
        channels.add(channel);
    }

    public void removeChannel(ChannelId channelId) {
        channels.remove(channelId);
        logger.info("channels:" + hashMap.values());
    }

    public void register(Channel channel, JSONObject jsonObject) {
        hashMap.put(jsonObject.getString("uuid"), channel.id());
        jsonObject.put("ret", "ACK");
        message(jsonObject.getString("uuid"), jsonObject.toJSONString());
        logger.debug(hashMap.keySet().toString());
    }

    public void unregister(String uuid) {
        hashMap.remove(uuid);
        logger.debug(hashMap.keySet().toString());
    }

    public void message(String toUuid, String message) {
        logger.debug(hashMap.keySet().toString());

        if (!hashMap.containsKey(toUuid)) {
            logger.debug("No user");
            return;
        }

        Channel channel = channels.find(hashMap.get(toUuid));

        if (channel == null) {
            unregister(toUuid);
            logger.debug("No user");
            return;
        }

        channel.writeAndFlush(new TextWebSocketFrame(message));
        logger.debug("Send success");
    }

    public void users(String uuid) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "users");
        jsonObject.put("users", JSON.parseArray(JSON.toJSONString(hashMap.keySet())));
        message(uuid, jsonObject.toJSONString());
    }

    public void broadcast(String msg) {
        logger.debug(hashMap.keySet().toString());
        channels.writeAndFlush(new TextWebSocketFrame(msg));
    }
}
