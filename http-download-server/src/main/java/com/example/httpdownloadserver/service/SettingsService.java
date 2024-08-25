package com.example.httpdownloadserver.service;

import com.example.httpdownloadserver.model.Settings;

import java.util.List;

public interface SettingsService {
    /**
     * 修改配置信息
     * @param settings
     * @return
     */
    int save(Settings settings);

    /**
     * 获取配置信息
     * @return
     */
    List<Settings> getSettings();
}
