package com.example.stockpilot;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

 

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Worker class for checking product thresholds and generating notifications
 * This worker checks for low stock and expiring products based on user-defined thresholds
 */
public class ThresholdCheckWorker extends Worker {
    private static final String TAG = "ThresholdCheckWorker";
    
    private final Context context;
    
    
    public ThresholdCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        
    }
    
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting threshold check worker");
        
        // Check if notifications are enabled
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        boolean lowStockEnabled = sharedPreferences.getBoolean("low_stock_notifications_enabled", true);
        boolean expiryEnabled = sharedPreferences.getBoolean("expiry_notifications_enabled", true);
        
        if (!notificationsEnabled || (!lowStockEnabled && !expiryEnabled)) {
            Log.d(TAG, "Relevant notifications are disabled. Skipping threshold check.");
            return Result.success();
        }
        
        // Get store ID from SharedPreferences
        SharedPreferences sessionPrefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String storeId = sessionPrefs.getString("store_id", "");
        
        if (storeId.isEmpty()) {
            Log.e(TAG, "Store ID not found. Cannot check thresholds.");
            return Result.failure();
        }
        
        Log.d(TAG, "Data layer removed");
        return Result.success();
    }
    
    /**
     * Show immediate notifications for critical items
     */
    private void showImmediateNotifications(JSONArray notifications) throws JSONException {
        NotificationService notificationService = NotificationService.getInstance(context);
        
        for (int i = 0; i < notifications.length(); i++) {
            JSONObject notification = notifications.getJSONObject(i);
            String type = notification.getString("type");
            
            if (type.equals("low_stock")) {
                String productName = notification.getString("product_name");
                int quantity = notification.getInt("quantity");
                int threshold = notification.getInt("threshold");
                
                String title = "Low Stock Alert";
                String message = productName + " is running low with only " + quantity + 
                        " units remaining. The minimum threshold is set to " + threshold + " units.";
                
                notificationService.showLowStockNotification(title, message, 10000 + i);
            } else if (type.equals("expired")) {
                String productName = notification.getString("product_name");
                String expiryDate = notification.getString("expiry_date");
                int daysUntilExpiry = notification.getInt("days_until_expiry");
                
                String title = "Expiry Alert";
                String message = productName + " will expire in " + daysUntilExpiry + 
                        " days (on " + expiryDate + "). Please take appropriate action.";
                
                notificationService.showExpiryNotification(title, message, 20000 + i);
            }
        }
    }
}
