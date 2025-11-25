// AdjustmentItem.java
package com.example.stockpilot;

import com.google.gson.annotations.SerializedName;

// This class represents a single inventory adjustment item fetched from the PHP API.
// It's a data model to easily parse the JSON response and use in your Android app.
public class AdjustmentItem {

    // @SerializedName is used to map JSON keys to Java field names,
    // especially if JSON keys are different or use snake_case.
    @SerializedName("movement_id")
    public int movement_id;

    @SerializedName("product_name")
    public String product_name;

    @SerializedName("movement_type")
    public String movement_type;

    // The PHP script provides a 'formatted_quantity' for display (+/- prefix)
    @SerializedName("formatted_quantity")
    public String formatted_quantity;

    // You can also keep the original 'quantity_moved' if needed for calculations
    // @SerializedName("quantity_moved")
    // public int quantity_moved;

    @SerializedName("reference_type")
    public String reference_type;

    @SerializedName("movement_date")
    public String movement_date; // Consider using a Date object and SimpleDateFormat for better handling

    @SerializedName("unit_price")
    public float unit_price;

    @SerializedName("performed_by")
    public String performed_by;

    // You can add a constructor, getters, and setters as needed for your application logic
    public AdjustmentItem() {
        // Default constructor
    }

    // Example constructor for creating an object (optional)
    public AdjustmentItem(int movement_id, String product_name, String movement_type,
                          String formatted_quantity, String reference_type,
                          String movement_date, float unit_price, String performed_by) {
        this.movement_id = movement_id;
        this.product_name = product_name;
        this.movement_type = movement_type;
        this.formatted_quantity = formatted_quantity;
        this.reference_type = reference_type;
        this.movement_date = movement_date;
        this.unit_price = unit_price;
        this.performed_by = performed_by;
    }

    // Example getter (optional)
    public String getProductName() {
        return product_name;
    }

    // You would place this file in your Android project, typically within a 'models' or 'data' package.
    // For example: `app/src/main/java/com/example/stockpilot/models/AdjustmentItem.java`
}
