package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.dao.TaskDAO;
import com.example.httpdownloadserver.model.File;
import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.service.DownloadService;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DownloadServiceImpl implements DownloadService {

    @Autowired
    private SettingsDAO settingsDAO;
    @Autowired
    private TaskDAO taskDAO;
    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public void download(Task task, int threadNum) throws IOException {
        SseEmitter emitter = new SseEmitter();
        emitters.put(task.getId().toString(), emitter);
        //创建url对象
        URL url = new URL(task.getDownloadLink());
        //打开连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");//发送HEAD请求
        connection.connect();//连接到服务器
        //获取文件大小，单位字节
        long fileSize = connection.getContentLengthLong();
        //获得切片大小
        int sliceSize = sliceSize(fileSize);
        //切片个数
        int sliceNum = (int) Math.ceil((double) fileSize / sliceSize);
        //rateLimiter实现限速
        RateLimiter rateLimiter = RateLimiter.create(Double.parseDouble(settingsDAO.selectByName("downloadSpeed").getSettingValue()));//每秒不超过指定的下载速度对应的字节数
        AtomicLong downloaded = new AtomicLong(0);
        AtomicInteger currentSlice = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
        int sliceIndex = task.getCurrentSlice();//断点续传
        for (int i = sliceIndex; i < sliceNum; i++) {
            long startIndex = (long) i * sliceSize;
            long endIndex = (i == sliceNum - 1) ? fileSize - 1 : startIndex + sliceSize - 1;
            executor.execute(new DownloadTask(task, settingsDAO.selectByName("downloadPath").getSettingValue(), startIndex, endIndex, downloaded, fileSize, currentSlice, taskDAO, rateLimiter, emitter));//线程逻辑：负责任务调度
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                executor.shutdown();//不再接收新任务 但会让已提交的任务继续执行 直到所有任务完成 新任务不再被接受
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();//尝试停止所有正在执行的任务 返回尚未执行的任务列表 会终端所有正在等待的任务
        }
        connection.disconnect();
    }

    @Override
    public SseEmitter getEmitter(String taskId) {
        if (emitters.containsKey(taskId)) {
            return emitters.get(taskId);
        }
        return null;
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
