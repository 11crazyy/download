package com.example.httpdownloadserver.dao;

import com.example.httpdownloadserver.dataobject.SettingsDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SettingsDAO {
    //修改设置信息
    int update(SettingsDO settingsDO);
    //获得设置信息
    SettingsDO get();

}
