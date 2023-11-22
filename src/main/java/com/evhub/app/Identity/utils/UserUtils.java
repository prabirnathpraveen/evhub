package com.evhub.app.Identity.utils;

import com.evhub.app.Identity.request.UserCreateRequest;
import com.evhub.app.Identity.response.UserResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserUtils {

    public UserRepresentation generateFrom(UserCreateRequest userCreateRequest, boolean isAdminUser) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(userCreateRequest.getUserName());
        userRepresentation.setEmail(userCreateRequest.getEmail());
        userRepresentation.setFirstName(userCreateRequest.getFirstName());
        userRepresentation.setLastName(userCreateRequest.getLastName());
       // userRepresentation.singleAttribute("contact", userCreateRequest.getContact());
        userRepresentation.setEnabled(true);
        userRepresentation.setRequiredActions(Collections.singletonList("UPDATE_PASSWORD"));

        if (isAdminUser){
            CredentialRepresentation passwordCredentials = new CredentialRepresentation();
            passwordCredentials.setTemporary(false);
            passwordCredentials.setType(CredentialRepresentation.PASSWORD);
            passwordCredentials.setValue(userCreateRequest.getPassword());

            userRepresentation.setCredentials(Collections.singletonList(passwordCredentials));
        }
        userRepresentation.singleAttribute("updatedAt", String.valueOf(new Date().getTime()));
        return userRepresentation;
    }

    public UserRepresentation generateForUpdate(UserCreateRequest userCreateRequest) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(userCreateRequest.getFirstName());
        userRepresentation.setLastName(userCreateRequest.getLastName());
        userRepresentation.setEmail(userCreateRequest.getEmail());
        //userRepresentation.singleAttribute("contact", userCreateRequest.getContact());
        userRepresentation.singleAttribute("updatedAt", String.valueOf(new Date().getTime()));

        return userRepresentation;
    }

    public UserResponse generateFrom(UserRepresentation userRepresentation) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(userRepresentation.getId());
        userResponse.setEmail(userRepresentation.getEmail());
        userResponse.setUserName(userRepresentation.getUsername());
        userResponse.setFirstName(userRepresentation.getFirstName());
        userResponse.setLastName(userRepresentation.getLastName());
        /*if (Objects.nonNull(userRepresentation.getAttributes()) && userRepresentation.getAttributes().containsKey("contact")) {
            userResponse.setContact(userRepresentation.getAttributes().get("contact").get(0));
        } else {
            userResponse.setContact(null);
        }*/
        if (Objects.nonNull(userRepresentation.getAttributes()) && userRepresentation.getAttributes().containsKey("updatedAt")) {
            userResponse.setUpdatedAt(Long.valueOf(userRepresentation.getAttributes().get("updatedAt").get(0)));
        } else {
            userResponse.setUpdatedAt(userRepresentation.getCreatedTimestamp());
        }
        return userResponse;
    }


    public UserResponse generateFrom(UserRepresentation userRepresentation, List<Map<String, Object>> roles) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(userRepresentation.getId());
        userResponse.setEmail(userRepresentation.getEmail());
        userResponse.setUserName(userRepresentation.getUsername());
        userResponse.setFirstName(userRepresentation.getFirstName());
        userResponse.setLastName(userRepresentation.getLastName());
         userResponse.setCreatedAt(userRepresentation.getCreatedTimestamp());
        /*if (Objects.nonNull(userRepresentation.getAttributes()) && userRepresentation.getAttributes().containsKey("contact")) {
            userResponse.setContact(userRepresentation.getAttributes().get("contact").get(0));
        } else {
            userResponse.setContact(null);
        }*/
        if (Objects.nonNull(userRepresentation.getAttributes()) && userRepresentation.getAttributes().containsKey("updatedAt")) {
            userResponse.setUpdatedAt(Long.valueOf(userRepresentation.getAttributes().get("updatedAt").get(0)));
        } else {
            userResponse.setUpdatedAt(userRepresentation.getCreatedTimestamp());
        }        userResponse.setRoles(roles);
        return userResponse;
    }
}
