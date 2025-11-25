package com.example.stockpilot;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

 

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

 

public class SalesReportsActivity extends AppCompatActivity {

    private static final String TAG = "SalesReportsActivity";

    private TextView tvFromDate, tvToDate, tvTotalSales, tvTotalOrders;
    private Button btnApplyFilter;
    private RecyclerView rvPaymentMethods, rvRecentSales;
    private ProgressBar progressBar;
    private ImageView ivBack;
    
    private Calendar fromCalendar, toCalendar;
    private SimpleDateFormat dateFormat;
    
    private String storeId;
    private NumberFormat currencyFormat;
    
    private PaymentMethodAdapter paymentMethodAdapter;
    private RecentSalesAdapter recentSalesAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_reports);
        
        // Initialize components
        initViews();
        setupListeners();
        
        // Initialize date format and calendars
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();
        
        // Set default date range (last 30 days)
        fromCalendar.add(Calendar.DAY_OF_MONTH, -30);
        tvFromDate.setText(dateFormat.format(fromCalendar.getTime()));
        tvToDate.setText(dateFormat.format(toCalendar.getTime()));
        
        // Initialize currency format
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        currencyFormat.setCurrency(Currency.getInstance("INR"));
        
        
        
        // Get store ID from UserSession
        UserSession userSession = UserSession.getInstance(this);
        storeId = userSession.getStoreId();
        
        // Set up RecyclerViews
        setupRecyclerViews();
        
        // Load initial data
        loadSalesReportData();
    }
    
    private void initViews() {
        tvFromDate = findViewById(R.id.tv_from_date);
        tvToDate = findViewById(R.id.tv_to_date);
        tvTotalSales = findViewById(R.id.tv_total_sales);
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        btnApplyFilter = findViewById(R.id.btn_apply_filter);
        rvPaymentMethods = findViewById(R.id.rv_payment_methods);
        rvRecentSales = findViewById(R.id.rv_recent_sales);
        progressBar = findViewById(R.id.progress_bar);
        ivBack = findViewById(R.id.iv_back);
    }
    
    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        tvFromDate.setOnClickListener(v -> showDatePickerDialog(true));
        tvToDate.setOnClickListener(v -> showDatePickerDialog(false));
        
        btnApplyFilter.setOnClickListener(v -> loadSalesReportData());
    }
    
    private void setupRecyclerViews() {
        // Payment Methods RecyclerView
        paymentMethodAdapter = new PaymentMethodAdapter(new ArrayList<>());
        rvPaymentMethods.setLayoutManager(new LinearLayoutManager(this));
        rvPaymentMethods.setAdapter(paymentMethodAdapter);
        
        // Recent Sales RecyclerView
        recentSalesAdapter = new RecentSalesAdapter(new ArrayList<>());
        rvRecentSales.setLayoutManager(new LinearLayoutManager(this));
        rvRecentSales.setAdapter(recentSalesAdapter);
    }
    
    private void showDatePickerDialog(final boolean isFromDate) {
        Calendar calendar = isFromDate ? fromCalendar : toCalendar;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    String formattedDate = dateFormat.format(calendar.getTime());
                    if (isFromDate) {
                        tvFromDate.setText(formattedDate);
                    } else {
                        tvToDate.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void loadSalesReportData() {
        progressBar.setVisibility(View.GONE);
        paymentMethodAdapter.updateData(new ArrayList<>());
        recentSalesAdapter.updateData(new ArrayList<>());
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }
    
    private void processReportData(JSONObject data) throws JSONException {
        // Extract summary data - handle both response formats
        final double totalSales;
        final int totalOrders;
        
        // Check which format we're dealing with
        if (data.has("total_sales")) {
            // Original format
            totalSales = data.getDouble("total_sales");
            totalOrders = data.getInt("total_orders");
        } else if (data.has("sales_summary")) {
            // Alternative format
            JSONObject summary = data.getJSONObject("sales_summary");
            totalSales = summary.getDouble("total_amount");
            totalOrders = summary.getInt("total_orders");
        } else {
            totalSales = 0;
            totalOrders = 0;
        }
        
        // Extract payment methods data - handle both formats
        JSONArray paymentMethodsArray;
        if (data.has("payment_methods")) {
            paymentMethodsArray = data.getJSONArray("payment_methods");
        } else if (data.has("payment_distribution")) {
            paymentMethodsArray = data.getJSONArray("payment_distribution");
        } else {
            paymentMethodsArray = new JSONArray(); // Empty array as fallback
        }
        
        final List<PaymentMethod> paymentMethods = new ArrayList<>();
        
        for (int i = 0; i < paymentMethodsArray.length(); i++) {
            JSONObject methodObj = paymentMethodsArray.getJSONObject(i);
            String method;
            int count;
            double total;
            
            // Handle different field names
            if (methodObj.has("method")) {
                method = methodObj.getString("method");
            } else if (methodObj.has("payment_method")) {
                method = methodObj.getString("payment_method");
            } else {
                method = "Unknown";
            }
            
            if (methodObj.has("count")) {
                count = methodObj.getInt("count");
            } else if (methodObj.has("transaction_count")) {
                count = methodObj.getInt("transaction_count");
            } else {
                count = 0;
            }
            
            if (methodObj.has("total")) {
                total = methodObj.getDouble("total");
            } else if (methodObj.has("amount")) {
                total = methodObj.getDouble("amount");
            } else {
                total = 0;
            }
            
            PaymentMethod paymentMethod = new PaymentMethod(method, count, total);
            paymentMethods.add(paymentMethod);
        }
        
        // Extract recent sales data - handle both formats
        JSONArray recentSalesArray;
        if (data.has("recent_sales")) {
            recentSalesArray = data.getJSONArray("recent_sales");
        } else if (data.has("recent_transactions")) {
            recentSalesArray = data.getJSONArray("recent_transactions");
        } else {
            recentSalesArray = new JSONArray(); // Empty array as fallback
        }
        
        final List<Sale> recentSales = new ArrayList<>();
        
        for (int i = 0; i < recentSalesArray.length(); i++) {
            JSONObject saleObj = recentSalesArray.getJSONObject(i);
            
            int id = saleObj.optInt("sale_id", saleObj.optInt("id", 0));
            String invoiceNumber = saleObj.optString("invoice_number", saleObj.optString("transaction_id", "Unknown"));
            String customerName = saleObj.optString("customer_name", "Walk-in Customer");
            String date = saleObj.optString("date", saleObj.optString("transaction_date", "Unknown"));
            double amount = saleObj.optDouble("amount", saleObj.optDouble("total_amount", 0));
            String paymentMethod = saleObj.optString("payment_method", "Unknown");
            
            // Extract sale items
            List<SaleItem> items = new ArrayList<>();
            String itemsArrayKey = saleObj.has("items") ? "items" : (saleObj.has("products") ? "products" : "");
            
            if (!itemsArrayKey.isEmpty() && saleObj.has(itemsArrayKey)) {
                JSONArray itemsArray = saleObj.getJSONArray(itemsArrayKey);
                for (int j = 0; j < itemsArray.length(); j++) {
                    JSONObject itemObj = itemsArray.getJSONObject(j);
                    String productName = itemObj.optString("product_name", itemObj.optString("name", "Unknown"));
                    int quantity = itemObj.optInt("quantity", 1);
                    double unitPrice = itemObj.optDouble("unit_price", itemObj.optDouble("price_per_unit", 0));
                    double totalPrice = itemObj.optDouble("total_price", itemObj.optDouble("price", 0));
                    
                    SaleItem item = new SaleItem(productName, quantity, unitPrice, totalPrice);
                    items.add(item);
                }
            }
            
            Sale sale = new Sale(id, invoiceNumber, customerName, date, amount, paymentMethod, items);
            recentSales.add(sale);
        }
        
        // Update UI on main thread
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            
            // Update summary
            tvTotalSales.setText(currencyFormat.format(totalSales));
            tvTotalOrders.setText(String.valueOf(totalOrders));
            
            // Update payment methods
            paymentMethodAdapter.updateData(paymentMethods);
            
            // Update recent sales
            recentSalesAdapter.updateData(recentSales);
        });
    }
    
    private void showError(final String message) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(SalesReportsActivity.this, message, Toast.LENGTH_SHORT).show();
        });
    }
    
    // Model classes
    public static class PaymentMethod {
        private String method;
        private int count;
        private double total;
        
        public PaymentMethod(String method, int count, double total) {
            this.method = method;
            this.count = count;
            this.total = total;
        }
        
        public String getMethod() {
            return method;
        }
        
        public int getCount() {
            return count;
        }
        
        public double getTotal() {
            return total;
        }
    }
    
    public static class Sale {
        private int id;
        private String invoiceNumber;
        private String customerName;
        private String date;
        private double amount;
        private String paymentMethod;
        private List<SaleItem> items;
        
        public Sale(int id, String invoiceNumber, String customerName, String date, double amount, String paymentMethod, List<SaleItem> items) {
            this.id = id;
            this.invoiceNumber = invoiceNumber;
            this.customerName = customerName;
            this.date = date;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.items = items;
        }
        
        public int getId() {
            return id;
        }
        
        public String getInvoiceNumber() {
            return invoiceNumber;
        }
        
        public String getCustomerName() {
            return customerName;
        }
        
        public String getDate() {
            return date;
        }
        
        public double getAmount() {
            return amount;
        }
        
        public String getPaymentMethod() {
            return paymentMethod;
        }
        
        public List<SaleItem> getItems() {
            return items;
        }
    }
    
    public static class SaleItem {
        private String productName;
        private int quantity;
        private double unitPrice;
        private double totalPrice;
        
        public SaleItem(String productName, int quantity, double unitPrice, double totalPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
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
            return totalPrice;
        }
    }
    
    // Adapter classes
    private class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {
        private List<PaymentMethod> paymentMethods;
        
        public PaymentMethodAdapter(List<PaymentMethod> paymentMethods) {
            this.paymentMethods = paymentMethods;
        }
        
        public void updateData(List<PaymentMethod> newData) {
            this.paymentMethods = newData;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_payment_method, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PaymentMethod method = paymentMethods.get(position);
            
            // Format method name for display (e.g., "credit_card" -> "Credit Card")
            String displayName = method.getMethod().replace('_', ' ');
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
            
            holder.tvMethodName.setText(displayName);
            holder.tvMethodCount.setText(String.valueOf(method.getCount()));
            holder.tvMethodAmount.setText(currencyFormat.format(method.getTotal()));
        }
        
        @Override
        public int getItemCount() {
            return paymentMethods.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMethodName, tvMethodCount, tvMethodAmount;
            
            ViewHolder(View itemView) {
                super(itemView);
                tvMethodName = itemView.findViewById(R.id.tvMethodName);
                tvMethodCount = itemView.findViewById(R.id.tvMethodCount);
                tvMethodAmount = itemView.findViewById(R.id.tvMethodAmount);
            }
        }
    }
    
    private class RecentSalesAdapter extends RecyclerView.Adapter<RecentSalesAdapter.ViewHolder> {
        private List<Sale> sales;
        
        public RecentSalesAdapter(List<Sale> sales) {
            this.sales = sales;
        }
        
        public void updateData(List<Sale> newData) {
            this.sales = newData;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_recent_sale, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Sale sale = sales.get(position);
            
            holder.tvInvoiceNumber.setText(sale.getInvoiceNumber());
            holder.tvCustomerName.setText(sale.getCustomerName());
            holder.tvSaleDate.setText(sale.getDate());
            holder.tvSaleAmount.setText(currencyFormat.format(sale.getAmount()));
            
            // Format payment method for display
            String displayMethod = sale.getPaymentMethod().replace('_', ' ');
            displayMethod = displayMethod.substring(0, 1).toUpperCase() + displayMethod.substring(1);
            holder.tvPaymentMethod.setText(displayMethod);
            
            // Set up item click to show details
            holder.itemView.setOnClickListener(v -> {
                // Show sale details in a dialog or new activity
                // This could be implemented based on your app's design
                Toast.makeText(SalesReportsActivity.this, 
                        "Sale details for " + sale.getInvoiceNumber(), 
                        Toast.LENGTH_SHORT).show();
            });
        }
        
        @Override
        public int getItemCount() {
            return sales.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvInvoiceNumber, tvCustomerName, tvSaleDate, tvSaleAmount, tvPaymentMethod;
            
            ViewHolder(View itemView) {
                super(itemView);
                tvInvoiceNumber = itemView.findViewById(R.id.tvInvoiceNumber);
                tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
                tvSaleDate = itemView.findViewById(R.id.tvSaleDate);
                tvSaleAmount = itemView.findViewById(R.id.tvSaleAmount);
                tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            }
        }
    }
}