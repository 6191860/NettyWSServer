package com.yanwq.nws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.log4j.Logger;


/**
 * Created by dodoca_android on 2017/4/24.
 */
public class ChatMgr {
    Logger logger = Logger.getLogger(ChatMgr.class.getSimpleName());

    public ChatMgr() {
        super();
    }

    public void initEvent() {
        EventController.getInstance().addEventCallback(new EventCallback() {
            public void call(Channel channel, JSONObject jsonObject) {
                String event = jsonObject.getString("event");

                if ("register".equalsIgnoreCase(event)) {
                    register(channel, jsonObject);
                } else if ("unregister".equalsIgnoreCase(event)) {
                    unregister(jsonObject);
                } else if ("message".equalsIgnoreCase(event)) {
                    message(jsonObject);
                } else if ("broadcast".equalsIgnoreCase(event)) {
                    Sender.getInstance().broadcast(jsonObject.toJSONString());
                } else if ("users".equalsIgnoreCase(event)) {
                    users(jsonObject);
                }
            }
        });
    }


    private void register(Channel channel, final JSONObject jsonObject) {
        Sender.getInstance().putUuidChannel(jsonObject.getString("uuid"), channel);
        jsonObject.put("ret", "ACK");
        Sender.getInstance().send(jsonObject.getString("uuid"), jsonObject.toJSONString());
    }

    private void unregister(JSONObject jsonObject) {
        Sender.getInstance().removeUuidChannel(jsonObject.getString("uuid"));
    }

    private void message(JSONObject jsonObject) {
        Sender.getInstance().send(jsonObject.getString("toUuid"), jsonObject.toJSONString());
        logger.debug("Send success");
    }

    private void users(JSONObject data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "users");
        jsonObject.put("users", JSON.parseArray(JSON.toJSONString(Sender.getInstance().getUuidChannel().keySet())));
        Sender.getInstance().send(data.getString("uuid"), jsonObject.toJSONString());
    }
}