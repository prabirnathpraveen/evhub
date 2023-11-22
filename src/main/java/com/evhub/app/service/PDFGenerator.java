package com.evhub.app.service;

import com.amazonaws.util.IOUtils;
import com.evhub.app.entities.*;
import com.evhub.app.repository.*;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PDFGenerator implements com.evhub.app.constant.PDFConstant {
    private static final String INTERMEDIATE_FILE_NAME = "/tmp/firstPdf.pdf";
    private static final String MAIN_PDF_FILE_NAME = "/tmp/conditional_report.pdf";
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private FormsRepository formsRepository;
    @Autowired
    private HtmlFileRepo htmlFileRepo;
    @Autowired
    private JobCardRepository jobCardRepository;

    @Autowired
    private ServiceRecordRepository serviceRecordRepository;
    private byte[] readFileDataAsByteArray(String filePath) throws Exception {
        return IOUtils.toByteArray(this.getClass().getResource(filePath).openStream());
    }


    private String createHTMLReplaceableAttribute(String classifier) {
        return "_"+ classifier + "_";
    }

    private LinkedHashMap<String, Object> getAllReplaceableAttributesForAForm(Set<String> attributes, Set<String> classifiers, String form) {
//        HashMap<String, Object> masterAttrs = new LinkedHashMap<>();
        LinkedHashMap<String, Object> attrForForm = new LinkedHashMap<>();
        classifiers.forEach(classifier -> {
//            attributes.forEach(attr -> {
            attrForForm.put(createHTMLReplaceableAttribute( classifier), null);
//            });
        });
        return attrForForm;
    }

    private LinkedHashMap<String, Object> populateAttributesValueFromDB(LinkedHashMap<String, Object> attrMaster, JobClassifier jobClassifier ) {

        for (Map.Entry<String, Object> att : attrMaster.entrySet()) {
            if(att.getKey().contains("result")){
                attrMaster.put(att.getKey(), (jobClassifier.getResult().length()!=0)?jobClassifier.getResult():"NA");
//            } else if(att.getKey().contains("error")){
//                attrMaster.put(att.getKey(), (jobClassifier.getErrorAndDeficiency().length()!=0)?jobClassifier.getErrorAndDeficiency():"NA");
            } else if (att.getKey().contains("status")){
                attrMaster.put(att.getKey(),(jobClassifier.getStatus().length()!=0)?jobClassifier.getStatus():"NA");
            }
            else if (att.getKey().contains("description")){
                attrMaster.put(att.getKey(),(jobClassifier.getDescription().length()!=0)?jobClassifier.getDescription():"NA");
            } else if (att.getKey().contains("measuredValue")){
                attrMaster.put(att.getKey(),(jobClassifier.getMeasuredValue().length()!=0)?jobClassifier.getMeasuredValue():"NA");
            }
        }

        return attrMaster;
    }

    private String replaceAttributesPlaceholderInReportByteArray(HashMap<String, Object> attrMaster, byte[] byteArr,String formName) {
        String reportString = new String(byteArr);
        for (Map.Entry<String, Object> attr : attrMaster.entrySet()) {
            if(Objects.nonNull(attr.getValue()))
                reportString = reportString.replaceAll(attr.getKey(), (String) attr.getValue());
            else
                reportString = reportString.replaceAll(attr.getKey(), "NA");

        }
        return reportString;
    }

    private String replaceAttributesPlaceholderInReportByteArray(HashMap<String, Object> attrMaster, byte[] byteArr) {
        String reportString = new String(byteArr);
        for (Map.Entry<String, Object> attr : attrMaster.entrySet()) {
            if(Objects.nonNull(attr.getValue()))
                reportString = reportString.replaceAll(attr.getKey(), (String) attr.getValue());
            else
                reportString = reportString.replaceAll(attr.getKey(), "NA");

        }
        return reportString;
    }


    public byte[] createPdfReport(
            String chassisNumber,String serviceNumber) throws Exception {

        cleanUpFiles(INTERMEDIATE_FILE_NAME);
        if (Objects.isNull(serviceNumber)) {
            serviceNumber = serviceRecordRepository.findByChassisNumber(chassisNumber).
                    stream().max(Comparator.comparingLong(ServiceRecord::getServiceStartTime)).get().getServiceNumber();
        }
        Vehicle vehicle = vehicleRepository.findById(chassisNumber).orElse(null);
        if (Objects.nonNull(vehicle)) {
            OutputStream outputStream =
                    new FileOutputStream(new File(MAIN_PDF_FILE_NAME));
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);


            StringBuilder allReport=new StringBuilder();
//            byte[] firstPageBytes = readFileDataAsByteArray("/templates/FirstPage.html");
            byte[] firstPageBytes = htmlFileRepo.findByFileName(FIRST_PAGE).getHtml().getData();
            VehicleConstants vehicleConstants = new VehicleConstants();
            Map<String, Object> reportValues = vehicleConstants.getVehicleReportValue(vehicle, serviceRecordRepository.findByChassisNumberAndServiceNumber(chassisNumber,serviceNumber));

            String firstPageVariable = new String(firstPageBytes);
            for (Map.Entry<String, Object> reportValue : reportValues.entrySet()) {
                firstPageVariable = firstPageVariable.replaceAll(reportValue.getKey(), (String) reportValue.getValue());
            }
            allReport.append(firstPageVariable);
            Map<String,String> overallHealths = new LinkedHashMap<>();
            HashMap<String,Object> overAllHealthReplaceFromHtml = new HashMap<>();
            Character formNumber ='A';
            String[] status={"Not OK, must be repaired now!","Next Service","OK", NOT_CHECKED};
            String[] colors={"#e74c3c;","#f1c40f;","#07bc0c;","#FC9943;"};
            for (int i = 0; i<status.length; i++){
                StringBuilder formDetail=new StringBuilder();
                ServiceRecord serviceRecord = serviceRecordRepository.findByChassisNumberAndServiceNumber(chassisNumber, serviceNumber);
                Map<String, List<JobCardDataAttributes>> allStatus = getAllStatus(serviceRecord.getJobCardData(), status[i]);
                byte[] headerSection = htmlFileRepo.findByFileName(HEADER_SECTION).getHtml().getData();
                AtomicInteger number = new AtomicInteger(1);
                String color = colors[i];
                for(Map.Entry<String,List<JobCardDataAttributes>> jobCard:allStatus.entrySet()) {
                    List<JobCardDataAttributes> formAttributeFromDB =  jobCard.getValue();
                    if (formAttributeFromDB.size() != 0){
                        Form form = formsRepository.findByFormName(jobCard.getKey());
                        if(!overallHealths.containsKey(jobCard.getKey())){
                            overallHealths.put(jobCard.getKey(),status[i]);

                            overAllHealthReplaceFromHtml.put(OVER_ALL_HEALTH_FORM_NAME +formNumber,jobCard.getKey());
                            overAllHealthReplaceFromHtml.put(OVER_ALL_HEALTH_STATUS +formNumber,status[i]);
                            overAllHealthReplaceFromHtml.put(OVER_ALL_HEALTH_COLOR +formNumber,color);
                            overAllHealthReplaceFromHtml.put(OVER_ALL_HEALTH_WIDTH +formNumber,getWidth(status[i]));
                            formNumber++;
                        }
                        try {

                            String formName = new String(htmlFileRepo.findByFileName(FORM_NAME).getHtml().getData()).replaceAll("_formName_",jobCard.getKey());
                            formName=formName.replaceAll(NUMBER_,String.valueOf( number.getAndIncrement()));
                            byte[] formSection = htmlFileRepo.findByFileName(FORM_DETAILS).getHtml().getData();
                            byte[] formHeader = htmlFileRepo.findById(form.getFileHeaderField()).get().getHtml().getData();
                            StringBuilder formHeaderSection = new StringBuilder(new String(formSection));
                            HashMap<String,Object> populateFormHeader = new HashMap<>();

                            Set<String> classifier = form.getClassifier();
                            Set<String> attributes = form.getAttributes();
                            StringBuilder formReport = new StringBuilder();

                            for (JobCardDataAttributes jobAttributes: formAttributeFromDB) {
                                try {
                                    byte[] brakesByteArr = htmlFileRepo.findById(form.getFileValueField()).get().getHtml().getData();
                                    LinkedHashMap<String, Object> attrMap = getAllReplaceableAttributesForAForm(attributes, classifier, form.getFormName());
                                    LinkedHashMap<String, Object> finalAttrs = populateAttributesValueFromDB(attrMap, jobAttributes.getJobClassifier());
                                    String attributeName = WordUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(jobAttributes.getAttributesName()), ' '));
                                    finalAttrs.put(ATTRIBUTES_, attributeName);
                                    finalAttrs.put(COLOR, color);
//                                    String s1 = new String(brakesByteArr);
                                    String report = replaceAttributesPlaceholderInReportByteArray(finalAttrs, brakesByteArr, form.getFormName());
                                    formReport.append(report);

                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            populateFormHeader.put(FORM_NAME_,formName);
                            populateFormHeader.put(FORM_HEADER_,new String(formHeader));
                            populateFormHeader.put(FORM_VALUE_,formReport.toString());
                            formDetail.append(replaceAttributesPlaceholderInReportByteArray(populateFormHeader, formSection, form.getFormName()));

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                String finalReport=new String(headerSection);
                finalReport=finalReport.replace(FORM_DETAILS_,formDetail.toString());

                finalReport=finalReport.replace(STATUS_,status[i]);

                HtmlConverter.convertToPdf(finalReport, new FileOutputStream(INTERMEDIATE_FILE_NAME));
                pdfDoc = copyPdfContents(pdfDoc, pdfDoc.getNumberOfPages()+1);
            }

            if(overallHealths.size()!=19){
                List<Form> forms = formsRepository.findAll();
                for (Form form : forms){
                    if (!overallHealths.containsKey(form.getFormName())){
                        overallHealths.put(form.getFormName(), NOT_CHECKED);
                        overAllHealthReplaceFromHtml.put(OVER_ALL_HEALTH_FORM_NAME +formNumber,form.getFormName());
                        overAllHealthReplaceFromHtml.put(OVER_ALL_HEALTH_STATUS +formNumber, NOT_CHECKED);
                        overAllHealthReplaceFromHtml.put(OVER_ALL_HEALTH_COLOR +formNumber,colors[3]);
                        overAllHealthReplaceFromHtml.put(OVER_ALL_HEALTH_WIDTH +formNumber, HUNDRED_PERCENTAGE);
                        formNumber++;
                    }
                }
            }
            HtmlConverter.convertToPdf(new String(htmlFileRepo.findByFileName("TC").getHtml().getData()), new FileOutputStream(INTERMEDIATE_FILE_NAME));
//            HtmlConverter.convertToPdf(new String(readFileDataAsByteArray("/templates/TC.html")), new FileOutputStream(INTERMEDIATE_FILE_NAME));
            pdfDoc = copyPdfContents(pdfDoc,pdfDoc.getNumberOfPages()+1);
            allReport= new StringBuilder(replaceAttributesPlaceholderInReportByteArray(overAllHealthReplaceFromHtml, allReport.toString().getBytes()));
            HtmlConverter.convertToPdf(allReport.toString(), new FileOutputStream(INTERMEDIATE_FILE_NAME));
            pdfDoc=copyPdfContents(pdfDoc,1);
            Document document = new Document(pdfDoc);
            document.getFontProvider();
            document.close();

            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.APPLICATION_PDF);
            header.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + chassisNumber.replace(" ", "_"));
            byte[] bytes1 = IOUtils.toByteArray(new FileInputStream(MAIN_PDF_FILE_NAME));

            header.setContentLength(bytes1.length);
            cleanUpFiles(MAIN_PDF_FILE_NAME);
            return bytes1;
        } else
            return null;

    }
    private Map<String,List<JobCardDataAttributes>> getAllStatus(List<JobCardData> jobCards, String status){
        Map<String ,List<JobCardDataAttributes>> statusAttributes = new HashMap<>();
        jobCards.forEach(jobCard -> {
            List<JobCardDataAttributes> jobAttributesList = new ArrayList<>();
            jobCard.getJobCardDataAttributes().forEach(jobAttributes -> {
                if(jobAttributes.getJobClassifier().getStatus().equals(status)){
                    jobAttributesList.add(jobAttributes);
                }
            });
            statusAttributes.put(jobCardRepository.findById(jobCard.getJobCardId()).get().getJobCardDisplayName(),jobAttributesList);
        });
        return statusAttributes;
    }
    private void cleanUpFiles(String path) {
        File targetFile = new File(path);
        targetFile.delete();

    }
    private   String getWidth(String status) {
        return status.equalsIgnoreCase("OK")?"80%;":status.equalsIgnoreCase("Next Service")?"60%;":
                status.equals("Not OK, must be repaired now!")?"25%;":"100%;";
    }
    private PdfDocument copyPdfContents(PdfDocument pdfDoc,Integer pageNumber) throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(INTERMEDIATE_FILE_NAME));
        pdfDocument.getNumberOfPages();
        pdfDocument.copyPagesTo(1,pdfDocument.getNumberOfPages(),pdfDoc,pageNumber);
        cleanUpFiles(INTERMEDIATE_FILE_NAME);
        return pdfDoc;
    }
}
