package com.yanwq.nws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yanwq.nws.event.EventCallback;
import com.yanwq.nws.event.EventMgr;
import com.yanwq.nws.message.MessageCallback;
import com.yanwq.nws.message.MessageConst;
import com.yanwq.nws.message.MessageMgr;
import com.yanwq.nws.netty.WebSocketServer;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;
import sun.plugin2.message.Message;


/**
 * Created by dodoca_android on 2017/4/24.
 */
public class IMEngine {
    Logger logger = Logger.getLogger(IMEngine.class.getSimpleName());

    public IMEngine() {
        super();
    }

    public static void main(String[] args) throws Exception {
        new IMEngine().listen();
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new WebSocketServer(port).run();
    }

    public void listen() {
        EventMgr.getInstance().addEventCallback(new EventCallback() {
            public void call(Channel channel, String data) {
                JSONObject jsonObject = JSON.parseObject(data);
                String event = jsonObject.getString("event");

                if ("register".equalsIgnoreCase(event)) {
                    register(channel, data);
                } else if ("unregister".equalsIgnoreCase(event)) {
                    unregister(data);
                } else if ("message".equalsIgnoreCase(event)) {
                    message(data);
                } else if ("broadcast".equalsIgnoreCase(event)) {
                    MessageMgr.getInstance().broadcast(jsonObject.toJSONString());
                } else if ("users".equalsIgnoreCase(event)) {
                    users(jsonObject);
                }
            }
        });
    }

    private void sendACK(String message) {
        String[] array = MessageConst.getArray(message);
        JSONObject jsonObject = JSON.parseObject(array[1]);
        String uuid = jsonObject.getString("from_uuid");
        jsonObject.put("status", "ACK");
        MessageMgr.getInstance().send(uuid, MessageConst.wrapMsg(array[0], message));
    }

    private void sendNACK(String message) {
        String[] array = MessageConst.getArray(message);
        JSONObject jsonObject = JSON.parseObject(array[1]);
        String uuid = jsonObject.getString("from_uuid");
        jsonObject.put("status", "NACK");
        MessageMgr.getInstance().send(uuid, MessageConst.wrapMsg(array[0], message));
    }

    private String getFromUuid(String message) {
        String[] array = MessageConst.getArray(message);
        JSONObject jsonObject = JSON.parseObject(array[1]);
        return jsonObject.getString("from_uuid");
    }

    private String getToUuid(String message) {
        String[] array = MessageConst.getArray(message);
        JSONObject jsonObject = JSON.parseObject(array[1]);
        return jsonObject.getString("to_uuid");
    }

    private void register(Channel channel, String message) {
        sendACK(message);
        MessageMgr.getInstance().putUuidChannel(getFromUuid(message), channel);
    }

    private void unregister(String message) {
        sendACK(message);
        MessageMgr.getInstance().removeUuidChannel(getFromUuid(message));
    }

    private void message(String message) {
        MessageMgr.getInstance().send(getToUuid(message), message, new MessageCallback() {
            public void onSuccess(String message) {
                sendACK(message);
            }

            public void onFailure(String message, FailureType failureType) {

            }
        });
    }

    private void users(JSONObject data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "users");
        jsonObject.put("users", JSON.parseArray(JSON.toJSONString(MessageMgr.getInstance().getUuidChannel().keySet())));
        MessageMgr.getInstance().send(data.getString("uuid"), jsonObject.toJSONString());
    }
}