package com.example.stockpilot;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.content.SharedPreferences;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stockpilot.R;
 
import com.example.stockpilot.UserSession;
import com.example.stockpilot.ErrorLogger;
import com.example.stockpilot.ApiUrls;
import com.example.stockpilot.AuthInterceptor;
import com.example.stockpilot.Constants;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAdjustmentActivity extends AppCompatActivity {

    private static final String TAG = "AddAdjustmentActivity";

    private UserSession userSession;

    // UI Components
    private TextView tvStoreInfo, tvCurrentStock, tvTotalValue;
    private AutoCompleteTextView actvProductName, actvMovementType;
    private TextInputEditText etQuantity, etUnitPrice, etNotes;
    private MaterialButton btnCancel, btnSaveAdjustment;
    private ProgressBar progressBar;

    // Data
    private List<Product> productList;
    private Product selectedProduct;
    private DecimalFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_adjustment);

        initializeComponents();
        setupUserSession();
        setupMovementTypes();
        loadProducts();
        setupListeners();
    }

    private void initializeComponents() {
        
        userSession = UserSession.getInstance(AddAdjustmentActivity.this);
        currencyFormat = new DecimalFormat("₹#,##0.00");
        productList = new ArrayList<>();

        // Initialize UI components
        tvStoreInfo = findViewById(R.id.tvStoreInfo);
        tvCurrentStock = findViewById(R.id.tvCurrentStock);
        tvTotalValue = findViewById(R.id.tvTotalValue);
        actvProductName = findViewById(R.id.actvProductName);
        actvMovementType = findViewById(R.id.actvMovementType);
        etQuantity = findViewById(R.id.etQuantity);
        etUnitPrice = findViewById(R.id.etUnitPrice);
        etNotes = findViewById(R.id.etNotes);
        btnCancel = findViewById(R.id.btnCancel);
        btnSaveAdjustment = findViewById(R.id.btnSaveAdjustment);
        progressBar = findViewById(R.id.progressBar);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        String apiBase = sharedPreferences.getString(Constants.API_URL, Constants.DEFAULT_API_URL);
        ApiUrls.setBaseUrl(apiBase);
        AuthInterceptor.setTokenProvider(() -> userSession.getToken());
    }

    private void setupUserSession() {
        String storeInfo = "Store: " + userSession.getStoreName() + " (ID: " + userSession.getStoreId() + ")";
        tvStoreInfo.setText(storeInfo);
    }

    private void setupMovementTypes() {
        String[] movementTypes = {"adjustment", "damaged", "expired", "lost"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, movementTypes);
        actvMovementType.setAdapter(adapter);
    }

    private void loadProducts() {
        showLoading(true);
        productList.clear();
        ApiUrls.getApiService().getProductSelection(userSession.getStoreId())
                .enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                parseProductsResponse(response.body());
                            } else {
                                Toast.makeText(AddAdjustmentActivity.this, Constants.ERROR_SERVER, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            ErrorLogger.logError(TAG, "Error parsing products", e);
                            Toast.makeText(AddAdjustmentActivity.this, Constants.ERROR_UNKNOWN, Toast.LENGTH_SHORT).show();
                        } finally {
                            showLoading(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        ErrorLogger.logError(TAG, "Products load failed", t);
                        Toast.makeText(AddAdjustmentActivity.this, Constants.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    }
                });
    }

    private void parseProductsResponse(List<Map<String, Object>> productsArray) {
        productList.clear();
        List<String> productNames = new ArrayList<>();

        if (productsArray != null && !productsArray.isEmpty()) { // Defensive check
            for (Map<String, Object> productObj : productsArray) {
                int id = 0;
                double price = 0;
                int quantity = 0;
                String name = "";
                Object idObj = productObj.get("id");
                Object priceObj = productObj.get("price");
                Object quantityObj = productObj.get("quantity");
                Object nameObj = productObj.get("product_name");
                if (idObj instanceof Number) {
                    id = ((Number) idObj).intValue();
                } else if (idObj != null) {
                    try { id = Integer.parseInt(idObj.toString()); } catch (Exception ignored) {}
                }
                if (priceObj instanceof Number) {
                    price = ((Number) priceObj).doubleValue();
                } else if (priceObj != null) {
                    try { price = Double.parseDouble(priceObj.toString()); } catch (Exception ignored) {}
                }
                if (quantityObj instanceof Number) {
                    quantity = ((Number) quantityObj).intValue();
                } else if (quantityObj != null) {
                    try { quantity = Integer.parseInt(quantityObj.toString()); } catch (Exception ignored) {}
                }
                if (nameObj != null) {
                    name = nameObj.toString();
                }
                Product product = new Product(id, name, price, quantity);
                productList.add(product);
                productNames.add(product.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddAdjustmentActivity.this,
                android.R.layout.simple_dropdown_item_1line, productNames);
        actvProductName.setAdapter(adapter);
    }

    private void setupListeners() {
        // Product selection listener
        actvProductName.setOnItemClickListener((parent, view, position, id) -> {
            selectedProduct = productList.get(position);
            tvCurrentStock.setText("Current Stock: " + selectedProduct.getQuantity());
            tvCurrentStock.setVisibility(View.VISIBLE);
            etUnitPrice.setText(String.valueOf(selectedProduct.getPrice()));
            calculateTotalValue();
        });

        // Quantity change listener
        etQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalValue();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Unit price change listener
        etUnitPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalValue();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Button listeners
        btnCancel.setOnClickListener(v -> finish());
        btnSaveAdjustment.setOnClickListener(v -> saveAdjustment());
    }

    private void calculateTotalValue() {
        try {
            String quantityStr = etQuantity.getText().toString().trim();
            String priceStr = etUnitPrice.getText().toString().trim();

            if (!quantityStr.isEmpty() && !priceStr.isEmpty()) {
                int quantity = Integer.parseInt(quantityStr);
                double unitPrice = Double.parseDouble(priceStr);
                double totalValue = Math.abs(quantity) * unitPrice;

                tvTotalValue.setText(currencyFormat.format(totalValue));
            } else {
                tvTotalValue.setText("₹0.00");
            }
        } catch (NumberFormatException e) {
            ErrorLogger.logError(TAG, "Error calculating total value", e);
            tvTotalValue.setText("₹0.00");
        }
    }

    private void saveAdjustment() {
        if (!validateInput()) {
            return;
        }

        if (selectedProduct == null) { // Defensive null check
            Toast.makeText(this, "Please select a product", Toast.LENGTH_SHORT).show();
            btnSaveAdjustment.setEnabled(true);
            showLoading(false);
            return;
        }

        showLoading(true);
        btnSaveAdjustment.setEnabled(false);

        // Prepare form data
        Map<String, String> adjustmentData = new HashMap<>();
        adjustmentData.put("store_id", userSession.getStoreId());
        adjustmentData.put("product_id", String.valueOf(selectedProduct.getId()));
        adjustmentData.put("movement_type", actvMovementType.getText().toString());
        adjustmentData.put("quantity", etQuantity.getText().toString());
        adjustmentData.put("unit_price", etUnitPrice.getText().toString());
        adjustmentData.put("total_value", getTotalValueNumeric());
        adjustmentData.put("notes", etNotes.getText().toString());
        adjustmentData.put("performed_by", userSession.getUserId()); // Use user ID

        ApiUrls.getApiService().addInventoryMovement(adjustmentData)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                        btnSaveAdjustment.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(AddAdjustmentActivity.this, "Adjustment saved", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddAdjustmentActivity.this, Constants.ERROR_SERVER, Toast.LENGTH_SHORT).show();
                        }
                        showLoading(false);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                        btnSaveAdjustment.setEnabled(true);
                        ErrorLogger.logError(TAG, "Adjustment save failed", t);
                        Toast.makeText(AddAdjustmentActivity.this, Constants.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    }
                });
    }

    private boolean validateInput() {
        if (selectedProduct == null) {
            Toast.makeText(this, "Please select a product", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (actvMovementType.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select movement type", Toast.LENGTH_SHORT).show();
            return false;
        }

        String quantityStr = etQuantity.getText().toString().trim();
        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity == 0) {
                Toast.makeText(this, "Quantity cannot be zero", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            ErrorLogger.logError(TAG, "Invalid quantity format", e);
            Toast.makeText(this, "Please enter valid quantity", Toast.LENGTH_SHORT).show();
            return false;
        }

        String priceStr = etUnitPrice.getText().toString().trim();
        if (priceStr.isEmpty()) {
            Toast.makeText(this, "Please enter unit price", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                Toast.makeText(this, "Unit price cannot be negative", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            ErrorLogger.logError(TAG, "Invalid unit price format", e);
            Toast.makeText(this, "Please enter valid unit price", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String getTotalValueNumeric() {
        try {
            String quantityStr = etQuantity.getText().toString().trim();
            String priceStr = etUnitPrice.getText().toString().trim();

            int quantity = Integer.parseInt(quantityStr);
            double unitPrice = Double.parseDouble(priceStr);
            double totalValue = Math.abs(quantity) * unitPrice;

            return String.valueOf(totalValue);
        } catch (NumberFormatException e) {
            ErrorLogger.logError(TAG, "Error getting total value", e);
            return "0.00";
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    // Product model class
    private static class Product {
        private int id;
        private String name;
        private double price;
        private int quantity;

        public Product(int id, String name, double price, int quantity) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
    }
}
