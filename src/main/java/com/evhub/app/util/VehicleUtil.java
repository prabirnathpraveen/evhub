package com.evhub.app.util;

import com.evhub.app.entities.Value;
import com.evhub.app.service.ServiceRecordsService;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ObjectUtils;

import java.text.ParseException;
import java.util.*;

public class VehicleUtil {
    private final static List<String> dateVariable = List.of("firstRegisteredOn", "firstRegisteredInNorway", "registrationDate", "nextMandatoryInspection");


    public static Map<String, Object> getVehicleResponse(Map<String, Object> responseBody,String param) throws ParseException {
        Map<String, Object> englishResponse = new LinkedHashMap<>();
        if (Objects.nonNull(responseBody)) {
            String fieldValues = responseBody.toString();



            //enhanced logix
            int indexOf=fieldValues.indexOf("PERSONLIG");
            if(indexOf>=0) {
                String output = fieldValues.substring(indexOf - 32, indexOf - 21).substring(fieldValues.substring(indexOf - 32, indexOf - 21).indexOf("=") + 1, fieldValues.substring(indexOf - 32, indexOf - 21).indexOf(","));
                englishResponse.put("personalizedNumber",output);
            }



            //logicx end



            for (Map.Entry<String, String> keyValue : getEnglishAttributes().entrySet()) {
                String fieldValue = getFieldValue(fieldValues, keyValue.getKey());
                if (dateVariable.contains(keyValue.getValue())) {
                    englishResponse.put(keyValue.getValue(), (Objects.nonNull(fieldValue) && !fieldValue.equalsIgnoreCase("")) ? CommonUtils.timestamp(fieldValue) : null);
                } else if (keyValue.getKey().equalsIgnoreCase("bruktimport")) {
                    Object value = checkImportedCar(fieldValues);
                    englishResponse.put(keyValue.getValue(), value);
                    if (value.equals("Yes")) {
                        englishResponse.put("importedCountry", getFieldValue(fieldValues, "landNavn"));
                    }
                } else if (keyValue.getValue().equals("engineCount")) {
                    int motorKode = StringUtils.countMatches(fieldValues, "motorKode");
                    englishResponse.put(keyValue.getValue(), motorKode);
                    Map<String, String> enginePowers = new LinkedHashMap<>();
                    for (int i = 1; i <= motorKode; i++) {
                        enginePowers.put("enginePower" + i, getFieldValue(fieldValues, "maksNettoEffekt"));
                        fieldValues = fieldValues.replaceFirst("maksNettoEffekt", "");
                    }
                    englishResponse.put("enginePowers", enginePowers);
                } else if (fieldValue.contains("merke")) {
                    englishResponse.put(keyValue.getValue(), fieldValue.split("=")[1].trim());
                } else englishResponse.put(keyValue.getValue(), fieldValue);

                fieldValues = fieldValues.replaceAll(keyValue.getKey(), "");
            }
            englishResponse.put("disclaimer", getDisclaimer(fieldValues, englishResponse));
            englishResponse.put("wheelDrive", getWheelDrive(fieldValues));
        }
        englishResponse.put("color", getColor((String) englishResponse.get("color")));
        englishResponse.put("hybridCategory", getHybrid((String) englishResponse.get("hybridCategory")));
        if(!ObjectUtils.isEmpty(englishResponse.get("personalizedNumber"))){
            englishResponse.put("regNumber",englishResponse.get("personalizedNumber"));
            englishResponse.put("personalizedNumber",null);
        }
        return englishResponse;
    }

    private static Object getWheelDrive(String fieldValues) {
        Boolean fwd = false;
        Boolean rwd = false;
        int akselListeCount=StringUtils.countMatches(fieldValues,"akselListe");
        String[] stringList=fieldValues.split("akselListe");
        int length = stringList.length;
        while (akselListeCount> 0) {
            String fieldValue = stringList[length-akselListeCount];
            Boolean drivAksel=Boolean.valueOf(getFieldValue(fieldValue,"drivAksel"));
            String axis = getFieldValue(fieldValue, "plasseringAksel");
            if (drivAksel && "1".equalsIgnoreCase(axis)) fwd = true;
            if (drivAksel && "2".equalsIgnoreCase(axis)) rwd = true;
            akselListeCount--;
        }
        if (rwd && fwd) return "4X4";
        else if (fwd) return "FWD";
        else if (rwd) return "RWD";
        else return "N/A";
    }


    private static Object getDisclaimer(String fieldValues, Map<String, Object> values) throws ParseException {
        if (fieldValues.contains("Avregistrert")) {
            return "The car has been unregistered on " + CommonUtils.DateFormatter(CommonUtils.applicableTo(getFieldValue(fieldValues, "avregistrertSidenDato")));
        }
        if (values.get("importedUsed").equals("Yes")) {
            return "The car was exported from " + values.get("importedCountry") + " on " + CommonUtils.DateFormatter(CommonUtils.applicableTo(getFieldValue(getFieldValue(fieldValues, "registrering={fomTidspunkt") + ",}", "fomTidspunkt")));
        }
        return null;
    }

    private static Object checkImportedCar(String fieldValues) {
        return fieldValues.contains("bruktimport") ? "Yes" : "No";
    }

    private static String getHybrid(String hybridCategory) {
        return "Ingen".equalsIgnoreCase(hybridCategory) || "Nei".equalsIgnoreCase(hybridCategory) ? "No" : "Ja".equalsIgnoreCase(hybridCategory) ? "Yes" : hybridCategory;
    }

    @NotNull
    private static String getFieldValue(String fieldValue, String key) {
        if (fieldValue.contains(key)) {

            String subStringValue = fieldValue.substring(fieldValue.indexOf(key));
            String replace = "[]}{]";
            if (key.equals("tekniskKode") || key.equals("hybridKategori") || key.equals("rFarge")) {
                subStringValue = subStringValue.substring(subStringValue.indexOf("=") + 1, subStringValue.indexOf("}"));
                subStringValue = subStringValue.substring(subStringValue.indexOf("kodeNavn"));
            }
            if (key.equals("merke")) {
                subStringValue = subStringValue.substring(subStringValue.indexOf("merke=[{"));
                subStringValue = subStringValue.substring(subStringValue.indexOf("=") + 1, subStringValue.indexOf("}"));

            }
            /*else if (key.equals("rFarge")) {
                subStringValue = subStringValue.substring(subStringValue.indexOf("=") + 1, subStringValue.indexOf("}"));
                subStringValue = subStringValue.substring(subStringValue.indexOf("kodeBeskrivelse")).replace("," ," ");
            }*/
            String value = subStringValue.substring(subStringValue.indexOf("=") + 1, subStringValue.contains(",") ? subStringValue.indexOf(",") : subStringValue.indexOf("}"));
            if (key.equals("kjennemerke")) {
                return value.replaceAll(replace, "").replace("[", "").trim().replace(" ", "");
            }
            return value.replaceAll(replace, "").replace("[", "").trim();
        }
        return "";
    }

    private static Map<String, String> getEnglishAttributes() {
        Map<String, String> document = new LinkedHashMap<>();
        document.put("kjennemerke", "regNumber");
        document.put("understellsnummer", "chassisNumber");
        document.put("handelsbetegnelse", "model"); //generelt,tekniskeData
        document.put("merke", "brand");
        document.put("tekniskKode", "carGroup"); //kodevan string
        document.put("forstegangRegistrertDato", "firstRegisteredOn");
        document.put("registrertForstegangNorgeDato", "firstRegisteredInNorway");// time
        document.put("registrertForstegangPaEierskap", "registrationDate");//time
        document.put("sitteplasserTotalt", "totalSeats");// int
        document.put("rFarge", "color");//
        document.put("maksimumHastighet", "maximumSpeed");//
        document.put("kontrollfrist", "nextMandatoryInspection");// timestamp
        document.put("egenvekt", "carWeight");// String
        document.put("fabrikantNavn", "manufacturer");// String
        document.put("tillattTotalvekt", "totalAllowedWeight");// String
        document.put("rekkeviddeKmBlandetkjoring", "electricRange");// String
        document.put("rekkeviddeKm", "electricRange");//string
        document.put("hybridKategori", "hybridCategory");// kodevan String
        document.put("motorKode", "engineCount");// kodevan String
        document.put("bruktimport", "importedUsed");// kodevan String

//        document.put("maksNettoEffekt","enginePower1");// kodevan String 2
        return document;

    }

    private static String getColor(String norwegianColor) {
        Map<String, String> colorTranslation = new HashMap<>();
        colorTranslation.put("Svart", "Black");
        colorTranslation.put("Hvit", "White");
        colorTranslation.put("Rød", "Red");
        colorTranslation.put("Gul", "Yellow");
        colorTranslation.put("Blå", "Blue");
        colorTranslation.put("Grønn", "Green");
        colorTranslation.put("Brun", "Brown");
        colorTranslation.put("Rosa", "Pink");
        colorTranslation.put("Oransje", "Orange");
        colorTranslation.put("Grå", "Grey");
        colorTranslation.put("Lilla", "Purple");
        if (colorTranslation.containsKey(norwegianColor)) return colorTranslation.get(norwegianColor);
        return norwegianColor;
    }

    public static LinkedHashMap<String, String> getMainBatteryFields() {
        LinkedHashMap<String, String> mainBatteryField = new LinkedHashMap<>();
        mainBatteryField.put("nominalFullPack", "capacity");
        mainBatteryField.put("fullIdealRange", "range");
        mainBatteryField.put("dcChargeTotal", "fastChargingDC");
        mainBatteryField.put("acChargeTotal", "regularChargingAC");
        mainBatteryField.put("dischargeTotal", "regenCharging");
        mainBatteryField.put("chargeTotal", "totalCharged");
        mainBatteryField.put("chargeTotal", "totalCharged");
        return mainBatteryField;
    }

    public static LinkedHashMap<String, LinkedHashMap<String, String>> getMainBatteryChildAttributes() {
        LinkedHashMap<String, LinkedHashMap<String, String>> mainBatteryChildField = new LinkedHashMap<>();
        LinkedHashMap<String, String> cellTemperatureChildAttributes = new LinkedHashMap<>();
        Map<String, List<Value>> data=ServiceRecordsService.dataCheck;
        if(data!=null) {
            if (!ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellTempMin")))
                cellTemperatureChildAttributes.put("cellTempMin", "minimum");
            if (!ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellTempMax")))
                cellTemperatureChildAttributes.put("cellTempMax", "maximum");
            if (!ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellTempAvg")))
                cellTemperatureChildAttributes.put("cellTempAvg", "average");

            LinkedHashMap<String, String> cellVoltageChildAttributes = new LinkedHashMap<>();
            if (!ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellVoltMin")))
                cellVoltageChildAttributes.put("cellVoltMin", "minimum");
            if (!ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellVoltMax")))
                cellVoltageChildAttributes.put("cellVoltMax", "maximum");
            if (!ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellVoltAvg")))
                cellVoltageChildAttributes.put("cellVoltAvg", "average");
            if (!ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellImbalance")))
                cellVoltageChildAttributes.put("cellImbalance", "imbalance");


            LinkedHashMap<String, String> CACChildAttributes = new LinkedHashMap<>();
            if (!ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cacMin")))
                CACChildAttributes.put("cacMin", "minimum");
            if (!ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cacMax")))
                CACChildAttributes.put("cacMax", "maximum");
            if (!ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cacAvg")))
                CACChildAttributes.put("cacAvg", "average");
            if (!ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cac_imbulance")))
                CACChildAttributes.put("cac_imbulance", "imbalance");
            if (!ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cacImbalance")))
                CACChildAttributes.put("cacImbalance", "imbalance");


            //if(ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellTempMin"))&&ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellTempMax"))&&ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellTempAvg")))

            if (cellTemperatureChildAttributes.size() != 0)
                mainBatteryChildField.put("cellTemperature", cellTemperatureChildAttributes);
            // if(ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellVoltMin"))&&ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellVoltMax"))&&ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellVoltAvg"))&&ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellImbalance")))
            if (cellVoltageChildAttributes.size() != 0)
                mainBatteryChildField.put("cellVoltage", cellVoltageChildAttributes);
            // if(ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cacImbalance"))&&ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cac_imbulance"))&&ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cacMax"))&&ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cellTempMin"))&&ObjectUtils.isEmpty(ServiceRecordsService.dataCheck.get("cacMin")))
            if (CACChildAttributes.size() != 0)
                mainBatteryChildField.put("calculatedAmpHourCapacityCAC", CACChildAttributes);
        }
        return mainBatteryChildField;
    }

}
