package com.evhub.app.Identity.builder;

import com.evhub.app.Identity.config.KeyCloakConfig;
// import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KeycloakClientBuilder {
    private static final ConcurrentHashMap<String, Keycloak> keycloakClients = new ConcurrentHashMap<>();
    private static final String CLIENT_KEYS = "keycloak";
    @Autowired
    private KeyCloakConfig keycloakConfig;

    private Keycloak keycloakClient() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakConfig.getUrl())
                .realm(keycloakConfig.getRealm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(keycloakConfig.getResource())
                .clientSecret(keycloakConfig.getClientSecret())
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build())
                .build();
    }

    public Keycloak getKeycloakInstance() {
        Keycloak keycloak = keycloakClients.get(CLIENT_KEYS);
        if (Objects.isNull(keycloak)) {
            keycloak = keycloakClient();
            keycloakClients.put(CLIENT_KEYS, keycloak);
        }
        return keycloak;
    }
}
