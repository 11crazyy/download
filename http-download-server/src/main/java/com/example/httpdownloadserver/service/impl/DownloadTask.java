package com.example.httpdownloadserver.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadTask implements Runnable {
    Logger logger = (Logger) LogManager.getLogger(DownloadTask.class);

    private String fileUrl;
    private String destination;
    private Long startIndex;//开始下载位置
    private Long endIndex;//结束下载位置

    private AtomicLong bytesDownloaded;
    private Long totalFileSize;

    public DownloadTask(String fileUrl, String destination, Long startIndex, Long endIndex, AtomicLong bytesDownloaded,Long totalFileSize) {
        this.fileUrl = fileUrl;
        this.destination = destination;
        this.endIndex = endIndex;
        this.startIndex = startIndex;
        this.bytesDownloaded = bytesDownloaded;
        this.totalFileSize = totalFileSize;
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
                bytesDownloaded.addAndGet(bytesRead);
                raf.write(buffer, 0, bytesRead);
                //计算下载速度
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                double downloadSpeed = bytesDownloaded.get() / (elapsedTime / 1000.0);
                //计算下载进度
                double progress = (double) bytesDownloaded.get() / totalFileSize * 100;
                //计算剩余时间
                long remainingBytes = totalFileSize - bytesDownloaded.get();
                double remainingTime = remainingBytes / downloadSpeed;

                logger.info(String.format("下载进度：%.2f%%, 下载速度：%.2fKB/s, 剩余时间：%.2f秒\n", progress, downloadSpeed / 1024, remainingTime));
            }
            //关闭流
            raf.close();
            inputStream.close();
            logger.info("切片下载完成：" + startIndex + "-" + endIndex);
            connection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

