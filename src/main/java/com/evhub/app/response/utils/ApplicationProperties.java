package com.evhub.app.response.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Component
@Data
public class ApplicationProperties {


    @Value("${smtp.host}")
    private String smtpHost;

    @Value("${smtp.port}")
    private String smtpPort;

    @Value("${smtp.username}")
    private String smtpUsername;

    @Value("${smtp.password}")
    private String smtpPassword;

    @Value("${smtp.auth}")
    private Boolean smtpAuth;

    @Value("${smtp.from}")
    private String from;

    @Value("${smtp.fromname}")
    private String fromName;

    @Value("${redirect-uri}")
    private String redirectUri;
}
