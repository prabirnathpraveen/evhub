package com.evhub.app.Identity.response;

public class ClientRolesResponse {

    private String role;
    private String description;

    public ClientRolesResponse() {
    }

    public ClientRolesResponse(String role, String description) {
        this.role = role;
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
