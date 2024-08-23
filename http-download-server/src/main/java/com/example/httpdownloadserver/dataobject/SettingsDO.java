package com.example.httpdownloadserver.dataobject;

import com.example.httpdownloadserver.model.Settings;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class SettingsDO {
    private Long id;
    private String settingName;
    private String settingValue;
    private Timestamp gmtCreated;
    private Timestamp gmtModified;

    public SettingsDO(Settings settings){
        this.id = settings.getId();
        this.settingName = settings.getSettingName();
        this.settingValue = settings.getSettingValue();
    }
    public Settings toModel(){
        Settings settings = new Settings();
        settings.setId(this.id);
        settings.setSettingValue(this.settingValue);
        settings.setSettingName(this.settingName);
        return settings;
    }
}
