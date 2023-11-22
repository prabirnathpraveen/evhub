package com.evhub.app.service;

import com.evhub.app.entities.CanBusData;
import com.evhub.app.entities.ServiceRecord;
import com.evhub.app.entities.Vehicle;
import com.evhub.app.repository.CanBusDataRepository;
import com.evhub.app.repository.ServiceRecordRepository;
import com.evhub.app.repository.VehicleRepository;
import com.evhub.app.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CanBusDataService {
    @Autowired
    private CanBusDataRepository canBusDataRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ServiceRecordRepository serviceRecordRepository;

    public Integer createNewVehicle() {
        List<CanBusData> activeCanDataList = canBusDataRepository.findByStatusIgnoreCase("active");
        if (Objects.nonNull(activeCanDataList) || activeCanDataList.size() != 0) {
            activeCanDataList.forEach(activeCanData -> {
                Vehicle newVehicle = new Vehicle();
                newVehicle.setChassisNumber(activeCanData.getVinnId());
                newVehicle.setBrand(activeCanData.getBrand());
                /*newVehicle.setRegNumber("EK"+ (6763+1));
                newVehicle.setManufacturer("Tesla,Inc");
                newVehicle.setModel("Model X");
                newVehicle.setEnginePower("193");
                newVehicle.setEnginePowerUnit("KW");
                newVehicle.setBatterySize("65");
                newVehicle.setBatteryType("65");
                newVehicle.setBatterySizeUnit("KW");
                newVehicle.setBatteryTypeUnit("KW");
                newVehicle.setYearOfManufacture("2022");
                newVehicle.setFirstRegisteredOn(activeCanData.getTimestamp());
                newVehicle.setRegistrationDate(activeCanData.getTimestamp());
//                newVehicle.setDownGradeValue(30.0);
                newVehicle.setPrice(300000.0);
                newVehicle.setEuControl(activeCanData.getTimestamp());
                newVehicle.setImportedUsed("No");
                newVehicle.setKilometer("12346");*/
                Vehicle vehicle = vehicleRepository.findById(activeCanData.getVinnId()).orElse(newVehicle);
                // vehicleRepository.save(vehicle);
                // createNewService(activeCanData.getVinnId(),activeCanData.getTimestamp());
                //activeCanData.setStatus("Done");
                //canBusDataRepository.save(activeCanData);
            });
//            return  "New Vehicle Created Successfully";
        }
        return vehicleRepository.findAll().size();
    }

    private void createNewService(String chassisNumber, Long timestamp) {
        ServiceRecord newServiceRecord = new ServiceRecord();
//        List<ServiceRecord> serviceRecords = serviceRecordRepository.findByChassisNumber(chassisNumber);
        List<Long> status = new ArrayList<>();
        status.add(1l);
//        status.add("");
//        List<ServiceRecord> serviceRecords = serviceRecordRepository.findByChassisNumberAndServicingStatusIgnoreCase(chassisNumber,"InProgress");
        Sort sort = Sort.by(Sort.Direction.DESC, "serviceStartTime");
        Pageable pageable = PageRequest.of(0, 2, sort);
        List<ServiceRecord> serviceRecords = serviceRecordRepository.findByChassisNumberAndServicingStatusIn(chassisNumber, status,pageable).toList();
        if (Objects.nonNull(serviceRecords) && serviceRecords.size() != 0) {
            ServiceRecord serviceRecord = serviceRecords.stream().max(Comparator.comparingLong(ServiceRecord::getServiceStartTime)).get();
            newServiceRecord.setServiceNumber(serviceRecord.getServiceNumber() + 1);
            if (serviceRecord.getServicingStatus().equals(2)) {
                serviceRecord.setServicingStatus(1);
                serviceRecordRepository.save(serviceRecord);
            }
            newServiceRecord.setServicingStatus(0);
        } else {
            //newServiceRecord.setServiceNumber();
            newServiceRecord.setServicingStatus(1);
        }
        newServiceRecord.setServiceStartTime(timestamp);
        newServiceRecord.setJobCardData(new ArrayList<>());
        newServiceRecord.setChassisNumber(chassisNumber);
//        newServiceRecord.setServicingStatus("InProgress");
        newServiceRecord.setId(CommonUtils.generateUUID());
        newServiceRecord.setDownGradeValue(30.0);
        newServiceRecord.setServicePrice(30000.0);
        serviceRecordRepository.save(newServiceRecord);
    }

    public Object getCanData(String vinnId, String serviceNumber) {
        CanBusData canData = new CanBusData();
        if (Objects.isNull(serviceNumber)) {
            List<ServiceRecord> byChassisNumber = serviceRecordRepository.findByChassisNumberAndServicingStatus(vinnId, 1);
            if (!ObjectUtils.isEmpty(byChassisNumber)) {
                ServiceRecord serviceRecord = byChassisNumber.stream().max(Comparator.comparingLong(ServiceRecord::getServiceStartTime)).get();
                serviceNumber = serviceRecord.getServiceNumber();
            }
//            List<CanBusData> byVinnIdAndServiceNumber = canBusDataRepository.findByVinnIdAndServiceNumber(vinnId, serviceRecordServiceNumber);

//            canData = byVinnIdAndServiceNumber.stream().max(Comparator.comparingLong(CanBusData::getTimestamp)).get();


//            List<Integer> status = new ArrayList<>();
//            status.add(0);
//            status.add(1);
//            List<CanBusData> canBusDataList = canBusDataRepository.findByVinnIdAndStatusIn(vinnId, status);
//            if (Objects.nonNull(canBusDataList)) {
//                canData = canBusDataList.stream().max(Comparator.comparingLong(CanBusData::getTimestamp)).orElse(new CanBusData());
//                serviceNumber = canData.getServiceNumber();
//
//               if(ObjectUtils.isEmpty(serviceRecordRepository.findByChassisNumberAndServiceNumber(vinnId, serviceNumber))){
//                   return new ArrayList<>();
//               }else {
//                   return canData;
//               }
//            }
        }
//        else {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        Pageable pageable = PageRequest.of(0, 1, sort);
        List<CanBusData> canBusDataList = canBusDataRepository.findByVinnIdAndServiceNumber(vinnId, serviceNumber,pageable);
        return   (Objects.nonNull(canBusDataList) && canBusDataList.size() != 0) ? canBusDataList.get(0) : new ArrayList<>();
//        }
//        if (!ObjectUtils.isEmpty(canData)) {
//            return canData;
//        }
//        return new ArrayList<>();
    }
}
