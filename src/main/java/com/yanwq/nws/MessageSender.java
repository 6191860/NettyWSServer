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

    public void removeChannel(Channel channel) {
        channels.remove(channel);
        logger.info("channels:" + hashMap.values());
    }


    public void register(String uuid, ChannelId channelId) {
        hashMap.put(uuid, channelId);
        logger.debug(hashMap.keySet().toString());
    }

    public void unRegister(String uuid) {
        hashMap.remove(uuid);
        logger.debug(hashMap.keySet().toString());
    }

    public void message(String uuid, String msg) {
        logger.debug(hashMap.keySet().toString());

        if (!hashMap.containsKey(uuid)) {
            logger.debug("No user");
            return;
        }

        Channel channel = channels.find(hashMap.get(uuid));

        if (channel == null) {
            unRegister(uuid);
            logger.debug("No user");
            return;
        }

        channel.writeAndFlush(new TextWebSocketFrame(msg));
        logger.debug("Send success");
    }

    public void getOnLineUsers(String uuid) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "getOnLineUser");
        jsonObject.put("users", JSON.parseArray(JSON.toJSONString(hashMap.keySet())));
        message(uuid, jsonObject.toJSONString());
    }

    public void broadcast(String msg) {
        logger.debug(hashMap.keySet().toString());
        channels.writeAndFlush(new TextWebSocketFrame(msg));
    }
}
