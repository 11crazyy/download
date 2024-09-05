package com.example.httpdownloadserver.model;

import lombok.Data;

import java.sql.Timestamp;
@Data
public class File {
    private Integer id;
    private String url;
    private FileType fileType;
    private String fileName;
    private Long size;
    private Timestamp gmtCreated;

}
