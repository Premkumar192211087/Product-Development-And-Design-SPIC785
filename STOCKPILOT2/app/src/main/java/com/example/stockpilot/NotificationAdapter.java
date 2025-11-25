package com.example.stockpilot;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<NotificationModel> notificationList;
    private OnNotificationClickListener listener;

    // Interface for click events
    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationModel notification, int position);
    }

    // Constructor
    public NotificationAdapter(Context context, List<NotificationModel> notificationList, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_notificationrecycler, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);
        
        // Set notification data
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTimestamp.setText(notification.getFormattedTimestamp());
        holder.tvType.setText(notification.getTypeDisplayName());
        
        // Set notification type color
        int typeColor = notification.getTypeColor();
        holder.ivIcon.setBackgroundTintList(ColorStateList.valueOf(typeColor));
        holder.tvType.setBackgroundTintList(ColorStateList.valueOf(typeColor));
        
        // Set read/unread status indicator
        holder.viewStatusIndicator.setVisibility(notification.isRead() ? View.INVISIBLE : View.VISIBLE);
        
        // Set notification icon based on type
        setNotificationIcon(holder.ivIcon, notification.getType());
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // Update notification list
    public void setNotifications(List<NotificationModel> notifications) {
        this.notificationList = notifications;
        notifyDataSetChanged();
    }

    // Remove notification at position
    public void removeNotification(int position) {
        if (position >= 0 && position < notificationList.size()) {
            notificationList.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Set appropriate icon based on notification type
    private void setNotificationIcon(ImageView imageView, String type) {
        switch (type.toLowerCase()) {
            case "low_stock":
                imageView.setImageResource(R.drawable.ic_inventory);
                break;
            case "restock":
                imageView.setImageResource(R.drawable.ic_restock);
                break;
            case "expired":
                imageView.setImageResource(R.drawable.ic_expired);
                break;
            case "damaged":
                imageView.setImageResource(R.drawable.ic_damaged);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_notifications);
                break;
        }
    }

    // ViewHolder class
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTimestamp, tvType;
        ImageView ivIcon;
        View viewStatusIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTimestamp = itemView.findViewById(R.id.tv_notification_timestamp);
            tvType = itemView.findViewById(R.id.tv_notification_type);
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
            viewStatusIndicator = itemView.findViewById(R.id.view_status_indicator);
        }
    }
}
