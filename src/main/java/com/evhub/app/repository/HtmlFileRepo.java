package com.evhub.app.repository;

import com.evhub.app.entities.HtmlFiles;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface HtmlFileRepo extends MongoRepository<HtmlFiles,String> {
    HtmlFiles findByFileName(String brakes);
}
