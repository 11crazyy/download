package com.example.httpdownloadserver.model;

import lombok.Data;

@Data
public class Settings {
    private Long id;
    private String downloadPath;
    private int maxThreadNum;
    private int maxDownloadSpeed;
}
