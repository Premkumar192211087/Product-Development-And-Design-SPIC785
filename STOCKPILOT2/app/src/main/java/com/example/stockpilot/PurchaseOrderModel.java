package com.example.stockpilot;

/**
 * Model class for purchase orders
 */
public class PurchaseOrderModel {
    private String id;
    private String poNumber;
    private String vendorId;
    private String vendorName;
    private String storeId;
    private String poDate;
    private String expectedDate;
    private String status;
    private double subtotal;
    private double tax;
    private double discount;
    private double total;
    private String notes;
    private String createdAt;
    private String updatedAt;

    /**
     * Constructor for purchase order model
     *
     * @param id Purchase order ID
     * @param poNumber Purchase order number
     * @param vendorId Vendor ID
     * @param vendorName Vendor name
     * @param storeId Store ID
     * @param poDate Purchase order date
     * @param expectedDate Expected delivery date
     * @param status Status
     * @param subtotal Subtotal
     * @param tax Tax
     * @param discount Discount
     * @param total Total
     * @param notes Notes
     * @param createdAt Created at timestamp
     * @param updatedAt Updated at timestamp
     */
    public PurchaseOrderModel(String id, String poNumber, String vendorId, String vendorName,
                                String storeId, String poDate, String expectedDate, String status,
                                double subtotal, double tax, double discount, double total,
                                String notes, String createdAt, String updatedAt) {
        this.id = id;
        this.poNumber = poNumber;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.storeId = storeId;
        this.poDate = poDate;
        this.expectedDate = expectedDate;
        this.status = status;
        this.subtotal = subtotal;
        this.tax = tax;
        this.discount = discount;
        this.total = total;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
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

    public String getPoDate() {
        return poDate;
    }

    public void setPoDate(String poDate) {
        this.poDate = poDate;
    }

    public String getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(String expectedDate) {
        this.expectedDate = expectedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
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

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}