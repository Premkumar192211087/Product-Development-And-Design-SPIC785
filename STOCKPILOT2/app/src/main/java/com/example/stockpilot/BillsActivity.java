
package com.example.stockpilot;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

 

public class BillsActivity extends AppCompatActivity implements BillsAdapter.OnBillClickListener {
    // Implement the required interface method
    @Override
    public void onPaymentClick(Bills bill) {
        // Handle payment click
        Toast.makeText(this, "Payment for bill " + bill.getBillNumber(), Toast.LENGTH_SHORT).show();
        // Add implementation as needed
    }

    private static final String TAG = "BillsActivity";

    // UI Components
    private RecyclerView recyclerViewBills;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    private EditText etSearch;
    private AutoCompleteTextView dropdownFilter, dropdownSort;
    private FloatingActionButton fabAdd;

    // Data
    private List<Bills> billsList;
    private List<Bills> filteredBillsList;
    private BillsAdapter adapter;
    private UserSession session;
    private String storeId;
    

    // Filter options
    private String currentFilter = "All";
    private String currentSort = "Date (Newest)";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bills);

        // Initialize UI components
        initializeViews();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.bills);
        }

        // Initialize data
        session = new UserSession(this);
        storeId = String.valueOf(session.getStoreId());
        

        // Setup RecyclerView
        setupRecyclerView();

        // Setup filters
        setupFilters();

        // Setup click listeners
        setupClickListeners();

        // Load bills
        loadBills();
    }

    private void initializeViews() {
        recyclerViewBills = findViewById(R.id.rv_bills);
        emptyStateLayout = findViewById(R.id.layout_empty_state);
        progressBar = findViewById(R.id.progress_bar);
        etSearch = findViewById(R.id.et_search);
        dropdownFilter = findViewById(R.id.dropdown_status);
        dropdownSort = findViewById(R.id.dropdown_sort);
        fabAdd = findViewById(R.id.fab_add);
    }

    private void setupRecyclerView() {
        billsList = new ArrayList<>();
        filteredBillsList = new ArrayList<>();
        adapter = new BillsAdapter(filteredBillsList, this, this);
        recyclerViewBills.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBills.setAdapter(adapter);
    }

    private void setupFilters() {
        // Setup status filter
        String[] statusOptions = {"All", "Pending", "Paid", "Partial", "Cancelled"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statusOptions);
        dropdownFilter.setAdapter(statusAdapter);
        dropdownFilter.setText(statusOptions[0], false);
        dropdownFilter.setOnItemClickListener((parent, view, position, id) -> {
            currentFilter = statusOptions[position];
            filterBills();
        });

        // Setup sort options
        String[] sortOptions = {"Date (Newest)", "Date (Oldest)", "Amount (High to Low)", "Amount (Low to High)"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sortOptions);
        dropdownSort.setAdapter(sortAdapter);
        dropdownSort.setText(sortOptions[0], false);
        dropdownSort.setOnItemClickListener((parent, view, position, id) -> {
            currentSort = sortOptions[position];
            filterBills();
        });

        // Setup search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().toLowerCase().trim();
                filterBills();
            }
        });
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> {
            // Navigate to add bill activity
            // Intent intent = new Intent(BillsActivity.this, AddBillActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Add bill functionality coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadBills() {
        progressBar.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
        billsList = new ArrayList<>();
        filteredBillsList = new ArrayList<>();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private void showEmptyState(String message) {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            TextView tvEmptyState = findViewById(R.id.tv_empty_state_message);
            if (tvEmptyState != null) {
                tvEmptyState.setText(message);
            }
        }
    }

    private void filterBills() {
        filteredBillsList.clear();

        for (Bills bill : billsList) {
            // Apply status filter
            if (!currentFilter.equals("All") && !bill.getStatus().equalsIgnoreCase(currentFilter)) {
                continue;
            }

            // Apply search filter
            if (!searchQuery.isEmpty() &&
                    !bill.getBillNumber().toLowerCase().contains(searchQuery) &&
                    !bill.getVendorName().toLowerCase().contains(searchQuery)) {
                continue;
            }

            filteredBillsList.add(bill);
        }

        // Apply sorting
        switch (currentSort) {
            case "Date (Newest)":
                filteredBillsList.sort((b1, b2) -> b2.getBillDate().compareTo(b1.getBillDate()));
                break;
            case "Date (Oldest)":
                filteredBillsList.sort((b1, b2) -> b1.getBillDate().compareTo(b2.getBillDate()));
                break;
            case "Amount (High to Low)":
                filteredBillsList.sort((b1, b2) -> Double.compare(b2.getAmount(), b1.getAmount()));
                break;
            case "Amount (Low to High)":
                filteredBillsList.sort((b1, b2) -> Double.compare(b1.getAmount(), b2.getAmount()));
                break;
        }

        adapter.notifyDataSetChanged();

        // Show empty state if no bills match filters
        if (filteredBillsList.isEmpty()) {
            showEmptyState("No bills match your filters");
        } else {
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBillClick(Bills bill) {
        android.content.Intent intent = new android.content.Intent(BillsActivity.this, BillDetailsActivity.class);
        intent.putExtra("bill_id", bill.getId());
        startActivity(intent);
    }

    @Override
    public void onPayBillClick(Bills bill) {
        // Show payment dialog
        showPaymentDialog(bill);
    }

    private void showPaymentDialog(Bills bill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Record Payment");
        builder.setMessage("This feature is coming soon!");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh bills when returning to this activity
        loadBills();
    }
}
