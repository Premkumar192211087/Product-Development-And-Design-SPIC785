package com.example.stockpilot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.stockpilot.R;
import com.example.stockpilot.NotificationModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationDetailActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView tvStoreName;
    private TextView tvTitle;
    private TextView tvMessage;
    private TextView tvTimestamp;
    private TextView tvType;
    private ImageView ivIcon;
    private CardView cardType;
    
    private NotificationModel notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);
        
        // Initialize views
        initializeViews();
        
        // Get notification from intent
        getNotificationFromIntent();
        
        // Set up listeners
        setupListeners();
        
        // Display notification details
        displayNotificationDetails();
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.iv_back);
        tvStoreName = findViewById(R.id.tv_store_name);
        tvTitle = findViewById(R.id.tv_notification_title);
        tvMessage = findViewById(R.id.tv_notification_message);
        tvTimestamp = findViewById(R.id.tv_notification_timestamp);
        tvType = findViewById(R.id.tv_notification_type);
        ivIcon = findViewById(R.id.iv_notification_icon);
        cardType = findViewById(R.id.card_notification_type);
    }
    
    private void getNotificationFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            notification = (NotificationModel) intent.getSerializableExtra("notification");
            String storeName = intent.getStringExtra("store_name");
            
            if (tvStoreName != null && storeName != null) {
                tvStoreName.setText(storeName);
            }
        }
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
    }
    
    private void displayNotificationDetails() {
        if (notification != null) {
            // Set title and message
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());
            
            // Format and set timestamp
            String formattedTimestamp = formatTimestamp(notification.getTimestamp());
            tvTimestamp.setText(formattedTimestamp);
            
            // Set type display name
            tvType.setText(notification.getTypeDisplayName());
            
            // Set type color
            cardType.setCardBackgroundColor(notification.getTypeColor());
            
            // Set icon based on notification type
            setNotificationIcon(notification.getType());
            
            // Add additional content based on notification type
            addTypeSpecificContent(notification.getType());
        }
    }
    
    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return timestamp;
        }
    }
    
    private void setNotificationIcon(String type) {
        switch (type) {
            case "low_stock":
                ivIcon.setImageResource(R.drawable.ic_inventory);
                break;
            case "restock":
                ivIcon.setImageResource(R.drawable.ic_restock);
                break;
            case "expired":
                ivIcon.setImageResource(R.drawable.ic_expired);
                break;
            case "damaged":
                ivIcon.setImageResource(R.drawable.ic_damaged);
                break;
            default:
                ivIcon.setImageResource(R.drawable.ic_notifications);
                break;
        }
    }
    
    private void addTypeSpecificContent(String type) {
        // Add additional content based on notification type
        // This can be implemented later based on specific requirements
        // For example, showing product details for low stock notifications
    }
}
