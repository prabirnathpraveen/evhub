package com.evhub.app.Identity.dao;

import com.evhub.app.Identity.response.UserResponse;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public interface UsersDao {

    void sendVerificationMail(String realm, String userId,String Attribute);

    void sendVerificationMailWithId(String realm, String userId, String Attribute, String clientId, String redirectUri);

    List<UserRepresentation> getUsersList(String realm);

    List<UserResponse> getUsers(String realm);

    List<UserRepresentation> getUsers(String realm, Integer offset, Integer max);

    UserRepresentation getUserById(String realm, String userid);

    List<Map<String, Object>> getUserRoleByUserId(String realm, String userid);

    List<RoleRepresentation> getUserRoleRepresentationByUserId(String realm, String userid);

    List<UserRepresentation> getUserByUsername(String realm, String username);

    void deleteUsersById(String realm, String userid);

    void updateUsersById(UserRepresentation userRepresentation, String realm, String userid);

    void usersRoleMappingById(List<RoleRepresentation> roleRepresentationList, String realm, String userid, String clientId);

    void removeUserRole(List<RoleRepresentation> roleRepresentationList, String realm, String userid);

    Response createUsers(UserRepresentation userRepresentation, String realm);

    Integer count(String realm);

    List<UserRepresentation> search(String realm, Map<String, Object> queryParams);

    Integer searchCount(String realm, Map<String, Object> queryParams);
    
    List<UserRepresentation> searchByUserName(String realm, String userName);
}