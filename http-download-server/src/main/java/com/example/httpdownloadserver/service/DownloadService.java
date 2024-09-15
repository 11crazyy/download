package com.example.httpdownloadserver.service;

import com.example.httpdownloadserver.model.Task;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface DownloadService {
    /**
     * 下载文件
     *
     * @param task
     * @return
     */
    void download(Task task, int threadNum, boolean isPaused) throws IOException;

    /**
     * 根据id获得SseEmitter
     *
     * @param taskId
     * @return
     */

    SseEmitter getEmitter(String taskId);
}
