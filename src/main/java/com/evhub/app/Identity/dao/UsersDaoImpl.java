package com.evhub.app.Identity.dao;

import com.evhub.app.Identity.builder.KeycloakClientBuilder;
import com.evhub.app.Identity.response.UserResponse;
import com.evhub.app.Identity.utils.UserUtils;
import com.evhub.app.exception.ValidationException;

import lombok.extern.log4j.Log4j2;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class UsersDaoImpl implements UsersDao {

    @Autowired
    private KeycloakClientBuilder keycloakClientBuilder;
    @Autowired
    private UserUtils userUtils;

    @Override
    public Response createUsers(UserRepresentation userRepresentation, String realm) {
        try {
            return keycloakClientBuilder.getKeycloakInstance().realm(realm).users().create(userRepresentation);
        } catch (ValidationException validationException) {
            throw validationException;
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Unable to create user");
        }
    }

    @Override
    public void sendVerificationMail(String realm, String userId, String Attribute) {
        try {
            UserResource userResource = keycloakClientBuilder.getKeycloakInstance().realm(realm).users().get(userId);
            userResource.executeActionsEmail(Collections.singletonList(Attribute), 3600);

        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Unable to send verification mail");
        }
    }

    @Override
    public void sendVerificationMailWithId(String realm, String userId, String Attribute, String clientId,
            String redirectUri) {
        try {

            UserResource userResource = keycloakClientBuilder.getKeycloakInstance().realm(realm).users().get(userId);
            userResource.executeActionsEmail(clientId, redirectUri, 3600, Collections.singletonList(Attribute));

        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Unable to send verification mail");
        }
    }

    @Override
    public List<UserRepresentation> getUsersList(String realm) {
        return keycloakClientBuilder.getKeycloakInstance().realm(realm).users().list();
    }

    @Override
    public List<UserResponse> getUsers(String realm) {
        List<UserRepresentation> userRepresentationList = keycloakClientBuilder.getKeycloakInstance().realm(realm)
                .users().list();
        List<UserResponse> userResponseList = new ArrayList<>();
        if (Objects.nonNull(userRepresentationList)) {
            for (UserRepresentation userRepresentation : userRepresentationList) {
                userResponseList.add(userUtils.generateFrom(userRepresentation));
            }
            return userResponseList;
        }
        return null;
    }

    @Override
    public List<UserRepresentation> getUsers(String realm, Integer offSet, Integer max) {
        return keycloakClientBuilder.getKeycloakInstance().realm(realm).users().list(offSet, max);
    }

    @Override
    public UserRepresentation getUserById(String realm, String userid) {
        UserRepresentation userRepresentation = null;
        try {
            userRepresentation = keycloakClientBuilder.getKeycloakInstance().realm(realm).users().get(userid)
                    .toRepresentation();
        } catch (Exception e) {
            log.error("error while fetching user" + e.getMessage());
        }
        return userRepresentation;
    }

    @Override
    public List<Map<String, Object>> getUserRoleByUserId(String realm, String userid) {
        List<Map<String, Object>> userRoleList = new ArrayList<>();
        List<RoleRepresentation> allRoleRepresentationList = keycloakClientBuilder.getKeycloakInstance().realm(realm)
                .users().get(userid).roles().realmLevel().listEffective();
        List<RoleRepresentation> roleRepresentationList = allRoleRepresentationList.stream()
                .filter(roleRepresentation -> !(roleRepresentation.getName().equals("offline_access") ||
                        roleRepresentation.getName().equals("default-roles-evhub")
                        || roleRepresentation.getName().equals("uma_authorization")))
                .collect(Collectors.toList());
        Map<String, String> roleMap = roleRepresentationList.stream()
                .collect(Collectors.toMap(RoleRepresentation::getId, RoleRepresentation::getName));

        roleMap.entrySet().forEach(ele -> {
            Map<String, Object> roleDetail = new HashMap<>();
            roleDetail.put("id", ele.getKey());
            roleDetail.put("name", ele.getValue());

            userRoleList.add(roleDetail);
        });
        return userRoleList;
    }

    @Override
    public List<RoleRepresentation> getUserRoleRepresentationByUserId(String realm, String userid) {
        List<RoleRepresentation> roleRepresentationList = keycloakClientBuilder.getKeycloakInstance().realm(realm)
                .users().get(userid).roles().realmLevel().listEffective();
        return roleRepresentationList;
    }

    @Override
    public List<UserRepresentation> getUserByUsername(String realm, String username) {
        return keycloakClientBuilder.getKeycloakInstance().realm(realm).users().search(username);
    }

    @Override
    public void deleteUsersById(String realm, String userid) {
        try {
            keycloakClientBuilder.getKeycloakInstance().realm(realm).users().delete(userid);
        } catch (ValidationException validationException) {
            throw validationException;
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Unable to delete user");
        }
    }

    @Override
    public void updateUsersById(UserRepresentation userRepresentation, String realm, String userid) {
        try {
            keycloakClientBuilder.getKeycloakInstance().realm(realm).users().get(userid).update(userRepresentation);
        } catch (ValidationException validationException) {
            throw validationException;
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Unable to update user");
        }
    }

    @Override
    public void usersRoleMappingById(List<RoleRepresentation> roleRepresentationList, String realm, String userid,
            String clientId) {
        keycloakClientBuilder.getKeycloakInstance().realm(realm).users().get(userid).roles().clientLevel(clientId)
                .add(roleRepresentationList);
    }

    @Override
    public void removeUserRole(List<RoleRepresentation> roleRepresentationList, String realm, String userid) {
        keycloakClientBuilder.getKeycloakInstance().realm(realm).users().get(userid).roles().realmLevel()
                .remove(roleRepresentationList);
    }

    @Override
    public Integer count(String realm) {
        return keycloakClientBuilder.getKeycloakInstance().realm(realm).users().count();
    }

    @Override
    public List<UserRepresentation> search(String realm, Map<String, Object> queryParams) {
        // return
        // keycloakClientBuilder.getKeycloakInstance().realm(realm).users().search((String)
        // queryParams.get("userName"), (String) queryParams.get("firstName"), (String)
        // queryParams.get("lastName"), (String) queryParams.get("email"),
        // Integer.parseInt((String) queryParams.get("offset")),
        // Integer.parseInt((String) queryParams.get("max")), true, true);
        // return
        // keycloakClientBuilder.getKeycloakInstance().realm(realm).users().search((String)
        // queryParams.get("userName"), (String) queryParams.get("firstName"), (String)
        // queryParams.get("lastName"), (String) queryParams.get("email"), (Integer)
        // queryParams.get("offset"), (Integer) queryParams.get("max"), true, true);
        return keycloakClientBuilder.getKeycloakInstance().realm(realm).users().search(
                (String) queryParams.get("search"), (Integer) queryParams.get("offset"),
                (Integer) queryParams.get("max"), true);
    }

    @Override
    public Integer searchCount(String realm, Map<String, Object> queryParams) {
        return keycloakClientBuilder.getKeycloakInstance().realm(realm).users().count(
                (String) queryParams.get("lastName"), (String) queryParams.get("firstName"),
                (String) queryParams.get("email"), true, (String) queryParams.get("userName"));
    }

    @Override
    public List<UserRepresentation> searchByUserName(String realm, String userName) {
        return keycloakClientBuilder.getKeycloakInstance().realm(realm).users().search(userName);
    }
}