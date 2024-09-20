package com.example.httpdownloadserver.model;

import com.example.httpdownloadserver.common.EnumInterface;

public enum FileType implements EnumInterface {
    ALL(1),
    VIDEO(2),
    PHOTO(3),
    ARCHIVE(4),
    DOCUMENT(5)
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
