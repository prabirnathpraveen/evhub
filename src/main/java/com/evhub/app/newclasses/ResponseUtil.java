package com.evhub.app.newclasses;

import org.springframework.stereotype.Component;

import com.evhub.app.enums.ApiResponseCodeImpl;
import com.evhub.app.newclasses.ApiErrorResponseDTO;

@Component
public class ResponseUtil {

    public static <T> ResponseDTO<T> ok() {
        return new ApiResponseDTO<>();
    }

    public static <T> ResponseDTO<T> ok(T data, ApiResponseCodeImpl responseCode) {
        return new ApiResponseDTO<>(true, responseCode.getCode(), responseCode.getMessage(), data);
    }

    public static ResponseDTO<?> error(ApiResponseCodeImpl responseCode) {
        return new ApiErrorResponseDTO<>(responseCode.getCode(), responseCode.getMessage());
    }

}