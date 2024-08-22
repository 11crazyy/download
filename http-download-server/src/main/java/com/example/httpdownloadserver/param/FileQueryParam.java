package com.example.httpdownloadserver.param;

import lombok.Data;

@Data
public class FileQueryParam {
    private String fileType;
    private String orderType;
    private Boolean orderAsc;
}
