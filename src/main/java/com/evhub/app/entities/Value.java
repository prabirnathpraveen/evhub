package com.evhub.app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Value {
private String tag;
private String tagName;
private String unit;
private String map;
private String value;
}
