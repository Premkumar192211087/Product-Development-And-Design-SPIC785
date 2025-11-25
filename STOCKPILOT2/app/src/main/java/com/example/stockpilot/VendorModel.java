package com.example.stockpilot;

public class VendorModel {
    private String vendorId;
    private String vendorName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String status;
    private String outstandingBalance;
    private String formattedOutstandingBalance;
    private String notes;
    private String createdAt;
    private String updatedAt;

    public VendorModel(String vendorId, String vendorName, String contactPerson, String email, String phone,
                        String address, String city, String state, String zipCode, String country) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
        // Fixed: Remove incorrect assignments that were causing issues
        this.status = null; // Initialize as null
        this.outstandingBalance = "0.00"; // Initialize with default value
        this.formattedOutstandingBalance = null;
        this.notes = null;
        this.createdAt = null;
        this.updatedAt = null;
    }

    // Getters
    public String getId() { // Added getId() method that was being used in the Activity
        return vendorId;
    }

    public String getName() { // Added getName() method that was being used in the Activity
        return vendorName;
    }

    public String getVendorId() {
        return vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCountry() {
        return country;
    }

    public String getStatus() {
        return status;
    }

    public String getOutstandingBalance() {
        return outstandingBalance;
    }

    public String getFormattedOutstandingBalance() {
        return formattedOutstandingBalance;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOutstandingBalance(String outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public void setFormattedOutstandingBalance(String formattedOutstandingBalance) {
        this.formattedOutstandingBalance = formattedOutstandingBalance;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper method to get first letter of vendor name for avatar
    public String getAvatarLetter() {
        if (vendorName != null && !vendorName.isEmpty()) {
            return vendorName.substring(0, 1).toUpperCase();
        }
        return "?";
    }
}