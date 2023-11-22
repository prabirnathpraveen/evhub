package com.evhub.app.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.drew.imaging.ImageProcessingException;
import com.evhub.app.constant.TermAndCondition;
import com.evhub.app.entities.*;
import com.evhub.app.event.HeaderFooterPageEvent;
import com.evhub.app.event.RoundedBorder;
import com.evhub.app.exception.ValidationException;
import com.evhub.app.repository.JobCardRepository;
import com.evhub.app.repository.ServiceRecordRepository;
import com.evhub.app.repository.VehicleRepository;
import com.evhub.app.util.CommonUtils;
import com.evhub.app.util.ImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AtomicDouble;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.imageio.ImageIO;
import javax.print.DocFlavor;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ItextPdf {
    private static final String INTERMEDIATE_FILE_NAME = "/tmp/firstPdf.pdf";
    private static final String MAIN_PDF_FILE_NAME = "/tmp/conditional_report.pdf";
    @Autowired
    private AmazonS3 s3client;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ServiceRecordRepository serviceRecordRepository;
    @Autowired
    private JobCardRepository jobCardRepository;
    @Autowired
    private S3Service s3Service;
    private Double price = 0d;
    private Integer co2 = 0;
    @Autowired
    private ImageClass imageClass;
    private Double standardRepairCost;
    private Double recommendedRepairCost;
    private Boolean priceFlag;
    @Value("${bucket.name}")
    private String bucketName;
    @Autowired
    private ServiceRecordsService serviceRecordsService;

    public static boolean generalParts = false;

    public static boolean highVoltageParts = false;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0");

    private static final Logger LOG = LogManager.getLogger(ItextPdf.class);

    public Object createNewPdf(String chassisNumber, String serviceNumber) throws Exception {
        Vehicle vehicle = vehicleRepository.findById(chassisNumber)
                .orElseThrow(() -> new ValidationException(HttpStatus.SC_BAD_REQUEST, "Vehicle Information not Found"));
        // cleanUpFiles(INTERMEDIATE_FILE_NAME);
        // cleanUpFiles(MAIN_PDF_FILE_NAME);
        List<ServiceRecord> byChassisNumber = serviceRecordRepository.findByChassisNumber(chassisNumber);
        if (Objects.isNull(serviceNumber)) {
            serviceNumber = (byChassisNumber.stream().max(Comparator.comparingLong(ServiceRecord::getServiceStartTime))
                    .orElse(null).getServiceNumber());
        }
        copyFontToTmp();
        registerAllFont();
        ServiceRecord serviceRecord = serviceRecordRepository.findByChassisNumberAndServiceNumber(chassisNumber,
                serviceNumber);
        if (ObjectUtils.isEmpty(serviceRecord)) {
            throw new ValidationException(org.springframework.http.HttpStatus.BAD_REQUEST.value(),
                    "no service existing");
        }

        List<ServiceRecord> previousServiceRecordList = byChassisNumber.stream()
                .filter(serviceRecord1 -> serviceRecord1.getServiceStartTime() < serviceRecord.getServiceStartTime())
                .sorted(Comparator.comparing(ServiceRecord::getServiceStartTime).reversed()).toList();
        ServiceRecord previousServiceRecord = ObjectUtils.isEmpty(previousServiceRecordList) ? new ServiceRecord()
                : previousServiceRecordList.get(0);
        List<JobCard> jobCardList = jobCardRepository.findAll();
        checkCo2Emmision(serviceRecord, previousServiceRecord, jobCardList);
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        Long serviceEndTime = ObjectUtils.isEmpty(serviceRecord.getServiceEndTime())
                ? serviceRecord.getServiceStartTime()
                : serviceRecord.getServiceEndTime();
        Date date = new Date(serviceEndTime);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String serviceDate = dateFormat.format(date);
        // PdfWriter writer = PdfWriter.getInstance(document, new
        // FileOutputStream(MAIN_PDF_FILE_NAME));
        String pdfName = "/tmp/EVHub_Inspection_Report_" + serviceDate + "_" + serviceNumber + ".pdf";
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfName));
        writer.setCompressionLevel(9);
        HeaderFooterPageEvent headerFooterPageEvent = new HeaderFooterPageEvent();
        headerFooterPageEvent.setServiceNumber(serviceNumber);
        if (!ObjectUtils.isEmpty(serviceEndTime))
            headerFooterPageEvent.setServiceDate(new Date(serviceEndTime));
        writer.setPageEvent(headerFooterPageEvent);
        document.open();

        firstPage(document, vehicle, writer, serviceRecord);

        ColumnText column = new ColumnText(writer.getDirectContent());
        column.setSimpleColumn(20, 60, 286, 750, 56, Element.ALIGN_LEFT);
        AtomicInteger number = new AtomicInteger(1);
        jobCardList.forEach(jobCard -> {
            PdfPTable table = tableHeader(writer, jobCard.getJobCardDisplayName(), number.get());
            List<JobCardData> jobCardData2 = ObjectUtils.isEmpty(serviceRecord.getJobCardData()) ? new ArrayList<>()
                    : serviceRecord.getJobCardData();
            JobCardData jobCardData1 = jobCardData2.stream()
                    .filter(jobCardData -> jobCard.getId().equals(jobCardData.getJobCardId())).findFirst().orElse(null);
            addingContentToPDF(column, document, table, jobCard, jobCardData1, number.get());
            number.getAndIncrement();
        });

        column.addText(Chunk.NEWLINE);
        column.go();
        termAndConditionPage(document, writer);
        document.close();
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_PDF);
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + chassisNumber.replace(" ", "_"));
        // byte[] bytes = IOUtils.toByteArray(new FileInputStream(MAIN_PDF_FILE_NAME));
        byte[] bytes = IOUtils.toByteArray(new FileInputStream(pdfName));

        header.setContentLength(bytes.length);

        HashMap<String, Object> pdfContain = new HashMap<>();
        pdfContain.put("pdfName", pdfName.replaceAll("/tmp/", ""));
        pdfContain.put("pdfContain", bytes);
        // cleanUpFiles(pdfName);
        return pdfContain;
    }

    private void addingContentToPDF(ColumnText column, Document document, PdfPTable table, JobCard jobCard,
            JobCardData jobCardData, Integer jobcardNumber) {
        JobClassifier jobClassifier = new JobClassifier();
        boolean isAttachment = false;
        Integer attributeNumber = 1;
        try {
            // here the data come from the job card
            for (JobCardAttributes jobCardAttribute : jobCard.getJobAttributes()) {
                JobCardDataAttributes jobCardDataAttributes1 = new JobCardDataAttributes();
                jobCardDataAttributes1.setJobClassifier(new JobClassifier());
                if (!ObjectUtils.isEmpty(jobCardData)) {
                    jobCardDataAttributes1 = jobCardData.getJobCardDataAttributes().stream()
                            .filter(jobCardDataAttributes -> jobCardAttribute.getAttributesName()
                                    .equalsIgnoreCase(jobCardDataAttributes.getAttributesName()))
                            .findFirst().orElse(jobCardDataAttributes1);
                }
                String status = jobCardDataAttributes1.getJobClassifier().getStatus();
                jobClassifier.setStatus(ObjectUtils.isEmpty(status) ? "Not Checked" : status);
                jobClassifier.setUnit(jobCardDataAttributes1.getJobClassifier().getUnit());
                jobClassifier.setMeasuredValue(jobCardDataAttributes1.getJobClassifier().getMeasuredValue());
                if (!ObjectUtils.isEmpty(jobCardDataAttributes1.getJobClassifier().getMeasuredValue())) {
                    if (jobCardDataAttributes1.getAttributesName().equals("brakePads")) {
                        jobClassifier
                                .setMeasuredValue(jobCardDataAttributes1.getJobClassifier().getMeasuredValue() + " mm");
                    }
                    if (jobCardDataAttributes1.getAttributesName().equals("brakeFluid")) {
                        jobClassifier
                                .setMeasuredValue(jobCardDataAttributes1.getJobClassifier().getMeasuredValue() + " L");
                    }
                    if (jobCardDataAttributes1.getAttributesName().equals("coolantLiquid")) {
                        jobClassifier
                                .setMeasuredValue(jobCardDataAttributes1.getJobClassifier().getMeasuredValue() + " L");
                    }
                    if (jobCardDataAttributes1.getAttributesName().equals("battery12V")) {
                        jobClassifier
                                .setMeasuredValue(jobCardDataAttributes1.getJobClassifier().getMeasuredValue() + " v");
                    }
                    if (jobCardDataAttributes1.getAttributesName().equals("oil")) {
                        jobClassifier
                                .setMeasuredValue(jobCardDataAttributes1.getJobClassifier().getMeasuredValue() + " L");
                    }
                }
                jobClassifier.setImage(jobCardDataAttributes1.getJobClassifier().getImage());
                jobClassifier.setResult(jobCardDataAttributes1.getJobClassifier().getResult());
                jobClassifier.setDescription(jobCardDataAttributes1.getJobClassifier().getDescription());
                // change after adding new field into the classifier
                Function<String, String> myLambda = (value) -> ObjectUtils.isEmpty(value) ? null : value + " mm";
                jobClassifier.setBrakeDiscThicknessFrontLeft(
                        (myLambda.apply(jobCardDataAttributes1.getJobClassifier().getBrakeDiscThicknessFrontLeft())));
                jobClassifier.setBrakeDiscThicknessFrontRight(
                        myLambda.apply(jobCardDataAttributes1.getJobClassifier().getBrakeDiscThicknessFrontRight()));
                jobClassifier.setBrakeDiscThicknessRearRight(
                        myLambda.apply(jobCardDataAttributes1.getJobClassifier().getBrakeDiscThicknessRearRight()));
                jobClassifier.setBrakeDiscThicknessRearLeft(
                        myLambda.apply(jobCardDataAttributes1.getJobClassifier().getBrakeDiscThicknessRearLeft()));
                jobClassifier.setBrand(jobCardDataAttributes1.getJobClassifier().getBrand());
                jobClassifier
                        .setProductionMonthYear(jobCardDataAttributes1.getJobClassifier().getProductionMonthYear());
                jobClassifier.setTireDimension(jobCardDataAttributes1.getJobClassifier().getTireDimension());
                jobClassifier.setTirePatternFrontLeft(
                        myLambda.apply(jobCardDataAttributes1.getJobClassifier().getTirePatternFrontLeft()));
                jobClassifier.setTirePatternFrontRight(
                        myLambda.apply(jobCardDataAttributes1.getJobClassifier().getTirePatternFrontRight()));
                jobClassifier.setTirePatternRearLeft(
                        myLambda.apply(jobCardDataAttributes1.getJobClassifier().getTirePatternRearLeft()));
                jobClassifier.setTirePatternRearRight(
                        myLambda.apply(jobCardDataAttributes1.getJobClassifier().getTirePatternRearRight()));
                // jobClassifier.setErrorAndDeficiency(jobCardDataAttributes1.getJobClassifier().getErrorAndDeficiency());
                jobClassifier.setEstimatedRepairCostStandard(
                        jobCardDataAttributes1.getJobClassifier().getEstimatedRepairCostStandard());
                jobCardDataAttributes1.getChildAttributes();
                if (jobCard.getJobCardKey().equalsIgnoreCase("theMainBattery"))
                    fillMainBattery(table, jobCardAttribute.getAttributesDisplayName(), jobCardDataAttributes1,
                            jobcardNumber, attributeNumber);
                else if (jobCard.getJobCardKey().equalsIgnoreCase("attachments")) {
                    fillAttachment(table, column, document, jobCardAttribute.getAttributesDisplayName(),
                            jobCardDataAttributes1, jobcardNumber, attributeNumber);
                    isAttachment = true;
                } else
                    setAttributesValue(table, jobCardAttribute.getAttributesDisplayName(), jobClassifier, jobcardNumber,
                            attributeNumber);
                attributeNumber++;
            }

            PdfPCell blankCell = new PdfPCell();
            blankCell.setColspan(10);
            blankCell.setPaddingTop(-10);
            blankCell.addElement(Chunk.NEWLINE);
            blankCell.setBorder(PdfPCell.NO_BORDER);
            blankCell.setBorderColor(BaseColor.WHITE);
            table.addCell(blankCell);
            if (!isAttachment) {
                column.addElement(table);
                int go = column.go(false, table);
                boolean noMoreText = ColumnText.hasMoreText(go);
                while (ColumnText.hasMoreText(go)) {
                    int alignment = column.getAlignment();
                    if ((noMoreText || go == ColumnText.NO_MORE_COLUMN) && alignment == Element.ALIGN_RIGHT) {
                        column.addText(Chunk.NEWLINE);
                        document.newPage();
                        column.setSimpleColumn(20, 60, 286, 750, 16, Element.ALIGN_LEFT);
                        go = column.go();
                    } else if ((noMoreText || go == ColumnText.NO_MORE_COLUMN)) {
                        column.addText(Chunk.NEWLINE);
                        column.setYLine(36);
                        column.setSimpleColumn(309, 60, 575, 750, 16, Element.ALIGN_RIGHT);
                        go = column.go();
                    }
                }
            }
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ImageProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private void fillMainBattery(PdfPTable table, String attributeName, JobCardDataAttributes jobCardDataAttributes,
            Integer jobCardNumber, Integer attributeNumber) {
        JobClassifier jobClassifier = jobCardDataAttributes.getJobClassifier();
        String status = ObjectUtils.isEmpty(jobClassifier.getStatus()) ? "ok" : "ok";
        String unit;
        String measuredValue;

        if (!ObjectUtils.isEmpty(jobCardDataAttributes.getChildAttributes())) {
            measuredValue = "";

        } else if (ObjectUtils.isEmpty(jobClassifier.getMeasuredValue())) {
            measuredValue = "Not Available";
        } else {
            measuredValue = jobClassifier.getMeasuredValue();
        }

        if (measuredValue.equals("Not Available")) {
            jobClassifier.setUnit("");

        }
        unit = jobClassifier.getUnit();

        PdfPCell pdfPCell = new PdfPCell();
        pdfPCell.setColspan(10);
        pdfPCell.setBorder(PdfPCell.NO_BORDER);
        pdfPCell.setBorderColor(BaseColor.WHITE);
        PdfPTable pdfPTable = new PdfPTable(10);
        pdfPTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        pdfPTable.setWidthPercentage(98);
        BaseColor attributeBckGroundColor = setAttributeBckGroundColor(status);
        float x = 0;
        float y = 0;
        float w = 0;
        float h = 0;
        RoundedBorder cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { x, -y, w, h });

        if (attributeName.length() <= 17 && !ObjectUtils.isEmpty(measuredValue)) {
            // cellEvent = new RoundedBorder(attributeBckGroundColor, new float[]{1, -40,
            // -3, -13});
            cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -28, -3, -9 });//

        } else if (attributeName.length() <= 20 && measuredValue.equals("")) {
            cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -27, -3, -10 });//

        } else if (attributeName.length() <= 20 && status.equalsIgnoreCase("ok")) {
            cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -41, -3, -10 });

        } else if (attributeName.length() > 20 && measuredValue.equals("")) {
            cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -27, 4, -13 });
        } else if (attributeName.equalsIgnoreCase("Calculated Amp-hour Capacity CAC")
                && !ObjectUtils.isEmpty(measuredValue)) {
            cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -38, 4, -13 });
        } else if (attributeName.length() > 20) {
            cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -51, -3, -13 });

        } else {
            cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -27, -3, -10 });
        }
        pdfPCell.setCellEvent(cellEvent);
        pdfPCell.setBorder(Rectangle.NO_BORDER);

        BaseColor baseColor = BaseColor.BLACK;
        // Font attributeFont = new Font(Font.FontFamily.UNDEFINED, 9.0f, Font.BOLD,
        // baseColor);
        Font attributeFont = FontFactory.getFont("Poppins-semi", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        attributeFont.setColor(baseColor);
        Paragraph phrase = new Paragraph(jobCardNumber + "." + attributeNumber + " " + attributeName, attributeFont);

        phrase.setAlignment(Element.ALIGN_LEFT);
        int columnSpan = mainBatteryColSpan(status, measuredValue);
        PdfPCell attributeCell = new PdfPCell();
        attributeCell.setPaddingBottom(10);
        attributeCell.setPaddingTop(4);// 10
        attributeCell.setPaddingLeft(5);
        attributeCell.setColspan(10 - columnSpan);
        attributeCell.setBorder(PdfPCell.NO_BORDER);
        attributeCell.setBackgroundColor(BaseColor.WHITE);
        attributeCell.setBorderColor(BaseColor.WHITE);
        attributeCell.addElement(phrase);
        pdfPTable.addCell(attributeCell);

        if (!ObjectUtils.isEmpty(measuredValue)) {
            BaseColor baseColor2 = selectColor(status);
            Font statusFont = FontFactory.getFont("Poppins-bold", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
            statusFont.setColor(baseColor2);
            // Font statusFont = new Font(Font.FontFamily.UNDEFINED, 9.0f, Font.BOLD,
            // baseColor2);
            PdfPCell measuredValueCell = new PdfPCell();
            measuredValueCell.setBorder(PdfPCell.NO_BORDER);
            measuredValueCell.setBackgroundColor(BaseColor.WHITE);
            measuredValueCell.setBorderColor(BaseColor.WHITE);

            Paragraph notOk = new Paragraph(measuredValue + " " + (ObjectUtils.isEmpty(unit) ? "" : unit), statusFont);
            notOk.setAlignment(Element.ALIGN_RIGHT);
            measuredValueCell.addElement(notOk);
            measuredValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            measuredValueCell.setPaddingBottom(10);
            measuredValueCell.setPaddingTop(4);
            if (attributeName.length() > 16) {
                measuredValueCell.setPaddingTop(10);
            }
            measuredValueCell.setPaddingLeft(4);
            if (columnSpan == 6) {
                columnSpan = 3;
            }
            measuredValueCell.setColspan(columnSpan);
            pdfPTable.addCell(measuredValueCell);
        }
        if (!ObjectUtils.isEmpty(status)) {
            BaseColor baseColor2 = selectColor(status);
            // Font statusFont = new Font(Font.FontFamily.UNDEFINED, 9.0f, Font.BOLD,
            // baseColor2);
            Font statusFont = FontFactory.getFont("Poppins-bold", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
            statusFont.setColor(baseColor2);
            PdfPCell attributeStatusCell = new PdfPCell();
            attributeStatusCell.setBorder(PdfPCell.NO_BORDER);
            attributeStatusCell.setBackgroundColor(BaseColor.WHITE);
            attributeStatusCell.setBorderColor(BaseColor.WHITE);

            Paragraph notOk = new Paragraph(getStatus(status), statusFont);
            notOk.setAlignment(Element.ALIGN_RIGHT);
            attributeStatusCell.addElement(notOk);
            attributeStatusCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            attributeStatusCell.setPaddingBottom(10);
            attributeStatusCell.setPaddingTop(4);// 10
            if (attributeName.equalsIgnoreCase("Calculated Amp-hour Capacity CAC")
                    && !measuredValue.equalsIgnoreCase("Not Available")) {
                attributeStatusCell.setPadding(6);
            } else if (attributeName.length() > 16 && !ObjectUtils.isEmpty(measuredValue)) {
                attributeStatusCell.setPaddingTop(10);
            } else if (attributeName.length() > 20 && status.equalsIgnoreCase("ok")
                    && ObjectUtils.isEmpty(measuredValue)) {
                attributeStatusCell.setPaddingTop(2);

            } else if (attributeName.length() > 20) {
                attributeStatusCell.setPaddingTop(10);

            }
            attributeStatusCell.setPaddingLeft(4);
            attributeStatusCell.setColspan(columnSpan);

            pdfPTable.addCell(attributeStatusCell);
        }
        if (status.equalsIgnoreCase("not checked") && ObjectUtils.isEmpty(measuredValue)) {
            status = getStatus("");

            BaseColor baseColor2 = selectColor(status);
            // BaseColor baseColor2 = status.equalsIgnoreCase("OK") ||
            // status.equalsIgnoreCase("Not Checked") ? selectColor(status) :
            // BaseColor.WHITE;
            // Font statusFont = new Font(Font.FontFamily.UNDEFINED, 9.0f, Font.BOLD,
            // baseColor2);
            Font statusFont = FontFactory.getFont("Poppins-bold", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
            statusFont.setColor(baseColor2);
            PdfPCell attributeStatusCell = new PdfPCell();
            attributeStatusCell.setBorder(PdfPCell.NO_BORDER);
            attributeStatusCell.setBackgroundColor(BaseColor.WHITE);
            attributeStatusCell.setBorderColor(BaseColor.WHITE);

            Paragraph notOk = new Paragraph(status, statusFont);
            notOk.setAlignment(Element.ALIGN_RIGHT);
            attributeStatusCell.addElement(notOk);
            attributeStatusCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            attributeStatusCell.setPaddingBottom(10);
            attributeStatusCell.setPaddingTop(4);// 10
            attributeStatusCell.setPaddingLeft(4);
            attributeStatusCell.setColspan(columnSpan);
            // if(attributeName.length()>17){
            // cellEvent = new RoundedBorder(new BaseColor(245, 245, 245, 255), new
            // float[]{1, -42, -2, -8});
            // attributeStatusCell.setPaddingTop(17);
            // }
            // else {
            // cellEvent = new RoundedBorder(new BaseColor(245, 245, 245, 255), new
            // float[]{1, -30, -2, -9});
            // // cellEvent = new RoundedBorder(new BaseColor(245, 245, 245, 255), new
            // float[]{1, -28, -3, -10});
            //
            // }
            pdfPCell.setCellEvent(cellEvent);
            pdfPCell.setBorderColor(new BaseColor(245, 245, 245, 255));
            pdfPTable.addCell(attributeStatusCell);
        }
        pdfPCell.addElement(pdfPTable);
        table.addCell(pdfPCell);
        if ("OK".equalsIgnoreCase(status)
                && ("Cell Temperature".equalsIgnoreCase(attributeName) || "Cell voltage".equalsIgnoreCase(attributeName)
                        || "Calculated Amp-hour Capacity CAC".equalsIgnoreCase(attributeName))) {
            LinkedHashMap<String, String> childAttributes = jobCardDataAttributes.getChildAttributes();
            table.addCell(addMaxMinValue(ObjectUtils.isEmpty(childAttributes) ? new LinkedHashMap<>() : childAttributes,
                    jobClassifier.getUnit(), attributeName));
        }
    }

    private PdfPCell addMaxMinValue(LinkedHashMap<String, String> childAttributes, String unit, String attributeName) {
        PdfPCell pdfPCell = new PdfPCell();
        pdfPCell.setBorder(Rectangle.NO_BORDER);
        PdfPTable pdfPTable;
        pdfPTable = new PdfPTable(3);
        if (childAttributes.containsKey("imbalance")) {
            pdfPTable = new PdfPTable(4);
        }
        pdfPTable.setWidthPercentage(95);
        Font classifierFont = new Font(Font.FontFamily.UNDEFINED, 8.0f, Font.BOLD, new BaseColor(143, 142, 142, 255));// 8
        Font valueFont = new Font(Font.FontFamily.UNDEFINED, 8, Font.BOLD, BaseColor.BLACK);// 10
        pdfPTable.setWidthPercentage(95);

        unit = ObjectUtils.isEmpty(unit) ? "" : unit;

        PdfPCell minimum = new PdfPCell();
        PdfPCell average = new PdfPCell();
        PdfPCell maximum = new PdfPCell();
        PdfPCell imBalance = new PdfPCell();
        PdfPCell minimumValue = new PdfPCell();
        PdfPCell averageValue = new PdfPCell();
        PdfPCell maximumValue = new PdfPCell();
        PdfPCell imBalanceValue = new PdfPCell();
        if (childAttributes.containsKey("imbalance")) {
            maxMinMethod(imBalance, "Imbalance", classifierFont);
        }
        maxMinMethod(maximum, "Maximum", classifierFont);
        maxMinMethod(minimum, "Minimum", classifierFont);
        maxMinMethod(average, "Average", classifierFont);

        minimumValue.setBorder(Rectangle.LEFT);
        minimumValue.setBorderColor(BaseColor.LIGHT_GRAY);
        minimumValue.setRowspan(2);
        String minmumValue = ObjectUtils.isEmpty(childAttributes.get("minimum")) ? "NA"
                : childAttributes.get("minimum");
        if (minmumValue.equals("NA")) {
            minimumValue.addElement(new Paragraph(minmumValue, valueFont));
        } else {
            minimumValue.addElement(new Paragraph(minmumValue + unit, valueFont));
        }
        averageValue.setBorder(Rectangle.LEFT);
        averageValue.setBorderColor(BaseColor.LIGHT_GRAY);
        averageValue.setRowspan(2);
        String avgValue = ObjectUtils.isEmpty(childAttributes.get("average")) ? "NA" : childAttributes.get("average");
        if (avgValue.equals("NA")) {
            averageValue.addElement(new Paragraph(avgValue, valueFont));
        } else {
            averageValue.addElement(new Paragraph(avgValue + unit, valueFont));
        }
        maximumValue.setBorder(Rectangle.LEFT);
        maximumValue.setBorderColor(BaseColor.LIGHT_GRAY);
        maximumValue.setRowspan(2);
        String maxValue = ObjectUtils.isEmpty(childAttributes.get("maximum")) ? "NA" : childAttributes.get("maximum");
        if (maxValue.equals("NA")) {
            maximumValue.addElement(new Paragraph(maxValue, valueFont));
        } else {
            maximumValue.addElement(new Paragraph(maxValue + unit, valueFont));
        }

        if (childAttributes.containsKey("imbalance")) {
            if (attributeName.equalsIgnoreCase("cell voltage")) {
                imBalanceValue.setBorder(Rectangle.LEFT);
                imBalanceValue.setBorderColor(BaseColor.LIGHT_GRAY);
                imBalanceValue.setRowspan(2);
                String imbalanceUnit = ObjectUtils.isEmpty(childAttributes.get("imbalance")) ? "NA"
                        : childAttributes.get("imbalance");
                // int index = imbalanceUnit.indexOf('V');
                // String subString = imbalanceUnit.substring(0, index);

                if (imbalanceUnit.equals("NA")) {
                    imBalanceValue.addElement(new Paragraph(imbalanceUnit, valueFont));
                } else {
                    imBalanceValue.addElement(new Paragraph(imbalanceUnit + "mV", valueFont));
                }
            } else {
                imBalanceValue.setBorder(Rectangle.LEFT);
                imBalanceValue.setBorderColor(BaseColor.LIGHT_GRAY);
                imBalanceValue.setRowspan(2);
                String imbalanceUnit = ObjectUtils.isEmpty(childAttributes.get("imbalance")) ? "NA"
                        : childAttributes.get("imbalance");
                if (imbalanceUnit.equals("NA")) {
                    imBalanceValue.addElement(new Paragraph(imbalanceUnit, valueFont));
                } else {
                    imBalanceValue.addElement(new Paragraph(imbalanceUnit + unit, valueFont));

                }
            }
        }

        pdfPTable.addCell(minimum);
        pdfPTable.addCell(maximum);

        pdfPTable.addCell(average);
        if (childAttributes.containsKey("imbalance")) {
            pdfPTable.addCell(imBalance);
        }

        pdfPTable.addCell(minimumValue);
        pdfPTable.addCell(maximumValue);
        pdfPTable.addCell(averageValue);
        if (childAttributes.containsKey("imbalance")) {
            pdfPTable.addCell(imBalanceValue);
        }

        pdfPCell.setColspan(10);
        pdfPCell.addElement(pdfPTable);

        return pdfPCell;
    }

    public void maxMinMethod(PdfPCell name, String unit, Font classifierFont) {
        name.setBorder(Rectangle.LEFT);
        name.setBorderColor(BaseColor.LIGHT_GRAY);
        name.addElement(new Paragraph(unit, classifierFont));

    }

    private int mainBatteryColSpan(String status, String measuredValue) {
        if (ObjectUtils.isEmpty(status) && ObjectUtils.isEmpty(measuredValue)) {
            return 3;
        } else if (!ObjectUtils.isEmpty(status) && ObjectUtils.isEmpty(measuredValue)) {
            return columnSpan(status);
        } else
            return 6;

    }

    private void setAttributesValue(PdfPTable table, String attributeName, JobClassifier jobClassifier,
            Integer jobCardNumber, Integer attributeNumber) throws DocumentException {

        String status = jobClassifier.getStatus();
        PdfPCell pdfPCell = new PdfPCell();
        pdfPCell.setColspan(10);
        pdfPCell.setBorder(PdfPCell.NO_BORDER);
        pdfPCell.setBorderColor(BaseColor.WHITE);
        PdfPTable pdfPTable = new PdfPTable(10);
        pdfPTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        pdfPTable.setWidthPercentage(100);
        BaseColor attributeBckGroundColor = setAttributeBckGroundColor(status);
        RoundedBorder cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -28, -3, -10 });
        if (attributeName.equals("Full System Scan Diagnosis Generated") && !status.equals("OK")) {
            cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -40, -3, -10 });
        } else if (attributeName.equals("General Control Of Lights Reflectors")
                && (status.equals("Fail") || status.equals("Partially OK"))) {
            cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -40, -3, -10 });
        } else if (attributeName.equals("Visual Inspection Of The All Batteries") && status.equals("Fail")) {
            cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -40, -3, -10 });
        }

        // BaseColor baseColor = status.equalsIgnoreCase("OK") ||
        // status.equalsIgnoreCase("Not Checked") ? BaseColor.BLACK : BaseColor.WHITE;
        BaseColor baseColor = status.toLowerCase().contains("fail") ? BaseColor.WHITE : BaseColor.BLACK;
        // Font attributeFont = new Font(Font.FontFamily.UNDEFINED, 9.0f, Font.BOLD,
        // baseColor);
        Font attributeFont = FontFactory.getFont("Poppins-semi", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        attributeFont.setColor(baseColor);
        String attribute = jobCardNumber + "." + attributeNumber + " " + attributeName;
        Paragraph phrase = new Paragraph(attribute, attributeFont);
        phrase.setAlignment(Element.ALIGN_LEFT);
        int columnSpan = columnSpan(status);
        PdfPCell cellInternal = new PdfPCell();

        cellInternal.setPaddingBottom(10);
        cellInternal.setPaddingTop(5);
        cellInternal.setPaddingLeft(5);
        cellInternal.setColspan(10 - columnSpan);

        cellInternal.setBorder(PdfPCell.NO_BORDER);
        cellInternal.setBackgroundColor(BaseColor.WHITE);
        cellInternal.setBorderColor(BaseColor.WHITE);

        cellInternal.addElement(phrase);
        cellInternal.setCalculatedHeight(10);
        pdfPTable.addCell(cellInternal);
        BaseColor baseColor2 = selectColor(status);
        // BaseColor baseColor2 = status.equalsIgnoreCase("OK") ||
        // status.equalsIgnoreCase("Not Checked") ? selectColor(status) :
        // BaseColor.WHITE;
        // Font statusFont = new Font(Font.FontFamily.UNDEFINED, 9.0f, Font.BOLD,
        // baseColor2);
        Font statusFont = FontFactory.getFont("Poppins-bold", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        statusFont.setColor(baseColor2);
        PdfPCell cellInternal1 = new PdfPCell();
        cellInternal1.setBorder(PdfPCell.NO_BORDER);
        cellInternal1.setBackgroundColor(BaseColor.WHITE);
        cellInternal1.setBorderColor(BaseColor.WHITE);

        String status1 = getStatus(status);
        String charterLength = attribute + status1;
        Paragraph notOk = new Paragraph(status1, statusFont);
        notOk.setAlignment(Element.ALIGN_RIGHT);
        cellInternal1.addElement(notOk);
        cellInternal1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellInternal1.setPaddingBottom(10);
        cellInternal1.setPaddingTop(5);
        cellInternal1.setPaddingLeft(4);
        cellInternal1.setColspan(columnSpan);
        pdfPTable.addCell(cellInternal1);
        if (charterLength.length() > 50) {
            cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -40, -3, -10 });
        }
        if (attributeName.equalsIgnoreCase("Visual Inspection Of The Underside Of Ducts")
                && "OK".equalsIgnoreCase(status1)) {
            cellEvent = new RoundedBorder(attributeBckGroundColor, new float[] { 1, -28, -3, -10 });
        }
        pdfPCell.setCellEvent(cellEvent);
        pdfPCell.addElement(pdfPTable);
        // table.addCell(pdfPCell);
        addCellToTable(table, jobClassifier, pdfPCell);
    }

    // private int[] cellWidth(String status) {
    //
    // if ("Not Checked".equalsIgnoreCase(status) || "Next
    // Service".equalsIgnoreCase(status)) {
    // int[] cellWidth = {75, 25};
    // return cellWidth;
    // } else if ("OK".equalsIgnoreCase(status)) {
    // int[] cellWidth = {90, 10};
    // return cellWidth;
    // } else {
    // int[] cellWidth = {65, 35};
    // return cellWidth;
    // }
    // }

    private int columnSpan(String status) {

        // if ("Not Checked".equalsIgnoreCase(status) || "Next
        // Service".equalsIgnoreCase(status)) {
        // return 3;
        if ("OK".equalsIgnoreCase(status)) {
            return 1;
        } else {
            return 3;
        }
    }

    private BaseColor selectColor(String status) {
        if ("Not Checked".equalsIgnoreCase(status))
            return new BaseColor(238, 158, 83);
        else if ("Partially OK".equalsIgnoreCase(status))
            return BaseColor.BLACK;
        else if ("OK".equalsIgnoreCase(status))
            return new BaseColor(23, 156, 20);
        else if (!ObjectUtils.isEmpty(status) && !("Fail".equalsIgnoreCase(status)))
            return new BaseColor(23, 156, 20);
        else
            return BaseColor.WHITE;
    }

    private PdfPTable tableHeader(PdfWriter writer, String k, int number) {
        // Font headerFont = new Font(Font.FontFamily.UNDEFINED, 15.0f, Font.BOLD,
        // BaseColor.WHITE);
        Font headerFont = FontFactory.getFont("Poppins-semi", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 15);
        headerFont.setColor(BaseColor.WHITE);
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        PdfPTable pdfPTable = new PdfPTable(10);
        pdfPTable.setWidthPercentage(100);
        PdfPCell pdfPCell = new PdfPCell();
        pdfPCell.setColspan(10);
        pdfPCell.setBorderColor(BaseColor.WHITE);
        pdfPCell.setBackgroundColor(BaseColor.WHITE);
        pdfPCell.setBorder(Rectangle.NO_BORDER);
        pdfPCell.setCellEvent(new RoundedBorder(new BaseColor(16, 75, 140), new float[] { 1, -50, -2, -14 }));
        pdfPCell.setPaddingBottom(10);
        String headerValue = number + ". " + k;
        Phrase phrase = new Phrase(headerValue, headerFont);
        PdfPCell cell = new PdfPCell();
        cell.addElement(phrase);
        cell.setColspan(8);
        cell.setBackgroundColor(BaseColor.WHITE);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPaddingBottom(10);
        cell.setPaddingTop(10);
        cell.setPaddingLeft(5);
        cell.setBorderColor(new BaseColor(24, 76, 140));
        pdfPTable.addCell(cell);
        try {
            PdfPCell arrowCell = new PdfPCell();
            Image image = Image.getInstance(this.getClass().getResource("/image/arrow.png"));
            image.scaleToFit(25, 25);
            arrowCell.addElement(image);
            arrowCell.setColspan(2);
            arrowCell.setBackgroundColor(BaseColor.WHITE);
            arrowCell.setBorder(PdfPCell.NO_BORDER);
            arrowCell.setPaddingTop(16);
            arrowCell.setPaddingLeft(27);
            arrowCell.setBorderColor(new BaseColor(24, 76, 140));
            pdfPTable.addCell(arrowCell);
            pdfPCell.addElement(pdfPTable);
            table.addCell(pdfPCell);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (BadElementException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return table;
    }

    private void cleanUpFiles(String path) {
        File targetFile = new File(path);
        targetFile.delete();
    }

    private void fillAttachment(PdfPTable table, ColumnText column, Document document, String attributeName,
            JobCardDataAttributes jobCardDataAttributes, Integer jobCardNumber, Integer attributeNumber)
            throws DocumentException, IOException, ImageProcessingException {
        List<String> imagesString = new ArrayList<>();
        int alignment = column.getAlignment();
        JobClassifier jobClassifier = jobCardDataAttributes.getJobClassifier();
        String status = jobClassifier.getStatus();
        PdfPCell pdfPCell = new PdfPCell();
        pdfPCell.setColspan(10);
        pdfPCell.setBorder(PdfPCell.NO_BORDER);
        pdfPCell.setBorderColor(BaseColor.WHITE);
        PdfPTable pdfPTable1 = new PdfPTable(10);
        pdfPTable1.setHorizontalAlignment(Element.ALIGN_LEFT);
        pdfPTable1.setWidthPercentage(98);
        RoundedBorder cellEvent = new RoundedBorder(new BaseColor(244, 244, 244, 255), new float[] { 1, -28, -3, -10 });
        Font attributeFont = FontFactory.getFont("Poppins-semi", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        attributeFont.setColor(BaseColor.BLACK);
        String attribute = jobCardNumber + "." + attributeNumber + " " + attributeName;
        Paragraph phrase = new Paragraph(attribute, attributeFont);
        phrase.setAlignment(Element.ALIGN_LEFT);
        // int columnSpan = columnSpan(status);
        PdfPCell cellInternal = new PdfPCell();

        cellInternal.setPaddingBottom(10);
        cellInternal.setPaddingTop(5);
        cellInternal.setPaddingLeft(5);
        cellInternal.setColspan(10);

        cellInternal.setBorder(PdfPCell.NO_BORDER);
        cellInternal.setBackgroundColor(BaseColor.WHITE);
        cellInternal.setBorderColor(BaseColor.WHITE);

        cellInternal.addElement(phrase);
        cellInternal.setCalculatedHeight(10);
        pdfPTable1.addCell(cellInternal);

        pdfPCell.setCellEvent(cellEvent);
        pdfPCell.addElement(pdfPTable1);
        table.addCell(pdfPCell);
        column.addElement(table);

        column.go();
        if (!ObjectUtils.isEmpty(jobClassifier)) {
            List<String> images = jobClassifier.getImage();

            if (!ObjectUtils.isEmpty(images)) {
                int size = images.size();
                int i = 0;
                while (i < size) {
                    PdfPTable pdfPTable = new PdfPTable(10);
                    // byte[] data = Base64.getDecoder().decode(images.get(i).split("base64,")[1]);
                    byte[] data = getByteArrayFromImageS3Bucket(images.get(i).split("com/")[1]);
                    try {
                        PdfPCell imageCell = new PdfPCell();
                        imageCell.setColspan(2);
                        imageCell.setBorder(PdfPCell.NO_BORDER);
                        imageCell.setBorderColor(BaseColor.WHITE);
                        Image image = Image.getInstance(data);
                        image.setAlignment(Element.ALIGN_LEFT);
                        imageCell.addElement(image);
                        imageCell.setColspan(10);
                        imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        pdfPTable.addCell(imageCell);
                        column.addElement(pdfPTable);
                        i++;
                        int go = column.go();
                        boolean noMoreText = ColumnText.hasMoreText(go);
                        if (noMoreText && column.getAlignment() == Element.ALIGN_RIGHT) {
                            document.newPage();
                            column.setSimpleColumn(20, 60, 575, 750, 16, Element.ALIGN_LEFT);
                            break;
                        } else if (noMoreText && alignment == Element.ALIGN_LEFT) {
                            column.setSimpleColumn(309, 60, 575, 750, 16, Element.ALIGN_RIGHT);
                        } else if (noMoreText) {
                            document.newPage();
                            column.setSimpleColumn(20, 60, 575, 750, 16, Element.ALIGN_LEFT);
                        }
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    } catch (BadElementException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
                while (i < size) {
                    {
                        PdfPTable pdfPTable = new PdfPTable(10);
                        byte[] data = getByteArrayFromImageS3Bucket(images.get(i).split("com/")[1]);
                        try {
                            PdfPCell imageCell = new PdfPCell();
                            imageCell.setBorder(PdfPCell.NO_BORDER);
                            imageCell.setBorderColor(BaseColor.WHITE);
                            Image image = Image.getInstance(data);
                            i++;
                            image.setAlignment(Element.ALIGN_LEFT);
                            imageCell.addElement(image);
                            imageCell.setColspan(5);
                            imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            pdfPTable.addCell(imageCell);

                            if (i < size) {
                                byte[] data1 = getByteArrayFromImageS3Bucket(images.get(i).split("com/")[1]);

                                PdfPCell imageCell1 = new PdfPCell();
                                imageCell1.setBorder(PdfPCell.NO_BORDER);
                                imageCell1.setBorderColor(BaseColor.WHITE);
                                Image secondImage = Image.getInstance(data1);
                                i++;
                                secondImage.setAlignment(Element.ALIGN_LEFT);
                                imageCell1.addElement(secondImage);
                                imageCell1.setColspan(5);
                                imageCell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                                pdfPTable.addCell(imageCell1);

                            }
                            column.addElement(pdfPTable);
                            int go = column.go();
                            boolean noMoreText = ColumnText.hasMoreText(go);
                            if (noMoreText) {
                                document.newPage();
                                column.setSimpleColumn(20, 60, 575, 750, 16, Element.ALIGN_LEFT);
                            }

                            // } else {
                            // PdfPCell imageCell = new PdfPCell();
                            // imageCell.setBorder(PdfPCell.NO_BORDER);
                            // imageCell.setBackgroundColor(BaseColor.WHITE);
                            // imageCell.setBorderColor(BaseColor.WHITE);
                            // imageCell.setColspan(5);
                            // imageCell.addElement(Image.getInstance(data));
                            // i++;
                            // byte[] data2 = Base64.getDecoder().decode(images.get(i).split("base64,")[1]);
                            //
                            // PdfPCell imageCell1 = new PdfPCell();
                            // imageCell1.setBorder(PdfPCell.NO_BORDER);
                            // imageCell1.setBackgroundColor(BaseColor.WHITE);
                            // imageCell1.setBorderColor(BaseColor.WHITE);
                            // imageCell1.setColspan(5);
                            // imageCell1.addElement(Image.getInstance(data2));
                            // table.addCell(imageCell);
                            // table.addCell(imageCell1);
                            // i++;
                            // }
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        } catch (BadElementException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }

                // if(ColumnText.hasMoreText(column.go())){
                // column.setSimpleColumn(20, 60, 575, 750, 16, Element.ALIGN_LEFT);
                // }
            }
            // }
        }

    }

    private void addCellToTable(PdfPTable table, JobClassifier jobClassifier, PdfPCell cell) {
        // AtomicBoolean image = new AtomicBoolean(false);
        AtomicBoolean first = new AtomicBoolean(true);
        PdfPCell firstCell = new PdfPCell();
        List<String> list = new ArrayList<>();
        // pdfPCell.setBorder(PdfPCell.NO_BORDER);
        Font classifierFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 7);
        classifierFont.setColor(new BaseColor(136, 136, 136));
        Font valueFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        valueFont.setColor(BaseColor.BLACK);
        BaseColor backgroundColor = selectClassifierBackGroundColor(jobClassifier.getStatus());
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> classifierKeyValue = objectMapper.convertValue(jobClassifier, Map.class);
        classifierKeyValue.remove("status");
        classifierKeyValue.remove("errorAndDeficiency");
        boolean allValuesAreNull = classifierKeyValue.values()
                .stream()
                .allMatch(ObjectUtils::isEmpty);
        if (!allValuesAreNull) {
            classifierKeyValue.forEach((key, value) -> {
                if (!key.equalsIgnoreCase("unit") && !ObjectUtils.isEmpty(value)) {
                    if (!"image".equalsIgnoreCase(key)) {
                        PdfPTable table1 = new PdfPTable(10);
                        table1.setWidthPercentage(101);
                        PdfPCell classifierName = new PdfPCell();
                        classifierName.setBackgroundColor(backgroundColor);
                        classifierName.setBorderColor(backgroundColor);
                        String attributeName = WordUtils
                                .capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(key), ' '));
                        Phrase element = new Phrase(attributeName, classifierFont);
                        classifierName.addElement(element);
                        classifierName.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        classifierName.setPaddingBottom(-2);
                        classifierName.setColspan(10);
                        classifierName.setPaddingLeft(10);
                        table1.addCell(classifierName);

                        PdfPCell classifierValue = new PdfPCell();
                        PdfPCell classifierValueImage = new PdfPCell();
                        classifierValueImage.setColspan(10);
                        classifierValue.setBackgroundColor(backgroundColor);
                        classifierValue.setBorderColor(backgroundColor);
                        Phrase element1 = new Phrase((String) value, valueFont);
                        if (key.equalsIgnoreCase("measuredValue")) {
                            Object unit = classifierKeyValue.get("unit");
                            element1.add(new Phrase((Objects.nonNull(unit) ? " " + unit : ""), classifierFont));
                        }
                        classifierValue.addElement(element1);
                        classifierValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        classifierValue.setPaddingBottom(5);
                        classifierValue.setTop(0);
                        classifierValue.setPaddingLeft(10);
                        classifierValue.setPaddingRight(10);
                        classifierValue.setColspan(10);
                        table1.addCell(classifierValue);
                        PdfPCell pdfPCell = new PdfPCell();
                        pdfPCell.setBorder(PdfPCell.NO_BORDER);
                        pdfPCell.setBackgroundColor(backgroundColor);
                        pdfPCell.setColspan(10);
                        pdfPCell.setPaddingTop(-2);
                        pdfPCell.addElement(table1);
                        if (first.get()) {
                            PdfPTable firstTable = new PdfPTable(10);
                            firstTable.setWidthPercentage(101.5f);
                            firstTable.addCell(cell);
                            firstTable.addCell(pdfPCell);
                            firstCell.setColspan(10);
                            firstCell.setBorder(Rectangle.NO_BORDER);
                            firstCell.addElement(firstTable);
                            table.addCell(firstCell);
                            first.set(false);
                        } else
                            table.addCell(pdfPCell);
                    } else {
                        addImageToClassifier(table, (List<String>) value, backgroundColor, cell, first.get());
                        first.set(false);
                    }

                }
            });
        } else {
            table.addCell(cell);
            first.set(false);
        }
        // if (image.get()) {
        // addImageToClassifier(table, list, backgroundColor);
        // }
    }

    public static BaseColor selectClassifierBackGroundColor(String status) {
        if ("Partially OK".equalsIgnoreCase(status))
            return new BaseColor(255, 253, 232, 255);
        else if ("OK".equalsIgnoreCase(status))
            return new BaseColor(243, 255, 235, 255);
        else
            return new BaseColor(254, 240, 240, 255);
    }

    private BaseColor setAttributeBckGroundColor(String status) {
        if ("Partially OK".equalsIgnoreCase(status))
            return new BaseColor(255, 199, 0, 255);
        else if ("OK".equalsIgnoreCase(status) || "Not Checked".equalsIgnoreCase(status))
            return new BaseColor(244, 244, 244, 255);
        // else if(!ObjectUtils.isEmpty(status)&&!("Partially
        // Ok".equalsIgnoreCase(status))&&!("Failure".equalsIgnoreCase(status))) return
        // new BaseColor(0,255,0,255);
        else if (!(ObjectUtils.isEmpty(status)) && !("Partially OK".equalsIgnoreCase(status))
                && !("OK".equalsIgnoreCase(status)) && !("Not Checked".equalsIgnoreCase(status))
                && !("Fail".equalsIgnoreCase(status)))
            return new BaseColor(244, 244, 244, 255);
        else
            return new BaseColor(255, 0, 0, 255);
    }

    private void termAndConditionPage(Document document, PdfWriter writer) throws IOException, DocumentException {

        HeaderFooterPageEvent pageEvent = (HeaderFooterPageEvent) writer.getPageEvent();
        pageEvent.setTermsAndCondition(true);
        writer.setPageEvent(pageEvent);
        document.newPage();
        Font font2 = FontFactory.getFont("Poppins400", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 7);

        // document.add(Image.getInstance(this.getClass().getResource("/image/evhub.jpeg")));
        Font font1 = new Font(Font.FontFamily.UNDEFINED, 11f, Font.BOLD, new BaseColor(16, 75, 140));
        // Font font2 = new Font(Font.FontFamily.UNDEFINED, 7.3f, Font.NORMAL,
        // BaseColor.BLACK);

        // document.add(new Paragraph("EV HUB Terms & Conditions", headerFont));
        // document.add(new Paragraph("for EV/Hybrid Vehicles", header));
        TermAndCondition termAndCondition = new TermAndCondition();
        ColumnText column = new ColumnText(writer.getDirectContent());
        column.setSimpleColumn(36, 250, 286, 700);

        termAndCondition.termsAndCondition1().forEach((k, v) -> {
            Paragraph paragraph = new Paragraph(k, font1);
            try {
                column.addElement(paragraph);
                if ("".equals(k)) {
                    column.setSimpleColumn(309, 250, 513, 715);
                    column.addElement(Image.getInstance(this.getClass().getResource("/image/imageTermCondition.png")));

                } else {
                    Paragraph paragraph1 = new Paragraph(v, font2);
                    // paragraph1.setAlignment(Element.ALIGN_JUSTIFIED);
                    column.addElement(paragraph1);
                }
                column.addElement(Chunk.NEWLINE);
                boolean noMoreText = ColumnText.hasMoreText(column.go());
                if (noMoreText) {
                    column.setYLine(36);
                    column.setSimpleColumn(309, 250, 559, 700);// 309, 45, 559, 760
                    column.go();
                }
            } catch (BadElementException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }
        });
        column.setSimpleColumn(36, 30, 559, 410);
        termAndCondition.termsAndCondition2().forEach((k, v) -> {
            try {
                column.addElement(new Paragraph(k, font1));
                Paragraph elements = new Paragraph(v, font2);
                // elements.setAlignment(Element.ALIGN_JUSTIFIED);
                column.addElement(elements);
                column.addElement(Chunk.NEWLINE);
                column.go();
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String getStatus(String status) {
        if ("Partially OK".equalsIgnoreCase(status))
            return "Attention";
        else if ("OK".equalsIgnoreCase(status))
            return "OK";
        else if (status.equalsIgnoreCase("fail"))
            return "Failure";
        else if (!ObjectUtils.isEmpty(status))
            return status;
        else
            return "Not Checked";
    }

    public S3Object getImageFromS3Bucket(String fileName) {
        // S3Object object = s3client.getObject(new GetObjectRequest("evhub-data-dev",
        // fileName));
        S3Object object = s3client.getObject(bucketName, fileName);
        return object;
    }

    public byte[] getByteArrayFromImageS3Bucket(String fileName) throws IOException, ImageProcessingException {
        ImageService imageService = new ImageService();
        byte[] awsByte = IOUtils.toByteArray(getImageFromS3Bucket(fileName).getObjectContent());
        int byteSize = awsByte.length;
        double imageSizeMB = (double) byteSize / 1048576;
        double imageSizeinKb = (double) byteSize / 1024;
        String extension = fileName.split("\\.")[1];
        if (imageSizeMB > 7.0) {
            // do compression
            LOG.info("The size of image is greater then 7 MB so compression needed " + fileName);
            int orientation = imageService.getOrientation(awsByte, extension);
            BufferedImage rotatedImage = imageService.getRotatedImage(awsByte, orientation);
            return imageService.compressImage(rotatedImage, extension, imageSizeinKb);
        }
        return awsByte;

        /*
         * InputStream in = getImageFromS3Bucket(fileName).getObjectContent();
         * int lastIndex = fileName.lastIndexOf('.');
         * String imageExtensions = fileName.substring(lastIndex + 1);
         * 
         * 
         * BufferedImage imageFromAWS = ImageIO.read(in);
         * ByteArrayOutputStream baos = new ByteArrayOutputStream();
         * ImageIO.write(imageFromAWS, imageExtensions, baos);
         * byte[] imageBytes = baos.toByteArray();
         * in.close();
         * return imageBytes;
         */
    }

    public byte[] getByteArrayFromImageS3BucketForMultiple(String fileName, Vehicle vehicle) throws IOException {
        InputStream in = getImageFromS3Bucket(fileName).getObjectContent();

        BufferedImage imageFromAWS = ImageIO.read(in);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(imageFromAWS, extensionFind(vehicle), baos);
        byte[] imageBytes = baos.toByteArray();
        in.close();
        return imageBytes;
    }

    public String extensionFind(Vehicle vehicle) {
        String extractedData = null;
        Pattern pattern = Pattern.compile("image/(.*?);");
        Matcher matcher = pattern.matcher(vehicle.getImage());
        if (matcher.find()) {
            extractedData = matcher.group(1);
        }
        return extractedData;
    }

    private void firstPage(Document document, Vehicle vehicle, PdfWriter writer, ServiceRecord serviceRecord)
            throws DocumentException {
        Circle circle = new Circle();
        try {
            byte[] imageArray;

            String vehicleImage = vehicle.getImage();
            if (!ObjectUtils.isEmpty(vehicleImage)) {
                imageArray = Base64.getDecoder().decode(vehicleImage.split("base64,")[1]);// images.get(i).split("base64,")[1]
                if (vehicleImage.contains("data:image")) {
                    Pattern pattern = Pattern.compile("image/(.*?);");
                    Matcher matcher = pattern.matcher(vehicleImage);
                    String extension = "jpej";
                    if (matcher.find()) {
                        extension = matcher.group(1);
                    }
                    ImageService imageService = new ImageService();
                    int orientation = imageService.getOrientation(imageArray, extension);
                    BufferedImage rotatedImage = imageService.getRotatedImage(imageArray, orientation);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ImageIO.write(rotatedImage, extension, bos);
                    imageArray = bos.toByteArray();
                }
            } else {
                BufferedImage image = getDefaultImage(vehicle.getModel());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", bos);
                imageArray = bos.toByteArray();
            }
            // InputStream targetStream = new ByteArrayInputStream(imageArray);
            // BufferedImage bufferedImage = ImageIO.read(targetStream);
            // BufferedImage bufferedImage1 = RoundImage.makeRoundedCorner(bufferedImage,
            // 20);
            // ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // ImageIO.write(bufferedImage1, extensionFind(vehicle), baos);
            byte[] bytes = imageArray;

            Image image = Image.getInstance(bytes);
            if (image.getScaledHeight() > 130) {
                image.scaleToFit(300, 130);
            } else {
                image.scaleToFit(200, 900);
            }
            // double percent = 1.5;
            //
            // int scaledWidth = (int) (image.getWidth() * percent);
            // int scaledHeight = (int) (image.getHeight() * percent);

            // image.scaleToFit(200, 900);
            // image.scaleToFit(scaledWidth, scaledHeight);
            //
            image.setBackgroundColor(BaseColor.WHITE);
            image.setAbsolutePosition(320, 575);// 320,575
            document.add(image);

            List<JobCard> jobCardList = jobCardRepository.findAll();
            List<String> excludedIds = Arrays.asList("fa2dee1f6fdc41d78a38fc668a2f36c9",
                    "360eca6cd6554f2b852bde02009bb946");
            List<JobCard> filteredUsers = jobCardList.stream().filter(user -> !excludedIds.contains(user.getId()))
                    .collect(Collectors.toList());
            // Integer count = jobCardList.stream().mapToInt(jobCard ->
            // jobCard.getJobAttributes().size()).sum();

            Integer count = filteredUsers.stream().mapToInt(jobCard -> jobCard.getJobAttributes().size()).sum();
            // .filter(jobCard ->
            // !jobCard.getId().equals("fa2dee1f6fdc41d78a38fc668a2f36c9") ||
            // jobCard.getId().equals("360eca6cd6554f2b852bde02009bb946")).
            List<JobCardData> jobCardData = ObjectUtils.isEmpty(serviceRecord.getJobCardData()) ? new ArrayList<>()
                    : serviceRecord.getJobCardData();

            Double percentage = 0d;
            if (!ObjectUtils.isEmpty(jobCardData))
                percentage = (100d * countOkStatus(jobCardData)) / count;

            circle.createCircle(writer, 520, 715, 40, 10, percentage.intValue(), new BaseColor(245, 30, 0));
            circle.createCircle(writer, 566, 761, 46, 1, new BaseColor(185, 205, 250));
            circle.createCircle(writer, 554, 749, 34, 1, new BaseColor(185, 205, 250));
            PdfPTable pdfPTable = new PdfPTable(1);

            ColumnText column = new ColumnText(writer.getDirectContent());
            column.setSimpleColumn(-65, -400, 486, 740, 16, Element.ALIGN_LEFT);

            columnTextMethod(pdfPTable);

            PdfPCell Cell1 = new PdfPCell(new Paragraph("Cell 1"));
            Font font = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 7);
            font.setColor(new BaseColor(198, 198, 198));

            Font font1 = FontFactory.getFont("Poppins-semi", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 11);
            // font1.setSize(8.25f);
            // font1.setStyle(Font.BOLD);
            font1.setColor(new BaseColor(255, 255, 255));

            // Font font = new Font(Font.FontFamily.UNDEFINED, 7, Font.NORMAL, new
            // BaseColor(198,198,198));
            RoundedBorder cornerCell = new RoundedBorder(new BaseColor(16, 75, 140), new float[] { 41, -36, -357, 7 });
            Cell1.setBorder(PdfPCell.NO_BORDER);
            Cell1.setCellEvent(cornerCell);
            PdfPTable nestedTables = new PdfPTable(1);
            PdfPCell registrationNumber = new PdfPCell(new Paragraph("Registration Number", font));
            if (!ObjectUtils.isEmpty(vehicle.getPersonalizedNumber())) {
                registrationNumber = new PdfPCell(new Paragraph("Personal Number", font));
            }
            registrationNumber.setBorder(PdfPCell.NO_BORDER);
            nestedTables.addCell(registrationNumber);
            Cell1.addElement(nestedTables);
            PdfPCell cell1Child = new PdfPCell(new Paragraph("Cell 1"));
            cell1Child.setBorder(PdfPCell.NO_BORDER);
            PdfPTable nestedTables1 = new PdfPTable(1);
            Font regFont = new Font(Font.FontFamily.UNDEFINED, 10, Font.BOLD, BaseColor.WHITE);
            PdfPCell registrationValue = new PdfPCell(
                    new Paragraph(ObjectUtils.isEmpty(vehicle.getRegNumber()) ? "NA" : vehicle.getRegNumber(), font1));
            if (!ObjectUtils.isEmpty(vehicle.getPersonalizedNumber())) {
                registrationValue = new PdfPCell(new Paragraph(vehicle.getPersonalizedNumber(), font1));
            }
            registrationValue.setBorder(PdfPCell.NO_BORDER);

            nestedTables.addCell(registrationValue);
            cell1Child.addElement(nestedTables1);
            pdfPTable.addCell(Cell1);
            column.addElement(pdfPTable);
            column.go();

            PdfPTable pdfPTable1 = new PdfPTable(1);
            column.setSimpleColumn(31, -400, 561, 740, 16, Element.ALIGN_LEFT);

            columnTextMethod(pdfPTable1);
            PdfPCell cell2 = new PdfPCell(new Paragraph("Cell 2"));

            RoundedBorder cornerCell1 = new RoundedBorder(new BaseColor(16, 75, 140), new float[] { 38, -36, -260, 7 });
            cell2.setBorder(PdfPCell.NO_BORDER);
            cell2.setCellEvent(cornerCell1);
            PdfPTable nestedTable = new PdfPTable(1); // Create 2 columns in
            PdfPCell chassisNumber = new PdfPCell(new Paragraph("Chassis Number", font));

            chassisNumber.setBorder(PdfPCell.NO_BORDER);
            nestedTable.addCell(chassisNumber);
            cell2.addElement(nestedTable);// add nested table in

            PdfPCell cell2Child = new PdfPCell(new Paragraph("Cell 2"));
            PdfPTable nestedTable112 = new PdfPTable(1);
            PdfPCell chassisValue = new PdfPCell(new Paragraph(vehicle.getChassisNumber(), font1));
            chassisValue.setBorder(PdfPCell.NO_BORDER);
            nestedTable.addCell(chassisValue);
            cell2Child.addElement(nestedTable112);
            pdfPTable1.addCell(cell2);

            column.addElement(pdfPTable1);
            column.go();

            PdfPTable pdfPTable1Tesla = new PdfPTable(1);
            column.setSimpleColumn(-64, -350, 461, 775, 16, Element.ALIGN_LEFT);

            columnTextMethod(pdfPTable1Tesla);
            PdfPCell cellTesla = new PdfPCell(new Paragraph("Cell 2"));

            Font font3 = new Font(Font.FontFamily.UNDEFINED, 17, Font.BOLD, BaseColor.BLACK);

            cellTesla.setBorder(PdfPCell.NO_BORDER);
            PdfPTable nestedTableTesla = new PdfPTable(1); // Create 2 columns in

            String brand1 = vehicle.getBrand();
            if (!ObjectUtils.isEmpty(brand1) && brand1.toLowerCase().contains("tesla")) {
                brand1 = brand1.toLowerCase().replace("motors", "");
            }
            vehicle.setBrand(WordUtils.capitalize(Objects.isNull(brand1) ? null : brand1.toLowerCase()));
            vehicle.setModel(
                    WordUtils.capitalize(Objects.isNull(vehicle.getModel()) ? null : vehicle.getModel().toLowerCase()));

            String brand = ObjectUtils.isEmpty(vehicle.getBrand()) ? "Brand N/A" : vehicle.getBrand();
            String model = ObjectUtils.isEmpty(vehicle.getModel()) ? "Model N/A" : vehicle.getModel();
            Font brandFont = FontFactory.getFont("Poppins-bold", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 18);
            brandFont.setColor(BaseColor.BLACK);
            Font modelFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 18);
            modelFont.setColor(BaseColor.BLACK);
            Phrase phraseBrand = new Phrase(brand, brandFont);
            Phrase phraseModel = new Phrase(model, modelFont);
            Paragraph elements = new Paragraph(phraseBrand);
            elements.add(" ");
            elements.add(phraseModel);
            // PdfPCell teslaBrandModel = new PdfPCell(new Paragraph(brand + " " + model,
            // font3));
            PdfPCell teslaBrandModel = new PdfPCell(new Paragraph(elements));

            teslaBrandModel.setBorder(PdfPCell.NO_BORDER);

            nestedTableTesla.addCell(teslaBrandModel);
            cellTesla.addElement(nestedTableTesla);
            pdfPTable1Tesla.addCell(cellTesla);
            column.addElement(pdfPTable1Tesla);
            column.go();

            imageClass.highVoltageParts(document, writer);
            imageClass.ImageForVoltageParts(writer, serviceRecord);
            co2 = imageClass.getCo2Percentage();
            imageClass.generalParts(document, writer);
            imageClass.ImageForGeneralParts(writer, serviceRecord);
            firstPageTable(document, vehicle, writer, jobCardData);

            document.newPage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ImageProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage getDefaultImage(String model) throws IOException {
        if ("model s".equalsIgnoreCase(model))
            return ImageIO.read(this.getClass().getResource("/image/modelS.png"));
        else if ("model x".equalsIgnoreCase(model))
            return ImageIO.read(this.getClass().getResource("/image/ModelX.png"));
        else if ("model 3".equalsIgnoreCase(model))
            return ImageIO.read(this.getClass().getResource("/image/Model3.png"));
        else if ("model y".equalsIgnoreCase(model))
            return ImageIO.read(this.getClass().getResource("/image/modely.png"));
        else
            return ImageIO.read(this.getClass().getResource("/image/noImage.png"));

    }

    private int countOkStatus(List<JobCardData> jobCardDataList) {
        return jobCardDataList.stream()
                .filter(jobCard -> !jobCard.getJobCardId().equals("fa2dee1f6fdc41d78a38fc668a2f36c9")
                        || jobCard.getJobCardId().equals("360eca6cd6554f2b852bde02009bb946"))
                .mapToInt(jobCardData -> jobCardData.getJobCardDataAttributes().stream()
                        .filter(jobCardDataAttributes -> "ok"
                                .equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "60 km/h".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "80 km/h".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "100 km/h".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "110 km/h".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "0-1 km".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "1-5 km".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "5+ km".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "Dry".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "Wet".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "Snow".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "Icy".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "Slush".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus())
                                || "Others".equalsIgnoreCase(jobCardDataAttributes.getJobClassifier().getStatus()))
                        .toList().size())
                .sum();
    }

    private void standardEstimatedPrice(List<JobCardData> jobCardDataList) {
        standardRepairCost = 0d;
        recommendedRepairCost = 0d;
        boolean standardRepairCostFlag = false;
        boolean recommendedRepairCostFlag = false;
        priceFlag = false;
        for (JobCardData jobCardData : jobCardDataList) {
            for (JobCardDataAttributes jobCardDataAttributes : jobCardData.getJobCardDataAttributes()) {
                String estimatedRepairCostStandard = jobCardDataAttributes.getJobClassifier()
                        .getEstimatedRepairCostStandard();
                Double estimatedRepairCost = !NumberUtils.isNumber(estimatedRepairCostStandard) ? 0d
                        : Double.valueOf(estimatedRepairCostStandard);
                String estimatedRepairCostPlanetFriendly = jobCardDataAttributes.getJobClassifier()
                        .getEstimatedRepairCostPlanetFriendly();
                Double recommendedEstimatedRepairCost = !NumberUtils.isNumber(estimatedRepairCostPlanetFriendly) ? 0d
                        : Double.valueOf(estimatedRepairCostPlanetFriendly);
                if (recommendedEstimatedRepairCost > 0) {
                    recommendedRepairCostFlag = true;
                }
                if (estimatedRepairCost > 0) {
                    standardRepairCostFlag = true;
                }
                if (standardRepairCostFlag && recommendedRepairCostFlag) {
                    priceFlag = true;
                    standardRepairCost = standardRepairCost.intValue() == 0 ? recommendedRepairCost
                            : standardRepairCost;
                    recommendedRepairCost = recommendedRepairCost.intValue() == 0 ? standardRepairCost
                            : recommendedRepairCost;
                }

                // if ((estimatedRepairCost.intValue() == 0 &&
                // recommendedEstimatedRepairCost.intValue() != 0) ||
                // (estimatedRepairCost.intValue() != 0 &&
                // recommendedEstimatedRepairCost.intValue() == 0))
                // priceFlag = true;

                if (priceFlag) {
                    estimatedRepairCost = estimatedRepairCost.intValue() == 0 ? recommendedEstimatedRepairCost
                            : estimatedRepairCost;
                    recommendedEstimatedRepairCost = recommendedEstimatedRepairCost.intValue() == 0
                            ? estimatedRepairCost
                            : recommendedEstimatedRepairCost;
                }
                standardRepairCost = standardRepairCost + estimatedRepairCost;
                recommendedRepairCost = recommendedRepairCost + recommendedEstimatedRepairCost;

            }
        }
    }

    private void firstPageTable(Document document, Vehicle vehicle, PdfWriter writer, List<JobCardData> jobCardDataList)
            throws DocumentException, IOException {
        PdfPTable newTable = new PdfPTable(3);
        ColumnText column = new ColumnText(writer.getDirectContent());
        column.setSimpleColumn(-3, 0, 325, 695, 16, Element.ALIGN_LEFT);
        columnTextMethod(newTable);

        Date firstRegistrationdate = CommonUtils.getDate(vehicle.getFirstRegisteredOn());
        String firstRegistration = ObjectUtils.isEmpty(firstRegistrationdate) ? "N/A"
                : CommonUtils.getSimpleDateFormat(CommonUtils.getDay(firstRegistrationdate))
                        .format(firstRegistrationdate);
        Date firstRegistrationInNorwayDate = CommonUtils.getDate(vehicle.getFirstRegisteredInNorway());
        String firstRegistrationInNorway = ObjectUtils.isEmpty(firstRegistrationInNorwayDate) ? "N/A"
                : CommonUtils.getSimpleDateFormat(CommonUtils.getDay(firstRegistrationInNorwayDate))
                        .format(firstRegistrationInNorwayDate);
        Date nextInsepectionDate = CommonUtils.getDate(vehicle.getNextMandatoryInspection());
        String vehicleInspection = ObjectUtils.isEmpty(nextInsepectionDate) ? "N/A"
                : CommonUtils.getSimpleDateFormat(CommonUtils.getDay(nextInsepectionDate)).format(nextInsepectionDate);
        String odometer = ObjectUtils.isEmpty(vehicle.getKilometer()) ? "N/A" : vehicle.getKilometer() + " km";
        String importerCar = ObjectUtils.isEmpty(vehicle.getImportedUsed()) ? "N/A" : vehicle.getImportedUsed();
        String color = ObjectUtils.isEmpty(vehicle.getColor()) ? "N/A" : vehicle.getColor();
        String range = ObjectUtils.isEmpty(vehicle.getElectricRange()) ? "N/A" : vehicle.getElectricRange() + " km";
        String wheelDrive = ObjectUtils.isEmpty(vehicle.getWheelDrive()) ? "N/A" : vehicle.getWheelDrive();
        AtomicDouble enginePower = new AtomicDouble(0d);
        if (vehicle.getEngineCount() > 0) {
            Map<String, String> enginePowers = vehicle.getEnginePowers();
            if (!ObjectUtils.isEmpty(enginePowers)) {
                enginePowers.forEach((k, v) -> {
                    if (!ObjectUtils.isEmpty(v))
                        enginePower.getAndAdd(Double.valueOf(v));
                });
            }
        }
        Font font = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
        font.setColor(BaseColor.BLACK);
        Double hpValue = Double.parseDouble(DECIMAL_FORMAT.format(Math.floor(enginePower.get() * 1.3596216173)));
        Double kwValue = Double.parseDouble(DECIMAL_FORMAT.format(enginePower.get()));
        addVehicleInformation(newTable, "First Registration", new Paragraph(firstRegistration, font));
        addVehicleInformation(newTable, "Registration in Norway", new Paragraph(firstRegistrationInNorway, font));
        addVehicleInformation(newTable, "Next inspection", new Paragraph(vehicleInspection, font));

        addVehicleInformation(newTable, "Odometer", new Paragraph(odometer, font));
        addVehicleInformation(newTable, "Imported car?", new Paragraph(importerCar, font));
        addVehicleInformation(newTable, "Color", new Paragraph(color, font));

        addVehicleInformation(newTable, "Range", new Paragraph(range, font));
        addVehicleInformation(newTable, "Wheel drive", new Paragraph(wheelDrive, font));
        Phrase hpParagraph = new Phrase("" + hpValue.intValue() + " HP", font);
        Font font1 = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 8);
        font1.setColor(BaseColor.BLACK);
        Phrase kwParagraph = new Phrase(" (" + kwValue.intValue() + " kW)", font1);
        Paragraph horsePowerParagraph = new Paragraph(hpParagraph);
        horsePowerParagraph.add(kwParagraph);
        addVehicleInformation(newTable, "Horsepower", horsePowerParagraph);

        column.addElement(newTable);
        column.go();

        standardEstimatedPrice(jobCardDataList);
        // horizontalLine(writer, 52, 653, 350, BaseColor.LIGHT_GRAY, 0.4F);
        horizontalLine(writer, 31, 650, 285, BaseColor.LIGHT_GRAY, 0.4F);

        horizontalLine(writer, 31, 615, 285, BaseColor.LIGHT_GRAY, 0.4F);
        String informationRecommended = "\nThis option may not be cheap,but\nuses only new parts from authorized \ndealers";
        if (generalParts && highVoltageParts) {
            repairNotNeed(writer);
        } else {
            if (recommendedRepairCost.intValue() == standardRepairCost.intValue() || recommendedRepairCost > 0) {

                RoundBoxRight(writer);
            }
            if (recommendedRepairCost.intValue() == standardRepairCost.intValue() || standardRepairCost > 0) {
                RoundBoxLeft(writer);
            }
        }
        ourScore(writer, document);
        circleShadowInside(writer, document);
        // circleShadowLower(writer,document);
        repairOptions(writer);
    }

    public void repairNotNeed(PdfWriter writer) throws DocumentException, IOException {

        int x2 = 318;
        int pading = -5;
        int repairPadding = 97;
        // int[] imagePosition = {510, -130, 584, 230};
        int[] imagePosition = { 380, -100, 584, 220 };
        float[] border = { 80, -48, -250, 50 };
        int y = 130;

        PdfPTable pdfPTable = new PdfPTable(1);
        ColumnText column = new ColumnText(writer.getDirectContent());
        column.setSimpleColumn(-125, -400, 800, 215, 16, Element.ALIGN_LEFT);
        columnTextMethod(pdfPTable);

        PdfPCell standardCell = new PdfPCell(new Paragraph("Standard cell"));
        Font standardCellFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 20);
        standardCellFont.setColor(new BaseColor(33, 106, 52, 255));
        RoundedBorder cornerCell = new RoundedBorder(new BaseColor(233, 246, 233, 255),
                new float[] { -10, -106, -60, +105 });// -10,-102,-60,102
        standardCell.setBorder(PdfPCell.NO_BORDER);
        PdfPTable nestedTables = new PdfPTable(1);
        PdfPCell registrationNumber = new PdfPCell(new Paragraph("REPAIR NOT NEEDED", standardCellFont));
        registrationNumber.setBorder(PdfPCell.NO_BORDER);
        registrationNumber.setCellEvent(cornerCell);
        registrationNumber.setPaddingLeft(-3);
        nestedTables.addCell(registrationNumber);
        standardCell.addElement(nestedTables);
        pdfPTable.addCell(standardCell);

        Font standardParagraphFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 13);
        standardParagraphFont.setColor(new BaseColor(71, 71, 71));

        Font greatFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, 13, Font.BOLD);
        greatFont.setColor(new BaseColor(71, 71, 71));

        String greatNews = "Great News! ";
        String middleContent = "Your car seems to be in a great condition";
        String lastContent = "and it doesn't require any repairs at the moment.";

        PdfPCell greatCell = new PdfPCell(new Paragraph(greatNews, greatFont));
        greatCell.setBorder(PdfPCell.NO_BORDER);
        greatCell.setPaddingLeft(73);
        pdfPTable.addCell(greatCell);

        PdfPCell middleContentCell = new PdfPCell(new Paragraph(middleContent, standardParagraphFont));
        middleContentCell.setBorder(PdfPCell.NO_BORDER);
        middleContentCell.setPaddingLeft(160);
        middleContentCell.setPaddingTop(-15);
        pdfPTable.addCell(middleContentCell);

        PdfPCell lastContentCell = new PdfPCell(new Paragraph(lastContent, standardParagraphFont));
        lastContentCell.setBorder(PdfPCell.NO_BORDER);
        lastContentCell.setPaddingLeft(73);
        pdfPTable.addCell(lastContentCell);

        column.addElement(pdfPTable);

        column.go();

        imagesForBox(writer, imagePosition[0], imagePosition[1], imagePosition[2], imagePosition[3],
                "/image/notRepairBG1.png", false);
        // imagesForBox(writer, 420, imagePosition[1], imagePosition[2], 200,
        // "/image/notRepairBG2.png",false);

    }

    private static void horizontalLine(PdfWriter writer, int x1, int y, int x2, BaseColor color, float width) {
        PdfContentByte contentByte = writer.getDirectContent();
        contentByte.setLineWidth(width);
        contentByte.setColorStroke(color);
        contentByte.moveTo(x1, y);
        contentByte.lineTo(x2, y);
        contentByte.closePathStroke();
        contentByte.stroke();
    }

    private void addVehicleInformation(PdfPTable newTable, String vehicleField, Paragraph vehicleFieldValue) {
        // Font font = new Font(Font.FontFamily.UNDEFINED, 7, Font.BOLD, new
        // BaseColor(24, 76, 140));
        Font font = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 6);
        font.setColor(new BaseColor(16, 75, 140));

        Font font1 = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
        font1.setColor(BaseColor.BLACK);

        PdfPCell topLevelCell = new PdfPCell();
        topLevelCell.setBorder(Rectangle.NO_BORDER);
        topLevelCell.setPaddingBottom(10);

        PdfPTable nestedTableTop = new PdfPTable(1);
        nestedTableTop.setWidthPercentage(100);
        PdfPCell nestedCellTop = new PdfPCell(new Paragraph(vehicleField, font));
        nestedCellTop.setBorder(Rectangle.LEFT);
        nestedCellTop.setPaddingLeft(5);
        nestedCellTop.setBorderWidth(1);
        nestedCellTop.setBorderColor(BaseColor.LIGHT_GRAY);
        nestedTableTop.addCell(nestedCellTop);
        // Font font1 = new Font(Font.FontFamily.UNDEFINED, 9, Font.BOLD,
        // BaseColor.BLACK);
        PdfPCell nestedTableCellTop = new PdfPCell(vehicleFieldValue);
        nestedTableCellTop.setBorder(Rectangle.LEFT);
        nestedTableCellTop.setBorderWidth(1);
        nestedTableCellTop.setPaddingLeft(5);
        nestedTableCellTop.setBorderColor(BaseColor.LIGHT_GRAY);
        nestedTableTop.addCell(nestedTableCellTop);
        topLevelCell.addElement(nestedTableTop);
        newTable.addCell(topLevelCell);
    }

    public void columnTextMethod(PdfPTable tableName) {

        PdfPCell blankCell = new PdfPCell();
        blankCell.setColspan(10);
        blankCell.setPaddingTop(-5);
        blankCell.addElement(Chunk.NEWLINE);
        blankCell.setBorder(PdfPCell.NO_BORDER);
        blankCell.setBorderColor(BaseColor.WHITE);
        tableName.addCell(blankCell);
    }

    private void RoundBoxLeft(PdfWriter writer) throws DocumentException, IOException {

        int x2 = 210;
        int y = 130;
        int x1 = 35;
        int pading = -5;
        int repairPadding = 97;
        int[] imagePosition = { 210, 40, 295, 225 };
        float[] border = { 80, -48, -250, 50 };
        String information = "\nThis option may not be cheap,but\nuses only new parts from authorized \ndealers";
        PdfPTable pdfPTable = new PdfPTable(1);
        ColumnText column = new ColumnText(writer.getDirectContent());
        column.setSimpleColumn(-60, -400, 450, 215, 16, Element.ALIGN_LEFT);// 210
        boolean standardOnly = false;
        if (recommendedRepairCost.intValue() == 0 && standardRepairCost > 0) {
            information = "\nThis option may not be cheap,but uses only new parts from authorized dealers";
            column.setSimpleColumn(-130, -400, 810, 215, 16, Element.ALIGN_LEFT);// 210
            x2 = 553;
            x1 = 42;
            imagePosition = new int[] { 510, 40, 463, 230 };
            // imagePosition = new int[]{510, 40, 570, 225};
            pading = 10;
            repairPadding = 275;
            border = new float[] { 220, -48, -520, 50 };
            y = 136;
            standardOnly = true;
        }
        columnTextMethod(pdfPTable);

        PdfPCell standardCell = new PdfPCell(new Paragraph("Standard cell"));
        Font standardCellFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 20);
        standardCellFont.setColor(new BaseColor(16, 75, 140));

        RoundedBorder cornerCell = new RoundedBorder(new BaseColor(223, 236, 251),
                new float[] { -10, -106, -60, +105 });// -10,-102,-60,102
        standardCell.setBorder(PdfPCell.NO_BORDER);
        PdfPTable nestedTables = new PdfPTable(1);
        PdfPCell registrationNumber = new PdfPCell(new Paragraph("STANDARD", standardCellFont));
        registrationNumber.setBorder(PdfPCell.NO_BORDER);
        registrationNumber.setCellEvent(cornerCell);
        nestedTables.addCell(registrationNumber);
        standardCell.addElement(nestedTables);

        PdfPCell standardParagraphCell = new PdfPCell(new Paragraph("Standard paragraph cell"));
        standardParagraphCell.setBorder(PdfPCell.NO_BORDER);
        PdfPTable nestedTables1 = new PdfPTable(1);
        Font standardParagraphFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        standardParagraphFont.setColor(BaseColor.BLACK);

        Font priceFont = FontFactory.getFont("Poppins-bold", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 20);
        priceFont.setColor(new BaseColor(16, 75, 140));
        String priceValue = NumberFormat.getIntegerInstance().format(standardRepairCost.intValue()).replace(",", " ");
        Phrase price = new Phrase(priceValue, priceFont);
        Phrase similar = new Phrase("≈ ", standardCellFont);
        Phrase priceUnit = new Phrase(" NOK", standardCellFont);
        Phrase pricePhrase = new Phrase(similar);
        pricePhrase.add(price);
        pricePhrase.add(priceUnit);

        PdfPCell registrationValue = new PdfPCell(new Paragraph(information, standardParagraphFont));
        registrationValue.setBorder(PdfPCell.NO_BORDER);
        nestedTables.addCell(registrationValue);
        standardParagraphCell.addElement(nestedTables1);
        PdfPCell NokCell = new PdfPCell(new Paragraph("nok cell", standardCellFont));
        RoundedBorder nokCornerCell = new RoundedBorder(new BaseColor(223, 236, 251, 255),
                new float[] { -10, -42, -60, +85 });// 80
        NokCell.setBorder(PdfPCell.NO_BORDER);
        NokCell.setPaddingTop(pading);
        PdfPTable nestedNokTables = new PdfPTable(1);
        // PdfPCell NokValue = new PdfPCell(new Paragraph(" " +
        // standardRepairCost.intValue() + " NOK", standardCellFont));
        PdfPCell NokValue = new PdfPCell(pricePhrase);
        NokValue.setBorder(PdfPCell.NO_BORDER);
        NokValue.setPaddingTop(7);
        NokValue.setCellEvent(nokCornerCell);
        nestedNokTables.addCell(NokValue);
        NokCell.addElement(nestedNokTables);

        pdfPTable.addCell(standardCell);
        pdfPTable.addCell(NokCell);
        column.addElement(pdfPTable);
        column.go();
        horizontalLine(writer, x1, y, x2, new BaseColor(16, 75, 140, 255), 1F);
        imagesForBox(writer, imagePosition[0], imagePosition[1], imagePosition[2], imagePosition[3],
                "/image/standardCar.png", standardOnly);
        if (!((recommendedRepairCost.intValue() > 0 && standardRepairCost.intValue() == 0)
                || (recommendedRepairCost.intValue() == 0 && standardRepairCost.intValue() > 0))) {
            Map<String, Object> repairingOption = checkReparingPtion(standardRepairCost, true);
            repairingOption(column, (BaseColor) repairingOption.get("color"), border,
                    (String) repairingOption.get("option"), repairPadding);
        }
    }

    private void RoundBoxRight(PdfWriter writer) throws DocumentException, IOException {
        int x2 = 318;
        int pading = -5;
        int repairPadding = 97;
        // int[] imagePosition = {510, -130, 584, 230};
        int[] imagePosition = { 450, -100, 584, 238 };
        float[] border = { 80, -48, -250, 50 };
        int y = 130;
        String information = "\nThis option is budget friendly,\ntake renewed parts into account,\nand leaves less footprint on our planet.";
        PdfPTable pdfPTable = new PdfPTable(1);
        ColumnText column = new ColumnText(writer.getDirectContent());
        column.setSimpleColumn(222, -400, 732, 215, 16, Element.ALIGN_LEFT);
        if (standardRepairCost.intValue() == 0 && recommendedRepairCost > 0) {
            information = "\nThis option is budget friendly, take renewed parts into account, and leaves less footprint on our planet.";
            column.setSimpleColumn(-130, -400, 810, 215, 16, Element.ALIGN_LEFT);// 210
            x2 = 45;
            imagePosition = new int[] { 450, 43, 585, 238 };
            pading = 10;
            repairPadding = 275;
            border = new float[] { 220, -48, -520, 50 };
            y = 136;

        }
        columnTextMethod(pdfPTable);

        PdfPCell standardCell = new PdfPCell(new Paragraph("Standard cell"));
        Font standardCellFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 20);
        standardCellFont.setColor(new BaseColor(33, 106, 52, 255));
        Font priceFont = FontFactory.getFont("Poppins-bold", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 20);
        priceFont.setColor(new BaseColor(33, 106, 52, 255));
        String priceValue = NumberFormat.getIntegerInstance().format(recommendedRepairCost.intValue()).replace(",",
                " ");
        Phrase price = new Phrase(priceValue, priceFont);
        Phrase priceUnit = new Phrase(" NOK", standardCellFont);
        Phrase similar = new Phrase("≈ ", standardCellFont);
        Phrase pricePhrase = new Phrase(similar);
        pricePhrase.add(price);
        pricePhrase.add(priceUnit);
        RoundedBorder cornerCell = new RoundedBorder(new BaseColor(233, 246, 233, 255),
                new float[] { -10, -106, -60, +105 });// -10,-102,-60,102
        standardCell.setBorder(PdfPCell.NO_BORDER);
        PdfPTable nestedTables = new PdfPTable(1);
        PdfPCell registrationNumber = new PdfPCell(new Paragraph("PLANET FRIENDLY", standardCellFont));
        registrationNumber.setBorder(PdfPCell.NO_BORDER);
        registrationNumber.setCellEvent(cornerCell);
        nestedTables.addCell(registrationNumber);
        standardCell.addElement(nestedTables);

        PdfPCell standardParagraphCell = new PdfPCell(new Paragraph("Standard paragraph cell"));
        standardParagraphCell.setBorder(PdfPCell.NO_BORDER);
        PdfPTable nestedTables1 = new PdfPTable(1);
        Font standardParagraphFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        standardParagraphFont.setColor(new BaseColor(71, 71, 71));
        PdfPCell registrationValue = new PdfPCell(new Paragraph(information, standardParagraphFont));
        registrationValue.setBorder(PdfPCell.NO_BORDER);
        nestedTables.addCell(registrationValue);
        standardParagraphCell.addElement(nestedTables1);
        PdfPCell NokCell = new PdfPCell(new Paragraph("nok cell", standardCellFont));
        RoundedBorder nokCornerCell = new RoundedBorder(new BaseColor(233, 246, 233, 255),
                new float[] { -10, -42, -60, +85 });// 80
        NokCell.setBorder(PdfPCell.NO_BORDER);
        NokCell.setPaddingTop(pading);
        PdfPTable nestedNokTables = new PdfPTable(1);
        PdfPCell NokValue = new PdfPCell(pricePhrase);
        NokValue.setBorder(PdfPCell.NO_BORDER);
        NokValue.setPaddingTop(7);
        NokValue.setCellEvent(nokCornerCell);
        nestedNokTables.addCell(NokValue);
        NokCell.addElement(nestedNokTables);

        pdfPTable.addCell(standardCell);
        pdfPTable.addCell(NokCell);
        column.addElement(pdfPTable);
        column.go();
        horizontalLine(writer, 525, y, x2, new BaseColor(33, 106, 52, 255), 1F);
        if (co2 != 0)
            imagesForBox(writer, imagePosition[0], imagePosition[1], imagePosition[2], imagePosition[3],
                    "/image/planetFriendly.png", false);

        if (!((recommendedRepairCost.intValue() > 0 && standardRepairCost.intValue() == 0)
                || (recommendedRepairCost.intValue() == 0 && standardRepairCost.intValue() > 0))) {
            Map<String, Object> repairingOption = checkReparingPtion(recommendedRepairCost, false);
            repairingOption(column, (BaseColor) repairingOption.get("color"), border,
                    (String) repairingOption.get("option"), repairPadding);
        }
    }

    private Map<String, Object> checkReparingPtion(Double price, boolean isStandard) {
        long vehiclePrice = 300000l;
        Map<String, Object> repairingOption = new HashMap<>();
        double percentage = (price / vehiclePrice) * 100;
        if (percentage > 50) {
            repairingOption.put("color", new BaseColor(255, 0, 0, 255));
            repairingOption.put("option", "NOT WORTH REPAIRING");
            return repairingOption;
        }
        if (isStandard) {
            if ((standardRepairCost.intValue() > recommendedRepairCost.intValue()
                    || recommendedRepairCost.intValue() == standardRepairCost.intValue())
                    && recommendedRepairCost.intValue() != 0) {
                repairingOption.put("color", new BaseColor(255, 0, 0, 255));
                repairingOption.put("option", "NOT WORTH REPAIRING");
            } else if (standardRepairCost.intValue() == 0) {
                repairingOption.put("color", new BaseColor(255, 0, 0, 255));
                repairingOption.put("option", "NOT WORTH REPAIRING");
            } else {
                repairingOption.put("color", new BaseColor(32, 100, 44));
                repairingOption.put("option", "RECOMMENDED OPTION");
            }
        } else {
            if ((standardRepairCost.intValue() < recommendedRepairCost.intValue())
                    && standardRepairCost.intValue() > 0) {
                repairingOption.put("color", new BaseColor(255, 0, 0, 255));
                repairingOption.put("option", "NOT WORTH REPAIRING");
            } else {
                repairingOption.put("color", new BaseColor(32, 100, 44));
                repairingOption.put("option", "RECOMMENDED OPTION");
            }
        }

        return repairingOption;

    }

    public void repairingOption(ColumnText columnText, BaseColor baseColor, float[] border, String name,
            Integer padding) throws DocumentException {
        PdfPTable pdfPTable = new PdfPTable(1);
        columnTextMethod(pdfPTable);
        Font font1 = new Font(Font.FontFamily.UNDEFINED, 10, Font.BOLD, baseColor);
        RoundedBorder recommendedCell = new RoundedBorder(BaseColor.WHITE, border);
        PdfPCell repairOption = new PdfPCell(new Paragraph(name, font1));
        repairOption.setBorder(PdfPCell.NO_BORDER);
        repairOption.setCellEvent(recommendedCell);
        repairOption.setPaddingLeft(padding);
        repairOption.setPaddingTop(-5);
        repairOption.setPaddingRight(-15);
        pdfPTable.addCell(repairOption);
        columnText.addElement(pdfPTable);
        columnText.go();
    }

    public void repairOptions(PdfWriter writer) throws DocumentException {
        PdfPTable repairOptionTable = new PdfPTable(1);
        ColumnText column = new ColumnText(writer.getDirectContent());
        column.setSimpleColumn(-28, -394, 510, 265, 16, Element.ALIGN_LEFT);
        columnTextMethod(repairOptionTable);

        // Font font1 = new Font(Font.FontFamily.UNDEFINED, 15, Font.BOLD,
        // BaseColor.BLACK);
        Font font1 = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 16);
        font1.setColor(BaseColor.BLACK);
        PdfPCell repairOptionCell = new PdfPCell(new Paragraph("Repair options", font1));
        repairOptionCell.setBorder(PdfPCell.NO_BORDER);
        repairOptionCell.setPaddingLeft(10);
        repairOptionTable.addCell(repairOptionCell);
        column.addElement(repairOptionTable);
        column.go();
    }

    public void imagesForBox(PdfWriter writer, float leftx, float lefty, float upperx, float uppery, String imagePath,
            boolean standandardOnly) throws DocumentException, IOException {
        PdfPTable imageFoStandard = new PdfPTable(1);
        ColumnText column = new ColumnText(writer.getDirectContent());
        column.setSimpleColumn(leftx, lefty, upperx, uppery, 16, Element.ALIGN_LEFT);
        columnTextMethod(imageFoStandard);

        PdfPCell imageStandardCell = new PdfPCell();
        Image image = Image.getInstance(this.getClass().getResource(imagePath));
        if (standandardOnly)
            image.scaleToFit(90, 100);//
        imageStandardCell.setBorder(PdfPCell.NO_BORDER);
        imageStandardCell.addElement(image);
        imageFoStandard.addCell(imageStandardCell);
        column.addElement(imageFoStandard);
        column.go();
        if (imagePath.equals("/image/planetFriendly.png")) {

            PdfTemplate template = writer.getDirectContent().createTemplate(100, 100);
            // template.setMatrix(-0.866f, 0.5f, -0.5f, 0.866f, 0, 0);
            template.setMatrix(0.866f, -0.85f, 0.9f, 0.866f, 0, 100);

            // add text to the template
            template.beginText();
            BaseFont customFont = BaseFont.createFont("/tmp/PoppinsMedium500.ttf", BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED);
            template.setFontAndSize(customFont, 7);
            // template.showText(StringUtils.reverse("Hello, World!"));

            template.showText(co2 + "% CO2 reduction");

            template.endText();
            PdfContentByte directContent = writer.getDirectContent();
            directContent.setRGBColorFill(255, 255, 255);
            directContent.addTemplate(template, 492, 100);
        }
    }

    public void ourScore(PdfWriter writer, Document document) throws DocumentException, IOException {
        Image image = Image.getInstance(this.getClass().getResource("/image/ourScoreFigma.png"));
        image.setAbsolutePosition(292, 724);
        image.setBackgroundColor(new BaseColor(238, 238, 238, 255));
        image.scaleAbsolute(200, 28);
        document.add(image);

        PdfPTable ourScoreTable = new PdfPTable(1);
        ColumnText column = new ColumnText(writer.getDirectContent());
        column.setSimpleColumn(390, -400, 510, 770, 16, Element.ALIGN_LEFT);
        columnTextMethod(ourScoreTable);
        column.addElement(ourScoreTable);
        column.go();
    }

    public void circleShadowInside(PdfWriter writer, Document document) throws DocumentException, IOException {
        Image image = Image.getInstance(this.getClass().getResource("/image/shadowCrop.png"));
        image.setAbsolutePosition(492, 709);
        // image.setBackgroundColor(new BaseColor(238, 238, 238, 255));
        image.scaleAbsolute(70, 40);
        document.add(image);

        PdfPTable circleShadow = new PdfPTable(1);
        ColumnText column = new ColumnText(writer.getDirectContent());
        column.setSimpleColumn(390, -400, 510, 770, 16, Element.ALIGN_LEFT);
        columnTextMethod(circleShadow);
        column.addElement(circleShadow);
        column.go();
    }
    // public void circleShadowLower(PdfWriter writer, Document document) throws
    // DocumentException, IOException {
    // Image image =
    // Image.getInstance(this.getClass().getResource("/image/lowerShadowCrops.png"));
    // image.setAbsolutePosition(475, 664);
    // // image.setBackgroundColor(new BaseColor(238, 238, 238, 255));
    // image.scaleAbsolute(60,40);
    // document.add(image);
    //
    // PdfPTable circleOutside = new PdfPTable(1);
    // ColumnText column = new ColumnText(writer.getDirectContent());
    // column.setSimpleColumn(390, -400, 510, 770, 16, Element.ALIGN_LEFT);
    // columnTextMethod(circleOutside);
    // column.addElement(circleOutside);
    // column.go();
    // }

    private void registerAllFont() {
        FontFactory.register("/tmp/PoppinsMedium500.ttf", "Poppins");
        FontFactory.register("/tmp/PoppinsRegular400.ttf", "Poppins-400");
        FontFactory.register("/tmp/PoppinsLight300.ttf", "Poppins-300");
        FontFactory.register("/tmp/PoppinsSemiBold600.ttf", "Poppins-semi");
        FontFactory.register("/tmp/PoppinsBold700.ttf", "Poppins-bold");
    }

    private void copyFontToTmp() {
        try {
            byte[] medium500 = this.getClass().getResource("/font/PoppinsMedium500.ttf").openStream().readAllBytes();
            File mediumFile = new File("/tmp/PoppinsMedium500.ttf");
            OutputStream outputStream = new FileOutputStream(mediumFile);
            outputStream.write(medium500);
            outputStream.close();

            byte[] regular400 = this.getClass().getResource("/font/PoppinsRegular400.ttf").openStream().readAllBytes();
            File regularFile = new File("/tmp/PoppinsRegular400.ttf");
            outputStream = new FileOutputStream(regularFile);
            outputStream.write(regular400);
            outputStream.close();

            byte[] light300 = this.getClass().getResource("/font/PoppinsLight300.ttf").openStream().readAllBytes();
            File lightFile = new File("/tmp/PoppinsLight300.ttf");
            outputStream = new FileOutputStream(lightFile);
            outputStream.write(light300);
            outputStream.close();

            byte[] medium600 = this.getClass().getResource("/font/PoppinsSemiBold600.ttf").openStream().readAllBytes();
            File mediumBoldFile = new File("/tmp/PoppinsSemiBold600.ttf");
            outputStream = new FileOutputStream(mediumBoldFile);
            outputStream.write(medium600);
            outputStream.close();

            byte[] bold700 = this.getClass().getResource("/font/PoppinsBold700.ttf").openStream().readAllBytes();
            File boldFile = new File("/tmp/PoppinsBold700.ttf");
            outputStream = new FileOutputStream(boldFile);
            outputStream.write(bold700);
            outputStream.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkCo2Emmision(ServiceRecord serviceRecord, ServiceRecord previousServiceRecord,
            List<JobCard> jobCardList) {
        List<String> list = jobCardList.stream()
                .filter(jobCard -> jobCard.getJobCardKey().equalsIgnoreCase("electricalComponents")
                        || jobCard.getJobCardKey().equalsIgnoreCase("driveUnit"))
                .map(JobCard::getId).toList();
        List<JobCardData> jobCardDataList = new ArrayList<>();
        List<JobCardData> jobCardDataListPrevious = new ArrayList<>();
        if (!ObjectUtils.isEmpty(serviceRecord.getJobCardData()))
            jobCardDataList.addAll(serviceRecord.getJobCardData().stream()
                    .filter(jobCardData -> list.contains(jobCardData.getJobCardId())).toList());
        if (!ObjectUtils.isEmpty(previousServiceRecord) && !ObjectUtils.isEmpty(previousServiceRecord.getJobCardData()))
            jobCardDataListPrevious.addAll(previousServiceRecord.getJobCardData().stream()
                    .filter(jobCardData -> list.contains(jobCardData.getJobCardId())).toList());

    }

    private int getCo2Emmision(List<JobCardData> jobCardDataList) {
        AtomicInteger count = new AtomicInteger(0);
        for (JobCardData jobCardData : jobCardDataList) {
            jobCardData.getJobCardDataAttributes().forEach(jobAttributes -> {
                if (jobAttributes.getJobClassifier().getStatus().equals("Fail")) {
                    count.incrementAndGet();
                }
            });

        }
        return count.get() * 73;
    }
    ////////////////////////////////////////////////

    private void addCellToTableSecond(PdfPTable table, JobClassifier jobClassifier, int k) {
        PdfPTable table1 = new PdfPTable(10);
        PdfPCell pdfPCell = new PdfPCell();
        table1.setWidthPercentage(99);
        pdfPCell.setBorder(PdfPCell.NO_BORDER);
        // pdfPCell.setBorder(PdfPCell.NO_BORDER);
        Font classifierFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 7);
        classifierFont.setColor(new BaseColor(136, 136, 136));
        // Font classifierFont = new Font(Font.FontFamily.UNDEFINED, 8.0f, Font.BOLD,
        // new BaseColor(143, 142, 142, 255));

        // Font valueFont = new Font(Font.FontFamily.UNDEFINED, 8.0f, Font.BOLD,
        // BaseColor.BLACK);
        Font valueFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        valueFont.setColor(BaseColor.BLACK);
        BaseColor backgroundColor = selectClassifierBackGroundColor(jobClassifier.getStatus());
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> classifierKeyValue = objectMapper.convertValue(jobClassifier, Map.class);
        classifierKeyValue.remove("status");
        classifierKeyValue.remove("errorAndDeficiency");
        classifierKeyValue.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("unit") && !ObjectUtils.isEmpty(value)) {
                PdfPCell classifierName = new PdfPCell();
                classifierName.setBackgroundColor(backgroundColor);
                classifierName.setBorderColor(backgroundColor);
                String attributeName = WordUtils
                        .capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(key), ' '));
                Phrase element = new Phrase(attributeName, classifierFont);
                classifierName.addElement(element);
                classifierName.setHorizontalAlignment(Element.ALIGN_RIGHT);
                classifierName.setPaddingBottom(-2);
                classifierName.setColspan(10);
                classifierName.setPaddingLeft(10);
                table1.addCell(classifierName);

                PdfPCell classifierValue = new PdfPCell();
                classifierValue.setBackgroundColor(backgroundColor);
                classifierValue.setBorderColor(backgroundColor);
                if (key.equalsIgnoreCase("image")) {
                    List<String> images = (List<String>) value;
                    int size = images.size();
                    int i = k;
                    while (i < size) {
                        if (i <= i + 1) {

                            PdfPTable pdfPTable = new PdfPTable(2);
                            pdfPTable.setWidthPercentage(100);
                            // imageArray =
                            // getByteArrayFromImageS3Bucket(vehicle.getImage().substring(vehicle.getImage().indexOf("com/")
                            // + 4));
                            byte[] data;
                            // byte[] data = Base64.getDecoder().decode(images.get(i).split("base64,")[1]);
                            try {
                                data = getByteArrayFromImageS3Bucket(images.get(i).split("com/")[1]);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (ImageProcessingException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                if (i == size - 1) {
                                    PdfPCell imageCell = new PdfPCell();
                                    imageCell.setColspan(2);
                                    imageCell.setBorder(PdfPCell.NO_BORDER);
                                    imageCell.setBackgroundColor(backgroundColor);
                                    imageCell.setBorderColor(backgroundColor);
                                    imageCell.addElement(Image.getInstance(data));
                                    pdfPTable.addCell(imageCell);
                                    i++;

                                } else {
                                    PdfPCell imageCell = new PdfPCell();
                                    imageCell.setBorder(PdfPCell.NO_BORDER);
                                    imageCell.setBackgroundColor(backgroundColor);
                                    imageCell.setBorderColor(backgroundColor);
                                    imageCell.addElement(Image.getInstance(data));
                                    i++;
                                    // byte[] data2 = Base64.getDecoder().decode(images.get(i).split("base64,")[1]);
                                    byte[] data2 = getByteArrayFromImageS3Bucket(images.get(i).split("com/")[1]);

                                    PdfPCell imageCell1 = new PdfPCell();
                                    imageCell1.setBorder(PdfPCell.NO_BORDER);
                                    imageCell1.setBackgroundColor(backgroundColor);
                                    imageCell1.setBorderColor(backgroundColor);
                                    imageCell1.addElement(Image.getInstance(data2));
                                    pdfPTable.addCell(imageCell);
                                    pdfPTable.addCell(imageCell1);
                                    //
                                    i++;
                                }
                                classifierValue.addElement(pdfPTable);
                            } catch (BadElementException e) {
                                throw new RuntimeException(e);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (ImageProcessingException e) {
                                throw new RuntimeException(e);
                            }

                        }
                        break;
                    }
                } else {
                    Phrase element1 = new Phrase((String) value, valueFont);
                    if (key.equalsIgnoreCase("measuredValue")) {
                        Object unit = classifierKeyValue.get("unit");
                        element1.add(new Phrase((Objects.nonNull(unit) ? " " + unit : ""), classifierFont));
                    }
                    classifierValue.addElement(element1);
                }
                classifierValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
                classifierValue.setPaddingBottom(10);
                classifierValue.setTop(0);
                classifierValue.setPaddingLeft(10);
                classifierValue.setPaddingRight(10);
                classifierValue.setColspan(10);
                table1.addCell(classifierValue);
            }
        });
        pdfPCell.setPaddingTop(-2);
        pdfPCell.addElement(table1);
        pdfPCell.setColspan(10);
        table.addCell(pdfPCell);
    }

    private void addImageToClassifier(PdfPTable table, List<String> images, BaseColor backgroundColor, PdfPCell cell,
            boolean first) {
        int size = images.size();
        int i = 0;
        table.setPaddingTop(-2);
        Font classifierFont = FontFactory.getFont("Poppins", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 7);
        classifierFont.setColor(new BaseColor(136, 136, 136));
        PdfPCell classifierName = new PdfPCell();
        classifierName.setBackgroundColor(backgroundColor);
        classifierName.setBorderColor(backgroundColor);
        Phrase element = new Phrase("Image", classifierFont);
        classifierName.addElement(element);
        classifierName.setHorizontalAlignment(Element.ALIGN_RIGHT);
        classifierName.setColspan(10);
        classifierName.setPaddingLeft(10);
        while (i < size) {
            PdfPTable pdfPTable = new PdfPTable(10);
            pdfPTable.setWidthPercentage(100);
            // byte[] data = Base64.getDecoder().decode(images.get(i).split("base64,")[1]);
            try {
                byte[] data = getByteArrayFromImageS3Bucket(images.get(i).split("com/")[1]);
                if (i == size - 1) {
                    PdfPCell imageCell = new PdfPCell();
                    imageCell.setColspan(10);
                    imageCell.setBorder(PdfPCell.NO_BORDER);
                    imageCell.setBackgroundColor(backgroundColor);
                    imageCell.setBorderColor(backgroundColor);
                    imageCell.setPaddingLeft(5);
                    imageCell.setPaddingRight(5);
                    imageCell.setBorder(Rectangle.TOP);
                    Image image = Image.getInstance(data);
                    image.setAlignment(Element.ALIGN_CENTER);
                    image.setBackgroundColor(backgroundColor);
                    // image.setBottom(50);
                    // image.scaleToFit(220,150);
                    imageCell.addElement(image);
                    if (i == 0) {
                        PdfPCell pdfPCell = new PdfPCell();
                        // pdfPCell.setBackgroundColor(backgroundColor);
                        pdfPCell.setBorder(Rectangle.NO_BORDER);
                        pdfPCell.setPaddingTop(-2);
                        pdfPCell.setColspan(10);
                        pdfPTable.setWidthPercentage(101.5f);
                        pdfPTable.addCell(classifierName);
                        pdfPTable.addCell(imageCell);
                        if (first) {
                            PdfPTable firstTable = new PdfPTable(10);
                            firstTable.setWidthPercentage(101.5f);
                            firstTable.addCell(cell);
                            PdfPCell firstCell = new PdfPCell();
                            firstCell.setBorder(Rectangle.NO_BORDER);
                            firstCell.setColspan(10);
                            firstCell.setPaddingTop(-2);
                            firstCell.addElement(pdfPTable);
                            firstTable.addCell(firstCell);
                            pdfPCell.addElement(firstTable);
                        } else {
                            pdfPCell.addElement(pdfPTable);
                        }
                        table.addCell(pdfPCell);
                    } else
                        table.addCell(imageCell);
                    i++;
                } else {
                    PdfPCell imageCell = new PdfPCell();
                    imageCell.setColspan(5);
                    imageCell.setBorder(PdfPCell.NO_BORDER);
                    imageCell.setBackgroundColor(backgroundColor);
                    imageCell.setBorderColor(backgroundColor);
                    imageCell.addElement(Image.getInstance(data));
                    imageCell.setPaddingLeft(5);
                    imageCell.setPaddingRight(5);
                    int firstImage = i;
                    i++;
                    // byte[] data2 = Base64.getDecoder().decode(images.get(i).split("base64,")[1]);
                    byte[] data2 = getByteArrayFromImageS3Bucket(images.get(i).split("com/")[1]);
                    PdfPCell imageCell1 = new PdfPCell();
                    imageCell1.setColspan(5);
                    imageCell1.setBorder(PdfPCell.NO_BORDER);
                    imageCell1.setBackgroundColor(backgroundColor);
                    imageCell1.setBorderColor(backgroundColor);
                    imageCell1.addElement(Image.getInstance(data2));
                    imageCell1.setPaddingLeft(5);
                    imageCell1.setPaddingRight(5);
                    if (firstImage == 0) {
                        PdfPCell pdfPCell = new PdfPCell();
                        // pdfPCell.setBackgroundColor(backgroundColor);
                        pdfPCell.setBorder(Rectangle.NO_BORDER);
                        pdfPCell.setPaddingTop(-2);
                        pdfPCell.setColspan(10);
                        pdfPTable.setWidthPercentage(100);
                        pdfPTable.addCell(classifierName);
                        pdfPTable.addCell(imageCell);
                        pdfPTable.addCell(imageCell1);
                        if (first) {
                            PdfPTable firstTable = new PdfPTable(10);
                            firstTable.setWidthPercentage(103f);
                            firstTable.addCell(cell);
                            PdfPCell firstCell = new PdfPCell();
                            firstCell.setBorder(Rectangle.NO_BORDER);
                            firstCell.setColspan(10);
                            firstCell.setPaddingTop(-2);
                            firstCell.addElement(pdfPTable);
                            firstTable.addCell(firstCell);
                            pdfPCell.setPaddingBottom(-2);
                            pdfPCell.addElement(firstTable);
                        } else {
                            pdfPCell.addElement(pdfPTable);
                        }
                        table.addCell(pdfPCell);
                    } else {
                        table.addCell(imageCell);
                        table.addCell(imageCell1);
                    }
                    i++;
                }
            } catch (BadElementException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ImageProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
