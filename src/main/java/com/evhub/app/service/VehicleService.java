package com.evhub.app.service;

import com.aspose.pdf.internal.imaging.coreexceptions.ImageException;
import com.evhub.app.constant.ApplicationConstant;
import com.evhub.app.entities.*;
import com.evhub.app.generic.CountResponse;
import com.evhub.app.repository.CanBusDataRepository;
import com.evhub.app.repository.ServiceRecordRepository;
import com.evhub.app.repository.VehicleRepository;
import com.evhub.app.request.FilterRequest;
import com.evhub.app.request.PageableRequest;
import com.evhub.app.request.SearchFilterRequest;
import com.evhub.app.util.CommonUtils;
import com.evhub.app.util.ImageService;
import com.evhub.app.util.VehicleUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.evhub.app.exception.ValidationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.evhub.app.constant.ApplicationConstant.*;
import static org.keycloak.util.JsonSerialization.mapper;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ServiceRecordRepository serviceRecordRepository;
    @Autowired
    private CanBusDataRepository canBusDataRepository;
    @Autowired
    private S3Service s3Service;

    public CountResponse getVehicles(int offset, int max, String sortOn, String sortType, String search,
            List<String> brand, List<String> model, List<Integer> engineCount) {
        Pageable pageable = PageRequest.of(offset, max,
                Sort.by((sortType.equals("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC), sortOn));
        // Map<String, Object> searchedQuery = params.entrySet().stream().filter(param
        // -> param.getKey().equals("chassisNumber") || param.getKey().equals(BRAND)
        // || param.getKey().equals("regNumber") || param.getKey().equals("search") ||
        // param.getKey().equals(BRAND) ||
        // param.getKey().equals(MODEL)).collect(Collectors.toMap(Map.Entry::getKey,
        // Map.Entry::getValue));
        Map<String, Object> searchedQuery = new HashMap<>();
        addQueryParam(searchedQuery, search, brand, model, engineCount);
        if (searchedQuery.size() > 0) {
            return getSearchVehicles(pageable, searchedQuery);
        }
        Page<Vehicle> vehicles = vehicleRepository.findAll(pageable);
        /*
         * LookupOperation lookupOperation = LookupOperation.newLookup()
         * .from("serviceRecord")
         * .localField("_id")
         * .foreignField("chassisNumber")
         * .as("vehicles_sorted");
         * Aggregation aggregation = Aggregation.newAggregation(lookupOperation,
         * sort(Sort.Direction.DESC, "vehicles_sorted.serviceStartTime",
         * "lastServiceOn", "timestamp"),
         * skip((long) pageable.getPageNumber() * pageable.getPageSize()),
         * limit(pageable.getPageSize()));
         * AggregationResults<Vehicle> vehicles = mongoTemplate.aggregate(aggregation,
         * "vehicles", Vehicle.class);
         */
        CountResponse countResponse = new CountResponse();
        countResponse.setCount(vehicleRepository.count());
        countResponse.setResponse(getVehicleResponse(vehicles.toList()));
        return countResponse;
    }

    public CountResponse getSearchVehicles(Pageable pageable, Map<String, Object> params) {
        Query dynamicQuery = new Query();
        params.forEach((k, v) -> {
            StringBuilder stringBuilder = new StringBuilder(".*");
            stringBuilder.append(v).append(".*");
            if (k.equals("search")) {
                String[] brandModel = ((String) v).split(" ");
                Criteria chassisCriteria = Criteria.where("chassisNumber").regex(stringBuilder.toString(), "i");
                Criteria regCriteria = Criteria.where("regNumber").regex(stringBuilder.toString(), "i");
                Criteria brandCriteria = Criteria.where(BRAND).regex(".*" + brandModel[0] + ".*", "i");
                Criteria modelCriteria = Criteria.where(MODEL).regex(".*" + brandModel[brandModel.length - 1] + ".*",
                        "i");
                Criteria criteria = new Criteria().orOperator(chassisCriteria, regCriteria, brandCriteria,
                        modelCriteria);
                dynamicQuery.addCriteria(criteria);
            } else {
                Criteria criteria = Criteria.where(k).in((List) (v));
                dynamicQuery.addCriteria(criteria);
            }
        });
        String search = Objects.nonNull(params.get("search")) ? ((String) params.get("search")).trim() : "";
        List<Vehicle> searchVehicles = new ArrayList<>();
        Long vehicleCount = 0L;
        if (search.contains(" ")) {
            dynamicQuery.with(pageable.getSort());
            List<Vehicle> vehicleList = mongoTemplate.find(dynamicQuery, Vehicle.class, "vehicles");
            for (Vehicle vehicle : vehicleList) {
                if (((vehicle.getBrand() + " " + vehicle.getModel()).toLowerCase().trim())
                        .contains(search.toLowerCase()))
                    searchVehicles.add(vehicle);
            }
            vehicleCount = (long) searchVehicles.size();
            if (vehicleCount >= pageable.getOffset())
                searchVehicles = searchVehicles.subList((int) pageable.getOffset(),
                        (int) Math.min(vehicleCount, pageable.getOffset() + pageable.getPageSize()));
            else
                searchVehicles = new ArrayList<>();
        } else {
            vehicleCount = mongoTemplate.count(dynamicQuery, Vehicle.class, "vehicles");
            dynamicQuery.with(pageable);
            searchVehicles = mongoTemplate.find(dynamicQuery, Vehicle.class, "vehicles");
        }

        CountResponse countResponse = new CountResponse();
        countResponse.setCount(vehicleCount);
        countResponse.setResponse(getVehicleResponse(searchVehicles));
        return countResponse;
    }

    private List<Vehicle> getVehicleResponse(List<Vehicle> vehicles) {
        List<Vehicle> vehicleList = new ArrayList<>();
        List<Long> status = new ArrayList<>();
        status.add(1l);
        status.add(2l);
        Sort sort = Sort.by(Sort.Direction.DESC, "serviceStartTime");
        Pageable pageable = PageRequest.of(0, 2, sort);
        vehicles.forEach(vehicle -> {
            List<ServiceRecord> serviceRecordList = serviceRecordRepository
                    .findByChassisNumberAndServicingStatusIn(vehicle.getChassisNumber(), status, pageable).toList();

            ServiceRecord serviceRecord = serviceRecordList.stream()
                    .max(Comparator.comparingLong(ServiceRecord::getServiceStartTime)).orElse(new ServiceRecord());
            Integer servicingStatus = serviceRecord.getServicingStatus();
            Long serviceEndTime = serviceRecord.getServiceEndTime();
            if (servicingStatus != 2 && serviceRecordList.size() > 1)
                serviceEndTime = serviceRecordList.get(1).getServiceEndTime();
            vehicle.setLastServiceOn(serviceEndTime);
            vehicle.setServicingStatus(servicingStatus);

            Sort sortCanDataForTimeStamp = Sort.by(Sort.Direction.DESC, "timestamp");
            Pageable pageableForCanData = PageRequest.of(0, 1, sortCanDataForTimeStamp);
            List<CanBusData> canBusData = canBusDataRepository.findByVinnIdAndServiceNumber(
                    serviceRecord.getChassisNumber(), serviceRecord.getServiceNumber(), pageableForCanData);
            if (Objects.nonNull(canBusData) && canBusData.size() != 0) {
                Value odometer = canBusData.get(0).getData().get("odometer").get(0);
                String odometerValue = odometer.getValue();
                Long kilometerFromCanBus = Objects.isNull(odometerValue)
                        || (Math.round(Double.parseDouble(odometerValue)) == 0) ? 0
                                : Math.round(Double.parseDouble(odometerValue));
                Long kilometerFromDB = Objects.isNull(vehicle.getKilometer())
                        || (Math.round(Double.parseDouble(vehicle.getKilometer())) == 0) ? 0
                                : Math.round(Double.parseDouble(vehicle.getKilometer()));
                int compare = kilometerFromDB.compareTo(kilometerFromCanBus);
                vehicle.setKilometer((compare == 0 && kilometerFromDB.equals(0)) ? null
                        : compare > 0 ? kilometerFromDB.toString() : kilometerFromCanBus.toString());
                vehicleRepository.save(vehicle);
            }

            //
            String brand = vehicle.getBrand();
            if (!ObjectUtils.isEmpty(brand) && brand.toLowerCase().contains("tesla")) {
                brand = brand.toLowerCase().replace("motors", "");
            }
            vehicle.setBrand(WordUtils.capitalize(Objects.isNull(brand) ? null : brand.toLowerCase()));
            vehicle.setModel(
                    WordUtils.capitalize(Objects.isNull(vehicle.getModel()) ? null : vehicle.getModel().toLowerCase()));
            if (vehicle.getChassisNumber().equals(vehicle.getRegNumber())) {
                vehicle.setChassisNumber(null);
            }

            vehicleList.add(vehicle);
        });

        return vehicleList;
    }

    public static String capitalizeWord(String str) {
        String words[] = str.split("\\s");
        String capitalizeWord = "";
        for (String w : words) {
            String first = w.substring(0, 1);
            String afterfirst = w.substring(1);
            capitalizeWord += first.toUpperCase() + afterfirst + " ";
        }
        return capitalizeWord.trim();
    }

    public Document createNewVehicle(VehicleCreateRequest vehicleCreateRequest) throws ParseException {
        String chassisNumber = vehicleCreateRequest.getChassisNumber();
        Boolean vehicleFromApi = true;
        String regNumber = vehicleCreateRequest.getRegNumber();
        String personalizedNumber = vehicleCreateRequest.getPersonalizedNumber();
        Vehicle vehicleDetailsFromAPI;
        boolean regNumberEmpty = (Objects.isNull(regNumber) || regNumber.isEmpty());
        if (!(Objects.isNull(chassisNumber) || chassisNumber.isEmpty() || chassisNumber.isBlank())) {
            Optional<Vehicle> vehicle = vehicleRepository.findById(chassisNumber);
            if (vehicle.isPresent()) {
                return createService(vehicle.get());
            }
            vehicleDetailsFromAPI = getVehicleDetailsFromAPI(ApplicationConstant.THIRD_PARTY_URL, chassisNumber);
            if (chassisNumber.equals(vehicleDetailsFromAPI.getChassisNumber())) {
                vehicleDetailsFromAPI.setChassisNumber(chassisNumber);
            } else {
                vehicleDetailsFromAPI = new Vehicle();
                vehicleFromApi = false;
                vehicleDetailsFromAPI.setChassisNumber(chassisNumber);
            }
            if (!regNumberEmpty) {
                vehicleDetailsFromAPI.setRegNumber(regNumber.replace(" ", ""));
            }
        } else if (!regNumberEmpty) {
            if (regNumber.isBlank()) {
                Document document = new Document();
                document.put("vehicleFromApi", true);
                document.put("message",
                        "Please enter either registration number or chassis number or personal number plate");
                return document;
            }

            Vehicle vehicleByRegNumber = vehicleRepository.findByRegNumber(regNumber);
            if (Objects.nonNull(vehicleByRegNumber)) {
                return createService(vehicleByRegNumber);
            }
            vehicleDetailsFromAPI = getVehicleDetailsFromAPI(ApplicationConstant.THIRD_PARTY_URL_REG_NUMBER,
                    regNumber.replace(" ", ""));
            if (Objects.nonNull(vehicleDetailsFromAPI.getRegNumber())
                    && ((regNumber.replace(" ", "").equals(vehicleDetailsFromAPI.getRegNumber().replace(" ", ""))))) {
                vehicleRepository.findById(vehicleDetailsFromAPI.getChassisNumber()).ifPresent(vehicle -> {
                    throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Vehicle Details Already Exist ");
                });
                vehicleDetailsFromAPI.setRegNumber(regNumber.replace(" ", ""));
            } else {
                vehicleDetailsFromAPI = new Vehicle();
                vehicleDetailsFromAPI.setRegNumber(regNumber.replace(" ", ""));
                vehicleFromApi = false;
            }
            if (Objects.isNull(vehicleDetailsFromAPI.getChassisNumber())) {
                vehicleDetailsFromAPI.setChassisNumber(regNumber);
                vehicleDetailsFromAPI.setRegNumber(regNumber.replace(" ", ""));
            }
        }
        // added for the new field personlized number field
        else if (Objects.nonNull(vehicleCreateRequest.getPersonalizedNumber())) {
            vehicleDetailsFromAPI = getVehicleDetailsFromAPI(ApplicationConstant.THIRD_PARTY_URL_REG_NUMBER,
                    personalizedNumber.replace(" ", ""));

            if (Objects.nonNull(vehicleDetailsFromAPI.getPersonalizedNumber()) && ((personalizedNumber.replace(" ", "")
                    .equals(vehicleDetailsFromAPI.getPersonalizedNumber().replace(" ", ""))))) {
                Optional<Vehicle> vehicle1 = vehicleRepository.findById(vehicleDetailsFromAPI.getChassisNumber());
                if (vehicle1.isPresent()) {
                    return createService(vehicle1.get());
                }
                vehicleRepository.findById(vehicleDetailsFromAPI.getChassisNumber()).ifPresent(vehicle -> {
                    throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Vehicle Details Already Exist ");
                });
                vehicleDetailsFromAPI.setPersonalizedNumber(personalizedNumber.replace(" ", ""));
            } else {
                vehicleDetailsFromAPI = new Vehicle();
                vehicleDetailsFromAPI.setPersonalizedNumber(personalizedNumber.replace(" ", ""));
                vehicleFromApi = false;
            }
            if (Objects.isNull(vehicleDetailsFromAPI.getChassisNumber())) {
                vehicleDetailsFromAPI.setChassisNumber(personalizedNumber);
            }
        }
        // personlized number logic ends

        else {
            Document document = new Document();
            document.put("vehicleFromApi", true);
            document.put("message", "Please enter either registration number or chassis number or personal number");
            return document;
        }
        return createVehicle(vehicleDetailsFromAPI, vehicleFromApi);
    }

    @NotNull
    private Document createService(Vehicle vehicleByRegNumber) {
        // List<ServiceRecord> activeServiceRecords =
        // serviceRecordRepository.findByChassisNumberAndServicingStatusIn(vehicleByRegNumber.getChassisNumber(),
        // Arrays.asList(0l, 1l));
        if (vehicleByRegNumber.getServicingStatus() == 2) {
            ServiceRecord activeServiceRecord = new ServiceRecord();
            List<ServiceRecord> serviceRecordList = serviceRecordRepository
                    .findByChassisNumber(vehicleByRegNumber.getChassisNumber());
            if (!ObjectUtils.isEmpty(serviceRecordList))
                activeServiceRecord = serviceRecordList.stream()
                        .max(Comparator.comparing(ServiceRecord::getServiceStartTime)).orElse(new ServiceRecord());
            int status = activeServiceRecord.getServicingStatus();
            if (status != 2) {
                activeServiceRecord.setServicingStatus(2);
                serviceRecordRepository.save(activeServiceRecord);
            }
            createNewServiceRecord(vehicleByRegNumber, 1);
            Document document = new Document();
            document.put("vehicleFromApi", true);
            document.put("message", "New Service record is created");
            return document;
        } else
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Existing service is  InProgress state");
    }

    public Document createVehicle(Vehicle vehicle, Boolean vehicleFromApi) {
        vehicle.setChassisNumber(vehicle.getChassisNumber());
        Sort sort = Sort.by(Sort.Direction.DESC, "serviceStartTime");
        Pageable pageable = PageRequest.of(0, 1, sort);
        List<ServiceRecord> serviceRecords = serviceRecordRepository
                .findByChassisNumberAndServicingStatusIn(vehicle.getChassisNumber(), List.of(0l, 1l), pageable)
                .toList();
        int servicingStatus = (Objects.isNull(serviceRecords) || serviceRecords.size() == 0) ? 1 : 0;
        if (Objects.isNull(serviceRecords) || serviceRecords.size() == 0) {
            vehicle.setServicingStatus(servicingStatus);
            vehicle.setTimestamp(CommonUtils.getCurrentTimeInMillis());
        }
        // vehicleRepository.save(vehicle);
        createNewServiceRecord(vehicle, servicingStatus);
        String disclaimer = vehicle.getDisclaimer();
        String message = "Vehicle  Details is Saved Successfully";
        if (Objects.nonNull(disclaimer)) {

            message = disclaimer;
        }
        Document document = new Document();
        document.put("vehicleFromApi", vehicleFromApi);
        document.put("message", message);
        return document;
    }

    private void createNewServiceRecord(Vehicle vehicle, int servicingStatus) {
        ServiceRecord serviceRecord = new ServiceRecord();
        serviceRecord.setId(CommonUtils.generateUUID());
        serviceRecord.setChassisNumber(vehicle.getChassisNumber());
        serviceRecord.setServicingStatus(servicingStatus);
        long serviceTime = CommonUtils.getCurrentTimeInMillis();
        serviceRecord.setServiceStartTime(serviceTime);
        serviceRecord.setServiceNumber("S-" + serviceTime);
        vehicle.setLatestServiceTime(serviceTime);
        vehicleRepository.save(vehicle);
        serviceRecordRepository.save(serviceRecord);
    }

    public String deleteVehicle(String id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(
                () -> new ValidationException(HttpStatus.NO_CONTENT.value(), "Vehicle Details Not available "));
        vehicleRepository.deleteById(id);
        serviceRecordRepository.deleteByChassisNumber(id);
        List<CanBusData> canData = canBusDataRepository.findByVinnIdAndStatus(id, 1);
        canData.forEach(canBusData -> canBusData.setStatus(3));
        canBusDataRepository.saveAll(canData);
        return "Vehicle  Details is Deleted Successfully";
    }

    public void validationUpdate(Vehicle vehicleRequest) {
        if (!ObjectUtils.isEmpty(vehicleRequest.getCarWeight())) {
            if (!NumberUtils.isNumber(vehicleRequest.getCarWeight())) {
                throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "car weight is not valid");
            }
            if (Integer.parseInt(vehicleRequest.getCarWeight()) < 0) {
                throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "car weight can not negative");
            }
        }
        if (!ObjectUtils.isEmpty(vehicleRequest.getMaximumSpeed())) {
            if (!NumberUtils.isNumber(vehicleRequest.getMaximumSpeed())) {
                throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "maximum speed is not valid");
            }
            if (Integer.parseInt(vehicleRequest.getMaximumSpeed()) < 0) {
                throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "maximum speed can not negative");
            }
        }
        if (!ObjectUtils.isEmpty(vehicleRequest.getKilometer())) {
            if (!NumberUtils.isNumber(vehicleRequest.getKilometer())) {
                throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "kilometer speed is not valid");
            }
            if (Integer.parseInt(vehicleRequest.getKilometer()) < 0) {
                throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "kilometer can not negative");
            }
        }
        if (!ObjectUtils.isEmpty(vehicleRequest.getTotalAllowedWeight())) {
            if (!NumberUtils.isNumber(vehicleRequest.getTotalAllowedWeight())) {
                throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "totalAllowedWeight speed is not valid");
            }
            if (Integer.parseInt(vehicleRequest.getTotalAllowedWeight()) < 0) {
                throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "totalAllowedWeight can not negative");
            }
        }
        if (!ObjectUtils.isEmpty(vehicleRequest.getTotalSeats())) {
            if (vehicleRequest.getTotalSeats() < 0) {
                throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "totalSeats can not negative");
            }
        }
    }

    public String updateVehicle(Vehicle vehicleRequest, String chassisNumber) {
        Vehicle vehicle = vehicleRepository.findById(chassisNumber).orElseThrow(
                () -> new ValidationException(HttpStatus.NO_CONTENT.value(), "Vehicle Details Not available "));

        validationUpdate(vehicleRequest);

        if (!ObjectUtils.isEmpty(vehicleRequest.getLastServiceOn())
                && !ObjectUtils.isEmpty(vehicleRequest.getRegistrationDate())) {
            if (vehicleRequest.getLastServiceOn() < vehicleRequest.getRegistrationDate()) {
                throw new ValidationException(HttpStatus.BAD_REQUEST.value(),
                        "Service is not before the registration date");
            }
        }
        vehicleRequest.setChassisNumber(chassisNumber);

        // extensionforVehicleImage(vehicleRequest);
        //
        // String imageBase64=vehicleRequest.getImage();
        // String imageDataString=
        // imageBase64.substring(vehicleRequest.getImage().indexOf(',')+1);
        // s3Service.uploadBase64Image(imageDataString, vehicleRequest);

        Vehicle updatedVehicle = getUpdateVehicle(vehicleRequest, vehicle);
        vehicleRepository.save(updatedVehicle);
        return "Vehicle  Details is Updated Successfully";
    }

    public static String extensionforVehicleImage(Vehicle vehicleRequest) {
        String image = vehicleRequest.getImage();
        Pattern pattern = Pattern.compile("image/(.*?);");
        Matcher matcher = pattern.matcher(image);
        if (matcher.find()) {
            String vehicleImageExtension = matcher.group(1);
            System.out.println(vehicleImageExtension);
            return vehicleImageExtension;
        }

        return null;
    }

    public Vehicle getUpdateVehicle(Vehicle vehicleRequest, Vehicle vehicle) {

        String imageRequest = vehicleRequest.getImage();
        vehicleRequest.setImage(getCompressedImage(imageRequest));

        Map<String, Object> vehicleFieldValueRequest = mapper.convertValue(vehicleRequest,
                new TypeReference<Map<String, Object>>() {
                });
        Map<String, Object> existingVehicleFieldValue = mapper.convertValue(vehicle,
                new TypeReference<Map<String, Object>>() {
                });
        vehicleFieldValueRequest.forEach((k, v) -> {
            if (Objects.nonNull(v) && !String.valueOf(v).isEmpty()) {
                existingVehicleFieldValue.put(k, v);
            }
        });
        Vehicle updatedVehicle = mapper.convertValue(existingVehicleFieldValue, Vehicle.class);
        return updatedVehicle;
    }

    public String getCompressedImage(String imageRequest) throws ImageException {
        try {
            if (!ObjectUtils.isEmpty(imageRequest)) {
                ImageService imageService = new ImageService();
                Pattern pattern = Pattern.compile("image/(.*?);");
                Matcher matcher = pattern.matcher(imageRequest);
                String extension = "jpej";
                if (matcher.find()) {
                    extension = matcher.group(1);
                }

                String encodedImage = imageRequest.split("base64,")[1];
                double sizeInKB = (4 * Math.ceil((encodedImage.length() / 3)) * 0.5624896334383812) / 1000;
                byte[] originalImage = Base64.getDecoder().decode(encodedImage);
                // InputStream imageInputStream = new ByteArrayInputStream(originalImage);
                // BufferedImage image = ImageIO.read(imageInputStream);
                int orientation = imageService.getOrientation(originalImage, extension);
                BufferedImage rotatedImage = imageService.getRotatedImage(originalImage, orientation);
                byte[] compressedByteImage = imageService.compressImage(rotatedImage, extension, sizeInKB);
                String compressedImage = Base64.getEncoder().encodeToString(compressedByteImage);
                String outputImage = imageRequest.split("base64,")[0] + "base64," + compressedImage;
                // return image
                return outputImage;

            }
        } catch (Exception e) {
            throw new ImageException(e.getMessage());
        }
        return imageRequest;
    }

    public void compressAllImage(int min, int max) {
        Pageable pageable = PageRequest.of(min, max);
        List<Vehicle> vehicleList = vehicleRepository.findAll(pageable).toList();
        if (!ObjectUtils.isEmpty(vehicleList)) {
            for (Vehicle vehicle : vehicleList) {
                vehicle.setImage(getCompressedImage(vehicle.getImage()));
            }
            vehicleRepository.saveAll(vehicleList);
        }
    }

    private static boolean isFieldToBeUpdatedField(Object existingFieldValue, Object newFieldValue) {
        if (Objects.isNull(existingFieldValue)
                || ((Objects.nonNull(newFieldValue) && !String.valueOf(existingFieldValue).isEmpty())
                        && !newFieldValue.equals(existingFieldValue)))
            return true;
        else
            return false;
    }

    public List<String> getDistinctBrand(String brand) {
        if (Objects.isNull(brand) || brand.isEmpty())
            return mongoTemplate.findDistinct(BRAND, Vehicle.class, String.class).stream()
                    .filter(a -> !ObjectUtils.isEmpty(a)).sorted().collect(Collectors.toList());
        else {
            return mongoTemplate
                    .findDistinct(new Query().addCriteria(Criteria.where(BRAND).regex(".*" + brand + ".*", "i")), BRAND,
                            Vehicle.class, String.class)
                    .stream().sorted().collect(Collectors.toList());

        }
    }

    public List<Integer> getEngineCount(Integer engineCount) {
        if (Objects.isNull(engineCount)) {
            return mongoTemplate.findDistinct("engineCount", Vehicle.class, Integer.class).stream()
                    .filter(engine -> !ObjectUtils.isEmpty(engine)).sorted().collect(Collectors.toList());
        } else {
            return mongoTemplate.findDistinct(new Query().addCriteria(Criteria.where("engineCount").is(engineCount)),
                    "engineCount", Vehicle.class, Integer.class).stream().sorted().collect(Collectors.toList());
        }
    }

    public Object getDistinctModel(List brands, String model) {
        Criteria modelCriteria = new Criteria();
        if (Objects.nonNull(model) && !model.isEmpty()) {
            modelCriteria = Criteria.where(MODEL).regex(".*" + model + ".*", "i");
        }
        Aggregation aggregation = Aggregation.newAggregation(
                Objects.nonNull(brands) ? match(Criteria.where(BRAND).in(brands).andOperator(modelCriteria))
                        : match(modelCriteria),
                Aggregation.group(MODEL).count().as(COUNT), project(MODEL, COUNT),
                sort(Sort.Direction.DESC, COUNT, MODEL), limit(10));
        AggregationResults<Document> vehicles = mongoTemplate.aggregate(aggregation, "vehicles", Document.class);
        return vehicles.getMappedResults().stream().map(result -> result.getString("_id")).collect(Collectors.toList());
    }

    public List<Vehicle> getVehicleGraphResponse(Map<String, String> params) {
        Query dynamicQuery = new Query();
        params.forEach((k, v) -> {
            Criteria criteria = Criteria.where(k).is(v);
            dynamicQuery.addCriteria(criteria);

        });
        List<Vehicle> result = mongoTemplate.find(dynamicQuery, Vehicle.class, "vehicle");

        return result;
    }

    private Vehicle getVehicleDetailsFromAPI(String url, String param) throws ParseException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(ApplicationConstant.HEADER_KEY, ApplicationConstant.HEADER_VALUE);
        HttpEntity<HttpHeaders> httpEntity = new HttpEntity<>(httpHeaders);
        try {
            ResponseEntity<Map> exchange = restTemplate.exchange(url + param, HttpMethod.GET, httpEntity, Map.class);
            Map<String, Object> responseBody = exchange.getBody();
            VehicleUtil vehicleUtil = new VehicleUtil();
            Map<String, Object> vehicleResponseFromAPI = vehicleUtil.getVehicleResponse(responseBody, param);
            Vehicle vehicleResponse = mapper.convertValue(vehicleResponseFromAPI, Vehicle.class);
            return vehicleResponse;
        } catch (Exception e) {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Invalid Registration or Chassis Number");
        }
        // }else
        // throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Invalid
        // Registration or Chassis Number");
    }

    public Vehicle loadVehicleDetails(String chassisNumber) throws ParseException {
        return getVehicleDetailsFromAPI(ApplicationConstant.THIRD_PARTY_URL, chassisNumber);

    }

    private void addQueryParam(Map<String, Object> searchedQuery, String search, List<String> brand, List<String> model,
            List<Integer> engineCount) {
        if (Objects.nonNull(search) && !search.isEmpty())
            searchedQuery.put("search", search);
        if (Objects.nonNull(brand) && !brand.isEmpty())
            searchedQuery.put("brand", brand);
        if (Objects.nonNull(model) && !model.isEmpty())
            searchedQuery.put("model", model);
        if (Objects.nonNull(engineCount) && !engineCount.isEmpty())
            searchedQuery.put("engineCount", engineCount);
    }

    public CountResponse getFilterRequest(SearchFilterRequest searchFilterRequest) {
        PageableRequest pageableRequest = Objects.nonNull(searchFilterRequest.getPageableRequest())
                ? searchFilterRequest.getPageableRequest()
                : new PageableRequest();
        FilterRequest filterRequest = searchFilterRequest.getFilterRequest();
        String sortBy = ObjectUtils.isEmpty(pageableRequest.getSortOn()) ? "latestServiceTime"
                : pageableRequest.getSortOn();
        String search = searchFilterRequest.getSearch();
        if (!ObjectUtils.isEmpty(filterRequest) && !ObjectUtils.isEmpty(filterRequest.getTimeFrame())
                && !ObjectUtils.isEmpty(filterRequest.getTimeFrame().getString("key"))) {
            sortBy = filterRequest.getTimeFrame().getString("key").equals("createdOn") ? "timestamp"
                    : pageableRequest.getSortOn();
        }

        Pageable pageable = PageRequest.of(pageableRequest.getOffset(), pageableRequest.getMax(), Sort
                .by((pageableRequest.getSortType().equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC), sortBy));
        if ((Objects.nonNull(search) && !search.trim().isEmpty()) || Objects.nonNull(filterRequest))
            return getSearchVehicles(pageable, search, filterRequest);

        Page<Vehicle> vehicles = vehicleRepository.findAll(pageable);
        CountResponse countResponse = new CountResponse();
        countResponse.setCount(vehicleRepository.count());
        countResponse.setResponse(getVehicleResponse(vehicles.toList()));
        Long end = CommonUtils.getCurrentTimeInMillis();
        return countResponse;
    }

    public CountResponse getSearchVehicles(Pageable pageable, String search, FilterRequest filterRequest) {
        Query dynamicQuery = new Query();
        StringBuilder stringBuilder = new StringBuilder(".*");
        stringBuilder.append(search).append(".*");
        if (Objects.nonNull(search) && !search.trim().isEmpty()) {
            String[] brandModel = search.split(" ");
            Criteria chassisCriteria = Criteria.where("chassisNumber").regex(stringBuilder.toString(), "i");
            Criteria regCriteria = Criteria.where("regNumber").regex(stringBuilder.toString(), "i");
            Criteria brandCriteria = Criteria.where(BRAND).regex(".*" + brandModel[0] + ".*", "i");
            Criteria modelCriteria = Criteria.where(MODEL).regex(".*" + brandModel[brandModel.length - 1] + ".*", "i");
            Criteria personalNumberCriteria = Criteria.where("personalizedNumber").regex(stringBuilder.toString(), "i");
            Criteria criteria = new Criteria().orOperator(chassisCriteria, regCriteria, brandCriteria, modelCriteria,
                    personalNumberCriteria);
            dynamicQuery.addCriteria(criteria);
        }
        if (Objects.nonNull(filterRequest)) {
            getFilterCriteria(filterRequest, dynamicQuery);
        }
        search = Objects.nonNull(search) ? search.trim() : "";
        List<Vehicle> searchVehicles = new ArrayList<>();
        Long vehicleCount = 0L;
        if (search.contains(" ")) {
            dynamicQuery.with(pageable.getSort());
            List<Vehicle> vehicleList = mongoTemplate.find(dynamicQuery, Vehicle.class, "vehicles");
            for (Vehicle vehicle : vehicleList) {
                if (((vehicle.getBrand() + " " + vehicle.getModel()).toLowerCase().trim())
                        .contains(search.toLowerCase()))
                    searchVehicles.add(vehicle);
            }
            vehicleCount = (long) searchVehicles.size();
            if (vehicleCount >= pageable.getOffset())
                searchVehicles = searchVehicles.subList((int) pageable.getOffset(),
                        (int) Math.min(vehicleCount, pageable.getOffset() + pageable.getPageSize()));
            else
                searchVehicles = new ArrayList<>();
        } else {
            vehicleCount = mongoTemplate.count(dynamicQuery, Vehicle.class, "vehicles");
            dynamicQuery.with(pageable);
            searchVehicles = mongoTemplate.find(dynamicQuery, Vehicle.class, "vehicles");
        }

        CountResponse countResponse = new CountResponse();
        countResponse.setCount(vehicleCount);
        countResponse.setResponse(getVehicleResponse(searchVehicles));
        return countResponse;
    }

    private void getFilterCriteria(FilterRequest filterRequest, Query query) {
        List<String> model = filterRequest.getModel();
        List<String> brands = filterRequest.getBrands();
        List<Integer> engineCount = filterRequest.getEngineCount();
        Document timeFrame = filterRequest.getTimeFrame();
        if (Objects.nonNull(model) && !model.isEmpty())
            query.addCriteria(Criteria.where(MODEL).in(model));
        if (Objects.nonNull(brands) && !brands.isEmpty())
            query.addCriteria(Criteria.where(BRAND).in(brands));
        if (Objects.nonNull(engineCount) && !engineCount.isEmpty())
            query.addCriteria(Criteria.where("engineCount").in(engineCount));
        // if (Objects.nonNull(timeFrame) && !timeFrame.isEmpty() ) {

        if ((!ObjectUtils.isEmpty(timeFrame)) && !ObjectUtils.isEmpty(timeFrame.getString("key"))) {
            String key = timeFrame.getString("key");
            // List<String> key = timeFrame.getList("key", String.class, new ArrayList<>());
            Long startTime = Objects.nonNull(timeFrame.getLong("startTime")) ? timeFrame.getLong("startTime") : 0;
            Long endTime = Objects.nonNull(timeFrame.getLong("endTime")) ? timeFrame.getLong("endTime")
                    : CommonUtils.getCurrentTimeInMillis();
            // Criteria criteria = new Criteria();
            // for (String value : key) {

            if (key.contains("createdOn")) {
                query.addCriteria(Criteria.where("timestamp").gte(startTime).lt(endTime));
                // criteria.orOperator(Criteria.where("timestamp").gte(startTime).lt(endTime));
            } else {
                query.addCriteria(Criteria.where("chassisNumber")
                        .in(getDistinctChassisNUmberForServiceTime(key, startTime, endTime)));
                // criteria.orOperator(Criteria.where("chassisNumber").in(getDistinctChassisNUmberForServiceTime(key,
                // startTime, endTime)));
            }
            // query.addCriteria(criteria);
            // }
        }
    }

    private List<String> getDistinctChassisNUmberForServiceTime(String key, Long startTime, Long endTime) {
        Query query = new Query();
        query.addCriteria(Criteria.where(key).gte(startTime).lt(endTime));
        return mongoTemplate.findDistinct(query, "chassisNumber", ServiceRecord.class, String.class);
    }

    public CountResponse getVehicleByChassisNumber(String chassisNumber, String serviceNumber) throws ParseException {
        Vehicle vehicle = vehicleRepository.findById(chassisNumber)
                .orElseThrow(() -> new ValidationException(HttpStatus.BAD_REQUEST.value(), "Invalid chassisNumber"));
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        Pageable pageable = PageRequest.of(0, 1, sort);
        Sort sortService = Sort.by(Sort.Direction.DESC, "serviceStartTime");
        Pageable pageableService = PageRequest.of(0, 1, sortService);
        List<CanBusData> canBusData = new ArrayList<>();
        List<ServiceRecord> serviceRecordList = new ArrayList<>();
        if (ObjectUtils.isEmpty(serviceNumber)) {
            canBusData = canBusDataRepository.findByVinnIdAndStatus(chassisNumber, 1, pageable);
            serviceRecordList = serviceRecordRepository
                    .findByChassisNumberAndServicingStatusIn(chassisNumber, List.of(1l), pageableService).toList();
        } else {
            canBusData = canBusDataRepository.findByVinnIdAndServiceNumber(chassisNumber, serviceNumber, pageable);
            serviceRecordList = List
                    .of(serviceRecordRepository.findByChassisNumberAndServiceNumber(chassisNumber, serviceNumber));
        }
        if (!ObjectUtils.isEmpty(serviceRecordList)) {
            ServiceRecord serviceRecord = serviceRecordList.get(0);
            long lastUpdatedValue = ObjectUtils.isEmpty(serviceRecord.getLastUpdated()) ? 0
                    : serviceRecord.getLastUpdated();
            Vehicle vehicleFromAPI = loadVehicleDetails(chassisNumber);
            vehicle = getUpdateVehicle(vehicleFromAPI, vehicle);
            String serviceNumberFromDb = ObjectUtils.isEmpty(serviceRecord.getServiceNumber()) ? ""
                    : serviceRecord.getServiceNumber();
            if (!ObjectUtils.isEmpty(canBusData)
                    && serviceNumberFromDb.equalsIgnoreCase(canBusData.get(0).getServiceNumber())) {
                Value odometer = canBusData.get(0).getData().get("odometer").get(0);
                String odometerValue = odometer.getValue();
                Long kilometerFromCanBus = Objects.isNull(odometerValue)
                        || (Math.round(Double.parseDouble(odometerValue)) == 0) ? 0
                                : Math.round(Double.parseDouble(odometerValue));
                Long kilometerFromDB = Objects.isNull(vehicle.getKilometer())
                        || (Math.round(Double.parseDouble(vehicle.getKilometer())) == 0) ? 0
                                : Math.round(Double.parseDouble(vehicle.getKilometer()));
                int compare = kilometerFromDB.compareTo(kilometerFromCanBus);
                if (serviceRecord.getServicingStatus() == 1)
                    vehicleRepository.save(vehicle);
                vehicle.setKilometer((compare == 0 && kilometerFromDB.equals(0)) ? null
                        : compare > 0 ? kilometerFromDB.toString() : kilometerFromCanBus.toString());
            }
            vehicle.setLastServiceOn(serviceRecord.getServiceEndTime());
            vehicle.setServicingStatus(serviceRecord.getServicingStatus());
        }

        String brand = vehicle.getBrand();
        if (!ObjectUtils.isEmpty(brand) && brand.toLowerCase().contains("tesla")) {
            brand = brand.toLowerCase().replace("motors", "");
        }
        vehicle.setBrand(WordUtils.capitalize(Objects.isNull(brand) ? null : brand.toLowerCase()));
        vehicle.setModel(
                WordUtils.capitalize(Objects.isNull(vehicle.getModel()) ? null : vehicle.getModel().toLowerCase()));
        CountResponse countResponse = new CountResponse<>();
        countResponse.setCount(1l);
        countResponse.setResponse(List.of(vehicle));
        return countResponse;
    }
}
