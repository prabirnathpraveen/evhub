package com.evhub.app.controller;

import com.evhub.app.enums.ApiResponseCodeImpl;
import com.evhub.app.newclasses.ResponseDTO;
import com.evhub.app.newclasses.ResponseUtil;
import com.evhub.app.service.ItextPdf;
import com.evhub.app.service.PDFGenerator;
import com.evhub.app.service.PdfService;
// import com.flex83.app.annotations.PreHandle;
// import com.flex83.app.enums.ApiResponseCode;
// import com.flex83.app.response.generic.ResponseDTO;
// import com.flex83.app.response.utils.ResponseUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/pdf")
public class PdfGenerateController {

        @Autowired
        private PdfService pdfGenerator;

        @Autowired
        private ItextPdf itextPdf;
        // private PDFGenerator pdfGenerator;
        @Autowired
        private ResponseUtil responseUtil;

        // @PreHandle(roles = {"Admin","Mechanic","Customer"})
        @ApiOperation(value = "Get Service Number")
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

        public ResponseDTO<Object> getVehicles(@PathVariable String chassisNumber,
                        @RequestParam(name = "serviceNumber", required = false) String serviceNumber) throws Exception {
                // HttpHeaders headers = new HttpHeaders();
                // headers.add("content-type", "application/pdf");
                return responseUtil.ok(itextPdf.createNewPdf(chassisNumber, serviceNumber),
                                ApiResponseCodeImpl.SUCCESS);
                // return new
                // ResponseEntity<>(pdfGenerator.createPdfReport(chassisNumber,serviceNumber),
                // headers,HttpStatus.OK);
        }
}
