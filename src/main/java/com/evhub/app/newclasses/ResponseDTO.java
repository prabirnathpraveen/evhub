package com.evhub.app.newclasses;

import com.evhub.app.enums.ApiResponseCodeImpl;

public interface ResponseDTO<T> {
    String getMessage();

    ResponseDTO setMessage(String message);

    T getData();

    void setData(T data);

}
