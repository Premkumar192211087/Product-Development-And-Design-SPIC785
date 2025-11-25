package com.example.stockpilot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stockpilot.R;
import com.example.stockpilot.ProfileActivity;

public class Settings extends AppCompatActivity {

    private ImageView backButton;
    private TextView tvStoreName;
    private Switch switchNotifications;
    private Switch switchDarkMode;
    private Switch switchLowStockNotifications;
    private Switch switchExpiryNotifications;
    private Switch switchDamagedItemsNotifications;

    // New UI elements for configurable settings
    private TextView tvLowStockThreshold;
    private TextView tvExpiryDays;

    // Profile section
    private LinearLayout profileContainer;

    private SharedPreferences sharedPreferences;
    private String storeId;
    private String storeName;

    // Default values
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;
    private static final int DEFAULT_EXPIRY_DAYS = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        initializeViews();

        // Get store information from SharedPreferences
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        storeId = sharedPreferences.getString("store_id", "");
        storeName = sharedPreferences.getString("store_name", "Store");

        // Set store name in the UI
        if (tvStoreName != null) {
            tvStoreName.setText(storeName);
        }

        // Load saved preferences
        loadSavedPreferences();

        // Set up listeners
        setupListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        tvStoreName = findViewById(R.id.tvStoreName);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchLowStockNotifications = findViewById(R.id.switchLowStockNotifications);
        switchExpiryNotifications = findViewById(R.id.switchExpiryNotifications);
        switchDamagedItemsNotifications = findViewById(R.id.switchDamagedItemsNotifications);

        // Initialize new UI elements
        tvLowStockThreshold = findViewById(R.id.tvLowStockThreshold);
        tvExpiryDays = findViewById(R.id.tvExpiryDays);

        // Initialize profile container
        profileContainer = findViewById(R.id.profileContainer);
    }

    private void loadSavedPreferences() {
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode_enabled", false);
        boolean lowStockNotificationsEnabled = sharedPreferences.getBoolean("low_stock_notifications_enabled", true);
        boolean expiryNotificationsEnabled = sharedPreferences.getBoolean("expiry_notifications_enabled", true);
        boolean damagedItemsNotificationsEnabled = sharedPreferences.getBoolean("damaged_items_notifications_enabled", true);

        // Load threshold values
        int lowStockThreshold = sharedPreferences.getInt("low_stock_threshold", DEFAULT_LOW_STOCK_THRESHOLD);
        int expiryDays = sharedPreferences.getInt("expiry_days_threshold", DEFAULT_EXPIRY_DAYS);

        switchNotifications.setChecked(notificationsEnabled);
        switchDarkMode.setChecked(darkModeEnabled);
        switchLowStockNotifications.setChecked(lowStockNotificationsEnabled);
        switchExpiryNotifications.setChecked(expiryNotificationsEnabled);
        switchDamagedItemsNotifications.setChecked(damagedItemsNotificationsEnabled);

        // Set threshold values in UI
        if (tvLowStockThreshold != null) {
            tvLowStockThreshold.setText(String.valueOf(lowStockThreshold));
        }

        if (tvExpiryDays != null) {
            tvExpiryDays.setText(String.valueOf(expiryDays));
        }

        // Update visibility of specific notification settings based on main notification toggle
        updateNotificationSettingsVisibility(notificationsEnabled);
    }

    private void setupListeners() {
        // Back button click listener
        backButton.setOnClickListener(v -> finish());

        // Profile click listener
        if (profileContainer != null) {
            profileContainer.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        // Notifications switch listener
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();

            // Update visibility of specific notification settings
            updateNotificationSettingsVisibility(isChecked);

            Toast.makeText(Settings.this,
                    isChecked ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        // Dark mode switch listener
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode_enabled", isChecked);
            editor.apply();

            Toast.makeText(Settings.this,
                    isChecked ? "Dark mode enabled" : "Dark mode disabled",
                    Toast.LENGTH_SHORT).show();
            // Note: Actual dark mode implementation would require additional code
        });

        // Low stock notifications switch listener
        switchLowStockNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("low_stock_notifications_enabled", isChecked);
            editor.apply();

            Toast.makeText(Settings.this,
                    isChecked ? "Low stock notifications enabled" : "Low stock notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        // Expiry notifications switch listener
        switchExpiryNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("expiry_notifications_enabled", isChecked);
            editor.apply();

            Toast.makeText(Settings.this,
                    isChecked ? "Expiry notifications enabled" : "Expiry notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        // Damaged items notifications switch listener
        switchDamagedItemsNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("damaged_items_notifications_enabled", isChecked);
            editor.apply();

            Toast.makeText(Settings.this,
                    isChecked ? "Damaged items notifications enabled" : "Damaged items notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        // Low stock threshold click listener
        if (tvLowStockThreshold != null) {
            tvLowStockThreshold.setOnClickListener(v -> showNumberInputDialog(
                    "Set Low Stock Threshold",
                    "Enter the quantity below which items will be considered low stock:",
                    tvLowStockThreshold.getText().toString(),
                    "low_stock_threshold"));
        }

        // Expiry days threshold click listener
        if (tvExpiryDays != null) {
            tvExpiryDays.setOnClickListener(v -> showNumberInputDialog(
                    "Set Expiry Notification Days",
                    "Enter how many days before expiry you want to be notified:",
                    tvExpiryDays.getText().toString(),
                    "expiry_days_threshold"));
        }
    }

    private void updateNotificationSettingsVisibility(boolean notificationsEnabled) {
        int visibility = notificationsEnabled ? View.VISIBLE : View.GONE;
        View lowStockContainer = findViewById(R.id.lowStockNotificationContainer);
        View expiryContainer = findViewById(R.id.expiryNotificationContainer);
        View damagedItemsContainer = findViewById(R.id.damagedItemsNotificationContainer);

        if (lowStockContainer != null) {
            lowStockContainer.setVisibility(visibility);
        }

        if (expiryContainer != null) {
            expiryContainer.setVisibility(visibility);
        }

        if (damagedItemsContainer != null) {
            damagedItemsContainer.setVisibility(visibility);
        }
    }

    /**
     * Shows a dialog for inputting numeric values for thresholds
     */
    private void showNumberInputDialog(String title, String message, String currentValue, String preferenceName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(currentValue);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String inputValue = input.getText().toString().trim();
            if (!inputValue.isEmpty()) {
                try {
                    int value = Integer.parseInt(inputValue);
                    if (value <= 0) {
                        Toast.makeText(Settings.this, "Please enter a value greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save the value
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(preferenceName, value);
                    editor.apply();

                    // Update UI
                    if (preferenceName.equals("low_stock_threshold") && tvLowStockThreshold != null) {
                        tvLowStockThreshold.setText(inputValue);
                        Toast.makeText(Settings.this, "Low stock threshold updated", Toast.LENGTH_SHORT).show();
                    } else if (preferenceName.equals("expiry_days_threshold") && tvExpiryDays != null) {
                        tvExpiryDays.setText(inputValue);
                        Toast.makeText(Settings.this, "Expiry notification days updated", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(Settings.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Settings.this, "Please enter a value", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
