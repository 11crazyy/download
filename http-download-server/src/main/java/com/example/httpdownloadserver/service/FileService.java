package com.example.httpdownloadserver.service;

import com.example.httpdownloadserver.model.File;
import com.example.httpdownloadserver.model.Paging;
import com.example.httpdownloadserver.param.FileQueryParam;
import com.example.httpdownloadserver.param.PageQueryParam;

import java.util.List;

public interface FileService {
    /**
     * 按类型筛选文件 排序文件
     * @param param
     * @return
     */
    List<File> listFilesByType(FileQueryParam param);

    /**
     * 分页查询文件
     * @param param
     * @return
     */
    Paging<File> pageQuery(PageQueryParam param);
}
