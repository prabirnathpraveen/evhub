package com.evhub.app.entities;


import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Map;

@Document(collection = "vehicles")

public class Vehicle {

    @MongoId
    private String chassisNumber;
    private String regNumber;
    private String brand;

    private String kilometer;
    private Long registrationDate;
    //    private String enginePower;
//    private String enginePowerUnit;
//    private String modelNumber;
//    private String batteryType;
//    private String batteryTypeUnit;
//    private String batterySize;
//    private String batterySizeUnit;
    private Long firstRegisteredOn;
    private Long firstRegisteredInNorway;
    private String importedUsed;


    private String importedCountry;
    //    private Long euControl;
    private String model;
    private String manufacturer;
    //    private String yearOfManufacture;
    private Integer servicingStatus;
    //    private String category;
    private Long timestamp;
    private Double price;
    private Long lastServiceOn;
    private Long latestServiceTime;
    private String carGroup;
    private String color;
    private String maximumSpeed;
    private String carWeight;
    private String totalAllowedWeight;
    private int totalSeats;
    private int engineCount;
    private Long nextMandatoryInspection;
    private String electricRange;
    private String wheelDrive;
    private String kiloMeterUnit="km";
    private String electricRangeUnit="km";

    private String personalizedNumber;

    public String getPersonalizedNumber() {
        return personalizedNumber;
    }

    public void setPersonalizedNumber(String personalizedNumber) {
        this.personalizedNumber = personalizedNumber;
    }

    public String getKiloMeterUnit() {
        return kiloMeterUnit;
    }

    public void setKiloMeterUnit(String kiloMeterUnit) {
        this.kiloMeterUnit = kiloMeterUnit;
    }

    private String image;


    public String getElectricRangeUnit() {
        return electricRangeUnit;
    }

    public void setElectricRangeUnit(String electricRangeUnit) {
        this.electricRangeUnit = electricRangeUnit;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    private String disclaimer;

    public String getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public String getBrand() {
        return brand;
    }

    public String getKilometer() {
        return kilometer;
    }

    public void setKilometer(String kilometer) {
        this.kilometer = kilometer;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Long getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Long registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Long getFirstRegisteredOn() {
        return firstRegisteredOn;
    }

    public void setFirstRegisteredOn(Long firstRegisteredOn) {
        this.firstRegisteredOn = firstRegisteredOn;
    }

    public Long getFirstRegisteredInNorway() {
        return firstRegisteredInNorway;
    }

    public void setFirstRegisteredInNorway(Long firstRegisteredInNorway) {
        this.firstRegisteredInNorway = firstRegisteredInNorway;
    }

    public String getImportedUsed() {
        return importedUsed;
    }

    public void setImportedUsed(String importedUsed) {
        this.importedUsed = importedUsed;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getServicingStatus() {
        return servicingStatus;
    }

    public void setServicingStatus(Integer servicingStatus) {
        this.servicingStatus = servicingStatus;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getLastServiceOn() {
        return lastServiceOn;
    }

    public void setLastServiceOn(Long lastServiceOn) {
        this.lastServiceOn = lastServiceOn;
    }

    public Long getLatestServiceTime() {
        return latestServiceTime;
    }

    public void setLatestServiceTime(Long latestServiceTime) {
        this.latestServiceTime = latestServiceTime;
    }

    public String getCarGroup() {
        return carGroup;
    }

    public void setCarGroup(String carGroup) {
        this.carGroup = carGroup;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMaximumSpeed() {
        return maximumSpeed;
    }

    public void setMaximumSpeed(String maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
    }

    public String getCarWeight() {
        return carWeight;
    }

    public void setCarWeight(String carWeight) {
        this.carWeight = carWeight;
    }

    public String getTotalAllowedWeight() {
        return totalAllowedWeight;
    }

    public void setTotalAllowedWeight(String totalAllowedWeight) {
        this.totalAllowedWeight = totalAllowedWeight;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getEngineCount() {
        return engineCount;
    }

    public void setEngineCount(int engineCount) {
        this.engineCount = engineCount;
    }

    public Long getNextMandatoryInspection() {
        return nextMandatoryInspection;
    }

    public void setNextMandatoryInspection(Long nextMandatoryInspection) {
        this.nextMandatoryInspection = nextMandatoryInspection;
    }

    public String getElectricRange() {
        return electricRange;
    }

    public void setElectricRange(String electricRange) {
        this.electricRange = electricRange;
    }

    public String getHybridCategory() {
        return hybridCategory;
    }

    public void setHybridCategory(String hybridCategory) {
        this.hybridCategory = hybridCategory;
    }

    public Map<String, String> getEnginePowers() {
        return enginePowers;
    }

    public void setEnginePowers(Map<String, String> enginePowers) {
        this.enginePowers = enginePowers;
    }

    private String hybridCategory;
    private Map<String, String> enginePowers;

    public String getImportedCountry() {
        return importedCountry;
    }

    public void setImportedCountry(String importedCountry) {
        this.importedCountry = importedCountry;
    }

    public String getWheelDrive() {
        return wheelDrive;
    }

    public void setWheelDrive(String wheelDrive) {
        this.wheelDrive = wheelDrive;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
