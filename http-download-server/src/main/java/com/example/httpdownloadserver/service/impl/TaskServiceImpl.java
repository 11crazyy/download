package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.dao.TaskDAO;
import com.example.httpdownloadserver.dataobject.SettingsDO;
import com.example.httpdownloadserver.dataobject.TaskDO;
import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.model.TaskStatus;
import com.example.httpdownloadserver.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskDAO taskDAO;
    @Autowired
    private SettingsDAO settingsDAO;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);//线程池，最多同时下载4个文件
    private final BlockingDeque<Task> taskDeque = new LinkedBlockingDeque<>();//线程安全，支持阻塞操作，适合生产者-消费者模式

    private final ConcurrentHashMap<Long,Future<?>> taskFutures = new ConcurrentHashMap<>();//Future<?>用于表示任务的状态，ConcurrentHashMap用于存储任务状态
    @Override
    public Task submitDownload(String url) {
        //将下载任务存入数据库
        TaskDO taskDO = new TaskDO();
        taskDO.setDownloadLink(url);
        taskDO.setDownloadPath(settingsDAO.selectByName("downloadPath").getSettingValue());
        taskDO.setDownloadThread(Integer.parseInt(settingsDAO.selectByName("threadCount").getSettingValue()));
        taskDO.setStatus(String.valueOf(TaskStatus.Pending));
        taskDAO.insert(taskDO);
        //将任务信息存到下载队列中
        taskDeque.offer(taskDO.toModel());
        //启动任务处理，从队列中取出任务并启动下载过程
        processTasks();
        return taskDO.toModel();
    }
    private void processTasks(){
//        while (!taskDeque.isEmpty()){
//            Task task = taskDeque.poll();
//            if (task.getStatus() == TaskStatus.Pending){
//                Future<?> future = executor.submit(task);
//                taskFutures.put(task.getId(),future);
//            }
//        }
    }

    @Override
    public boolean restartDownload(Long id) {
        //重新开始下载，先取消下载，再重新提交下载任务
        if (cancelDownload(id)){
            Task task = taskDAO.selectById(id).toModel();
            task.setStatus(TaskStatus.Pending);
            taskDeque.offer(task);
            processTasks();
        }
        return false;
    }

    @Override
    public boolean pauseDownload(Long id) {
        //利用future.cancel暂停下载
        Future<?> future = taskFutures.get(id);
        if (future != null){
            future.cancel(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean resumeDownload(Long id) {
        //得到已经下载的分片数字，继续下载
//        Task task = taskDAO.selectById(id).toModel();
//        if (task.getStatus() == TaskStatus.Pending){
//            Future<?> future = executor.submit(task);
//            taskFutures.put(task.getId(),future);
//            return true;
//        }
       return false;
    }

    @Override
    public boolean cancelDownload(Long id) {
        Future<?> future = taskFutures.get(id);
        if (future != null){
            future.cancel(true);
            taskFutures.remove(id);
            taskDAO.deleteById(id);
            return true;
        }
        return false;
    }
}
