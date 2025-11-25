package com.example.stockpilot;

public class ItemOrder {
    private static String orderNumber;
    private static String orderDate;
    private static String orderPrice;

    public ItemOrder(String orderNumber, String orderDate, String orderPrice) {
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.orderPrice = orderPrice;
    }

    public static String getOrderNumber() {
        return orderNumber;
    }

    public static String getOrderDate() {
        return orderDate;
    }

    public static String getOrderPrice() {
        return orderPrice;
    }
}

