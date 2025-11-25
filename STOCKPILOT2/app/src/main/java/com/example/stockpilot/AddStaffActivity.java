package com.example.stockpilot;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stockpilot.R;
import com.example.stockpilot.ApiResponse;
import com.example.stockpilot.UserSession;
 
 
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
 

public class AddStaffActivity extends AppCompatActivity {

    private static final String TAG = "AddStaffActivity";

    private TextView tvStoreName;
    private TextInputLayout tilUsername, tilPassword, tilConfirmPassword, tilFullName, tilEmail, tilPhone, tilAddress;
    private TextInputEditText etUsername, etPassword, etConfirmPassword, etFullName, etEmail, etPhone, etAddress;
    private RadioGroup radioGroupRole;
    private RadioButton radioAdmin, radioManager, radioStaff;
    private Button btnAddStaff;
    private ProgressBar progressBar;
    private ImageView ivBack;
    private UserSession userSession;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_staff);

        // Initialize views
        initViews();

        

        // Initialize UserSession
        userSession = new UserSession(this);

        // Set store name from UserSession
        tvStoreName.setText(userSession.getStoreName());

        // Set up click listeners
        setupClickListeners();
    }

    private void initViews() {
        tvStoreName = findViewById(R.id.tv_store_name);
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        tilFullName = findViewById(R.id.til_full_name);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);
        tilAddress = findViewById(R.id.til_address);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        radioGroupRole = findViewById(R.id.radio_group_role);
        radioAdmin = findViewById(R.id.radio_admin);
        radioManager = findViewById(R.id.radio_manager);
        radioStaff = findViewById(R.id.radio_staff);
        btnAddStaff = findViewById(R.id.btn_add_staff);
        progressBar = findViewById(R.id.progress_bar);
        ivBack = findViewById(R.id.iv_back);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());

        btnAddStaff.setOnClickListener(v -> {
            if (validateInputs()) {
                addStaff();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String username = etUsername.getText() != null ? etUsername.getText().toString() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";
        String fullName = etFullName.getText() != null ? etFullName.getText().toString() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString() : "";
        String address = etAddress.getText() != null ? etAddress.getText().toString() : "";

        // Validate username
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Please enter a username");
            isValid = false;
        } else {
            tilUsername.setError(null);
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Please enter a password");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm the password");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Please enter full name");
            isValid = false;
        } else {
            tilFullName.setError(null);
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Please enter an email");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // Validate phone
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Please enter a phone number");
            isValid = false;
        } else {
            tilPhone.setError(null);
        }

        // Validate address
        if (TextUtils.isEmpty(address)) {
            tilAddress.setError("Please enter an address");
            isValid = false;
        } else {
            tilAddress.setError(null);
        }

        return isValid;
    }

    private void addStaff() {
        showLoading(true);

        String username = etUsername.getText() != null ? etUsername.getText().toString() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String fullName = etFullName.getText() != null ? etFullName.getText().toString() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString() : "";
        String address = etAddress.getText() != null ? etAddress.getText().toString() : "";

        // Get selected role
        String role = "staff";
        int selectedId = radioGroupRole.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_admin) {
            role = "admin";
        } else if (selectedId == R.id.radio_manager) {
            role = "manager";
        }

        // Create JSON request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("password", password);
            jsonBody.put("full_name", fullName);
            jsonBody.put("email", email);
            jsonBody.put("phone", phone);
            jsonBody.put("address", address);
            jsonBody.put("role", role);
            jsonBody.put("store_id", userSession.getStoreId());
            jsonBody.put("created_by", userSession.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
            showLoading(false);
            Toast.makeText(this, "Error preparing request", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonBody.toString());

        showLoading(false);
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private void clearInputFields() {
        etUsername.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
        etFullName.setText("");
        etEmail.setText("");
        etPhone.setText("");
        etAddress.setText("");
        radioStaff.setChecked(true);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnAddStaff.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnAddStaff.setEnabled(true);
        }
    }
}
