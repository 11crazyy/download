package com.example.httpdownloadserver.dataobject;

import lombok.Data;

import java.util.Date;

@Data
public class TaskDO {
    private Long id;
    private String status;
    private Double downloadSpeed;
    private Integer downloadProgress;
    private Long downloadRemainingTime;
    private String downloadPath;
    private String downloadLink;
    private Integer currentSlice;
    private Integer threadCount;
    private Long contentLength;
    private Integer shardSize;
    private Date gmtCreated;
    private Date gmtModified;
}
