package com.evhub.app.newclasses;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "success", "errorCode", "messageCode", "message", "data" })
public class ApiResponseDTO<T> implements ResponseDTO<T> {
    private boolean success;
    private int errorCode;
    private int messageCode;
    private String message;
    private T data;

    public ApiResponseDTO() {
    }

    public ApiResponseDTO(boolean success, int messageCode, String message, T data) {
        this.success = success;
        this.errorCode = errorCode;
        this.messageCode = messageCode;
        this.message = message;
        this.data = data;
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
