package com.evhub.app.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterRequest {
    private List<String> brands;
    private List<String> model;
    private List<Integer> engineCount;
    private Document timeFrame;

}
