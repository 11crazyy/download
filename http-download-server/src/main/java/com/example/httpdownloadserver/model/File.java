package com.example.httpdownloadserver.model;

import com.example.httpdownloadserver.dataobject.FileDO;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
@Data
public class File {
    private Integer id;
    private String url;
    private FileType fileType;
    private String fileName;
    private Long size;
    private Timestamp gmtCreated;

}
