package com.example.stockpilot;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import java.util.HashMap;
import java.util.Map;

 

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePasswordActivity";

    private TextView tvStoreName;
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword;
    private ProgressBar progressBar;
    private ImageView ivBack;
    private UserSession userSession;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

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
        tilCurrentPassword = findViewById(R.id.til_current_password);
        tilNewPassword = findViewById(R.id.til_new_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnChangePassword = findViewById(R.id.btn_change_password);
        progressBar = findViewById(R.id.progress_bar);
        ivBack = findViewById(R.id.iv_back);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());

        btnChangePassword.setOnClickListener(v -> {
            if (validateInputs()) {
                changePassword();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate current password
        if (TextUtils.isEmpty(currentPassword)) {
            tilCurrentPassword.setError("Please enter your current password");
            isValid = false;
        } else {
            tilCurrentPassword.setError(null);
        }

        // Validate new password
        if (TextUtils.isEmpty(newPassword)) {
            tilNewPassword.setError("Please enter a new password");
            isValid = false;
        } else if (newPassword.length() < 6) {
            tilNewPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            tilNewPassword.setError(null);
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your new password");
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        return isValid;
    }

    private void changePassword() {
        showLoading(true);

        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        // Create parameters map for Retrofit
        Map<String, String> params = new HashMap<>();
        params.put("user_id", userSession.getUserId());
        params.put("current_password", currentPassword);
        params.put("new_password", newPassword);

        showLoading(false);
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }



    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnChangePassword.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnChangePassword.setEnabled(true);
        }
    }
}
