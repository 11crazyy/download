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
     * @param id
     * @return
     */
    public boolean startDownload(Long id) {
        return taskService.restartDownload(id);
    }

    /**
     * 暂停下载任务
     * @param id
     * @return
     */
    public boolean pauseDownload(Long id) {
        return taskService.pauseDownload(id);
    }

    /**
     * 继续下载任务
     * @param id
     * @return
     */
    public boolean resumeDownload(Long id) {
        return taskService.resumeDownload(id);
    }

    /**
     * 取消下载任务
     * @param id
     * @return
     */
    public boolean cancelDownload(Long id) {
        return taskService.cancelDownload(id);
    }
}
