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
import java.util.*;
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
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 10, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>());//任务下载线程池，最多同时下载4个文件
    private final ConcurrentHashMap<Long, Future<?>> taskFutures = new ConcurrentHashMap<>();

    @Override
    public Task submitDownload(String url) {
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
        taskDO.setThreadCount(Integer.parseInt(threadNum));
        taskDO.setStatus(String.valueOf(TaskStatus.PENDING));
        taskDAO.insert(taskDO);
        Task task = PowerConverter.convert(taskDO, Task.class);
        task.setStatus(TaskStatus.valueOf(taskDO.getStatus()));
        processTasks(task);
        return task;
    }

    private void processTasks(Task task) {
        Future<?> future = executor.submit(() -> {
            downloadService.download(task);
        });
        taskFutures.put(task.getId(), future);
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
            processTasks(task);
            return true;
        }
        return false;
    }

    @Override
    public boolean pauseDownload(Long id) {
        downloadService.pauseTask(id);
        return true;
    }

    @Override
    public boolean resumeDownload(Long id) {
        downloadService.resumeTask(id);
        return true;
    }

    @Override
    public boolean cancelDownload(Long id) {
        Future<?> future = taskFutures.get(id);
        if (future != null) {
            boolean cancel = future.cancel(true);//取消下载任务
            if (!cancel) {
                LOGGER.error("task cancel failed:" + id);
                return false;
            }
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
        return downloadService.updateThreadNum(taskId, threadNum);
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
