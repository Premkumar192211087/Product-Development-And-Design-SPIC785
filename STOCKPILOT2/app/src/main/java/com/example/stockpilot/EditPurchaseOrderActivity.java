package com.example.stockpilot;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonElement;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.PurchaseOrderModel;
import com.example.stockpilot.PurchaseOrderItemModel;
import com.example.stockpilot.VendorModel;
 
 
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

 

/**
 * Activity for editing purchase orders
 */
public class EditPurchaseOrderActivity extends AppCompatActivity {

    private TextInputLayout tilPoNumber, tilVendor, tilPoDate, tilExpectedDate;
    private TextInputLayout tilSubtotal, tilTax, tilDiscount, tilTotal, tilNotes;
    private TextInputEditText etPoNumber, etPoDate, etExpectedDate;
    private TextInputEditText etSubtotal, etTax, etDiscount, etTotal, etNotes;
    private AutoCompleteTextView spinnerVendor, spinnerStatus;
    private RecyclerView rvPoItems;
    private Button btnAddItem;
    private FloatingActionButton fabSave;

    
    private String poId;
    private PurchaseOrderModel purchaseOrder;
    private List<PurchaseOrderItemModel> itemList;
    private PurchaseOrderItemsAdapter itemsAdapter;
    private List<VendorModel> vendorList;
    private Map<String, String> vendorMap; // Maps vendor name to ID
    private UserSession session;

    private SimpleDateFormat apiDateFormat;
    private SimpleDateFormat displayDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_purchase_order);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize date formats
        apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        // Initialize views
        initViews();

        // Get purchase order ID from intent
        poId = getIntent().getStringExtra("po_id");
        if (poId == null || poId.isEmpty()) {
            Toast.makeText(this, R.string.error_loading_purchase_order, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize request queue
        
        session = UserSession.getInstance(this);

        // Initialize vendor map
        vendorMap = new HashMap<>();

        // Initialize item list and adapter
        itemList = new ArrayList<>();
        itemsAdapter = new PurchaseOrderItemsAdapter(itemList, new PurchaseOrderItemsAdapter.OnItemActionListener() {
            @Override
            public void onItemClick(PurchaseOrderItemModel item, int position) {
                showAddEditItemDialog(item, position);
            }

            @Override
            public void onRemoveItem(PurchaseOrderItemModel item, int position) {
                removeItem(position);
            }
        }, true); // Editable in edit view

        rvPoItems.setLayoutManager(new LinearLayoutManager(this));
        rvPoItems.setAdapter(itemsAdapter);

        // Set up listeners
        setupListeners();

        // Load data
        loadVendors();
        loadPurchaseOrderData();
    }

    /**
     * Initialize views
     */
    private void initViews() {
        tilPoNumber = findViewById(R.id.til_po_number);
        tilVendor = findViewById(R.id.til_vendor);
        tilPoDate = findViewById(R.id.til_po_date);
        tilExpectedDate = findViewById(R.id.til_expected_date);
        tilSubtotal = findViewById(R.id.til_subtotal);
        tilTax = findViewById(R.id.til_tax);
        tilDiscount = findViewById(R.id.til_discount);
        tilTotal = findViewById(R.id.til_total);
        tilNotes = findViewById(R.id.til_notes);

        etPoNumber = findViewById(R.id.et_po_number);
        spinnerVendor = findViewById(R.id.spinner_vendor);
        spinnerStatus = findViewById(R.id.spinner_status);
        etPoDate = findViewById(R.id.et_po_date);
        etExpectedDate = findViewById(R.id.et_expected_date);
        etSubtotal = findViewById(R.id.et_subtotal);
        etTax = findViewById(R.id.et_tax);
        etDiscount = findViewById(R.id.et_discount);
        etTotal = findViewById(R.id.et_total);
        etNotes = findViewById(R.id.et_notes);

        rvPoItems = findViewById(R.id.rv_po_items);
        btnAddItem = findViewById(R.id.btn_add_item);
        fabSave = findViewById(R.id.fab_save);

        // Set up status spinner
        String[] statusOptions = getResources().getStringArray(R.array.purchase_order_status_options);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, statusOptions);
        spinnerStatus.setAdapter(statusAdapter);
    }

    /**
     * Set up listeners for views
     */
    private void setupListeners() {
        // Date pickers
        etPoDate.setOnClickListener(v -> showDatePicker(etPoDate));
        etExpectedDate.setOnClickListener(v -> showDatePicker(etExpectedDate));

        // Add item button
        btnAddItem.setOnClickListener(v -> showAddEditItemDialog(null, -1));

        // Save button
        fabSave.setOnClickListener(v -> validateAndSave());

        // Financial calculations
        TextWatcher financialWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateTotal();
            }
        };

        etTax.addTextChangedListener(financialWatcher);
        etDiscount.addTextChangedListener(financialWatcher);
    }

    /**
     * Show date picker dialog
     *
     * @param editText The EditText to update with selected date
     */
    private void showDatePicker(final TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();

        // If there's already a date in the field, use it as the initial date
        if (editText.getText() != null && !editText.getText().toString().isEmpty()) {
            try {
                Date date = displayDateFormat.parse(editText.getText().toString());
                if (date != null) {
                    calendar.setTime(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    editText.setText(displayDateFormat.format(calendar.getTime()));
                }, year, month, day);

        datePickerDialog.show();
    }

    /**
     * Load vendors from API
     */
    private void loadVendors() {
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Parse vendors data from API response
     *
     * @param vendorsArray List of vendor data maps
     */
    private void parseVendorsData(List<Vendor> vendors) {
        vendorList = new ArrayList<>();
        List<String> vendorNames = new ArrayList<>();

        for (Vendor vendor : vendors) {
            vendorList.add(new VendorModel(vendor.getId(), vendor.getName(), vendor.getContactPerson(), vendor.getEmail(), vendor.getPhone(), vendor.getAddress(), vendor.getCity(), vendor.getState(), vendor.getZipCode(), vendor.getCountry()));
            vendorNames.add(vendor.getName());
            vendorMap.put(vendor.getName(), vendor.getId());
        }

        // Set up vendor spinner
        ArrayAdapter<String> vendorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, vendorNames);
        spinnerVendor.setAdapter(vendorAdapter);
    }

    /**
     * Load purchase order data from API
     */
    private void loadPurchaseOrderData() {
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Parse purchase order data from JSON
     *
     * @param poData JSON object containing purchase order data
     * @throws JSONException If JSON parsing fails
     */
    private void parsePurchaseOrderData(Map<String, Object> poData) {
        // Create purchase order model
        purchaseOrder = new PurchaseOrderModel(
                (String) poData.get("id"),
                (String) poData.get("po_number"),
                (String) poData.get("vendor_id"),
                (String) poData.get("vendor_name"),
                (String) poData.get("store_id"),
                (String) poData.get("po_date"),
                (String) poData.get("expected_date"),
                (String) poData.get("status"),
                ((Number) poData.get("subtotal")).doubleValue(),
                ((Number) poData.get("tax")).doubleValue(),
                ((Number) poData.get("discount")).doubleValue(),
                ((Number) poData.get("total")).doubleValue(),
                poData.containsKey("notes") ? (String) poData.get("notes") : "",
                (String) poData.get("created_at"),
                (String) poData.get("updated_at")
        );

        // Display purchase order data
        displayPurchaseOrderData();
    }

    /**
     * Parse items data from JSON array
     *
     * @param itemsArray JSON array containing items data
     * @throws JSONException If JSON parsing fails
     */
    private void parseItemsData(List<Map<String, Object>> itemsArray) {
        itemList.clear();

        for (Map<String, Object> itemData : itemsArray) {
            PurchaseOrderItemModel item = new PurchaseOrderItemModel(
                    (String) itemData.get("id"),
                    (String) itemData.get("po_id"),
                    (String) itemData.get("item_name"),
                    itemData.containsKey("item_description") ? (String) itemData.get("item_description") : "",
                    ((Number) itemData.get("quantity")).doubleValue(),
                    ((Number) itemData.get("unit_price")).doubleValue(),
                    ((Number) itemData.get("total")).doubleValue()
            );

            itemList.add(item);
        }

        itemsAdapter.notifyDataSetChanged();
        calculateSubtotal();
    }

    /**
     * Display purchase order data in views
     */
    private void displayPurchaseOrderData() {
        // Set basic info
        etPoNumber.setText(purchaseOrder.getPoNumber());
        spinnerVendor.setText(purchaseOrder.getVendorName(), false);
        spinnerStatus.setText(purchaseOrder.getStatus(), false);

        // Set dates
        try {
            Date poDate = apiDateFormat.parse(purchaseOrder.getPoDate());
            if (poDate != null) {
                etPoDate.setText(displayDateFormat.format(poDate));
            }

            Date expectedDate = apiDateFormat.parse(purchaseOrder.getExpectedDate());
            if (expectedDate != null) {
                etExpectedDate.setText(displayDateFormat.format(expectedDate));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Set financial info
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        etSubtotal.setText(String.format(Locale.US, "%.2f", purchaseOrder.getSubtotal()));
        etTax.setText(String.format(Locale.US, "%.2f", purchaseOrder.getTax()));
        etDiscount.setText(String.format(Locale.US, "%.2f", purchaseOrder.getDiscount()));
        etTotal.setText(String.format(Locale.US, "%.2f", purchaseOrder.getTotal()));

        // Set notes
        if (purchaseOrder.getNotes() != null) {
            etNotes.setText(purchaseOrder.getNotes());
        }
    }

    /**
     * Show dialog to add or edit an item
     *
     * @param item     Item to edit, or null for a new item
     * @param position Position of item in list, or -1 for a new item
     */
    private void showAddEditItemDialog(PurchaseOrderItemModel item, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_item, null);
        builder.setView(dialogView);

        // Get dialog views
        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextInputEditText etItemName = dialogView.findViewById(R.id.et_item_name);
        TextInputEditText etItemDescription = dialogView.findViewById(R.id.et_item_description);
        TextInputEditText etQuantity = dialogView.findViewById(R.id.et_quantity);
        TextInputEditText etUnitPrice = dialogView.findViewById(R.id.et_unit_price);
        TextInputEditText etItemTotal = dialogView.findViewById(R.id.et_total);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        // Set dialog title based on whether we're adding or editing
        tvDialogTitle.setText(item == null ? R.string.add_item : R.string.edit_item);

        // If editing, populate fields with item data
        if (item != null) {
            etItemName.setText(item.getItemName());
            etItemDescription.setText(item.getItemDescription());
            etQuantity.setText(String.format(Locale.US, "%.2f", item.getQuantity()));
            etUnitPrice.setText(String.format(Locale.US, "%.2f", item.getUnitPrice()));
            etItemTotal.setText(String.format(Locale.US, "%.2f", item.getTotal()));
        }

        // Set up listeners for quantity and unit price to calculate total
        TextWatcher itemTotalWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double quantity = etQuantity.getText() != null && !etQuantity.getText().toString().isEmpty() ?
                            Double.parseDouble(etQuantity.getText().toString()) : 0;
                    double unitPrice = etUnitPrice.getText() != null && !etUnitPrice.getText().toString().isEmpty() ?
                            Double.parseDouble(etUnitPrice.getText().toString()) : 0;
                    double total = quantity * unitPrice;

                    etItemTotal.setText(String.format(Locale.US, "%.2f", total));
                } catch (NumberFormatException e) {
                    etItemTotal.setText("0.00");
                }
            }
        };

        etQuantity.addTextChangedListener(itemTotalWatcher);
        etUnitPrice.addTextChangedListener(itemTotalWatcher);

        // Create and show dialog
        AlertDialog dialog = builder.create();

        // Set up button listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            // Validate fields
            if (etItemName.getText() == null || etItemName.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, R.string.error_item_name_required, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double quantity = etQuantity.getText() != null && !etQuantity.getText().toString().isEmpty() ?
                        Double.parseDouble(etQuantity.getText().toString()) : 0;
                double unitPrice = etUnitPrice.getText() != null && !etUnitPrice.getText().toString().isEmpty() ?
                        Double.parseDouble(etUnitPrice.getText().toString()) : 0;

                if (quantity <= 0) {
                    Toast.makeText(this, R.string.error_quantity_required, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (unitPrice <= 0) {
                    Toast.makeText(this, R.string.error_unit_price_required, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create or update item
                String itemName = etItemName.getText().toString().trim();
                String itemDescription = etItemDescription.getText() != null ?
                        etItemDescription.getText().toString().trim() : "";

                if (item == null) {
                    // Add new item
                    PurchaseOrderItemModel newItem = new PurchaseOrderItemModel(
                            itemName, itemDescription, quantity, unitPrice);
                    itemList.add(newItem);
                    itemsAdapter.notifyItemInserted(itemList.size() - 1);
                } else {
                    // Update existing item
                    item.setItemName(itemName);
                    item.setItemDescription(itemDescription);
                    item.setQuantity(quantity);
                    item.setUnitPrice(unitPrice);
                    item.calculateTotal();
                    itemsAdapter.notifyItemChanged(position);
                }

                // Recalculate subtotal
                calculateSubtotal();

                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_invalid_number, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    /**
     * Remove an item from the list
     *
     * @param position Position of item to remove
     */
    private void removeItem(int position) {
        if (position >= 0 && position < itemList.size()) {
            itemList.remove(position);
            itemsAdapter.notifyItemRemoved(position);
            itemsAdapter.notifyItemRangeChanged(position, itemList.size());
            calculateSubtotal();
        }
    }

    /**
     * Calculate subtotal based on items
     */
    private void calculateSubtotal() {
        double subtotal = 0;
        for (PurchaseOrderItemModel item : itemList) {
            subtotal += item.getTotal();
        }

        etSubtotal.setText(String.format(Locale.US, "%.2f", subtotal));
        calculateTotal();
    }

    /**
     * Calculate total based on subtotal, tax, and discount
     */
    private void calculateTotal() {
        try {
            double subtotal = etSubtotal.getText() != null && !etSubtotal.getText().toString().isEmpty() ?
                    Double.parseDouble(etSubtotal.getText().toString()) : 0;
            double tax = etTax.getText() != null && !etTax.getText().toString().isEmpty() ?
                    Double.parseDouble(etTax.getText().toString()) : 0;
            double discount = etDiscount.getText() != null && !etDiscount.getText().toString().isEmpty() ?
                    Double.parseDouble(etDiscount.getText().toString()) : 0;

            double total = subtotal + tax - discount;
            etTotal.setText(String.format(Locale.US, "%.2f", total));
        } catch (NumberFormatException e) {
            etTotal.setText("0.00");
        }
    }

    /**
     * Validate fields and save purchase order
     */
    private void validateAndSave() {
        // Validate PO Number
        if (etPoNumber.getText() == null || etPoNumber.getText().toString().trim().isEmpty()) {
            tilPoNumber.setError(getString(R.string.error_po_number_required));
            etPoNumber.requestFocus();
            return;
        } else {
            tilPoNumber.setError(null);
        }

        // Validate Vendor
        String vendorName = spinnerVendor.getText().toString();
        if (vendorName.trim().isEmpty() || !vendorMap.containsKey(vendorName)) {
            tilVendor.setError(getString(R.string.error_vendor_required));
            spinnerVendor.requestFocus();
            return;
        } else {
            tilVendor.setError(null);
        }

        // Validate PO Date
        if (etPoDate.getText() == null || etPoDate.getText().toString().trim().isEmpty()) {
            tilPoDate.setError(getString(R.string.error_po_date_required));
            etPoDate.requestFocus();
            return;
        } else {
            tilPoDate.setError(null);
        }

        // Validate Expected Date
        if (etExpectedDate.getText() == null || etExpectedDate.getText().toString().trim().isEmpty()) {
            tilExpectedDate.setError(getString(R.string.error_expected_date_required));
            etExpectedDate.requestFocus();
            return;
        } else {
            tilExpectedDate.setError(null);
        }

        // Prepare data for saving
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("po_id", poId); // Include po_id in the request body
        requestBody.put("po_number", etPoNumber.getText().toString().trim());
        requestBody.put("vendor_id", vendorMap.get(vendorName));
        requestBody.put("status", spinnerStatus.getText().toString());
        requestBody.put("notes", etNotes.getText() != null ? etNotes.getText().toString().trim() : "");

        // Format dates for API
        try {
            Date poDate = displayDateFormat.parse(etPoDate.getText().toString());
            if (poDate != null) {
                requestBody.put("po_date", apiDateFormat.format(poDate));
            }

            Date expectedDate = displayDateFormat.parse(etExpectedDate.getText().toString());
            if (expectedDate != null) {
                requestBody.put("expected_date", apiDateFormat.format(expectedDate));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_invalid_date_format, Toast.LENGTH_SHORT).show();
            return;
        }

        // Add financial data
        try {
            requestBody.put("subtotal", etSubtotal.getText() != null && !etSubtotal.getText().toString().isEmpty() ? Double.parseDouble(etSubtotal.getText().toString()) : 0.0);
            requestBody.put("tax", etTax.getText() != null && !etTax.getText().toString().isEmpty() ? Double.parseDouble(etTax.getText().toString()) : 0.0);
            requestBody.put("discount", etDiscount.getText() != null && !etDiscount.getText().toString().isEmpty() ? Double.parseDouble(etDiscount.getText().toString()) : 0.0);
            requestBody.put("total", etTotal.getText() != null && !etTotal.getText().toString().isEmpty() ? Double.parseDouble(etTotal.getText().toString()) : 0.0);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_invalid_financials, Toast.LENGTH_SHORT).show();
            return;
        }

        // Add items
        List<Map<String, Object>> itemsData = new ArrayList<>();
        for (PurchaseOrderItemModel item : itemList) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.getId()); // Include item ID for existing items
            itemMap.put("item_name", item.getItemName());
            itemMap.put("item_description", item.getItemDescription());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("unit_price", item.getUnitPrice());
            itemsData.add(itemMap);
        }
        requestBody.put("items", itemsData);

        // Make API call to update purchase order
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}