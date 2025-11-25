package com.example.stockpilot;

public class staffmodel {
    private int staffId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String address;

    public staffmodel(int staffId, String fullName, String email, String phone, String role, String address) {
        this.staffId = staffId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.address = address;
    }

    public int getStaffId() { return staffId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getAddress() { return address; }
}

