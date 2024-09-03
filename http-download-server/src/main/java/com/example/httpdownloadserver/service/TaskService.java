package com.example.httpdownloadserver.service;

import com.example.httpdownloadserver.model.Task;

import java.util.List;

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

    /**
     * 所选列表重新开始下载
     * @param taskIds
     * @return
     */
    boolean restartDownloads(List<Long> taskIds);

    /**
     * 所选列表取消下载
     * @param taskIds
     * @return
     */
    boolean cancelDownloads(List<Long> taskIds);

    /**
     * 所选列表暂停下载
     * @param taskIds
     * @return
     */
    boolean pauseDownloads(List<Long> taskIds);

    /**
     * 所选列表继续下载
     * @param taskIds
     * @return
     */
    boolean resumeDownloads(List<Long> taskIds);

    /**
     * 所选列表删除下载
     * @param taskIds
     * @return
     */
    boolean deleteDownloads(List<Long> taskIds);

    /**
     * 更新线程数
     * @return
     */
    int updateThreadCount(Long taskId,int threadNum);

    /**
     * 获取线程数
     * @param taskId
     * @return
     */
    int getThreadCount(Long taskId);
    /**
     * 按下载状态筛选任务
     * @param status
     * @return
     */
    List<Task> listByStatus(String status);



}
