package com.example.httpdownloadserver.control;

import com.example.httpdownloadserver.model.Result;
import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.param.ThreadUpdateParam;
import com.example.httpdownloadserver.service.TaskService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class TaskController {
    @Autowired
    private TaskService taskService;
    private static final Logger LOGGER = LogManager.getLogger(TaskController.class);
    /**
     * 提交下载任务
     * @param url
     * @return
     */
    @PostMapping("/task/submit")
    @ResponseBody
    public Result<Task> submitDownload(@RequestBody String url) {
        Result<Task> result = new Result<>();
        Task task = taskService.submitDownload(url);
        if (task != null) {
            result.setSuccess(true);
            result.setData(task);
            LOGGER.info("submit task success");
        } else {
            result.setSuccess(false);
            LOGGER.warn("submit task failed");
        }
        return result;
    }

    /**
     * 重新开始下载任务
     * @param id
     * @return
     */
    @PostMapping("/task/restart")
    @ResponseBody
    public Result<Boolean> restartDownload(@RequestBody Long id) {
        Result<Boolean> result = new Result<>();
        boolean success = taskService.restartDownload(id);
        if (success) {
            result.setSuccess(true);
            LOGGER.info("restart task success");
        } else {
            result.setSuccess(false);
            LOGGER.warn("restart task failed");
        }
        return result;
    }

    /**
     * 暂停下载任务
     * @param id
     * @return
     */
    @PostMapping("/task/pause")
    @ResponseBody
    public Result<Boolean> pauseDownload(@RequestBody Long id) {
        Result<Boolean> result = new Result<>();
        boolean success = taskService.pauseDownload(id);
        if (success) {
            result.setSuccess(true);
            LOGGER.info("pause task success");
        } else {
            result.setSuccess(false);
            LOGGER.warn("pause task failed");
        }
        return result;
    }

    /**
     * 继续下载任务
     * @param id
     * @return
     */
    @PostMapping("/task/resume")
    @ResponseBody
    public Result<Boolean> resumeDownload(@RequestBody Long id) {
        Result<Boolean> result = new Result<>();
        boolean success = taskService.resumeDownload(id);
        if (success) {
            result.setSuccess(true);
            LOGGER.info("resume task success");
        } else {
            result.setSuccess(false);
            LOGGER.warn("resume task failed");
        }
        return result;
    }

    /**
     * 所选文件取消下载
     * @param id
     * @return
     */
    @PostMapping("/task/cancel")
    @ResponseBody
    public Result<Boolean> cancelDownload(@RequestBody Long id) {
        Result<Boolean> result = new Result<>();
        boolean success = taskService.cancelDownload(id);
        if (success) {
            result.setSuccess(true);
            LOGGER.info("cancel task success");
        } else {
            result.setSuccess(false);
            LOGGER.warn("cancel task failed");
        }
        return result;
    }
    /**
     * 所选文件重新开始下载
     * @param ids
     * @return
     */
    @PostMapping("/tasks/restart")
    @ResponseBody
    public Result<Boolean> startDownloads(@RequestBody List<Long> ids) {
        Result<Boolean> result = new Result<>();
        boolean success = taskService.restartDownloads(ids);
        if (success) {
            result.setSuccess(true);
            LOGGER.info("restart tasks success");
        } else {
            result.setSuccess(false);
            LOGGER.warn("restart tasks failed");
        }
        return result;
    }

    /**
     * 所选文件暂停下载
     * @param ids
     * @return
     */
    @PostMapping("/tasks/pause")
    @ResponseBody
    public boolean pauseDownloads(@RequestBody List<Long> ids) {
        return false;
    }

    /**
     * 所选文件继续下载
     * @param ids
     * @return
     */
    @PostMapping("/tasks/resume")
    @ResponseBody
    public boolean resumeDownloads(@RequestBody List<Long> ids) {
        return false;
    }

    /**
     * 所选文件取消下载
     * @param ids
     * @return
     */
    @PostMapping("/tasks/cancel")
    @ResponseBody
    public boolean cancelDownloads(@RequestBody List<Long> ids) {
        return false;
    }

    /**
     * 获取线程数
     * @return
     */
    @PostMapping("/thread/update")
    @ResponseBody
    public Result<Integer> updateThread(@RequestBody ThreadUpdateParam param) {
        Result<Integer> result = new Result<>();
        result.setData(taskService.updateThreadCount(param.getTaskId(), param.getThreadNum()));
        return result;
    }

    /**
     * 根据下载状态筛选任务
     * @param status
     * @return
     */
    public List<Task> filterTasks(@RequestBody String status) {
        return null;
    }
}
