package com.example.httpdownloadserver.service;

import com.example.httpdownloadserver.model.File;

import java.io.IOException;

public interface DownloadService {
    /**
     * 下载文件
     * @param file
     * @return
     */
    void download(File file) throws IOException;
}
