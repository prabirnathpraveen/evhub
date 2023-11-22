package com.evhub.app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobCardAttributes {
    private String attributesName;
    private String attributesDisplayName;
    private String step;
    private List<String> childAttributes;
    private List<String> classifiers;
}
