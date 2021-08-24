package com.wobenwudi.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerRequestHandler extends ChannelInboundHandlerAdapter {

    // provider
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PackageMsg packageMsg = (PackageMsg) msg;
//        System.out.println("server handler : " +
//                packageMsg.getContent().getMethodName() +
//                packageMsg.getContent().getArgs()[0]
//        );

        // TODO:: dispatcher 分发请求给不同路由对应方法

        // 假设处理了用户请求 要给客户端返回了~
        ctx.writeAndFlush("".getBytes());

        // RPC 持有request ID
        // client 端解决解码问题
        // 关注之前定义的通信协议
        // 来时的flag 是 1 << 0
        // 此时可以重新定义 1 << ?
        // 新的header content
        String ioThreadName = Thread.currentThread().getName();
        // 1.直接在当前线程处理IO 和返回
        // 2. 使用netty 自己的event loop 处理业务和返回
//        ctx.executor().execute(() -> {
        // 3. 拿组里其他的
        ctx.executor().parent().next().execute(() -> {

            String execThreadName = Thread.currentThread().getName();
            MyContent content = new MyContent();

            String format = String.format("io thread: %s , " +
                            "exec thread: %s " +
                            "from args: %s ",
                    ioThreadName,
                    execThreadName,
                    packageMsg.getContent().getArgs()[0]
            );
            System.out.println(format);
            content.setResult(format);

            byte[] contentByte = Constant.ser(content);

            MyHeader header = new MyHeader();
            header.setRequestID(packageMsg.getHeader().getRequestID());
            header.setFlag(Constant.FLAG_SERVER);
            header.setDataLen(contentByte.length);

            byte[] headerByte = Constant.ser(header);

            ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT
                    .directBuffer(headerByte.length + contentByte.length);
            byteBuf.writeBytes(headerByte);
            byteBuf.writeBytes(contentByte);
            ctx.writeAndFlush(byteBuf);
        });


    }

}
