package com.example.httpdownloadserver.dataobject;

import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.model.TaskStatus;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class TaskDO {
    private Long id;
    private String status;
    private Double downloadSpeed;
    private int downloadProgress;
    private Long downloadRemainingTime;
    private int downloadThread;
    private String downloadPath;
    private String downloadLink;
    private Timestamp gmtCreated;
    private Timestamp gmtModified;

    public TaskDO(Task task) {
        this.id = task.getId();
        this.status = String.valueOf(task.getStatus());
        this.downloadSpeed = task.getDownloadSpeed();
        this.downloadProgress = task.getDownloadProgress();
        this.downloadRemainingTime = task.getDownloadRemainingTime();
        this.downloadThread = task.getDownloadThread();
        this.downloadPath = task.getDownloadPath();
        this.downloadLink = task.getDownloadLink();
    }

    public Task toModel() {
        Task task = new Task();
        task.setId(this.id);
        task.setStatus(TaskStatus.valueOf(this.status));
        task.setDownloadSpeed(this.downloadSpeed);
        task.setDownloadProgress(this.downloadProgress);
        task.setDownloadRemainingTime(this.downloadRemainingTime);
        task.setDownloadThread(this.downloadThread);
        task.setDownloadPath(this.downloadPath);
        task.setDownloadLink(this.downloadLink);
        return task;
    }
}
