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

import java.io.*;
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
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 20, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>());

    private static final Logger LOGGER = LogManager.getLogger(DownloadServiceImpl.class);
    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final Map<Long, ConcurrentHashMap<Long, ThreadStatus>> threadMap = new ConcurrentHashMap<>();
    private static final Map<Long, ConcurrentHashMap<Integer, SliceStatus>> sliceMap = new ConcurrentHashMap<>();
    private static final AtomicLong downloaded = new AtomicLong(0);
    private static final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).build();

    @Override
    public void download(Task task) {
        LOGGER.info("开始下载任务");
        SseEmitter emitter = new SseEmitter();
        emitters.put(task.getId().toString(), emitter);
        Request request = new Request.Builder().head().url(task.getDownloadLink()).build();
        long fileSize;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file:" + response);
            }
            String directoryPath = settingsDAO.selectByName("downloadPath").getSettingValue();
            String fullPath = getFullPath(task, directoryPath);
            task.setDownloadPath(saveFilePath(directoryPath, fullPath));//保存文件下载路径
            String contentLen = response.header("Content-Length");//文件大小 单位字节
            fileSize = Long.parseLong(contentLen);
        } catch (IOException e) {
            LOGGER.error("下载文件失败", e);
            return;
        }
        task.setContentLength(fileSize);
        int sliceSize = sliceSize(fileSize);//根据文件大小确定切片大小
        task.setShardSize(sliceSize);
        int sliceNum = (int) Math.ceil((double) fileSize / sliceSize);
        String speed = settingsDAO.selectByName("downloadSpeed").getSettingValue();//MB/s
        RateLimiter rateLimiter = RateLimiter.create(Double.parseDouble(speed) * 1024 * 1024);//每秒不超过指定的下载速度对应的字节数
        ConcurrentHashMap<Integer, SliceStatus> map = new ConcurrentHashMap<>();
        for (int i = 0; i < sliceNum; i++) {
            map.put(i, SliceStatus.WAITING);//初始化切片状态
        }
        sliceMap.put(task.getId(), map);
        threadMap.put(task.getId(), new ConcurrentHashMap<>());
        TaskDO taskDO = PowerConverter.convert(task, TaskDO.class);
        taskDO.setStatus(TaskStatus.PENDING.toString());
        taskDAO.updateById(taskDO);
        File progressFile = new File(task.getDownloadPath() + ".tmp");//记录进度的临时文件
        try {
            progressFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < task.getThreadCount(); i++) {
            executor.submit(new DownloadTask(task, downloaded, fileSize, rateLimiter, emitter, sliceNum, sliceMap, sliceSize, progressFile, threadMap,taskDAO));//线程逻辑：负责任务调度
            if (downloaded.get() == fileSize) {
                LOGGER.info("文件下载完成");
                taskDO.setStatus(TaskStatus.DOWNLOADED.toString());
                taskDAO.updateById(taskDO);
                progressFile.delete();
            }
        }
    }

    private static String getFullPath(Task task, String directoryPath) {
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
        ConcurrentHashMap<Long, ThreadStatus> threadStatusMap = threadMap.get(taskId);
        if (threadStatusMap != null) {
            for (Map.Entry<Long, ThreadStatus> entry : threadStatusMap.entrySet()) {
                if (entry.getValue() == ThreadStatus.RUNNING) {
                    threadStatusMap.put(entry.getKey(), ThreadStatus.STOPPED);
                }
            }
        }
        ConcurrentHashMap<Integer, SliceStatus> sliceStatusMap = sliceMap.get(taskId);
        if (sliceStatusMap != null) {
            for (Map.Entry<Integer, SliceStatus> entry : sliceStatusMap.entrySet()) {
                if (entry.getValue() == SliceStatus.DOWNLOADING) {
                    sliceStatusMap.put(entry.getKey(), SliceStatus.WAITING); // 重置下载中的切片状态
                }
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
                        sliceMap.get(taskId).put(sliceIndex, status);
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
            executor.submit(new DownloadTask(task, downloaded, task.getContentLength(), rateLimiter, new SseEmitter(), sliceNum, sliceMap, task.getShardSize(), progressFile, threadMap,taskDAO));//线程逻辑：负责任务调度
        }
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOGGER.error("下载任务失败", e);
        }
    }

    @Override
    public int updateThreadNum(Long taskId, int threadNum) {
        LOGGER.info("更新线程数为" + threadNum);
        //如果要更新的线程数少于当前线程数则把多余线程状态改为STOPPED 如果要更新的线程数大于当前线程数 则增加线程
        TaskDO taskDO = taskDAO.selectById(taskId);
        int currentNum = taskDO.getThreadCount();
        if (threadNum < currentNum) {
            AtomicInteger count = new AtomicInteger(currentNum - threadNum);
            //如果状态为running的线程少于count
            threadMap.get(taskId).entrySet().stream().filter(entry -> entry.getValue() == ThreadStatus.RUNNING)
                    .limit(count.get())
                    .forEach(enter -> {
                        threadMap.get(taskId).put(enter.getKey(), ThreadStatus.STOPPED);
                        count.getAndDecrement();
                    });
        } else if (threadNum > currentNum) {
            for (int i = 0;i < threadNum - currentNum;i++){
                Task task = PowerConverter.convert(taskDO,Task.class);
                task.setStatus(TaskStatus.valueOf(taskDO.getStatus()));
                int sliceNum = (int) Math.ceil((double) task.getContentLength() / task.getShardSize());
                String speed = settingsDAO.selectByName("downloadSpeed").getSettingValue();//MB/s
                RateLimiter rateLimiter = RateLimiter.create(Double.parseDouble(speed) * 1024 * 1024);
                File progressFile = new File(task.getDownloadPath() + ".tmp");
                executor.submit(new DownloadTask(task, downloaded, task.getContentLength(), rateLimiter, new SseEmitter(), sliceNum, sliceMap, task.getShardSize(), progressFile, threadMap,taskDAO));//线程逻辑：负责任务调度
            }
        }
        return 1;
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
