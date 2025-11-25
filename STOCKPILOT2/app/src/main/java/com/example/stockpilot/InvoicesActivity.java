package com.example.stockpilot;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.InvoiceModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class InvoicesActivity extends AppCompatActivity implements invoices_recycle.OnInvoiceClickListener {
    private static final String TAG = "invoices";

    private RecyclerView rvInvoices;
    private invoices_recycle adapter;
    private List<InvoiceModel> invoiceList;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupStatus;
    private FloatingActionButton fabAdd;

    
    private String currentSearchQuery = "";
    private String currentStatusFilter = "all";
    private int currentStoreId = 1; // You can get this from shared preferences or intent
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoices);

        initViews();
        setupRecyclerView();
        setupSearchAndFilters();
        setupFAB();

        
        loadInvoices();
    }

    private void initViews() {
        rvInvoices = findViewById(R.id.rv_invoices);
        etSearch = findViewById(R.id.et_search_invoices);
        chipGroupStatus = findViewById(R.id.chip_group_invoice_status);
        fabAdd = findViewById(R.id.fab_add_item);

        // Set default selected chip
        Chip chipAll = findViewById(R.id.chip_all_invoices);
        chipAll.setChecked(true);
    }

    private void setupRecyclerView() {
        invoiceList = new ArrayList<>();
        adapter = new invoices_recycle(this, invoiceList);
        adapter.setOnInvoiceClickListener(this);

        rvInvoices.setLayoutManager(new LinearLayoutManager(this));
        rvInvoices.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                loadInvoices();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Status filter functionality
        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                currentStatusFilter = chip.getTag().toString();
                loadInvoices();
            }
        });
    }

    private void setupFAB() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(InvoicesActivity.this, CreateInvoiceActivity.class);
            startActivity(intent);
        });
    }

    private void loadInvoices() {
        invoiceList.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onInvoiceClick(InvoiceModel invoice) {
        Intent intent = new Intent(this, InvoiceDetailsActivity.class);
        intent.putExtra("invoice_id", invoice.getInvoiceId());
        startActivity(intent);
    }

    @Override
    public void onInvoiceLongClick(InvoiceModel invoice) {
        // Implement actions for long click, e.g., show a context menu
        new AlertDialog.Builder(this)
                .setTitle("Invoice Actions")
                .setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit
                            // Implement edit functionality
                            break;
                        case 1: // Delete
                            // Implement delete functionality
                            break;
                    }
                })
                .show();
    }
}