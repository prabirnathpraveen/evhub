//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.evhub.app.util;

// import com.flex83.app.enums.ApiResponseCode;
// import com.flex83.app.locale.MessageByLocale;
// import com.flex83.app.response.ApiErrorResponseDTO;
// import com.flex83.app.response.ApiResponseDTO;
// import com.flex83.app.response.generic.ResponseCode;
// import com.flex83.app.response.generic.ValidationErrorDTO;
// import com.flex83.app.response.generic.ValidationErrorResponse;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.evhub.app.enums.ApiResponseCodeImpl;
import com.evhub.app.newclasses.MessageByLocale;
import com.evhub.app.response.ApiErrorResponseDTO;
import com.evhub.app.response.ApiResponseDTO;
import com.evhub.app.response.generic.ResponseCode;
import com.evhub.app.response.generic.ValidationErrorDTO;
import com.evhub.app.response.generic.ValidationErrorResponse;

@Component
public final class ResponseUtils {
    private static final Map data = new HashMap();

    @Autowired
    private MessageByLocale messageByLocale;

    public ResponseUtils() {
    }

    public Locale getLocale(String locale) {
        return locale != null ? new Locale(locale) : Locale.UK;
    }

    public ApiResponseDTO ok() {
        String message = this.messageByLocale.getMessage(String.valueOf(ApiResponseCodeImpl.SUCCESS.getCode()));
        return new ApiResponseDTO(ApiResponseCodeImpl.SUCCESS.getCode(), message, data);
    }

    public ApiResponseDTO ok(Object data) {
        String message = this.messageByLocale.getMessage(String.valueOf(ApiResponseCodeImpl.SUCCESS.getCode()));
        return new ApiResponseDTO(ApiResponseCodeImpl.SUCCESS.getCode(), message, data);
    }

    public ApiResponseDTO ok(ResponseCode responseCode) {
        String message = this.messageByLocale.getMessage(String.valueOf(responseCode.getCode()));
        return new ApiResponseDTO(responseCode.getCode(), message, data);
    }

    public ApiResponseDTO ok(Object data, ResponseCode responseCode) {
        String message = this.messageByLocale.getMessage(String.valueOf(responseCode.getCode()));
        return new ApiResponseDTO(responseCode.getCode(), message, data);
    }

    public ApiResponseDTO ok(Object data, ResponseCode responseCode, String message) {
        return new ApiResponseDTO(responseCode.getCode(), message, data);
    }

    public ApiResponseDTO ok(ResponseCode responseCode, String locale) {
        String message = this.messageByLocale.getMessage(String.valueOf(responseCode.getCode()));
        return new ApiResponseDTO(responseCode.getCode(), message, data);
    }

    public ApiErrorResponseDTO exception(int errorCode, int code) {
        String message = this.messageByLocale.getMessage(String.valueOf(code));
        return new ApiErrorResponseDTO(errorCode, code, message, data);
    }

    public ApiErrorResponseDTO exception(int errorCode, int code, String message) {
        return new ApiErrorResponseDTO(errorCode, code, message, data);
    }

    public ApiErrorResponseDTO exception(int errorCode, ResponseCode responseCode) {
        String message = this.messageByLocale.getMessage(String.valueOf(responseCode.getCode()));
        return new ApiErrorResponseDTO(errorCode, responseCode.getCode(), message, data);
    }

    public ApiErrorResponseDTO validationFailed(int errorCode, int code, String message) {
        return new ApiErrorResponseDTO(errorCode, code, message, data);
    }

    public ApiErrorResponseDTO validationFailed(int errorCode, int code, ValidationErrorDTO validationErrorDTO) {
        return new ApiErrorResponseDTO(errorCode, code, "Missing / Invalid Parameter(s)",
                validationErrorDTO.getFieldErrors());
    }

    public ApiErrorResponseDTO validationFailed(int errorCode, int code, ValidationErrorResponse errorResponse) {
        return new ApiErrorResponseDTO(errorCode, code, "Missing / Invalid Parameter(s)",
                errorResponse.getErrorMessages());
    }
}
