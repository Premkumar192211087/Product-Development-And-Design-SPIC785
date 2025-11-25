package com.example.stockpilot;

import java.io.Serializable;

public class Shipment implements Serializable {
    private int shipmentId;
    private String shipmentNumber;
    private String orderType;
    private int orderId;
    private String orderNumber;
    private String carrierName;
    private String trackingNumber;
    private String shippingMethod;
    private double shippingCost;
    private String shipDate;
    private String estimatedDeliveryDate;
    private String actualDeliveryDate;
    private String status;
    private String recipientName;
    private String recipientAddress;
    private String recipientPhone;
    private String notes;
    private String createdAt;
    private String updatedAt;
    private boolean canTrack;
    private boolean isOverdue;

    // Default constructor
    public Shipment() {}

    // Getters and Setters
    public int getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(int shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getShipmentNumber() {
        return shipmentNumber != null ? shipmentNumber : "";
    }

    public void setShipmentNumber(String shipmentNumber) {
        this.shipmentNumber = shipmentNumber;
    }

    public String getOrderType() {
        return orderType != null ? orderType : "";
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber != null ? orderNumber : "";
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCarrierName() {
        return carrierName != null ? carrierName : "";
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getTrackingNumber() {
        return trackingNumber != null ? trackingNumber : "";
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getShippingMethod() {
        return shippingMethod != null ? shippingMethod : "";
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public String getShipDate() {
        return shipDate;
    }

    public void setShipDate(String shipDate) {
        this.shipDate = shipDate;
    }

    public String getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(String estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public String getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public void setActualDeliveryDate(String actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }

    public String getStatus() {
        return status != null ? status : "";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecipientName() {
        return recipientName != null ? recipientName : "";
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientAddress() {
        return recipientAddress != null ? recipientAddress : "";
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public String getRecipientPhone() {
        return recipientPhone != null ? recipientPhone : "";
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getNotes() {
        return notes != null ? notes : "";
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt != null ? createdAt : "";
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt != null ? updatedAt : "";
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean canTrack() {
        return canTrack;
    }

    public void setCanTrack(boolean canTrack) {
        this.canTrack = canTrack;
    }

    public boolean isOverdue() {
        return isOverdue;
    }

    public void setOverdue(boolean overdue) {
        isOverdue = overdue;
    }

    // Helper methods
    public String getFormattedStatus() {
        return getStatus().replace("_", " ").toUpperCase();
    }

    public boolean isPending() {
        return "pending".equals(getStatus());
    }

    public boolean isShipped() {
        return "shipped".equals(getStatus());
    }

    public boolean isInTransit() {
        return "in_transit".equals(getStatus());
    }

    public boolean isDelivered() {
        return "delivered".equals(getStatus());
    }

    public boolean hasTrackingNumber() {
        return getTrackingNumber() != null && !getTrackingNumber().trim().isEmpty();
    }

    @Override
    public String toString() {
        return "Shipment{" +
                "shipmentId=" + shipmentId +
                ", shipmentNumber='" + shipmentNumber + '\'' +
                ", orderType='" + orderType + '\'' +
                ", status='" + status + '\'' +
                ", carrierName='" + carrierName + '\'' +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", recipientName='" + recipientName + '\'' +
                '}';
    }
}
