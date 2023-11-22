package com.evhub.app.repository;

import com.evhub.app.entities.Form;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormsRepository extends MongoRepository<Form,String> {

    Form findByFormName(String formName);
}
