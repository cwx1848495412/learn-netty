package com.wobenwudi.rpc;

import java.io.Serializable;

/**
 * 通信协议
 * 1.
 * 2. UUID
 * 3. DATE_LEN
 */
public class MyHeader implements Serializable {

    /**
     * 32bit 可设置状态很多
     */
    private int flag;

    private long requestID;

    private long dataLen;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public long getRequestID() {
        return requestID;
    }

    public void setRequestID(long requestID) {
        this.requestID = requestID;
    }

    public long getDataLen() {
        return dataLen;
    }

    public void setDataLen(long dataLen) {
        this.dataLen = dataLen;
    }
}
