
package com.example.stockpilot;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

 

public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private static final String TAG = "NotificationsActivity";
    
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ImageView backButton;
    private TextView tvStoreName;
    private ImageView emptyView;
    private SwipeRefreshLayout refreshLayout;
    private List<NotificationModel> notificationList;
    
    private String storeId;
    private String storeName;
    
    private UserSession userSession;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        
        
        
        // Initialize user session and get store info
        setupUserSession();
        
        // Initialize views
        initializeViews();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup listeners
        setupListeners();
        
        // Setup swipe to delete
        setupSwipeToDelete();
        
        // Fetch notifications
        fetchNotifications();
    }
    
    private void setupUserSession() {
        userSession = UserSession.getInstance(this);
        if (!userSession.isLoggedIn()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        storeId = userSession.getStoreId();
        storeName = userSession.getStoreName();
        
        // Check if we got store info from intent (for deep linking)
        Intent intent = getIntent();
        if (intent != null) {
            String intentStoreId = intent.getStringExtra("store_id");
            String intentStoreName = intent.getStringExtra("store_name");
            
            if (intentStoreId != null && !intentStoreId.isEmpty()) {
                storeId = intentStoreId;
            }
            
            if (intentStoreName != null && !intentStoreName.isEmpty()) {
                storeName = intentStoreName;
            }
        }
        
        if (storeId == null || storeId.isEmpty()) {
            Toast.makeText(this, "Store information not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initializeViews() {
        recyclerView = findViewById(R.id.rv_notifications);
        backButton = findViewById(R.id.backButton);
        tvStoreName = findViewById(R.id.tvStoreName);
        emptyView = findViewById(R.id.emptyView);
        refreshLayout = findViewById(R.id.refreshLayout);
        
        // Set store name in header
        tvStoreName.setText(storeName);
    }
    
    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        refreshLayout.setOnRefreshListener(this::fetchNotifications);
    }
    
    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                NotificationModel notification = notificationList.get(position);
                deleteNotification(notification.getId(), position);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void fetchNotifications() {
        refreshLayout.setRefreshing(true);
        ApiUrls.getApiService().getNotifications(storeId).enqueue(new retrofit2.Callback<ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<java.util.Map<String, Object>>> call, retrofit2.Response<ApiResponse<java.util.Map<String, Object>>> response) {
                refreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.Map<String, Object> data = response.body().getData();
                    java.util.List<java.util.Map<String, Object>> list = null;
                    if (data != null) {
                        Object arr = data.get("notifications");
                        if (arr instanceof java.util.List) {
                            list = (java.util.List<java.util.Map<String, Object>>) arr;
                        }
                    }
                    notificationList.clear();
                    if (list != null) {
                        for (java.util.Map<String, Object> m : list) {
                            int id = m.get("id") instanceof Number ? ((Number) m.get("id")).intValue() : 0;
                            String title = String.valueOf(m.get("title"));
                            String message = String.valueOf(m.get("message"));
                            String type = String.valueOf(m.get("type"));
                            String status = String.valueOf(m.get("status"));
                            String timestamp = String.valueOf(m.get("timestamp"));
                            int sId = 0;
                            try { sId = Integer.parseInt(storeId); } catch (Exception ignored) {}
                            notificationList.add(new NotificationModel(id, sId, title, message, type, status, timestamp));
                        }
                    }
                    adapter.setNotifications(notificationList);
                    emptyView.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(notificationList.isEmpty() ? View.GONE : View.VISIBLE);
                } else {
                    Toast.makeText(NotificationsActivity.this, Constants.ERROR_SERVER, Toast.LENGTH_SHORT).show();
                    refreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                refreshLayout.setRefreshing(false);
                Toast.makeText(NotificationsActivity.this, Constants.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void deleteNotification(int notificationId, int position) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("notification_id", String.valueOf(notificationId));
        requestBody.put("store_id", storeId);
        ApiUrls.getApiService().deleteNotification(requestBody).enqueue(new retrofit2.Callback<ApiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse> call, retrofit2.Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    int idx = position;
                    if (idx >= 0 && idx < notificationList.size()) {
                        notificationList.remove(idx);
                        adapter.setNotifications(notificationList);
                    }
                } else {
                    Toast.makeText(NotificationsActivity.this, Constants.ERROR_SERVER, Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse> call, Throwable t) {
                Toast.makeText(NotificationsActivity.this, Constants.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
            }
        });
    }
    
    @Override
    public void onNotificationClick(NotificationModel notification, int position) {
        // Mark notification as read
        if (notification.getStatus().equals("unread")) {
            markNotificationAsRead(notification.getId());
            notification.setStatus("read");
            adapter.notifyItemChanged(position);
        }
        
        // Handle notification click based on type
        switch (notification.getType()) {
            case "low_stock":
                navigateToInventory();
                break;
                
            case "expiry":
                navigateToInventory();
                break;
                
            case "purchase_order":
                navigateToPurchaseOrders();
                break;
                
            default:
                // Show notification details in a dialog
                showNotificationDetailsDialog(notification);
                break;
        }
    }

    private void markNotificationAsRead(int notificationId) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("notification_id", String.valueOf(notificationId));
        requestBody.put("store_id", storeId);
        ApiUrls.getApiService().markNotificationAsRead(requestBody).enqueue(new retrofit2.Callback<ApiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse> call, retrofit2.Response<ApiResponse> response) {}

            @Override
            public void onFailure(retrofit2.Call<ApiResponse> call, Throwable t) {}
        });
    }

    private void navigateToInventory() {
        Intent intent = new Intent(this, ItemsActivity.class);
        startActivity(intent);
    }
    
    private void navigateToPurchaseOrders() {
        Intent intent = new Intent(this, PurchaseOrdersActivity.class);
        startActivity(intent);
    }
    
    private void showNotificationDetailsDialog(NotificationModel notification) {
        new AlertDialog.Builder(this)
                .setTitle(notification.getTitle())
                .setMessage(notification.getMessage())
                .setPositiveButton("OK", null)
                .show();
    }
}
