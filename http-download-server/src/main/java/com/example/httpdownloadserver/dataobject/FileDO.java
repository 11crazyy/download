package com.example.httpdownloadserver.dataobject;

import com.example.httpdownloadserver.model.File;
import com.example.httpdownloadserver.model.FileType;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class FileDO {
    private Integer id;
    private String url;
    private String fileType;
    private String fileName;
    private Long size;
    private Timestamp gmtCreated;
    private Timestamp gmtModified;
}
