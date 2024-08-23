package com.example.httpdownloadserver.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class Paging<R> implements Serializable {

    private static final long serialVersionUID = 522660448543880825L;
    /**
     * 页数
     */
    private int pageNum = 1;

    /**
     * 每页数量
     */
    private int pageSize = 15;
    /**
     * 总页数
     */
    private int totalPage;

    /**
     * 总记录数
     */
    private long totalCount;

    /**
     * 集合数据
     */
    private List<R> data;

    public Paging() {

    }

    public Paging(int pageNum, int pageSize, int totalPage, long totalCount, List<R> data) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPage = totalPage;
        this.totalCount = totalCount;
        this.data = data;
    }
}
