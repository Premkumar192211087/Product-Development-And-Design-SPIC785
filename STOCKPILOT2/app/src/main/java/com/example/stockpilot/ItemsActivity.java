package com.example.stockpilot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.ItemsAdapter;
import com.example.stockpilot.Item;
import com.example.stockpilot.UserSession;
import com.example.stockpilot.ItemsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ItemsActivity extends AppCompatActivity {

    private static final String TAG = "ItemsActivity";
    private ItemsViewModel viewModel;
    private ItemsAdapter itemsAdapter;
    private List<Item> itemsList;
    private UserSession userSession;

    private String currentStatus = "All";
    private String currentSortBy = "product_name";
    private String currentSortOrder = "ASC";

    // View references
    private TextView tvStoreName;
    private ImageView backIcon, searchIcon;
    private Spinner spinnerStatus, spinnerSortBy, spinnerSortOrder;
    private RecyclerView itemsRecyclerView;
    private FloatingActionButton fabAddItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        // Find views by ID
        tvStoreName = findViewById(R.id.tv_store_name);
        backIcon = findViewById(R.id.back_icon);
        searchIcon = findViewById(R.id.search_icon);
        spinnerStatus = findViewById(R.id.spinner_status);
        spinnerSortBy = findViewById(R.id.spinner_sort_by);
        spinnerSortOrder = findViewById(R.id.spinner_sort_order);
        itemsRecyclerView = findViewById(R.id.items_recycler_view);
        fabAddItem = findViewById(R.id.fab_add_item);

        viewModel = new ViewModelProvider(this).get(ItemsViewModel.class);
        initializeViews();
        setupUserSession();
        setupSpinners();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void initializeViews() {
        itemsList = new ArrayList<>();
    }

    private void setupUserSession() {
        userSession = UserSession.getInstance(this);

        if (!userSession.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String storeName = userSession.getStoreName();
        tvStoreName.setText(storeName.isEmpty() ? "Store Name" : storeName);
    }

    private void observeViewModel() {
        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            // Uncomment if you add a progress bar with id progress_bar in XML
            // binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Snackbar.make(itemsRecyclerView, errorMsg, Snackbar.LENGTH_LONG).show();
            }
        });

        // Observe items data
        viewModel.getItems().observe(this, items -> {
            if (items != null) {
                itemsList.clear();
                itemsList.addAll(items);
                itemsAdapter.notifyDataSetChanged();
                // Uncomment if you add an empty view with id empty_view in XML
                // binding.emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                itemsRecyclerView.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        // Load initial data
        viewModel.loadItems(userSession.getStoreId());
    }

    private void setupSpinners() {
        String[] statusOptions = {"All", "In Stock", "Low Stock", "Out of Stock"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentStatus = statusOptions[position];
                viewModel.setStatus(currentStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<CharSequence> sortByAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_by_options, android.R.layout.simple_spinner_item);
        sortByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortBy.setAdapter(sortByAdapter);
        spinnerSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] sortByValues = {"product_name", "sku", "category", "quantity", "price"};
                currentSortBy = sortByValues[position];
                viewModel.setSortBy(currentSortBy);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<CharSequence> sortOrderAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_order_options, android.R.layout.simple_spinner_item);
        sortOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOrder.setAdapter(sortOrderAdapter);
        spinnerSortOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortOrder = position == 0 ? "ASC" : "DESC";
                viewModel.setSortOrder(currentSortOrder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerView() {
        itemsAdapter = new ItemsAdapter(this, itemsList);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemsRecyclerView.setAdapter(itemsAdapter);
    }

    private void setupClickListeners() {
        backIcon.setOnClickListener(v -> finish());

        searchIcon.setOnClickListener(v -> {
            // Implement search functionality
            Toast.makeText(this, "Search functionality coming soon", Toast.LENGTH_SHORT).show();
        });

        fabAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(ItemsActivity.this, AdditemActivity.class);
            startActivity(intent);
        });
    }

    private void loadItems() {
        String storeId = userSession.getStoreId();
        Log.d(TAG, "Store ID: " + storeId);

        if (storeId == null || storeId.trim().isEmpty() || storeId.equals("0")) {
            Log.e(TAG, "Invalid store ID. Please log in again.");
            return;
        }

        // Use the ViewModel to load items
        viewModel.loadItems(storeId,
                currentStatus.equals("All") ? "" : currentStatus,
                currentSortBy,
                currentSortOrder);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }
}
