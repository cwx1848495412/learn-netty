package com.wobenwudi;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.net.InetSocketAddress;

public class MyNetty {


    @Test
    public void loopExecutor() throws Exception {
        // 线程池
        NioEventLoopGroup selector = new NioEventLoopGroup(2);

        selector.execute(() -> {
            for (; ; ) {
                System.out.println("hello world - 1");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        selector.execute(() -> {
            for (; ; ) {
                System.out.println("hello world - 2");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        System.in.read();
    }

    @Test
    public void clientMode() throws Exception {
        NioEventLoopGroup threadGroup = new NioEventLoopGroup(1);

        // 客户端模式
        NioSocketChannel client = new NioSocketChannel();
        threadGroup.register(client); // epoll_ctl(5,ADD,3)


        // 异步回调 模板方法模式?
        ChannelPipeline pipeline = client.pipeline();
        pipeline.addLast(new MyInHandler());


        // 全异步
        ChannelFuture connect = client.connect(
                new InetSocketAddress("192.168.1.227", 9090)
        );
        ChannelFuture sync = connect.sync();

        ByteBuf byteBuf = Unpooled.copiedBuffer("hello server".getBytes());
        ChannelFuture send = client.writeAndFlush(byteBuf);
        send.sync();


        // 等服务端连接关闭的异步回调 再关闭客户端
        sync.channel().closeFuture().sync();
        System.out.println("client over...");
    }

    @Test
    public void serverMode() throws Exception {
        NioEventLoopGroup threadGroup = new NioEventLoopGroup(1);

        NioServerSocketChannel server = new NioServerSocketChannel();
        threadGroup.register(server);

        // 读事件  是有客户端发起的 服务端不知道数据什么时候会来
        ChannelPipeline pipeline = server.pipeline();

        // 接受accept 时间 注册 readable 事件
        pipeline.addLast(new MyAcceptHandler(threadGroup, new ChannelInit()));


        ChannelFuture future = server.bind(
                new InetSocketAddress("192.168.1.44", 9090)
        );

        // 连接成功 并且 客户端关闭连接
        future.sync().channel().closeFuture().sync();
        System.out.println("Server close ...");
    }

    @Test
    public void nettyClient() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);

        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture connect = bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MyInHandler());
                    }
                })
                .connect(
                        new InetSocketAddress("192.168.1.227", 9090)
                );

        Channel client = connect.sync().channel();

        ByteBuf byteBuf = Unpooled.copiedBuffer("Hello Server".getBytes());
        ChannelFuture send = client.writeAndFlush(byteBuf);
        send.sync();
        client.closeFuture().sync();


    }


    @Test
    public void nettyServer() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap bs = new ServerBootstrap();
        ChannelFuture bind = bs.group(group, group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MyInHandler());
                    }
                })
                .bind(new InetSocketAddress("192.168.1.44", 9090));

        bind.sync().channel().closeFuture().sync();


    }



}
