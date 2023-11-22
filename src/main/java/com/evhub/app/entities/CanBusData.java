package com.evhub.app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;
import java.util.Map;

@Document(collection = "canBusData")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CanBusData {
    @MongoId
    private ObjectId id;
    private String vinnId;
    private Integer status;
    private String brand;
    private String serviceNumber;
    private Long timestamp;
    private String fileName;
    private Map<String , List<Value>> data;


}
