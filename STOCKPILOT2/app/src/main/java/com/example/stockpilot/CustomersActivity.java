
package com.example.stockpilot;



import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomersActivity extends AppCompatActivity implements CustomersAdapter.OnCustomerActionListener {

    private static final String TAG = "CustomersActivity";

    private RecyclerView rvCustomers;
    private CustomersAdapter customerAdapter;
    private List<Customer> customerList;
    private List<Customer> filteredCustomerList;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;
    private EditText etSearch;
    private Spinner spinnerStatus, spinnerSort;
    private TextView tvStoreName;
    private ImageView ivBack, ivAddCustomer;

    
    private UserSession userSession;
    private String storeId, storeName;

    // Filter and sort variables
    private String currentSearchQuery = "";
    private String currentStatusFilter = "All";
    private String currentSortOption = "Name (A-Z)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers);

        Log.d(TAG, "onCreate: Initializing CustomersActivity");

        initializeViews();
        setupUserSession();
        setupRecyclerView();
        setupListeners();
        loadCustomers();
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews: Setting up UI components");

        rvCustomers = findViewById(R.id.rv_customers);
        llEmptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        etSearch = findViewById(R.id.etSearch);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerSort = findViewById(R.id.spinner_sort);
        tvStoreName = findViewById(R.id.tvStoreName);
        ivBack = findViewById(R.id.ivBack);
        ivAddCustomer = findViewById(R.id.iv_add_customer);

        
    }

    private void setupUserSession() {
        Log.d(TAG, "setupUserSession: Retrieving user session data");

        userSession = new UserSession(this);
        storeId = String.valueOf(userSession.getStoreId());
        storeName = userSession.getStoreName();

        Log.d(TAG, "setupUserSession: Store ID = " + storeId + ", Store Name = " + storeName);

        if (tvStoreName != null) {
            tvStoreName.setText(storeName != null ? storeName : "Store");
        }
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Configuring RecyclerView");

        customerList = new ArrayList<>();
        filteredCustomerList = new ArrayList<>();
        customerAdapter = new CustomersAdapter(CustomersActivity.this, filteredCustomerList, this);
        rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        rvCustomers.setAdapter(customerAdapter);
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners: Setting up click listeners");

        ivBack.setOnClickListener(v -> {
            Log.d(TAG, "setupListeners: Back button clicked");
            finish();
        });

        ivAddCustomer.setOnClickListener(v -> {
            Log.d(TAG, "setupListeners: Add customer button clicked");
            startActivity(new Intent(this, AddCustomerActivity.class));
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "setupListeners: Search query changed: " + s.toString());
                currentSearchQuery = s.toString();
                applyFiltersAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentStatusFilter = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "setupListeners: Status filter changed: " + currentStatusFilter);
                applyFiltersAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortOption = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "setupListeners: Sort option changed: " + currentSortOption);
                applyFiltersAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Commenting out the getBatches call as it's not used and has incorrect parameters
        /*
        Map<String, String> params = new HashMap<>();
        params.put("store_id", storeId);
        Call<List<Batch>> call = apiService.getBatches(params);
        call.enqueue(new Callback<List<Batch>>() {
            @Override
            public void onResponse(Call<List<Batch>> call, Response<List<Batch>> response) {
                if (response.isSuccessful()) {
                    // Handle successful response
                } else {
                    // Handle error
                }
            }

            @Override
            public void onFailure(Call<List<Batch>> call, Throwable t) {
                // Handle failure
            }
        });
        */
    }

    private void applyFiltersAndSort() {
        Log.d(TAG, "applyFiltersAndSort: Applying filters");

        filteredCustomerList.clear();

        for (Customer customer : customerList) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                              customer.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                              customer.getEmail().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                              customer.getPhone().contains(currentSearchQuery);

            boolean matchesStatus = currentStatusFilter.equals("All") ||
                               customer.getStatus().equalsIgnoreCase(currentStatusFilter);

            if (matchesSearch && matchesStatus) {
                filteredCustomerList.add(customer);
            }
        }

        // Apply sorting
        switch (currentSortOption) {
            case "Name (A-Z)":
                filteredCustomerList.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
                break;
            case "Name (Z-A)":
                filteredCustomerList.sort((c1, c2) -> c2.getName().compareToIgnoreCase(c1.getName()));
                break;
            case "Recent":
                filteredCustomerList.sort((c1, c2) -> {
                    if (c2.getRegistrationDate() == null) return -1;
                    if (c1.getRegistrationDate() == null) return 1;
                    return c2.getRegistrationDate().compareTo(c1.getRegistrationDate());
                });
                break;
        }

        customerAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredCustomerList.isEmpty()) {
            rvCustomers.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvCustomers.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void loadCustomers() {
        showLoading(true);
        ApiUrls.getApiService().getCustomers(String.valueOf(userSession.getStoreId()), currentSearchQuery, currentStatusFilter.equals("All") ? null : currentStatusFilter)
                .enqueue(new retrofit2.Callback<ApiResponse<java.util.List<java.util.Map<String, Object>>>>() {
                    @Override
                    public void onResponse(retrofit2.Call<ApiResponse<java.util.List<java.util.Map<String, Object>>>> call, retrofit2.Response<ApiResponse<java.util.List<java.util.Map<String, Object>>>> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            java.util.List<java.util.Map<String, Object>> raw = response.body().getData();
                            java.util.List<Customer> customers = new java.util.ArrayList<>();
                            if (raw != null) {
                                for (java.util.Map<String, Object> m : raw) {
                                    int id = parseInt(m.get("customer_id"));
                                    String name = asString(m.get("customer_name"));
                                    String email = asString(m.get("email"));
                                    String phone = asString(m.get("phone"));
                                    String address = asString(m.get("address"));
                                    String dob = asString(m.get("date_of_birth"));
                                    String status = asString(m.get("status"));
                                    int points = parseInt(m.get("loyalty_points"));
                                    Customer c = new Customer(id, name, email, phone, status, points, null, null, null, address, dob, name != null && !name.isEmpty() ? name.substring(0,1) : "" );
                                    customers.add(c);
                                }
                            }
                            updateCustomerList(customers);
                        } else {
                            Toast.makeText(CustomersActivity.this, Constants.ERROR_SERVER, Toast.LENGTH_SHORT).show();
                            updateEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ApiResponse<java.util.List<java.util.Map<String, Object>>>> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(CustomersActivity.this, Constants.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                    }
                });
    }

    private List<Customer> parseCustomersFromMap(List<Map<String, Object>> customersArray) {
        Log.d(TAG, "parseCustomersFromMap: Parsing " + customersArray.size() + " customers");

        List<Customer> customers = new ArrayList<>();
        int index = 0;
        for (Map<String, Object> customerMap : customersArray) {
            try {
                Customer customer = new Customer(
                        ((Number) customerMap.get("customer_id")).intValue(),
                        (String) customerMap.get("name"),
                        (String) customerMap.get("email"),
                        (String) customerMap.get("phone"),
                        (String) customerMap.get("status"),
                        ((Number) customerMap.get("loyalty_points")).intValue(),
                        (String) customerMap.get("last_purchase"),
                        (String) customerMap.get("last_purchase_display"),
                        (String) customerMap.get("registration_date"),
                        customerMap.get("address") != null ? (String) customerMap.get("address") : "",
                        (String) customerMap.get("date_of_birth"),
                        (String) customerMap.get("avatar_letter")
                );
                customers.add(customer);
                Log.v(TAG, "parseCustomersFromMap: Parsed customer " + (++index) + ": " + customer.getName());
            } catch (Exception e) {
                Log.e(TAG, "parseCustomersFromMap: Error parsing customer at index " + index, e);
            }
        }

        Log.d(TAG, "parseCustomersFromMap: Successfully parsed " + customers.size() + " customers");
        return customers;
    }
    
    private List<Customer> parseCustomers(JSONArray customersArray) throws JSONException {
        Log.d(TAG, "parseCustomers: Parsing " + customersArray.length() + " customers");

        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < customersArray.length(); i++) {
            try {
                JSONObject customerObj = customersArray.getJSONObject(i);
                Customer customer = new Customer(
                        customerObj.getInt("customer_id"),
                        customerObj.getString("name"),
                        customerObj.getString("email"),
                        customerObj.getString("phone"),
                        customerObj.getString("status"),
                        customerObj.getInt("loyalty_points"),
                        customerObj.optString("last_purchase", null),
                        customerObj.getString("last_purchase_display"),
                        customerObj.getString("registration_date"),
                        customerObj.optString("address", ""),
                        customerObj.optString("date_of_birth", null),
                        customerObj.getString("avatar_letter")
                );
                customers.add(customer);
                Log.v(TAG, "parseCustomers: Parsed customer " + (i + 1) + ": " + customer.getName());
            } catch (JSONException e) {
                Log.e(TAG, "parseCustomers: Error parsing customer at index " + i, e);
            }
        }

        Log.d(TAG, "parseCustomers: Successfully parsed " + customers.size() + " customers");
        return customers;
    }

    private void updateCustomerList(List<Customer> customers) {
        Log.d(TAG, "updateCustomerList: Updating UI with " + customers.size() + " customers");

        customerList.clear();
        customerList.addAll(customers);
        filteredCustomerList.clear();
        filteredCustomerList.addAll(customers);
        customerAdapter.notifyDataSetChanged();

        if (customers.isEmpty()) {
            Log.d(TAG, "updateCustomerList: No customers found - showing empty state");
            rvCustomers.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "updateCustomerList: Showing customer list");
            rvCustomers.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
        
        // Apply filters and sorting after updating the list
        applyFiltersAndSort();
    }

    private void showLoading(boolean show) {
        Log.d(TAG, "showLoading: " + (show ? "Showing" : "Hiding") + " loading indicator");
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEditCustomer(Customer customer) {
        Log.d(TAG, "onEditCustomer: Edit requested for customer: " + customer.getName() + " (ID: " + customer.getCustomerId() + ")");

        Intent intent = new Intent(this, EditCustomerActivity.class);
        intent.putExtra("customer_id", customer.getCustomerId());
        startActivity(intent);
    }

    @Override
    public void onDeleteCustomer(Customer customer) {
        Log.d(TAG, "onDeleteCustomer: Delete requested for customer: " + customer.getName() + " (ID: " + customer.getCustomerId() + ")");

        new AlertDialog.Builder(this)
                .setTitle("Delete Customer")
                .setMessage("Are you sure you want to delete " + customer.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Log.d(TAG, "onDeleteCustomer: User confirmed deletion");
                    deleteCustomer(customer);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d(TAG, "onDeleteCustomer: User cancelled deletion");
                })
                .show();
    }

    @Override
    public void onToggleCustomerStatus(Customer customer) {
        Log.d(TAG, "onToggleCustomerStatus: Status toggle requested for customer: " + customer.getName() + " (ID: " + customer.getCustomerId() + ")");
        toggleCustomerStatus(customer);
    }

    private void toggleCustomerStatus(Customer customer) {
        Log.d(TAG, "toggleCustomerStatus: Showing confirmation dialog for customer: " + customer.getName());

        String currentStatus = customer.getStatus();
        String newAction = "Active".equals(currentStatus) ? "deactivate" : "activate";

        new AlertDialog.Builder(this)
                .setTitle("Toggle Customer Status")
                .setMessage("Are you sure you want to " + newAction + " " + customer.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Log.d(TAG, "toggleCustomerStatus: User confirmed status toggle");
                    updateCustomerStatus(customer);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Log.d(TAG, "toggleCustomerStatus: User cancelled status toggle");
                })
                .show();
    }

    private void updateCustomerStatus(Customer customer) {
        Log.d(TAG, "updateCustomerStatus: Starting status update for customer ID: " + customer.getCustomerId());
        showLoading(true);

        JSONObject payload = new JSONObject();
        try {
            payload.put("customer_id", customer.getCustomerId());
            payload.put("store_id", getStoreId());
            payload.put("user_id", getUserId());
            payload.put("action", "toggle_status");

            Log.d(TAG, "updateCustomerStatus: Request payload = " + payload.toString());
        } catch (JSONException e) {
            Log.e(TAG, "updateCustomerStatus: Error creating JSON payload", e);
            showLoading(false);
            Toast.makeText(this, "Error preparing status update request", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("store_id", String.valueOf(getStoreId()));
        ApiUrls.getApiService().toggleCustomerStatus(String.valueOf(customer.getCustomerId()), body)
                .enqueue(new retrofit2.Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(retrofit2.Call<ApiResponse<Map<String, Object>>> call, retrofit2.Response<ApiResponse<Map<String, Object>>> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            customer.setStatus("Active".equals(customer.getStatus()) ? "Inactive" : "Active");
                            customerAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(CustomersActivity.this, Constants.ERROR_SERVER, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(CustomersActivity.this, Constants.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteCustomer(Customer customer) {
        Log.d(TAG, "deleteCustomer: Starting deletion process for customer ID: " + customer.getCustomerId());
        showLoading(true);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("customer_id", customer.getCustomerId());
        requestMap.put("store_id", Integer.parseInt(storeId));
        requestMap.put("user_id", getUserId());
        requestMap.put("action", "delete_customer");

        Log.d(TAG, "deleteCustomer: Request body = " + requestMap.toString());

        ApiUrls.getApiService().deleteCustomer(String.valueOf(customer.getCustomerId())).enqueue(new retrofit2.Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Map<String, Object>>> call, retrofit2.Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    for (int i = 0; i < customerList.size(); i++) {
                        if (customerList.get(i).getCustomerId() == customer.getCustomerId()) {
                            customerList.remove(i);
                            break;
                        }
                    }
                    filteredCustomerList.clear();
                    filteredCustomerList.addAll(customerList);
                    customerAdapter.notifyDataSetChanged();
                    updateEmptyState();
                } else {
                    Toast.makeText(CustomersActivity.this, Constants.ERROR_SERVER, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CustomersActivity.this, Constants.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper methods to get store ID and user ID
    private int getStoreId() {
        try {
            return Integer.parseInt(storeId);
        } catch (NumberFormatException e) {
            Log.e(TAG, "getStoreId: Error parsing store ID", e);
            return 0;
        }
    }

    private int getUserId() {
        // Get user ID from session or return a default value
        // You might want to add this to your UserSession class
        String userId = userSession.getUserId(); // Assuming this method exists
        try {
            return Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            Log.e(TAG, "getUserId: Error parsing user ID, using default", e);
            return 1; // Default user ID
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity resumed, refreshing customer data");
        loadCustomers(); // Refresh data when returning to activity
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Activity paused");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity destroyed");
    }

    private void searchCustomers(String query) {
        showLoading(true);

        showLoading(false);
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }
    private int parseInt(Object v) { if (v instanceof Number) return ((Number)v).intValue(); try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return 0; } }
    private String asString(Object v) { return v == null ? "" : String.valueOf(v); }
}
