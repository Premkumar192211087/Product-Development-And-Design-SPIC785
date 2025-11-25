package com.example.stockpilot;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stockpilot.UserSession;
import com.example.stockpilot.ErrorLogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

 

public class InventoryReportsActivity extends AppCompatActivity {

    private static final String TAG = "InventoryReportsActivity";

    private TextView tvTotalProducts, tvLowStock, tvExpired;
    private Spinner spinnerCategory;
    private RecyclerView rvStockMovements, rvLowStockAlerts;
    private ProgressBar progressBar;
    private ImageView ivBack;

    
    private String storeId;
    private SimpleDateFormat dateFormat;

    private StockMovementAdapter stockMovementAdapter;
    private LowStockAdapter lowStockAdapter;
    private List<String> categories;
    private ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_reports);

        // Initialize components
        initViews();
        setupListeners();

        // Initialize date format
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        

        // Get store ID from UserSession
        UserSession userSession = UserSession.getInstance(this);
        storeId = userSession.getStoreId();

        // Set up RecyclerViews
        setupRecyclerViews();

        // Initialize category spinner
        categories = new ArrayList<>();
        categories.add("All"); // Default option
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Load initial data
        loadInventoryReportData(null);
    }

    private void initViews() {
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvLowStock = findViewById(R.id.tvLowStock);
        tvExpired = findViewById(R.id.tvexpired);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        rvStockMovements = findViewById(R.id.rvStockMovements);
        rvLowStockAlerts = findViewById(R.id.rvLowStockAlerts);
        progressBar = findViewById(R.id.progressBar);
        ivBack = findViewById(R.id.btn_back);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                if (position == 0) { // "All" option
                    loadInventoryReportData(null);
                } else {
                    loadInventoryReportData(selectedCategory);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupRecyclerViews() {
        // Stock Movements RecyclerView
        stockMovementAdapter = new StockMovementAdapter(new ArrayList<>());
        rvStockMovements.setLayoutManager(new LinearLayoutManager(this));
        rvStockMovements.setAdapter(stockMovementAdapter);

        // Low Stock Alerts RecyclerView
        lowStockAdapter = new LowStockAdapter(new ArrayList<>());
        rvLowStockAlerts.setLayoutManager(new LinearLayoutManager(this));
        rvLowStockAlerts.setAdapter(lowStockAdapter);
    }

    // Fix the loadInventoryReportData method around line 85-95:

    private void loadInventoryReportData(String category) {
        progressBar.setVisibility(View.GONE);
        tvTotalProducts.setText("0");
        tvLowStock.setText("0");
        tvExpired.setText("0");
        stockMovementAdapter.updateData(new ArrayList<>());
        lowStockAdapter.updateData(new ArrayList<>());
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private void processReportData(JSONObject data) throws JSONException {
        // Extract inventory summary - handle both response formats
        int totalProducts = 0;
        int lowStock = 0;
        int expired = 0;
        
        if (data.has("inventory_summary")) {
            // Original format
            JSONObject summaryObj = data.getJSONObject("inventory_summary");
            totalProducts = summaryObj.optInt("total_products", 0);
            lowStock = summaryObj.optInt("low_stock", 0);
            expired = summaryObj.optInt("expired", 0);
        } else if (data.has("summary")) {
            // Alternative format
            JSONObject summaryObj = data.getJSONObject("summary");
            totalProducts = summaryObj.optInt("total_products", summaryObj.optInt("product_count", 0));
            lowStock = summaryObj.optInt("low_stock", summaryObj.optInt("low_stock_count", 0));
            expired = summaryObj.optInt("expired", summaryObj.optInt("expired_count", 0));
        }
        
        final int finalTotalProducts = totalProducts;
        final int finalLowStock = lowStock;
        final int finalExpired = expired;

        // Extract stock movements - handle both formats
        JSONArray movementsArray;
        if (data.has("stock_movements")) {
            movementsArray = data.getJSONArray("stock_movements");
        } else if (data.has("movements")) {
            movementsArray = data.getJSONArray("movements");
        } else {
            movementsArray = new JSONArray(); // Empty array as fallback
        }
        
        final List<StockMovement> stockMovements = new ArrayList<>();

        for (int i = 0; i < movementsArray.length(); i++) {
            JSONObject movementObj = movementsArray.getJSONObject(i);
            int id = movementObj.optInt("movement_id", movementObj.optInt("id", 0));
            String productName = movementObj.optString("product_name", movementObj.optString("name", "Unknown"));
            String category = movementObj.optString("category", "Unknown");
            String movementType = movementObj.optString("movement_type", movementObj.optString("type", "Unknown"));
            int quantity = movementObj.optInt("quantity", 0);
            String referenceType = movementObj.optString("reference_type", movementObj.optString("reference", "Unknown"));
            String performedBy = movementObj.optString("performed_by", movementObj.optString("user", "Unknown"));
            String timestamp = movementObj.optString("timestamp", movementObj.optString("date", "Unknown"));

            stockMovements.add(new StockMovement(id, productName, category, movementType, quantity, referenceType, performedBy, timestamp));
        }

        // Extract low stock alerts - handle both formats
        JSONArray alertsArray;
        if (data.has("low_stock_alerts")) {
            alertsArray = data.getJSONArray("low_stock_alerts");
        } else if (data.has("low_stock")) {
            alertsArray = data.getJSONArray("low_stock");
        } else {
            alertsArray = new JSONArray(); // Empty array as fallback
        }
        
        final List<LowStockAlert> lowStockAlerts = new ArrayList<>();

        for (int i = 0; i < alertsArray.length(); i++) {
            JSONObject alertObj = alertsArray.getJSONObject(i);
            int productId = alertObj.optInt("product_id", alertObj.optInt("id", 0));
            String productName = alertObj.optString("product_name", alertObj.optString("name", "Unknown"));
            String category = alertObj.optString("category", "Unknown");
            int currentStock = alertObj.optInt("current_stock", alertObj.optInt("stock", 0));
            int minStockLevel = alertObj.optInt("min_stock_level", alertObj.optInt("min_level", 0));
            int reorderQuantity = alertObj.optInt("reorder_quantity", alertObj.optInt("reorder", 0));
            String status = alertObj.optString("status", currentStock == 0 ? "Out of Stock" : "Low Stock");

            lowStockAlerts.add(new LowStockAlert(productId, productName, category, currentStock, minStockLevel, reorderQuantity, status));
        }

        // Extract categories for filter
        if (data.has("categories")) {
            JSONArray categoriesArray = data.getJSONArray("categories");
            final List<String> newCategories = new ArrayList<>();
            newCategories.add("All"); // Default option

            for (int i = 0; i < categoriesArray.length(); i++) {
                newCategories.add(categoriesArray.getString(i));
            }

            // Update categories on UI thread
            runOnUiThread(() -> {
                categories.clear();
                categories.addAll(newCategories);
                categoryAdapter.notifyDataSetChanged();
            });
        }

        // Update UI on main thread
        final List<StockMovement> finalStockMovements = stockMovements;
        final List<LowStockAlert> finalLowStockAlerts = lowStockAlerts;
        
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);

            // Update summary
            tvTotalProducts.setText(String.valueOf(finalTotalProducts));
            tvLowStock.setText(String.valueOf(finalLowStock));
            tvExpired.setText(String.valueOf(finalExpired));

            // Update stock movements
            stockMovementAdapter.updateData(finalStockMovements);

            // Update low stock alerts
            lowStockAdapter.updateData(finalLowStockAlerts);
        });
    }

    private void showError(final String message) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(InventoryReportsActivity.this, message, Toast.LENGTH_SHORT).show();
        });
    }

    // Model classes
    public static class StockMovement {
        private int id;
        private String productName;
        private String category;
        private String movementType;
        private int quantity;
        private String referenceType;
        private String performedBy;
        private String timestamp;

        public StockMovement(int id, String productName, String category, String movementType, int quantity, String referenceType, String performedBy, String timestamp) {
            this.id = id;
            this.productName = productName;
            this.category = category;
            this.movementType = movementType;
            this.quantity = quantity;
            this.referenceType = referenceType;
            this.performedBy = performedBy;
            this.timestamp = timestamp;
        }

        public int getId() {
            return id;
        }

        public String getProductName() {
            return productName;
        }

        public String getCategory() {
            return category;
        }

        public String getMovementType() {
            return movementType;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getReferenceType() {
            return referenceType;
        }

        public String getPerformedBy() {
            return performedBy;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    public static class LowStockAlert {
        private int productId;
        private String productName;
        private String category;
        private int currentStock;
        private int minStockLevel;
        private int reorderQuantity;
        private String status;

        public LowStockAlert(int productId, String productName, String category, int currentStock, int minStockLevel, int reorderQuantity, String status) {
            this.productId = productId;
            this.productName = productName;
            this.category = category;
            this.currentStock = currentStock;
            this.minStockLevel = minStockLevel;
            this.reorderQuantity = reorderQuantity;
            this.status = status;
        }

        public int getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public String getCategory() {
            return category;
        }

        public int getCurrentStock() {
            return currentStock;
        }

        public int getMinStockLevel() {
            return minStockLevel;
        }

        public int getReorderQuantity() {
            return reorderQuantity;
        }

        public String getStatus() {
            return status;
        }
    }

    // Adapter classes
    private class StockMovementAdapter extends RecyclerView.Adapter<StockMovementAdapter.ViewHolder> {
        private List<StockMovement> stockMovements;

        public StockMovementAdapter(List<StockMovement> stockMovements) {
            this.stockMovements = stockMovements;
        }

        public void updateData(List<StockMovement> newData) {
            this.stockMovements = newData;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_stock_movement, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            StockMovement movement = stockMovements.get(position);

            holder.tvProductName.setText(movement.getProductName());
            holder.tvCategory.setText(movement.getCategory());

            // Format movement type for display
            String movementType = movement.getMovementType();
            String displayType = movementType.substring(0, 1).toUpperCase() + movementType.substring(1);
            holder.tvMovementType.setText(displayType);

            // Set color based on movement type
            int colorResId;
            if (movementType.equals("in")) {
                colorResId = R.color.success;
                holder.tvQuantity.setText("+" + movement.getQuantity());
            } else if (movementType.equals("out")) {
                colorResId = R.color.error;
                holder.tvQuantity.setText("-" + movement.getQuantity());
            } else {
                colorResId = R.color.info;
                holder.tvQuantity.setText(String.valueOf(movement.getQuantity()));
            }
            holder.tvMovementType.setTextColor(getResources().getColor(colorResId));
            holder.tvQuantity.setTextColor(getResources().getColor(colorResId));

            // Format reference type for display
            String referenceType = movement.getReferenceType().replace('_', ' ');
            referenceType = referenceType.substring(0, 1).toUpperCase() + referenceType.substring(1);
            holder.tvReferenceType.setText(referenceType);

            holder.tvPerformedBy.setText(movement.getPerformedBy());
            holder.tvTimestamp.setText(movement.getTimestamp());
        }

        @Override
        public int getItemCount() {
            return stockMovements.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvProductName, tvCategory, tvMovementType, tvQuantity, tvReferenceType, tvPerformedBy, tvTimestamp;

            ViewHolder(View itemView) {
                super(itemView);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvMovementType = itemView.findViewById(R.id.tvMovementType);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvReferenceType = itemView.findViewById(R.id.tvReferenceType);
                tvPerformedBy = itemView.findViewById(R.id.tvPerformedBy);
                tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            }
        }
    }

    private class LowStockAdapter extends RecyclerView.Adapter<LowStockAdapter.ViewHolder> {
        private List<LowStockAlert> lowStockAlerts;

        public LowStockAdapter(List<LowStockAlert> lowStockAlerts) {
            this.lowStockAlerts = lowStockAlerts;
        }

        public void updateData(List<LowStockAlert> newData) {
            this.lowStockAlerts = newData;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_low_stock_alert, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LowStockAlert alert = lowStockAlerts.get(position);

            holder.tvProductName.setText(alert.getProductName());
            holder.tvCategory.setText(alert.getCategory());
            holder.tvCurrentStock.setText(String.valueOf(alert.getCurrentStock()));
            holder.tvMinStockLevel.setText(String.valueOf(alert.getMinStockLevel()));

            // Set status text and color
            holder.tvStatus.setText(alert.getStatus());
            int colorResId = alert.getStatus().equals("Out of Stock") ? R.color.error : R.color.warning;
            holder.tvStatus.setTextColor(getResources().getColor(colorResId));

            // Set up reorder button click
            holder.btnReorder.setOnClickListener(v -> {
                // Implement reorder functionality
                Toast.makeText(InventoryReportsActivity.this,
                        "Reordering " + alert.getReorderQuantity() + " units of " + alert.getProductName(),
                        Toast.LENGTH_SHORT).show();
                // In a real app, this would navigate to the purchase order creation screen
            });
        }

        @Override
        public int getItemCount() {
            return lowStockAlerts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvProductName, tvCategory, tvCurrentStock, tvMinStockLevel, tvStatus;
            Button btnReorder;

            ViewHolder(View itemView) {
                super(itemView);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvCurrentStock = itemView.findViewById(R.id.tvCurrentStock);
                tvMinStockLevel = itemView.findViewById(R.id.tvMinStockLevel);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnReorder = itemView.findViewById(R.id.btnReorder);
            }
        }
    }
}