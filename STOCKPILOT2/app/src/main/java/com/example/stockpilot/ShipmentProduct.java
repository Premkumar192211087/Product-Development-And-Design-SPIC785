package com.example.stockpilot;

public class ShipmentProduct {
    private String productName;
    private String sku;
    private String batchId;
    private int quantityShipped;
    private double unitPrice;
    private double totalValue;

    public ShipmentProduct(String productName, String sku, String batchId,
                           int quantityShipped, double unitPrice, double totalValue) {
        this.productName = productName;
        this.sku = sku;
        this.batchId = batchId;
        this.quantityShipped = quantityShipped;
        this.unitPrice = unitPrice;
        this.totalValue = totalValue;
    }

    // Getters
    public String getProductName() {
        return productName;
    }

    public String getSku() {
        return sku;
    }

    public String getBatchId() {
        return batchId;
    }

    public int getQuantityShipped() {
        return quantityShipped;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalValue() {
        return totalValue;
    }

    // Setters
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public void setQuantityShipped(int quantityShipped) {
        this.quantityShipped = quantityShipped;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }
}
