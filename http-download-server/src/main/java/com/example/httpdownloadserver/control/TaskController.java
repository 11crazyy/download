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
import org.springframework.web.bind.annotation.RequestParam;
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
    public Result<Task> submitDownload(@RequestParam String url) {
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
    public Result<Boolean> restartDownload(@RequestParam Long id) {
        Result<Boolean> result = new Result<>();
        if (id == null) {
            result.setErrorResult(result,"id is null");
            return result;
        }
        boolean success = taskService.restartDownload(id);
        if (success) {
            result.setSuccess(true);
            result.setMessage("restart task success");
            LOGGER.info("restart task success");
        } else {
            result.setSuccess(false);
            result.setCode("600");
            result.setMessage("restart task failed");
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
    public Result<Boolean> pauseDownload(@RequestParam Long id) {
        Result<Boolean> result = new Result<>();
        if (id == null) {
            result.setErrorResult(result,"id is null");
            return result;
        }
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
    public Result<Boolean> resumeDownload(@RequestParam Long id) {
        Result<Boolean> result = new Result<>();
        if (id == null) {
            result.setErrorResult(result,"id is null");
            return result;
        }
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
    public Result<Boolean> cancelDownload(@RequestParam Long id) {
        Result<Boolean> result = new Result<>();
        if (id == null) {
            result.setErrorResult(result,"id is null");
            return result;
        }
        boolean success = taskService.cancelDownload(id);
        if (success) {
            result.setSuccess(true);
            result.setMessage("cancel task success");
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
        if (ids == null) {
            result.setErrorResult(result,"ids are null");
            return result;
        }
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
    public Result<Boolean> pauseDownloads(@RequestBody List<Long> ids) {
        Result<Boolean> result = new Result<>();
        if (ids == null) {
            result.setErrorResult(result,"ids are null");
            return result;
        }
        boolean success = taskService.pauseDownloads(ids);
        if (success) {
            result.setSuccess(true);
            result.setMessage("pause tasks success");
            LOGGER.info("pause tasks success");
        } else {
            result.setSuccess(false);
            result.setMessage("pause tasks failed");
            LOGGER.warn("pause tasks failed");
        }
        return result;
    }
    /**
     * 所选文件继续下载
     * @param ids
     * @return
     */
    @PostMapping("/tasks/resume")
    @ResponseBody
    public Result<Boolean> resumeDownloads(@RequestBody List<Long> ids) {
        Result<Boolean> result = new Result<>();
        if (ids == null) {
            result.setErrorResult(result,"ids are null");
            return result;
        }
        boolean success = taskService.resumeDownloads(ids);
        if (success) {
            result.setSuccess(true);
            LOGGER.info("resume tasks success");
        } else {
            result.setSuccess(false);
            LOGGER.warn("resume tasks failed");
        }
        return result;
    }
    /**
     * 所选文件取消下载
     * @param ids
     * @return
     */
    @PostMapping("/tasks/cancel")
    @ResponseBody
    public Result<Boolean> cancelDownloads(@RequestBody List<Long> ids) {
        Result<Boolean> result = new Result<>();
        if (ids == null) {
            result.setErrorResult(result,"ids are null");
            return result;
        }
        boolean success = taskService.cancelDownloads(ids);
        if (success) {
            result.setSuccess(true);
            result.setMessage("cancel tasks success");
            LOGGER.info("cancel tasks success");
        } else {
            result.setSuccess(false);
            result.setMessage("cancel tasks failed");
            LOGGER.warn("cancel tasks failed");
        }
        return result;
    }
    /**
     * 获取线程数
     * @return
     */
    @PostMapping("/thread/update")
    @ResponseBody
    public Result<Long> updateThread(@RequestBody ThreadUpdateParam param) {
        Result<Long> result = new Result<>();
        if (param == null) {
            result.setErrorResult(result,"param is null");
        }
        int updateRes = taskService.updateThreadCount(param.getTaskId(), param.getThreadNum());
        if (updateRes == 0){
            result.setErrorResult(result,"update thread count failed");
            LOGGER.warn("update thread count failed");
            return result;
        }
        result.setSuccessResult(result,"update thread count success");
        return result;
    }
    /**
     * 根据下载状态筛选任务
     * @param status
     * @return
     */
    @PostMapping("/tasks/filter")
    @ResponseBody
    public Result<List<Task>> filterTasks(@RequestParam("status") String status) {
        Result<List<Task>> listResult = new Result<>();
        if (status == null) {
           listResult.setSuccessResult(listResult,"status is null");
            LOGGER.warn("status is null");
            return listResult;
        }
        listResult.setData(taskService.listByStatus(status));
        listResult.setSuccessResult(listResult,"filter tasks success");
        return listResult;
    }
}
