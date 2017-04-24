package com.yanwq.nws;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dodoca_android on 2017/4/24.
 */
public class EventController {
    private List<EventCallback> eventCallbackList;

    private static EventController instance;

    private synchronized static void syncInit() {
        if (instance == null) {
            instance = new EventController();
        }
    }

    public static EventController getInstance() {
        syncInit();
        return instance;
    }

    private EventController() {
        eventCallbackList = new ArrayList<EventCallback>();
    }

    public void call(Channel channel, JSONObject jsonObject) {
        for (EventCallback callback : eventCallbackList) {
            callback.call(channel, jsonObject);
        }
    }

    public void addEventCallback(EventCallback callback) {
        eventCallbackList.add(callback);
    }

    public void removeEventCallback(EventCallback callback) {
        eventCallbackList.remove(callback);
    }
}
