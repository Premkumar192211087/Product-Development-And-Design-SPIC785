package com.example.stockpilot;

public class Customer {
    private int customerId;
    private String name;
    private String email;
    private String phone;
    private String status;
    private int loyaltyPoints;
    private String lastPurchase;
    private String lastPurchaseDisplay;
    private String registrationDate;
    private String address;
    private String dateOfBirth;
    private String avatarLetter;

    public Customer(int customerId, String name, String email, String phone, String status,
                    int loyaltyPoints, String lastPurchase, String lastPurchaseDisplay,
                    String registrationDate, String address, String dateOfBirth, String avatarLetter) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.loyaltyPoints = loyaltyPoints;
        this.lastPurchase = lastPurchase;
        this.lastPurchaseDisplay = lastPurchaseDisplay;
        this.registrationDate = registrationDate;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.avatarLetter = avatarLetter;
    }

    // Getters
    public int getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public String getLastPurchase() { return lastPurchase; }
    public String getLastPurchaseDisplay() { return lastPurchaseDisplay; }
    public String getRegistrationDate() { return registrationDate; }
    public String getAddress() { return address; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getAvatarLetter() { return avatarLetter; }

    // Setters
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setStatus(String status) { this.status = status; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    public void setLastPurchase(String lastPurchase) { this.lastPurchase = lastPurchase; }
    public void setLastPurchaseDisplay(String lastPurchaseDisplay) { this.lastPurchaseDisplay = lastPurchaseDisplay; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
    public void setAddress(String address) { this.address = address; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setAvatarLetter(String avatarLetter) { this.avatarLetter = avatarLetter; }
}
