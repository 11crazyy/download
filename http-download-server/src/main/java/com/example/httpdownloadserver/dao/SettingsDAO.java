package com.example.httpdownloadserver.dao;

import com.example.httpdownloadserver.dataobject.SettingsDO;

public interface SettingsDAO {
    //修改设置信息
    int updateById(SettingsDO settingsDO);
    //获得设置信息
    SettingsDO get();
    int add(SettingsDO settingsDO);

}
