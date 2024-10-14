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
    void download(Task task);
    /**
     * 根据id获得SseEmitter
     *
     * @param taskId
     * @return
     */
    SseEmitter getEmitter(String taskId);
    void pauseTask(Long taskId);

    int updateThreadNum(Long taskId, int threadNum);

    void resumeTask(Long taskId);
}
