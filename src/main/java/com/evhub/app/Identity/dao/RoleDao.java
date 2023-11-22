package com.evhub.app.Identity.dao;

import com.evhub.app.Identity.request.RoleCreateRequest;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.List;
import java.util.Map;

public interface RoleDao {
    void createRole(String realm, String clientId, RoleCreateRequest roleCreateRequest);

    void updateRole(String realm, String clientId, String roleId, RoleCreateRequest roleCreateRequest);

    List<Map<String, Object>> getAppPermission(String realm, String clientId);

    void assignRoleToUser(String realm, String roleName, String userId);

    List<Map<String, Object>> getAllRoles(String realm);

    List<RoleRepresentation> getAllRoleRepresentation(String realm);

    Map<String, Object> getRoleById(String realm, String roleId);

    void deleteRoleById(String realm, String roleName);
}
