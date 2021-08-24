package com.wobenwudi.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseHandler {
    private static ConcurrentHashMap<Long, CompletableFuture> mapping = new ConcurrentHashMap<>();

    public static void addCallback(long requestID, CompletableFuture cb) {
        mapping.putIfAbsent(requestID, cb);
    }

    public static void runCallback(PackageMsg packageMsg) {
        CompletableFuture callback = mapping.get(packageMsg.getHeader().getRequestID());
        callback.complete(packageMsg.getContent().getResult());
        removeCallback(packageMsg.getHeader().getRequestID());
    }

    private static void removeCallback(long requestId) {
        mapping.remove(requestId);
    }
}
