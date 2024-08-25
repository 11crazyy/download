package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.service.TaskService;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {
    @Override
    public Task submitDownload(String url) {
        return null;
    }

    @Override
    public boolean restartDownload(String url) {
        return false;
    }

    @Override
    public boolean pauseDownload(String url) {
        return false;
    }

    @Override
    public boolean resumeDownload(String url) {
        return false;
    }

    @Override
    public boolean cancelDownload(String url) {
        return false;
    }
}
