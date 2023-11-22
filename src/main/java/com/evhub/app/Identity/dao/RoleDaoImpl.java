package com.evhub.app.Identity.dao;

import com.evhub.app.Identity.builder.KeycloakClientBuilder;
import com.evhub.app.Identity.request.RoleCreateRequest;
import com.evhub.app.exception.ValidationException;

// import com.flex83.app.exception.ValidationException;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class RoleDaoImpl implements RoleDao {
    @Autowired
    private KeycloakClientBuilder keycloakClientBuilder;

    @Override
    public void createRole(String realm, String clientId, RoleCreateRequest roleCreateRequest) {
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(roleCreateRequest.getName());
        roleRepresentation.setDescription(roleCreateRequest.getDescription());
        roleRepresentation.setClientRole(false);
        RealmResource realmResource = keycloakClientBuilder.getKeycloakInstance().realm(realm);

        RolesResource rolesResource = realmResource.roles();
        rolesResource.create(roleRepresentation);

        ClientRepresentation clientRepresentation = realmResource.clients().findByClientId(clientId).get(0);
        List<RoleRepresentation> roleRepresentationList = realmResource.clients().get(clientRepresentation.getId())
                .roles().list();
        roleRepresentationList = roleRepresentationList.stream()
                .filter(ele -> roleCreateRequest.getPermission().contains(ele.getName())).collect(Collectors.toList());
        rolesResource.get(roleCreateRequest.getName()).addComposites(roleRepresentationList);
    }

    @Override
    public void updateRole(String realm, String clientId, String roleId, RoleCreateRequest roleCreateRequest) {
        RealmResource realmResource = keycloakClientBuilder.getKeycloakInstance().realm(realm);
        RolesResource rolesResource = realmResource.roles();
        ClientRepresentation clientRepresentation = realmResource.clients().findByClientId(clientId).get(0);

        RoleRepresentation roleRepresentation = realmResource.rolesById().getRole(roleId);
        roleRepresentation.setDescription(roleCreateRequest.getDescription());
        rolesResource.get(roleRepresentation.getName()).update(roleRepresentation);

        Set<RoleRepresentation> userRoleRepresentations = rolesResource.get(roleRepresentation.getName())
                .getClientRoleComposites(clientRepresentation.getId());
        rolesResource.get(roleRepresentation.getName()).deleteComposites(new ArrayList<>(userRoleRepresentations));

        List<RoleRepresentation> roleRepresentationList = realmResource.clients().get(clientRepresentation.getId())
                .roles().list();
        roleRepresentationList = roleRepresentationList.stream()
                .filter(ele -> roleCreateRequest.getPermission().contains(ele.getName())).collect(Collectors.toList());

        rolesResource.get(roleRepresentation.getName()).addComposites(roleRepresentationList);
    }

    @Override
    public List<Map<String, Object>> getAppPermission(String realm, String clientId) {
        List<Map<String, Object>> listOfPermission = new ArrayList<>();
        RealmResource realmResource = keycloakClientBuilder.getKeycloakInstance().realm(realm);

        ClientRepresentation clientRepresentation = realmResource.clients().findByClientId(clientId).get(0);
        List<RoleRepresentation> roleRepresentationList = realmResource.clients().get(clientRepresentation.getId())
                .roles().list();
        Map<String, String> permissionMap = roleRepresentationList.stream()
                .collect(Collectors.toMap(RoleRepresentation::getId, RoleRepresentation::getName));
        permissionMap.entrySet().forEach(ele -> {
            Map<String, Object> permissionDetail = new HashMap<>();
            permissionDetail.put("id", ele.getKey());
            permissionDetail.put("name", ele.getValue());

            listOfPermission.add(permissionDetail);
        });
        return listOfPermission;
    }

    @Override
    public void assignRoleToUser(String realm, String roleName, String userId) {
        RealmResource realmResource = keycloakClientBuilder.getKeycloakInstance().realm(realm);
        UserResource userResource = realmResource.users().get(userId);
        RoleRepresentation assignedRole = realmResource.roles().get(roleName).toRepresentation();
        // userResource.roles().realmLevel().add(Arrays.asList(assignedRole));
        userResource.roles().realmLevel().remove(userResource.roles().realmLevel().listAll());
        userResource.roles().realmLevel().add(Arrays.asList(assignedRole));

    }

    @Override
    public List<Map<String, Object>> getAllRoles(String realm) {
        List<Map<String, Object>> roleList = new ArrayList<>();
        RealmResource realmResource = keycloakClientBuilder.getKeycloakInstance().realm(realm);
        RolesResource rolesResource = realmResource.roles();

        List<RoleRepresentation> list = realmResource.roles().list();
        List<RoleRepresentation> roleRepresentationList = list.stream()
                .filter(roleRepresentation -> !(roleRepresentation.getName().equals("offline_access") ||
                        roleRepresentation.getName().equals("default-roles-evhub")
                        || roleRepresentation.getName().equals("uma_authorization")))
                .collect(Collectors.toList());
        roleRepresentationList.forEach(ele -> {
            Set<RoleRepresentation> assignedRoleSet = rolesResource.get(ele.getName()).getRoleComposites();
            Map<String, Object> roleDetail = new HashMap<>();
            roleDetail.put("id", ele.getId());
            roleDetail.put("name", ele.getName());
            roleDetail.put("description", ele.getDescription());

            List<Map<String, Object>> permission = new ArrayList<>();

            Map<String, String> permissionMap = assignedRoleSet.stream()
                    .collect(Collectors.toMap(RoleRepresentation::getId, RoleRepresentation::getName));
            permissionMap.entrySet().forEach(element -> {
                Map<String, Object> permissionDetail = new HashMap<>();
                permissionDetail.put("id", element.getKey());
                permissionDetail.put("name", element.getValue());
                permission.add(permissionDetail);
            });

            roleDetail.put("permission", permission);

            roleList.add(roleDetail);
        });
        return roleList;
    }

    @Override
    public List<RoleRepresentation> getAllRoleRepresentation(String realm) {
        RealmResource realmResource = keycloakClientBuilder.getKeycloakInstance().realm(realm);
        RolesResource rolesResource = realmResource.roles();
        return rolesResource.list();
    }

    @Override
    public Map<String, Object> getRoleById(String realm, String roleId) {
        Map<String, Object> roleDetail = null;
        RealmResource realmResource = keycloakClientBuilder.getKeycloakInstance().realm(realm);
        RolesResource rolesResource = realmResource.roles();
        RoleRepresentation roleRepresentation = realmResource.rolesById().getRole(roleId);
        if (Objects.nonNull(roleRepresentation)) {
            roleDetail = new HashMap<>();
            roleDetail.put("id", roleRepresentation.getId());
            roleDetail.put("name", roleRepresentation.getName());
            roleDetail.put("description", roleRepresentation.getDescription());

            Set<RoleRepresentation> assignedRoleSet = rolesResource.get(roleRepresentation.getName())
                    .getRoleComposites();
            List<Map<String, Object>> permission = new ArrayList<>();
            Map<String, String> permissionMap = assignedRoleSet.stream()
                    .collect(Collectors.toMap(RoleRepresentation::getId, RoleRepresentation::getName));
            permissionMap.entrySet().forEach(element -> {
                Map<String, Object> permissionDetail = new HashMap<>();
                permissionDetail.put("id", element.getKey());
                permissionDetail.put("name", element.getValue());
                permission.add(permissionDetail);
            });

            roleDetail.put("permission", permission);
        }
        return roleDetail;
    }

    @Override
    public void deleteRoleById(String realm, String roleName) {
        try {
            keycloakClientBuilder.getKeycloakInstance().realm(realm).roles().deleteRole(roleName);
        } catch (ValidationException validationException) {
            throw validationException;
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Unable to delete role");
        }
    }

    // @Override
    // public void deleteRoleById(String realm, String roleName) {
    // try {
    // keycloakClientBuilder.getKeycloakInstance().realm(realm).roles().deleteRole(roleName);
    // } catch (Exception e) {
    // int httpStatusCode = HttpStatus.BAD_REQUEST.value();
    // throw new RuntimeException("Unable to delete role" + httpStatusCode, e);
    // }
    // }
}
