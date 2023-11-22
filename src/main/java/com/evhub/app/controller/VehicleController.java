package com.evhub.app.controller;

import com.evhub.app.entities.Vehicle;
import com.evhub.app.entities.VehicleCreateRequest;
import com.evhub.app.enums.ApiResponseCodeImpl;
import com.evhub.app.generic.CountResponse;
import com.evhub.app.newclasses.ResponseDTO;
import com.evhub.app.newclasses.ResponseUtil;
import com.evhub.app.request.FilterRequest;
import com.evhub.app.request.SearchFilterRequest;
import com.evhub.app.response.ApiResponseDTO;
import com.evhub.app.response.generic.AccessDeniedResponseDTO;
import com.evhub.app.response.generic.BadRequestResponseDTO;
import com.evhub.app.response.generic.NotAuthenticatedResponseDTO;
import com.evhub.app.service.S3ServiceList;
import com.evhub.app.service.VehicleService;
import com.evhub.app.util.CommonUtils;
import com.evhub.app.util.ResponseUtils;
// import com.flex83.app.annotations.PreHandle;
// import com.flex83.app.enums.ApiResponseCode;
// import com.flex83.app.response.generic.ResponseDTO;
import io.swagger.annotations.*;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/vehicle")
public class VehicleController {
        @Autowired
        VehicleService vehicleService;
        @Autowired
        private ResponseUtil responseUtil;

        @Autowired
        private S3ServiceList s3ServiceList;

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get All Vehicles")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getVehicles(
                        @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                        @RequestParam(name = "max", required = false, defaultValue = "10") int max,
                        @RequestParam(name = "sortOn", required = false, defaultValue = "latestServiceTime") String sortOn,
                        @RequestParam(name = "sortType", required = false, defaultValue = "DESC") String sortType,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) List<String> brand,
                        @RequestParam(required = false) List<String> model,
                        @RequestParam(required = false) List<Integer> engineCount) {
                return responseUtil.ok(vehicleService.getVehicles(offset, max, sortOn, sortType, search, brand, model,
                                engineCount), ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get Vehicle By ChassisNumber")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("{chassisNumber}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getVehicleByChassisNumber(@PathVariable String chassisNumber,
                        @RequestParam(name = "ServiceNumber", required = false) String serviceNumber)
                        throws ParseException {
                return responseUtil.ok(vehicleService.getVehicleByChassisNumber(chassisNumber, serviceNumber),
                                ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get All Vehicles")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PostMapping("/get")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getFilterVehicles(@RequestBody SearchFilterRequest searchFilterRequest) {
                Long start = CommonUtils.getCurrentTimeInMillis();
                CountResponse filterRequest = vehicleService.getFilterRequest(searchFilterRequest);
                return responseUtil.ok(filterRequest, ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Load Vehicle Details")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created"),
                        @ApiResponse(code = 400, message = "Bad Request"),
                        @ApiResponse(code = 401, message = "You are Not Authenticated"),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource"),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("load/{chassisNumber}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> loadVehicleDetails(@PathVariable(name = "chassisNumber") String chassisNumber)
                        throws ParseException {
                return responseUtil.ok(vehicleService.loadVehicleDetails(chassisNumber), ApiResponseCodeImpl.SUCCESS);
        }

        /*
         * @ApiOperation(value = "Get Vehicle Form Details")
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
         * @GetMapping("/{id}")
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
         * public ResponseDTO<?> getVehiclesForm(@PathVariable String
         * id, @RequestParam(name = "serviceNumber", required = false) Integer
         * serviceNumber, @RequestParam(name = "formName", required = false,
         * defaultValue = "formName") String formName
         * 
         * ) {
         * return
         * responseUtil.ok(vehicleService.getVehiclesForm(id,formName,serviceNumber),
         * ApiResponseCode.SUCCESS);
         * }
         */
        @ApiOperation(value = "Create new Vehicle from Reg number or Chassis number")
        // @ApiOperation(value = "Create Vehicle")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PostMapping
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> createVehicle(@RequestBody VehicleCreateRequest vehicleCreateRequest) throws Exception {
                Document vehicleResponse = vehicleService.createNewVehicle(vehicleCreateRequest);

                return responseUtil.ok();

        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Delete Vehicle By Id")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @DeleteMapping("/{chassisNumber}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> deleteVehicleById(@PathVariable String chassisNumber) {
                vehicleService.deleteVehicle(chassisNumber);
                return responseUtil.ok(null, ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Update Vehicle By Id")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PutMapping("/{chassisNumber}")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> updateVehicle(@RequestBody Vehicle vehicleUpdateRequest,
                        @PathVariable String chassisNumber) {
                return responseUtil.ok(vehicleService.updateVehicle(vehicleUpdateRequest, chassisNumber),
                                ApiResponseCodeImpl.SUCCESS);
        }

        /*
         * @ApiOperation(value = "Get All Models")
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
         * @GetMapping("/models")
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
         * public ResponseDTO<?> getAllDistinctModel(@RequestParam(name = "vehicleId",
         * required = false) String vehicleId, @RequestParam(name = "offset", required =
         * false, defaultValue = "0") int offset, @RequestParam(name = "max", required =
         * false, defaultValue = "10") int max, @RequestParam(name = "sortType",
         * required = false, defaultValue = "model") String sortType
         * ) {
         * return responseUtil.ok(vehicleService.getAllDistinctModel(sortType),
         * ApiResponseCode.SUCCESS);
         * }
         * 
         * @ApiOperation(value = "Get All graph Vehicle")
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
         * @GetMapping("/graph")
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
         * public ResponseDTO<?> getVehicleGraphResponse(@RequestParam(name =
         * "vehicleId", required = false) String vehicleId, @RequestParam(name =
         * "offset", required = false, defaultValue = "0") int
         * offset, @RequestParam(name = "max", required = false, defaultValue = "10")
         * int max, @RequestParam(name = "sortType", required = false, defaultValue =
         * "model") String sortType
         * , @RequestParam Map<String, String> params) {
         * return responseUtil.ok(vehicleService.getVehicleGraphResponse(params),
         * ApiResponseCode.SUCCESS);
         * }
         */
        /*
         * @ApiOperation(value = "Get All Forms for vehicle")
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
         * @GetMapping("/form/{id}")
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
         * public ResponseDTO<?> getSearVehicles(@PathVariable String id
         * ,@RequestParam(name = "formName", required = true) String formName){
         * return responseUtil.ok(vehicleService.getAllForms(id,formName),
         * ApiResponseCode.SUCCESS);
         * }
         */
        // @PreHandle(roles = {"Admin", "Mechanic"})

        // cmt bt prbr

        // @ApiOperation(value = "Get Distinct Model")
        // @ApiResponses(value = {
        // @ApiResponse(code = 201, message = "Created", response =
        // ApiResponseDTO.class),
        // @ApiResponse(code = 400, message = "Bad Request", response =
        // BadRequestResponseDTO.class),
        // @ApiResponse(code = 401, message = "You are Not Authenticated", response =
        // NotAuthenticatedResponseDTO.class),
        // @ApiResponse(code = 403, message = "Not Authorized on this resource",
        // response = AccessDeniedResponseDTO.class),
        // @ApiResponse(code = 404, message = "The resource you were trying to reach is
        // not found"),
        // })
        // @GetMapping("/model")
        // @ApiImplicitParams({
        // @ApiImplicitParam(name = "Authorization", value = "Access Token", required =
        // true, allowEmptyValue = false, paramType = "header", dataTypeClass =
        // String.class, example = "Bearer access_token"),
        // @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true,
        // allowEmptyValue = false, paramType = "header", dataTypeClass = String.class,
        // example = "evhub")
        // })
        // public ResponseDTO<?> getDistinctModel(@RequestParam(required = false)
        // List<String> brands,
        // @RequestParam(required = false) String model) {
        // return responseUtil.ok(vehicleService.getDistinctModel(brands, model),
        // ApiResponseCodeImpl.SUCCESS);
        // }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get Distinct Brand")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/brand")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getDistinctBrand(@RequestParam(required = false) String brand) {
                return responseUtil.ok(vehicleService.getDistinctBrand(brand), ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin", "Mechanic"})
        @ApiOperation(value = "Get Distinct engine")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @GetMapping("/engine")
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> getAllEngineCount(@RequestParam(required = false) Integer engineCount) {
                return responseUtil.ok(vehicleService.getEngineCount(engineCount), ApiResponseCodeImpl.SUCCESS);
        }

        // @PreHandle(roles = {"Admin"})
        @ApiOperation(value = "Update Vehicle Images")
        @ApiResponses(value = {
                        @ApiResponse(code = 201, message = "Created", response = ApiResponseDTO.class),
                        @ApiResponse(code = 400, message = "Bad Request", response = BadRequestResponseDTO.class),
                        @ApiResponse(code = 401, message = "You are Not Authenticated", response = NotAuthenticatedResponseDTO.class),
                        @ApiResponse(code = 403, message = "Not Authorized on this resource", response = AccessDeniedResponseDTO.class),
                        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
        })
        @PatchMapping
        @ApiImplicitParams({
                        @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token"),
                        @ApiImplicitParam(name = "x-realm", value = "Realm Name", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "evhub")
        })
        public ResponseDTO<?> updateVehicleAllImage(@RequestParam int min, @RequestParam int max) {
                vehicleService.compressAllImage(min, max);
                return responseUtil.ok();
        }

}
