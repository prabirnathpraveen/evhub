package com.evhub.app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobCardDataAttributes {
    private String attributesName;
    private String attributesDisplayName;
    private LinkedHashMap<String,String> childAttributes;
    private String step;
    private JobClassifier jobClassifier;
}
