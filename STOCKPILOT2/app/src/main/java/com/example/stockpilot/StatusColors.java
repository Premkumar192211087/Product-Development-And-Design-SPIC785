package com.example.stockpilot;

import android.content.Context;
import androidx.core.content.ContextCompat;

/**
 * Centralized mapping from domain statuses to color resources.
 * Use these helpers to avoid hardcoded colors and enable dark mode support.
 */
public class StatusColors {

    public static int forPurchaseOrderStatus(Context context, String status) {
        if (status == null) return ContextCompat.getColor(context, R.color.medium_gray);
        String s = status.trim().toLowerCase();
        switch (s) {
            case "draft":
                return ContextCompat.getColor(context, R.color.status_draft);
            case "approved":
                return ContextCompat.getColor(context, R.color.status_approved);
            case "sent":
                return ContextCompat.getColor(context, R.color.status_sent);
            case "received":
                return ContextCompat.getColor(context, R.color.status_received);
            case "cancelled":
                return ContextCompat.getColor(context, R.color.status_cancelled);
            case "closed":
                return ContextCompat.getColor(context, R.color.status_closed);
            default:
                return ContextCompat.getColor(context, R.color.medium_gray);
        }
    }

    public static int forShipmentStatus(Context context, String status) {
        if (status == null) return ContextCompat.getColor(context, R.color.medium_gray);
        String s = status.trim().toLowerCase();
        switch (s) {
            case "pending":
                return ContextCompat.getColor(context, R.color.pending_color);
            case "packed":
                return ContextCompat.getColor(context, R.color.status_packed);
            case "shipped":
                return ContextCompat.getColor(context, R.color.status_shipped);
            case "delivered":
                return ContextCompat.getColor(context, R.color.status_delivered);
            default:
                return ContextCompat.getColor(context, R.color.medium_gray);
        }
    }

    public static int forInvoiceStatus(Context context, String status) {
        if (status == null) return ContextCompat.getColor(context, R.color.medium_gray);
        String s = status.trim().toLowerCase();
        switch (s) {
            case "paid":
                return ContextCompat.getColor(context, R.color.status_paid);
            case "unpaid":
                return ContextCompat.getColor(context, R.color.status_unpaid);
            case "overdue":
                return ContextCompat.getColor(context, R.color.status_overdue);
            default:
                return ContextCompat.getColor(context, R.color.medium_gray);
        }
    }

    public static int forStockLevel(Context context, String level) {
        if (level == null) return ContextCompat.getColor(context, R.color.medium_gray);
        String s = level.trim().toLowerCase();
        switch (s) {
            case "low":
                return ContextCompat.getColor(context, R.color.colorLowStock);
            case "medium":
                return ContextCompat.getColor(context, R.color.colorMediumStock);
            case "high":
                return ContextCompat.getColor(context, R.color.colorHighStock);
            default:
                return ContextCompat.getColor(context, R.color.medium_gray);
        }
    }
}