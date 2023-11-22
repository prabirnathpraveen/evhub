package com.evhub.app.controller;

import com.evhub.app.Identity.request.UserCreateRequest;
import com.evhub.app.Identity.service.IAMService;
import com.evhub.app.enums.ApiResponseCodeImpl;
import com.evhub.app.newclasses.ResponseDTO;
import com.evhub.app.newclasses.ResponseUtil;
import com.evhub.app.response.ApiResponseDTO;
import com.evhub.app.response.generic.AccessDeniedResponseDTO;
import com.evhub.app.response.generic.BadRequestResponseDTO;
import com.evhub.app.response.generic.NotAuthenticatedResponseDTO;

// import com.flex83.app.annotations.PreHandle;
// import com.flex83.app.enums.ApiResponseCode;
// import com.flex83.app.response.ApiResponseDTO;
// import com.flex83.app.response.generic.AccessDeniedResponseDTO;
// import com.flex83.app.response.generic.BadRequestResponseDTO;
// import com.flex83.app.response.generic.NotAuthenticatedResponseDTO;
// import com.flex83.app.response.generic.ResponseDTO;
// import com.flex83.app.response.utils.ResponseUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/iam")
public class IAMController {

        @Autowired
        private ResponseUtil responseUtil;

        @Autowired
        private IAMService iamService;

        // @PreHandle(roles = {"Admin"})
        @ApiOperation(value = "Create User")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PostMapping("/users")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> createUser(@RequestBody UserCreateRequest userCreateRequest) throws InterruptedException {
                iamService.createUser(userCreateRequest, false);
                return responseUtil.ok(null, ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get All Users")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/users")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getUsers(@RequestParam(name = "offset", required = false) Integer offset,
                        @RequestParam(name = "max", required = false) Integer max,
                        @RequestParam(name = "search", required = false) String search) {
                return responseUtil.ok(iamService.getUsers(offset, max, search), ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Search User By QueryParams")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/users/search")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> searchUser(@RequestParam Map<String, Object> queryParams) {
                return responseUtil.ok(iamService.searchUser(queryParams), ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get User By Id")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/users/{userId}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getUserById(@PathVariable("userId") String userId) {
                return responseUtil.ok(iamService.getUserById(userId), ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Update User By Id")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PutMapping("/users/{userId}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> updateUserById(@RequestBody UserCreateRequest userCreateRequest,
                        @PathVariable("userId") String userId) {
                iamService.updateUserById(userCreateRequest, userId);
                return responseUtil.ok(null, ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Delete User By Id")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @DeleteMapping("users/{userId}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> deleteUserById(@PathVariable("userId") String userId) {
                iamService.deleteUserById(userId);
                return responseUtil.ok(null, ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin"})
        @ApiOperation(value = "Get All Roles")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/roles")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getAllRoles() {
                return responseUtil.ok(iamService.getAllRoles(), ApiResponseCodeImpl.SUCCESS);
        }

        @ApiOperation(value = "Get App Permissions")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/permissions")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getAppPermission() {
                return responseUtil.ok(iamService.getAppPermission(), ApiResponseCodeImpl.SUCCESS);
        }
}