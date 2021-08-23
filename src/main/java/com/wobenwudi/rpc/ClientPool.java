package com.wobenwudi.rpc;

import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientPool {

    private NioSocketChannel[] clients;

    /**
     * 伴生锁
     */
    private Object[] lock;

    public ClientPool(int size) {
        // 连接是空的
        clients = new NioSocketChannel[size];

        lock = new Object[size];

        // 初始化 伴生锁
        for (int i = 0; i < lock.length; i++) {
            lock[i] = new Object();
        }
    }


    public NioSocketChannel[] getClients() {
        return clients;
    }

    public void setClients(NioSocketChannel[] clients) {
        this.clients = clients;
    }

    public Object[] getLock() {
        return lock;
    }
}
