package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.common.PowerConverter;
import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.dao.TaskDAO;
import com.example.httpdownloadserver.dataobject.TaskDO;
import com.example.httpdownloadserver.model.SliceStatus;
import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.model.TaskStatus;
import com.example.httpdownloadserver.model.ThreadStatus;
import com.example.httpdownloadserver.service.DownloadService;
import com.google.common.util.concurrent.RateLimiter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DownloadServiceImpl implements DownloadService {
    @Autowired
    private SettingsDAO settingsDAO;
    @Autowired
    private TaskDAO taskDAO;
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 20, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    private static final Logger LOGGER = LogManager.getLogger(DownloadServiceImpl.class);
    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final Map<Long, ThreadStatus> threadMap = new ConcurrentHashMap<>();
    private static final Map<Integer, SliceStatus> sliceMap = new ConcurrentHashMap<>();
    private static final AtomicLong downloaded = new AtomicLong(0);
    private static final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).build();

    @Override
    public void download(Task task, int threadNum) throws IOException {
        LOGGER.info("开始下载任务");
        SseEmitter emitter = new SseEmitter();
        emitters.put(task.getId().toString(), emitter);
        //创建一个Request对象
        Request request = new Request.Builder().url(task.getDownloadLink()).build();
        //使用okhttpClient发送请求
        Response response = client.newCall(request).execute();
        try (response) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file:" + response);
            }
            String directoryPath = settingsDAO.selectByName("downloadPath").getSettingValue();
            String fullPath = getString(task, directoryPath);
            //获得正确的路径
            task.setDownloadPath(saveFilePath(directoryPath, fullPath));
        }
        //获取文件大小 单位字节
        String contentLen = response.header("Content-Length");
        if (contentLen == null) {
            throw new IOException("Content len is null");
        }
        Long fileSize = Long.parseLong(contentLen);
        task.setContentLength(fileSize);
        task.setThreadCount(threadNum);
        //获得切片大小
        int sliceSize = sliceSize(fileSize);
        task.setShardSize(sliceSize);
        //切片个数
        int sliceNum = (int) Math.ceil((double) fileSize / sliceSize);
        //rateLimiter实现限速
        String speed = settingsDAO.selectByName("downloadSpeed").getSettingValue();//MB/s
        RateLimiter rateLimiter = RateLimiter.create(Double.parseDouble(speed) * 1024 * 1024);//每秒不超过指定的下载速度对应的字节数
        for (int i = 0; i < sliceNum; i++) {
            sliceMap.put(i, SliceStatus.WAITING);
        }
        TaskDO taskDO = PowerConverter.convert(task, TaskDO.class);
        taskDO.setStatus(TaskStatus.PENDING.toString());
        taskDAO.updateById(taskDO);
        // todo 调度的时候如果需要减少线程，则随机挑选线程，将状态直接改为结束
        File progressFile = new File(task.getDownloadPath() + ".tmp");//记录进度的临时文件
        progressFile.createNewFile();
        for (int i = 0; i < threadNum; i++) {
            executor.submit(new DownloadTask(task, downloaded, fileSize, rateLimiter, emitter, sliceNum, sliceMap, sliceSize, progressFile, threadMap));//线程逻辑：负责任务调度
        }
    }

    private static String getString(Task task, String directoryPath) {
        // 获取原始文件名
        String fileName = task.getDownloadLink().substring(task.getDownloadLink().lastIndexOf("/") + 1);
        // 获取文件扩展名和文件的基础名称
        String baseName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex); // 获取"."后的扩展名
        } else {
            baseName = fileName; // 没有扩展名的情况
        }
        String fullPath = directoryPath + File.separator + fileName;
        File file = new File(fullPath);
        int i = 1;
        // 循环判断文件是否已经存在
        while (file.exists()) {
            // 检查 baseName 中是否已经包含类似 (n) 的后缀，并移除这个后缀
            if (baseName.matches(".*\\(\\d+\\)$")) {
                baseName = baseName.substring(0, baseName.lastIndexOf("("));
            }
            // 生成新的文件名，递增数字
            fileName = baseName + "(" + i + ")" + extension;
            fullPath = directoryPath + File.separator + fileName;
            file = new File(fullPath);
            i++;
        }
        return fullPath;
    }

    @Override
    public SseEmitter getEmitter(String taskId) {
        if (emitters.containsKey(taskId)) {
            return emitters.get(taskId);
        }
        return null;
    }

    @Override
    public void pauseTask(Long taskId) {
        for (Map.Entry<Long, ThreadStatus> entry : threadMap.entrySet()) {
            if (entry.getValue() == ThreadStatus.RUNNING) {
                threadMap.put(entry.getKey(), ThreadStatus.STOPPED);
            }
        }
        for (Map.Entry<Integer, SliceStatus> entry : sliceMap.entrySet()) {
            if (entry.getValue() == SliceStatus.DOWNLOADING) {
                sliceMap.put(entry.getKey(), SliceStatus.WAITING);
            }
        }
    }


    @Override
    public void resumeTask(Long taskId) {
        TaskDO taskDO = taskDAO.selectById(taskId);
        Task task = PowerConverter.convert(taskDO, Task.class);
        task.setStatus(TaskStatus.valueOf(taskDO.getStatus()));
        //根据路径找到进度文件
        File progressFile = new File(task.getDownloadPath() + ".tmp");
        if (progressFile.exists()) {
            synchronized (progressFile) {
                try (BufferedReader reader = new BufferedReader(new FileReader(progressFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        Integer sliceIndex = Integer.parseInt(parts[0]);
                        SliceStatus status = SliceStatus.valueOf(parts[1]);
                        sliceMap.put(sliceIndex, status);
                        if (status == SliceStatus.DOWNLOADED) {
                            downloaded.addAndGet(task.getShardSize());// 恢复已下载的字节数
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("读取进度文件失败", e);
                }
            }
        }
        int sliceNum = (int) Math.ceil((double) task.getContentLength() / task.getShardSize());
        String speed = settingsDAO.selectByName("downloadSpeed").getSettingValue();//MB/s
        RateLimiter rateLimiter = RateLimiter.create(Double.parseDouble(speed) * 1024 * 1024);
        for (int i = 0; i < task.getThreadCount(); i++) {
            executor.submit(new DownloadTask(task, downloaded, task.getContentLength(), rateLimiter, new SseEmitter(), sliceNum, sliceMap, task.getShardSize(), progressFile, threadMap));//线程逻辑：负责任务调度
        }
    }

    public int sliceSize(Long fileSize) {
        if (fileSize <= 100 << 20) { //小于100MB
            return 1 << 20; //1MB
        } else if (fileSize <= 1 << 30) { //小于1GB
            return 10 << 20; //10MB
        } else { //大于1GB
            return 50 << 20; //50MB;
        }
    }

    private String saveFilePath(String directoryPath, String destinationPath) throws IOException {
        File file = new File(destinationPath);
        File file1 = new File(directoryPath, file.getName());
        return file1.getAbsolutePath();
    }
}
