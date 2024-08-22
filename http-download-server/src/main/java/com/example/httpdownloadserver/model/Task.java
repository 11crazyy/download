package com.example.httpdownloadserver.model;

import lombok.Data;

@Data
public class Task {
    private Long id;
    private TaskStatus status;
    private Double downloadSpeed;
    private int downloadProgress;
    private Long downloadRemainingTime;
    private int downloadThread;
    private String downloadPath;
    private String downloadLink;
}
