package com.evhub.app.enums;

import com.evhub.app.response.generic.ResponseCode;

public enum ApiResponseCodeImpl implements ResponseCode {
    SUCCESS(0, "SUCCESS"),
    ERROR(1, "ERROR"),
    BAD_REQUEST(2, "BAD_REQUEST"),
    SYSTEM_CANNOT_BE_NULL_OR_EMPTY(3, "System cannot be null or empty"),
    SYSTEM_TYPE_CANNOT_BE_NULL_OR_EMPTY(4, "System Type cannot be null or empty"),
    AUTH_TYPE_CANNOT_BE_NULL_OR_EMPTY(5, "AuthType cannot be null or empty"),

    SYSTEM_URL_CANNOT_BE_NULL_OR_EMPTY(5, "System Url cannot be null or empty"),

    SYSTEM_USERNAME_CANNOT_BE_NULL_OR_EMPTY(6, "System Username cannot be null or empty"),


    SYSTEM_PASSWORD_CANNOT_BE_NULL_OR_EMPTY(7, "System Password cannot be null or empty"),

    TREND_NAME_CANNOT_BE_NULL_OR_EMPTY(8, "Trend Name cannot be null or empty"),

    INVALID_BMS_CREDENTIALS(9, "Invalid BMS credentials"),

    OBJECT_ID_CANNOT_BE_NULL_OR_EMPTY(10, "ObjectId cannot be null or empty"),

    BUILDING_NAME_CANNOT_BE_NULL_OR_EMPTY(11, "Building Name cannot be null or empty"),
    DASHBOARD_DETAILS_CANNOT_BE_NULL_OR_EMPTY(12, "Dashboard Details cannot be null or empty"),

    BUILDING_CONFIGURATION_DOES_NOT_EXIST(13, "Building Configuration does not exist"),

    LOGICAL_VIEW_URL_CANNOT_BE_NULL_OR_EMPTY(14, "Logical View Url cannot be null or empty");

    private int code;
    private String message;

    ApiResponseCodeImpl(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
