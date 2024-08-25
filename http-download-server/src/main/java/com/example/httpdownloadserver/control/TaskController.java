package com.example.httpdownloadserver.control;

import com.example.httpdownloadserver.model.Result;
import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class TaskController {
    @Autowired
    private TaskService taskService;
    /**
     * 提交下载任务
     * @param url
     * @return
     */
    public Result<Task> submitDownload(String url) {
        Task task = taskService.submitDownload(url);
        return null;
    }

    /**
     * 重新开始下载任务
     * @param url
     * @return
     */
    public boolean startDownload(String url) {
        return taskService.restartDownload(url);
    }

    /**
     * 暂停下载任务
     * @param url
     * @return
     */
    public boolean pauseDownload(String url) {
        return taskService.pauseDownload(url);
    }

    /**
     * 继续下载任务
     * @param url
     * @return
     */
    public boolean resumeDownload(String url) {
        return taskService.resumeDownload(url);
    }

    /**
     * 取消下载任务
     * @param url
     * @return
     */
    public boolean cancelDownload(String url) {
        return taskService.cancelDownload(url);
    }
}
