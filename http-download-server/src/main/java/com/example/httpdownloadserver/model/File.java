package com.example.httpdownloadserver.model;

import com.example.httpdownloadserver.dataobject.FileDO;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
@Data
public class File {
    private Long id;
    private FileType fileType;
    private String url;
    private String fileName;
    private Long size;
    private Timestamp createTime;

}
