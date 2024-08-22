package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.dataobject.SettingsDO;
import com.example.httpdownloadserver.model.Settings;
import com.example.httpdownloadserver.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsServiceImpl implements SettingsService {
    @Autowired
    private SettingsDAO settingsDAO;
    @Override
    public int updateSettings(Settings settings) {
        if (settings == null){
            return 0;
        }
        return settingsDAO.update(new SettingsDO(settings));
    }

    @Override
    public Settings getSettings() {
        return settingsDAO.get().toModel();
    }
}
