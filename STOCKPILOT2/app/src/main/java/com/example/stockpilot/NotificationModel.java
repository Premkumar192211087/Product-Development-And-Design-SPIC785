package com.example.stockpilot;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationModel implements Serializable {
    private int id;
    private int storeId;
    private String title;
    private String message;
    private String type;
    private String status;
    private String timestamp;

    // Constructor
    public NotificationModel(int id, int storeId, String title, String message, String type, String status, String timestamp) {
        this.id = id;
        this.storeId = storeId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getStoreId() {
        return storeId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    // Utility methods
    public boolean isRead() {
        return "read".equals(status);
    }

    public String getFormattedTimestamp() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date notificationTime = sdf.parse(timestamp);
            Date now = new Date();

            long diffInMillis = now.getTime() - notificationTime.getTime();
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            if (diffInMinutes < 60) {
                return diffInMinutes + "m ago";
            } else if (diffInHours < 24) {
                return diffInHours + "h ago";
            } else if (diffInDays < 7) {
                return diffInDays + "d ago";
            } else {
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return outputFormat.format(notificationTime);
            }
        } catch (ParseException e) {
            return timestamp;
        }
    }

    public String getDetailedTimestamp() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy • h:mm a", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return timestamp;
        }
    }

    // Get appropriate color for notification type
    public int getTypeColor() {
        switch (type.toLowerCase()) {
            case "low_stock":
                return 0xFFE57373; // Light Red
            case "restock":
                return 0xFF81C784; // Light Green
            case "expired":
                return 0xFFFFB74D; // Light Orange
            case "damaged":
                return 0xFFE53935; // Red
            default:
                return 0xFF5C6BC0; // Indigo - default
        }
    }

    // Get display name for type
    public String getTypeDisplayName() {
        switch (type.toLowerCase()) {
            case "low_stock":
                return "LOW STOCK";
            case "restock":
                return "RESTOCK";
            case "expired":
                return "EXPIRED";
            case "damaged":
                return "DAMAGED";
            default:
                return type.toUpperCase(Locale.getDefault());
        }
    }
}
