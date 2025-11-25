package com.example.stockpilot;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

 

import okhttp3.MediaType;
import okhttp3.RequestBody;
 

import com.example.stockpilot.R;
import com.example.stockpilot.ShipmentItemAdapter;
import com.example.stockpilot.Product;
import com.example.stockpilot.ShipmentItem;
import com.example.stockpilot.UserSession;
import com.example.stockpilot.Constants;
import com.example.stockpilot.ApiUrls;
import com.example.stockpilot.AuthInterceptor;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddShipmentActivity extends AppCompatActivity {

    private static final String TAG = "AddShipmentActivity";

    // UI Components
    private ImageView ivBack;
    private TextView tvStoreName;
    private TextInputEditText etShipmentNumber;
    private AutoCompleteTextView actvOrderType;
    private TextInputEditText etOrderId;
    private TextInputEditText etCarrierName;
    private TextInputEditText etTrackingNumber;
    private TextInputEditText etShippingMethod; // Changed from AutoCompleteTextView to TextInputEditText
    private TextInputEditText etShippingCost;
    private TextInputEditText etShipDate;
    private TextInputEditText etEstimatedDeliveryDate;
    private TextInputEditText etActualDeliveryDate;
    private AutoCompleteTextView actvStatus;
    private TextInputEditText etRecipientName;
    private TextInputEditText etRecipientPhone;
    private TextInputEditText etRecipientAddress;
    private ImageView btnAddItem;
    private RecyclerView rvShipmentItems;
    private TextView tvEmptyItems;
    private TextInputEditText etNotes;
    private Button btnCancel;
    private Button btnSaveShipment;

    // Data
    private int storeId;
    private String storeName;
    private String apiUrl;
    private List<Product> productList;
    private ShipmentItemAdapter shipmentItemAdapter;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shipment);

        // Initialize formatters
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        

        // Initialize UI components
        initializeViews();

        // Get user session data
        getUserSessionData();

        // Set up RecyclerView
        setupRecyclerView();

        // Set up spinners/dropdowns
        setupDropdowns();

        // Set up date pickers
        setupDatePickers();

        // Set up click listeners
        setupClickListeners();

        // Load products for item selection
        loadProducts();

        // Generate shipment number
        generateShipmentNumber();
    }

    private void initializeViews() {
        ivBack = findViewById(R.id.iv_back);
        tvStoreName = findViewById(R.id.tv_store_name);
        etShipmentNumber = findViewById(R.id.et_shipment_number);
        actvOrderType = findViewById(R.id.spinner_order_type);
        etOrderId = findViewById(R.id.et_order_id);
        etCarrierName = findViewById(R.id.et_carrier_name);
        etTrackingNumber = findViewById(R.id.et_tracking_number);
        etShippingMethod = findViewById(R.id.et_shipping_method); // Fixed - now correctly casting to TextInputEditText
        etShippingCost = findViewById(R.id.et_shipping_cost);
        etShipDate = findViewById(R.id.et_ship_date);
        etEstimatedDeliveryDate = findViewById(R.id.et_estimated_delivery_date);
        etActualDeliveryDate = findViewById(R.id.et_actual_delivery_date);
        actvStatus = findViewById(R.id.spinner_status);
        etRecipientName = findViewById(R.id.et_recipient_name);
        etRecipientPhone = findViewById(R.id.et_recipient_phone);
        etRecipientAddress = findViewById(R.id.et_recipient_address);
        btnAddItem = findViewById(R.id.btn_add_item);
        rvShipmentItems = findViewById(R.id.rv_shipment_items);
        tvEmptyItems = findViewById(R.id.tv_empty_items);
        etNotes = findViewById(R.id.et_notes);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSaveShipment = findViewById(R.id.btn_save_shipment);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        String apiBase = sharedPreferences.getString(Constants.API_URL, Constants.DEFAULT_API_URL);
        ApiUrls.setBaseUrl(apiBase);
        AuthInterceptor.setTokenProvider(() -> UserSession.getInstance(this).getToken());
    }

    private void getUserSessionData() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        storeId = sharedPreferences.getInt(Constants.STORE_ID, 1);
        storeName = sharedPreferences.getString(Constants.STORE_NAME, "Store");
        apiUrl = sharedPreferences.getString(Constants.API_URL, Constants.DEFAULT_API_URL);

        // Set store name in the UI
        tvStoreName.setText(storeName);
    }

    private void setupRecyclerView() {
        rvShipmentItems.setLayoutManager(new LinearLayoutManager(this));
        shipmentItemAdapter = new ShipmentItemAdapter(this, position -> {
            // Handle remove item click
            shipmentItemAdapter.removeItem(position);
            updateItemsVisibility();
        });
        rvShipmentItems.setAdapter(shipmentItemAdapter);
        updateItemsVisibility();
    }

    private void updateItemsVisibility() {
        if (shipmentItemAdapter.isEmpty()) {
            rvShipmentItems.setVisibility(View.GONE);
            tvEmptyItems.setVisibility(View.VISIBLE);
        } else {
            rvShipmentItems.setVisibility(View.VISIBLE);
            tvEmptyItems.setVisibility(View.GONE);
        }
    }

    private void setupDropdowns() {
        // Order Type dropdown
        String[] orderTypes = {"sales_order", "purchase_return", "other"};
        ArrayAdapter<String> orderTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, orderTypes);
        actvOrderType.setAdapter(orderTypeAdapter);
        actvOrderType.setText(orderTypes[0], false);

        // Status dropdown
        String[] statuses = {"pending", "shipped", "delivered", "cancelled"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, statuses);
        actvStatus.setAdapter(statusAdapter);
        actvStatus.setText(statuses[0], false);

        // Note: Shipping method is now a regular TextInputEditText, so no dropdown setup needed
        // The default value "standard" is already set in the XML layout
    }

    private void setupDatePickers() {
        // Ship Date picker
        setupDatePicker(etShipDate);

        // Estimated Delivery Date picker
        setupDatePicker(etEstimatedDeliveryDate);

        // Actual Delivery Date picker
        setupDatePicker(etActualDeliveryDate);
    }

    private void setupDatePicker(TextInputEditText dateField) {
        dateField.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AddShipmentActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);
                        dateField.setText(dateFormatter.format(selectedDate.getTime()));
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Set current date as default for ship date
        if (dateField == etShipDate) {
            etShipDate.setText(dateFormatter.format(Calendar.getInstance().getTime()));
        }
    }

    private void setupClickListeners() {
        // Back button click
        ivBack.setOnClickListener(v -> finish());

        // Cancel button click
        btnCancel.setOnClickListener(v -> finish());

        // Add Item button click
        btnAddItem.setOnClickListener(v -> showAddItemDialog());

        // Save Shipment button click
        btnSaveShipment.setOnClickListener(v -> validateAndSaveShipment());

        // Shipping cost calculation
        etShippingCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Nothing to do here for now
            }
        });
    }

    private void loadProducts() {
        productList = new ArrayList<>();
        ApiUrls.getApiService().getProductSelection(String.valueOf(storeId))
                .enqueue(new retrofit2.Callback<java.util.List<java.util.Map<String, Object>>>() {
                    @Override
                    public void onResponse(retrofit2.Call<java.util.List<java.util.Map<String, Object>>> call, retrofit2.Response<java.util.List<java.util.Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Map<String, Object>> items = response.body();
                            productList.clear();
                            for (Map<String, Object> obj : items) {
                                int id = parseInt(obj.get("id"));
                                String name = asString(obj.get("product_name"));
                                String sku = asString(obj.get("sku"));
                                String category = asString(obj.get("category"));
                                int stockQty = parseInt(obj.get("stock_quantity"));
                                double price = parseDouble(obj.get("selling_price"));
                                productList.add(new Product(id, name, sku, category, stockQty, price));
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<java.util.List<java.util.Map<String, Object>>> call, Throwable t) {
                    }
                });
    }

    private int parseInt(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return 0; }
    }

    private double parseDouble(Object v) {
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return 0; }
    }

    private String asString(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private void generateShipmentNumber() {
        // Generate a shipment number based on current timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        String shipmentNumber = "SHP" + timestamp.substring(timestamp.length() - 8);
        etShipmentNumber.setText(shipmentNumber);
    }

    private void showAddItemDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_shipment_item);
        dialog.setCancelable(true);

        // Defensive check for productList
        if (productList == null || productList.isEmpty()) {
            Toast.makeText(this, "No products available", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Initialize dialog views
        AutoCompleteTextView spinnerProduct = dialog.findViewById(R.id.spinner_product);
        TextInputEditText etQuantity = dialog.findViewById(R.id.et_quantity);
        TextInputEditText etUnitPrice = dialog.findViewById(R.id.et_unit_price);
        TextInputEditText etBatchId = dialog.findViewById(R.id.et_batch_id);
        TextInputEditText etTotalValue = dialog.findViewById(R.id.et_total_value);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel_item);
        Button btnAddItemConfirm = dialog.findViewById(R.id.btn_add_item_confirm);

        // Set up product spinner
        List<String> productNames = new ArrayList<>();
        Map<String, Product> productMap = new HashMap<>();

        for (Product product : productList) {
            String displayName = product.getProductName() + " (" + product.getSku() + ")";
            productNames.add(displayName);
            productMap.put(displayName, product);
        }

        ArrayAdapter<String> productAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, productNames);
        spinnerProduct.setAdapter(productAdapter);

        // Set up quantity and unit price change listeners to calculate total value
        TextWatcher calculateTotalWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateTotalValue(etQuantity, etUnitPrice, etTotalValue);
            }
        };

        etQuantity.addTextChangedListener(calculateTotalWatcher);
        etUnitPrice.addTextChangedListener(calculateTotalWatcher);

        // Set up product selection listener to set default unit price
        spinnerProduct.setOnItemClickListener((parent, view, position, id) -> {
            String selectedProductName = (String) parent.getItemAtPosition(position);
            Product selectedProduct = productMap.get(selectedProductName);
            if (selectedProduct != null) {
                etUnitPrice.setText(String.valueOf(selectedProduct.getSellingPrice()));
                calculateTotalValue(etQuantity, etUnitPrice, etTotalValue);
            }
        });

        // Set up button click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAddItemConfirm.setOnClickListener(v -> {
            // Validate inputs
            String selectedProductName = spinnerProduct.getText().toString();
            String quantityStr = etQuantity.getText() != null ? etQuantity.getText().toString() : "";
            String unitPriceStr = etUnitPrice.getText() != null ? etUnitPrice.getText().toString() : "";

            if (selectedProductName.isEmpty() || quantityStr.isEmpty() || unitPriceStr.isEmpty()) {
                Toast.makeText(AddShipmentActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected product
            Product selectedProduct = productMap.get(selectedProductName);
            if (selectedProduct == null) {
                Toast.makeText(AddShipmentActivity.this, "Please select a valid product", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse values
            int quantity = Integer.parseInt(quantityStr);
            double unitPrice = Double.parseDouble(unitPriceStr);
            String batchId = etBatchId.getText().toString();

            // Create shipment item
            ShipmentItem item = new ShipmentItem(
                    selectedProduct.getProductId(),
                    selectedProduct.getProductName(),
                    selectedProduct.getSku(),
                    quantity,
                    unitPrice,
                    batchId
            );

            // Add to adapter
            shipmentItemAdapter.addItem(item);
            updateItemsVisibility();

            // Dismiss dialog
            dialog.dismiss();
        });

        dialog.show();
    }

    private void calculateTotalValue(TextInputEditText etQuantity, TextInputEditText etUnitPrice, TextInputEditText etTotalValue) {
        try {
            String quantityStr = etQuantity.getText() != null ? etQuantity.getText().toString() : "";
            String unitPriceStr = etUnitPrice.getText() != null ? etUnitPrice.getText().toString() : "";

            if (!quantityStr.isEmpty() && !unitPriceStr.isEmpty()) {
                int quantity = Integer.parseInt(quantityStr);
                double unitPrice = Double.parseDouble(unitPriceStr);
                double totalValue = quantity * unitPrice;

                etTotalValue.setText(currencyFormatter.format(totalValue));
            } else {
                etTotalValue.setText("");
            }
        } catch (NumberFormatException e) {
            etTotalValue.setText("");
        }
    }

    private void validateAndSaveShipment() {
        // Validate required fields
        if (etShipmentNumber.getText().toString().isEmpty() ||
                actvOrderType.getText().toString().isEmpty() ||
                etShipDate.getText().toString().isEmpty() ||
                actvStatus.getText().toString().isEmpty() ||
                shipmentItemAdapter.isEmpty()) {

            Toast.makeText(this, "Please fill all required fields and add at least one item", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create shipment JSON object
        try {
            JSONObject shipmentData = new JSONObject();
            shipmentData.put("store_id", storeId);
            shipmentData.put("shipment_number", etShipmentNumber.getText().toString());
            shipmentData.put("order_type", actvOrderType.getText().toString());

            // Optional order ID
            String orderId = etOrderId.getText().toString();
            if (!orderId.isEmpty()) {
                shipmentData.put("order_id", Integer.parseInt(orderId));
            }

            // Carrier information
            shipmentData.put("carrier_name", etCarrierName.getText().toString());
            shipmentData.put("tracking_number", etTrackingNumber.getText().toString());
            shipmentData.put("shipping_method", etShippingMethod.getText().toString()); // Now correctly accessing TextInputEditText

            // Shipping cost
            String shippingCost = etShippingCost.getText().toString();
            if (!shippingCost.isEmpty()) {
                shipmentData.put("shipping_cost", Double.parseDouble(shippingCost));
            }

            // Dates
            shipmentData.put("ship_date", etShipDate.getText().toString());
            shipmentData.put("estimated_delivery_date", etEstimatedDeliveryDate.getText().toString());
            shipmentData.put("actual_delivery_date", etActualDeliveryDate.getText().toString());

            // Status
            shipmentData.put("status", actvStatus.getText().toString());

            // Recipient information
            shipmentData.put("recipient_name", etRecipientName.getText().toString());
            shipmentData.put("recipient_phone", etRecipientPhone.getText().toString());
            shipmentData.put("recipient_address", etRecipientAddress.getText().toString());

            // Notes
            shipmentData.put("notes", etNotes.getText().toString());

            // Create shipment items array
            JSONArray itemsArray = new JSONArray();
            List<ShipmentItem> items = shipmentItemAdapter.getAllItems();

            for (ShipmentItem item : items) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("product_id", item.getProductId());
                itemObj.put("quantity_shipped", item.getQuantityShipped());
                itemObj.put("unit_price", item.getUnitPrice());
                itemObj.put("batch_id", item.getBatchId());
                itemsArray.put(itemObj);
            }

            shipmentData.put("items", itemsArray);

            // Send to API
            saveShipment(shipmentData);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating shipment JSON: " + e.getMessage());
            Toast.makeText(this, "Error creating shipment data", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveShipment(JSONObject shipmentData) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, shipmentData.toString());
        ApiUrls.getApiService().addShipment(requestBody).enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddShipmentActivity.this, "Shipment saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddShipmentActivity.this, Constants.ERROR_SERVER, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                Toast.makeText(AddShipmentActivity.this, Constants.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
