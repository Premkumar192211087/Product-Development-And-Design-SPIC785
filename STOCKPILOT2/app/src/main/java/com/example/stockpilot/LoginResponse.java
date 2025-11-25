package com.example.stockpilot;

public class LoginResponse {
    private String status;
    private String store_id;
    private String store_name;
    private String role;
    private String message;

    // Constructors
    public LoginResponse() {}

    public LoginResponse(String status, String store_id, String store_name, String role, String message) {
        this.status = status;
        this.store_id = store_id;
        this.store_name = store_name;
        this.role = role;
        this.message = message;
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStoreId() { return store_id; }
    public void setStoreId(String store_id) { this.store_id = store_id; }

    public String getStoreName() { return store_name; }
    public void setStoreName(String store_name) { this.store_name = store_name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

