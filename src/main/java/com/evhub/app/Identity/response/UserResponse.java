package com.evhub.app.Identity.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class UserResponse {
    private String id;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private Long createdAt;
    private Long updatedAt;
    private List<Map<String, Object>> roles;
//    private String contact;
}
