package com.example.httpdownloadserver.dao;

import com.example.httpdownloadserver.dataobject.FileDO;
import com.example.httpdownloadserver.param.FileQueryParam;
import com.example.httpdownloadserver.param.PageQueryParam;

import java.util.List;

public interface FileDAO {
    //增加文件
    int insert(FileDO fileDO);

    //给文件按照文件名或文件大小或创建时间排序
    List<FileDO> order(FileQueryParam fileQueryParam);

    //分页查询
    List<FileDO> pageQuery(PageQueryParam param);

    //查询总数
    int selectAllCounts();


}
