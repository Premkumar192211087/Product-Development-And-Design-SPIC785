package com.example.stockpilot;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.stockpilot.R;
import com.example.stockpilot.VendorsAdapter;
import com.example.stockpilot.databinding.ActivityVendorsBinding;
import com.example.stockpilot.UserSession;
import com.example.stockpilot.Vendor;
import com.example.stockpilot.Constants;
import com.example.stockpilot.VendorViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class VendorsActivity extends AppCompatActivity implements VendorsAdapter.OnVendorActionListener {

    private static final String TAG = "VendorsActivity";
    
    private ActivityVendorsBinding binding;
    private VendorViewModel viewModel;
    private VendorsAdapter vendorAdapter;
    private List<Vendor> vendorList;
    
    private String storeId;
    private String storeName;
    private UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVendorsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(VendorViewModel.class);
        
        // Initialize data
        vendorList = new ArrayList<>();
        
        // Setup user session
        setupUserSession();
        
        // Initialize UI components
        setupRecyclerView();
        setupSpinners();
        setupSearchFilter();
        setupClickListeners();
        
        // Observe ViewModel data
        observeViewModel();
        
        // Load vendors
        viewModel.setStoreId(storeId);
    }
    
    private void setupUserSession() {
        userSession = UserSession.getInstance(this);
        if (!userSession.isLoggedIn()) {
            Log.e(TAG, "User not logged in");
            finish();
            return;
        }

        storeId = String.valueOf(userSession.getStoreId());
        storeName = userSession.getStoreName();

        if (storeId.isEmpty()) {
            Log.e(TAG, "Store ID not found");
            finish();
            return;
        }

        // Set the store name in the header
        binding.tvStoreName.setText(storeName);
    }
    
    private void setupRecyclerView() {
        vendorAdapter = new VendorsAdapter(vendorList, this);
        binding.rvVendors.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVendors.setAdapter(vendorAdapter);
    }
    
    private void setupSpinners() {
        // Status spinner
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this, R.array.vendor_status_filter_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(statusAdapter);
        binding.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String status = parent.getItemAtPosition(position).toString();
                if (status.equals("All")) {
                    viewModel.setSearchQuery(binding.etSearch.getText().toString());
                } else {
                    viewModel.setSearchQuery(binding.etSearch.getText().toString() + " status:" + status);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Sort spinner
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this, R.array.vendor_sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSort.setAdapter(sortAdapter);
        binding.spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sortOption = parent.getItemAtPosition(position).toString();
                if (sortOption.equals("Name (A-Z)")) {
                    viewModel.setSortBy(Constants.SORT_BY_NAME);
                    viewModel.setSortOrder(Constants.SORT_ASC);
                } else if (sortOption.equals("Name (Z-A)")) {
                    viewModel.setSortBy(Constants.SORT_BY_NAME);
                    viewModel.setSortOrder(Constants.SORT_DESC);
                } else if (sortOption.equals("Recently Added")) {
                    viewModel.setSortBy("created_at");
                    viewModel.setSortOrder(Constants.SORT_DESC);
                } else if (sortOption.equals("Oldest First")) {
                    viewModel.setSortBy("created_at");
                    viewModel.setSortOrder(Constants.SORT_ASC);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    
    private void setupSearchFilter() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setSearchQuery(s.toString());
            }
        });
    }
    
    private void setupClickListeners() {
        binding.ivBack.setOnClickListener(v -> finish());
    }
    
    private void observeViewModel() {
        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe error messages
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Snackbar.make(binding.main, errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });
        
        // Observe vendors data
        viewModel.getVendors().observe(this, vendors -> {
            if (vendors != null) {
                vendorList.clear();
                vendorList.addAll(vendors);
                vendorAdapter.notifyDataSetChanged();
                
                // Show empty state if no vendors
                updateEmptyState();
            }
        });
    }
    
    private void updateEmptyState() {
        if (vendorList.isEmpty()) {
            binding.llEmptyState.setVisibility(View.VISIBLE);
            binding.rvVendors.setVisibility(View.GONE);
        } else {
            binding.llEmptyState.setVisibility(View.GONE);
            binding.rvVendors.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && 
            (requestCode == Constants.REQUEST_CODE_ADD_VENDOR || 
             requestCode == Constants.REQUEST_CODE_EDIT_VENDOR)) {
            // Refresh vendor list
            viewModel.loadVendors(userSession.getStoreId());
        }
    }

    // VendorsAdapter.OnVendorActionListener implementation
    @Override
    public void onEditVendor(Vendor vendor) {
        Intent intent = new Intent(this, AddEditVendorActivity.class);
        intent.putExtra("vendor", vendor);
        startActivityForResult(intent, Constants.REQUEST_CODE_EDIT_VENDOR);
    }

    @Override
    public void onDeleteVendor(Vendor vendor) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Vendor")
                .setMessage("Are you sure you want to delete this vendor?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteVendor(vendor.getId()).observe(this, response -> {
                        if (response.isSuccess() && response.getData() != null && response.getData()) {
                            Snackbar.make(binding.main, "Vendor deleted successfully", Snackbar.LENGTH_SHORT).show();
                            viewModel.refreshVendors();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}