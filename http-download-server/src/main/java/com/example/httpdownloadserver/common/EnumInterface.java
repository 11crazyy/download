package com.example.httpdownloadserver.common;

public interface EnumInterface {

    int getCode();

    static <T extends Enum<T> & EnumInterface> T codeOf(Class<T> enumClass, int code) {
        T[] enumConstants = enumClass.getEnumConstants();
        for (T t : enumConstants) {
            if (t.getCode() == code) {
                return t;
            }
        }
        return enumConstants[0];
    }
}
