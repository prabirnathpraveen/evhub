package com.evhub.app.controller;

import com.evhub.app.entities.JobCard;
import com.evhub.app.enums.ApiResponseCodeImpl;
import com.evhub.app.newclasses.ResponseDTO;
import com.evhub.app.newclasses.ResponseUtil;
import com.evhub.app.service.JobCardService;
// import com.flex83.app.annotations.PreHandle;
// import com.flex83.app.enums.ApiResponseCode;
// import com.flex83.app.response.generic.ResponseDTO;
// import com.flex83.app.response.utils.ResponseUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/job/card")
public class JobCardController {
        @Autowired
        private ResponseUtil responseUtil;

        @Autowired
        private JobCardService jobCardService;

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get Job Card Details")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/{id}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getJobCardById(@PathVariable String id) {
                return responseUtil.ok(jobCardService.getJobCardById(id), ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin","Mechanic"})
        @ApiOperation(value = "Get All Job Card Details")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping()
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getAllJobCard() {
                return responseUtil.ok(jobCardService.getAllJobCard(), ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin","Mechanic"})
        @ApiOperation(value = "Create Job Card Details")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PostMapping()
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> createJobCard(@RequestBody JobCard jobCard) {
                return responseUtil.ok(jobCardService.createJobCard(jobCard), ApiResponseCodeImpl.SUCCESS);
        }
}
