package com.example.stockpilot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SalesOrder {
    private String invoiceNumber, customerName, paymentMethod, paymentStatus, saleDate, servedBy;
    private double finalAmount;
    private List<SaleItem> items;

    public SalesOrder(JSONObject obj) {
        this.invoiceNumber = obj.optString("invoice_number");
        this.customerName = obj.optString("customer_name");
        this.paymentMethod = formatText(obj.optString("payment_method"));
        this.paymentStatus = formatText(obj.optString("payment_status"));
        this.saleDate = formatDate(obj.optString("sale_date"));
        this.servedBy = obj.optString("served_by");
        this.finalAmount = obj.optDouble("final_amount", 0);
        this.items = new ArrayList<>();

        JSONArray itemArray = obj.optJSONArray("items");
        if (itemArray != null) {
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject itemObj = itemArray.optJSONObject(i);
                if (itemObj != null) {
                    items.add(new SaleItem(itemObj));
                }
            }
        }
    }

    private String formatText(String s) {
        if (s == null) return "";
        return s.replace("_", " ");
    }

    private String formatDate(String rawDate) {
        return rawDate != null && rawDate.contains("T") ? rawDate.split("T")[0] : rawDate;
    }

    public String getInvoiceNumber() { return invoiceNumber; }

    public String getCustomerName() { return customerName; }

    public String getPaymentMethod() { return paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }

    public String getSaleDate() { return saleDate; }

    public String getServedBy() { return servedBy; }

    public double getFinalAmount() { return finalAmount; }

    public List<SaleItem> getItems() { return items; }

    public int getItemsCount() { return items != null ? items.size() : 0; }
}
