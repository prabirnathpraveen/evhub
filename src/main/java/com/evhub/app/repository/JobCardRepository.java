package com.evhub.app.repository;

import com.evhub.app.entities.JobCard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobCardRepository extends MongoRepository<JobCard,String> {

    JobCard findByJobCardKey(String jobCardKey);
}
