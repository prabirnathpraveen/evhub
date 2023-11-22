package com.evhub.app.repository;

import com.evhub.app.entities.ServiceRecord;
import com.evhub.app.entities.Vehicle;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle,String> {
//    List<Vehicle> findByUserId(String id);
    @Query("{'_id':?0,'form':{$elemMatch:{key:?1}}}")
    Vehicle findByVehicleIdAndFormName(String Id , String formName);

    @Query(value = "{'_id':?0},{'form.?1':1}",fields = "{'form.?1':1}")
    Vehicle findFormByVehicleIdAndFormName(String Id , String formName);

    @Query(value = "{'_id':?0,'serviceRecords.serviceNumber':?2}",fields = "{'serviceRecords.form.?1':1,'_id':0}")
    Object findFormByVehicleIdAndServiceNumber(String Id , String formName, int serviceNo);

    @Query(value = "{'_id':?0,'serviceRecords.serviceNumber':?1}",fields = "{'serviceRecords.form':1,'_id':0}")
    Object findServiceRecordByVehicleIdAndServiceNumber(String Id , int serviceNo);
    @Aggregation(pipeline = {"{'$match':{'_id':?0,'serviceRecords.serviceNumber':?2}},{'$project':{'serviceRecords':1,regNumber:0}}"})
    List findFormByVehicleIdAndService(String Id , String formName, int serviceNo);


    Vehicle findByRegNumber(String regNumber);
}
