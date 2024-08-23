package com.example.httpdownloadserver.param;

import lombok.Data;

@Data
public class PageQueryParam {
    /**
     * 页数
     */
    private int pagination = 0;
    /**
     * 每页数量
     */
    private int pageSize = 10;


}
