package com.evhub.app.response;

import com.evhub.app.newclasses.ResponseDTO;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "success", "errorCode", "messageCode", "message", "data" })
public class ApiErrorResponseDTO<T> implements ResponseDTO<T> {
    private int errorCode;
    private int messageCode;
    private String message = "";
    private T data;
    private boolean success = false;

    public ApiErrorResponseDTO() {
    }

    public ApiErrorResponseDTO(int errorCode, int messageCode, String message, T data) {
        this.errorCode = errorCode;
        this.messageCode = messageCode;
        this.message = message;
        this.data = data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ResponseDTO setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public void setData(T data) {
        this.data = data;
    }
}
