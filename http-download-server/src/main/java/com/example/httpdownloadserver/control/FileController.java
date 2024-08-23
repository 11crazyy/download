package com.example.httpdownloadserver.control;

import com.example.httpdownloadserver.model.File;
import com.example.httpdownloadserver.model.Paging;
import com.example.httpdownloadserver.model.Result;
import com.example.httpdownloadserver.param.FileQueryParam;
import com.example.httpdownloadserver.param.PageQueryParam;
import com.example.httpdownloadserver.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

//private static final Logger logger = LoggerFactory.getLogger(FileController.class);

@Controller
public class FileController {
    @Autowired
    private FileService fileService;
    //按类型筛选文件 按文件名或文件大小或文件创建时间给文件列表排序
    @PostMapping("/file/list")
    @ResponseBody
    public Result<List<File>> listFiles(@RequestBody FileQueryParam param) {
        Result<List<File>> result = new Result<>();
        if (param == null){
            result.setSuccess(false);
            result.setMessage("param is null");
            return result;
        }
        result.setData(fileService.listFilesByType(param));
        result.setSuccess(true);
        return result;
    }
    //分页排序文件
    @PostMapping("/file/page")
    @ResponseBody
    public Result<Paging<File>> pageFiles(@RequestBody PageQueryParam param) {
        Result<Paging<File>> result = new Result<>();
        if (param == null){
        }
        return null;
    }
}
