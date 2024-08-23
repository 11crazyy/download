package com.example.httpdownloadserver.control;

import com.example.httpdownloadserver.dataobject.SettingsDO;
import com.example.httpdownloadserver.model.Settings;
import com.example.httpdownloadserver.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SettingsController {
    @Autowired
    private SettingsService settingsService;

    /**
     * 保存修改后的设置信息
     * @param settingsDO
     * @return
     */
    @PostMapping("/settings/save")
    @ResponseBody
    public int saveSettings(@RequestBody SettingsDO settingsDO) {
        return settingsService.save(settingsDO.toModel());
    }
}
