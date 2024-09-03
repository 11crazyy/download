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
}