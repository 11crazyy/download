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

   public void setErrorResult(Result<?> result,String errorMessage){
       result.setSuccess(false);
       result.setMessage(errorMessage);
   }

   public void setSuccessResult(Result<?> result,String successMessage){
       result.setSuccess(true);
       result.setMessage(successMessage);
   }
}
