package com.example.httpdownloadserver.dataobject;

import com.example.httpdownloadserver.model.File;
import com.example.httpdownloadserver.model.FileType;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class FileDO {
    private Long id;
    private String fileType;
    private String fileName;
    private Double size;
    private Timestamp gmtCreated;
    private Timestamp gmtModified;
    public FileDO(File file){
        this.id = file.getId();
        this.fileType = file.getFileType().name();
        this.fileName = file.getFileName();
        this.size = file.getSize();
        this.gmtCreated = file.getCreateTime();
    }
    public File toModel(){
        File file = new File();
        file.setId(this.id);
        file.setFileType(FileType.valueOf(this.fileType));
        file.setFileName(this.fileName);
        file.setSize(this.size);
        file.setCreateTime(this.gmtCreated);
        return file;
    }
}
