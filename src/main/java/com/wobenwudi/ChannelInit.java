package com.wobenwudi;

import io.netty.channel.*;

@ChannelHandler.Sharable
public class ChannelInit extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        ChannelPipeline pipeline = client.pipeline();

        // 2. client::pipeline[ChannelInit, MyInHandler]
        pipeline.addLast(new MyInHandler());
        // 过河拆桥
        ctx.pipeline().remove(this);
    }
}
