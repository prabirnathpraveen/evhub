package com.evhub.app.controller;

import com.evhub.app.entities.HtmlFiles;
import com.evhub.app.enums.ApiResponseCodeImpl;
import com.evhub.app.newclasses.ResponseDTO;
import com.evhub.app.newclasses.ResponseUtil;
import com.evhub.app.response.ApiResponseDTO;
import com.evhub.app.response.generic.AccessDeniedResponseDTO;
import com.evhub.app.response.generic.BadRequestResponseDTO;
import com.evhub.app.response.generic.NotAuthenticatedResponseDTO;
import com.evhub.app.service.HtmlFilrService;
// import com.flex83.app.annotations.PreHandle;
// import com.flex83.app.enums.ApiResponseCode;
// import com.flex83.app.response.generic.ResponseDTO;
// import com.flex83.app.response.utils.ResponseUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequestMapping(value = "/api/v1/file")
public class HtmlFileController {
        @Autowired
        private HtmlFilrService htmlFilrService;
        @Autowired
        private ResponseUtil responseUtil;

        // @PreHandle(roles = { "Admin" })
        @ApiOperation(value = "Save Html File Vehicle")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PostMapping("/upload")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
                return responseUtil.ok(htmlFilrService.saveHtmlFile(file), ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = { "Admin" })
        @ApiOperation(value = "Save Html File Vehicle")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PutMapping("/{id}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> updateFieFile(@RequestParam("file") MultipartFile file, @PathVariable String id)
                        throws Exception {
                return responseUtil.ok(htmlFilrService.updateHtmlFile(file, id), ApiResponseCodeImpl.SUCCESS);
        }
}
