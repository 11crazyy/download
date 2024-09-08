package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.control.TaskController;
import com.example.httpdownloadserver.dao.TaskDAO;
import com.example.httpdownloadserver.model.DownloadProgress;
import com.example.httpdownloadserver.model.Task;
import com.google.common.util.concurrent.RateLimiter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadTask implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(DownloadTask.class);
    private final String fileUrl;
    private final String destination;
    private final Long startIndex;//开始下载位置
    private final Long endIndex;//结束下载位置
    private final AtomicLong bytesDownloaded;
    private final Long totalFileSize;
    private final Task task;//计算剩余时间等属性
    private final AtomicInteger currentSlice;
    private final TaskDAO taskDAO;
    private final RateLimiter rateLimiter;
    private final SseEmitter emitter;
    private final int sliceNum;
    private static final OkHttpClient client = new OkHttpClient();
    private final Object lock = new Object();//锁 用于同步访问emitter和标志位
    private final AtomicBoolean isEmitterCompleted = new AtomicBoolean(false);//标志位 用于判断emitter是否已经完成

    public DownloadTask(Task task, String destination, Long startIndex, Long endIndex, AtomicLong bytesDownloaded, Long totalFileSize, AtomicInteger currentSlice, TaskDAO taskDAO, RateLimiter rateLimiter, SseEmitter emitter, int sliceNum) {
        this.task = task;
        this.fileUrl = task.getDownloadLink();
        this.destination = destination;
        this.endIndex = endIndex;
        this.startIndex = startIndex;
        this.bytesDownloaded = bytesDownloaded;
        this.totalFileSize = totalFileSize;
        this.currentSlice = currentSlice;
        this.taskDAO = taskDAO;
        this.rateLimiter = rateLimiter;
        this.emitter = emitter;
        this.sliceNum = sliceNum;
    }

    //任务逻辑：下载任务 计算剩余时间 下载进度 以及下载速度
    @Override
    public void run() {
        //构建Okhttp请求，并设置Range请求头
        Request request = new Request.Builder().url(fileUrl).addHeader("Range", "bytes=" + startIndex + "-" + endIndex).build();
        //发送请求并获取响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file:" + response);
            }
            //获取响应体的输入流
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }
            InputStream inputStream = body.byteStream();
            RandomAccessFile raf = new RandomAccessFile(destination, "rw");//随机访问文件 将下载的数据写入到目标文件的特定位置
            raf.seek(startIndex);
            //读取并写入数据
            byte[] buffer = new byte[1024];
            int bytesRead;
            long startTime = System.currentTimeMillis();//开始时间
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                //检查线程是否被中断
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                //控制下载速度
                rateLimiter.acquire(bytesRead);//请求下载所需的令牌
                bytesDownloaded.addAndGet(bytesRead);
                raf.write(buffer, 0, bytesRead);
                //计算下载速度
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                double downloadSpeed = bytesDownloaded.get() / (elapsedTime / 1000.0);
                task.setDownloadSpeed(downloadSpeed / 1024);//单位KB/s
                //计算下载进度
                double progress = (double) bytesDownloaded.get() / totalFileSize * 100;
                task.setDownloadProgress((int) (progress));
                //计算剩余时间
                long remainingBytes = totalFileSize - bytesDownloaded.get();
                double remainingTime = remainingBytes / downloadSpeed;//剩余时间 单位s
                task.setDownloadRemainingTime((long) remainingTime);
                LOGGER.info(String.format("下载进度：%d%%, 下载速度：%.2fKB/s, 剩余时间：%.2f秒,已经下载的字节数：%d字节\n", (int) progress, downloadSpeed / 1024, remainingTime,bytesDownloaded.get()));
                //同步对emitter的访问，确保多线程安全
                synchronized (lock) {
                    if (!isEmitterCompleted.get()) {
                        emitter.send(new DownloadProgress((int) progress, downloadSpeed / 1024, (long) remainingTime, bytesDownloaded.get()));
                    }
                }
            }
            synchronized (lock) {
                if (!isEmitterCompleted.get()) {
                    if (currentSlice.get() == sliceNum) {//整个文件全部下载完成
                        emitter.complete();
                        isEmitterCompleted.set(true);
                    }
                }
            }
            //关闭流
            raf.close();
            inputStream.close();
            LOGGER.info("索引为 " + currentSlice + " 的切片下载完成,该切片字节范围为：" + startIndex + " - " + endIndex);
            //分片下载完成 更新currentSlice和数据库
            currentSlice.incrementAndGet();
            task.setCurrentSlice(currentSlice.get());
            taskDAO.updateById(task.getId());
        } catch (IOException e) {
            synchronized (lock) {
                if (!isEmitterCompleted.get()) {
                    if (e instanceof InterruptedIOException) {//如果是cancel 则不抛出异常
                        LOGGER.info("下载任务被取消:" + e.getMessage());
                        Thread.currentThread().interrupt();
                    } else {
                        LOGGER.error("下载失败: " + e.getMessage(), e);
                        emitter.completeWithError(e);
                        isEmitterCompleted.set(true);
                    }
                }
            }
            if (!(e instanceof InterruptedIOException)) {
                throw new RuntimeException("下载失败", e);
            }
        }
    }
}

