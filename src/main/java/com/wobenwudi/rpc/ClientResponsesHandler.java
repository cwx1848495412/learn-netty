package com.wobenwudi.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutorGroup;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class ClientResponsesHandler extends ChannelInboundHandlerAdapter {
    // consumer
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PackageMsg packageMsg = (PackageMsg) msg;
        // 曾经没考虑返回
        ResponseHandler.runCallback(packageMsg);
    }
}
