package com.evhub.app.Identity.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeyCloakConfig {

    @Value("${url:http://localhost:8080/auth}")
    private String url;
    @Value("${realm:enzen}")
    private String realm;
    @Value("${resource:service-account}")
    private String resource;
    @Value("${clientSecret:clientSecret}")
    private String clientSecret;

}
