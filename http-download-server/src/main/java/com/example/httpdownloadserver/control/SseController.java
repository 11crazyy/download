package com.example.httpdownloadserver.control;

import com.example.httpdownloadserver.common.PowerConverter;
import com.example.httpdownloadserver.dao.TaskDAO;
import com.example.httpdownloadserver.dataobject.TaskDO;
import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class SseController {
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private TaskDAO taskDAO;

}
