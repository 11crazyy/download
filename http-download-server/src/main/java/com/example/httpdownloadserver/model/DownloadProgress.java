package com.example.httpdownloadserver.model;

import lombok.Data;

@Data
public class DownloadProgress {
    private int progress;//下载进度
    private double speed;//下载速度
    private long remainingTime;//剩余时间
    private String fileName;//文件名
    public DownloadProgress(int progress, double speed, long remainingTime, String fileName) {
        this.progress = progress;
        this.speed = speed;
        this.remainingTime = remainingTime;
        this.fileName = fileName;
    }
}
