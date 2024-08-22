package com.example.httpdownloadserver.control;

import com.example.httpdownloadserver.dataobject.SettingsDO;
import com.example.httpdownloadserver.model.Settings;
import com.example.httpdownloadserver.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class SettingsController {
    @Autowired
    private SettingsService settingsService;
    //保存修改后的设置信息
    public int saveSettings(SettingsDO settingsDO) {

        return settingsService.updateSettings(settingsDO.toModel());
    }
}
