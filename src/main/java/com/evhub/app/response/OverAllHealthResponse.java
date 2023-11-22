package com.evhub.app.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OverAllHealthResponse {
    private String jobCardDisplayName;
    private String status;
}
