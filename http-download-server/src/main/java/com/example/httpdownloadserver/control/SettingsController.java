package com.example.httpdownloadserver.control;

import com.example.httpdownloadserver.dataobject.SettingsDO;
import com.example.httpdownloadserver.model.Result;
import com.example.httpdownloadserver.service.SettingsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SettingsController {
    @Autowired
    private SettingsService settingsService;

    /**
     * 保存修改后的设置信息
     * @param settingsDO
     * @return
     */
    @PostMapping("/settings/save")
    @ResponseBody
    public Result<Integer> saveSettings(@RequestBody SettingsDO settingsDO) {
        Result<Integer> result = new Result<>();
        Logger logger = (Logger) LogManager.getLogger(SettingsController.class);
        logger.info("save settings");
        result.setData(settingsService.save(settingsDO.toModel()));
        result.setSuccess(true);
        return result;
    }
}
