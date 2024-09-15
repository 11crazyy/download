package com.example.httpdownloadserver.model;

import com.example.httpdownloadserver.common.EnumInterface;

public enum FileType implements EnumInterface {
    all(1),
    Video(2),
    Photo(3),
    Archive(4),
    Document(5)
    ;

    public final int code;

    FileType(int i) {
        this.code = i;
    }

    @Override
    public int getCode() {
        return code;
    }
}
