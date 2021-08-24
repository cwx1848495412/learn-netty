package com.wobenwudi.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

public class ServerDecode extends ByteToMessageDecoder {

    // 父类又channel read -> byte buf
    // [前老的拼buf decode(); 剩余留存； 对out 遍历]
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {

//        System.out.println("channel start: " + buf.readableBytes());

        int large = Constant.LARGE;

        // 读协议头
        while (buf.readableBytes() >= large) {
            byte[] bytes = new byte[large];
            // 不移动指针 粘包用
            buf.getBytes(buf.readerIndex(), bytes);

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream oin = new ObjectInputStream(in);
            MyHeader header = (MyHeader) oin.readObject();

//            System.out.println("server response header ID: " + header.getRequestID());

            if (buf.readableBytes() >= header.getDataLen()) {
                // 上面get 没有移动指针
                // 需要先移动到body的区域
                buf.readBytes(large);

                byte[] data = new byte[(int) header.getDataLen()];
                buf.readBytes(data);

                ByteArrayInputStream din = new ByteArrayInputStream(data);
                ObjectInputStream doin = new ObjectInputStream(din);


                if (header.getFlag() == Constant.FLAG_CLIENT) {
                    // 客户端发起请求
                    MyContent content = (MyContent) doin.readObject();
                    out.add(new PackageMsg(header, content));
                }

                if (header.getFlag() == Constant.FLAG_SERVER) {
                    // 服务端返回响应
                    MyContent content = (MyContent) doin.readObject();
                    out.add(new PackageMsg(header, content));
                }

            } else {
                break;
            }


        }
    }
}
