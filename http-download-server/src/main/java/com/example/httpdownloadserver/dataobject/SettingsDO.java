package com.example.httpdownloadserver.dataobject;

import com.example.httpdownloadserver.model.Settings;
import lombok.Data;

import java.util.Date;

@Data
public class SettingsDO {
    private Long id;

    private Date gmtCreated;

    private Date gmtModified;

    private String settingName;

    private String settingValue;

    public SettingsDO(Settings settings) {
        this.id = settings.getId();
        this.settingName = settings.getSettingName();
        this.settingValue = settings.getSettingValue();
    }

    public Settings toModel() {
        Settings settings = new Settings();
        settings.setId(this.id);
        settings.setSettingName(this.settingName);
        settings.setSettingValue(this.settingValue);
        return settings;
    }
}