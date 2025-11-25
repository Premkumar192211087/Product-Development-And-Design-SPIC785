
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
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

 
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
 * Activity for adding new purchase orders
 */
public class AddPurchaseOrderActivity extends AppCompatActivity {

    private TextInputLayout tilPoNumber, tilVendor, tilPoDate, tilExpectedDate;
    private TextInputLayout tilSubtotal, tilTax, tilDiscount, tilTotal, tilNotes;
    private TextInputEditText etPoNumber, etPoDate, etExpectedDate;
    private TextInputEditText etSubtotal, etTax, etDiscount, etTotal, etNotes;
    private AutoCompleteTextView spinnerVendor, spinnerStatus;
    private RecyclerView rvPoItems;
    private Button btnAddItem;
    private FloatingActionButton fabSave;
    private CheckBox cbGenerateBill;

    
    private List<PurchaseOrderItemModel> itemList;
    private PurchaseOrderItemsAdapter itemsAdapter;
    private List<VendorModel> vendorList;
    private Map<String, String> vendorMap; // Maps vendor name to ID

    private SimpleDateFormat apiDateFormat;
    private SimpleDateFormat displayDateFormat;
    private UserSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_purchase_order);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_add_po);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.add_purchase_order);
        }
        
        // Initialize checkbox for automatic bill generation
        cbGenerateBill = findViewById(R.id.cb_generate_bill);

        // Initialize date formats
        apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        // Initialize user session
        session = UserSession.getInstance(this);

        // Initialize views
        initViews();

        

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
        }, true); // Editable in add view

        rvPoItems.setLayoutManager(new LinearLayoutManager(this));
        rvPoItems.setAdapter(itemsAdapter);

        // Set up listeners
        setupListeners();

        // Set default values
        setDefaultValues();

        // Load vendors
        loadVendors();
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
     * Set default values for new purchase order
     */
    private void setDefaultValues() {
        // Set default status to "Draft"
        spinnerStatus.setText(getString(R.string.status_draft), false); // Use string resource

        // Set default dates to today and a week from today
        Calendar calendar = Calendar.getInstance();
        etPoDate.setText(displayDateFormat.format(calendar.getTime()));

        // Add 7 days for expected date
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        etExpectedDate.setText(displayDateFormat.format(calendar.getTime()));

        // Set default financial values
        etSubtotal.setText(getString(R.string.default_amount));
        etTax.setText(getString(R.string.default_amount));
        etDiscount.setText(getString(R.string.default_amount));
        etTotal.setText(getString(R.string.default_amount));

        // Generate PO ID
        generatePoNumber();
    }
    
    /**
     * Generate a unique PO number based on timestamp and store ID
     */
    private void generatePoNumber() {
        // Get current timestamp
        long timestamp = System.currentTimeMillis();
        
        // Get store ID from user session
        String storeId = String.valueOf(session.getStoreId());
        String storeName = session.getStoreName();
        
        // Get current year
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        
        // Generate PO number format: PO-{STORE_ID}-{YEAR}-{TIMESTAMP}
        String poNumber = "PO-" + storeId + "-" + year + "-" + timestamp % 10000;
        
        // Set the generated PO number
        etPoNumber.setText(poNumber);
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
     * Parse vendors data from Retrofit response
     *
     * @param vendorsArray List containing vendor data
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
        if (spinnerVendor.getText() == null || spinnerVendor.getText().toString().trim().isEmpty()) {
            tilVendor.setError(getString(R.string.error_vendor_required));
            spinnerVendor.requestFocus();
            return;
        } else {
            tilVendor.setError(null);
        }

        // Validate Status
        if (spinnerStatus.getText() == null || spinnerStatus.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.error_status_required, Toast.LENGTH_SHORT).show();
            spinnerStatus.requestFocus();
            return;
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

        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Save purchase order to API
     */
    private void savePurchaseOrder() {
        String poNumber = etPoNumber.getText() != null ? etPoNumber.getText().toString().trim() : "";
        String vendor = spinnerVendor.getText() != null ? spinnerVendor.getText().toString().trim() : "";
        String poDate = etPoDate.getText() != null ? etPoDate.getText().toString().trim() : "";
        String expectedDate = etExpectedDate.getText() != null ? etExpectedDate.getText().toString().trim() : "";
        String subtotal = etSubtotal.getText() != null ? etSubtotal.getText().toString().trim() : "";
        String tax = etTax.getText() != null ? etTax.getText().toString().trim() : "";
        String discount = etDiscount.getText() != null ? etDiscount.getText().toString().trim() : "";
        String total = etTotal.getText() != null ? etTotal.getText().toString().trim() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        try {
            // Create JSON request body
            JSONObject requestBody = new JSONObject();

            // Basic info
            requestBody.put("po_number", poNumber);
            requestBody.put("vendor_id", vendorMap.get(vendor));
            requestBody.put("status", spinnerStatus.getText().toString());

            // Dates
            try {
                Date poDateObj = displayDateFormat.parse(poDate);
                if (poDateObj != null) {
                    requestBody.put("po_date", apiDateFormat.format(poDateObj));
                }

                Date expectedDateObj = displayDateFormat.parse(expectedDate);
                if (expectedDateObj != null) {
                    requestBody.put("expected_date", apiDateFormat.format(expectedDateObj));
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.error_invalid_date, Toast.LENGTH_SHORT).show();
                return;
            }

            // Financial info
            requestBody.put("subtotal", Double.parseDouble(subtotal));
            requestBody.put("tax", Double.parseDouble(tax));
            requestBody.put("discount", Double.parseDouble(discount));
            requestBody.put("total", Double.parseDouble(total));

            // Notes
            if (notes != null && !notes.isEmpty()) {
                requestBody.put("notes", notes);
            }

            // Generate bill flag
            requestBody.put("generate_bill", cbGenerateBill.isChecked());

            // Items
            JSONArray itemsArray = new JSONArray();
            for (PurchaseOrderItemModel item : itemList) {
                JSONObject itemObject = new JSONObject();
                itemObject.put("item_name", item.getItemName());
                itemObject.put("item_description", item.getItemDescription());
                itemObject.put("quantity", item.getQuantity());
                itemObject.put("unit_price", item.getUnitPrice());
                itemObject.put("total", item.getTotal());
                itemsArray.put(itemObject);
            }
            requestBody.put("items", itemsArray);

            Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();

        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_creating_request, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Create a bill from purchase order data
     * @param poId Purchase order ID
     * @param poRequestBody Purchase order request body
     */
    private void createBillFromPurchaseOrder(String poId, JSONObject poRequestBody) {
        try {
            // Create bill request body
            JSONObject billRequestBody = new JSONObject();
            
            // Copy relevant data from purchase order
            billRequestBody.put("po_id", poId);
            billRequestBody.put("vendor_id", poRequestBody.getString("vendor_id"));
            billRequestBody.put("subtotal", poRequestBody.getDouble("subtotal"));
            billRequestBody.put("tax", poRequestBody.getDouble("tax"));
            billRequestBody.put("discount", poRequestBody.getDouble("discount"));
            billRequestBody.put("total", poRequestBody.getDouble("total"));
            
            // Generate bill number
            String year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
            String billNumber = "BILL-" + year + "-" + poId;
            billRequestBody.put("bill_number", billNumber);
            
            // Set bill date to current date
            String currentDate = apiDateFormat.format(new Date());
            billRequestBody.put("bill_date", currentDate);
            
            // Set due date (30 days from now)
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 30);
            String dueDate = apiDateFormat.format(calendar.getTime());
            billRequestBody.put("due_date", dueDate);
            
            // Copy items
            billRequestBody.put("items", poRequestBody.getJSONArray("items"));
            
            Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
            
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating bill request", Toast.LENGTH_SHORT).show();
        }
    }
}
