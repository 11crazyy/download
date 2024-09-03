package com.example.httpdownloadserver;

import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.model.Settings;
import com.example.httpdownloadserver.service.SettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsTest extends HttpDownloadServerApplicationTests {
    @Autowired
    private SettingsService settingsService;

    @Autowired
    private SettingsDAO settingsDAO;

    @Test
    public void saveTest() {
        Settings settings = new Settings();
        settings.setId(1L);
        settings.setSettingName("maxThread");
        settings.setSettingValue("5");
        settingsService.save(settings);
        assertEquals("5", settingsService.getSettings().get(0).getSettingName());
    }
}
