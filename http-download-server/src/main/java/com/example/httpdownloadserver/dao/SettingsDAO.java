package com.example.httpdownloadserver.dao;

import com.example.httpdownloadserver.dataobject.SettingsDO;

import java.util.List;

public interface SettingsDAO {
    int deleteByPrimaryKey(Long id);

    int insert(SettingsDO row);

    SettingsDO selectByName(String name);

    List<SettingsDO> selectAll();

    int updateByPrimaryKey(SettingsDO row);
}