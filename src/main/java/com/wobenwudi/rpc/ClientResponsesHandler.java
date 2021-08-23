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
        ByteBuf buf = (ByteBuf) msg;

        int large = Constant.LARGE;
        // 读协议头
        if (buf.readableBytes() >= large) {
            byte[] bytes = new byte[large];
            buf.readBytes(bytes);

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream oin = new ObjectInputStream(in);
            MyHeader header = (MyHeader) oin.readObject();

            System.out.println("client response header ID: "+header.getRequestID());
            // TODO:: 粘包拆包 找ID
            ResponseHandler.runCallback(header.getRequestID());

//            if (buf.readableBytes() >= header.getDataLen()) {
//
//                byte[] data = new byte[(int) header.getDataLen()];
//                buf.readBytes(data);
//
//                ByteArrayInputStream din = new ByteArrayInputStream(bytes);
//                ObjectInputStream doin = new ObjectInputStream(in);
//                MyContent content = (MyContent) doin.readObject();
//
//                System.out.println(content.getName());
//
//            }


        }

    }
}
