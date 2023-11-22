package com.evhub.app.Identity.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class RoleCreateRequest {
    private String name;
    private String description;
    private List<String> permission;
    private List<String> alerts;
}
