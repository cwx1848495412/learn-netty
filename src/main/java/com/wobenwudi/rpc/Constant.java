package com.wobenwudi.rpc;

public class Constant {
    /**
     * 定义为一号状态
     * 状态位可以用【| 位或】累加 去兼容
     * 参考 nio 包下的 A C R W 各个事件的做法
     */
    public static final int FLAG_ONE = 1 << 0;

    // msgHeader 大小
    public static final int LARGE = 96;
}
