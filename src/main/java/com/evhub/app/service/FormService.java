package com.evhub.app.service;

import com.evhub.app.entities.Form;
import com.evhub.app.exception.ValidationException;
import com.evhub.app.generic.CountResponse;
import com.evhub.app.repository.FormsRepository;
import com.evhub.app.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class FormService {
    @Autowired
    private FormsRepository formsRepository;

    public CountResponse getForms(String formId, int offset, int max, String sortType) {
        if (Objects.nonNull(formId)) {
            Form form = formsRepository.findById(formId).orElse(null);
            CountResponse countResponse = new CountResponse();
            countResponse.setResponse(List.of(form));
            countResponse.setCount(1l);
            return countResponse;
        }
        Pageable pageable = PageRequest.of(offset, max, Sort.by(sortType));
        Page<Form> formPage = formsRepository.findAll(pageable);
        List<Form> formList = new ArrayList<>();
        formPage.forEach(form -> {
            formList.add(form);
        });
        CountResponse countResponse = new CountResponse();
        countResponse.setResponse(formList);
        countResponse.setCount(formsRepository.count());
        return countResponse;

    }

    public String createForm(Form formRequest) {
        formRequest.setId(CommonUtils.generateUUID());
        formsRepository.save(formRequest);
        return "Form is created  Successfully ";

    }

    public String deleteForm(String id) {
        Form form = formsRepository.findById(id).orElse(null);
        if (Objects.nonNull(form)) {
            formsRepository.deleteById(id);
            return "Form is Delete  Successfully ";
        } else
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Form data does not Exists");

    }

    public String updateForm(Form formRequest, String id) {
        Form form = formsRepository.findById(id).orElse(null);
        if (Objects.nonNull(form)) {
            formRequest.setId(id);
            formsRepository.save(formRequest);
            return "Form is Updated  Successfully ";
        } else {
            throw new ValidationException(HttpStatus.BAD_REQUEST.value(), "Form data does not Exists");
        }
    }
}
