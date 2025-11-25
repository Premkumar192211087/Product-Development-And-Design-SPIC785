package com.example.stockpilot;

import java.util.Date;

/**
 * Model class for bills generated from purchase orders
 */
public class Bills {
    private String id;
    private String billNumber;
    private String purchaseOrderId;
    private String vendorId;
    private String vendorName;
    private String storeId;
    private String billDate;
    private String dueDate;
    private String status; // "pending", "paid", "partial", "cancelled"
    private double amount;
    private double amountPaid;
    private double balance;
    private String notes;
    private String createdAt;
    private String updatedAt;

    /**
     * Constructor for creating a new bill
     */
    public Bills(String billNumber, String purchaseOrderId, String vendorId, String vendorName,
                String storeId, String billDate, String dueDate, String status,
                double amount, double amountPaid, String notes) {
        this.billNumber = billNumber;
        this.purchaseOrderId = purchaseOrderId;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.storeId = storeId;
        this.billDate = billDate;
        this.dueDate = dueDate;
        this.status = status;
        this.amount = amount;
        this.amountPaid = amountPaid;
        this.balance = amount - amountPaid;
        this.notes = notes;
        this.createdAt = new Date().toString();
        this.updatedAt = new Date().toString();
    }

    /**
     * Constructor for existing bills
     */
    public Bills(String id, String billNumber, String purchaseOrderId, String vendorId, String vendorName,
                String storeId, String billDate, String dueDate, String status,
                double amount, double amountPaid, double balance, String notes,
                String createdAt, String updatedAt) {
        this.id = id;
        this.billNumber = billNumber;
        this.purchaseOrderId = purchaseOrderId;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.storeId = storeId;
        this.billDate = billDate;
        this.dueDate = dueDate;
        this.status = status;
        this.amount = amount;
        this.amountPaid = amountPaid;
        this.balance = balance;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Update payment status based on amount paid
     */
    public void updatePaymentStatus() {
        if (amountPaid >= amount) {
            status = "paid";
            balance = 0;
        } else if (amountPaid > 0) {
            status = "partial";
            balance = amount - amountPaid;
        } else {
            status = "pending";
            balance = amount;
        }
        updatedAt = new Date().toString();
    }

    /**
     * Make a payment towards this bill
     * @param paymentAmount Amount to pay
     * @return true if payment was successful, false otherwise
     */
    public boolean makePayment(double paymentAmount) {
        if (paymentAmount <= 0 || paymentAmount > balance) {
            return false;
        }
        
        amountPaid += paymentAmount;
        updatePaymentStatus();
        return true;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        this.balance = amount - amountPaid;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
        this.balance = amount - amountPaid;
    }

    public double getBalance() {
        return balance;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
