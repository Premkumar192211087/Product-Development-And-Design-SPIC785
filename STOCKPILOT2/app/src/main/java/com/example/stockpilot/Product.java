package com.example.stockpilot;

import java.io.Serializable;

public class Product implements Serializable {
    private int productId;
    private String productName;
    private String sku;
    private String category;
    private int stockQuantity;
    private double sellingPrice;
    
    public Product(int productId, String productName, String sku, String category, int stockQuantity, double sellingPrice) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.sellingPrice = sellingPrice;
    }
    
    // Constructor overload for backward compatibility
    public Product(int productId, String productName, String sku, double sellingPrice) {
        this(productId, productName, sku, "", 0, sellingPrice);
    }
    
    // Getters
    public int getProductId() {
        return productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public String getSku() {
        return sku;
    }
    
    public String getCategory() {
        return category;
    }
    
    public int getStockQuantity() {
        return stockQuantity;
    }
    
    public double getSellingPrice() {
        return sellingPrice;
    }
    
    // Setters
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
}
