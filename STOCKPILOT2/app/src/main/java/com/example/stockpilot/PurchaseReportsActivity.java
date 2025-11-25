package com.example.stockpilot;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.UserSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
 import java.util.Locale;
import java.util.Map;

 
 

public class PurchaseReportsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvFromDate, tvToDate, tvTotalPurchases, tvTotalOrders;
    private Button btnApplyFilter;
    private RecyclerView rvVendorDistribution, rvPurchaseStatus, rvRecentPurchases;
    private ProgressBar progressBar;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String fromDate, toDate;
    private String storeId;
    private NumberFormat currencyFormat;
    

    // Adapters
    private VendorDistributionAdapter vendorDistributionAdapter;
    private PurchaseStatusAdapter purchaseStatusAdapter;
    private RecentPurchaseAdapter recentPurchaseAdapter;

    // Data lists
    private List<VendorDistribution> vendorDistributions;
    private List<PurchaseStatus> purchaseStatuses;
    private List<PurchaseOrder> recentPurchases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_reports);

        // Initialize views
        initViews();

        // Setup date format and calendar
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        calendar = Calendar.getInstance();

        // Set default date range (last 30 days)
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.add(Calendar.DAY_OF_MONTH, -30);
        fromDate = dateFormat.format(fromCalendar.getTime());
        toDate = dateFormat.format(calendar.getTime());

        // Update date TextViews
        tvFromDate.setText(fromDate);
        tvToDate.setText(toDate);

        // Initialize currency format
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        currencyFormat.setCurrency(Currency.getInstance("INR"));

        

        // Get store ID from UserSession
        UserSession userSession = UserSession.getInstance(this);
        storeId = String.valueOf(userSession.getStoreId());

        // Initialize data lists
        vendorDistributions = new ArrayList<>();
        purchaseStatuses = new ArrayList<>();
        recentPurchases = new ArrayList<>();

        // Setup listeners
        setupListeners();

        // Setup RecyclerViews
        setupRecyclerViews();

        // Load initial purchase report data
        loadPurchaseReportData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);
        tvTotalPurchases = findViewById(R.id.tvTotalPurchases);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        rvVendorDistribution = findViewById(R.id.rvVendorDistribution);
        rvPurchaseStatus = findViewById(R.id.rvPurchaseStatus);
        rvRecentPurchases = findViewById(R.id.rvRecentPurchases);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        // Back button click listener
        ivBack.setOnClickListener(v -> finish());

        // From date click listener
        tvFromDate.setOnClickListener(v -> showDatePickerDialog(true));

        // To date click listener
        tvToDate.setOnClickListener(v -> showDatePickerDialog(false));

        // Apply filter button click listener
        btnApplyFilter.setOnClickListener(v -> loadPurchaseReportData());
    }

    private void setupRecyclerViews() {
        // Setup Vendor Distribution RecyclerView
        vendorDistributionAdapter = new VendorDistributionAdapter(vendorDistributions);
        rvVendorDistribution.setLayoutManager(new LinearLayoutManager(this));
        rvVendorDistribution.setAdapter(vendorDistributionAdapter);

        // Setup Purchase Status RecyclerView
        purchaseStatusAdapter = new PurchaseStatusAdapter(purchaseStatuses);
        rvPurchaseStatus.setLayoutManager(new LinearLayoutManager(this));
        rvPurchaseStatus.setAdapter(purchaseStatusAdapter);

        // Setup Recent Purchases RecyclerView
        recentPurchaseAdapter = new RecentPurchaseAdapter(recentPurchases);
        rvRecentPurchases.setLayoutManager(new LinearLayoutManager(this));
        rvRecentPurchases.setAdapter(recentPurchaseAdapter);
    }

    private void showDatePickerDialog(final boolean isFromDate) {
        Calendar currentDate = Calendar.getInstance();
        try {
            if (isFromDate && !tvFromDate.getText().toString().isEmpty()) {
                currentDate.setTime(dateFormat.parse(tvFromDate.getText().toString()));
            } else if (!isFromDate && !tvToDate.getText().toString().isEmpty()) {
                currentDate.setTime(dateFormat.parse(tvToDate.getText().toString()));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    String formattedDate = dateFormat.format(selectedDate.getTime());

                    if (isFromDate) {
                        fromDate = formattedDate;
                        tvFromDate.setText(formattedDate);
                    } else {
                        toDate = formattedDate;
                        tvToDate.setText(formattedDate);
                    }
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void loadPurchaseReportData() {
        progressBar.setVisibility(View.GONE);
        tvTotalPurchases.setText(currencyFormat.format(0));
        tvTotalOrders.setText(String.valueOf(0));
        vendorDistributions.clear();
        purchaseStatuses.clear();
        recentPurchases.clear();
        vendorDistributionAdapter.notifyDataSetChanged();
        purchaseStatusAdapter.notifyDataSetChanged();
        recentPurchaseAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private void processReportData(JSONObject jsonObject) {
        try {
            // Extract summary data - handle both formats
            double totalPurchaseAmount = 0;
            int totalOrders = 0;
            
            if (jsonObject.has("purchase_summary")) {
                // Original format
                JSONObject summary = jsonObject.getJSONObject("purchase_summary");
                totalPurchaseAmount = summary.optDouble("total_purchase_amount", 0);
                totalOrders = summary.optInt("total_orders", 0);
            } else if (jsonObject.has("summary")) {
                // Alternative format
                JSONObject summary = jsonObject.getJSONObject("summary");
                totalPurchaseAmount = summary.optDouble("total_amount", summary.optDouble("amount", 0));
                totalOrders = summary.optInt("total_orders", summary.optInt("orders", 0));
            } else if (jsonObject.has("total_purchase_amount") && jsonObject.has("total_orders")) {
                // Direct in root format
                totalPurchaseAmount = jsonObject.optDouble("total_purchase_amount", 0);
                totalOrders = jsonObject.optInt("total_orders", 0);
            }

            // Extract vendor distribution data - handle both formats
            JSONArray vendorDistributionArray;
            if (jsonObject.has("vendor_distribution")) {
                vendorDistributionArray = jsonObject.getJSONArray("vendor_distribution");
            } else if (jsonObject.has("vendors")) {
                vendorDistributionArray = jsonObject.getJSONArray("vendors");
            } else {
                vendorDistributionArray = new JSONArray(); // Empty array as fallback
            }
            
            List<VendorDistribution> newVendorDistributions = new ArrayList<>();
            for (int i = 0; i < vendorDistributionArray.length(); i++) {
                JSONObject vendorObj = vendorDistributionArray.getJSONObject(i);
                String vendorName = vendorObj.optString("vendor_name", vendorObj.optString("name", "Unknown"));
                double amount = vendorObj.optDouble("amount", 0);
                double percentage = vendorObj.optDouble("percentage", 0);
                newVendorDistributions.add(new VendorDistribution(vendorName, amount, percentage));
            }

            // Extract purchase status data - handle both formats
            JSONArray purchaseStatusArray;
            if (jsonObject.has("purchase_status")) {
                purchaseStatusArray = jsonObject.getJSONArray("purchase_status");
            } else if (jsonObject.has("status_distribution")) {
                purchaseStatusArray = jsonObject.getJSONArray("status_distribution");
            } else {
                purchaseStatusArray = new JSONArray(); // Empty array as fallback
            }
            
            List<PurchaseStatus> newPurchaseStatuses = new ArrayList<>();
            for (int i = 0; i < purchaseStatusArray.length(); i++) {
                JSONObject statusObj = purchaseStatusArray.getJSONObject(i);
                String status = statusObj.optString("status", "Unknown");
                int count = statusObj.optInt("count", 0);
                double percentage = statusObj.optDouble("percentage", 0);
                newPurchaseStatuses.add(new PurchaseStatus(status, count, percentage));
            }

            // Extract recent purchases data - handle both formats
            JSONArray recentPurchasesArray;
            if (jsonObject.has("recent_purchases")) {
                recentPurchasesArray = jsonObject.getJSONArray("recent_purchases");
            } else if (jsonObject.has("purchases")) {
                recentPurchasesArray = jsonObject.getJSONArray("purchases");
            } else {
                recentPurchasesArray = new JSONArray(); // Empty array as fallback
            }
            
            List<PurchaseOrder> newRecentPurchases = new ArrayList<>();
            for (int i = 0; i < recentPurchasesArray.length(); i++) {
                JSONObject purchaseObj = recentPurchasesArray.getJSONObject(i);
                String poId = purchaseObj.optString("po_id", purchaseObj.optString("id", "Unknown"));
                String vendorName = purchaseObj.optString("vendor_name", purchaseObj.optString("vendor", "Unknown"));
                String orderDate = purchaseObj.optString("order_date", purchaseObj.optString("date", "Unknown"));
                double totalAmount = purchaseObj.optDouble("total_amount", purchaseObj.optDouble("amount", 0));
                String status = purchaseObj.optString("status", "Unknown");

                // Extract purchase items if available
                List<PurchaseItem> items = new ArrayList<>();
                if (purchaseObj.has("items")) {
                    JSONArray itemsArray = purchaseObj.getJSONArray("items");
                    for (int j = 0; j < itemsArray.length(); j++) {
                        JSONObject itemObj = itemsArray.getJSONObject(j);
                        String productName = itemObj.optString("product_name", itemObj.optString("name", "Unknown"));
                        int quantity = itemObj.optInt("quantity", 0);
                        double unitPrice = itemObj.optDouble("unit_price", itemObj.optDouble("price", 0));
                        items.add(new PurchaseItem(productName, quantity, unitPrice));
                    }
                } else if (purchaseObj.has("products")) {
                    JSONArray itemsArray = purchaseObj.getJSONArray("products");
                    for (int j = 0; j < itemsArray.length(); j++) {
                        JSONObject itemObj = itemsArray.getJSONObject(j);
                        String productName = itemObj.optString("product_name", itemObj.optString("name", "Unknown"));
                        int quantity = itemObj.optInt("quantity", 0);
                        double unitPrice = itemObj.optDouble("unit_price", itemObj.optDouble("price", 0));
                        items.add(new PurchaseItem(productName, quantity, unitPrice));
                    }
                }

                newRecentPurchases.add(new PurchaseOrder(poId, vendorName, orderDate, totalAmount, status, items));
            }

            // Update UI on the main thread
            final double finalTotalPurchaseAmount = totalPurchaseAmount;
            final int finalTotalOrders = totalOrders;
            final List<VendorDistribution> finalVendorDistributions = newVendorDistributions;
            final List<PurchaseStatus> finalPurchaseStatuses = newPurchaseStatuses;
            final List<PurchaseOrder> finalRecentPurchases = newRecentPurchases;
            
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);

                // Update summary
                tvTotalPurchases.setText(currencyFormat.format(finalTotalPurchaseAmount));
                tvTotalOrders.setText(String.valueOf(finalTotalOrders));

                // Update vendor distribution
                vendorDistributions.clear();
                vendorDistributions.addAll(finalVendorDistributions);
                vendorDistributionAdapter.notifyDataSetChanged();

                // Update purchase status
                purchaseStatuses.clear();
                purchaseStatuses.addAll(finalPurchaseStatuses);
                purchaseStatusAdapter.notifyDataSetChanged();

                // Update recent purchases
                recentPurchases.clear();
                recentPurchases.addAll(finalRecentPurchases);
                recentPurchaseAdapter.notifyDataSetChanged();
            });

        } catch (JSONException e) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                showError("Error processing data: " + e.getMessage());
            });
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Model classes
    public static class VendorDistribution {
        private String vendorName;
        private double amount;
        private double percentage;

        public VendorDistribution(String vendorName, double amount, double percentage) {
            this.vendorName = vendorName;
            this.amount = amount;
            this.percentage = percentage;
        }

        public String getVendorName() {
            return vendorName;
        }

        public double getAmount() {
            return amount;
        }

        public double getPercentage() {
            return percentage;
        }
    }

    public static class PurchaseStatus {
        private String status;
        private int count;
        private double percentage;

        public PurchaseStatus(String status, int count, double percentage) {
            this.status = status;
            this.count = count;
            this.percentage = percentage;
        }

        public String getStatus() {
            return status;
        }

        public int getCount() {
            return count;
        }

        public double getPercentage() {
            return percentage;
        }
    }

    public static class PurchaseOrder {
        private String poId;
        private String vendorName;
        private String orderDate;
        private double totalAmount;
        private String status;
        private List<PurchaseItem> items;

        public PurchaseOrder(String poId, String vendorName, String orderDate, double totalAmount, String status, List<PurchaseItem> items) {
            this.poId = poId;
            this.vendorName = vendorName;
            this.orderDate = orderDate;
            this.totalAmount = totalAmount;
            this.status = status;
            this.items = items;
        }

        public String getPoId() {
            return poId;
        }

        public String getVendorName() {
            return vendorName;
        }

        public String getOrderDate() {
            return orderDate;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public String getStatus() {
            return status;
        }

        public List<PurchaseItem> getItems() {
            return items;
        }
    }

    public static class PurchaseItem {
        private String productName;
        private int quantity;
        private double unitPrice;

        public PurchaseItem(String productName, int quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public double getTotalPrice() {
            return quantity * unitPrice;
        }
    }
}