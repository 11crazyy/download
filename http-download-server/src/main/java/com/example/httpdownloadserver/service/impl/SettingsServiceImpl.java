package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.dataobject.SettingsDO;
import com.example.httpdownloadserver.model.Settings;
import com.example.httpdownloadserver.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

@Service
public class SettingsServiceImpl implements SettingsService {
    @Autowired
    private SettingsDAO settingsDAO;
    @Override
    public int save(Settings settings) {
        int result = settingsDAO.updateById(new SettingsDO(settings));
        if (result == 0){
            result = settingsDAO.add(new SettingsDO(settings));
        }
        return result;
    }

    @Override
    public Settings getSettings() {
        SettingsDO settingsDO = settingsDAO.get();
        if (settingsDO == null){
            return null;
        }
        return settingsDO.toModel();
    }
}
