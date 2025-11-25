package com.example.stockpilot;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
 
import com.example.stockpilot.AdjustmentAdapter;
 
import com.example.stockpilot.UserSession;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class InventoryAdjustmentsActivity extends AppCompatActivity {
    private static final String TAG = "InventoryAdjustments";

    private UserSession userSession;
    
    private Gson gson;

    private TextView tvStoreName;
    private Spinner spinnerMovementType;
    private Spinner spinnerReferenceType;
    private Spinner spinnerQuickFilter;
    private EditText etSearchProduct;
    private RecyclerView recyclerViewAdjustments;
    private ProgressBar progressBar;
    private AdjustmentAdapter adjustmentAdapter;
    private List<AdjustmentItem> adjustmentList;

    private String[] movementTypes = {"All", "In", "Out", "Transfer", "Adjustment"};
    private String[] referenceTypes = {"All", "Purchase", "Sale", "Return", "Damage", "Expired", "Stock Count", "Manual"};
    private String[] quickFilters = {"All", "In", "Out", "Today", "This Week", "This Month"};

    private String currentMovementType = "All";
    private String currentReferenceType = "All";
    private String currentQuickFilter = "All";
    private String currentSearchProduct = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_adjustments);

        userSession = UserSession.getInstance(getApplicationContext());
        
        gson = new Gson();

        tvStoreName = findViewById(R.id.tvStoreName);
        spinnerMovementType = findViewById(R.id.spinnerMovementType);
        spinnerReferenceType = findViewById(R.id.spinnerReferenceType);
        spinnerQuickFilter = findViewById(R.id.spinnerQuickFilter);
        etSearchProduct = findViewById(R.id.etSearch);
        recyclerViewAdjustments = findViewById(R.id.rvAdjustments);
        progressBar = findViewById(R.id.progressBar);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnFilter = findViewById(R.id.btnFilter);
        View filterPanel = findViewById(R.id.filterPanel);

        tvStoreName.setText(userSession.getStoreName());

        setupSpinner(spinnerMovementType, movementTypes, new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentMovementType = movementTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional
            }
        });
        setupSpinner(spinnerReferenceType, referenceTypes, new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentReferenceType = referenceTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setupSpinner(spinnerQuickFilter, quickFilters, new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentQuickFilter = quickFilters[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        adjustmentList = new ArrayList<>();
        adjustmentAdapter = new AdjustmentAdapter(InventoryAdjustmentsActivity.this, (List<com.example.stockpilot.AdjustmentItem>) InventoryAdjustmentsActivity.this);
        recyclerViewAdjustments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdjustments.setAdapter(adjustmentAdapter);

        findViewById(R.id.btnApplyFilter).setOnClickListener(v -> {
            currentSearchProduct = etSearchProduct.getText().toString();
            fetchAdjustments();
            if (filterPanel.getVisibility() == View.VISIBLE) {
                filterPanel.setVisibility(View.GONE);
            }
        });

        btnBack.setOnClickListener(v -> onBackPressed());

        btnFilter.setOnClickListener(v -> {
            if (filterPanel.getVisibility() == View.GONE) {
                filterPanel.setVisibility(View.VISIBLE);
            } else {
                filterPanel.setVisibility(View.GONE);
            }
        });

        fetchAdjustments();
    }

    private void setupSpinner(Spinner spinner, String[] items, AdapterView.OnItemSelectedListener listener) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(listener);
    }

    private void fetchAdjustments() {
        progressBar.setVisibility(View.GONE);
        adjustmentList.clear();
        adjustmentAdapter.notifyDataSetChanged();
    }

    // Data model
    public static class AdjustmentItem {
        public int movement_id;
        public String product_name;
        public String movement_type;
        public String formatted_quantity;
        public String reference_type;
        public String movement_date;
        public float unit_price;
        public String performed_by;
    }
}
