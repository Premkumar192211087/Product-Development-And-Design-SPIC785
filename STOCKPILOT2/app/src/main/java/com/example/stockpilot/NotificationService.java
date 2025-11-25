package com.example.stockpilot;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.stockpilot.R;
import com.example.stockpilot.NotificationsActivity;

import java.util.concurrent.TimeUnit;

public class NotificationService {
    private static final String TAG = "NotificationService";
    
    // Notification channel IDs
    public static final String CHANNEL_LOW_STOCK = "low_stock_channel";
    public static final String CHANNEL_EXPIRY = "expiry_channel";
    public static final String CHANNEL_DAMAGED = "damaged_channel";
    public static final String CHANNEL_GENERAL = "general_channel";
    
    // Notification IDs
    private static final int NOTIFICATION_ID_LOW_STOCK = 1001;
    private static final int NOTIFICATION_ID_EXPIRY = 2001;
    private static final int NOTIFICATION_ID_DAMAGED = 3001;
    private static final int NOTIFICATION_ID_GENERAL = 4001;
    
    // Work request tags
    private static final String WORK_TAG_FETCH_NOTIFICATIONS = "fetch_notifications_work";
    private static final String WORK_TAG_CHECK_THRESHOLDS = "check_thresholds_work";
    
    private static NotificationService instance;
    private final Context context;
    
    private NotificationService(Context context) {
        this.context = context.getApplicationContext();
        createNotificationChannels();
    }
    
    public static synchronized NotificationService getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationService(context);
        }
        return instance;
    }
    
    /**
     * Create notification channels for different notification types
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            // Low Stock Channel
            NotificationChannel lowStockChannel = new NotificationChannel(
                    CHANNEL_LOW_STOCK,
                    "Low Stock Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            lowStockChannel.setDescription("Notifications for products with low stock");
            lowStockChannel.enableLights(true);
            lowStockChannel.setLightColor(ContextCompat.getColor(context, R.color.error));
            lowStockChannel.enableVibration(true);
            notificationManager.createNotificationChannel(lowStockChannel);
            
            // Expiry Channel
            NotificationChannel expiryChannel = new NotificationChannel(
                    CHANNEL_EXPIRY,
                    "Expiry Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            expiryChannel.setDescription("Notifications for products nearing expiry");
            expiryChannel.enableLights(true);
            expiryChannel.setLightColor(ContextCompat.getColor(context, R.color.warning));
            expiryChannel.enableVibration(true);
            notificationManager.createNotificationChannel(expiryChannel);
            
            // Damaged Channel
            NotificationChannel damagedChannel = new NotificationChannel(
                    CHANNEL_DAMAGED,
                    "Damaged Items Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT);
            damagedChannel.setDescription("Notifications for damaged items");
            damagedChannel.enableLights(true);
            damagedChannel.setLightColor(ContextCompat.getColor(context, R.color.info));
            damagedChannel.enableVibration(true);
            notificationManager.createNotificationChannel(damagedChannel);
            
            // General Channel
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            generalChannel.setDescription("General notifications");
            notificationManager.createNotificationChannel(generalChannel);
        }
    }
    
    /**
     * Schedule periodic work to fetch notifications from the server
     */
    public void scheduleNotificationFetching() {
        // Check if notifications are enabled
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        
        if (!notificationsEnabled) {
            Log.d(TAG, "Notifications are disabled. Not scheduling notification fetching.");
            return;
        }
        
        // Set constraints - require network connection
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        // Create periodic work request - run every 15 minutes
        PeriodicWorkRequest fetchNotificationsWork =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .addTag(WORK_TAG_FETCH_NOTIFICATIONS)
                        .build();
        
        // Enqueue the work request
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG_FETCH_NOTIFICATIONS,
                ExistingPeriodicWorkPolicy.REPLACE,
                fetchNotificationsWork);
        
        Log.d(TAG, "Scheduled notification fetching work");
    }
    
    /**
     * Schedule periodic work to check thresholds and generate notifications
     */
    public void scheduleThresholdChecking() {
        // Check if notifications are enabled
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        boolean lowStockEnabled = sharedPreferences.getBoolean("low_stock_notifications_enabled", true);
        boolean expiryEnabled = sharedPreferences.getBoolean("expiry_notifications_enabled", true);
        
        if (!notificationsEnabled || (!lowStockEnabled && !expiryEnabled)) {
            Log.d(TAG, "Relevant notifications are disabled. Not scheduling threshold checking.");
            return;
        }
        
        // Set constraints - require network connection
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        // Create periodic work request - run every 6 hours
        PeriodicWorkRequest checkThresholdsWork =
                new PeriodicWorkRequest.Builder(ThresholdCheckWorker.class, 6, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .addTag(WORK_TAG_CHECK_THRESHOLDS)
                        .build();
        
        // Enqueue the work request
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG_CHECK_THRESHOLDS,
                ExistingPeriodicWorkPolicy.REPLACE,
                checkThresholdsWork);
        
        Log.d(TAG, "Scheduled threshold checking work");
    }
    
    /**
     * Cancel scheduled notification fetching
     */
    public void cancelNotificationFetching() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG_FETCH_NOTIFICATIONS);
        Log.d(TAG, "Cancelled notification fetching work");
    }
    
    /**
     * Cancel scheduled threshold checking
     */
    public void cancelThresholdChecking() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG_CHECK_THRESHOLDS);
        Log.d(TAG, "Cancelled threshold checking work");
    }
    
    /**
     * Show a low stock notification
     */
    public void showLowStockNotification(String title, String message, int notificationId) {
        // Check if low stock notifications are enabled
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        boolean lowStockEnabled = sharedPreferences.getBoolean("low_stock_notifications_enabled", true);
        
        if (!lowStockEnabled) {
            Log.d(TAG, "Low stock notifications are disabled. Not showing notification for: " + title);
            return;
        }
        
        // Create intent for notification click
        Intent intent = new Intent(context, NotificationsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);
        
        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_LOW_STOCK)
                .setSmallIcon(R.drawable.ic_inventory)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(ContextCompat.getColor(context, R.color.error));
        
        // Show notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
        
        Log.d(TAG, "Showed low stock notification: " + title);
    }
    
    /**
     * Show an expiry notification
     */
    public void showExpiryNotification(String title, String message, int notificationId) {
        // Check if expiry notifications are enabled
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        boolean expiryEnabled = sharedPreferences.getBoolean("expiry_notifications_enabled", true);
        
        if (!expiryEnabled) {
            Log.d(TAG, "Expiry notifications are disabled. Not showing notification for: " + title);
            return;
        }
        
        // Create intent for notification click
        Intent intent = new Intent(context, NotificationsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);
        
        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_EXPIRY)
                .setSmallIcon(R.drawable.ic_expired)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(ContextCompat.getColor(context, R.color.warning));
        
        // Show notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
        
        Log.d(TAG, "Showed expiry notification: " + title);
    }
    
    /**
     * Show a damaged items notification
     */
    public void showDamagedItemNotification(String title, String message, int notificationId) {
        // Check if damaged items notifications are enabled
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        boolean damagedEnabled = sharedPreferences.getBoolean("damaged_items_notifications_enabled", true);
        
        if (!damagedEnabled) {
            Log.d(TAG, "Damaged items notifications are disabled. Not showing notification for: " + title);
            return;
        }
        
        // Create intent for notification click
        Intent intent = new Intent(context, NotificationsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);
        
        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_DAMAGED)
                .setSmallIcon(R.drawable.ic_damaged)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(ContextCompat.getColor(context, R.color.info));
        
        // Show notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
        
        Log.d(TAG, "Showed damaged item notification: " + title);
    }
    
    /**
     * Show a general notification
     */
    public void showGeneralNotification(String title, String message, int notificationId) {
        // Create intent for notification click
        Intent intent = new Intent(context, NotificationsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);
        
        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_GENERAL)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        
        // Show notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
        
        Log.d(TAG, "Showed general notification: " + title);
    }
}
