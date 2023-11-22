package com.evhub.app.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "form")
public class Form {

    @Id
    private String id ;
    private String formName;
    private String label;
    private Set<String> classifier;
    private Set<String> attributes;
    private String fileHeaderField;
    private String fileValueField;


}
