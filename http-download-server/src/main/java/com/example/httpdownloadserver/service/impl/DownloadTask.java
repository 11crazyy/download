package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.dao.TaskDAO;
import com.example.httpdownloadserver.model.DownloadProgress;
import com.example.httpdownloadserver.model.Task;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadTask implements Runnable {
    Logger logger = (Logger) LogManager.getLogger(DownloadTask.class);
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

    public DownloadTask(Task task, String destination, Long startIndex, Long endIndex, AtomicLong bytesDownloaded, Long totalFileSize, AtomicInteger currentSlice, TaskDAO taskDAO, RateLimiter rateLimiter,SseEmitter emitter) {
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
    }

    //任务逻辑：下载任务 计算剩余时间 下载进度 以及下载速度
    @Override
    public void run() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
            connection.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);//设置下载范围
            connection.connect();
            InputStream inputStream = connection.getInputStream();//从连接中读取下载的数据
            RandomAccessFile raf = new RandomAccessFile(destination, "rw");//随机访问文件 将下载的数据写入到目标文件的特定位置
            raf.seek(startIndex);//指定从哪个位置开始写入数据
            //读取并写入数据
            byte[] buffer = new byte[1024];
            int bytesRead;
            long startTime = System.currentTimeMillis();//开始时间
            while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
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
                logger.info(String.format("下载进度：%.2f%%, 下载速度：%.2fKB/s, 剩余时间：%.2f秒\n", progress, downloadSpeed / 1024, remainingTime));
                emitter.send(new DownloadProgress((int) progress, downloadSpeed / 1024, (long) remainingTime, task.getDownloadPath()));
            }
            emitter.complete();
            //关闭流
            raf.close();
            inputStream.close();
            logger.info("索引为 " + currentSlice + " 的切片下载完成,该切片字节范围为：" + startIndex + " - " + endIndex);
            connection.disconnect();
            //分片下载完成 更新currentSlice和数据库
            currentSlice.incrementAndGet();
            task.setCurrentSlice(currentSlice.get());
            taskDAO.updateById(task.getId());
        } catch (IOException e) {
            emitter.completeWithError(e);
            throw new RuntimeException(e);
        }
    }
}

