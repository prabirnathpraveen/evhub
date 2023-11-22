package com.evhub.app.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchFilterRequest {
    private  PageableRequest pageableRequest;
    private String search;
    private FilterRequest filterRequest;
}
