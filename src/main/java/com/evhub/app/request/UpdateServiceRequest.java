package com.evhub.app.request;

import com.evhub.app.entities.JobCardData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceRequest {
    private List<String> deletedImage;
    private List<JobCardData> jobCardData;
}
