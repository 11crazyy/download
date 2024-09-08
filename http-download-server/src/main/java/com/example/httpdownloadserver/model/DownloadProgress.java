package com.example.httpdownloadserver.model;

import lombok.Data;

@Data
public class DownloadProgress {
    private int progress;//下载进度
    private double speed;//下载速度
    private long remainingTime;//剩余时间
    private long downloadedBytes;

    public DownloadProgress(int progress, double speed, long remainingTime, long downloadedBytes) {
        this.progress = progress;
        this.speed = speed;
        this.remainingTime = remainingTime;
        this.downloadedBytes = downloadedBytes;
    }
}
