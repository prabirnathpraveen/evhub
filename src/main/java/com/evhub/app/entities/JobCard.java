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
@Document(collection = "jobCard")
public class JobCard {
    @Id
    private String id;
    private String  jobCardKey;
    private String jobCardDisplayName;
    private List<String> steps;
    private List<JobCardAttributes> jobAttributes;

}
