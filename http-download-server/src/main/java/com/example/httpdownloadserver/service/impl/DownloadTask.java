package com.example.httpdownloadserver.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask implements Runnable {
    Logger logger = (Logger) LogManager.getLogger(DownloadTask.class);

    private String fileUrl;
    private String destination;
    private Long startIndex;//开始下载位置
    private Long endIndex;//结束下载位置

    public DownloadTask(String fileUrl, String destination, Long startIndex, Long endIndex) {
        this.fileUrl = fileUrl;
        this.destination = destination;
        this.endIndex = endIndex;
        this.startIndex = startIndex;
    }

    //任务逻辑：下载任务
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
            while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
                raf.write(buffer, 0, bytesRead);
            }
            //关闭流
            raf.close();
            inputStream.close();
            logger.info("下载完成：" + startIndex + "-" + endIndex);
            connection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

