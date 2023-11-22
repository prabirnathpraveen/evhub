package com.evhub.app.Identity.service;

import com.evhub.app.Identity.config.KeyCloakConfig;
import com.evhub.app.Identity.dao.RoleDao;
import com.evhub.app.Identity.dao.UsersDao;
import com.evhub.app.Identity.request.UserCreateRequest;
import com.evhub.app.Identity.response.RoleResponse;
import com.evhub.app.Identity.response.UserResponse;
import com.evhub.app.Identity.utils.UserUtils;
import com.evhub.app.constant.ApplicationConstant;
import com.evhub.app.exception.ValidationException;
import com.evhub.app.generic.CountResponse;
import com.evhub.app.response.utils.ApplicationProperties;

import lombok.extern.log4j.Log4j2;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.evhub.app.constant.ApplicationConstant.CLIENT_ID;

@Service
@Log4j2
public class IAMService {
    @Autowired
    private UsersDao usersDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private KeyCloakConfig keycloakConfig;
    @Autowired
    private UserUtils userUtils;
    @Autowired
    private ApplicationProperties applicationProperties;

    public String createUser(UserCreateRequest userCreateRequest, boolean isAdminUser) {
        List<UserResponse> userResponseList = usersDao.getUsers("evhub");
        UserResponse userDetails = userResponseList.stream()
                .filter(ele -> ele.getUserName().equals(userCreateRequest.getUserName())).findAny().orElse(null);
        if (Objects.nonNull(userDetails)) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(),
                    "User already exist on keycloak,please try with some other.png username");
        }
        UserResponse userDetailsEmail = userResponseList.stream()
                .filter(ele -> ele.getEmail().equals(userCreateRequest.getEmail())).findAny().orElse(null);
        if (Objects.nonNull(userDetailsEmail)) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Email Id already exit");
        }
        UserRepresentation userRepresentation = userUtils.generateFrom(userCreateRequest, isAdminUser);
        String userId = CreatedResponseUtil
                .getCreatedId(usersDao.createUsers(userRepresentation, keycloakConfig.getRealm()));

        usersDao.sendVerificationMailWithId(keycloakConfig.getRealm(), userId,
                ApplicationConstant.UPDATE_PASSWORD_ATTRIBUTE, "app-console", applicationProperties.getRedirectUri());
        roleDao.assignRoleToUser(keycloakConfig.getRealm(), userCreateRequest.getRoles(), userId);

        return userId;
    }

    public Object getUsers(Integer offset, Integer max, String search) {
        offset = Objects.nonNull(offset) ? offset * max : 0;
        boolean offsetExists = Objects.nonNull(offset);
        max = Objects.nonNull(max) ? max : 20;
        if (Objects.isNull(search) || search.isEmpty()) {
            List<UserRepresentation> userRepresentationList;
            max = Objects.nonNull(max) ? max : 20;
            userRepresentationList = usersDao.getUsers(keycloakConfig.getRealm(), offset, max);
            List<UserResponse> listOfUser = new ArrayList<>();
            userRepresentationList.forEach(ele -> {
                listOfUser.add(userUtils.generateFrom(ele,
                        usersDao.getUserRoleByUserId(keycloakConfig.getRealm(), ele.getId())));
            });
            if (offsetExists) {
                Map<String, Object> result = new HashMap<>();
                result.put("total", usersDao.count(keycloakConfig.getRealm()));
                result.put("records", listOfUser);
                return result;
            }
            CountResponse countResponse = new CountResponse();
            countResponse.setCount((long) usersDao.count(keycloakConfig.getRealm()));
            countResponse.setResponse(listOfUser);
            return countResponse;
        }
        Map<String, Object> queryParam = new HashMap<>();
        /*
         * if (search.contains(" ")){
         * String[] params = search.split(" ");
         * queryParam.put("firstName",params[0]);
         * queryParam.put("lastName",params[1]);
         * }else {
         * queryParam.put("firstName", search);
         * queryParam.put("lastName", search);
         * queryParam.put("email", search);
         * }
         */
        queryParam.put("max", max);
        queryParam.put("offset", offset);
        queryParam.put("search", search);
        if (offsetExists) {
            return searchUser(queryParam);
        } else {
            List<UserResponse> listOfUser = (List<UserResponse>) searchUser(queryParam);

            CountResponse countResponse = new CountResponse();
            countResponse.setCount((long) usersDao.count(keycloakConfig.getRealm()));
            countResponse.setResponse(listOfUser);
            return countResponse;
        }
    }

    public Object searchUser(Map<String, Object> queryParams) {
        List<UserRepresentation> userRepresentationList = usersDao.search(keycloakConfig.getRealm(), queryParams);
        List<UserResponse> listOfUser = new ArrayList<>();
        userRepresentationList.forEach(ele -> {
            listOfUser.add(
                    userUtils.generateFrom(ele, usersDao.getUserRoleByUserId(keycloakConfig.getRealm(), ele.getId())));
        });
        if (Objects.nonNull(queryParams.get("offset"))) {
            Map<String, Object> result = new HashMap<>();
            result.put("total", usersDao.searchCount(keycloakConfig.getRealm(), queryParams));
            result.put("records", listOfUser);
            return result;
        }
        return listOfUser;
    }

    List<UserResponse> searchByUserName(String userName) {
        List<UserRepresentation> userRepresentationList = usersDao.searchByUserName(keycloakConfig.getRealm(),
                userName);
        List<UserResponse> listOfUser = new ArrayList<>();
        userRepresentationList.forEach(ele -> {
            listOfUser.add(
                    userUtils.generateFrom(ele, usersDao.getUserRoleByUserId(keycloakConfig.getRealm(), ele.getId())));
        });
        return listOfUser;
    }

    public UserResponse getUserById(String userId) {
        UserRepresentation userRepresentation = usersDao.getUserById(keycloakConfig.getRealm(), userId);
        if (Objects.isNull(userRepresentation))
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "User detail does not exists");
        UserResponse userResponse = userUtils.generateFrom(userRepresentation,
                usersDao.getUserRoleByUserId(keycloakConfig.getRealm(), userRepresentation.getId()));
        return userResponse;
    }

    public void updateUserById(UserCreateRequest userCreateRequest, String userId) {
        UserRepresentation existingUserDetails = usersDao.getUserById(keycloakConfig.getRealm(), userId);
        if (Objects.isNull(existingUserDetails))
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "User doesn't exist with this username");

        List<RoleRepresentation> roleRepresentationListOnKeycloak = roleDao
                .getAllRoleRepresentation(keycloakConfig.getRealm());

        if (!userCreateRequest.getEmail().isEmpty()) {
            existingUserDetails.setEmail(userCreateRequest.getEmail());
            existingUserDetails.setEmailVerified(false);
        }
        if (!userCreateRequest.getFirstName().isEmpty()) {
            existingUserDetails.setFirstName(userCreateRequest.getFirstName());
        }
        if (!userCreateRequest.getLastName().isEmpty()) {
            existingUserDetails.setLastName(userCreateRequest.getLastName());
        }
        /*
         * if (!userCreateRequest.getContact().isEmpty()) {
         * existingUserDetails.singleAttribute("contact",
         * userCreateRequest.getContact());
         * }
         */
        existingUserDetails.singleAttribute("updatedAt", String.valueOf(new Date().getTime()));

        if (Objects.nonNull(userCreateRequest.getPassword()) && !userCreateRequest.getPassword().isEmpty()) {
            CredentialRepresentation passwordCredentials = new CredentialRepresentation();
            passwordCredentials.setTemporary(false);
            passwordCredentials.setType(CredentialRepresentation.PASSWORD);
            passwordCredentials.setValue(userCreateRequest.getPassword());
            existingUserDetails.setCredentials(Collections.singletonList(passwordCredentials));
        }
        // existingUserDetails.setRealmRoles(List.of(userCreateRequest.getRoles()));
        roleDao.assignRoleToUser(keycloakConfig.getRealm(), userCreateRequest.getRoles(), userId);
        // usersDao.sendVerificationMail(keycloakConfig.getRealm(), userId,
        // ApplicationConstant.VERIFY_EMAIL_ATTRIBUTE);
        usersDao.updateUsersById(existingUserDetails, keycloakConfig.getRealm(), userId);

    }

    public void deleteUserById(String userId) {
        UserRepresentation userRepresentation = usersDao.getUserById(keycloakConfig.getRealm(), userId);
        if (userRepresentation.getUsername().equals("super.admin")) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Super Admin can not be deleted");
        }
        if (Objects.isNull(userRepresentation))
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "User detail does not exists");
        usersDao.deleteUsersById(keycloakConfig.getRealm(), userId);
    }

    public List<RoleResponse> getAllRoles() {
        List<Map<String, Object>> roleList = roleDao.getAllRoles(keycloakConfig.getRealm());
        List<RoleResponse> listOfRole = new ArrayList<>();
        roleList.forEach(ele -> {
            RoleResponse roleResponse = new RoleResponse();
            roleResponse.setId((String) ele.get("id"));
            roleResponse.setName((String) ele.get("name"));
            roleResponse.setDescription((String) ele.get("description"));

            listOfRole.add(roleResponse);
        });
        return listOfRole;
    }

    public List<Map<String, Object>> getAppPermission() {
        List<Map<String, Object>> response = populatePermission(
                roleDao.getAppPermission(keycloakConfig.getRealm(), CLIENT_ID));
        return response;
    }

    private List<Map<String, Object>> populatePermission(List<Map<String, Object>> list) {
        List<Map<String, Object>> response = new ArrayList<>();
        Map<String, List<Map<String, Object>>> data = new HashMap<>();
        list.forEach(ele -> {
            String[] category = String.valueOf(ele.get("name")).split("-");
            if (data.containsKey(category[1])) {
                List<Map<String, Object>> existing = data.get(category[1]);
                Map<String, Object> setData = new HashMap<>();
                setData.put("id", ele.get("id"));
                setData.put("name", ele.get("name"));

                existing.add(setData);
            } else {
                List<Map<String, Object>> createData = new ArrayList<>();

                Map<String, Object> setData = new HashMap<>();
                setData.put("id", ele.get("id"));
                setData.put("name", ele.get("name"));
                createData.add(setData);
                data.put(category[1], createData);
            }
        });
        data.entrySet().forEach(ele -> {
            Map<String, Object> permissionDetail = new HashMap<>();
            permissionDetail.put("category", ele.getKey());
            permissionDetail.put("permission", ele.getValue());
            response.add(permissionDetail);
        });

        return response;
    }
}