package com.example.stockpilot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.SalesOrderAdapter;
import com.example.stockpilot.SalesOrder;
import com.example.stockpilot.UserSession;
 

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SalesOrdersActivity extends AppCompatActivity {

    private TextView tvStoreName, tvTotalOrders, tvTotalAmount;
    private RecyclerView rvSalesOrders;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;

    private Spinner spinnerDateRange, spinnerPaymentStatus, spinnerPaymentMethod, spinnerAmountRange;
    private EditText etSearch;
    private Button btnSearch, btnClearFilters, btnApplyFilters;
    private SalesOrderAdapter adapter;
    private List<SalesOrder> salesOrderList = new ArrayList<>();

    private UserSession session;
    private String storeId;
    private static final String TAG = "SalesOrdersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_orders);

        session = UserSession.getInstance(this);
        storeId = String.valueOf(session.getStoreId());
        
        

        initViews();
        setupRecyclerView();
        setupFilters();
        fetchSalesOrders();
    }

    private void initViews() {
        tvStoreName = findViewById(R.id.tv_store_name);
        tvStoreName.setText(session.getStoreName());

        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        progressBar = findViewById(R.id.progress_bar);

        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        btnClearFilters = findViewById(R.id.btn_clear_filters);
        btnApplyFilters = findViewById(R.id.btnApplyFilter);

        spinnerDateRange = findViewById(R.id.spinner_date_range);
        spinnerPaymentStatus = findViewById(R.id.spinner_payment_status);
        spinnerPaymentMethod = findViewById(R.id.spinner_payment_method);
        spinnerAmountRange = findViewById(R.id.spinner_amount_range);

        findViewById(R.id.iv_back_arrow).setOnClickListener(v -> finish()); // Use finish() instead of onBackPressed()

        findViewById(R.id.iv_add_sale).setOnClickListener(v -> {
            Intent intent = new Intent(SalesOrdersActivity.this, AddSaleActivity.class);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> fetchSalesOrders());
        btnApplyFilters.setOnClickListener(v -> fetchSalesOrders());

        btnClearFilters.setOnClickListener(v -> {
            spinnerDateRange.setSelection(0);
            spinnerPaymentStatus.setSelection(0);
            spinnerPaymentMethod.setSelection(0);
            spinnerAmountRange.setSelection(0);
            etSearch.setText("");
            fetchSalesOrders();
        });


    }

    private void setupRecyclerView() {
        rvSalesOrders = findViewById(R.id.rv_sales_orders);
        rvSalesOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SalesOrderAdapter(salesOrderList, this);
        rvSalesOrders.setAdapter(adapter);
    }

    private void setupFilters() {
        // Set up date range spinner
        ArrayAdapter<CharSequence> dateRangeAdapter = ArrayAdapter.createFromResource(
                this, R.array.date_range_options, android.R.layout.simple_spinner_item);
        dateRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDateRange.setAdapter(dateRangeAdapter);
        
        // Set up payment status spinner
        ArrayAdapter<CharSequence> paymentStatusAdapter = ArrayAdapter.createFromResource(
                this, R.array.payment_status_options, android.R.layout.simple_spinner_item);
        paymentStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentStatus.setAdapter(paymentStatusAdapter);
        
        // Set up payment method spinner
        ArrayAdapter<CharSequence> paymentMethodAdapter = ArrayAdapter.createFromResource(
                this, R.array.payment_method_options, android.R.layout.simple_spinner_item);
        paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMethod.setAdapter(paymentMethodAdapter);
        
        // Set up amount range spinner
        ArrayAdapter<CharSequence> amountRangeAdapter = ArrayAdapter.createFromResource(
                this, R.array.amount_range_options, android.R.layout.simple_spinner_item);
        amountRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAmountRange.setAdapter(amountRangeAdapter);
    }

    private void fetchSalesOrders() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        salesOrderList.clear();

        // Get filter values
        String dateRange = spinnerDateRange.getSelectedItem() != null ? spinnerDateRange.getSelectedItem().toString() : "";
        String paymentStatus = spinnerPaymentStatus.getSelectedItem() != null ? spinnerPaymentStatus.getSelectedItem().toString() : "";
        String paymentMethod = spinnerPaymentMethod.getSelectedItem() != null ? spinnerPaymentMethod.getSelectedItem().toString() : "";
        String amountRange = spinnerAmountRange.getSelectedItem() != null ? spinnerAmountRange.getSelectedItem().toString() : "";
        String searchQuery = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";

        layoutEmptyState.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    // Helper method to safely get string values from Map
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }
}
