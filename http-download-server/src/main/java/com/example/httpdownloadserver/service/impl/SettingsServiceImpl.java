package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.dataobject.SettingsDO;
import com.example.httpdownloadserver.model.Settings;
import com.example.httpdownloadserver.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SettingsServiceImpl implements SettingsService{
    @Autowired
    private SettingsDAO settingsDAO;
    @Override
    public int save(Settings settings) {
        //如果数据库有数据则直接修改 没有则添加
        SettingsDO settingsDO = settingsDAO.selectByName(settings.getSettingName());
        if (settingsDO == null){
            return settingsDAO.insert(new SettingsDO(settings));
        }
        return settingsDAO.updateByPrimaryKey(settingsDO);

    }

    @Override
    public List<Settings> getSettings() {
        List<SettingsDO> settingsDOS = settingsDAO.selectAll();
        List<Settings> result = new ArrayList<>();
        for (SettingsDO settingsDO : settingsDOS) {
            result.add(settingsDO.toModel());
        }
        return result;
    }
}
