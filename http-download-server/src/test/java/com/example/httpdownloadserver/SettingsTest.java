package com.example.httpdownloadserver;

import com.example.httpdownloadserver.model.Settings;
import com.example.httpdownloadserver.service.SettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsTest extends HttpDownloadServerApplicationTests {
    @Autowired
    private SettingsService settingsService;
    @Test
    public void test() {
//        Settings settings = new Settings();
//        settings.setId(1L);
//        settings.setDownloadPath("C:\\Users\\Administrator\\Desktop\\test");
//        settings.setMaxDownloadSpeed(1024);
//        settings.setMaxThreadNum(10);
//        settingsService.save(settings);
//        assertEquals(10, settingsService.getSettings().getMaxThreadNum());
    }
}
