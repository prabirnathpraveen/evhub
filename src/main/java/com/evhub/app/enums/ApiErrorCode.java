package com.evhub.app.enums;

import com.evhub.app.response.generic.ResponseCode;

public enum ApiErrorCode implements ResponseCode {
    INTERNAL_SERVER_ERROR(1000, "INTERNAL_SERVER_ERROR"),
    USER_INPUT_ERROR(1001, "USER_INPUT_ERROR"),
    HANDLER_NOT_FOUND(1001, "HANDLER_NOT_FOUND"),
    REQUEST_METHOD_NOT_SUPPORTED(1001, "REQUEST_METHOD_NOT_SUPPORTED")
    ;

    ApiErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;


    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getMessage() {
        return null;
    }
}
