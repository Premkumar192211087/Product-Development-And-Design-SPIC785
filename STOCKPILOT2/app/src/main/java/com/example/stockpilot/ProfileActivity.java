package com.example.stockpilot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stockpilot.R;
 
import com.example.stockpilot.UserSession;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private TextView tvStoreName, tvUsername, tvFullName, tvEmail, tvPhone, tvRole, tvAddress;
    private Button btnEditProfile, btnChangePassword, btnAddStaff, btnDeleteStaff, btnLogout;
    private ImageView ivBack;
    private UserSession userSession;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        initViews();

        // Set up click listeners
        setupClickListeners();

        // Initialize UserSession
        userSession = new UserSession(this);

        

        // Set up click listeners
        setupClickListeners();

        // Check user role and set visibility of admin/manager buttons
        checkUserRole();

        // Load user profile data
        loadProfileData();
    }

    private void initViews() {
        tvStoreName = findViewById(R.id.tv_store_name);
        tvUsername = findViewById(R.id.tv_username);
        tvFullName = findViewById(R.id.tv_full_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvRole = findViewById(R.id.tv_role);
        tvAddress = findViewById(R.id.tv_address);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnAddStaff = findViewById(R.id.btn_add_staff);
        btnDeleteStaff = findViewById(R.id.btn_delete_staff);
        btnLogout = findViewById(R.id.btn_logout);
        ivBack = findViewById(R.id.iv_back);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnAddStaff.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddStaffActivity.class);
            startActivity(intent);
        });

        btnDeleteStaff.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, DeleteStaffActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            userSession.clearSession();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkUserRole() {
        String userRole = userSession.getRole();
        if (userRole != null && (userRole.equalsIgnoreCase("admin") || userRole.equalsIgnoreCase("manager"))) {
            btnAddStaff.setVisibility(View.VISIBLE);
            btnDeleteStaff.setVisibility(View.VISIBLE);
        } else {
            btnAddStaff.setVisibility(View.GONE);
            btnDeleteStaff.setVisibility(View.GONE);
        }
    }

    private void loadProfileData() {
        tvStoreName.setText(userSession.getStoreName());
        tvUsername.setText(userSession.getUsername());
        tvRole.setText(userSession.getRole());
        tvFullName.setText("");
        tvEmail.setText("");
        tvPhone.setText("");
        tvAddress.setText("");
    }

    private void handleProfileResponse(String responseData) {
        try {
            Gson gson = new Gson();
            Map<String, Object> jsonResponse = gson.fromJson(responseData, Map.class);

            // Check if response has status field
            if (!jsonResponse.containsKey("status")) {
                Log.e(TAG, "Response missing status field");
                Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get status as string
            String status = jsonResponse.get("status").toString();

            if ("success".equals(status)) {
                // Check if data field exists
                if (!jsonResponse.containsKey("data")) {
                    Log.e(TAG, "Success response missing data field");
                    Toast.makeText(this, "Invalid response data", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> userData = (Map<String, Object>) jsonResponse.get("data");

                // Safely extract and set user data to views with null checks
                if (userData.containsKey("username") && userData.get("username") != null) {
                    tvUsername.setText(userData.get("username").toString());
                }

                if (userData.containsKey("full_name") && userData.get("full_name") != null) {
                    tvFullName.setText(userData.get("full_name").toString());
                }

                if (userData.containsKey("email") && userData.get("email") != null) {
                    tvEmail.setText(userData.get("email").toString());
                }

                if (userData.containsKey("phone") && userData.get("phone") != null) {
                    tvPhone.setText(userData.get("phone").toString());
                }

                if (userData.containsKey("role") && userData.get("role") != null) {
                    tvRole.setText(userData.get("role").toString());
                }
                
                // Update session data if needed
                userSession.setUserData(
                    userData.containsKey("user_id") ? userData.get("user_id").toString() : userSession.getUserId(),
                    userData.containsKey("username") ? userData.get("username").toString() : userSession.getUsername(),
                    userData.containsKey("role") ? userData.get("role").toString() : userSession.getRole(),
                    userData.containsKey("store_id") ? userData.get("store_id").toString() : userSession.getStoreId(),
                    userData.containsKey("store_name") ? userData.get("store_name").toString() : userSession.getStoreName()
                );

                if (userData.containsKey("address") && userData.get("address") != null) {
                    tvAddress.setText(userData.get("address").toString());
                }

            } else {
                // Handle error status
                String message = "Unknown error";
                if (jsonResponse.containsKey("message") && jsonResponse.get("message") != null) {
                    message = jsonResponse.get("message").toString();
                }
                Log.e(TAG, "API Error: " + message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing profile data: " + e.getMessage(), e);
            Toast.makeText(this, "Error parsing profile data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile data when returning to this activity
        loadProfileData();
    }
}
