package com.example.stockpilot;

import org.json.JSONObject;

public class SaleItem {
    private String productName;
    private int quantity;
    private double unitPrice, totalPrice;

    public SaleItem(JSONObject obj) {
        this.productName = obj.optString("product_name");
        this.quantity = obj.optInt("quantity", 0);
        this.unitPrice = obj.optDouble("unit_price", 0);
        this.totalPrice = obj.optDouble("total_price", quantity * unitPrice);
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getPrice() {
        return unitPrice; // for PDF compatibility
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public double getSubtotal() {
        return totalPrice > 0 ? totalPrice : quantity * unitPrice; // fallback
    }
}

