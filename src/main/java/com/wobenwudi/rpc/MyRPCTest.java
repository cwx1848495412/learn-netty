package com.wobenwudi.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 来回通信
 * 连接数量
 * 粘包拆包
 * 动态代理 序列化 协议封装
 * 连接池
 */
public class MyRPCTest {

    @Test
    public void consumer() throws Exception {

        Thread thread = new Thread(() -> {
            startServer();

        });
        thread.start();

        System.out.println("server started....");

        AtomicInteger num = new AtomicInteger(0);
        int size = 20;
        Thread[] threads = new Thread[size];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                // 动态代理获取对象
                Car car = proxyGet(Car.class);
                String param = "hello " + num.incrementAndGet();
                String moveRes = car.move(param);
                System.out.println("client over msg: " + moveRes + "param: " + param);
            });
        }

        for (Thread th : threads) {
            th.start();
        }

        System.in.read();
    }

    @Test
    public void startServer() {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = boss;

        ServerBootstrap sbs = new ServerBootstrap();
        ChannelFuture bind = sbs.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("server accept client port: " + ch.remoteAddress().getPort());
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ServerDecode());
                        pipeline.addLast(new ServerRequestHandler());
                    }
                })
                .bind(new InetSocketAddress("localhost", 9090));

        try {
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public <T> T proxyGet(Class<T> interfaceInfo) {
        ClassLoader classLoader = interfaceInfo.getClassLoader();

        Class<?>[] classInfoList = {interfaceInfo};

        return (T) Proxy.newProxyInstance(classLoader, classInfoList, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                // 1. 调用服务 方法 参数 ==> 封装成 message bytes
                // message [content]
                String name = interfaceInfo.getName();
                String methodName = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();

                MyContent content = new MyContent();
                content.setArgs(args);
                content.setName(name);
                content.setMethodName(methodName);
                content.setParameterTypes(parameterTypes);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(out);
                oout.writeObject(content);
                byte[] msgBody = out.toByteArray();
                out.reset();

                // 2. requestID + message 本地缓存 返回识别
                // 协议 [header][msgBody]
                MyHeader header = createHeader(msgBody);

                oout = new ObjectOutputStream(out);
                oout.writeObject(header);
                byte[] msgHeader = out.toByteArray();

//                System.out.println("msgHeader Size: " + msgHeader.length);
                out.reset();

                // 3. 连接池 取得连接
                ClientFactory factory = ClientFactory.getFactory();
                NioSocketChannel clientChannel = factory.getClient(new InetSocketAddress("localhost", 9090));

                // 4. 发送-->走IO Netty 驱动里的回调

                CompletableFuture<String> future = new CompletableFuture<>();
                ResponseHandler.addCallback(header.getRequestID(), future);

                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT
                        .directBuffer(msgHeader.length + msgBody.length);
                byteBuf.writeBytes(msgHeader);
                byteBuf.writeBytes(msgBody);

                ChannelFuture channelFuture = clientChannel.writeAndFlush(byteBuf);
                // 同步等待
                // IO 是双向的 看似有个sync 仅代表out
                channelFuture.sync();
                // 发送成功了 其实客户端并没有返回值

                // 死等 等待其他线程通过requestID  唤醒


                // 5. 需要实现阻塞 线程睡眠 等待返回
                // 6. countdownLatch
                return future.get();
            }
        });
    }

    private MyHeader createHeader(byte[] msgBody) {
        MyHeader header = new MyHeader();

        long requestID = Math.abs(UUID.randomUUID().getLeastSignificantBits());

        // 标志位
        header.setFlag(Constant.FLAG_CLIENT);
        header.setDataLen(msgBody.length);
        header.setRequestID(requestID);

        return header;
    }
}

