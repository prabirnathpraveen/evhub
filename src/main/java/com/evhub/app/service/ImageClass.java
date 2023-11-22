package com.evhub.app.service;

import com.evhub.app.entities.JobCard;
import com.evhub.app.entities.JobCardData;
import com.evhub.app.entities.JobCardDataAttributes;
import com.evhub.app.entities.ServiceRecord;
import com.evhub.app.event.RoundedBorder;
import com.evhub.app.repository.JobCardRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

@Service
public class ImageClass {
    @Autowired
    private JobCardRepository jobCardRepository;
    Integer co2Percentage = 0;

    public Integer getCo2Percentage() {
        return co2Percentage;
    }

    public void highVoltageParts(Document document, PdfWriter writer) throws DocumentException {
        PdfPTable pdfPTable = new PdfPTable(1);

        ColumnText column = new ColumnText(writer.getDirectContent());
//        column.setSimpleColumn(-70, -400, 450, 520, 16, Element.ALIGN_LEFT);
        column.setSimpleColumn(-65, -400, 450, 570, 16, Element.ALIGN_LEFT);
        PdfPCell blankCell = new PdfPCell();
        blankCell.setColspan(10);
        blankCell.setPaddingTop(-5);
        blankCell.addElement(Chunk.NEWLINE);
        blankCell.setBorder(PdfPCell.NO_BORDER);
        blankCell.setBorderColor(BaseColor.WHITE);
        pdfPTable.addCell(blankCell);


        PdfPCell cellVoltage = new PdfPCell(new Paragraph("Cell 2"));

        Font volatgeFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 16);
        volatgeFont.setColor(BaseColor.BLACK);
//        Font font3 = new Font(Font.FontFamily.UNDEFINED, 15, Font.BOLD, BaseColor.BLACK);

        cellVoltage.setBorder(PdfPCell.NO_BORDER);
        PdfPTable nestedTableTesla = new PdfPTable(1); // Create 2 columns in

        PdfPCell highVoltageParts = new PdfPCell(new Paragraph("High Voltage Parts", volatgeFont));
        highVoltageParts.setBorder(PdfPCell.NO_BORDER);
        nestedTableTesla.addCell(highVoltageParts);
        cellVoltage.addElement(nestedTableTesla);
        pdfPTable.addCell(cellVoltage);
        column.addElement(pdfPTable);
        column.go();
    }

    public void ImageForVoltageParts(PdfWriter writer, ServiceRecord serviceRecord) throws DocumentException, IOException {
        PdfPTable pdfPTable = new PdfPTable(6);
        pdfPTable.setWidthPercentage(100);
        pdfPTable.setTotalWidth(new float[]{18, 18, 18, 18, 18, 18});

        ColumnText column = new ColumnText(writer.getDirectContent());

        column.setSimpleColumn(25, 0, 571, 540, 16, Element.ALIGN_LEFT);

        PdfPCell blankCell = new PdfPCell();
        blankCell.setColspan(10);
        blankCell.setPaddingTop(-5);
        blankCell.addElement(Chunk.NEWLINE);
        blankCell.setBorder(PdfPCell.NO_BORDER);
        blankCell.setBorderColor(BaseColor.WHITE);
        pdfPTable.addCell(blankCell);

        ItextPdf.highVoltageParts=false;
        Map<String, String> overAllStatus = highVoltageOverallStatus(serviceRecord);
        if(!ObjectUtils.isEmpty(overAllStatus)){
           if(overAllStatus.get("EV Battery").equals("OK")&&
            overAllStatus.get("Drive unit").equals("OK")&&
            overAllStatus.get("Air Condition").equals("OK")&&
            overAllStatus.get("Interior Heater").equals("OK")&&
            overAllStatus.get("Onboard Charger").equals("OK")&&
            overAllStatus.get("DC/DC Converter").equals("OK")){
               ItextPdf.highVoltageParts=true;
           }
        }
        OverAllStatus(pdfPTable, this.getClass().getResource("/image/ev.png"), "EV Battery", overAllStatus.get("EV Battery"), 63, 10, 0);
//        OverAllStatus(pdfPTable, font, "/home/jatin/Pictures/Screenshot from 2023-04-08 13-59-19.png", "Ev Battery", "ok",50,10);
        OverAllStatus(pdfPTable, this.getClass().getResource("/image/driveUnit.png"), "Drive Unit", overAllStatus.get("Drive unit"), 63, 12, 0);
        OverAllStatus(pdfPTable, this.getClass().getResource("/image/airCondition.png"), "Air Condition", overAllStatus.get("Air Condition"), 63, 10, 0);
        OverAllStatus(pdfPTable, this.getClass().getResource("/image/interiorHeater.png"), "Interior Heater", overAllStatus.get("Interior Heater"), 62, 12, 0);
        OverAllStatus(pdfPTable, this.getClass().getResource("/image/onboardCharger.png"), "Onboard Charger", overAllStatus.get("Onboard Charger"), 62, 10, 0);
        OverAllStatus(pdfPTable, this.getClass().getResource("/image/dcConverter.png"), "DC/DC Converter", overAllStatus.get("DC/DC Converter"), 62, 12, 0);
        column.addElement(pdfPTable);
        column.go();
    }

    private static void OverAllStatus(PdfPTable pdfPTable, URL imagePath, String fieldName, String status, float width, float height, int padding) throws BadElementException, IOException {
        Font font = new Font(Font.FontFamily.UNDEFINED, 8, Font.BOLD, new BaseColor(137, 137, 137));
        PdfPCell evBatteryImage = new PdfPCell();
        PdfPTable EvBatteryNested = new PdfPTable(1);
        Image image = Image.getInstance(imagePath);
        image.setWidthPercentage(100);
        image.scaleAbsolute(width, height);

        evBatteryImage.setBorder(PdfPCell.NO_BORDER);
        evBatteryImage.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        evBatteryImage.setPaddingLeft(padding);
        evBatteryImage.addElement(image);
        EvBatteryNested.addCell(evBatteryImage);

        PdfPCell EvBattery = new PdfPCell();
        EvBattery.setBorder(PdfPCell.NO_BORDER);
        EvBattery.setPaddingBottom(20);
        PdfPCell evBatteryCell = new PdfPCell(new Paragraph(fieldName, font));
        evBatteryCell.setPaddingLeft(1);

        evBatteryCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        evBatteryCell.setBorder(PdfPCell.NO_BORDER);
        EvBatteryNested.addCell(evBatteryCell);
//        Font statusFont = new Font(Font.FontFamily.UNDEFINED, 10, Font.BOLD, selectFontColor(status));
        Font statusFont = FontFactory.getFont("Poppins-semi", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
        statusFont.setColor(selectFontColor(status));
        PdfPCell EvBatteryChild = new PdfPCell(new Paragraph(status, statusFont));
        RoundedBorder cornerCell = new RoundedBorder(selectClassifierBackGroundColor(status), new float[]{70, -30, -140, -3});
        EvBatteryChild.setCellEvent(cornerCell);
        EvBatteryChild.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        EvBatteryChild.setPaddingLeft(2);
        EvBatteryChild.setPaddingTop(13);
        EvBatteryChild.setBorder(PdfPCell.NO_BORDER);
        EvBatteryNested.addCell(EvBatteryChild);

        EvBattery.addElement(EvBatteryNested);

        pdfPTable.addCell(EvBattery);
    }

    public void generalParts(Document document, PdfWriter writer) throws DocumentException {
        PdfPTable pdfPTable = new PdfPTable(1);

        ColumnText column = new ColumnText(writer.getDirectContent());
        column.setSimpleColumn(-65, -400, 450, 410, 16, Element.ALIGN_LEFT);
        PdfPCell blankCell = new PdfPCell();
        blankCell.setColspan(10);
        blankCell.setPaddingTop(-5);
        blankCell.addElement(Chunk.NEWLINE);
        blankCell.setBorder(PdfPCell.NO_BORDER);
        blankCell.setBorderColor(BaseColor.WHITE);
        pdfPTable.addCell(blankCell);


        PdfPCell cellGeneral = new PdfPCell(new Paragraph("Cell 2"));

//        Font font3 = new Font(Font.FontFamily.UNDEFINED, 15, Font.BOLD, BaseColor.BLACK);
        Font generalFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 16);
        generalFont.setColor(BaseColor.BLACK);
        cellGeneral.setBorder(PdfPCell.NO_BORDER);
        PdfPTable nestedTableTesla = new PdfPTable(1); // Create 2 columns in

        PdfPCell generalParts = new PdfPCell(new Paragraph("General Parts", generalFont));

        generalParts.setBorder(PdfPCell.NO_BORDER);
        nestedTableTesla.addCell(generalParts);
        cellGeneral.addElement(nestedTableTesla);
        pdfPTable.addCell(cellGeneral);
        column.addElement(pdfPTable);
        column.go();
    }

    public void ImageForGeneralParts(PdfWriter writer, ServiceRecord serviceRecord) throws DocumentException, IOException {
        PdfPTable pdfPTable = new PdfPTable(6);
        pdfPTable.setWidthPercentage(100);
        pdfPTable.setTotalWidth(new float[]{18, 18, 18, 18, 18, 18});

        ColumnText column = new ColumnText(writer.getDirectContent());

        column.setSimpleColumn(25, 0, 571, 380, 16, Element.ALIGN_LEFT);

        PdfPCell blankCell = new PdfPCell();
        blankCell.setColspan(10);
        blankCell.setPaddingTop(-5);
        // blankCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        blankCell.addElement(Chunk.NEWLINE);
        blankCell.setBorder(PdfPCell.NO_BORDER);
        blankCell.setBorderColor(BaseColor.WHITE);
        pdfPTable.addCell(blankCell);
        Font font = new Font(Font.FontFamily.UNDEFINED, 8, Font.UNDEFINED, new BaseColor(24, 76, 140));
        Map<String, String> addOverallStatus = generalVoltageOverallStatus(serviceRecord);
        ItextPdf.generalParts=false;
        if(!ObjectUtils.isEmpty(addOverallStatus)){
             if(addOverallStatus.get("Brakes").equals("OK")&&
            addOverallStatus.get("Suspension").equals("OK")&&
            addOverallStatus.get("Tires").equals("OK")&&
            addOverallStatus.get("Lights").equals("OK")&&
            addOverallStatus.get("Body").equals("OK")&&
            addOverallStatus.get("Other").equals("OK")){
                ItextPdf.generalParts=true;
             }

        }

        OverAllStatus(pdfPTable, this.getClass().getResource("/image/brakes.png"), "Brakes", addOverallStatus.get("Brakes"), 46, 10, 10);
        OverAllStatus(pdfPTable, this.getClass().getResource("/image/suspension.png"), "Suspension", addOverallStatus.get("Suspension"), 46, 10, 10);
        OverAllStatus(pdfPTable, this.getClass().getResource("/image/tires.png"), "Tires", addOverallStatus.get("Tires"), 47, 10, 10);
        OverAllStatus(pdfPTable, this.getClass().getResource("/image/lights.png"), "Lights", addOverallStatus.get("Lights"), 47, 12, 10);
        OverAllStatus(pdfPTable, this.getClass().getResource("/image/body.png"), "Body", addOverallStatus.get("Body"), 46, 10, 10);
        OverAllStatus(pdfPTable, this.getClass().getResource("/image/other.png"), "Other", addOverallStatus.get("Other"), 45, 10, 10);

        column.addElement(pdfPTable);
        column.go();
    }

    Map<String, String> highVoltageOverallStatus(ServiceRecord serviceRecord) {
        Map<String, String> overAllStatus = new LinkedHashMap<>();
        List<JobCard> jobCardList = jobCardRepository.findAll();

        List<JobCardData> jobCardData = ObjectUtils.isEmpty(serviceRecord.getJobCardData()) ? new ArrayList<>() : serviceRecord.getJobCardData();

        Map<String, JobCardData> jobCardListStatus = getJobCardList(jobCardData, jobCardList);
        JobCardData jobCardDataBattery = getJobCardList("battery", jobCardData, jobCardList);
        List<JobCardDataAttributes> jobCardDataAttributes = jobCardDataBattery.getJobCardDataAttributes();
        jobCardDataAttributes = ObjectUtils.isEmpty(jobCardDataAttributes) ? new ArrayList<>() : jobCardDataAttributes;
        jobCardDataAttributes.addAll(jobCardListStatus.get("evBattery").getJobCardDataAttributes());

        jobCardDataBattery.setJobCardDataAttributes(jobCardDataAttributes);
        overAllStatus.put("EV Battery", getOverallStatus(jobCardDataBattery));
        if (getOverallStatus(jobCardDataBattery).equals("Failure")) {
            co2Percentage = +40;
        }
        JobCardData jobCardDataDrive = getJobCardList("driveunit", jobCardData, jobCardList);
        overAllStatus.put("Drive unit", getOverallStatus(jobCardDataDrive));
        if (getOverallStatus(jobCardDataDrive).equals("Failure")) {
            co2Percentage = +30;
        }

        JobCardData airCondition = jobCardListStatus.get("airCondition");
        overAllStatus.put("Air Condition", getOverallStatus(airCondition));
        if (getOverallStatus(airCondition).equals("Failure")) {
            co2Percentage = +5;
        }
        JobCardData jobCardDataInteriorHeater = jobCardListStatus.get("interiorHeater");
        overAllStatus.put("Interior Heater", getOverallStatus(jobCardDataInteriorHeater));
        if (getOverallStatus(jobCardDataInteriorHeater).equals("Failure")) {
            co2Percentage = +5;
        }
        JobCardData jobCardDataCharger = jobCardListStatus.get("onBoardCharging");
        overAllStatus.put("Onboard Charger", getOverallStatus(jobCardDataCharger));
        if (getOverallStatus(jobCardDataCharger).equals("Failure")) {
            co2Percentage = +10;
        }
        JobCardData jobCardDataConverter = jobCardListStatus.get("dcDcConverter");
        overAllStatus.put("DC/DC Converter", getOverallStatus(jobCardDataConverter));
        if (getOverallStatus(jobCardDataConverter).equals("Failure")) co2Percentage += 10;
        return overAllStatus;
    }

    Map<String, String> generalVoltageOverallStatus(ServiceRecord serviceRecord) {
        Map<String, String> overAllStatus = new LinkedHashMap<>();
        List<JobCard> jobCardList = jobCardRepository.findAll();
        List<JobCardData> jobCardData = ObjectUtils.isEmpty(serviceRecord.getJobCardData()) ? new ArrayList<>() : serviceRecord.getJobCardData();


        JobCardData jobCardDataListBrakes = getJobCardList("brake", jobCardData, jobCardList);
        overAllStatus.put("Brakes", getOverallStatus(jobCardDataListBrakes));
        JobCardData jobCardDataListSuspension = getJobCardList("suspension", jobCardData, jobCardList);
        overAllStatus.put("Suspension", getOverallStatus(jobCardDataListSuspension));
        JobCardData jobCardDataListTire = getJobCardList("tire", jobCardData, jobCardList);
        overAllStatus.put("Tires", getOverallStatus(jobCardDataListTire));
        JobCardData jobCardDataListLight = getJobCardList("light", jobCardData, jobCardList);
        overAllStatus.put("Lights", getOverallStatus(jobCardDataListLight));
        JobCardData jobCardDataListBody = getJobCardList("body", jobCardData, jobCardList);
        overAllStatus.put("Body", getOverallStatus(jobCardDataListBody));
        overAllStatus.put("Other", getOtherOverAllStatus(jobCardData, jobCardList));
        return overAllStatus;
    }


    private JobCardData getJobCardList(String attributeName, List<JobCardData> jobCardDataList, List<JobCard> jobCardList) {
        List<String> brakes = jobCardList.stream().filter(jobCard -> jobCard.getJobCardKey().toLowerCase().contains(attributeName)).map(JobCard::getId).toList();
        JobCardData jobCardData = jobCardDataList.stream().filter(CardData -> brakes.contains(CardData.getJobCardId())).findFirst().orElse(new JobCardData());
        return jobCardData;
    }
    private Map<String, JobCardData> getJobCardList(List<JobCardData> jobCardDataList, List<JobCard> jobCardList) {
        Map<String, JobCardData> jobCardMapping = new HashMap<>();
        List<String> jobCardId = jobCardList.stream().filter(jobCard -> jobCard.getJobCardKey().toLowerCase().contains("electricalcomponents")).map(JobCard::getId).toList();
        JobCardData jobCardData = jobCardDataList.stream().filter(CardData -> jobCardId.contains(CardData.getJobCardId())).findFirst().orElse(new JobCardData());
        JobCardData onBoardCharging = new JobCardData();
        List<JobCardDataAttributes> onBoardChargingAttributes = new ArrayList<>();
        JobCardData dcDcConverter = new JobCardData();
        List<JobCardDataAttributes> dcDcConverterAttributes = new ArrayList<>();
        JobCardData interiorHeater = new JobCardData();
        List<JobCardDataAttributes> interiorHeaterAttributes = new ArrayList<>();
        JobCardData airCondition = new JobCardData();
        List<JobCardDataAttributes> airConditionAttributes = new ArrayList<>();
        JobCardData evBattery = new JobCardData();
        List<JobCardDataAttributes> evBatteryAttributes = new ArrayList<>();

        List<JobCardDataAttributes> jobCardDataAttributesList = jobCardData.getJobCardDataAttributes();
        if (!ObjectUtils.isEmpty(jobCardDataAttributesList)) {
            for (JobCardDataAttributes jobCardDataAttributes : jobCardDataAttributesList) {
                String attributesName = jobCardDataAttributes.getAttributesName();
                if ("chargingSpeed".equals(attributesName) || "chargingPort".equals(attributesName) || "chargingCableTest".equals(attributesName)) {
                    onBoardChargingAttributes.add(jobCardDataAttributes);
                }
                if ("heater".equals(attributesName) || "heatedSeatsCooling".equals(attributesName) || "heatedWindows".equals(attributesName)) {
                    interiorHeaterAttributes.add(jobCardDataAttributes);
                }
                if ("airConditioningCompressor".equals(attributesName) || "capacitor".equals(attributesName) || "acPipes".equals(attributesName)) {
                    airConditionAttributes.add(jobCardDataAttributes);
                }
                if ("battery12V".equals(attributesName)) {
                    dcDcConverterAttributes.add(jobCardDataAttributes);
                }
                if ("visualInspectionOfTheAllBatteries".equals(attributesName)) {
                    evBatteryAttributes.add(jobCardDataAttributes);
                }

            }
        }
        onBoardCharging.setJobCardDataAttributes(onBoardChargingAttributes);
        dcDcConverter.setJobCardDataAttributes(dcDcConverterAttributes);
        airCondition.setJobCardDataAttributes(airConditionAttributes);
        interiorHeater.setJobCardDataAttributes(interiorHeaterAttributes);
        evBattery.setJobCardDataAttributes(evBatteryAttributes);
        jobCardMapping.put("airCondition", airCondition);
        jobCardMapping.put("interiorHeater", interiorHeater);
        jobCardMapping.put("onBoardCharging", onBoardCharging);
        jobCardMapping.put("dcDcConverter", dcDcConverter);
        jobCardMapping.put("evBattery", evBattery);

        return jobCardMapping;

    }

    private String getOtherOverAllStatus(List<JobCardData> jobCardDataList, List<JobCard> jobCardList) {
        List<String> jobCardId = jobCardList.stream().filter(jobCard -> jobCard.getJobCardKey().equals("driveBeltGear") || jobCard.getJobCardKey().equals("electricalComponents") || jobCard.getJobCardKey().equals("steering") || jobCard.getJobCardKey().equals("interior") || jobCard.getJobCardKey().equals("other")).map(JobCard::getId).toList();
        if (ObjectUtils.isEmpty(jobCardDataList)) {
            return "Not checked";
        }
        Set<String> statusSet = new HashSet();
        for (JobCardData jobCardData : jobCardDataList) {
            if (!ObjectUtils.isEmpty(jobCardData) && jobCardId.contains(jobCardData.getJobCardId())) {
                List<JobCardDataAttributes> jobCardDataAttributeList = jobCardData.getJobCardDataAttributes();
                if (!ObjectUtils.isEmpty(jobCardDataAttributeList)) {
                    for (JobCardDataAttributes jobCardDataAttributes : jobCardDataAttributeList) {
                        if (jobCardData.getJobCardId().equals("f5e7077a0a1e448fbe456dc6afb85ff2") && (jobCardDataAttributes.getAttributesName().equals("visualInspectionOfHighVoltageCables") || jobCardDataAttributes.getAttributesName().equals("airNozzles") || jobCardDataAttributes.getAttributesName().equals("pollenfilter") || jobCardDataAttributes.getAttributesName().equals("centralLockDoorLock") || jobCardDataAttributes.getAttributesName().equals("airbag") || jobCardDataAttributes.getAttributesName().equals("instrumentLighting") || jobCardDataAttributes.getAttributesName().equals("cabinFanLowHigh") || jobCardDataAttributes.getAttributesName().equals("fullSystemScanDiagnosisGenerated") || jobCardDataAttributes.getAttributesName().equals("fullSystemScanDiagnosisTool"))) {
                            String status = jobCardDataAttributes.getJobClassifier().getStatus();
                            if ("Fail".equals(status)) return "Failure";
                            statusSet.add(status);
                        } else {
                            String status = jobCardDataAttributes.getJobClassifier().getStatus();
                            if ("Fail".equals(status)) return "Failure";
                            statusSet.add(status);
                        }

                    }
                }
            }
        }
        if (statusSet.contains("Partially OK")) return "Attention";
        if (statusSet.contains("OK")) return "OK";
        return "Not checked";
    }

    private String getOverallStatus(JobCardData jobCardData) {
        Set<String> status = new HashSet<>();
        if (!ObjectUtils.isEmpty(jobCardData) && !ObjectUtils.isEmpty(jobCardData.getJobCardDataAttributes())) {
            jobCardData.getJobCardDataAttributes().forEach(jobAttributes -> {
                if (!"Not checked".equals(jobAttributes.getJobClassifier().getStatus())) {
                    status.add(jobAttributes.getJobClassifier().getStatus());
                }
            });
            if (status.size() == 0) {
                return "Not checked";
            } else {
                String status1 = " ";
                if (status.contains("Fail")) status1 = "Failure";
                else if (status.contains("Partially OK")) status1 = "Attention";
                else if (status.contains("OK")) {
                    status1 = "OK";
                } else status1 = "Not checked";
                return status1;
            }
        } else return "Not checked";
    }

    private static BaseColor selectClassifierBackGroundColor(String status) {
        if ("Attention".equalsIgnoreCase(status)) return new BaseColor(255, 199, 0, 255);
        else if ("OK".equalsIgnoreCase(status) || "Not Checked".equalsIgnoreCase(status))
            return new BaseColor(232, 245, 232, 255);
        else return new BaseColor(255, 0, 0, 255);
    }

    private static BaseColor selectFontColor(String status) {
        if ("Attention".equalsIgnoreCase(status)) return new BaseColor(165, 65, 8, 255);
        else if ("OK".equalsIgnoreCase(status)) return new BaseColor(25, 101, 45, 255);
        else if ("Not Checked".equalsIgnoreCase(status)) return new BaseColor(238, 158, 83);
        else return BaseColor.WHITE;
    }

}
