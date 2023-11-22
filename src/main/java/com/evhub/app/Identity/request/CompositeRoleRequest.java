package com.evhub.app.Identity.request;

import java.util.List;

public class CompositeRoleRequest {

    private String role;
    private String clientId;
    private List<String> clientRolesList;

    public CompositeRoleRequest() {
    }

    public CompositeRoleRequest(String role, String clientId, List<String> clientRolesList) {
        this.role = role;
        this.clientId = clientId;
        this.clientRolesList = clientRolesList;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<String> getClientRolesList() {
        return clientRolesList;
    }

    public void setClientRolesList(List<String> clientRolesList) {
        this.clientRolesList = clientRolesList;
    }
}
