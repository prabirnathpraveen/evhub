package com.evhub.app.enums;

public enum SwaggerConstant {
    JWT("JWT"),
    AUTHORIZATION("Authorization"),
    GLOBAL("global"),
    ACCESS_EVERYTHING("accessEverything"),
    HEADER("header");

    String value;

    SwaggerConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
