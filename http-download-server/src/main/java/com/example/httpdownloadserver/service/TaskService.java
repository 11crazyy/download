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
     * @param url
     * @return
     */
    boolean restartDownload(String url);

    /**
     * 暂停下载任务
     * @param url
     * @return
     */
    boolean pauseDownload(String url);

    /**
     * 继续下载任务
     * @param url
     * @return
     */
    boolean resumeDownload(String url);

    /**
     * 取消下载任务
     * @param url
     * @return
     */
    boolean cancelDownload(String url);

}
