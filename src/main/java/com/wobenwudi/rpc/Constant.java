package com.wobenwudi.rpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Constant {
    /**
     * 定义为一号状态
     * 状态位可以用【| 位或】累加 去兼容
     * 参考 nio 包下的 A C R W 各个事件的做法
     */
    public static final int FLAG_CLIENT = 1 << 0;

    public static final int FLAG_SERVER = 1 << 1;

    // msgHeader 大小
    public static final int LARGE = 96;

    private static ByteArrayOutputStream out = new ByteArrayOutputStream();

    /**
     * 序列化
     *
     * @param msg
     * @return
     */
    public synchronized static byte[] ser(Object msg) {
        out.reset();
        ObjectOutputStream oout = null;
        byte[] msgBody = null;
        try {
            oout = new ObjectOutputStream(out);
            oout.writeObject(msg);
            msgBody = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msgBody;
    }
}
