
package com.example.stockpilot;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.staffadapter;
 
import com.example.stockpilot.UserSession;
import com.example.stockpilot.staffmodel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.MediaType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

 

public class DeleteStaffActivity extends AppCompatActivity implements staffadapter.OnDeleteClickListener {

    private static final String TAG = "DeleteStaffActivity";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private ImageView ivBack;
    private TextView tvStoreName;
    private EditText etSearch;
    private ImageView ivSearch;
    private RecyclerView rvStaff;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    
    private UserSession userSession;
    private ArrayList<staffmodel> staffList;
    private ArrayList<staffmodel> filteredStaffList;
    private staffadapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_staff);

        initViews();
        initData();
        setupListeners();
        loadStaffData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvStoreName = findViewById(R.id.tv_store_name);
        etSearch = findViewById(R.id.et_search);
        ivSearch = findViewById(R.id.iv_search);
        rvStaff = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_no_staff);
        progressBar = findViewById(R.id.progress_bar);

        rvStaff.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initData() {
        
        userSession = new UserSession(this);
        staffList = new ArrayList<>();
        filteredStaffList = new ArrayList<>();

        tvStoreName.setText(userSession.getStoreName());

        adapter = new staffadapter(filteredStaffList, this);
        rvStaff.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivSearch.setOnClickListener(v -> filterStaff(etSearch.getText().toString()));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterStaff(s.toString());
            }
        });
    }

    private void filterStaff(String query) {
        filteredStaffList.clear();

        if (query.isEmpty()) {
            filteredStaffList.addAll(staffList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (staffmodel staff : staffList) {
                if (staff.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                        staff.getRole().toLowerCase().contains(lowerCaseQuery) ||
                        staff.getEmail().toLowerCase().contains(lowerCaseQuery)) {
                    filteredStaffList.add(staff);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (filteredStaffList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvStaff.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvStaff.setVisibility(View.VISIBLE);
        }
    }

    private void loadStaffData() {
        showLoading(true);

        Map<String, String> params = new HashMap<>();
        params.put("store_id", userSession.getStoreId());

        showLoading(false);
        filteredStaffList.clear();
        staffList.clear();
        adapter.notifyDataSetChanged();
        updateEmptyView();
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private void handleStaffResponse(Map<String, Object> responseMap) {
        try {
            String status = (String) responseMap.get("status");
            
            if ("success".equals(status)) {
                List<Map<String, Object>> staffData = (List<Map<String, Object>>) responseMap.get("data");
                
                staffList.clear();
                String currentUserId = userSession.getUserId();
                
                if (staffData != null) {
                    for (Map<String, Object> staffMap : staffData) {
                        // Parse staff data
                        Object staffIdObj = staffMap.get("staff_id");
                        int staffId = staffIdObj instanceof Double ? 
                                     ((Double) staffIdObj).intValue() : 
                                     (Integer) staffIdObj;
                        
                        String fullName = (String) staffMap.get("full_name");
                        String email = (String) staffMap.get("email");
                        String phone = (String) staffMap.get("phone");
                        String role = (String) staffMap.get("role");
                        String address = (String) staffMap.get("address");
                        
                        // Create staff object using constructor
                        staffmodel staff = new staffmodel(staffId, fullName, email, phone, role, address);
                        
                        // Filter out current user
                        if (!String.valueOf(staffId).equals(currentUserId)) {
                            staffList.add(staff);
                        }
                    }
                }
                
                filteredStaffList.clear();
                filteredStaffList.addAll(staffList);
                adapter.notifyDataSetChanged();
                updateEmptyView();
            } else {
                String message = (String) responseMap.get("message");
                Toast.makeText(this, message != null ? message : "Failed to load staff data", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API error: " + message);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error parsing response: " + e.getMessage());
        }
    }

    @Override
    public void onDeleteClick(int staffId) {
        // Find the staff member by ID
        staffmodel staffToDelete = null;
        for (staffmodel staff : staffList) {
            if (staff.getStaffId() == staffId) {
                staffToDelete = staff;
                break;
            }
        }

        if (staffToDelete != null) {
            showDeleteConfirmation(staffToDelete);
        }
    }

    private void showDeleteConfirmation(staffmodel staff) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete " + staff.getFullName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteStaff(staff.getStaffId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteStaff(int staffId) {
        showLoading(true);

        Map<String, String> params = new HashMap<>();
        params.put("staff_id", String.valueOf(staffId));
        params.put("deleted_by", userSession.getUserId());

        showLoading(false);
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private void handleDeleteResponse(Map<String, Object> responseMap, int staffId) {
        try {
            String status = (String) responseMap.get("status");
            
            if ("success".equals(status)) {
                Toast.makeText(this, "Staff deleted successfully", Toast.LENGTH_SHORT).show();
                
                // Remove the deleted staff from the lists
                for (int i = 0; i < staffList.size(); i++) {
                    if (staffList.get(i).getStaffId() == staffId) {
                        staffList.remove(i);
                        break;
                    }
                }
                
                for (int i = 0; i < filteredStaffList.size(); i++) {
                    if (filteredStaffList.get(i).getStaffId() == staffId) {
                        filteredStaffList.remove(i);
                        break;
                    }
                }
                
                adapter.notifyDataSetChanged();
                updateEmptyView();
            } else {
                String message = (String) responseMap.get("message");
                Toast.makeText(this, message != null ? message : "Failed to delete staff", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API error: " + message);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error parsing response: " + e.getMessage());
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
