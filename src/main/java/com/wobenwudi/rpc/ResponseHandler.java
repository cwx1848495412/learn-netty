package com.wobenwudi.rpc;

import java.util.concurrent.ConcurrentHashMap;

public class ResponseHandler {
    private static ConcurrentHashMap<Long, Callback> mapping = new ConcurrentHashMap<>();

    public static void addCallback(long requestID, Callback cb) {
        mapping.putIfAbsent(requestID, cb);
    }

    public static void runCallback(long requestId) {
        Callback callback = mapping.get(requestId);
        callback.run();
        removeCallback(requestId);
    }

    private static void removeCallback(long requestId) {
        mapping.remove(requestId);
    }
}
