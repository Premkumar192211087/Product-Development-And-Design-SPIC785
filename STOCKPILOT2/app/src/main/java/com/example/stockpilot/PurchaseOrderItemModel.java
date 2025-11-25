package com.example.stockpilot;

/**
 * Model class for purchase order items
 */
public class PurchaseOrderItemModel {
    private String itemId;
    private String poId;
    private String itemName;
    private String itemDescription;
    private double quantity;
    private double unitPrice;
    private double total;

    /**
     * Constructor for creating a new item (without ID)
     *
     * @param itemName Item name
     * @param itemDescription Item description
     * @param quantity Quantity
     * @param unitPrice Unit price
     */
    public PurchaseOrderItemModel(String itemName, String itemDescription, double quantity, double unitPrice) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateTotal();
    }

    /**
     * Constructor for existing items (with ID)
     *
     * @param itemId Item ID
     * @param poId Purchase order ID
     * @param itemName Item name
     * @param itemDescription Item description
     * @param quantity Quantity
     * @param unitPrice Unit price
     * @param total Total price
     */
    public PurchaseOrderItemModel(String itemId, String poId, String itemName, String itemDescription,
                                     double quantity, double unitPrice, double total) {
        this.itemId = itemId;
        this.poId = poId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = total;
    }

    /**
     * Calculate total based on quantity and unit price
     */
    public void calculateTotal() {
        this.total = this.quantity * this.unitPrice;
    }

    // Getters and setters
    public String getId() {
        return itemId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getPoId() {
        return poId;
    }

    public void setPoId(String poId) {
        this.poId = poId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
        calculateTotal();
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotal();
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}