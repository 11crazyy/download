package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.common.PowerConverter;
import com.example.httpdownloadserver.dao.TaskDAO;
import com.example.httpdownloadserver.dataobject.TaskDO;
import com.example.httpdownloadserver.model.DownloadProgress;
import com.example.httpdownloadserver.model.SliceStatus;
import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.model.ThreadStatus;
import com.google.common.util.concurrent.RateLimiter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class DownloadTask implements Runnable {
    private static final int MAX_RETRY_COUNT = 3;
    private static final Logger LOGGER = LogManager.getLogger(DownloadTask.class);
    private final String fileUrl;
    private AtomicLong bytesDownloaded;
    private final Long totalFileSize;
    private final Task task;//计算剩余时间等属性
    private final Map<Integer, SliceStatus> sliceMap;
    private final Map<Long, ThreadStatus> threadMap;
    private final RateLimiter rateLimiter;
    private final SseEmitter emitter;
    private final File progressFile;
    private final int sliceNum;
    private final int sliceSize;
    private static final OkHttpClient client = new OkHttpClient();
    private final Object lock = new Object();//锁 用于同步访问emitter和标志位
    private final AtomicBoolean isEmitterCompleted = new AtomicBoolean(false);//标志位 用于判断emitter是否已经完成

    public DownloadTask(Task task, AtomicLong bytesDownloaded, Long totalFileSize, RateLimiter rateLimiter, SseEmitter emitter, int sliceNum, Map<Integer, SliceStatus> sliceMap, int sliceSize, File progressFile, Map<Long, ThreadStatus> threadMap) {
        this.task = task;
        this.fileUrl = task.getDownloadLink();
        this.bytesDownloaded = bytesDownloaded;
        this.totalFileSize = totalFileSize;
        this.rateLimiter = rateLimiter;
        this.emitter = emitter;
        this.threadMap = threadMap;
        this.sliceNum = sliceNum;
        this.sliceMap = sliceMap;
        this.sliceSize = sliceSize;
        this.progressFile = progressFile;
    }

    //任务逻辑：下载任务 计算剩余时间 下载进度 以及下载速度
    @Override
    public void run() {
        threadMap.put(Thread.currentThread().getId(), ThreadStatus.RUNNING);//线程状态：运行
        Integer sliceIndex;
        while (true) {
            // 检查线程状态
            synchronized (threadMap) {
                if (threadMap.get(Thread.currentThread().getId()) == ThreadStatus.STOPPED) {
                    LOGGER.info("线程被中断");
                    saveProgress();
                    break;
                }
            }
            sliceIndex = claimSlice();
            if (sliceIndex == null) {
                break;
            }
            long endIndex = (sliceIndex == sliceNum - 1) ? totalFileSize - 1 : (long) (sliceIndex + 1) * sliceSize - 1;//如果索引是sliceNum-1
            // 构建 OkHttp 请求，并设置 Range 请求头
            Request request = new Request.Builder().url(fileUrl).addHeader("Range", "bytes=" + sliceIndex * sliceSize + "-" + endIndex).build();
            int retryCount = 0;
            while (retryCount < MAX_RETRY_COUNT) {
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("下载文件失败：" + response);
                    }
                    ResponseBody body = response.body();
                    if (body == null) {
                        throw new IOException("空响应体");
                    }
                    InputStream inputStream = body.byteStream();
                    RandomAccessFile raf = new RandomAccessFile(task.getDownloadPath(), "rw");
                    raf.seek(sliceIndex);
                    // 读取并写入数据
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long startTime = System.currentTimeMillis();
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        synchronized (threadMap) {
                            if (Thread.currentThread().isInterrupted() || threadMap.get(Thread.currentThread().getId()) == ThreadStatus.STOPPED) {
                                LOGGER.info("线程被中断");
                                saveProgress();  // 保存进度
                                return;
                            }
                        }
                        rateLimiter.acquire(bytesRead);
                        bytesDownloaded.addAndGet(bytesRead);
                        raf.write(buffer, 0, bytesRead);
                        // 更新下载进度、速度、剩余时间等
                        DownloadProgress downloadProgress = updateDownloadMetrics(startTime);
                        synchronized (lock) {
                            if (!isEmitterCompleted.get()) {
                                emitter.send(downloadProgress);
                            }
                        }
                    }
                    synchronized (lock) {
                        if (!isEmitterCompleted.get() && sliceIndex == sliceNum) {
                            emitter.complete();
                            isEmitterCompleted.set(true);
                        }
                    }
                    raf.close();
                    inputStream.close();
                    sliceMap.put(sliceIndex, SliceStatus.DOWNLOADED);
                    // 下载完成后，保存进度
                    saveProgress();
                    LOGGER.info("索引为 " + sliceIndex + " 的切片下载完成, 该切片字节范围为：" + sliceIndex * sliceSize + " - " + endIndex);
                    //文件下载完成时删除临时文件
                    if (sliceMap.values().stream().allMatch(status -> status == SliceStatus.DOWNLOADED)) {
                        progressFile.delete();
                    }
                    break;
                } catch (IOException e) {
                    retryCount++;
                    LOGGER.warn("下载失败，正在重试第" + retryCount + "次");
                    if (retryCount == MAX_RETRY_COUNT) {
                        LOGGER.error("分片下载失败，重试次数已达上限");
                        sliceMap.put(sliceIndex, SliceStatus.WAITING);
                        break;
                    }
                }
            }
        }
        threadMap.remove(Thread.currentThread().getId());
    }

    private void saveProgress() {
        // 保存当前进度到 progressFile
        synchronized (progressFile) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(progressFile))) {
                writer.write(String.valueOf(bytesDownloaded.get()));
                for (Map.Entry<Integer, SliceStatus> entry : sliceMap.entrySet()) {
                    writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
                }
            } catch (IOException e) {
                LOGGER.error("写入进度文件失败", e);
            }
        }
    }

    private DownloadProgress updateDownloadMetrics(long startTime) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        double downloadSpeed = bytesDownloaded.get() / (elapsedTime / 1000.0);
        task.setDownloadSpeed(downloadSpeed / 1024); // 单位 KB/s
        double progress = (double) bytesDownloaded.get() / totalFileSize * 100;
        task.setDownloadProgress((int) progress);
        long remainingBytes = totalFileSize - bytesDownloaded.get();
        double remainingTime = remainingBytes / downloadSpeed;  // 单位秒
        task.setDownloadRemainingTime((long) remainingTime);
        return new DownloadProgress((int) progress, downloadSpeed / 1024, (long) remainingTime, bytesDownloaded.get());
    }

    private synchronized Integer claimSlice() {//确保分片的唯一认领
        for (Map.Entry<Integer, SliceStatus> statusEntry : sliceMap.entrySet()) {
            if (statusEntry.getValue() == SliceStatus.WAITING) {
                if (sliceMap.replace(statusEntry.getKey(), SliceStatus.WAITING, SliceStatus.DOWNLOADING)) {
                    return statusEntry.getKey();
                }
            }
        }
        return null;
    }
}

