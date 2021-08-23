package com.wobenwudi;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;

/**
 * 服务端handler
 */
public class MyAcceptHandler extends ChannelInboundHandlerAdapter {

    /**
     * Netty线程池
     * 相当于Nio Selector
     */
    private NioEventLoopGroup selector;

    private ChannelHandler handler;


    public MyAcceptHandler(NioEventLoopGroup selector, ChannelHandler handler) {
        this.selector = selector;
        this.handler = handler; // ChannelInit
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server register ...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // accept netty 做过了
        // msg 直接解出来客户端
        SocketChannel client = (SocketChannel) msg;

        // 注册读事件
        ChannelPipeline pipeline = client.pipeline();
        // 1. client::pipeline[ChannelInit]
        pipeline.addLast(handler);

        selector.register(client);
    }
}
