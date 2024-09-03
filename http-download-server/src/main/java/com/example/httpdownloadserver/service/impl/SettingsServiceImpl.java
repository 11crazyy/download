package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.common.PowerConverter;
import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.dataobject.SettingsDO;
import com.example.httpdownloadserver.model.Settings;
import com.example.httpdownloadserver.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsServiceImpl implements SettingsService {
    @Autowired
    private SettingsDAO settingsDAO;

    @Override
    public int save(Settings settings) {
        return settingsDAO.insert(PowerConverter.convert(settings, SettingsDO.class));
    }

    @Override
    public List<Settings> getSettings() {
        List<SettingsDO> settingsDOS = settingsDAO.selectAll();
        return PowerConverter.batchConvert(settingsDOS, Settings.class);
    }
}
