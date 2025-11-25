package com.example.stockpilot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


 
import com.example.stockpilot.R;
import com.example.stockpilot.UserSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BatchListActivity extends AppCompatActivity {

    private RecyclerView batchRecyclerView;
    private EditText searchProductEditText;
    private Button filterButton;
    private ImageButton backButton, addButton;
    private Spinner sortSpinner, statusSpinner;
    private LinearLayout emptyStateLayout;
    
    private BatchListAdapter batchAdapter;
    private List<BatchItem> batchList;
    private List<BatchItem> filteredBatchList;
    
    private UserSession userSession;
    
    
    private boolean isFilterVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_list);

        initializeViews();
        setupClickListeners();
        setupSpinners();
        
        userSession = new UserSession(this);
        
        batchList = new ArrayList<>();
        filteredBatchList = new ArrayList<>();
        
        setupRecyclerView();
        loadBatchData();
    }

    private void initializeViews() {
        batchRecyclerView = findViewById(R.id.batch_recycler_view);
        searchProductEditText = findViewById(R.id.search_product_edit_text);
        filterButton = findViewById(R.id.filter_button);
        backButton = findViewById(R.id.back_button);
        addButton = findViewById(R.id.add_button);
        sortSpinner = findViewById(R.id.sort_spinner);
        statusSpinner = findViewById(R.id.status_spinner);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BatchListActivity.this, AddBatchActivity.class);
                intent.putExtra("store_id", userSession.getStoreId());
                intent.putExtra("store_name", userSession.getStoreName());
                startActivity(intent);
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFilterVisible = !isFilterVisible;
            }
        });

        searchProductEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    filterBatches();
                }
            }
        });
    }

    private void setupSpinners() {
        // Sort spinner
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this,
                R.array.batch_sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortBatches(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Status spinner
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.batch_status_filter, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterBatches();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        batchAdapter = new BatchListAdapter(filteredBatchList);
        batchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        batchRecyclerView.setAdapter(batchAdapter);
    }

    private void loadBatchData() {
        batchList.clear();
        filteredBatchList.clear();
        updateUI();
    }

    private void processBatchData(List<Map<String, Object>> batches) {
        batchList.clear();
        
        for (Map<String, Object> batch : batches) {
            BatchItem batchItem = new BatchItem();
            batchItem.setBatchId((String) batch.get("batch_id"));
            batchItem.setProductCode((String) batch.get("product_code"));
            batchItem.setProductName((String) batch.get("product_name"));
            batchItem.setManufacturingDate((String) batch.get("manufacturing_date"));
            batchItem.setExpiryDate((String) batch.get("expiry_date"));
            batchItem.setQuantity(Integer.parseInt(batch.get("quantity").toString()));
            batchItem.setStatus((String) batch.get("status"));
            batchList.add(batchItem);
        }
        
        filterBatches();
    }

    private void filterBatches() {
        String searchText = searchProductEditText.getText().toString().toLowerCase().trim();
        String selectedStatus = statusSpinner.getSelectedItem().toString();
        
        filteredBatchList.clear();
        
        for (BatchItem batch : batchList) {
            boolean matchesSearch = searchText.isEmpty() || 
                    batch.getProductCode().toLowerCase().contains(searchText) ||
                    batch.getProductName().toLowerCase().contains(searchText);
            
            boolean matchesStatus = selectedStatus.equals("All") || 
                    batch.getStatus().equals(selectedStatus);
            
            if (matchesSearch && matchesStatus) {
                filteredBatchList.add(batch);
            }
        }
        
        updateUI();
    }

    private void sortBatches(int sortOption) {
        switch (sortOption) {
            case 0: // Product Code
                filteredBatchList.sort((a, b) -> a.getProductCode().compareTo(b.getProductCode()));
                break;
            case 1: // Expiry Date (ascending)
                filteredBatchList.sort((a, b) -> a.getExpiryDate().compareTo(b.getExpiryDate()));
                break;
            case 2: // Quantity (descending)
                filteredBatchList.sort((a, b) -> Integer.compare(b.getQuantity(), a.getQuantity()));
                break;
        }
        
        batchAdapter.notifyDataSetChanged();
    }

    private void updateUI() {
        batchAdapter.notifyDataSetChanged();
        
        if (filteredBatchList.isEmpty()) {
            batchRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            batchRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBatchData();
    }

    // Inner class for batch items
    public static class BatchItem {
        private String batchId;
        private String productCode;
        private String productName;
        private String manufacturingDate;
        private String expiryDate;
        private int quantity;
        private String status;

        public String getBatchId() { return batchId; }
        public void setBatchId(String batchId) { this.batchId = batchId; }
        
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public String getManufacturingDate() { return manufacturingDate; }
        public void setManufacturingDate(String manufacturingDate) { this.manufacturingDate = manufacturingDate; }
        
        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}