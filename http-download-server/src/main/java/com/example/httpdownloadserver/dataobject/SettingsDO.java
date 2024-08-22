package com.example.httpdownloadserver.dataobject;

import com.example.httpdownloadserver.model.Settings;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class SettingsDO {
    private Long id;
    private String downloadPath;
    private int maxThreadNum;
    private int maxDownloadSpeed;
    private Timestamp gmtCreated;
    private Timestamp gmtModified;

    public SettingsDO(Settings settings){
        this.id = settings.getId();
        this.downloadPath = settings.getDownloadPath();
        this.maxThreadNum = settings.getMaxThreadNum();
        this.maxDownloadSpeed = settings.getMaxDownloadSpeed();
    }
    public Settings toModel(){
        Settings settings = new Settings();
        settings.setId(this.id);
        settings.setDownloadPath(this.downloadPath);
        settings.setMaxThreadNum(this.maxThreadNum);
        settings.setMaxDownloadSpeed(this.maxDownloadSpeed);
        return settings;
    }
}
