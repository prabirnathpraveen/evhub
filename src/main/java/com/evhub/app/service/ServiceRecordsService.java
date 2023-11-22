package com.evhub.app.service;

import com.evhub.app.AppApplication;
import com.evhub.app.entities.*;
import com.evhub.app.repository.CanBusDataRepository;
import com.evhub.app.repository.JobCardRepository;
import com.evhub.app.repository.ServiceRecordRepository;
import com.evhub.app.repository.VehicleRepository;
import com.evhub.app.request.UpdateServiceRequest;
import com.evhub.app.response.OverAllHealthResponse;
import com.evhub.app.util.CommonUtils;
import com.evhub.app.util.ImageService;
import com.evhub.app.util.S3Utils;
import com.evhub.app.util.VehicleUtil;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ServiceRecordsService {
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ServiceRecordRepository serviceRecordRepository;
    @Autowired
    private CanBusDataRepository canBusDataRepository;
    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private S3ServiceList s3ServiceList;
    // public String extractedData;

    @Autowired
    S3Utils s3Utils;

    @Autowired
    S3NewService s3NewService;

    public static Map<String, List<Value>> dataCheck;

    @Autowired
    private JobCardRepository jobCardRepository;
    private static final ExecutorService executor = AppApplication.executor;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    private static final Logger LOG = LogManager.getLogger(ServiceRecordsService.class);

    // public List<String> getAllServiceRecordsNumber(String chassisNumber) {
    // List<ServiceRecord> serviceRecords =
    // serviceRecordRepository.findByChassisNumberAndServicingStatus(chassisNumber,
    // 2);
    //// if (Objects.nonNull(serviceRecords)) {
    //// return
    // serviceRecords.stream().map(ServiceRecord::getServiceNumber).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    //
    // if (Objects.nonNull(serviceRecords)) {
    // return serviceRecords.stream()
    // .map(record -> "serviceNumber:" +record.getServiceNumber() + "
    // serviceEndTime:" +record.getServiceEndTime())
    // .sorted(Comparator.reverseOrder())
    // .collect(Collectors.toList());
    // }
    //
    // return new ArrayList<>();
    // }
    public List<Map<String, Object>> getAllServiceRecordsNumber(String chassisNumber) {
        LOG.info("This is get all service records numbers");
        List<ServiceRecord> serviceRecords = serviceRecordRepository
                .findByChassisNumberAndServicingStatus(chassisNumber, 2);
        if (Objects.nonNull(serviceRecords)) {
            return serviceRecords.stream()
                    .sorted(Comparator.comparing(ServiceRecord::getServiceNumber).reversed())
                    .map(record -> {
                        Map<String, Object> serviceMap = new HashMap<>();
                        serviceMap.put("serviceNumber", record.getServiceNumber());
                        serviceMap.put("serviceEndTime", record.getServiceEndTime());
                        return serviceMap;
                    })
                    .collect(Collectors.toList());
        }
        LOG.info("End of the get all service records numbers");
        return new ArrayList<>();
    }

    public String updateServicingInformation(String chassisNumber) {
        LOG.info("This is update servicing infomation ");
        List<ServiceRecord> serviceRecords = serviceRecordRepository
                .findByChassisNumberAndServicingStatus(chassisNumber, 1);
        if (Objects.nonNull(serviceRecords) && serviceRecords.size() != 0) {
            ServiceRecord serviceRecord = serviceRecords.stream()
                    .max(Comparator.comparingLong(ServiceRecord::getServiceStartTime)).get();
            long serviceEndTime = CommonUtils.getCurrentTimeInMillis();
            serviceRecord.setServiceEndTime(serviceEndTime);
            serviceRecord.setServicingStatus(2);
            serviceRecord.setLastUpdated(CommonUtils.getCurrentTimeInMillis());
            serviceRecordRepository.save(serviceRecord);
            Vehicle vehicle = vehicleRepository.findById(chassisNumber).orElse(new Vehicle());
            List<ServiceRecord> inActiveServiceRecords = serviceRecordRepository
                    .findByChassisNumberAndServicingStatus(chassisNumber, 0);
            if (Objects.nonNull(inActiveServiceRecords) && inActiveServiceRecords.size() != 0) {
                ServiceRecord inActiveServiceRecord = inActiveServiceRecords.stream()
                        .min(Comparator.comparingLong(ServiceRecord::getServiceStartTime)).get();
                inActiveServiceRecord.setServicingStatus(1);
                serviceRecordRepository.save(inActiveServiceRecord);
                vehicle.setServicingStatus(1);
                vehicle.setLatestServiceTime(inActiveServiceRecord.getServiceStartTime());
                vehicleRepository.save(vehicle);
            } else {
                vehicle.setServicingStatus(2);
                vehicle.setLatestServiceTime(serviceEndTime);
                vehicleRepository.save(vehicle);
            }
            Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
            Pageable pageable = PageRequest.of(0, 1, sort);
            List<CanBusData> canBusDatas = canBusDataRepository.findByVinnIdAndServiceNumber(chassisNumber,
                    serviceRecord.getServiceNumber());
            if (Objects.nonNull(canBusDatas) && canBusDatas.size() != 0) {
                canBusDatas.forEach(canBusData -> canBusData.setStatus(2));
                canBusDataRepository.saveAll(canBusDatas);
            }
            LOG.info("update servicing infomation ");
            return "Servicing Records Updated";
        }
        LOG.info("update servicing infomation");
        return "There is no active service to update";
    }
    /*
     * return "No InFormation Found";
     * }
     */

    public Object getServicePrice(String chassisNumber, String serviceNumber) {
        LOG.info("This is the get service price");
        Vehicle vehicle = vehicleRepository.findById(chassisNumber).orElse(null);
        if (Objects.nonNull(vehicle)) {
            ServiceRecord serviceRecord = new ServiceRecord();
            if (Objects.nonNull(serviceNumber))
                serviceRecord = serviceRecordRepository.findByChassisNumberAndServiceNumber(chassisNumber,
                        serviceNumber);
            else {
                List<Long> status = new ArrayList<>();
                status.add(1l);
                status.add(2l);

                Sort sort = Sort.by(Sort.Direction.DESC, "serviceStartTime");
                Pageable pageable = PageRequest.of(0, 1, sort);
                List<ServiceRecord> serviceRecords = serviceRecordRepository
                        .findByChassisNumberAndServicingStatusIn(chassisNumber, status, pageable).toList();
                serviceRecord = serviceRecords.stream()
                        .max(Comparator.comparingLong(ServiceRecord::getServiceStartTime)).orElse(new ServiceRecord());
            }
            OverAllHealthResponse overAllHealthPrice = new OverAllHealthResponse();
            overAllHealthPrice.setJobCardDisplayName("ServicingPrice");
            Double servicePrice = serviceRecord.getServicePrice();
            Double originalServicePricing = Objects.nonNull(servicePrice) ? servicePrice : 0;
            overAllHealthPrice.setStatus((String.valueOf(originalServicePricing)));
            List<OverAllHealthResponse> overAllHealthResponses = new ArrayList<>();
            overAllHealthResponses.add(overAllHealthPrice);
            OverAllHealthResponse overAllHealthRepair = new OverAllHealthResponse();
            overAllHealthRepair.setJobCardDisplayName("repairedRequired");
            Double price = vehicle.getPrice();
            Double downGradeValue = serviceRecord.getDownGradeValue();
            double repairPercentage = (originalServicePricing)
                    / (((100 - (Objects.nonNull(downGradeValue) ? downGradeValue : 0)) / 100)
                            * (Objects.nonNull(price) ? price : 0));
            String repairStatus = (repairPercentage > 0.05 && repairPercentage <= 0.1) ? "Bargain to repair"
                    : (repairPercentage > 0.1 && repairPercentage <= 0.2) ? "Good to repair"
                            : (repairPercentage > 0.2 && repairPercentage <= 0.3) ? "Fair to repair"
                                    : (repairPercentage > 0.3 && repairPercentage <= 0.5) ? "High cost to repair"
                                            : (repairPercentage > 0.5) ? "Expensive to repair"
                                                    : "Repair Status Not available";
            overAllHealthRepair.setStatus(repairStatus);
            overAllHealthResponses.add(overAllHealthRepair);
            LOG.info("End of the get service price");
            return overAllHealthResponses;
        } else
            return new OverAllHealthResponse();
    }

    public String createServiceRecords(ServiceRecord serviceRecord, String regNumber) {

        LOG.info("This is the create service Records");
        List<ServiceRecord> serviceRecords = serviceRecordRepository.findByChassisNumber(regNumber);
        serviceRecord.setId(serviceRecord.getChassisNumber() + "_" + (serviceRecords.size() + 1));
        // serviceRecord.setServiceNumber(serviceRecords.size()+1l);
        serviceRecord.setServicingStatus(1);
        serviceRecord.setServiceStartTime(new Date().getTime());
        serviceRecordRepository.insert(serviceRecord);
        LOG.info(" End of get service price");
        return "ServiceRecords Created";
    }

    public String uploadImage(String image) throws Exception {
        log.info("Inside the Upload Image ------->>>>>>>>");
        String out = uploadImageS3(image);
        log.info("again inside after the Upload Image ------->>>>>>>>");
        Pattern pattern = Pattern.compile("image/(.*?);");
        Matcher matcher = pattern.matcher(image);
        String extension = "png";
        if (matcher.find()) {
            extension = matcher.group(1);
        }
        String encodedImage = image.split("base64,")[1];
        double sizeInKB = (4 * Math.ceil((encodedImage.length() / 3)) * 0.5624896334383812) / 1000;
        LOG.info("Uploading the image to s3 before compression, SIZE : " + sizeInKB + " Extension " + extension);
        ImageService imageService = new ImageService();
        byte[] originalImage = Base64.getDecoder().decode(encodedImage);
        // s3ServiceList.uploadBase64Image(originalImage, extension);
        return out;
    }

    public String uploadImageS3(String image) throws Exception {
        Pattern pattern = Pattern.compile("image/(.*?);");
        Matcher matcher = pattern.matcher(image);
        String extension = "png";
        if (matcher.find()) {
            extension = matcher.group(1);
        }
        String encodedImage = image.split("base64,")[1];
        double sizeInKB = (4 * Math.ceil((encodedImage.length() / 3)) * 0.5624896334383812) / 1000;
        LOG.info("Uploading the image to s3 before compression, SIZE : " + sizeInKB + " Extension " + extension);
        ImageService imageService = new ImageService();
        byte[] originalImage = Base64.getDecoder().decode(encodedImage);
        return s3NewService.uploadImage(originalImage, extension);
    }

    public Object updateServiceRecords(UpdateServiceRequest updateServiceRequest, String chassisNumber) {
        LOG.info("This is the update service records");
        Sort sort = Sort.by(Sort.Direction.DESC, "serviceStartTime");
        Pageable pageable = PageRequest.of(0, 1, sort);
        List<ServiceRecord> serviceRecords = serviceRecordRepository
                .findByChassisNumberAndServicingStatusIn(chassisNumber, Arrays.asList(1l, 2l), pageable).toList();
        if (Objects.nonNull(serviceRecords) && serviceRecords.size() != 0) {
            ServiceRecord serviceRecord = serviceRecords.stream()
                    .max(Comparator.comparingLong(ServiceRecord::getServiceStartTime)).get();
            if (serviceRecord.getServicingStatus().equals(1)) {
                serviceRecord = serviceRecords.stream()
                        .filter(serviceRecord1 -> 1 == serviceRecord1.getServicingStatus())
                        .min(Comparator.comparingLong(ServiceRecord::getServiceStartTime)).get();
                // ExecutorService mainExecutorService = Executors.newFixedThreadPool(1);
                ServiceRecord finalServiceRecord = serviceRecord;
                // mainExecutorService.submit(() -> {
                List<JobCardData> updatedJobCardList = new ArrayList<>();
                List<JobCardData> existingJobCardData = new ArrayList<>();
                List<JobCardData> updatedJobCardData = new ArrayList<>();
                List<JobCardData> jobCardList = updateServiceRequest.getJobCardData();
                List<String> jobCardId = jobCardList.parallelStream().map(jobCardData -> jobCardData.getJobCardId())
                        .collect(Collectors.toList());
                if (Objects.nonNull(finalServiceRecord.getJobCardData())) {
                    // List<JobCardData> existingJobCardData =
                    // serviceRecord.getJobCardData().stream().filter(jobCardData ->
                    // !jobCardId.contains(jobCardData.getJobCardId())).collect(Collectors.toList());
                    // updatedJobCardList.addAll(existingJobCardData);
                    finalServiceRecord.getJobCardData().forEach(jobCardData -> {
                        if (!jobCardId.contains(jobCardData.getJobCardId()))
                            existingJobCardData.add(jobCardData);
                        else
                            updatedJobCardData.add(jobCardData);
                    });
                    updatedJobCardList.addAll(existingJobCardData);
                }
                // added code

                //
                JobCard theMainBattery = jobCardRepository.findByJobCardKey("theMainBattery");

                JobCardData theMainBatteryData = jobCardList.stream()
                        .filter(jobCardData -> jobCardData.getJobCardId().equals(theMainBattery.getId())).findFirst()
                        .orElse(null);

                if (Objects.nonNull(theMainBatteryData)) {
                    jobCardList.remove(theMainBatteryData);
                    List<JobCardDataAttributes> theMainBatteryJobCardData = theMainBatteryData
                            .getJobCardDataAttributes().stream().map(jobCardDataAttributes -> {
                                JobClassifier jobClassifier = ObjectUtils
                                        .isEmpty(jobCardDataAttributes.getJobClassifier()) ? new JobClassifier()
                                                : jobCardDataAttributes.getJobClassifier();
                                jobClassifier.setStatus("OK");

                                jobCardDataAttributes.setJobClassifier(jobClassifier);
                                return jobCardDataAttributes;
                            }).collect(Collectors.toList());
                    theMainBatteryData.setJobCardDataAttributes(theMainBatteryJobCardData);
                    jobCardList.add(theMainBatteryData);
                }
                List<String> deletedImage = updateServiceRequest.getDeletedImage();
                jobCardList.parallelStream().forEach(jobCardData -> {
                    // for (JobCardData jobCardData : jobCardList) {
                    // ExecutorService executorService =
                    // Executors.newFixedThreadPool(jobCardData.getJobCardDataAttributes().size());
                    jobCardData.getJobCardDataAttributes().parallelStream().forEach(jobCardDataAttributes -> {
                        // for (JobCardDataAttributes jobCardDataAttributes :
                        // jobCardData.getJobCardDataAttributes()) {
                        // Future<JobCardDataAttributes> updatedAttributes = executorService.submit(()
                        // -> {
                        if (!ObjectUtils.isEmpty(jobCardDataAttributes.getJobClassifier().getImage())) {
                            List<String> list = new ArrayList<>();
                            // ExecutorService imageExecutor =
                            // Executors.newFixedThreadPool(jobCardDataAttributes.getJobClassifier().getImage().size());
                            jobCardDataAttributes.getJobClassifier().getImage().parallelStream().forEach(job -> {
                                // for (String job : jobCardDataAttributes.getJobClassifier().getImage()) {
                                // imageExecutor.submit(() -> {
                                if (job.contains("data:image")) {
                                    Pattern pattern = Pattern.compile("image/(.*?);");
                                    Matcher matcher = pattern.matcher(job);
                                    String extension = "jpej";
                                    if (matcher.find()) {
                                        extension = matcher.group(1);
                                    }
                                    String encodedImage = job.split("base64,")[1];
                                    double sizeInKB = (4 * Math.ceil((encodedImage.length() / 3)) * 0.5624896334383812)
                                            / 1000;

                                    // executor.submit(() -> {
                                    try {
                                        String url = null;
                                        // if
                                        // (!ObjectUtils.isEmpty(jobCardDataAttributes.getJobClassifier().getImage())) {
                                        // List<String> list1 =
                                        // jobCardDataAttributes.getJobClassifier().getImage().stream().map(data ->
                                        // data.split("base64,")[1]).toList();
                                        // for (String jobClassifier : list1) {
                                        LOG.info("Uploading the image to s3 before compression, SIZE : " + sizeInKB
                                                + " Extension " + extension);
                                        ImageService imageService = new ImageService();
                                        byte[] originalImage = Base64.getDecoder().decode(encodedImage);
                                        int orientation = imageService.getOrientation(originalImage, extension);
                                        BufferedImage rotatedImage = imageService.getRotatedImage(originalImage,
                                                orientation);
                                        byte[] compressedImage = imageService.compressImage(rotatedImage, extension,
                                                sizeInKB);
                                        try {
                                            LOG.info("Uploading the image to after Compression s3,  Size: "
                                                    + (double) compressedImage.length / 1024 + " Extension: "
                                                    + extension);
                                            // url = s3ServiceList.uploadBase64Image(compressedImage, extension);//OLD
                                            // S3 SERVICE
                                            url = s3NewService.uploadImage(compressedImage, extension);
                                            list.add(url);
                                        } catch (Exception e) {
                                            log.error("Error Occurred While Uploading File => {}", e.getMessage());
                                        }
                                        // }
                                        // }

                                    } catch (Exception exception) {
                                        log.error("Error Occurred While Uploading File => {}", exception.getMessage());
                                    }
                                } else {
                                    list.add(job);
                                }
                                // });
                                // imageExecutor.shutdown();
                            });
                            jobCardDataAttributes.getJobClassifier().setImage(list);
                        }
                        // });
                    });
                });
                updatedJobCardList.addAll(jobCardList);
                s3ServiceList.deleteS3SingleObject(deletedImage);// Old s3 delete method
                finalServiceRecord.setJobCardData(updatedJobCardList);
                finalServiceRecord.setLastUpdated(CommonUtils.getCurrentTimeInMillis());
                serviceRecordRepository.save(finalServiceRecord);
                // mainExecutorService.shutdown();
                // });
                LOG.info(" End update service records");
                return "Service Record Updated Successfully";
            }
            LOG.info(" End update service records");
            return "Historical service can not be updated";
        } else {
            LOG.info(" End update service records");
            return "There is no active service to update";
        }

    }

    private List<JobCardData> setSteps(List<JobCardData> jobCardDataList) {
        LOG.info("This is set steps");
        List<JobCardData> jobCardDataListWithStep = new ArrayList<>();
        if (!ObjectUtils.isEmpty(jobCardDataList)) {
            for (JobCardData jobCardData : jobCardDataList) {
                List<JobCardAttributes> jobCardAttributesList = jobCardRepository.findById(jobCardData.getJobCardId())
                        .get().getJobAttributes();
                List<JobCardDataAttributes> jobCardDataAttributes = jobCardData.getJobCardDataAttributes();
                List<JobCardDataAttributes> jobCardDataAttributeWithStep = new ArrayList<>();
                if (!ObjectUtils.isEmpty(jobCardDataAttributes)) {
                    for (JobCardDataAttributes cardDataAttribute : jobCardDataAttributes) {
                        JobCardAttributes jobCardAttribute = jobCardAttributesList.stream()
                                .filter(jobCardAttributes -> jobCardAttributes.getAttributesName()
                                        .equalsIgnoreCase(cardDataAttribute.getAttributesName()))
                                .findFirst().orElse(new JobCardAttributes());
                        // if(!ObjectUtils.isEmpty(jobCardAttribute)) {
                        cardDataAttribute.setStep(jobCardAttribute.getStep());
                        jobCardDataAttributeWithStep.add(cardDataAttribute);
                        // }
                    }
                    jobCardData.setJobCardDataAttributes(jobCardDataAttributeWithStep);
                    jobCardDataListWithStep.add(jobCardData);
                }
            }
        }
        LOG.info("End of the set steps");
        return jobCardDataListWithStep;
    }

    public Object getServiceRecordByRegNumberAndServiceNumber(String regNumber, String serviceNumber,
            String jobCardName) {
        LOG.info("This is the get Service record by reg number and service number");
        ServiceRecord serviceRecord = new ServiceRecord();
        if (Objects.isNull(serviceNumber)) {
            serviceRecord = serviceRecordRepository.findByChassisNumberAndServiceNumber(regNumber,
                    String.valueOf((long) serviceRecordRepository.findByChassisNumber(regNumber).size()));
        } else {
            serviceRecord = serviceRecordRepository.findByChassisNumberAndServiceNumber(regNumber, serviceNumber);
        }
        if (jobCardName.equals("overallHealth")) {
            return getOverallHealthJobCard(regNumber, serviceNumber);
        }
        LOG.info("End of the Service record by reg number and service number");
        return serviceRecord.getJobCardData().stream().filter(jobCard -> jobCard.getJobCardId().equals(jobCardName))
                .findFirst().orElse(null);
    }

    public Object getServiceRecordByRegNumberAndServiceNumber(String chassisNumber, String serviceNumber)
            throws ParseException {
        LOG.info("This is the get Service Record By Reg Number And Service Number");
        ServiceRecord serviceRecord = new ServiceRecord();
        if (Objects.isNull(serviceNumber)) {
            List<ServiceRecord> serviceRecords = serviceRecordRepository
                    .findByChassisNumberAndServicingStatus(chassisNumber, 1);
            serviceRecord = serviceRecords.stream().min(Comparator.comparingLong(ServiceRecord::getServiceStartTime))
                    .orElse(new ServiceRecord());
        } else
            serviceRecord = serviceRecordRepository.findByChassisNumberAndServiceNumber(chassisNumber, serviceNumber);
        if (Objects.nonNull(serviceRecord)) {
            // List<CanBusData> canBusData =
            // canBusDataRepository.findByVinnIdAndServiceNumber(serviceRecord.getChassisNumber(),
            // serviceRecord.getServiceNumber());
            List<JobCardData> jobCardDataList = ObjectUtils.isEmpty(serviceRecord.getJobCardData()) ? new ArrayList<>()
                    : serviceRecord.getJobCardData();

            Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
            Pageable pageable = PageRequest.of(0, 1, sort);
            List<CanBusData> canBusData = canBusDataRepository.findByVinnIdAndServiceNumber(
                    serviceRecord.getChassisNumber(), serviceRecord.getServiceNumber(), pageable);
            if (checkCanData(jobCardDataList, canBusData, serviceRecord.getLastUpdated())) {
                if (!ObjectUtils.isEmpty(canBusData) && 1 == serviceRecord.getServicingStatus()) {
                    JobCard theMainBattery = jobCardRepository.findByJobCardKey("theMainBattery");
                    jobCardDataList.removeIf(jobCardData -> theMainBattery.getId().equals(jobCardData.getJobCardId()));
                    JobCardData mainBatteryData = setCanData(canBusData.get(0));
                    jobCardDataList.add(mainBatteryData);
                } else {
                    List<JobCardDataAttributes> jobCardDataAttributeList = new ArrayList<>();
                    VehicleUtil.getMainBatteryFields().forEach((k, v) -> {
                        JobCardDataAttributes jobCardDataAttribute = new JobCardDataAttributes();

                        JobClassifier jobClassifier = new JobClassifier();
                        if (v.equals("capacity") || v.equals("fastChargingDC") || v.equals("regularChargingAC")
                                || v.equals("totalCharged")
                                || v.equals("regenCharging") && ObjectUtils.isEmpty(jobClassifier.getUnit())) {
                            jobClassifier.setUnit("kWh");
                        }
                        if (v.equals("range") && ObjectUtils.isEmpty(jobClassifier.getUnit())) {
                            jobClassifier.setUnit("km");
                        }
                        jobClassifier.setStatus("OK");
                        jobCardDataAttribute.setAttributesName(v);
                        jobCardDataAttribute.setAttributesDisplayName(WordUtils
                                .capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(v), " ")));
                        jobCardDataAttribute.setJobClassifier(jobClassifier);
                        jobCardDataAttributeList.add(jobCardDataAttribute);
                    });
                    VehicleUtil.getMainBatteryChildAttributes().forEach((k, v) -> {
                        JobCardDataAttributes jobCardDataAttribute = new JobCardDataAttributes();
                        JobClassifier jobClassifier = new JobClassifier();
                        if (k.equals("calculatedAmpHourCapacityCAC") && ObjectUtils.isEmpty(jobClassifier.getUnit())) {
                            jobClassifier.setUnit("Ah");
                        }
                        if (k.equals("cellTemperature") && ObjectUtils.isEmpty(jobClassifier.getUnit())) {
                            jobClassifier.setUnit("C");
                        }
                        if (k.equals("cellVoltage") && ObjectUtils.isEmpty(jobClassifier.getUnit())) {
                            jobClassifier.setUnit("V");
                        }
                        jobClassifier.setStatus("OK");
                        jobCardDataAttribute.setAttributesName(k);
                        jobCardDataAttribute.setAttributesDisplayName(WordUtils
                                .capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(k), " ")));
                        jobCardDataAttribute.setJobClassifier(jobClassifier);
                        jobCardDataAttributeList.add(jobCardDataAttribute);
                    });

                    JobCardDataAttributes jobCardDataAttribute = new JobCardDataAttributes();
                    JobClassifier jobClassifier = new JobClassifier();
                    jobClassifier.setUnit("kOhm");
                    jobClassifier.setStatus("OK");
                    jobClassifier.setMeasuredValue("2500");
                    jobCardDataAttribute.setAttributesName("isolationResistance");
                    jobCardDataAttribute.setAttributesDisplayName("Isolation resistance");
                    jobCardDataAttribute.setJobClassifier(jobClassifier);
                    jobCardDataAttributeList.add(jobCardDataAttribute);
                    JobCardData jobCardData = new JobCardData();
                    jobCardData.setJobCardId(jobCardRepository.findByJobCardKey("theMainBattery").getId());
                    jobCardData.setJobCardDataAttributes(jobCardDataAttributeList);
                    jobCardDataList.add(jobCardData);
                }
                serviceRecord.setLastUpdated(CommonUtils.getCurrentTimeInMillis());
                serviceRecord.setJobCardData(jobCardDataList);
                serviceRecordRepository.save(serviceRecord);
            }

            // if (Objects.nonNull(canBusData) && !(canBusData.size() == 0) &&
            // checkCanData(jobCardData)) {
            // JobCardData mainBatteryData = setCanData(canBusData.get(0));
            // jobCardData.add(mainBatteryData);
            // serviceRecord.setJobCardData(jobCardData);
            // serviceRecordRepository.save(serviceRecord);
            // }
            // CanBusData defaultCanData = new CanBusData();
            // defaultCanData.setVinnId(chassisNumber);
            // JobCardData mainBatteryData = setCanData(defaultCanData);
            // jobCardData.add(mainBatteryData);
            // serviceRecord.setJobCardData(jobCardData);
            // serviceRecordRepository.save(serviceRecord);
            // }
            LOG.info("End of the get Service Record By Reg Number And Service Number");
            return setSteps(jobCardDataList);

            // return jobCardData;
        } else {
            LOG.info("End of the get Service Record By Reg Number And Service Number");
            return "NO Service Records is available for Given Chassis Number and Service NUmber";
        }

    }

    public Object getOverallHealthJobCard(String chassissNumber, String serviceNumber) {
        LOG.info("This is get overall health job card");
        ServiceRecord serviceRecord;
        if (Objects.nonNull(serviceNumber))
            serviceRecord = serviceRecordRepository.findByChassisNumberAndServiceNumber(chassissNumber, serviceNumber);
        else {
            List<ServiceRecord> serviceRecords = serviceRecordRepository
                    .findByChassisNumberAndServicingStatusNot(chassissNumber, 0);
            serviceRecord = serviceRecords.stream().max(Comparator.comparingLong(ServiceRecord::getServiceStartTime))
                    .orElse(new ServiceRecord());
        }
        // JobCardData overAllHealthResponses = new JobCardData();
        /*
         * overAllHealthResponses.setJobCardId(jobCardName);
         * overAllHealthResponses.setJobCardDisplayName("Overall Health");
         * List<JobCardDataAttributes> jobAttributesList = new ArrayList<>();
         * if (Objects.nonNull(serviceRecord.getJobCardData())) {
         * serviceRecord.getJobCardData().forEach(jobCardData -> {
         * JobCardDataAttributes overAllHealthResponse = new JobCardDataAttributes();
         * overAllHealthResponse.setAttributesName(jobCardRepository.findById(
         * jobCardData.getJobCardId()).get().getJobCardDisplayName());
         * JobClassifier jobClassifier = new JobClassifier();
         * jobClassifier.setStatus(getOverAllStatus(jobCardData));
         * overAllHealthResponse.setJobClassifier(jobClassifier);
         * jobAttributesList.add(overAllHealthResponse);
         * });
         * }
         * overAllHealthResponses.setJobCardDataAttributes(jobAttributesList);
         */
        List<OverAllHealthResponse> overAllHealthResponses = new ArrayList<>();
        if (Objects.nonNull(serviceRecord.getJobCardData())) {
            serviceRecord.getJobCardData().forEach(jobCardData -> {
                OverAllHealthResponse overAllHealthResponse = new OverAllHealthResponse();
                JobCard jobCard = jobCardRepository.findById(jobCardData.getJobCardId()).get();
                overAllHealthResponse.setJobCardDisplayName(jobCard.getJobCardDisplayName());
                overAllHealthResponse.setStatus(getOverAllStatus(jobCardData));
                overAllHealthResponses.add(overAllHealthResponse);

            });
        }
        LOG.info("End of get overall health job card");
        return overAllHealthResponses;
    }

    public List<ServiceRecord> getServiceRecordsByDate(Long date) {
        LOG.info("This is the get service record by date");
        List<ServiceRecord> serviceRecords = serviceRecordRepository.findByServiceStartTime(date);
        if (Objects.nonNull(serviceRecords) || serviceRecords.size() != 0) {
            return serviceRecords;
        }
        LOG.info("End of the geet service record by date");
        return new ArrayList<>();

    }

    private String getOverAllStatus(JobCardData jobCardData) {
        LOG.info("This is the get over all status");
        Set<String> status = new HashSet<>();
        if (Objects.nonNull(jobCardData)) {
            jobCardData.getJobCardDataAttributes().forEach(jobAttributes -> {
                if (!"Not checked".equals(jobAttributes.getJobClassifier().getStatus())) {
                    status.add(jobAttributes.getJobClassifier().getStatus());
                }
            });
            if (status.size() == 0) {
                return "Not checked";
            } else {
                String status1 = " ";
                if (status.contains("Fail"))
                    status1 = "Fail";
                else if (status.contains("Partially OK"))
                    status1 = "Partially OK";
                else if (status.contains("OK")) {
                    status1 = "OK";
                } else
                    status1 = "Not checked";
                LOG.info("This is the get over all status");
                return status1;
            }
        } else
            return "NA";
    }

    private boolean checkCanData(List<JobCardData> jobCardData, List<CanBusData> canBusData, Long lastUpdated) {
        LOG.info("This is the check Can Data");
        long lastUpdatedValue = ObjectUtils.isEmpty(lastUpdated) ? 0 : lastUpdated;
        JobCard theMainBattery = jobCardRepository.findByJobCardKey("theMainBattery");
        boolean isMainBattery = jobCardData.stream()
                .noneMatch(jobCardData1 -> jobCardData1.getJobCardId().equals(theMainBattery.getId()));
        if (!isMainBattery && !ObjectUtils.isEmpty(canBusData)) {
            CanBusData data = canBusData.stream().max(Comparator.comparing(CanBusData::getTimestamp))
                    .orElse(new CanBusData());
            Long timestamp = ObjectUtils.isEmpty(data.getTimestamp()) ? 0 : data.getTimestamp();
            return timestamp < lastUpdatedValue ? false : true;
        }
        LOG.info("End check Can Data");
        return isMainBattery;

    }

    private JobCardData setCanData(CanBusData canBusData) {
        LOG.info("This is the set can data");
        LOG.info(canBusData.toString());
        JobCard theMainBattery = jobCardRepository.findByJobCardKey("theMainBattery");
        Map<String, List<Value>> data = canBusData.getData();
        dataCheck = data;
        List<JobCardDataAttributes> jobCardDataAttributeList = new ArrayList<>();
        JobCardData jobCardData = new JobCardData();
        if (Objects.nonNull(canBusData) || Objects.nonNull(canBusData.getVinnId())
                || !canBusData.getVinnId().isEmpty()) {

            VehicleUtil.getMainBatteryFields().forEach((k, v) -> {
                if (data.containsKey(k)) {
                    JobCardDataAttributes jobCardDataAttribute = new JobCardDataAttributes();
                    JobClassifier jobClassifier = new JobClassifier();

                    String value = data.get(k).get(0).getValue();
                    Double measuredValue = Double.parseDouble(Objects.isNull(value) || "".equals(value) ? "0" : value);

                    // BigDecimal measuredValue= new
                    // BigDecimal(data.get(k).get(0).getValue()).setScale(1, RoundingMode.HALF_UP);
                    jobClassifier.setMeasuredValue(DECIMAL_FORMAT.format(measuredValue));
                    jobClassifier.setUnit(data.get(k).get(0).getUnit());
                    jobClassifier.setStatus("OK");
                    jobCardDataAttribute.setAttributesName(v);
                    jobCardDataAttribute.setAttributesDisplayName(
                            WordUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(v), " ")));
                    jobCardDataAttribute.setJobClassifier(jobClassifier);
                    jobCardDataAttributeList.add(jobCardDataAttribute);
                }
            });
            VehicleUtil.getMainBatteryChildAttributes().forEach((k, v) -> {
                LOG.info("inside canbus data main battery child attributes");
                LOG.info(k.concat("Key: " + k + " values: " + v.toString()));
                JobCardDataAttributes jobCardDataAttribute = new JobCardDataAttributes();
                JobClassifier jobClassifier = new JobClassifier();

                jobClassifier.setUnit(data.get(v.keySet().toArray()[0]).get(0).getUnit());
                jobCardDataAttribute.setAttributesName(k);
                jobCardDataAttribute.setAttributesDisplayName(
                        WordUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(k), " ")));
                LinkedHashMap<String, String> childAttributes = new LinkedHashMap<>();
                v.forEach((canDataName, displayName) -> {
                    if (!ObjectUtils.isEmpty(data.get(canDataName))) {
                        String value1 = data.get(canDataName).get(0).getValue();
                        Double value = Double.parseDouble(Objects.isNull(value1) || "".equals(value1) ? "0" : value1);

                        childAttributes.put(displayName, DECIMAL_FORMAT.format(value));
                    }
                });
                jobCardDataAttribute.setJobClassifier(jobClassifier);
                jobCardDataAttribute.setChildAttributes(childAttributes);
                jobCardDataAttributeList.add(jobCardDataAttribute);

            });
            JobCardDataAttributes jobCardDataAttribute = new JobCardDataAttributes();
            JobClassifier jobClassifier = new JobClassifier();
            jobClassifier.setUnit("kOhm");
            jobClassifier.setStatus("OK");
            jobClassifier.setMeasuredValue("2500");
            jobCardDataAttribute.setAttributesName("isolationResistance");
            jobCardDataAttribute.setAttributesDisplayName("Isolation resistance");
            jobCardDataAttribute.setJobClassifier(jobClassifier);
            jobCardDataAttributeList.add(jobCardDataAttribute);
            jobCardData.setJobCardId(theMainBattery.getId());
            jobCardData.setJobCardDataAttributes(jobCardDataAttributeList);
        }
        LOG.info("End of set can data");
        return jobCardData;
    }

    public void removedDuplicateMainBattery() {
        List<ServiceRecord> duplicates = serviceRecordRepository.findAll();

        for (ServiceRecord duplicate : duplicates) {
            List<JobCardData> jobCardDataList = duplicate.getJobCardData();
            if (!ObjectUtils.isEmpty(jobCardDataList)) {
                List<JobCardData> jobCardDataList1 = jobCardDataList.stream()
                        .filter(jobCardData -> "182af721818a4d768b41179eccc2bd30".equals(jobCardData.getJobCardId()))
                        .toList();
                if (jobCardDataList1.size() > 1) {
                    jobCardDataList.removeIf(
                            jobCardData -> "182af721818a4d768b41179eccc2bd30".equals(jobCardData.getJobCardId()));
                    jobCardDataList.add(jobCardDataList1.get(jobCardDataList1.size() - 1));
                }
            }
        }
        serviceRecordRepository.saveAll(duplicates);
    }
}
