package com.example.stockpilot;

public class StockMovement {
    private String productName;
    private String movementType;
    private int quantity;
    private String timestamp;

    public StockMovement(String productName, String movementType, int quantity, String timestamp) {
        this.productName = productName;
        this.movementType = movementType;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getProductName() {
        return productName;
    }

    public String getMovementType() {
        return movementType;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDate() {
        return timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }
}