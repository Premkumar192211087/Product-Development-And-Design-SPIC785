package com.example.stockpilot;

import java.io.Serializable;

public class ShipmentItem implements Serializable {
    private int productId;
    private String productName;
    private String productSku;
    private int quantityShipped;
    private double unitPrice;
    private double totalValue;
    private String batchId;

    // Constructor for creating a new shipment item
    public ShipmentItem(int productId, String productName, String productSku, int quantityShipped, 
                        double unitPrice, String batchId) {
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.quantityShipped = quantityShipped;
        this.unitPrice = unitPrice;
        this.batchId = batchId;
        this.totalValue = quantityShipped * unitPrice;
    }

    // Constructor for creating from API response
    public ShipmentItem(int productId, String productName, String productSku, int quantityShipped, 
                        double unitPrice, double totalValue, String batchId) {
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.quantityShipped = quantityShipped;
        this.unitPrice = unitPrice;
        this.totalValue = totalValue;
        this.batchId = batchId;
    }

    // Getters and Setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public int getQuantityShipped() {
        return quantityShipped;
    }

    public void setQuantityShipped(int quantityShipped) {
        this.quantityShipped = quantityShipped;
        // Recalculate total value when quantity changes
        this.totalValue = this.quantityShipped * this.unitPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        // Recalculate total value when unit price changes
        this.totalValue = this.quantityShipped * this.unitPrice;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    // Helper method to recalculate total value
    public void recalculateTotalValue() {
        this.totalValue = this.quantityShipped * this.unitPrice;
    }
}
