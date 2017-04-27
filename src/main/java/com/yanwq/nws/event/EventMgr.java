package com.yanwq.nws.event;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dodoca_android on 2017/4/24.
 */
public class EventMgr {
    private List<EventCallback> eventCallbackList;

    private static EventMgr instance;

    private synchronized static void syncInit() {
        if (instance == null) {
            instance = new EventMgr();
        }
    }

    public static EventMgr getInstance() {
        syncInit();
        return instance;
    }

    private EventMgr() {
        eventCallbackList = new ArrayList<EventCallback>();
    }

    public void call(Channel channel, String data) {
        for (EventCallback callback : eventCallbackList) {
            callback.call(channel, data);
        }
    }

    public void addEventCallback(EventCallback callback) {
        eventCallbackList.add(callback);
    }

    public void removeEventCallback(EventCallback callback) {
        eventCallbackList.remove(callback);
    }
}
