package com.example.stockpilot;

public class Package {
    private int id;
    private String productName;
    private String type;
    private int quantity;
    private String price;
    private String timestamp;

    public Package(int id, String productName, String type, int quantity, String price, String timestamp) {
        this.id = id;
        this.productName = productName;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
    }

    // Getters
    public int getId() { return id; }
    public String getProductName() { return productName; }
    public String getType() { return type; }
    public int getQuantity() { return quantity; }
    public String getPrice() { return price; }
    public String getTimestamp() { return timestamp; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setType(String type) { this.type = type; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(String price) { this.price = price; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
