package com.example.httpdownloadserver.control;

import com.example.httpdownloadserver.model.File;
import com.example.httpdownloadserver.model.Paging;
import com.example.httpdownloadserver.model.Result;
import com.example.httpdownloadserver.param.FileQueryParam;
import com.example.httpdownloadserver.param.PageQueryParam;
import com.example.httpdownloadserver.service.FileService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
public class FileController {
    private static final Logger LOGGER = LogManager.getLogger(FileController.class);
    @Autowired
    private FileService fileService;

    /**
     * 按类型筛选文件 按文件名或文件大小或文件创建时间给文件列表排序
     * @param param
     * @return
     */
    @PostMapping("/file/list")
    @ResponseBody
    public Result<List<File>> listFiles(@RequestBody FileQueryParam param) {
        Result<List<File>> result = new Result<>();
        if (param == null) {
            result.setSuccess(false);
            LOGGER.warn("filequeryparam is null");
            result.setMessage("param is null");
            return result;
        }
        result.setData(fileService.listFilesByType(param));
        result.setSuccess(true);
        return result;
    }

    /**
     * 分页查询文件
     * @param param
     * @return
     */
    @PostMapping("/file/page")
    @ResponseBody
    public Result<Paging<File>> pageFiles(@RequestBody PageQueryParam param) {
        Result<Paging<File>> result = new Result<>();
        if (param == null) {
            LOGGER.warn("pagequeryparam is null");
            result.setMessage("param is null");
            result.setSuccess(false);
        } else {
            result.setSuccess(true);
            result.setData(fileService.pageQuery(param));
        }
        return result;
    }
}
