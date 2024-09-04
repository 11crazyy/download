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
    boolean restartDownload(Integer taskId);

    /**
     * 暂停下载任务
     * @param taskId
     * @return
     */
    boolean pauseDownload(Integer taskId);

    /**
     * 继续下载任务
     * @param taskId
     * @return
     */
    boolean resumeDownload(Integer taskId);

    /**
     * 取消下载任务
     * @param taskId
     * @return
     */
    boolean cancelDownload(Integer taskId);

    /**
     * 所选列表重新开始下载
     * @param taskIds
     * @return
     */
    boolean restartDownloads(List<Integer> taskIds);

    /**
     * 所选列表取消下载
     * @param taskIds
     * @return
     */
    boolean cancelDownloads(List<Integer> taskIds);

    /**
     * 所选列表暂停下载
     * @param taskIds
     * @return
     */
    boolean pauseDownloads(List<Integer> taskIds);

    /**
     * 所选列表继续下载
     * @param taskIds
     * @return
     */
    boolean resumeDownloads(List<Integer> taskIds);

    /**
     * 所选列表删除下载
     * @param taskIds
     * @return
     */
    boolean deleteDownloads(List<Integer> taskIds);

    /**
     * 更新线程数
     * @return
     */
    int updateThreadCount(Integer taskId,int threadNum);

    /**
     * 获取线程数
     * @param taskId
     * @return
     */
    int getThreadCount(Integer taskId);
    /**
     * 按下载状态筛选任务
     * @param status
     * @return
     */
    List<Task> listByStatus(String status);



}
