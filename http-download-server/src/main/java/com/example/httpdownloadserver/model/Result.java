package com.example.httpdownloadserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Result<D> implements Serializable {
    @JsonProperty("isSuccess")
    private boolean success = false;

    private String code;
    private String message;

    private D data;

}
