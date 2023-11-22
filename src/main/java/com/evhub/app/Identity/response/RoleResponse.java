package com.evhub.app.Identity.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoleResponse {
    private String id;
    private String name;
    private String description;
}
