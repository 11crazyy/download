package com.example.httpdownloadserver.service;

import com.example.httpdownloadserver.model.Task;

public interface TaskService {
    /**
     * 提交下载任务
     * @param url
     * @return
     */
    Task submitDownload(String url);
    /**
     * 重新开始下载任务
     * @param taskId
     * @return
     */
    boolean restartDownload(Long taskId);

    /**
     * 暂停下载任务
     * @param taskId
     * @return
     */
    boolean pauseDownload(Long taskId);

    /**
     * 继续下载任务
     * @param taskId
     * @return
     */
    boolean resumeDownload(Long taskId);

    /**
     * 取消下载任务
     * @param taskId
     * @return
     */
    boolean cancelDownload(Long taskId);

}
