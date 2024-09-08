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
    public Result<Boolean> restartDownload(@RequestParam Integer id) {
        Result<Boolean> result = new Result<>();
        if (isIdNull(result, id)) return result;
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
    public Result<Boolean> pauseDownload(@RequestParam Integer id) {
        Result<Boolean> result = new Result<>();
        if (isIdNull(result, id)) return result;
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
    public Result<Boolean> resumeDownload(@RequestParam Integer id) {
        Result<Boolean> result = new Result<>();
        if (isIdNull(result, id)) return result;
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
    public Result<Boolean> cancelDownload(@RequestParam Integer id) {
        Result<Boolean> result = new Result<>();
        if (isIdNull(result, id)) return result;
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
    public Result<Boolean> startDownloads(@RequestBody List<Integer> ids) {
        Result<Boolean> result = new Result<>();
        if (isIdsNull(result, ids)) return result;
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
    public Result<Boolean> pauseDownloads(@RequestBody List<Integer> ids) {
        Result<Boolean> result = new Result<>();
        if (isIdsNull(result, ids)) return result;
        boolean success = taskService.pauseDownloads(ids);
        if (success) {
            result.setSuccess(true);
            LOGGER.info("pause tasks success");
        } else {
            result.setSuccess(false);
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
    public Result<Boolean> resumeDownloads(@RequestBody List<Integer> ids) {
        Result<Boolean> result = new Result<>();
        if (isIdsNull(result, ids)) return result;
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
    public Result<Boolean> cancelDownloads(@RequestBody List<Integer> ids) {
        Result<Boolean> result = new Result<>();
        if (isIdsNull(result, ids)) return result;
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
    public Result<Integer> updateThread(@RequestBody ThreadUpdateParam param) {
        Result<Integer> result = new Result<>();
        if (param == null) {
            result.setSuccess(false);
            result.setMessage("param is null");
            LOGGER.warn("param is null");
            return result;
        }
        int updateRes = taskService.updateThreadCount(param.getTaskId(), param.getThreadNum());
        if (updateRes == 0){
            result.setSuccess(false);
            result.setMessage("update thread count failed");
            LOGGER.warn("update thread count failed");
            return result;
        }
        result.setMessage("update thread count success");
        result.setSuccess(true);
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
            listResult.setSuccess(false);
            listResult.setMessage("status is null");
            LOGGER.warn("status is null");
            return listResult;
        }
        listResult.setData(taskService.listByStatus(status));
        listResult.setSuccess(true);
        return listResult;
    }

    public boolean isIdNull(Result<?> result, Integer id) {
        if (id == null) {
            result.setSuccess(false);
            result.setMessage("id is null");
            LOGGER.warn("id is null");
            return true;
        }
        return false;
    }

    public boolean isIdsNull(Result<?> result, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("ids is null");
            LOGGER.warn("ids is null");
            return true;
        }
        return false;
    }
}
