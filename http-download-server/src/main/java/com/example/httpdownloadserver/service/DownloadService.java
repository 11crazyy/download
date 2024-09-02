package com.example.httpdownloadserver.service;

import com.example.httpdownloadserver.model.File;
import com.example.httpdownloadserver.model.Task;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;

import java.io.IOException;

public interface DownloadService {
    /**
     * 下载文件
     * @param task
     * @return
     */
    void download(Task task) throws IOException;

    SseEmitter getEmitter(String taskId);
}
