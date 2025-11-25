package com.example.stockpilot;

public class Item {
    private int id;
    private String productName;
    private String sku;
    private double quantity;
    private double price;
    private String imageUrl;
    private String status;
    private double purchasePrice;
    private String unit;
    private String storeId;
    private String description;
    private String category;
    private double cost;


    // Default constructor
    public Item() {
    }

    // Constructor with parameters
    public Item(int id, String productName, String sku, double quantity,
                double price, String imageUrl, String status) {
        this.id = id;
        this.productName = productName;
        this.sku = sku;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getSku() {
        return sku;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public String getUnit() {
        return unit;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    // Helper methods
    public boolean isInStock() {
        return quantity > 0;
    }

    public boolean isLowStock() {
        return quantity > 0 && quantity <= 10; // Adjust threshold as needed
    }

    public boolean isOutOfStock() {
        return quantity <= 0;
    }
    public String getStoreId() {
        return storeId;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getCost() {
        return cost;
    }
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }


    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
