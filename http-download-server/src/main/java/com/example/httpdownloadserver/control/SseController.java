package com.example.httpdownloadserver.control;

import com.example.httpdownloadserver.dao.TaskDAO;
import com.example.httpdownloadserver.dataobject.TaskDO;
import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class SseController {
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private TaskDAO taskDAO;

    @GetMapping("/download/sse")
    public void sendSseMessage(String taskId) throws IOException {
        TaskDO taskDO = taskDAO.selectById(Integer.parseInt(taskId));
        Task task = taskDO.toModel();
        downloadService.download(task, task.getDownloadThread(),false);
    }
}
