package com.example.stockpilot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stockpilot.R;
import com.example.stockpilot.LoginActivity;
 

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class signup extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private EditText etStoreName, etStoreLocation, etUsername, etPassword, etConfirmPassword,
            etRole, etFullName, etEmail, etPhone, etAddress;
    private CheckBox cbTerms;
    private Button btnCreateAccount;
    private TextView tvLogin;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Initializing signup activity");

        try {
            setContentView(R.layout.activity_signup1);
            
            

            etStoreName = findViewById(R.id.etStoreName);
            etStoreLocation = findViewById(R.id.etStoreLocation);
            etUsername = findViewById(R.id.etUsername);
            etPassword = findViewById(R.id.etPassword);
            etConfirmPassword = findViewById(R.id.etConfirmPassword);
            etFullName = findViewById(R.id.etFullName);
            etEmail = findViewById(R.id.etEmail);
            etPhone = findViewById(R.id.etPhone);
            etAddress = findViewById(R.id.etAddress);
            cbTerms = findViewById(R.id.cbTerms);
            btnCreateAccount = findViewById(R.id.btnCreateAccount);
            tvLogin = findViewById(R.id.tvLogin);

            btnCreateAccount.setOnClickListener(v -> attemptSignup());
            tvLogin.setOnClickListener(v -> {
                Log.d(TAG, "Navigating to login activity");
                startActivity(new Intent(signup.this, LoginActivity.class));
            });

            Log.d(TAG, "onCreate: Activity initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Error initializing activity", e);
        }
    }

    private void attemptSignup() {
        Log.d(TAG, "attemptSignup: Starting signup process");

        try {
            String storeName = etStoreName.getText().toString().trim();
            String storeLocation = etStoreLocation.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String role = etRole.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            // Log field validation (without sensitive data)
            Log.d(TAG, "attemptSignup: Validating fields - Username: " + username +
                    ", Email: " + email + ", Store: " + storeName);

            if (!cbTerms.isChecked()) {
                Log.w(TAG, "attemptSignup: Terms and conditions not accepted");
                return;
            }

            if (storeName.isEmpty() || storeLocation.isEmpty() || username.isEmpty() ||
                    password.isEmpty() || confirmPassword.isEmpty() || role.isEmpty() ||
                    fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Log.w(TAG, "attemptSignup: Validation failed - Empty required fields detected");
                return;
            }

            if (!password.equals(confirmPassword)) {
                Log.w(TAG, "attemptSignup: Password confirmation failed");
                return;
            }

            Log.d(TAG, "attemptSignup: Data layer removed");
            Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "attemptSignup: Unexpected error during signup attempt", e);
        }
    }

    private void performSignupRequest(String storeName, String storeLocation, String username,
                                      String password, String role, String fullName, String email,
                                      String phone, String address) {

        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Signup activity being destroyed");
        super.onDestroy();
    }
}
