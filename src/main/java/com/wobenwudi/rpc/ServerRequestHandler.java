package com.wobenwudi.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutorGroup;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class ServerRequestHandler extends ChannelInboundHandlerAdapter {

    // provider
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PackageMsg packageMsg = (PackageMsg) msg;
        System.out.println("server handler : " +
                packageMsg.getContent().getMethodName() +
                packageMsg.getContent().getArgs()[0]
        );
        // TODO:: dispatcher 分发请求给不同路由对应方法
        // 假设处理了用户请求 要给客户端返回了~
        ctx.writeAndFlush("".getBytes());



    }

}
