package com.evhub.app.repository;

import com.evhub.app.entities.ServiceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRecordRepository extends MongoRepository<ServiceRecord, String> {
    List<ServiceRecord> findByChassisNumber(String chassisNumber);
    List<ServiceRecord> findByChassisNumberAndServicingStatusNot(String chassisNumber, Integer status);
    List<ServiceRecord> findByChassisNumberAndServicingStatus(String chassisNumber, Integer status);
    ServiceRecord findByChassisNumberAndServiceNumber(String chassisNumber, String serviceNumber);

    List<ServiceRecord> findByServiceStartTime(long time);
    List<ServiceRecord> findByChassisNumberAndServicingStatusIn(String chassisNumber, List<Long> status);
    Page<ServiceRecord> findByChassisNumberAndServicingStatusIn(String chassisNumber, List<Long> status, Pageable pageable);
    List<ServiceRecord> deleteByChassisNumber(String chassisNumber);
}
