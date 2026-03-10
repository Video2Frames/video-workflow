package com.store.workflowService.update.infra.adapters.out.client.dto;

public class UserAuthResponseDTO {
    private String id;
    private String email;

    public UserAuthResponseDTO() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

