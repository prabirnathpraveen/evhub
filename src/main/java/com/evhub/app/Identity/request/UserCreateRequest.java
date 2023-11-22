package com.evhub.app.Identity.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Data
@NoArgsConstructor
public class UserCreateRequest {
    private String email;
    private String userName;
    private String firstName;
    private String lastName;
    private String password;
//    private Boolean enabled;
    private String roles;
//    private String contact;
}