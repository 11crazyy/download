package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.model.File;
import com.example.httpdownloadserver.model.SliceStatus;
import com.example.httpdownloadserver.model.ThreadStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskContext {
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>());
    private ConcurrentHashMap<Thread, ThreadStatus> threadStatusMap;
    private Map<Integer, SliceStatus> sliceStatusMap;
    private String path;

    public TaskContext() {
    }

}
