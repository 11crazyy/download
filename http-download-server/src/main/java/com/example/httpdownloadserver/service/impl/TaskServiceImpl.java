package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.common.PowerConverter;
import com.example.httpdownloadserver.control.SettingsController;
import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.dao.TaskDAO;
import com.example.httpdownloadserver.dataobject.SettingsDO;
import com.example.httpdownloadserver.dataobject.TaskDO;
import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.model.TaskStatus;
import com.example.httpdownloadserver.service.DownloadService;
import com.example.httpdownloadserver.service.TaskService;
import org.apache.ibatis.javassist.runtime.Inner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class TaskServiceImpl implements TaskService {
    private static final Logger LOGGER = LogManager.getLogger(TaskServiceImpl.class);
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private TaskDAO taskDAO;
    @Autowired
    private SettingsDAO settingsDAO;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);//任务下载线程池，最多同时下载4个文件
    private final BlockingDeque<Task> taskDeque = new LinkedBlockingDeque<>();//线程安全，支持阻塞操作，适合生产者-消费者模式
    private final ConcurrentHashMap<Long, Future<?>> taskFutures = new ConcurrentHashMap<>();//控制接口 更方便控制任务的暂停、继续、取消
    private final ConcurrentHashMap<Long, AtomicBoolean> taskPaused = new ConcurrentHashMap<>();

    @Override
    public Task submitDownload(String url) {
        //将下载任务存入数据库
        TaskDO taskDO = new TaskDO();
        taskDO.setDownloadLink(url);
        taskDO.setDownloadSpeed((double) 0);
        taskDO.setDownloadRemainingTime(0L);
        taskDO.setDownloadProgress(0);
        String downloadPath = settingsDAO.selectByName("downloadPath").getSettingValue();
        if (downloadPath == null || downloadPath.isEmpty()) {
            downloadPath = System.getProperty("user.dir");
        }
        taskDO.setDownloadPath(downloadPath);
        String threadNum = settingsDAO.selectByName("threadNum").getSettingValue();
        if (threadNum == null || threadNum.isEmpty()) {
            threadNum = "4";
        }
        taskDO.setDownloadThread(Integer.parseInt(threadNum));
        taskDO.setStatus(String.valueOf(TaskStatus.Pending));
        taskDAO.insert(taskDO);
        //将任务信息存到下载队列中
        Task task = PowerConverter.convert(taskDO, Task.class);
        task.setStatus(TaskStatus.valueOf(taskDO.getStatus()));
        taskPaused.put(task.getId(), new AtomicBoolean(false));
        taskDeque.offer(task);
        //启动任务处理，从队列中取出任务并启动下载过程
        processTasks();
        return PowerConverter.convert(taskDO, Task.class);
    }

    private void processTasks() {
        while (!taskDeque.isEmpty()) {
            Task task = taskDeque.poll();
            if (task != null) {
                Future<?> future = executor.submit(() -> {
                    try {
                        downloadService.download(task, task.getDownloadThread(), taskPaused.get(task.getId()));
                    } catch (IOException e) {
                        LOGGER.error("request error", e);
                    }
                });
                //将任务的Future对象保存到ConcurrentHashMap中
                taskFutures.put(task.getId(), future);
            }
        }
    }

    @Override
    public boolean restartDownload(Long id) {
        //重新开始下载，先取消下载，再重新提交下载任务
        if (cancelDownload(id)) {
            TaskDO taskDO = taskDAO.selectById(id);
            if (taskDO == null) {
                LOGGER.error("task not found:" + id);
                throw new RuntimeException("task not found:" + id);
            }
            Task task = PowerConverter.convert(taskDO, Task.class);
            taskDeque.offer(task);
            processTasks();
            return true;
        }
        return false;
    }

    @Override
    public boolean pauseDownload(Long id) {
        taskPaused.computeIfAbsent(id, k -> new AtomicBoolean(false));
        taskPaused.put(id, new AtomicBoolean(true));
        return true;
    }

    @Override
    public boolean resumeDownload(Long id) {
        taskPaused.put(id, new AtomicBoolean(false));
        return true;
    }

    @Override
    public boolean cancelDownload(Long id) {
        Future<?> future = taskFutures.get(id);
        if (future != null) {
            future.cancel(true);//取消下载任务
            //根据taskId获得相应的emitter 并用complete关闭连接
            SseEmitter emitter = downloadService.getEmitter(String.valueOf(id));
            if (emitter != null) {
                emitter.complete();
            }
            taskFutures.remove(id);
            taskDAO.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean restartDownloads(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return false;
        }
        for (Long taskId : taskIds) {
            restartDownload(taskId);
        }
        return true;
    }

    @Override
    public boolean cancelDownloads(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return false;
        }
        for (Long taskId : taskIds) {
            cancelDownload(taskId);
        }
        return true;
    }

    @Override
    public boolean pauseDownloads(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return false;
        }
        for (Long taskId : taskIds) {
            pauseDownload(taskId);
        }
        return true;
    }

    @Override
    public boolean resumeDownloads(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return false;
        }
        for (Long taskId : taskIds) {
            resumeDownload(taskId);
        }
        return true;
    }

    @Override
    public boolean deleteDownloads(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return false;
        }
        for (Long taskId : taskIds) {
            cancelDownload(taskId);
        }
        return true;
    }

    @Override
    public int updateThreadCount(Long taskId, int threadNum) {
        return taskDAO.updateThreadById(taskId, threadNum);
    }

    @Override
    public int getThreadCount(Long taskId) {
        return taskDAO.getThreadById(taskId);
    }

    @Override
    public List<Task> listByStatus(String status) {
        List<TaskDO> taskDOS = taskDAO.selectByStatus(status);
        return PowerConverter.batchConvert(taskDOS, Task.class);
    }
}
