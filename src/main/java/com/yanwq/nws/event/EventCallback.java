package com.yanwq.nws.event;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;

/**
 * Created by dodoca_android on 2017/4/24.
 */
public interface EventCallback {
    void call(Channel channel, String data);
}