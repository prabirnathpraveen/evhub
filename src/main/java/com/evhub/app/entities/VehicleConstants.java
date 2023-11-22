package com.evhub.app.entities;





import com.evhub.app.repository.ServiceRecordRepository;
import com.evhub.app.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


public class VehicleConstants {
    private  static String[] formKeys = {"regNumber","chassisNumber","model","registrationDate","manufacturer","batteryType","batterySize","mileage","enginePower"};
    private  static String[] image = {"https://dev-evhub.apps83.com/3eb990d599c620a1db4c5d75a3c2636f.png","https://dev-evhub.apps83.com/1dcdb9a1bc11eef0915a053226f4f112.png"};
    private Map<String,Object> faultMap= new HashMap<>() ;
    private Map<String, Object> attributesMap = new HashMap<>();
    private Map<String, Object> attributesMapStatus = new HashMap<>();

    public Map<String,Object> getVehicleReportValue(Vehicle vehicle,ServiceRecord serviceRecord) {
        Map<String, Object> reportValue = new HashMap<>();
        reportValue.put("_regNumber_", checkNullField(vehicle.getRegNumber()));
        reportValue.put("_chassisNumber_", checkNullField(vehicle.getChassisNumber()));
        reportValue.put("_model_", checkNullField(vehicle.getModel()));
        Long registrationDate = vehicle.getRegistrationDate();;
        reportValue.put("_registrationDate_",Objects.nonNull(registrationDate)?CommonUtils.DateFormatter(new Date(registrationDate)):"");
        reportValue.put("_manufacturer_", checkNullField(vehicle.getManufacturer()));
//        reportValue.put("_batteryType_", vehicle.getBatteryType()+" "+vehicle.getBatteryTypeUnit());
//        reportValue.put("_batterySize_", vehicle.getBatterySize()+" "+vehicle.getBatterySizeUnit());
        Map<String, String> enginePowers = vehicle.getEnginePowers();
        reportValue.put("_enginePower_", Objects.nonNull(registrationDate)?enginePowers.toString():"");
//        reportValue.put("_mileage_", vehicle.getKilometer());
        Double servicePrice = serviceRecord.getServicePrice();
        reportValue.put("_servicePrice_", Objects.nonNull(servicePrice)?String.valueOf(servicePrice):"");
        reportValue.put("_brand_",checkNullField(vehicle.getBrand()));
        reportValue.put("_image_main_",checkNullField(getImageSource(vehicle.getBrand())));


//       Map form = (Map) vehicle.getServiceRecords().get(serviceNumber).getForm();

//        reportValue.putAll( extracted(reportValue, form));
        return reportValue;
    }

    private Object getImageSource(String brand) {
        return "Tesla Model 3".equalsIgnoreCase(brand)?image[0]:image[1];
    }
private Object checkNullField(Object fieldValue){
        return Objects.nonNull(fieldValue)?fieldValue:"";
}

/*
private Map<String,Object> extracted(Map<String, Object> reportValue, Map form) {
        List<OverAllHealthResponse> overAllHealthResponses= new ArrayList<>();
        form.forEach((k, v) -> {
            Map value = (Map) v;
            OverAllHealthResponse overAllHealthResponse = new OverAllHealthResponse();
            overAllHealthResponse.setFormName((String) k);
            overAllHealthResponse.setStatus(VehicleUtil.getOverAllStatus(value));
            overAllHealthResponses.add(overAllHealthResponse);
        });
//        System.out.print(overallHealth(overAllHealthResponses));
        overAllHealthResponses.forEach(value-> {
            if (value.getFormName().equals("Test driving")) {
                reportValue.put("overTest", value.getStatus());
                reportValue.put("_test_driving_colour", getColor(value.getStatus()));
                reportValue.put("_test_driving_width", getWidth(value.getStatus()));
            } else if (value.getFormName().equals("Brakes")) {
                reportValue.put("overBrakes", value.getStatus());
                reportValue.put("_brake_color", getColor(value.getStatus()));
                reportValue.put("_brake_width", getWidth(value.getStatus()));
            } else if (value.getFormName().equals("Brakes - Visual inspection")) {
                reportValue.put("overVisual", value.getStatus());
                reportValue.put("_brake_visual_color", getColor(value.getStatus()));
                reportValue.put("_brake_visual_width", getWidth(value.getStatus()));
            } else if (value.getFormName().equals("Lights outside 1 of 2")) {
                reportValue.put("overLight1", value.getStatus());
                reportValue.put("_light_outside1_color", getColor(value.getStatus()));
                reportValue.put("_light_outside1_width", getWidth(value.getStatus()));
            } else if (value.getFormName().equals("Lights outside 2 of 2")) {
                reportValue.put("overLight2", value.getStatus());
                reportValue.put("_light_outside2_color", getColor(value.getStatus()));
                reportValue.put("_light_outside2_width", getWidth(value.getStatus()));
            } else if (value.getFormName().equals("Drive Unit")) {
                reportValue.put("overDrive", value.getStatus());
                reportValue.put("_Drive_unit_color", getColor(value.getStatus()));
                reportValue.put("_Drive_unit_width", getWidth(value.getStatus()));
            } else if (value.getFormName().equals("Drive belt/Gear")) {
                reportValue.put("overGear", value.getStatus());
                reportValue.put("_Drive_gear_Color", getColor(value.getStatus()));
                reportValue.put("_Drive_gear_width", getWidth(value.getStatus()));
            } else if (value.getFormName().equals("Electrical components 1 of 3")){
                reportValue.put("overElectric1", value.getStatus());
            reportValue.put("_electric_1_color", getColor(value.getStatus()));
            reportValue.put("_electric_1_width", getWidth(value.getStatus()));
        }
            else if (value.getFormName().equals("Electrical components 2 of 3")) {
                reportValue.put("overElectric2", value.getStatus());
                reportValue.put("_electric_2_color", getColor(value.getStatus()));
                reportValue.put("_electric_2_width", getWidth(value.getStatus()));
            }

            else if (value.getFormName().equals("Electrical components 3 of 3")) {
                reportValue.put("overElectric3", value.getStatus());
                reportValue.put("_electric_3_color", getColor(value.getStatus()));
                reportValue.put("_electric_3_width", getWidth(value.getStatus()));
            }
            else if (value.getFormName().equals("The main battery")) {
                reportValue.put("overBattery", value.getStatus());
                reportValue.put("_main_battery_color", getColor(value.getStatus()));
                reportValue.put("_main_battery_width", getWidth(value.getStatus()));
            }
            else if (value.getFormName().equals("Steering")) {
                reportValue.put("overSteering", value.getStatus());
                reportValue.put("_steering_color", getColor(value.getStatus()));
                reportValue.put("_steering_width", getWidth(value.getStatus()));
            }
            else if (value.getFormName().equals("Tire")) {
                reportValue.put("overTyre", value.getStatus());
                reportValue.put("_tyre_color_color", getColor(value.getStatus()));
                reportValue.put("_tyre_color_width", getWidth(value.getStatus()));
            }
            else if (value.getFormName().equals("Chassis/Suspension")) {
                reportValue.put("overChassis", value.getStatus());
                reportValue.put("_chassis_color", getColor(value.getStatus()));
                reportValue.put("_chassis_width", getWidth(value.getStatus()));
            }
            else if (value.getFormName().equals("Body 1 of 2")) {
                reportValue.put("overBody1", value.getStatus());
                reportValue.put("_body1_color", getColor(value.getStatus()));
                reportValue.put("_body1_width", getWidth(value.getStatus()));
            }
            else if (value.getFormName().equals("Body 2 of 2")) {
                reportValue.put("overBody2", value.getStatus());
                reportValue.put("_body2_color", getColor(value.getStatus()));
                reportValue.put("_body2_width", getWidth(value.getStatus()));
            }
            else if (value.getFormName().equals("Interior 1 of 2")) {
                reportValue.put("overInterior1", value.getStatus());
                reportValue.put("_interior1_color", getColor(value.getStatus()));
                reportValue.put("_interior1_width", getWidth(value.getStatus()));
            }
            else if (value.getFormName().equals("Interior 2 of 2")) {
                reportValue.put("overInterior2", value.getStatus());
                reportValue.put("_interior1_color", getColor(value.getStatus()));
                reportValue.put("_interior1_width", getWidth(value.getStatus()));
            }
            else if (value.getFormName().equals("General elements")) {
                reportValue.put("overGeneral", value.getStatus());
                reportValue.put("_general_element_color", getColor(value.getStatus()));
                reportValue.put("_general_element_width", getWidth(value.getStatus()));
            }
            });
        return  reportValue;
    }



    public  String getWidth(String status) {
        return status.equalsIgnoreCase("OK")?"80%;":status.equalsIgnoreCase("Next Service")?"60%;":
                status.equals("Not OK, must be repaired now!")?"25%;":"60%;";
    }
    private String getColor(String status) {
        return status.equalsIgnoreCase("OK")?"#07bc0c;":status.equalsIgnoreCase("Next Service")?"#f1c40f;":
                status.equalsIgnoreCase("Not OK, must be repaired now!")?"#e74c3c;":"#f1c40f;";
    }


    public List<Map<String, Object>> getTestDriveReport(ServiceRecord serviceRecord, int i) {
        List<Map<String,Object>> mapList = new ArrayList<>();
        Map<String,Object> testDriveValue = new HashMap<>();
        String formName="Test driving";
        Map testDriving = (Map) ((Map)serviceRecord.getForm()).get("Test driving");
        if (Objects.nonNull(testDriving)){
        testDriveValue.put("_highwayDrivingStatus", getStatus(formName,"Highway Driving",((Map) testDriving.get("highwayDriving")).get("status")));
        testDriveValue.put("_highwayDrivingResult", getResultStatus(((Map) testDriving.get("highwayDriving")).get("result")));
        testDriveValue.put("_highwayDrivingMeasuredValue", ((Map) testDriving.get("highwayDriving")).get("measuredValues")==null?"NA":((Map) testDriving.get("highwayDriving")).get("measuredValues"));
        testDriveValue.put("_highwayDrivingRemarks", ((Map) testDriving.get("highwayDriving")).get("description")==null?"NA":((Map) testDriving.get("highwayDriving")).get("description"));
        testDriveValue.put("_color_highwayDriving", getColor((String) ((Map) testDriving.get("highwayDriving")).get("status")));

        testDriveValue.put("_numberOfTestDriveKilometersStatus", getStatus(formName,"Number Of TestDrive Kilometers",((Map) testDriving.get("numberOfTestDriveKilometers")).get("status")));
        testDriveValue.put("_numberOfTestDriveKilometersResult", getResultStatus(((Map) testDriving.get("numberOfTestDriveKilometers")).get("result")));
        testDriveValue.put("_numberOfTestDriveKilometersMeasuredValue", ((Map) testDriving.get("numberOfTestDriveKilometers")).get("measuredValues")==null?"NA":((Map) testDriving.get("numberOfTestDriveKilometers")).get("measuredValues"));
        testDriveValue.put("_numberOfTestDriveKilometersRemarks", ((Map) testDriving.get("numberOfTestDriveKilometers")).get("description")==null?"NA":((Map) testDriving.get("numberOfTestDriveKilometers")).get("description"));
        testDriveValue.put("_color_numberOfTestDriveKilometers", getColor((String) ((Map) testDriving.get("numberOfTestDriveKilometers")).get("status")));

        testDriveValue.put("_roadDrivingStatus", getStatus(formName,"Road Driving",((Map) testDriving.get("roadDriving")).get("status")));
        testDriveValue.put("_roadDrivingResult", getResultStatus(((Map) testDriving.get("roadDriving")).get("result")));
        testDriveValue.put("_roadDrivingMeasuredValue", ((Map) testDriving.get("roadDriving")).get("measuredValues")==null?"NA":((Map) testDriving.get("roadDriving")).get("measuredValues"));
        testDriveValue.put("_roadDrivingRemarks", ((Map) testDriving.get("roadDriving")).get("description")==null?"NA":((Map) testDriving.get("roadDriving")).get("description"));
        testDriveValue.put("_color_roadDriving", getColor((String) ((Map) testDriving.get("roadDriving")).get("status")));

        testDriveValue.put("_testdrivespeedStatus", getStatus(formName,"Test Drive Speed",((Map) testDriving.get("testdrivespeed")).get("status")));
        testDriveValue.put("_testdrivespeedResult", getResultStatus(((Map) testDriving.get("testdrivespeed")).get("result")));
        testDriveValue.put("_testdrivespeedMeasuredValue", ((Map) testDriving.get("testdrivespeed")).get("measuredValues")==null?"NA":((Map) testDriving.get("testdrivespeed")).get("measuredValues"));
        testDriveValue.put("_testdrivespeedRemarks", ((Map) testDriving.get("testdrivespeed")).get("description")==null?"NA":((Map) testDriving.get("testdrivespeed")).get("description"));
        testDriveValue.put("_color_testdrivespeed", getColor((String) ((Map) testDriving.get("testdrivespeed")).get("status")));

        testDriveValue.put("_conditionsDuringTheTestDriveStatus", getStatus(formName,"Conditions During The TestDrive",((Map) testDriving.get("conditionsDuringTheTestDrive")).get("status")));
        testDriveValue.put("_conditionsDuringTheTestDriveResult", getResultStatus(((Map) testDriving.get("conditionsDuringTheTestDrive")).get("result")));
        testDriveValue.put("_conditionsDuringTheTestDriveMeasuredValue", ((Map) testDriving.get("conditionsDuringTheTestDrive")).get("measuredValues")==null?"NA":((Map) testDriving.get("conditionsDuringTheTestDrive")).get("measuredValues"));
        testDriveValue.put("_conditionsDuringTheTestDriveRemarks", ((Map) testDriving.get("conditionsDuringTheTestDrive")).get("description")==null?"NA":((Map) testDriving.get("conditionsDuringTheTestDrive")).get("description"));
        testDriveValue.put("_color_conditionsDuringTheTestDrive", getColor((String) ((Map) testDriving.get("conditionsDuringTheTestDrive")).get("status")));

        testDriveValue.put("_highwayDrivingStatus", getStatus(formName,"Highway Driving",((Map) testDriving.get("highwayDriving")).get("status")));
        testDriveValue.put("_highwayDrivingResult", getResultStatus(((Map) testDriving.get("highwayDriving")).get("result")));
        testDriveValue.put("_highwayDrivingMeasuredValue", ((Map) testDriving.get("highwayDriving")).get("measuredValues")==null?"NA":((Map) testDriving.get("highwayDriving")).get("measuredValues"));
        testDriveValue.put("_highwayDrivingRemarks", ((Map) testDriving.get("highwayDriving")).get("description")==null?"NA":((Map) testDriving.get("highwayDriving")).get("description"));
        testDriveValue.put("_color_highwayDriving", getColor((String) ((Map) testDriving.get("highwayDriving")).get("status")));

        testDriveValue.put("_drivingUphillStatus", getStatus(formName,"Driving Uphill",((Map) testDriving.get("drivingUphill")).get("status")));
        testDriveValue.put("_drivingUphillResult", getResultStatus(((Map) testDriving.get("drivingUphill")).get("result")));
        testDriveValue.put("_drivingUphillMeasuredValue", ((Map) testDriving.get("drivingUphill")).get("measuredValues")==null?"NA":((Map) testDriving.get("drivingUphill")).get("measuredValues"));
        testDriveValue.put("_drivingUphillRemarks", ((Map) testDriving.get("drivingUphill")).get("description")==null?"NA":((Map) testDriving.get("drivingUphill")).get("description"));
        testDriveValue.put("_color_drivingUphill", getColor((String) ((Map) testDriving.get("drivingUphill")).get("status")));

        mapList.add(testDriveValue);
        faultMap.put(formName,attributesMap);
        mapList.add(faultMap);
        return mapList;
        } else {

            testDriveValue.put("_highwayDrivingStatus", "NA");
            testDriveValue.put("_highwayDrivingResult", "NA");
            testDriveValue.put("_highwayDrivingMeasuredValue", "NA");
            testDriveValue.put("_highwayDrivingRemarks", "NA");
            testDriveValue.put("_color_highwayDriving", "NA");

            testDriveValue.put("_numberOfTestDriveKilometersStatus", "NA");
            testDriveValue.put("_numberOfTestDriveKilometersResult", "NA");
            testDriveValue.put("_numberOfTestDriveKilometersMeasuredValue", "NA");
            testDriveValue.put("_numberOfTestDriveKilometersRemarks", "NA");

            testDriveValue.put("_roadDrivingStatus", "NA");
            testDriveValue.put("_roadDrivingResult", "NA");
            testDriveValue.put("_roadDrivingMeasuredValue", "NA");
            testDriveValue.put("_roadDrivingRemarks", "NA");

            testDriveValue.put("_testdrivespeedStatus", "NA");
            testDriveValue.put("_testdrivespeedResult", "NA");
            testDriveValue.put("_testdrivespeedMeasuredValue", "NA");
            testDriveValue.put("_testdrivespeedRemarks", "NA");

            testDriveValue.put("_conditionsDuringTheTestDriveStatus", "NA");
            testDriveValue.put("_conditionsDuringTheTestDriveResult", "NA");
            testDriveValue.put("_conditionsDuringTheTestDriveMeasuredValue", "NA");
            testDriveValue.put("_conditionsDuringTheTestDriveRemarks", "NA");

            testDriveValue.put("_highwayDrivingStatus", "NA");
            testDriveValue.put("_highwayDrivingResult", "NA");
            testDriveValue.put("_highwayDrivingMeasuredValue", "NA");
            testDriveValue.put("_highwayDrivingRemarks", "NA");

            testDriveValue.put("_drivingUphillStatus", "NA");
            testDriveValue.put("_drivingUphillResult", "NA");
            testDriveValue.put("_drivingUphillMeasuredValue","NA");
            testDriveValue.put("_drivingUphillRemarks", "NA");
            mapList.add(testDriveValue);
            return mapList;
        }
    }



 public List<Map<String, Object>> getBrakesReport(ServiceRecord serviceRecord, int i) {
//errorAndDeficiency
        String formName="Brakes";
        Map brakes.png = (Map) ((Map)serviceRecord.getForm()).get(formName);
        if(Objects.nonNull(brakes.png)) {
            Map<String,Object> brakesValue = new HashMap<>();

            brakesValue.put("_brakeFunctionStatus", getStatus(formName,"Brake Function",((Map) brakes.png.get("brakeFunction")).get("status")));
            brakesValue.put("_brakeFunctionMeasuredValue", ((Map) brakes.png.get("brakeFunction")).get("measuredValues") == null ? "NA" : ((Map) brakes.png.get("brakeFunction")).get("measuredValues"));
            brakesValue.put("_brakeFunctionResult", getResultStatus(((Map) brakes.png.get("brakeFunction")).get("result")));
            brakesValue.put("_brakeFunctionRemarks", ((Map) brakes.png.get("brakeFunction")).get("description") == null ? "NA" : ((Map) brakes.png.get("brakeFunction")).get("description"));
            brakesValue.put("_brakeFunctionError", ((Map) brakes.png.get("brakeFunction")).get("errorAndDeficiency") == null ? "NA" : ((Map) brakes.png.get("brakeFunction")).get("errorAndDeficiency"));
            brakesValue.put("_color_brakeFunction", getColor((String) ((Map) brakes.png.get("brakeFunction")).get("status")));

            brakesValue.put("_brakeDiscsStatus", getStatus(formName,"Brake Discs",((Map) brakes.png.get("brakeDiscs")).get("status")));
            brakesValue.put("_brakeDiscsMeasuredValue", ((Map) brakes.png.get("brakeDiscs")).get("measuredValues") == null ? "NA" : ((Map) brakes.png.get("brakeDiscs")).get("measuredValues"));
            brakesValue.put("_brakeDiscsResult", getResultStatus(((Map) brakes.png.get("brakeDiscs")).get("result")));
            brakesValue.put("_brakeDiscsRemarks", ((Map) brakes.png.get("brakeDiscs")).get("description") == null ? "NA" : ((Map) brakes.png.get("brakeDiscs")).get("description"));
            brakesValue.put("_brakeDiscsError", ((Map) brakes.png.get("brakeDiscs")).get("errorAndDeficiency") == null ? "NA" : ((Map) brakes.png.get("brakeDiscs")).get("errorAndDeficiency"));
            brakesValue.put("_color_brakeDiscs", getColor((String) ((Map) brakes.png.get("brakeDiscs")).get("status")));

            brakesValue.put("_brakePadsStatus", getStatus(formName,"Brake Pads",((Map) brakes.png.get("brakePads")).get("status")));
            brakesValue.put("_brakePadsResult", getResultStatus(((Map) brakes.png.get("brakePads")).get("brakePads")));
            brakesValue.put("_brakePadsMeasuredValue", ((Map) brakes.png.get("brakePads")).get("measuredValues") == null ? "NA" : ((Map) brakes.png.get("brakePads")).get("measuredValues"));
            brakesValue.put("_brakePadsRemarks", ((Map) brakes.png.get("brakePads")).get("description") == null ? "NA" : ((Map) brakes.png.get("brakePads")).get("description"));
            brakesValue.put("_brakePadsError", ((Map) brakes.png.get("brakePads")).get("errorAndDeficiency") == null ? "NA" : ((Map) brakes.png.get("brakePads")).get("errorAndDeficiency"));
            brakesValue.put("_color_brakePads", getColor((String) ((Map) brakes.png.get("brakePads")).get("status")));

            brakesValue.put("_brakeCalipersStatus", getStatus(formName,"Brake Calipers",((Map) brakes.png.get("brakeCalipers")).get("status")));
            brakesValue.put("_brakeCalipersMeasuredValue", ((Map) brakes.png.get("brakeCalipers")).get("measuredValues") == null ? "NA" : ((Map) brakes.png.get("brakeCalipers")).get("measuredValues"));
            brakesValue.put("_brakeCalipersResult", getResultStatus(((Map) brakes.png.get("brakeCalipers")).get("result")));
            brakesValue.put("_brakeCalipersRemarks", ((Map) brakes.png.get("brakeCalipers")).get("description") == null ? "NA" : ((Map) brakes.png.get("brakeCalipers")).get("description"));
            brakesValue.put("_brakeCalipersError", ((Map) brakes.png.get("brakeCalipers")).get("errorAndDeficiency") == null ? "NA" : ((Map) brakes.png.get("brakeCalipers")).get("errorAndDeficiency"));
            brakesValue.put("_color_brakeCalipers", getColor((String) ((Map) brakes.png.get("brakeCalipers")).get("status")));

            brakesValue.put("_parkingbrakeCalipersDriveStatus", getStatus(formName,"Parking Brake Calipers",((Map) brakes.png.get("parkingbrakeCalipers")).get("status")));
            brakesValue.put("_parkingbrakeCalipersMeasuredValue", ((Map) brakes.png.get("parkingbrakeCalipers")).get("measuredValues") == null ? "NA" : ((Map) brakes.png.get("parkingbrakeCalipers")).get("measuredValues"));
            brakesValue.put("_parkingbrakeCalipersDriveResult", getResultStatus(((Map) brakes.png.get("parkingbrakeCalipers")).get("result")));
            brakesValue.put("_parkingbrakeCalipersRemarks", ((Map) brakes.png.get("parkingbrakeCalipers")).get("description") == null ? "NA" : ((Map) brakes.png.get("parkingbrakeCalipers")).get("description"));
            brakesValue.put("_parkingbrakeCalipersError", ((Map) brakes.png.get("parkingbrakeCalipers")).get("errorAndDeficiency") == null ? "NA" : ((Map) brakes.png.get("parkingbrakeCalipers")).get("errorAndDeficiency"));
            brakesValue.put("_color_parkingbrakeCalipers", getColor((String) ((Map) brakes.png.get("parkingbrakeCalipers")).get("status")));

            brakesValue.put("_brakeCaliperNipplesStatus", getStatus(formName,"Brake Caliper Nipples",((Map) brakes.png.get("brakeCaliperNipples")).get("status")));
            brakesValue.put("_brakeCaliperNipplesMeasuredValue", ((Map) brakes.png.get("brakeCaliperNipples")).get("measuredValues") == null ? "NA" : ((Map) brakes.png.get("brakeCaliperNipples")).get("measuredValues"));
            brakesValue.put("_brakeCaliperNipplesResult", getResultStatus(((Map) brakes.png.get("brakeCaliperNipples")).get("result")));
            brakesValue.put("_brakeCaliperNipplesRemarks", ((Map) brakes.png.get("brakeCaliperNipples")).get("description") == null ? "NA" : ((Map) brakes.png.get("brakeCaliperNipples")).get("description"));
            brakesValue.put("_brakeCaliperNipplesError", ((Map) brakes.png.get("brakeCaliperNipples")).get("errorAndDeficiency") == null ? "NA" : ((Map) brakes.png.get("brakeCaliperNipples")).get("errorAndDeficiency"));
            brakesValue.put("_color_brakeCaliperNipples", getColor((String) ((Map) brakes.png.get("brakeCaliperNipples")).get("status")));

            brakesValue.put("_brakeFluidStatus", getStatus(formName,"Brake Fluid",((Map) brakes.png.get("brakeFluid")).get("status")));
            brakesValue.put("_brakeFluidMeasuredValue", ((Map) brakes.png.get("brakeFluid")).get("measuredValues") == null ? "NA" : ((Map) brakes.png.get("brakeFluid")).get("measuredValues"));
            brakesValue.put("_brakeFluidResult", getResultStatus(((Map) brakes.png.get("brakeFluid")).get("result")));
            brakesValue.put("_brakeFluidRemarks", ((Map) brakes.png.get("brakeFluid")).get("description") == null ? "NA" : ((Map) brakes.png.get("brakeFluid")).get("description"));
            brakesValue.put("_brakeFluidError", ((Map) brakes.png.get("brakeFluid")).get("errorAndDeficiency") == null ? "NA" : ((Map) brakes.png.get("brakeFluid")).get("errorAndDeficiency"));
            brakesValue.put("_color_brakeFluid", getColor((String) ((Map) brakes.png.get("brakeFluid")).get("status")));

            brakesValue.put("_disassembleBrakeCalipersStatus", getStatus(formName,"Disassemble Brake Calipers",((Map) brakes.png.get("disassembleBrakeCalipers")).get("status")));
            brakesValue.put("_disassembleBrakeCalipersMeasuredValue", ((Map) brakes.png.get("disassembleBrakeCalipers")).get("measuredValues") == null ? "NA" : ((Map) brakes.png.get("disassembleBrakeCalipers")).get("measuredValues"));
            brakesValue.put("_disassembleBrakeCalipersResult", getResultStatus(((Map) brakes.png.get("disassembleBrakeCalipers")).get("result")));
            brakesValue.put("_disassembleBrakeCalipersRemarks", ((Map) brakes.png.get("disassembleBrakeCalipers")).get("description") == null ? "NA" : ((Map) brakes.png.get("disassembleBrakeCalipers")).get("description"));
            brakesValue.put("_disassembleBrakeCalipersError", ((Map) brakes.png.get("disassembleBrakeCalipers")).get("errorAndDeficiency") == null ? "NA" : ((Map) brakes.png.get("disassembleBrakeCalipers")).get("errorAndDeficiency"));
            brakesValue.put("_color_disassembleBrakeCalipers", getColor((String) ((Map) brakes.png.get("disassembleBrakeCalipers")).get("status")));

            brakesValue.put("_brakeDrumsStatus", getStatus(formName,"Brake Drums",((Map) brakes.png.get("brakeDrums")).get("status")));
            brakesValue.put("_brakeDrumsMeasuredValue", ((Map) brakes.png.get("brakeDrums")).get("measuredValues") == null ? "NA" : ((Map) brakes.png.get("brakeDrums")).get("measuredValues"));
            brakesValue.put("_brakeDrumsResult", getResultStatus(((Map) brakes.png.get("brakeDrums")).get("result")));
            brakesValue.put("_brakeDrumsRemarks", ((Map) brakes.png.get("brakeDrums")).get("description") == null ? "NA" : ((Map) brakes.png.get("brakeDrums")).get("description"));
            brakesValue.put("_brakeDrumsError", ((Map) brakes.png.get("brakeDrums")).get("errorAndDeficiency") == null ? "NA" : ((Map) brakes.png.get("brakeDrums")).get("errorAndDeficiency"));
            brakesValue.put("_color_brakeDrums", getColor((String) ((Map) brakes.png.get("brakeDrums")).get("status")));

            brakesValue.put("_brakeWiresStatus", getStatus(formName,"Brake Wires",((Map) brakes.png.get("brakeWires")).get("status")));
            brakesValue.put("_brakeWiresMeasuredValue", ((Map) brakes.png.get("brakeWires")).get("measuredValues") == null ? "NA" : ((Map) brakes.png.get("brakeWires")).get("measuredValues"));
            brakesValue.put("_brakeWiresResult", getResultStatus(((Map) brakes.png.get("brakeWires")).get("result")));
            brakesValue.put("_brakeWiresRemarks", ((Map) brakes.png.get("brakeWires")).get("description") == null ? "NA" : ((Map) brakes.png.get("brakeWires")).get("description"));
            brakesValue.put("_brakeWiresError", ((Map) brakes.png.get("brakeWires")).get("errorAndDeficiency") == null ? "NA" : ((Map) brakes.png.get("brakeWires")).get("errorAndDeficiency"));
            brakesValue.put("_color_brakeWires", getColor((String) ((Map) brakes.png.get("brakeWires")).get("status")));

            List<Map<String,Object>> mapList = new ArrayList<>();
            mapList.add(brakesValue);
            mapList.add(faultMap);
            mapList.add(attributesMapStatus);
            return mapList;
        }else {
            Map<String,Object> brakesValue = new HashMap<>();
            brakesValue.put("_brakeFunctionStatus", "NA");
            brakesValue.put("_brakeFunctionMeasuredValue", "NA");
            brakesValue.put("_brakeFunctionResult", "NA");
            brakesValue.put("_brakeFunctionRemarks", "NA");
            brakesValue.put("_brakeFunctionError","NA");

            brakesValue.put("_brakeDiscsStatus", "NA");
            brakesValue.put("_brakeDiscsMeasuredValue", "NA");
            brakesValue.put("_brakeDiscsResult", "NA");
            brakesValue.put("_brakeDiscsRemarks", "NA");
            brakesValue.put("_brakeDiscsError", "NA");

            brakesValue.put("_brakePadsStatus", "NA");
            brakesValue.put("_brakePadsResult", "NA");
            brakesValue.put("_brakePadsMeasuredValue", "NA");
            brakesValue.put("_brakePadsRemarks", "NA");
            brakesValue.put("_brakePadsError", "NA");

            brakesValue.put("_brakeCalipersStatus","NA");
            brakesValue.put("_brakeCalipersMeasuredValue", "NA");
            brakesValue.put("_brakeCalipersResult", "NA");
            brakesValue.put("_brakeCalipersRemarks", "NA");
            brakesValue.put("_brakeCalipersError", "NA");

            brakesValue.put("_parkingbrakeCalipersDriveStatus", "NA");
            brakesValue.put("_parkingbrakeCalipersMeasuredValue", "NA");
            brakesValue.put("_parkingbrakeCalipersDriveResult", "NA");
            brakesValue.put("_parkingbrakeCalipersRemarks", "NA");
            brakesValue.put("_parkingbrakeCalipersError", "NA");

            brakesValue.put("_brakeCaliperNipplesStatus", "NA");
            brakesValue.put("_brakeCaliperNipplesMeasuredValue", "NA");
            brakesValue.put("_brakeCaliperNipplesResult", "NA");
            brakesValue.put("_brakeCaliperNipplesRemarks", "NA");
            brakesValue.put("_brakeCaliperNipplesError", "NA");

            brakesValue.put("_brakeFluidStatus", "NA");
            brakesValue.put("_brakeFluidMeasuredValue", "NA");
            brakesValue.put("_brakeFluidResult", "NA");
            brakesValue.put("_brakeFluidRemarks", "NA");
            brakesValue.put("_brakeFluidError", "NA");

            brakesValue.put("_disassembleBrakeCalipersStatus", "NA");
            brakesValue.put("_disassembleBrakeCalipersMeasuredValue", "NA");
            brakesValue.put("_disassembleBrakeCalipersResult","NA");
            brakesValue.put("_disassembleBrakeCalipersRemarks", "NA");
            brakesValue.put("_disassembleBrakeCalipersError", "NA");

            brakesValue.put("_brakeDrumsStatus", "NA");
            brakesValue.put("_brakeDrumsMeasuredValue", "NA");
            brakesValue.put("_brakeDrumsResult","NA");
            brakesValue.put("_brakeDrumsRemarks", "NA");
            brakesValue.put("_brakeDrumsError", "NA");

            brakesValue.put("_brakeWiresStatus", "NA");
            brakesValue.put("_brakeWiresMeasuredValue", "NA");
            brakesValue.put("_brakeWiresResult", "NA");
            brakesValue.put("_brakeWiresRemarks", "NA");
            brakesValue.put("_brakeWiresError", "NA");
            List<Map<String,Object>> mapList = new ArrayList<>();
            mapList.add(brakesValue);
            return mapList;
        }
    }


    private Object getResultStatus(Object result){
        if(Objects.nonNull(result)){
            return  ( (Map) result).get("label")==null?"NA":( (Map) result).get("label");
        }else
            return "NA";
    }
    private Object getStatus(String formName,String attributes,Object status){
        if(Objects.nonNull(status)){
            String mainStatus = ((String) status);
            if(mainStatus.equalsIgnoreCase("Not OK, must be repaired now!")){
                attributesMap.put(attributes,status);
            }
            int number =mainStatus.equalsIgnoreCase("Ok")?1:mainStatus.equalsIgnoreCase("Not OK, must be repaired now!")?2:1;
            attributesMapStatus.put(attributes,number);
            return  status;
        }else
            return "NA";
    }

    public List<Map<String, Object>> getBrakeVisualReport(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            String formName="Brakes - Visual inspection";
            brakesVisualValue.put("_BrakeHosesStatus", getStatus(formName,"Brake Hoses",((Map) brakesVisual.get("BrakeHoses")).get("status")));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));
            brakesVisualValue.put("_color_BrakeHoses", getColor((String) ((Map) brakesVisual.get("BrakeHoses")).get("status")));

            brakesVisualValue.put("_brakePipesStatus", getStatus(formName,"Brake Pipes",((Map) brakesVisual.get("brakePipes")).get("status")));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));
            brakesVisualValue.put("_color_brakePipes", getColor((String) ((Map) brakesVisual.get("brakePipes")).get("status")));

            brakesVisualValue.put("_brakePedalStatus",getStatus (formName,"Brake Pedal",((Map) brakesVisual.get("brakePedal")).get("status") ));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
            brakesVisualValue.put("_color_brakePedal", getColor((String) ((Map) brakesVisual.get("brakePedal")).get("status")));

            List<Map<String,Object>> mapList = new ArrayList<>();
            mapList.add(brakesVisualValue);
            mapList.add(faultMap);
            return mapList;
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        List<Map<String,Object>> mapList = new ArrayList<>();
        mapList.add(brakesVisualValue);
        return mapList;
    }

    public Map<String, Object> getFaultMap() {
        return faultMap;
    }

    public Map<String, Object> getLightOutSide1(ServiceRecord serviceRecord, int i) {
        Map<String,Object> lightOutside1Val = new HashMap<>();
        Map lightOutside1 = (Map) ((Map)serviceRecord.getForm()).get("Lights outside 1 of 2");

//        if(Objects.nonNull(lightOutside1)) {
            try{
                lightOutside1Val.put("_lowBeam_status", ((Map) lightOutside1.get("lowBeam")).get("status") == null ? "NA" : ((Map) lightOutside1.get("lowBeam")).get("status"));
                lightOutside1Val.put("_lowBeam_result", getResultStatus(((Map) lightOutside1.get("lowBeam")).get("result")));
                lightOutside1Val.put("_lowBeam_Value", ((Map) lightOutside1.get("lowBeam")).get("description") == null ? "NA" : ((Map) lightOutside1.get("lowBeam")).get("description"));

                lightOutside1Val.put("_highBeam_status", ((Map) lightOutside1.get("highBeam")).get("status") == null ? "NA" : ((Map) lightOutside1.get("highBeam")).get("status"));
                lightOutside1Val.put("_highBeam_result", getResultStatus(((Map) lightOutside1.get("highBeam")).get("result")));
                lightOutside1Val.put("_highBeam_Value", ((Map) lightOutside1.get("highBeam")).get("description") == null ? "NA" : ((Map) lightOutside1.get("highBeam")).get("description"));

                lightOutside1Val.put("_flashing_status", ((Map) lightOutside1.get("flashingLight")).get("status") == null ? "NA" : ((Map) lightOutside1.get("flashingLight")).get("status"));
                lightOutside1Val.put("_flashing_result", getResultStatus(((Map) lightOutside1.get("flashingLight")).get("result")));
                lightOutside1Val.put("_flashing_Value", ((Map) lightOutside1.get("flashingLight")).get("description") == null ? "NA" : ((Map) lightOutside1.get("flashingLight")).get("description"));

                lightOutside1Val.put("_parking_status", ((Map) lightOutside1.get("parkingLight")).get("status") == null ? "NA" : ((Map) lightOutside1.get("parkingLight")).get("status"));
                lightOutside1Val.put("_parking_result", getResultStatus(((Map) lightOutside1.get("parkingLight")).get("result")));
                lightOutside1Val.put("_parking_Value", ((Map) lightOutside1.get("parkingLight")).get("description") == null ? "NA" : ((Map) lightOutside1.get("parkingLight")).get("description"));

                lightOutside1Val.put("_generalControl_status", ((Map) lightOutside1.get("generalControlOfLightsReflectors")).get("status") == null ? "NA" : ((Map) lightOutside1.get("generalControlOfLightsReflectors")).get("status"));
                lightOutside1Val.put("_generalControl_result", getResultStatus(((Map) lightOutside1.get("generalControlOfLightsReflectors")).get("result")));
                lightOutside1Val.put("_generalControl_Value", ((Map) lightOutside1.get("generalControlOfLightsReflectors")).get("description") == null ? "NA" : ((Map) lightOutside1.get("generalControlOfLightsReflectors")).get("description"));

                lightOutside1Val.put("_brightness_status", ((Map) lightOutside1.get("brightnessSettings")).get("status") == null ? "NA" : ((Map) lightOutside1.get("brightnessSettings")).get("status"));
                lightOutside1Val.put("_brightness_result", getResultStatus(((Map) lightOutside1.get("brightnessSettings")).get("result")));
                lightOutside1Val.put("_brightness_Value", ((Map) lightOutside1.get("brightnessSettings")).get("description") == null ? "NA" : ((Map) lightOutside1.get("brightnessSettings")).get("description"));
            }catch (Exception e){
                lightOutside1Val.put("_lowBeam_status", "NA");
                lightOutside1Val.put("_lowBeam_result","NA");
                lightOutside1Val.put("_lowBeam_Value", "NA");

                lightOutside1Val.put("_highBeam_status", "NA");
                lightOutside1Val.put("_highBeam_result","NA");
                lightOutside1Val.put("_highBeam_Value", "NA");

                lightOutside1Val.put("_flashing_status", "NA");
                lightOutside1Val.put("_flashing_result","NA");
                lightOutside1Val.put("_flashing_Value", "NA");

                lightOutside1Val.put("_parking_status", "NA");
                lightOutside1Val.put("_parking_result","NA");
                lightOutside1Val.put("_parking_Value", "NA");

                lightOutside1Val.put("_generalControl_status", "NA");
                lightOutside1Val.put("_generalControl_result","NA");
                lightOutside1Val.put("_generalControl_Value", "NA");

                lightOutside1Val.put("_brightness_status", "NA");
                lightOutside1Val.put("_brightness_result","NA");
                lightOutside1Val.put("_brightness_Value", "NA");
            }



//        }else {
//            lightOutside1Val.put("_lowBeam_status", "NA");
//            lightOutside1Val.put("_lowBeam_result","NA");
//            lightOutside1Val.put("_lowBeam_Value", "NA");
//
//            lightOutside1Val.put("_highBeam_status", "NA");
//            lightOutside1Val.put("_highBeam_result","NA");
//            lightOutside1Val.put("_highBeam_Value", "NA");
//
//            lightOutside1Val.put("_flashing_status", "NA");
//            lightOutside1Val.put("_flashing_result","NA");
//            lightOutside1Val.put("_flashing_Value", "NA");
//
//            lightOutside1Val.put("_parking_status", "NA");
//            lightOutside1Val.put("_parking_result","NA");
//            lightOutside1Val.put("_parking_Value", "NA");
//
//            lightOutside1Val.put("_generalControl_status", "NA");
//            lightOutside1Val.put("_generalControl_result","NA");
//            lightOutside1Val.put("_generalControl_Value", "NA");
//
//            lightOutside1Val.put("_brightness_status", "NA");
//            lightOutside1Val.put("_brightness_result","NA");
//            lightOutside1Val.put("_brightness_Value", "NA");
//
//        }
        return lightOutside1Val;
    }
    public Map<String, Object> getLightOutSide2(ServiceRecord serviceRecord, int i) {
        Map<String,Object> lightOutside2Val = new HashMap<>();
        Map lightOutside2 = (Map) ((Map)serviceRecord.getForm()).get("Lights outside 2 of 2");
        try{
            lightOutside2Val.put("_fogLights_status", ((Map) lightOutside2.get("fogLights")).get("status") == null ? "NA" : ((Map) lightOutside2.get("fogLights")).get("status"));
            lightOutside2Val.put("_fogLights_result", getResultStatus(((Map) lightOutside2.get("fogLights")).get("result")));
            lightOutside2Val.put("_fogLights_Value", ((Map) lightOutside2.get("fogLights")).get("description") == null ? "NA" : ((Map) lightOutside2.get("fogLights")).get("description"));

            lightOutside2Val.put("_brakeLights_status", ((Map) lightOutside2.get("brakeLights")).get("status") == null ? "NA" : ((Map) lightOutside2.get("brakeLights")).get("status"));
            lightOutside2Val.put("_brakeLights_result", getResultStatus(((Map) lightOutside2.get("brakeLights")).get("result")));
            lightOutside2Val.put("_brakeLights_Value", ((Map) lightOutside2.get("brakeLights")).get("description") == null ? "NA" : ((Map) lightOutside2.get("brakeLights")).get("description"));

            lightOutside2Val.put("_backLights_status", ((Map) lightOutside2.get("backLights")).get("status") == null ? "NA" : ((Map) lightOutside2.get("backLights")).get("status"));
            lightOutside2Val.put("_backLights_result", getResultStatus(((Map) lightOutside2.get("backLights")).get("result")));
            lightOutside2Val.put("_backLights_Value", ((Map) lightOutside2.get("backLights")).get("description") == null ? "NA" : ((Map) lightOutside2.get("backLights")).get("description"));

            lightOutside2Val.put("_signLight_status", ((Map) lightOutside2.get("signLight")).get("status") == null ? "NA" : ((Map) lightOutside2.get("signLight")).get("status"));
            lightOutside2Val.put("_signLight_result", getResultStatus(((Map) lightOutside2.get("signLight")).get("result")));
            lightOutside2Val.put("_signLight_Value", ((Map) lightOutside2.get("signLight")).get("description") == null ? "NA" : ((Map) lightOutside2.get("signLight")).get("description"));

            lightOutside2Val.put("_HornSoundSignal_status", ((Map) lightOutside2.get("HornSoundSignal")).get("status") == null ? "NA" : ((Map) lightOutside2.get("HornSoundSignal")).get("status"));
            lightOutside2Val.put("_HornSoundSignal_result", getResultStatus(((Map) lightOutside2.get("HornSoundSignal")).get("result")));
            lightOutside2Val.put("_HornSoundSignal_Value", ((Map) lightOutside2.get("HornSoundSignal")).get("description") == null ? "NA" : ((Map) lightOutside2.get("HornSoundSignal")).get("description"));

            lightOutside2Val.put("_errorMessagesOnLights_status", ((Map) lightOutside2.get("errorMessagesOnLights")).get("status") == null ? "NA" : ((Map) lightOutside2.get("errorMessagesOnLights")).get("status"));
            lightOutside2Val.put("_errorMessagesOnLights_result", getResultStatus(((Map) lightOutside2.get("errorMessagesOnLights")).get("result")));
            lightOutside2Val.put("_errorMessagesOnLights_Value", ((Map) lightOutside2.get("errorMessagesOnLights")).get("description") == null ? "NA" : ((Map) lightOutside2.get("errorMessagesOnLights")).get("description"));

            lightOutside2Val.put("_reflex_status", ((Map) lightOutside2.get("reflex")).get("status") == null ? "NA" : ((Map) lightOutside2.get("reflex")).get("status"));
            lightOutside2Val.put("_reflex_result", getResultStatus(((Map) lightOutside2.get("reflex")).get("result")));
            lightOutside2Val.put("_reflex_Value", ((Map) lightOutside2.get("reflex")).get("description") == null ? "NA" : ((Map) lightOutside2.get("reflex")).get("description"));

            lightOutside2Val.put("_headlightWashers_status", ((Map) lightOutside2.get("headlightWashers")).get("status") == null ? "NA" : ((Map) lightOutside2.get("headlightWashers")).get("status"));
            lightOutside2Val.put("_headlightWashers_result", getResultStatus(((Map) lightOutside2.get("headlightWashers")).get("result")));
            lightOutside2Val.put("_headlightWashers_Value", ((Map) lightOutside2.get("headlightWashers")).get("description") == null ? "NA" : ((Map) lightOutside2.get("headlightWashers")).get("description"));
        }catch (Exception e){
            lightOutside2Val.put("_fogLights_status", "NA");
            lightOutside2Val.put("_fogLights_result", "NA");
            lightOutside2Val.put("_fogLights_Value", "NA");

            lightOutside2Val.put("_brakeLights_status", "NA");
            lightOutside2Val.put("_brakeLights_result", "NA");
            lightOutside2Val.put("_brakeLights_Value", "NA");

            lightOutside2Val.put("_backLights_status", "NA");
            lightOutside2Val.put("_backLights_result", "NA");
            lightOutside2Val.put("_backLights_Value", "NA");

            lightOutside2Val.put("_signLight_status", "NA");
            lightOutside2Val.put("_signLight_result", "NA");
            lightOutside2Val.put("_signLight_Value", "NA");

            lightOutside2Val.put("_HornSoundSignal_status", "NA");
            lightOutside2Val.put("_HornSoundSignal_result", "NA");
            lightOutside2Val.put("_HornSoundSignal_Value", "NA");

            lightOutside2Val.put("_errorMessagesOnLights_status", "NA");
            lightOutside2Val.put("_errorMessagesOnLights_result", "NA");
            lightOutside2Val.put("_errorMessagesOnLights_Value", "NA");

            lightOutside2Val.put("_reflex_status", "NA");
            lightOutside2Val.put("_reflex_result", "NA");
            lightOutside2Val.put("_reflex_Value", "NA");

            lightOutside2Val.put("_headlightWashers_status", "NA");
            lightOutside2Val.put("_headlightWashers_result", "NA");
            lightOutside2Val.put("_headlightWashers_Value", "NA");
        }
        return lightOutside2Val;
    }
    public Map<String, Object> getDriveUnit(ServiceRecord serviceRecord, int i) {
        Map<String,Object> driveUnitValue = new HashMap<>();
        Map driveUnit = (Map) ((Map)serviceRecord.getForm()).get("Drive Unit");

        try{
            driveUnitValue.put("_overallCondition_status", ((Map) driveUnit.get("overallCondition")).get("status") == null ? "NA" : ((Map) driveUnit.get("overallCondition")).get("status"));
            driveUnitValue.put("_overallCondition_result", getResultStatus(((Map) driveUnit.get("overallCondition")).get("result")));
            driveUnitValue.put("_overallCondition_mv", ((Map) driveUnit.get("overallCondition")).get("measuredValues") == null ? "NA" : ((Map) driveUnit.get("overallCondition")).get("measuredValues"));
            driveUnitValue.put("_overallCondition_Value", ((Map) driveUnit.get("overallCondition")).get("description") == null ? "NA" : ((Map) driveUnit.get("overallCondition")).get("description"));

            driveUnitValue.put("_noice_status", ((Map) driveUnit.get("noice")).get("status") == null ? "NA" : ((Map) driveUnit.get("noice")).get("status"));
            driveUnitValue.put("_noice_result", getResultStatus(((Map) driveUnit.get("noice")).get("result")));
            driveUnitValue.put("_noice_mv", ((Map) driveUnit.get("noice")).get("measuredValues") == null ? "NA" : ((Map) driveUnit.get("noice")).get("measuredValues"));
            driveUnitValue.put("_noice_Value", ((Map) driveUnit.get("noice")).get("description") == null ? "NA" : ((Map) driveUnit.get("noice")).get("description"));

            driveUnitValue.put("_leakage_status", ((Map) driveUnit.get("leakage")).get("status") == null ? "NA" : ((Map) driveUnit.get("leakage")).get("status"));
            driveUnitValue.put("_leakage_result", getResultStatus(((Map) driveUnit.get("leakage")).get("result")));
            driveUnitValue.put("_leakage_mv", ((Map) driveUnit.get("leakage")).get("measuredValues") == null ? "NA" : ((Map) driveUnit.get("leakage")).get("measuredValues"));
            driveUnitValue.put("_leakage_Value", ((Map) driveUnit.get("leakage")).get("description") == null ? "NA" : ((Map) driveUnit.get("leakage")).get("description"));

            driveUnitValue.put("_oil_status", ((Map) driveUnit.get("oil")).get("status") == null ? "NA" : ((Map) driveUnit.get("oil")).get("status"));
            driveUnitValue.put("_oil_result", getResultStatus(((Map) driveUnit.get("oil")).get("result")));
            driveUnitValue.put("_oil_mv", ((Map) driveUnit.get("oil")).get("measuredValues") == null ? "NA" : ((Map) driveUnit.get("oil")).get("measuredValues"));
            driveUnitValue.put("_oil_Value", ((Map) driveUnit.get("oil")).get("description") == null ? "NA" : ((Map) driveUnit.get("oil")).get("description"));

            driveUnitValue.put("_coolantLiquid_status", ((Map) driveUnit.get("coolantLiquid")).get("status") == null ? "NA" : ((Map) driveUnit.get("coolantLiquid")).get("status"));
            driveUnitValue.put("_coolantLiquid_result", getResultStatus(((Map) driveUnit.get("coolantLiquid")).get("result")));
            driveUnitValue.put("_coolantLiquid_mv", ((Map) driveUnit.get("coolantLiquid")).get("measuredValues") == null ? "NA" : ((Map) driveUnit.get("coolantLiquid")).get("measuredValues"));
            driveUnitValue.put("_coolantLiquid_Value", ((Map) driveUnit.get("coolantLiquid")).get("description") == null ? "NA" : ((Map) driveUnit.get("coolantLiquid")).get("description"));

            driveUnitValue.put("_bearing_status", ((Map) driveUnit.get("bearing")).get("status") == null ? "NA" : ((Map) driveUnit.get("bearing")).get("status"));
            driveUnitValue.put("_bearing_result", getResultStatus(((Map) driveUnit.get("bearing")).get("result")));
            driveUnitValue.put("_bearing_mv", ((Map) driveUnit.get("bearing")).get("measuredValues") == null ? "NA" : ((Map) driveUnit.get("bearing")).get("measuredValues"));
            driveUnitValue.put("_bearing_Value", ((Map) driveUnit.get("bearing")).get("description") == null ? "NA" : ((Map) driveUnit.get("bearing")).get("description"));
        }catch (Exception e){
            driveUnitValue.put("_overallCondition_status", "NA");
            driveUnitValue.put("_overallCondition_result", "NA");
            driveUnitValue.put("_overallCondition_mv", "NA");
            driveUnitValue.put("_overallCondition_Value", "NA");

            driveUnitValue.put("_noice_status", "NA");
            driveUnitValue.put("_noice_result", "NA");
            driveUnitValue.put("_noice_mv", "NA");
            driveUnitValue.put("_noice_Value", "NA");

            driveUnitValue.put("_leakage_status", "NA");
            driveUnitValue.put("_leakage_result", "NA");
            driveUnitValue.put("_leakage_mv", "NA");
            driveUnitValue.put("_leakage_Value", "NA");

            driveUnitValue.put("_oil_status", "NA");
            driveUnitValue.put("_oil_result", "NA");
            driveUnitValue.put("_oil_mv", "NA");
            driveUnitValue.put("_oil_Value", "NA");

            driveUnitValue.put("_coolantLiquid_status", "NA");
            driveUnitValue.put("_coolantLiquid_result", "NA");
            driveUnitValue.put("_coolantLiquid_mv", "NA");
            driveUnitValue.put("_coolantLiquid_Value", "NA");

            driveUnitValue.put("_bearing_status", "NA");
            driveUnitValue.put("_bearing_result", "NA");
            driveUnitValue.put("_bearing_mv", "NA");
            driveUnitValue.put("_bearing_Value", "NA");
        }

        return driveUnitValue;
    }
    public Map<String, Object> getDriveBelt(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
    public Map<String, Object> getElectricComponent1(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
    public Map<String, Object> getElectricComponent2(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
    public Map<String, Object> getElectricComponent3(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
    public Map<String, Object> getMainBattery(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
    public Map<String, Object> getTire(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
    public Map<String, Object> getSteering(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
    public Map<String, Object> getChassis(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
    public Map<String, Object> getBody1(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
    public Map<String, Object> getBody2(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
    public Map<String, Object> getInt1(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
    public Map<String, Object> getInt2(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
    public Map<String, Object> getGeneralElement(ServiceRecord serviceRecord, int i) {
        Map<String,Object> brakesVisualValue = new HashMap<>();
        Map brakesVisual = (Map) ((Map)serviceRecord.getForm()).get("Brakes - Visual inspection");
        if(Objects.nonNull(brakesVisualValue)) {
            brakesVisualValue.put("_BrakeHosesStatus", ((Map) brakesVisual.get("BrakeHoses")).get("status") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("status"));
            brakesVisualValue.put("_BrakeHosesResult", getResultStatus(((Map) brakesVisual.get("BrakeHoses")).get("result")));
            brakesVisualValue.put("_BrakeHosesRemarks", ((Map) brakesVisual.get("BrakeHoses")).get("description") == null ? "NA" : ((Map) brakesVisual.get("BrakeHoses")).get("description"));

            brakesVisualValue.put("_brakePipesStatus", ((Map) brakesVisual.get("brakePipes")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("status"));
            brakesVisualValue.put("_brakePipesResult", getResultStatus(((Map) brakesVisual.get("brakePipes")).get("result")));
            brakesVisualValue.put("_brakePipesRemarks", ((Map) brakesVisual.get("brakePipes")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePipes")).get("description"));

            brakesVisualValue.put("_brakePedalStatus", ((Map) brakesVisual.get("brakePedal")).get("status") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("status"));
            brakesVisualValue.put("_brakePedalResult", getResultStatus(((Map) brakesVisual.get("brakePedal")).get("result")));
            brakesVisualValue.put("_brakePedalRemarks", ((Map) brakesVisual.get("brakePedal")).get("description") == null ? "NA" : ((Map) brakesVisual.get("brakePedal")).get("description"));
        }else {
            brakesVisualValue.put("_BrakeHosesStatus", "NA");
            brakesVisualValue.put("_BrakeHosesResult","NA");
            brakesVisualValue.put("_BrakeHosesRemarks", "NA");

            brakesVisualValue.put("_brakePipesStatus", "NA");
            brakesVisualValue.put("_brakePipesResult", "NA");
            brakesVisualValue.put("_brakePipesRemarks", "NA");

            brakesVisualValue.put("_brakePedalStatus", "NA");
            brakesVisualValue.put("_brakePedalResult", "NA");
            brakesVisualValue.put("_brakePedalRemarks", "NA");
        }
        return brakesVisualValue;
    }
public static List<OverAllHealthResponse> overallHealth(List<OverAllHealthResponse> overAllHealthResponse){
    System.out.println(overAllHealthResponse);

        overAllHealthResponse.sort((a, b) -> {
    if (a.getStatus().contains("Not Ok") || b.getStatus().contains("Not Ok")) {
        return 1;
    } else if (a.getStatus().equals("Next Service") && ((b.getStatus().equals("OK")) || b.getStatus().equalsIgnoreCase("Not checked"))) {
        return 1;
    } else if (a.getStatus().equals("Ok") && (b.getStatus().equalsIgnoreCase("Not checked"))){
        return 1;
    }else
        return -1;});
    System.out.println(overAllHealthResponse);
    List<OverAllHealthResponse> collect = overAllHealthResponse.stream().sorted((a, b) -> {
        if (a.getStatus().contains("Not Ok")||b.getStatus().contains("Not Ok"))
            return 1;
        else if (a.getStatus().equals("Next Service") && ((b.getStatus().equals("OK")) || b.getStatus().equalsIgnoreCase("Not checked")))
            return 1;
        else if (a.getStatus().equals("Ok") && (b.getStatus().equalsIgnoreCase("Not checked")))
            return 1;
        else
            return -1;
    }).collect(Collectors.toList());
    return collect;
}
*/

}
