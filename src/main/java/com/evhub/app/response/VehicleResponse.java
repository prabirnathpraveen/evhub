package com.evhub.app.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class VehicleResponse {
//    private String vehicleId;
    private String regNumber;
    private String brand;
    private String kilometer;
    private Long registrationDate;
    private String enginePower;
    private String enginePowerUnit;
    private String chassisNumber;
    private String model;
    private String batteryType;
    private String batterySize;
    private String batteryTypeUnit;
   private String batterySizeUnit;
    private Long firstRegisteredOn;
    private String importedUsed ;
    private Long euControl;
    private String servicingStatus;
    private String serviceNumber;
}
