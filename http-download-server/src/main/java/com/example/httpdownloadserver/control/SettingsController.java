package com.example.httpdownloadserver.control;

import com.example.httpdownloadserver.common.PowerConverter;
import com.example.httpdownloadserver.model.Result;
import com.example.httpdownloadserver.model.Settings;
import com.example.httpdownloadserver.service.SettingsService;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.apache.logging.log4j.Logger;

@Controller
public class SettingsController {
    private static final Logger LOGGER = LogManager.getLogger(SettingsController.class);

    @Autowired
    private SettingsService settingsService;


    /**
     * 保存修改后的设置信息
     * @param settings
     * @return
     */
    @PostMapping("/settings/save")
    @ResponseBody
    public Result<Integer> saveSettings(@RequestBody Settings settings) {
        Result<Integer> result = new Result<>();
        LOGGER.info("save settings");
        result.setData(settingsService.save(settings));
        result.setSuccess(true);
        return result;
    }
}
