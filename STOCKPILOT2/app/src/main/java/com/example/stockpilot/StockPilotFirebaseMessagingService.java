package com.example.stockpilot;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.example.stockpilot.InventoryReportsActivity;
import com.example.stockpilot.ItemsActivity;
import com.example.stockpilot.PurchaseOrderDetailsActivity;
import com.example.stockpilot.PurchaseOrdersActivity;
import com.example.stockpilot.NotificationsActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockPilotFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    
    // Channel IDs
    private static final String CHANNEL_LOW_STOCK = "low_stock_channel";
    private static final String CHANNEL_EXPIRY = "expiry_channel";
    private static final String CHANNEL_GENERAL = "general_channel";
    private static final String CHANNEL_PO = "purchase_order_channel";
    
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        
        // Create notification channels for Android O and above
        createNotificationChannels();
        
        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }
        
        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification(
                remoteMessage.getNotification().getTitle(),
                remoteMessage.getNotification().getBody(),
                CHANNEL_GENERAL,
                null
            );
        }
    }
    
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        
        // Get user and store IDs from UserSession
        UserSession userSession = UserSession.getInstance(this);
        if (userSession.isLoggedIn()) {
            String userId = userSession.getUserId();
            String storeId = userSession.getStoreId();
            
            // Send the new token to your server
            sendRegistrationToServer(token, userId, storeId);
        } else {
            Log.d(TAG, "User not logged in, token registration skipped");
        }
    }
    
    private void handleDataMessage(Map<String, String> data) {
        try {
            String title = data.get("title");
            String message = data.get("message");
            String type = data.get("type");
            String channelId = CHANNEL_GENERAL;
            Intent intent = null;
            
            // Determine which activity to open based on notification type
            switch (type) {
                case "low_stock":
                    channelId = CHANNEL_LOW_STOCK;
                    if (data.containsKey("product_id")) {
                        intent = new Intent(this, ItemsActivity.class);
                        intent.putExtra("product_id", data.get("product_id"));
                    } else {
                        intent = new Intent(this, ItemsActivity.class);
                    }
                    break;
                    
                case "expiry":
                    channelId = CHANNEL_EXPIRY;
                    if (data.containsKey("product_id")) {
                        intent = new Intent(this, ItemsActivity.class);
                        intent.putExtra("product_id", data.get("product_id"));
                    } else {
                        intent = new Intent(this, ItemsActivity.class);
                    }
                    break;
                    
                case "purchase_order":
                    channelId = CHANNEL_PO;
                    if (data.containsKey("po_id")) {
                        intent = new Intent(this, PurchaseOrderDetailsActivity.class);
                        intent.putExtra("po_id", data.get("po_id"));
                    } else {
                        intent = new Intent(this, PurchaseOrdersActivity.class);
                    }
                    break;
                    
                default:
                    intent = new Intent(this, NotificationsActivity.class);
                    break;
            }
            
            showNotification(title, message, channelId, intent);
            
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }
    
    private void showNotification(String title, String message, String channelId, Intent intent) {
        // Default intent if none provided
        if (intent == null) {
            intent = new Intent(this, NotificationsActivity.class);
        }
        
        // Get store ID and name from UserSession
        UserSession userSession = UserSession.getInstance(this);
        if (userSession.isLoggedIn()) {
            String storeId = userSession.getStoreId();
            String storeName = userSession.getStoreName();
            
            // Add store info to intent
            intent.putExtra("store_id", storeId);
            intent.putExtra("store_name", storeName);
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );
        
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        
        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Generate a unique notification ID
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
    
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            
            // Low Stock Channel
            NotificationChannel lowStockChannel = new NotificationChannel(
                CHANNEL_LOW_STOCK,
                "Low Stock Alerts",
                NotificationManager.IMPORTANCE_HIGH);
            lowStockChannel.setDescription("Notifications for products with low stock");
            lowStockChannel.enableLights(true);
            lowStockChannel.setLightColor(Color.RED);
            lowStockChannel.enableVibration(true);
            notificationManager.createNotificationChannel(lowStockChannel);
            
            // Expiry Channel
            NotificationChannel expiryChannel = new NotificationChannel(
                CHANNEL_EXPIRY,
                "Expiry Alerts",
                NotificationManager.IMPORTANCE_HIGH);
            expiryChannel.setDescription("Notifications for products nearing expiry");
            expiryChannel.enableLights(true);
            expiryChannel.setLightColor(Color.YELLOW);
            expiryChannel.enableVibration(true);
            notificationManager.createNotificationChannel(expiryChannel);
            
            // Purchase Order Channel
            NotificationChannel poChannel = new NotificationChannel(
                CHANNEL_PO,
                "Purchase Order Alerts",
                NotificationManager.IMPORTANCE_DEFAULT);
            poChannel.setDescription("Notifications for purchase orders");
            poChannel.enableLights(true);
            poChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(poChannel);
            
            // General Channel
            NotificationChannel generalChannel = new NotificationChannel(
                CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT);
            generalChannel.setDescription("General notifications");
            notificationManager.createNotificationChannel(generalChannel);
        }
    }
    
    private void sendRegistrationToServer(String token, String userId, String storeId) {
        Log.d(TAG, "Data layer removed");
    }
}
