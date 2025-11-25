package com.example.stockpilot;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.PackagesAdapter;
 
import com.example.stockpilot.Package;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PackagesActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ChipGroup chipGroupPackageStatus;
    private Chip chipAllPackages, chipPacked, chipShipped, chipDelivered;
    private RecyclerView rvPackages;

    private PackagesAdapter packagesAdapter;
    private List<Package> packagesList;
    private ProgressDialog progressDialog;
    

    private int storeId;
    private String currentFilter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packages);

        // Get store_id from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("StockPilotPrefs", Context.MODE_PRIVATE);
        storeId = sharedPreferences.getInt("store_id", -1);

        if (storeId == -1) {
            Toast.makeText(this, "Store ID not found. Please login again.", Toast.LENGTH_LONG).show();
            LoginActivity.logoutUser(this);
            return;
        }

        // Initialize Retrofit
        

        initViews();
        setupRecyclerView();
        setupChipFilters();
        loadPackages("");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_packages);
        chipGroupPackageStatus = findViewById(R.id.chip_group_package_status);
        chipAllPackages = findViewById(R.id.chip_all_packages);
        chipPacked = findViewById(R.id.chip_packed);
        chipShipped = findViewById(R.id.chip_shipped);
        chipDelivered = findViewById(R.id.chip_delivered);
        rvPackages = findViewById(R.id.rv_packages);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        packagesList = new ArrayList<>();
        packagesAdapter = new PackagesAdapter(this, packagesList);
        rvPackages.setLayoutManager(new LinearLayoutManager(this));
        rvPackages.setAdapter(packagesAdapter);
    }

    private void setupChipFilters() {
        chipAllPackages.setChecked(true);

        chipGroupPackageStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipAllPackages.setChecked(true);
                return;
            }

            int checkedId = checkedIds.get(0);
            String filter = "";

            if (checkedId == R.id.chip_packed) {
                filter = "packed";
            } else if (checkedId == R.id.chip_shipped) {
                filter = "shipped";
            } else if (checkedId == R.id.chip_delivered) {
                filter = "delivered";
            }

            currentFilter = filter;
            loadPackages(filter);
        });
    }

    private void loadPackages(String status) {
        packagesList.clear();
        packagesAdapter.notifyDataSetChanged();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Packages (0)");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
