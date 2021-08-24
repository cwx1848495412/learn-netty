package com.wobenwudi.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一个consumer 可以连接很多的provider
 * <p>
 * 每个provider都有自己的pool
 */
public class ClientFactory {

    private int poolSize = 1;
    private Random random = new Random();
    private NioEventLoopGroup clientWorker;

    private final ConcurrentHashMap<InetSocketAddress, ClientPool> outBoxs = new ConcurrentHashMap<>();


    private static final ClientFactory factory;

    static {
        factory = new ClientFactory();
    }

    public static ClientFactory getFactory() {
        return factory;
    }

    private ClientFactory() {

    }


    public synchronized NioSocketChannel getClient(InetSocketAddress address) {
        ClientPool clientPool = outBoxs.get(address);

        if (clientPool == null) {
            outBoxs.putIfAbsent(address, new ClientPool(poolSize));
            clientPool = outBoxs.get(address);
        }

        // 池里面去做负载均衡--暂时没做
        int i = random.nextInt(poolSize);
        NioSocketChannel[] clients = clientPool.getClients();
        NioSocketChannel client = clients[i];
        if (client != null && client.isActive()) {
            return client;
        }

        Object[] locks = clientPool.getLock();

        synchronized (locks[i]) {
            return clients[i] = create(address);
        }
    }

    /**
     * 基于netty 的客户端
     *
     * @param address
     * @return
     */
    private NioSocketChannel create(InetSocketAddress address) {
        clientWorker = new NioEventLoopGroup(1);

        Bootstrap bs = new Bootstrap();
        ChannelFuture connect = bs.group(clientWorker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ServerDecode());
                        pipeline.addLast(new ClientResponsesHandler());
                    }
                }).connect(address);

        try {
            NioSocketChannel client = (NioSocketChannel) connect.sync().channel();

            return client;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }


}
