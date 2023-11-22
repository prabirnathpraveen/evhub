package com.evhub.app.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageableRequest {
    private int offset = 0;
    private int max = 10;
    private String sortOn = "latestServiceTime";
    private String sortType = "DESC";

}
