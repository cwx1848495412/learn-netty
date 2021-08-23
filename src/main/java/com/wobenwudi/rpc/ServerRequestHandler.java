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
        ByteBuf buf = (ByteBuf) msg;
        ByteBuf sendBuf = buf.copy();

        int large = Constant.LARGE;

        // 读协议头
        if (buf.readableBytes() >= large) {
            byte[] bytes = new byte[large];
            buf.readBytes(bytes);

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream oin = new ObjectInputStream(in);
            MyHeader header = (MyHeader) oin.readObject();

            System.out.println("server response header ID: " + header.getRequestID());

            if (buf.readableBytes() >= header.getDataLen()) {

                byte[] data = new byte[(int) header.getDataLen()];
                buf.readBytes(data);

                ByteArrayInputStream din = new ByteArrayInputStream(data);
                ObjectInputStream doin = new ObjectInputStream(din);
                MyContent content = (MyContent) doin.readObject();

                System.out.println(content.getName());

            }


        }


        ChannelFuture channelFuture = ctx.writeAndFlush(sendBuf);
        channelFuture.sync();
    }

}
