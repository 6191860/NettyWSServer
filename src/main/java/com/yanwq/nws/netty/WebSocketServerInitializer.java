/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.yanwq.nws.netty;

import com.yanwq.nws.message.MessageMgr;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.log4j.Logger;

/**
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    Logger logger = Logger.getLogger(getClass());

    @Override
    public void initChannel(final SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("idle", new IdleStateHandler(2 * 60, 2 * 60, 4 * 60));
        pipeline.addLast("idle-event", new ChannelInboundHandlerAdapter() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                super.userEventTriggered(ctx, evt);
                if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                    logger.info(ctx.channel().id() + " ping timeout,will be remove");
                    MessageMgr.getInstance().removeChannel(ctx.channel());
                    ctx.channel().closeFuture().sync();
                }
            }
        });
        pipeline.addLast("codec-http", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("handler", new WebSocketServerHandler());
    }
}
