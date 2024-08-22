package com.example.httpdownloadserver.service;

import com.example.httpdownloadserver.dataobject.SettingsDO;
import com.example.httpdownloadserver.model.Settings;

public interface SettingsService {
    /**
     * 修改配置信息
     * @param settings
     * @return
     */
    int updateSettings(Settings settings);

    /**
     * 获取配置信息
     * @return
     */
    Settings getSettings();
}
