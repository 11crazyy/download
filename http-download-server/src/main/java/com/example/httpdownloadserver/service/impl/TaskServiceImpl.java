package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.common.PowerConverter;
import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.dao.TaskDAO;
import com.example.httpdownloadserver.dataobject.SettingsDO;
import com.example.httpdownloadserver.dataobject.TaskDO;
import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.model.TaskStatus;
import com.example.httpdownloadserver.service.DownloadService;
import com.example.httpdownloadserver.service.TaskService;
import org.apache.ibatis.javassist.runtime.Inner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private TaskDAO taskDAO;
    @Autowired
    private SettingsDAO settingsDAO;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);//任务下载线程池，最多同时下载4个文件
    private final BlockingDeque<Task> taskDeque = new LinkedBlockingDeque<>();//线程安全，支持阻塞操作，适合生产者-消费者模式
    private final ConcurrentHashMap<Integer, Future<?>> taskFutures = new ConcurrentHashMap<>();//控制接口 更方便控制任务的暂停、继续、取消

    @Override
    public Task submitDownload(String url) {
        //将下载任务存入数据库
        TaskDO taskDO = new TaskDO();
        taskDO.setDownloadLink(url);
        taskDO.setDownloadSpeed((double) 0);
        taskDO.setDownloadRemainingTime(0L);
        taskDO.setDownloadProgress(0);
        taskDO.setDownloadPath(settingsDAO.selectByName("downloadPath").getSettingValue());
        taskDO.setDownloadThread(Integer.parseInt(settingsDAO.selectByName("threadNum").getSettingValue()));
        taskDO.setStatus(String.valueOf(TaskStatus.Pending));
        taskDAO.insert(taskDO);
        //将任务信息存到下载队列中
        Task task = PowerConverter.convert(taskDO,Task.class);
        task.setStatus(TaskStatus.valueOf(taskDO.getStatus()));
        taskDeque.offer(task);
        //启动任务处理，从队列中取出任务并启动下载过程
        processTasks();
        return taskDO.toModel();
    }

    private void processTasks() {
        while (!taskDeque.isEmpty()) {
            Task task = taskDeque.poll();
            if (task != null) {
                Future<?> future = executor.submit(() -> {
                    try {
                        downloadService.download(task,getThreadCount(task.getId()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                //将任务的Future对象保存到ConcurrentHashMap中
                taskFutures.put(task.getId(), future);
            }
        }
    }

    @Override
    public boolean restartDownload(Integer id) {
        //重新开始下载，先取消下载，再重新提交下载任务
        if (cancelDownload(id)) {
            Task task = taskDAO.selectById(id).toModel();
            taskDeque.offer(task);
            processTasks();
        }
        return false;
    }

    @Override
    public boolean pauseDownload(Integer id) {
        //利用future.cancel暂停下载
        Future<?> future = taskFutures.get(id);
        if (future != null) {
            future.cancel(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean resumeDownload(Integer id) {
        //得到已经下载的分片数字，继续下载
        Task task = taskDAO.selectById(id).toModel();
        int sliceIndex = task.getCurrentSlice();
        Future<?> future = executor.submit(() -> {
            try {
                downloadService.download(task,getThreadCount(task.getId()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        taskFutures.put(task.getId(), future);
        return true;
    }

    @Override
    public boolean cancelDownload(Integer id) {
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
    public boolean restartDownloads(List<Integer> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return false;
        }
        for (Integer taskId : taskIds) {
            restartDownload(taskId);
        }
        return true;
    }

    @Override
    public boolean cancelDownloads(List<Integer> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return false;
        }
        for (Integer taskId : taskIds) {
            cancelDownload(taskId);
        }
        return true;
    }

    @Override
    public boolean pauseDownloads(List<Integer> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return false;
        }
        for (Integer taskId : taskIds) {
            pauseDownload(taskId);
        }
        return true;
    }

    @Override
    public boolean resumeDownloads(List<Integer> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return false;
        }
        for (Integer taskId : taskIds) {
            resumeDownload(taskId);
        }
        return true;
    }

    @Override
    public boolean deleteDownloads(List<Integer> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return false;
        }
        for (Integer taskId : taskIds) {
            cancelDownload(taskId);
        }
        return true;
    }

    @Override
    public int updateThreadCount(Integer taskId,int threadNum) {
        return taskDAO.updateThreadById(taskId,threadNum);
    }

    @Override
    public int getThreadCount(Integer taskId) {
        return taskDAO.getThreadById(taskId);
    }

    @Override
    public List<Task> listByStatus(String status) {
        List<TaskDO> taskDOS = taskDAO.selectByStatus(status);
        List<Task> tasks = new ArrayList<>();
        for (TaskDO taskDO : taskDOS) {
            tasks.add(taskDO.toModel());
        }
        return tasks;
    }
}
