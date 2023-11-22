package com.evhub.app.repository;

import com.evhub.app.entities.CanBusData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanBusDataRepository extends MongoRepository<CanBusData,String> {
    List<CanBusData> findByStatusIgnoreCase(String status);

    List<CanBusData> findByVinnIdAndStatus(String chassisNumber,int status);
    List<CanBusData> findByVinnIdAndStatus(String chassisNumber,int status,Pageable pageable);

    List<CanBusData> findByVinnIdAndServiceNumber(String vinnId, String serviceNumber);

    List<CanBusData> findByVinnIdAndStatusIn(String vinnId, List<Integer> status);

    List<CanBusData> findByVinnIdAndServiceNumber(String vinnId, String serviceNumber, Pageable pageable);


}
