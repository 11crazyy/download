package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.model.File;
import com.example.httpdownloadserver.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DownloadServiceImpl implements DownloadService {

    @Autowired
    private SettingsDAO settingsDAO;

    @Override
    public void download(File file) throws IOException {
        //创建url对象
        URL url = new URL(file.getUrl());
        //打开连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");//发送HEAD请求
        connection.connect();//连接到服务器
        //获取文件大小，单位字节
        long fileSize = connection.getContentLengthLong();
        file.setSize(fileSize);
        //获得切片大小
        int sliceSize = sliceSize(fileSize);
        //线程数
        int threadNum = Integer.parseInt(settingsDAO.selectByName("threadNum").getSettingValue());
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            long startIndex = (long) i * sliceSize;
            long endIndex = (i == threadNum - 1) ? fileSize - 1 : startIndex + sliceSize - 1;
            executor.execute(new DownloadTask(file.getUrl(), settingsDAO.selectByName("downloadPath").getSettingValue(), startIndex, endIndex));//线程逻辑：负责任务调度
        }
        executor.shutdown();
        connection.disconnect();
    }

    public int sliceSize(Long fileSize) {
        if (fileSize <= 100 * 1024 * 1024) {//小于100MB
            return 1024 * 1024;//1MB
        } else if (fileSize <= 1024 * 1024 * 1024) {//小于1GB
            return 10 * 1024 * 1024;//10MB
        } else {//大于1GB
            return 50 * 1024 * 1024;//50MB;
        }
    }

}
