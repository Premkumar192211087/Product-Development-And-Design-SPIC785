package com.example.stockpilot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.example.stockpilot.services.NetworkDiscoveryService;
import com.google.android.material.textfield.TextInputEditText;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvSignup;
    private TextView textViewStatus;

    
    private UserSession userSession;
    private BroadcastReceiver discoveryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);

        initViews();
        initUserSession();
        checkIfAlreadyLoggedIn();
        setupClickListeners();

        discoveryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra(NetworkDiscoveryService.EXTRA_ERROR_MESSAGE)) {
                    String errorMessage = intent.getStringExtra(NetworkDiscoveryService.EXTRA_ERROR_MESSAGE);
                    Log.e("LoginActivity", "Discovery failed: " + errorMessage);
                    Toast.makeText(context, "Discovery failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(NetworkDiscoveryService.ACTION_DISCOVERY_RESULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(discoveryReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(discoveryReceiver);
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        tvSignup = findViewById(R.id.tv_signup);
        // Assuming you have a textView with this id in your layout. If not, this will be null.
        textViewStatus = findViewById(R.id.textViewStatus);
    }

    private void initOkHttp() {}

    private void initUserSession() {
        userSession = UserSession.getInstance(this);
    }

    private void checkIfAlreadyLoggedIn() {
        if (userSession.isLoggedIn()) {
            redirectToAdminHome();
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, signup.class);
            startActivity(intent);
        });
    }

    private void updateUiForDiscoveryState(Object state) {}

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        showLoading(true);

        showLoading(false);
        showError("Data layer removed");
    }

    private void handleLoginResponse(String responseBody, int responseCode, String username) {
        try {
            Gson gson = new Gson();
            LoginResponse loginResponse = gson.fromJson(responseBody, LoginResponse.class);

            if (responseCode == 200 && "success".equals(loginResponse.getStatus())) {
                userSession.createUserSession(
                        loginResponse.getStoreId(),
                        loginResponse.getStoreName(),
                        username,
                        loginResponse.getRole()
                );

                showSuccess("Login successful! Welcome " + loginResponse.getStoreName());

                new android.os.Handler().postDelayed(this::redirectToAdminHome, 1500);

            } else {
                String errorMessage = loginResponse.getMessage() != null ?
                        loginResponse.getMessage() : "Login failed";
                ErrorLogger.logError(TAG, "Login failed: " + errorMessage);
                showError(errorMessage);
            }

        } catch (Exception e) {
            ErrorLogger.logError(TAG, "Error parsing login response", e);
            showError("Error parsing response: " + e.getMessage());
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnLogin.setText("Signing In...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnLogin.setText("Sign In");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void redirectToAdminHome() {
        Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public static void logoutUser(Context context) {
        UserSession userSession = UserSession.getInstance(context);
        userSession.clearSession();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        if (context instanceof Activity) {
            ((Activity) context).finish();
        }

        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}

