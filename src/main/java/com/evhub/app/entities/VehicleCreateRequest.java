package com.evhub.app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleCreateRequest {

    private String chassisNumber;
    private String regNumber;
    private String personalizedNumber;
}

