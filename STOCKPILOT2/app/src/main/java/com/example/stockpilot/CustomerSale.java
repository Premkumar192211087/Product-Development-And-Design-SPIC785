package com.example.stockpilot;

import java.io.Serializable;

public class CustomerSale implements Serializable {
    private int customerId;
    private String customerName;

    public CustomerSale(int customerId, String customerName) {
        this.customerId = customerId;
        this.customerName = customerName;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    @Override
    public String toString() {
        return customerName;
    }
}
