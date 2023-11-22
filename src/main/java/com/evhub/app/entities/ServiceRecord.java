package com.evhub.app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "serviceRecord")
public class ServiceRecord {

    @Id
    private String id;
    private String serviceNumber;
    private  Long serviceStartTime;
    private  Long serviceEndTime;
    private  Long lastUpdated;
//    private Object form;
//    private List<JobCard> jobCards;
    private List<JobCardData> jobCardData;
    private List<Object> canData;
    private Double servicePrice;
    private Integer servicingStatus;
    private String chassisNumber;
    private String servicefile;
    private Double downGradeValue;
    private Integer canbusfile;

}
