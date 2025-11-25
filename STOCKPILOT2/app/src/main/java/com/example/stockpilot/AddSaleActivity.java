package com.example.stockpilot;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.AddItemRecycler;
import com.example.stockpilot.CustomerSale;
import com.example.stockpilot.Product;
import com.example.stockpilot.SaleItemModel;
import com.example.stockpilot.UserSession;
 
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.MediaType;
import okhttp3.RequestBody;
 

public class AddSaleActivity extends AppCompatActivity implements AddItemRecycler.OnItemRemoveListener {

    private static final String TAG = "AddSaleActivity";
    private static final int REQUEST_SELECT_PRODUCT = 1001;

    private TextView tvStoreName, tvSubtotal, tvTotal;
    private Button btnSave;
    private ImageButton btnAdd;
    private EditText etTax, etDiscount;
    private TextInputEditText etInvoice, etNotes;
    private AutoCompleteTextView spinnerCustomer, spinnerPaymentMethod, spinnerPaymentStatus;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmptyItems;
    private ProgressBar progressBar;

    private List<SaleItemModel> saleItemList = new ArrayList<>();
    private List<CustomerSale> customerList = new ArrayList<>();

    private AddItemRecycler adapter;
    
    private UserSession userSession;

    private double subtotal = 0.0;
    private double discount = 0.0;
    private double tax = 0.0;
    private double total = 0.0;
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sale);

        userSession = UserSession.getInstance(this);
        
        
        bindViews();
        setupListeners();
        setupRecyclerView();
        setupSpinners();
        generateInvoiceNumber();
        fetchCustomers();
        updateOrderSummary();
    }

    private void bindViews() {
        tvStoreName = findViewById(R.id.tvStoreName);
        btnSave = findViewById(R.id.btn_save);
        btnAdd = findViewById(R.id.btn_add);
        etInvoice = findViewById(R.id.et_invoice_number);
        etNotes = findViewById(R.id.et_notes);
        spinnerCustomer = findViewById(R.id.spinner_customer);
        spinnerPaymentMethod = findViewById(R.id.spinner_payment_method);
        spinnerPaymentStatus = findViewById(R.id.spinner_payment_status);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvTotal = findViewById(R.id.tv_total_price);
        etDiscount = findViewById(R.id.et_discount);
        etTax = findViewById(R.id.et_tax);
        recyclerView = findViewById(R.id.recycler_view);
        layoutEmptyItems = findViewById(R.id.layout_empty_state);
        progressBar = findViewById(R.id.progress_bar);

        tvStoreName.setText(userSession.getStoreName());
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> {
            if (!isSubmitting && validateInputs()) {
                saveSale();
            }
        });
        btnAdd.setOnClickListener(v -> showProductSelectionDialog());

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                updateOrderSummary();
            }
        };
        etDiscount.addTextChangedListener(watcher);
        etTax.addTextChangedListener(watcher);
    }

    private void setupRecyclerView() {
        adapter = new AddItemRecycler(this, saleItemList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        updateEmptyView();
    }

    private void setupSpinners() {
        spinnerPaymentMethod.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.payment_method_options)));
        spinnerPaymentStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.payment_status_options)));
    }

    private void generateInvoiceNumber() {
        String pattern = "yyyyMMdd-HHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        String date = simpleDateFormat.format(new Date());
        // Format: INV-20230910-153025
        etInvoice.setText("INV-" + date);
    }

    private void fetchCustomers() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private boolean validateInputs() {
        if (saleItemList.isEmpty()) {
            Toast.makeText(this, "Add at least one product.", Toast.LENGTH_SHORT).show();
            return false;
        }
        for (SaleItemModel item : saleItemList) {
            if (item.getQuantity() <= 0) {
                Toast.makeText(this, "Quantity for " + item.getProductName() + " must be > 0.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (spinnerCustomer.getText() == null || spinnerCustomer.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Select a customer.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerPaymentMethod.getText() == null || spinnerPaymentMethod.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Select a payment method.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerPaymentStatus.getText() == null || spinnerPaymentStatus.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Select a payment status.", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            double d = Double.parseDouble(etDiscount.getText().toString());
            if (d < 0) {
                Toast.makeText(this, "Discount cannot be negative.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception ignored) {}
        try {
            double t = Double.parseDouble(etTax.getText().toString());
            if (t < 0) {
                Toast.makeText(this, "Tax cannot be negative.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception ignored) {}
        return true;
    }

    private void updateOrderSummary() {
        subtotal = 0.0;
        for (SaleItemModel item : saleItemList) {
            subtotal += item.getTotalPrice();
        }
        try {
            discount = Double.parseDouble(etDiscount.getText().toString());
        } catch (Exception e) {
            discount = 0;
        }
        try {
            tax = Double.parseDouble(etTax.getText().toString());
        } catch (Exception e) {
            tax = 0;
        }
        total = subtotal - discount + tax;
        tvSubtotal.setText(String.format(Locale.getDefault(), "₹ %.2f", subtotal));
        tvTotal.setText(String.format(Locale.getDefault(), "₹ %.2f", total));
    }

    private void updateEmptyView() {
        if (saleItemList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmptyItems.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmptyItems.setVisibility(View.GONE);
        }
    }

    private void saveSale() {
        if (isSubmitting) return;

        isSubmitting = true;
        btnSave.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        try {
            JSONObject saleData = new JSONObject();
            saleData.put("store_id", userSession.getStoreId());
            saleData.put("invoice_number", etInvoice.getText() != null ? etInvoice.getText().toString() : "");

            String custName = spinnerCustomer.getText() != null ? spinnerCustomer.getText().toString().trim() : "";
            int custId = -1;
            for (CustomerSale cs : customerList) {
                if (cs.getCustomerName().equals(custName)) {
                    custId = cs.getCustomerId();
                    break;
                }
            }
            if (custId == -1) {
                Toast.makeText(this, "Invalid customer selected.", Toast.LENGTH_SHORT).show();
                enableUi();
                return;
            }
            saleData.put("customer_id", custId);
            saleData.put("payment_method", spinnerPaymentMethod.getText() != null ? spinnerPaymentMethod.getText().toString() : "");
            saleData.put("payment_status", spinnerPaymentStatus.getText() != null ? spinnerPaymentStatus.getText().toString() : "");
            saleData.put("notes", etNotes.getText() != null ? etNotes.getText().toString() : "");
            saleData.put("discount", discount);
            saleData.put("tax", tax);
            saleData.put("total", total);

            JSONArray itemsArr = new JSONArray();
            for (SaleItemModel item : saleItemList) {
                JSONObject obj = new JSONObject();
                obj.put("product_id", item.getProductId());
                obj.put("quantity", item.getQuantity());
                obj.put("unit_price", item.getUnitPrice());
                itemsArr.put(obj);
            }
            saleData.put("items", itemsArr);

            RequestBody body = RequestBody.create(MediaType.get("application/json"), saleData.toString());
            
            Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
            enableUi();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to prepare sale: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            enableUi();
        }
    }

    private void enableUi() {
        isSubmitting = false;
        btnSave.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    private void showProductSelectionDialog() {
        Intent intent = new Intent(this, ProductSelectionActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_PRODUCT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_PRODUCT && resultCode == RESULT_OK && data != null) {
            Product product = (Product) data.getSerializableExtra("selected_product");
            if (product != null) addOrUpdateProduct(product);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addOrUpdateProduct(Product product) {
        for (SaleItemModel item : saleItemList) {
            if (item.getProductId() == product.getProductId()) {
                item.setQuantity(item.getQuantity() + 1);
                adapter.notifyDataSetChanged();
                updateOrderSummary();
                updateEmptyView();
                return;
            }
        }
        SaleItemModel newItem = new SaleItemModel(
                product.getProductId(),
                product.getProductName(),
                product.getSku(),
                1,
                product.getSellingPrice(),
                0,
                0,
                product.getSellingPrice());
        saleItemList.add(newItem);
        adapter.notifyItemInserted(saleItemList.size() - 1);
        updateOrderSummary();
        updateEmptyView();
    }

    @Override
    public void onRemove(int position) {
        if (position >= 0 && position < saleItemList.size()) {
            saleItemList.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, saleItemList.size());
            updateOrderSummary();
            updateEmptyView();
        }
    }
}
