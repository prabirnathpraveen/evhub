package com.evhub.app.response;

import com.evhub.app.entities.Attributes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormResponse {
    private List<Attributes> attributes;
    private Object formData;

}
