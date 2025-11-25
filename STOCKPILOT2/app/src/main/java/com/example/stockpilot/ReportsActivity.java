package com.example.stockpilot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.InvoiceAdapter;
import com.example.stockpilot.LowStockAdapter;
import com.example.stockpilot.UserSession;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

 
 

public class ReportsActivity extends AppCompatActivity {

    private static final String TAG = "ReportsActivity";

    private MaterialToolbar topAppBar;
    private Spinner spinnerDateRange;
    private TextView tvRevenue, tvCost, tvProfit, tvTotalSkus, tvLowStock, tvTotalSales, tvTotalOrders;
    private RecyclerView rvLowStock, rvInvoices;
    private BarChart chartStockByProduct;
    private LineChart chartRevenueTrend;
    private PieChart chartRevenueByCategory;
    private HorizontalBarChart chartTopProducts;

    private String storeId, storeName;
    private UserSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting ReportsActivity");

        try {
            setContentView(R.layout.activity_reports);

            // --- SharedPreferences: get store ID & name ---
            session = new UserSession(this);
            storeId = String.valueOf(session.getStoreId());
            storeName = session.getStoreName();

            Log.d(TAG, "onCreate: Store ID: " + storeId + ", Store Name: " + storeName);

            if (storeId == null || storeId.isEmpty()) {
                Log.e(TAG, "onCreate: Store ID is null or empty");
                Toast.makeText(this, "Store information not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            initializeViews();
            setupSpinner();

            Log.d(TAG, "onCreate: Initialization completed successfully");

            // --- Initial load ---
            fetchReports("All Time", null, null);

        } catch (Exception e) {
            Log.e(TAG, "onCreate: Fatal error during initialization", e);
            Toast.makeText(this, "Failed to initialize reports", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews: Setting up UI components");

        try {
            // --- Toolbar setup ---
            topAppBar = findViewById(R.id.topAppBar);
            topAppBar.setTitle(storeName);
            topAppBar.setNavigationOnClickListener(v -> {
                Log.d(TAG, "Navigation clicked, finishing activity");
                finish();
            });

            // --- UI bindings ---
            spinnerDateRange = findViewById(R.id.spinnerDateRange);
            tvRevenue = findViewById(R.id.tvTotalRevenue);
            tvCost = findViewById(R.id.tvTotalCost);
            tvProfit = findViewById(R.id.tvNetProfit);
            tvTotalSkus = findViewById(R.id.tvTotalSKUs);
            tvLowStock = findViewById(R.id.tvLowStockCount);
            tvTotalSales = findViewById(R.id.tvSalesTitle);
            tvTotalOrders = findViewById(R.id.tvSalesSubtitle);
            rvLowStock = findViewById(R.id.rvLowStock);
            rvInvoices = findViewById(R.id.rvRecentInvoices);
            chartStockByProduct = findViewById(R.id.chartStockByProduct);
            chartRevenueTrend = findViewById(R.id.chartRevenueTrend);
            chartRevenueByCategory = findViewById(R.id.chartRevenueByCategory);
            chartTopProducts = findViewById(R.id.chartTopProducts);

            rvLowStock.setLayoutManager(new LinearLayoutManager(this));
            rvInvoices.setLayoutManager(new LinearLayoutManager(this));

            Log.d(TAG, "initializeViews: All views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "initializeViews: Error initializing views", e);
            throw e; // Re-throw to be caught in onCreate
        }
    }

    private void setupSpinner() {
        Log.d(TAG, "setupSpinner: Setting up date range spinner");

        try {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    this, R.array.date_range_options, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDateRange.setAdapter(adapter);

            spinnerDateRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedRange = parent.getItemAtPosition(position).toString();
                    Log.d(TAG, "Spinner selection changed to: " + selectedRange);
                    fetchReports(selectedRange, null, null);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d(TAG, "Spinner: No item selected");
                }
            });

            Log.d(TAG, "setupSpinner: Spinner setup completed");

        } catch (Exception e) {
            Log.e(TAG, "setupSpinner: Error setting up spinner", e);
            throw e;
        }
    }

    private void fetchReports(String dateRange, String startDate, String endDate) {
        Log.d(TAG, "fetchReports: Starting fetch for range: " + dateRange);

        Map<String, String> salesParams = new HashMap<>();
        salesParams.put("range", dateRange);
        Map<String, String> invParams = new HashMap<>();
        invParams.put("range", dateRange);
        String from = null;
        String to = null;
        if ("Custom Range".equals(dateRange) && startDate != null && endDate != null) {
            from = startDate; to = endDate;
            salesParams.put("from", from);
            salesParams.put("to", to);
            invParams.put("from", from);
            invParams.put("to", to);
        }

        retrofit2.Call<java.util.Map<String, Object>> financialCall = ApiUrls.getApiService().getFinancialReports(storeId, dateRange, from, to);
        retrofit2.Call<java.util.Map<String, Object>> salesCall = ApiUrls.getApiService().getSalesReports(salesParams);
        retrofit2.Call<java.util.Map<String, Object>> inventoryCall = ApiUrls.getApiService().getInventoryReports(invParams);

        final JSONObject result = new JSONObject();

        java.util.concurrent.atomic.AtomicInteger completed = new java.util.concurrent.atomic.AtomicInteger(0);
        int total = 3;
        Runnable maybeUpdateUI = () -> {
            if (completed.incrementAndGet() >= total) {
                updateUI(result);
            }
        };

        salesCall.enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                try {
                    JSONObject sales = new JSONObject(response.isSuccessful() && response.body() != null ? response.body() : new java.util.HashMap<>());
                    result.put("sales", sales);
                    maybeUpdateUI.run();
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) { maybeUpdateUI.run(); }
        });

        inventoryCall.enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                try {
                    JSONObject inventory = new JSONObject(response.isSuccessful() && response.body() != null ? response.body() : new java.util.HashMap<>());
                    result.put("inventory", inventory);
                    maybeUpdateUI.run();
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) { maybeUpdateUI.run(); }
        });

        financialCall.enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                try {
                    JSONObject financial = new JSONObject(response.isSuccessful() && response.body() != null ? response.body() : new java.util.HashMap<>());
                    result.put("financial", financial);
                    maybeUpdateUI.run();
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) { maybeUpdateUI.run(); }
        });

        // completion handled via maybeUpdateUI Runnable above
    }

    private void updateUI(JSONObject json) {
        Log.d(TAG, "updateUI: Starting UI update");

        try {
            JSONObject financial = json.getJSONObject("financial");
            JSONObject inventorySummary = json.getJSONObject("inventory").getJSONObject("summary");
            JSONArray lowStockArray = json.getJSONObject("inventory").getJSONArray("low_stock_items");
            JSONObject salesSummary = json.getJSONObject("sales").getJSONObject("summary");
            JSONArray topProductsArray = json.getJSONObject("sales").getJSONArray("top_products");
            JSONArray invoicesArray = json.getJSONObject("sales").getJSONArray("recent_invoices");
            JSONArray revenueTrendArray = json.getJSONObject("sales").getJSONArray("revenue_trend");
            JSONArray revenueByCategoryArray = json.getJSONObject("sales").getJSONArray("revenue_by_category");

            Log.d(TAG, "updateUI: JSON parsed successfully");
            Log.d(TAG, "updateUI: Financial data - Revenue: " + financial.optString("revenue", "0"));
            Log.d(TAG, "updateUI: Inventory summary - Total SKUs: " + inventorySummary.optString("total_skus", "0"));
            Log.d(TAG, "updateUI: Sales summary - Total sales: " + salesSummary.optString("total_sales", "0"));
            Log.d(TAG, "updateUI: Low stock items count: " + lowStockArray.length());
            Log.d(TAG, "updateUI: Top products count: " + topProductsArray.length());
            Log.d(TAG, "updateUI: Revenue trend points: " + revenueTrendArray.length());
            Log.d(TAG, "updateUI: Revenue by category: " + revenueByCategoryArray.length());
            Log.d(TAG, "updateUI: Recent invoices: " + invoicesArray.length());

            runOnUiThread(() -> {
                try {
                    // --- Financial ---
                    tvRevenue.setText("₹ " + financial.optString("revenue", "0"));
                    tvCost.setText("₹ " + financial.optString("cost", "0"));
                    tvProfit.setText("₹ " + financial.optString("profit", "0"));

                    // --- Inventory ---
                    tvTotalSkus.setText(inventorySummary.optString("total_skus", "0"));
                    tvLowStock.setText(inventorySummary.optString("low_stock", "0"));
                    rvLowStock.setAdapter(new LowStockAdapter(lowStockArray));

                    // --- Sales ---
                    tvTotalSales.setText("Total Sales: ₹ " + salesSummary.optString("total_sales", "0"));
                    tvTotalOrders.setText("Orders: " + salesSummary.optString("total_orders", "0"));
                    rvInvoices.setAdapter(new InvoiceAdapter(invoicesArray));

                    // --- Charts ---
                    setupTopProductsChart(topProductsArray);
                    setupRevenueTrendChart(revenueTrendArray);
                    setupRevenueByCategoryChart(revenueByCategoryArray);
                    setupStockByProductChart();

                    Log.d(TAG, "updateUI: UI updated successfully");

                } catch (Exception e) {
                    Log.e(TAG, "updateUI: Error during UI update", e);
                    Toast.makeText(ReportsActivity.this, "UI update error", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "updateUI: JSON structure error", e);
            runOnUiThread(() -> Toast.makeText(ReportsActivity.this, "Invalid data format", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            Log.e(TAG, "updateUI: Unexpected error", e);
        }
    }

    private void setupTopProductsChart(JSONArray topProductsArray) throws JSONException {
        Log.d(TAG, "setupTopProductsChart: Setting up chart with " + topProductsArray.length() + " products");

        try {
            ArrayList<BarEntry> entries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();

            for (int i = 0; i < topProductsArray.length(); i++) {
                JSONObject obj = topProductsArray.getJSONObject(i);
                int qtySold = obj.optInt("qty_sold", 0);
                String productName = obj.optString("product_name", "Unknown Product");

                entries.add(new BarEntry(i, qtySold));
                labels.add(productName);

                Log.v(TAG, "setupTopProductsChart: Product " + i + ": " + productName + " - " + qtySold + " sold");
            }

            BarDataSet dataSet = new BarDataSet(entries, "Top Products");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            BarData data = new BarData(dataSet);
            chartTopProducts.setData(data);
            chartTopProducts.invalidate();

            Log.d(TAG, "setupTopProductsChart: Chart setup completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "setupTopProductsChart: Error setting up chart", e);
            throw e;
        }
    }

    private void setupRevenueTrendChart(JSONArray revenueTrendArray) throws JSONException {
        Log.d(TAG, "setupRevenueTrendChart: Setting up chart with " + revenueTrendArray.length() + " data points");

        try {
            ArrayList<Entry> entries = new ArrayList<>();

            for (int i = 0; i < revenueTrendArray.length(); i++) {
                JSONObject obj = revenueTrendArray.getJSONObject(i);
                float value = (float) obj.optDouble("revenue", 0);
                entries.add(new Entry(i, value));

                Log.v(TAG, "setupRevenueTrendChart: Data point " + i + ": " + value);
            }

            LineDataSet dataSet = new LineDataSet(entries, "Revenue Trend");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            dataSet.setCircleColors(ColorTemplate.MATERIAL_COLORS);
            dataSet.setLineWidth(2f);

            LineData lineData = new LineData(dataSet);
            chartRevenueTrend.setData(lineData);

            Description desc = new Description();
            desc.setText("Daily Revenue");
            chartRevenueTrend.setDescription(desc);

            chartRevenueTrend.invalidate();

            Log.d(TAG, "setupRevenueTrendChart: Chart setup completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "setupRevenueTrendChart: Error setting up chart", e);
            throw e;
        }
    }

    private void setupRevenueByCategoryChart(JSONArray revenueByCategoryArray) throws JSONException {
        Log.d(TAG, "setupRevenueByCategoryChart: Setting up chart with " + revenueByCategoryArray.length() + " categories");

        try {
            ArrayList<PieEntry> entries = new ArrayList<>();

            for (int i = 0; i < revenueByCategoryArray.length(); i++) {
                JSONObject obj = revenueByCategoryArray.getJSONObject(i);
                float value = (float) obj.optDouble("revenue", 0);
                String label = obj.optString("category_name", "Other");
                entries.add(new PieEntry(value, label));

                Log.v(TAG, "setupRevenueByCategoryChart: Category " + i + ": " + label + " - " + value);
            }

            PieDataSet dataSet = new PieDataSet(entries, "Revenue by Category");
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            dataSet.setValueTextSize(12f);

            PieData pieData = new PieData(dataSet);
            chartRevenueByCategory.setData(pieData);

            Description desc = new Description();
            desc.setText("Category Revenue Share");
            chartRevenueByCategory.setDescription(desc);

            chartRevenueByCategory.invalidate();

            Log.d(TAG, "setupRevenueByCategoryChart: Chart setup completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "setupRevenueByCategoryChart: Error setting up chart", e);
            throw e;
        }
    }

    private void setupStockByProductChart() {
        Log.d(TAG, "setupStockByProductChart: Setting up placeholder chart");

        try {
            // Placeholder: extend PHP to return stock by product for real data
            ArrayList<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0, 50));
            entries.add(new BarEntry(1, 30));
            entries.add(new BarEntry(2, 20));

            BarDataSet dataSet = new BarDataSet(entries, "Stock by Product");
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            BarData barData = new BarData(dataSet);
            chartStockByProduct.setData(barData);
            chartStockByProduct.invalidate();

            Log.d(TAG, "setupStockByProductChart: Placeholder chart setup completed");

        } catch (Exception e) {
            Log.e(TAG, "setupStockByProductChart: Error setting up chart", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ReportsActivity destroyed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ReportsActivity paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ReportsActivity resumed");
    }
}
