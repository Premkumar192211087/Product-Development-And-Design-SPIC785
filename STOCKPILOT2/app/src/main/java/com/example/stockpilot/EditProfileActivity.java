
package com.example.stockpilot;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stockpilot.R;
 
import com.example.stockpilot.UserSession;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.RequestBody;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
     

    private TextView tvStoreName;
    private TextInputLayout tilFullName, tilEmail, tilPhone, tilAddress;
    private TextInputEditText etFullName, etEmail, etPhone, etAddress;
    private Button btnSaveProfile;
    private ProgressBar progressBar;
    private ImageView ivBack;
    private UserSession userSession;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views
        initViews();

        

        // Initialize UserSession
        userSession = new UserSession(this);

        // Set store name from UserSession
        tvStoreName.setText(userSession.getStoreName());

        // Set up click listeners
        setupClickListeners();

        // Load user profile data
        loadProfileData();
    }

    private void initViews() {
        tvStoreName = findViewById(R.id.tv_store_name);
        tilFullName = findViewById(R.id.til_full_name);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);
        tilAddress = findViewById(R.id.til_address);
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        progressBar = findViewById(R.id.progress_bar);
        ivBack = findViewById(R.id.iv_back);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());

        btnSaveProfile.setOnClickListener(v -> {
            if (validateInputs()) {
                updateProfile();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Please enter your full name");
            isValid = false;
        } else {
            tilFullName.setError(null);
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Please enter your email");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // Validate phone
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Please enter your phone number");
            isValid = false;
        } else {
            tilPhone.setError(null);
        }

        // Validate address
        if (TextUtils.isEmpty(address)) {
            tilAddress.setError("Please enter your address");
            isValid = false;
        } else {
            tilAddress.setError(null);
        }

        return isValid;
    }

    private void loadProfileData() {
        showLoading(true);

        // Create request parameters as Map<String, String>
        Map<String, String> params = new HashMap<>();
        params.put("user_id", userSession.getUserId());
        params.put("store_id", userSession.getStoreId());

        showLoading(false);
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private void handleProfileResponse(String responseData) {
        try {
            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(responseData, JsonObject.class);

            // Check if response has status field
            if (!jsonResponse.has("status")) {
                Log.e(TAG, "Response missing status field");
                Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get status as string (not boolean)
            String status = jsonResponse.get("status").getAsString();

            if ("success".equals(status)) {
                // Check if data field exists
                if (!jsonResponse.has("data")) {
                    Log.e(TAG, "Success response missing data field");
                    Toast.makeText(this, "Invalid response data", Toast.LENGTH_SHORT).show();
                    return;
                }

                JsonObject userData = jsonResponse.getAsJsonObject("data");

                // Safely set user data to input fields with null checks
                if (userData.has("full_name") && userData.get("full_name") != null) {
                    etFullName.setText(userData.get("full_name").getAsString());
                }

                if (userData.has("email") && userData.get("email") != null) {
                    etEmail.setText(userData.get("email").getAsString());
                }

                if (userData.has("phone") && userData.get("phone") != null) {
                    etPhone.setText(userData.get("phone").getAsString());
                }

                if (userData.has("address") && userData.get("address") != null) {
                    etAddress.setText(userData.get("address").getAsString());
                }

            } else {
                // Handle error status
                String message = "Unknown error";
                if (jsonResponse.has("message") && jsonResponse.get("message") != null) {
                    message = jsonResponse.get("message").getAsString();
                }
                Log.e(TAG, "API Error: " + message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing profile data: " + e.getMessage(), e);
            Toast.makeText(this, "Error parsing profile data", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfile() {
        showLoading(true);

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Create JSON request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userSession.getUserId());
            jsonBody.put("full_name", fullName);
            jsonBody.put("email", email);
            jsonBody.put("phone", phone);
            jsonBody.put("address", address);
        } catch (JSONException e) {
            e.printStackTrace();
            showLoading(false);
            Toast.makeText(this, "Error preparing request", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestBody = RequestBody.create(
                okhttp3.MediaType.parse("application/json; charset=utf-8"),
                jsonBody.toString());

        showLoading(false);
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private void handleUpdateResponse(String responseData) {
        try {
            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(responseData, JsonObject.class);

            // Check if response has status field
            if (!jsonResponse.has("status")) {
                Log.e(TAG, "Response missing status field");
                Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get status as string (not boolean)
            String status = jsonResponse.get("status").getAsString();

            // Get message safely
            String message = "Unknown response";
            if (jsonResponse.has("message") && jsonResponse.get("message") != null) {
                message = jsonResponse.get("message").getAsString();
            }

            if ("success".equals(status)) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                finish(); // Close activity on successful update
            } else {
                // Handle error status
                Log.e(TAG, "API Error: " + message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
            Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSaveProfile.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSaveProfile.setEnabled(true);
        }
    }
}
