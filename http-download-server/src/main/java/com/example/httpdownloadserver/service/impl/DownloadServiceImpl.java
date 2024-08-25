package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.model.File;
import com.example.httpdownloadserver.service.DownloadService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class DownloadServiceImpl implements DownloadService {
    Logger logger = (Logger) LogManager.getLogger(DownloadServiceImpl.class);

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
        //检查http状态
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            //获取输入流
            InputStream inputStream = connection.getInputStream();
            //创建输出流
            FileOutputStream outputStream = new FileOutputStream(settingsDAO.selectByName("downloadPath").getSettingValue());
            //读取并写入数据
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            //关闭流
            outputStream.close();
            inputStream.close();
            logger.info("fileDownloaded:" + file.getFileName());
        } else {
            logger.error("fileDownloadFailed:" + file.getFileName());
        }
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
