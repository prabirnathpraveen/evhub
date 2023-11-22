package com.evhub.app.service;

import com.evhub.app.entities.JobCard;
import com.evhub.app.repository.JobCardRepository;
import com.evhub.app.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobCardService {

    @Autowired
    private JobCardRepository jobCardRepository;

    public String createJobCard (JobCard jobCard){
        jobCard.setId(CommonUtils.generateUUID());
        jobCardRepository.save(jobCard);
        return "Job Card Inserted";
    }

    public JobCard getJobCardById(String id ){
        return jobCardRepository.findById(id).orElse(null);
    }

    public List<JobCard> getAllJobCard( ){
        return jobCardRepository.findAll();
    }

}
