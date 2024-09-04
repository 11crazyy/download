package com.example.httpdownloadserver.model;

import lombok.Data;

@Data
public class Task {
    private Integer id;
    private TaskStatus status;
    private Double downloadSpeed;
    private int downloadProgress;
    private Long downloadRemainingTime;
    private int downloadThread;
    private String downloadPath;
    private String downloadLink;
    private int currentSlice;//当前下载到的切片的索引值
}
