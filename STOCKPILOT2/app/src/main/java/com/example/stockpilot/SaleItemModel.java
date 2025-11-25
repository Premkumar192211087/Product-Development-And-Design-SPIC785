package com.example.stockpilot;

public class SaleItemModel {
    private int productId;
    private String productName;
    private String sku;
    private int quantity;
    private double unitPrice;
    private double discountPercent;
    private double discountAmount;
    private double totalPrice;

    public SaleItemModel(int productId, String productName, String sku, int quantity,
                    double unitPrice, double discountPercent, double discountAmount, double totalPrice) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountPercent = discountPercent;
        this.discountAmount = discountAmount;
        this.totalPrice = totalPrice;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}


