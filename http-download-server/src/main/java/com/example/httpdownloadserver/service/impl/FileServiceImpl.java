package com.example.httpdownloadserver.service.impl;

import com.example.httpdownloadserver.dao.FileDAO;
import com.example.httpdownloadserver.dataobject.FileDO;
import com.example.httpdownloadserver.model.File;
import com.example.httpdownloadserver.model.Paging;
import com.example.httpdownloadserver.param.FileQueryParam;
import com.example.httpdownloadserver.param.PageQueryParam;
import com.example.httpdownloadserver.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileDAO fileDAO;

    @Override
    public List<File> listFilesByType(FileQueryParam param) {
        List<File> fileList = new ArrayList<>();
        if (param == null) {
            return fileList;
        }
        List<FileDO> fileDOS = fileDAO.order(param);
        for (FileDO fileDO : fileDOS) {
            fileList.add(fileDO.toModel());
        }
        return fileList;
    }

    @Override
    public Paging<File> pageQuery(PageQueryParam param) {
        Paging<File> result = new Paging<>();
        if (param == null) {
            return result;
        }
        if (param.getPagination() < 0) {
            param.setPagination(0);
        }
        if (param.getPageSize() < 0) {
            param.setPageSize(0);
        }
        //查询查询总数
        int counts = fileDAO.selectAllCounts();
        if (counts < 0) {
            return result;
        }
        //组装返回数据
        result.setTotalCount(counts);
        result.setPageSize(param.getPageSize());
        result.setPageNum(param.getPagination());

        int totalPage = (int) Math.ceil(counts / (param.getPageSize() * 1.0));
        result.setTotalPage(totalPage);

        //实际返回的数据
        List<FileDO> fileDOS = fileDAO.pageQuery(param);
        List<File> fileList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(fileDOS)) {
            for (FileDO fileDO : fileDOS) {
                fileList.add(fileDO.toModel());
            }
        }
        result.setData(fileList);
        return result;
    }
}
