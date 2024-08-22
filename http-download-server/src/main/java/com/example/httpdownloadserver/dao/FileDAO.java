package com.example.httpdownloadserver.dao;

import com.example.httpdownloadserver.dataobject.FileDO;
import com.example.httpdownloadserver.param.FileQueryParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileDAO {
    //增加文件
    int insert(FileDO fileDO);

    //给文件按照文件名或文件大小或创建时间排序
    List<FileDO> order(FileQueryParam fileQueryParam);

    //分页查询
    List<FileDO> pageQuery(FileQueryParam fileQueryParam);


}
