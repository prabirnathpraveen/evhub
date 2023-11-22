
package com.evhub.app.response;

import com.evhub.app.newclasses.ResponseDTO;
import com.evhub.app.response.generic.ResponseCode;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Locale;
import java.util.StringJoiner;

@JsonPropertyOrder({ "success", "messageCode", "message", "data" })
@SuppressWarnings({ "rawtypes", "unused" })
public class ApiResponseDTO<T> implements ResponseDTO<T> {
    private boolean success = true;
    private int messageCode;
    private String message = "";
    private T data;

    public ApiResponseDTO() {
    }

    public ApiResponseDTO(ResponseCode responseCode, T data) {
        this.messageCode = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.data = data;
    }

    public ApiResponseDTO(int messageCode, String message, T data) {
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }

    private Locale getLocale(String locale) {
        return locale != null ? new Locale(locale) : Locale.UK;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                .add("success = " + messageCode)
                .add("messageCode = " + messageCode)
                .add("message = " + message)
                .add("data = " + data).toString();
    }
}
