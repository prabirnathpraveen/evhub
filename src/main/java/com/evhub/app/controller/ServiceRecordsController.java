package com.evhub.app.controller;

import com.evhub.app.entities.ServiceRecord;
import com.evhub.app.enums.ApiResponseCodeImpl;
import com.evhub.app.newclasses.ResponseDTO;
import com.evhub.app.newclasses.ResponseUtil;
import com.evhub.app.request.UpdateServiceRequest;
import com.evhub.app.response.ApiResponseDTO;
import com.evhub.app.response.generic.AccessDeniedResponseDTO;
import com.evhub.app.response.generic.BadRequestResponseDTO;
import com.evhub.app.response.generic.NotAuthenticatedResponseDTO;
import com.evhub.app.service.ServiceRecordsService;
// import com.flex83.app.annotations.PreHandle;
// import com.flex83.app.enums.ApiResponseCode;
// import com.flex83.app.response.generic.ResponseDTO;
// import com.flex83.app.response.utils.ResponseUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping(value = "/api/v1/service")
public class ServiceRecordsController {
        @Autowired
        private ServiceRecordsService serviceRecordsService;
        @Autowired
        private ResponseUtil responseUtil;

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get Service Number")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/number/{chassisNumber}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getAllServiceRecordsNumber(@PathVariable String chassisNumber) {
                return responseUtil.ok(serviceRecordsService.getAllServiceRecordsNumber(chassisNumber),
                                ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Update Servicing Status")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PutMapping("/status/{chassisNumber}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> updateServicingInformation(@PathVariable String chassisNumber) {
                return responseUtil.ok(serviceRecordsService.updateServicingInformation(chassisNumber),
                                ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get Servicing price")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/price/{chassisNumber}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getServicePrice(@PathVariable String chassisNumber,
                        @RequestParam(name = "ServiceNumber", required = false) String serviceNumber) {
                return responseUtil.ok(serviceRecordsService.getServicePrice(chassisNumber, serviceNumber),
                                ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Create Service Records")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PostMapping("/{chassisNumber}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> createNewServiceRecordsNumber(@RequestBody ServiceRecord serviceRecord,
                        @PathVariable String chassisNumber) {
                return responseUtil.ok(serviceRecordsService.createServiceRecords(serviceRecord, chassisNumber),
                                ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Update Service Records")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PutMapping("/{chassisNumber}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> updateNewServiceRecordsNumber(@RequestBody UpdateServiceRequest updateServiceRequest,
                        @PathVariable String chassisNumber) {
                return responseUtil.ok(serviceRecordsService.updateServiceRecords(updateServiceRequest, chassisNumber),
                                ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get Service Records for a vehicle")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/{chassisNumber}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getServiceRecordsByChassisNumber(@PathVariable String chassisNumber,
                        @RequestParam(name = "ServiceNumber", required = false) String serviceNumber)
                        throws ParseException {
                return responseUtil.ok(serviceRecordsService.getServiceRecordByRegNumberAndServiceNumber(chassisNumber,
                                serviceNumber), ApiResponseCodeImpl.SUCCESS);
        }
        /*
         * //@PreHandle(roles = {"Admin", "Mechanic"})
         * 
         * @ApiOperation(value = "Get Service Records for a vehicle")
         * 
         * @ApiResponses(value = {
         * 
         * @ApiResponse(code = 201, message = "Created"),
         * 
         * @ApiResponse(code = 400, message = "Bad Request"),
         * 
         * @ApiResponse(code = 401, message = "You are Not Authenticated"),
         * 
         * @ApiResponse(code = 403, message = "Not Authorized on this resource"),
         * 
         * @ApiResponse(code = 404, message =
         * "The resource you were trying to reach is not found"),
         * })
         * 
         * @GetMapping ("/all/{chassisNumber}")
         * 
         * @ApiImplicitParams({
         * 
         * @ApiImplicitParam(name = "Authorization", value = "Access Token", required =
         * true, allowEmptyValue = false, paramType = "header", dataTypeClass =
         * String.class, example = "Bearer access_token"),
         * 
         * @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true,
         * allowEmptyValue = false, paramType = "header", dataTypeClass = String.class,
         * example = "evhub")
         * })
         * public ResponseDTO<?> getAllServiceRecordsByRegistrationNumber(@PathVariable
         * String chassisNumber,@RequestParam (name = "ServiceNumber", required = false)
         * String serviceNumber) {
         * return responseUtil.ok(serviceRecordsService.
         * getServiceRecordByRegNumberAndServiceNumber(chassisNumber,serviceNumber),
         * ApiResponseCode.SUCCESS);
         * }
         */

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get Overall status for a service records")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/Overall/{chassisNumber}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getOverallHealth(@PathVariable String chassisNumber,
                        @RequestParam(name = "ServiceNumber", required = false) String serviceNumber) {
                return responseUtil.ok(serviceRecordsService.getOverallHealthJobCard(chassisNumber, serviceNumber),
                                ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get Service Records for a vehicle BY Date")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/date")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getServiceRecordsByDate(
                        @RequestParam(name = "servicingDate", required = false) Long serviceDate) {
                return responseUtil.ok(serviceRecordsService.getServiceRecordsByDate(serviceDate),
                                ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        // @ApiOperation(value = "Get Service Records for a vehicle BY Date")
        // @ApiResponses(value = {
        // @ApiResponse(code = 201, message = "Created"),
        // @ApiResponse(code = 400, message = "Bad Request"),
        // @ApiResponse(code = 401, message = "You are Not Authenticated"),
        // @ApiResponse(code = 403, message = "Not Authorized on this resource"),
        // @ApiResponse(code = 404, message = "The resource you were trying to reach is
        // not found"),
        // })
        // @DeleteMapping("/main/Battery")
        // @ApiImplicitParams({
        // @ApiImplicitParam(name = "Authorization", value = "Access Token", required =
        // true, allowEmptyValue = false, paramType = "header", dataTypeClass =
        // String.class, example = "Bearer access_token"),
        // @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true,
        // allowEmptyValue = false, paramType = "header", dataTypeClass = String.class,
        // example = "evhub")
        // })
        // public ResponseDTO<?> deleteDuplicateMainBatteryRecords() {
        // serviceRecordsService.removedDuplicateMainBattery();
        // return responseUtil.ok(ApiResponseCodeImpl.SUCCESS);
        // }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get AWS link for a single Image")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PostMapping("/uploadImage")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public String uploadImage(@RequestBody String image) throws Exception {

                return serviceRecordsService.uploadImage(image);
        }
}
