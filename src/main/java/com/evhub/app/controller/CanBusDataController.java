package com.evhub.app.controller;

import com.evhub.app.enums.ApiResponseCodeImpl;
import com.evhub.app.newclasses.ResponseDTO;
import com.evhub.app.newclasses.ResponseUtil;
import com.evhub.app.service.CanBusDataService;
import com.evhub.app.util.ResponseUtils;

// import com.flex83.app.enums.ApiResponseCode;
// import com.flex83.app.response.generic.ResponseDTO;
// import com.flex83.app.response.utils.ResponseUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/v1/can-data")
public class CanBusDataController {
        @Autowired
        private CanBusDataService canBusDataService;

        @Autowired
        private ResponseUtil responseUtil;

        @ApiOperation(value = "Create New Vehicle from can data")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @ApiIgnore
        @PostMapping()
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })

        public ResponseDTO<?> createVehicle() {
                return responseUtil.ok(canBusDataService.createNewVehicle(), ApiResponseCodeImpl.SUCCESS);
        }

        @ApiOperation(value = "Get Can data for a vehicle")
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

        public ResponseDTO<?> getCanData(@RequestParam(name = "VinnId", required = false) String vinnId,
                        @RequestParam(name = "serviceNumber", required = false) String serviceNumber) {
                return responseUtil.ok(canBusDataService.getCanData(vinnId, serviceNumber),
                                ApiResponseCodeImpl.SUCCESS);
        }
}
