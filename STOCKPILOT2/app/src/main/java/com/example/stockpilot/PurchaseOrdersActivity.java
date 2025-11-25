package com.example.stockpilot;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.UserSession;
import com.example.stockpilot.PurchaseOrderModel;
 
 

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PurchaseOrdersActivity extends AppCompatActivity implements PurchaseOrdersAdapter.OnPurchaseOrderClickListener {

    private static final String TAG = "PurchaseOrdersActivity";

    // UI Components
    private RecyclerView recyclerViewPurchaseOrders;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private EditText etSearch;
    private Spinner dropdownFilter, dropdownSort;
    private ImageButton btnBack, btnAdd;

    // Data
    private List<PurchaseOrderModel> purchaseOrderList;
    private List<PurchaseOrderModel> filteredPurchaseOrderList;
    private PurchaseOrdersAdapter adapter;
    private UserSession session;
    private String storeId;
    

    // Filter options
    private String currentFilter = "All";
    private String currentSort = "Date (Newest)";
    private String searchQuery = "";
    private View Spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_orders);

        // Initialize UI components
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Initialize data
        session = new UserSession(this);
        storeId = String.valueOf(session.getStoreId());
        

        // Setup RecyclerView
        setupRecyclerView();

        // Setup filters
        setupFilters();

        // Load purchase orders
        loadPurchaseOrders();
    }

    private void initializeViews() {
        recyclerViewPurchaseOrders = findViewById(R.id.recycler_purchase_orders);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        progressBar = findViewById(R.id.progress_bar);
        etSearch = findViewById(R.id.et_search);
        dropdownFilter = findViewById(R.id.spinner_filter);
        dropdownSort = findViewById(R.id.dropdown_sort);
        btnBack = findViewById(R.id.btn_back);
        btnAdd = findViewById(R.id.fab_add_po);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(PurchaseOrdersActivity.this, AddPurchaseOrderActivity.class);
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().toLowerCase().trim();
                filterPurchaseOrders();
            }
        });
    }

    private void setupRecyclerView() {
        purchaseOrderList = new ArrayList<>();
        filteredPurchaseOrderList = new ArrayList<>();
        adapter = new PurchaseOrdersAdapter(filteredPurchaseOrderList, this);
        recyclerViewPurchaseOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPurchaseOrders.setAdapter(adapter);
    }

    private void setupFilters() {
        // Setup status filter
        String[] statusOptions = {"All", "Draft", "Sent", "Received", "Cancelled"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownFilter.setAdapter(statusAdapter);
        dropdownFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = statusOptions[position];
                filterPurchaseOrders();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup sort options
        String[] sortOptions = {"Date (Newest)", "Date (Oldest)", "Total (High to Low)", "Total (Low to High)"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownSort.setAdapter(sortAdapter);
        dropdownSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSort = sortOptions[position];
                filterPurchaseOrders();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadPurchaseOrders() {
        showProgress(true);

        showProgress(false);
        updateEmptyState(true, "Data layer removed");
    }

    private void parsePurchaseOrders(List<Map<String, Object>> ordersArray) {
        purchaseOrderList.clear();

        for (Map<String, Object> orderObject : ordersArray) {
            String id = (String) orderObject.get("id");
            String poNumber = (String) orderObject.get("po_number");
            String vendorId = (String) orderObject.get("vendor_id");
            String vendorName = (String) orderObject.get("vendor_name");
            String storeId = (String) orderObject.get("store_id");
            String poDate = (String) orderObject.get("po_date");
            String expectedDate = (String) orderObject.get("expected_date");
            String status = (String) orderObject.get("status");
            double subtotal = ((Number) orderObject.get("subtotal")).doubleValue();
            double tax = ((Number) orderObject.get("tax")).doubleValue();
            double discount = ((Number) orderObject.get("discount")).doubleValue();
            double total = ((Number) orderObject.get("total")).doubleValue();
            String notes = (String) orderObject.get("notes");
            String createdAt = (String) orderObject.get("created_at");
            String updatedAt = (String) orderObject.get("updated_at");

            PurchaseOrderModel order = new PurchaseOrderModel(
                    id, poNumber, vendorId, vendorName, storeId,
                    poDate, expectedDate, status, subtotal,
                    tax, discount, total, notes, createdAt, updatedAt
            );

            purchaseOrderList.add(order);
        }

        if (purchaseOrderList.isEmpty()) {
            updateEmptyState(true, "No purchase orders found");
        } else {
            updateEmptyState(false, "");
            filterPurchaseOrders();
        }
    }

    private void filterPurchaseOrders() {
        filteredPurchaseOrderList.clear();

        for (PurchaseOrderModel order : purchaseOrderList) {
            // Apply status filter
            if (!currentFilter.equals("All") && !order.getStatus().equalsIgnoreCase(currentFilter)) {
                continue;
            }

            // Apply search filter
            if (!searchQuery.isEmpty() &&
                    !(order.getPoNumber().toLowerCase().contains(searchQuery) ||
                            order.getVendorName().toLowerCase().contains(searchQuery))) {
                continue;
            }

            filteredPurchaseOrderList.add(order);
        }

        // Apply sorting
        sortPurchaseOrders();

        // Update UI
        adapter.notifyDataSetChanged();
        updateEmptyState(filteredPurchaseOrderList.isEmpty(), "No matching purchase orders found");
    }

    private void sortPurchaseOrders() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        switch (currentSort) {
            case "Date (Newest)":
                Collections.sort(filteredPurchaseOrderList, (o1, o2) -> {
                    try {
                        Date date1 = dateFormat.parse(o1.getPoDate());
                        Date date2 = dateFormat.parse(o2.getPoDate());
                        return date2.compareTo(date1); // Descending order
                    } catch (ParseException e) {
                        return 0;
                    }
                });
                break;

            case "Date (Oldest)":
                Collections.sort(filteredPurchaseOrderList, (o1, o2) -> {
                    try {
                        Date date1 = dateFormat.parse(o1.getPoDate());
                        Date date2 = dateFormat.parse(o2.getPoDate());
                        return date1.compareTo(date2); // Ascending order
                    } catch (ParseException e) {
                        return 0;
                    }
                });
                break;

            case "Total (High to Low)":
                Collections.sort(filteredPurchaseOrderList, (o1, o2) ->
                        Double.compare(o2.getTotal(), o1.getTotal())); // Descending order
                break;

            case "Total (Low to High)":
                Collections.sort(filteredPurchaseOrderList, Comparator.comparingDouble(PurchaseOrderModel::getTotal)); // Ascending order
                break;
        }
    }

    private void updateEmptyState(boolean isEmpty, String message) {
        if (isEmpty) {
            recyclerViewPurchaseOrders.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            TextView tvEmptyStateMessage = findViewById(R.id.tv_empty_state_message);
            tvEmptyStateMessage.setText(message);
        } else {
            recyclerViewPurchaseOrders.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recyclerViewPurchaseOrders.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPurchaseOrderClick(PurchaseOrderModel order) {
        Intent intent = new Intent(PurchaseOrdersActivity.this, EditPurchaseOrderActivity.class);
        intent.putExtra("purchase_order_id", order.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload purchase orders when returning to this activity
        loadPurchaseOrders();
    }
}