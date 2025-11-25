package com.example.stockpilot;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Vendor implements Parcelable {
    private String id;
    private String name;
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
    private String createdAt;

    public Vendor() {
        // Empty constructor required for Parcelable
    }

    public Vendor(String id, String name, String contactPerson, String email, String phone, 
                 String address, String city, String state, String zipCode, String country) {
        this.id = id;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
        this.status = "active";
        this.outstandingBalance = "0.0";
    }

    protected Vendor(Parcel in) {
        id = in.readString();
        name = in.readString();
        contactPerson = in.readString();
        email = in.readString();
        phone = in.readString();
        address = in.readString();
        city = in.readString();
        state = in.readString();
        zipCode = in.readString();
        country = in.readString();
        status = in.readString();
        outstandingBalance = in.readString();
        createdAt = in.readString();
    }

    public static final Creator<Vendor> CREATOR = new Creator<Vendor>() {
        @Override
        public Vendor createFromParcel(Parcel in) {
            return new Vendor(in);
        }

        @Override
        public Vendor[] newArray(int size) {
            return new Vendor[size];
        }
    };

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(String outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Get full address as a single string
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null && !address.isEmpty()) sb.append(address);
        
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        
        if (state != null && !state.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(state);
        }
        
        if (zipCode != null && !zipCode.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(zipCode);
        }
        
        if (country != null && !country.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }
        
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(contactPerson);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(zipCode);
        dest.writeString(country);
        dest.writeString(status);
        dest.writeString(outstandingBalance);
        dest.writeString(createdAt);
    }
}