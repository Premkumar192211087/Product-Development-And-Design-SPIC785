package com.example.stockpilot;

/**
 * Model class for payments made to vendors/suppliers
 */
public class PaymentMade {
    private int id;
    private int billId;
    private String billNumber;
    private String paymentNumber;
    private String vendorName;
    private String supplierName;
    private double amount;
    private String paymentMethod;
    private String paymentDate;
    private String referenceNumber;
    private String notes;
    private String status;

    public PaymentMade() {
        // Default constructor
    }

    public PaymentMade(int id, int billId, String billNumber, String paymentNumber, String vendorName, 
                      String supplierName, double amount, String paymentMethod, String paymentDate, 
                      String referenceNumber, String notes, String status) {
        this.id = id;
        this.billId = billId;
        this.billNumber = billNumber;
        this.paymentNumber = paymentNumber;
        this.vendorName = vendorName;
        this.supplierName = supplierName;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}