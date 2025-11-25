package com.example.stockpilot;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHomeActivity extends AppCompatActivity {

    private TextView tvStoreName;
    private TextView tvToBePacked, tvToBeShipped, tvToBeDelivered, tvToBeInvoiced, tvInHand, tvToBeReceived;
    

    // Navigation elements
    private DrawerLayout drawerLayout;
    private ImageView menuBtn;

    // Add notification button
    private ImageView notificationBtn;

    // Main navigation sections
    private LinearLayout navHome, navInventory, navSales, navPurchases, navReports, navSettings, navLogout;

    // Dropdown menus
    private LinearLayout inventorySubmenu, salesSubmenu, purchasesSubmenu;

    // Dropdown indicators
    private ImageView inventoryExpandIcon, salesExpandIcon, purchasesExpandIcon;

    // Individual menu items
    private TextView navItems, navItemGroups, navInventoryAdjustments, navBatchDetails;
    private TextView navCustomers, navSalesOrders, navShipments, navInvoices, navPaymentsReceived, navSalesReturns;
    private TextView navVendors, navPurchaseOrders, navPurchaseReceives, navBills, navPaymentsMade, navVendorCredits;

    // Track dropdown states
    private boolean isInventoryExpanded = false;
    private boolean isSalesExpanded = false;
    private boolean isPurchasesExpanded = false;

    // Updated to use String to match UserSession
    private String storeId;
    private String storeName;
    private UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_admin_home); // You may want to rename this to activity_admin_home.xml

        // Initialize UserSession
        userSession = UserSession.getInstance(this);

        

        // Check if user is logged in
        if (!userSession.isLoggedIn()) {
            redirectToLogin();
            return;
        }


        // Load user session data
        storeId = String.valueOf(userSession.getStoreId());
        storeName = userSession.getStoreName();

        if (storeId == null || storeId.isEmpty()) {
            redirectToLogin();
            return;
        }

        // Initialize views
        initializeViews();

        // Setup navigation
        setupNavigation();

        // Setup notification and home click listeners
        setupTopBarListeners();

        // Initialize dropdown states
        initializeDropdownStates();

        // Set store name in toolbar
        if (tvStoreName != null) {
            tvStoreName.setText(storeName);
        }

        // Find dashboard views
        tvToBePacked = findViewById(R.id.tv_to_be_packed);
        tvToBeShipped = findViewById(R.id.tv_to_be_shipped);
        tvToBeDelivered = findViewById(R.id.tv_to_be_delivered);
        tvToBeInvoiced = findViewById(R.id.tv_to_be_invoiced);
        tvInHand = findViewById(R.id.tv_in_hand);
        tvToBeReceived = findViewById(R.id.tv_to_be_received);

        // Fetch dashboard data
        fetchDashboardData();

        // Initialize and schedule notification service
        initializeNotificationService();
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void initializeViews() {
        // Drawer and menu button
        drawerLayout = findViewById(R.id.drawer_layout);
        menuBtn = findViewById(R.id.menuBtn);

        // Initialize notification button
        notificationBtn = findViewById(R.id.notificationBtn);

        // Initialize store name TextView
        tvStoreName = findViewById(R.id.appTitle);

        // Main navigation sections with debug logging
        navHome = findViewById(R.id.nav_home);
        if (navHome == null) {
            android.util.Log.e("AdminHome", "navHome is null - check R.id.nav_home in layout");
        }

        navInventory = findViewById(R.id.nav_inventory);
        if (navInventory == null) {
            android.util.Log.e("AdminHome", "navInventory is null - check R.id.nav_inventory in layout");
        }

        navSales = findViewById(R.id.nav_sales);
        if (navSales == null) {
            android.util.Log.e("AdminHome", "navSales is null - check R.id.nav_sales in layout");
        }

        navPurchases = findViewById(R.id.nav_purchases);
        if (navPurchases == null) {
            android.util.Log.e("AdminHome", "navPurchases is null - check R.id.nav_purchases in layout");
        }

        navReports = findViewById(R.id.nav_reports);
        if (navReports == null) {
            android.util.Log.e("AdminHome", "navReports is null - check R.id.nav_reports in layout");
        }

        navSettings = findViewById(R.id.nav_settings);
        if (navSettings == null) {
            android.util.Log.e("AdminHome", "navSettings is null - check R.id.nav_settings in layout");
        }

        navLogout = findViewById(R.id.nav_logout);
        if (navLogout == null) {
            android.util.Log.e("AdminHome", "navLogout is null - check R.id.nav_logout in layout");
        }

        // Dropdown menus
        inventorySubmenu = findViewById(R.id.inventory_submenu);
        if (inventorySubmenu == null) {
            android.util.Log.e("AdminHome", "inventorySubmenu is null - check R.id.inventory_submenu in layout");
        }

        salesSubmenu = findViewById(R.id.sales_submenu);
        if (salesSubmenu == null) {
            android.util.Log.e("AdminHome", "salesSubmenu is null - check R.id.sales_submenu in layout");
        }

        purchasesSubmenu = findViewById(R.id.purchases_submenu);
        if (purchasesSubmenu == null) {
            android.util.Log.e("AdminHome", "purchasesSubmenu is null - check R.id.purchases_submenu in layout");
        }

        // Get expand icons from the navigation sections
        if (navInventory != null) {
            inventoryExpandIcon = navInventory.findViewById(R.id.inventory_expand_icon);
            if (inventoryExpandIcon == null) {
                android.util.Log.e("AdminHome", "inventoryExpandIcon is null - check R.id.inventory_expand_icon in nav_inventory layout");
            }
        }

        if (navSales != null) {
            salesExpandIcon = navSales.findViewById(R.id.sales_expand_icon);
            if (salesExpandIcon == null) {
                android.util.Log.e("AdminHome", "salesExpandIcon is null - check R.id.sales_expand_icon in nav_sales layout");
            }
        }

        if (navPurchases != null) {
            purchasesExpandIcon = navPurchases.findViewById(R.id.purchases_expand_icon);
            if (purchasesExpandIcon == null) {
                android.util.Log.e("AdminHome", "purchasesExpandIcon is null - check R.id.purchases_expand_icon in nav_purchases layout");
            }
        }

        // Individual menu items - Inventory
        navItems = findViewById(R.id.nav_items);
        navItemGroups = findViewById(R.id.nav_item_groups);
        navInventoryAdjustments = findViewById(R.id.nav_inventory_adjustments);
        navBatchDetails = findViewById(R.id.nav_batch_details);

        // Individual menu items - Sales
        navCustomers = findViewById(R.id.nav_customers);
        navSalesOrders = findViewById(R.id.nav_sales_orders);
        navShipments = findViewById(R.id.nav_shipments);
        navInvoices = findViewById(R.id.nav_invoices);
        navPaymentsReceived = findViewById(R.id.nav_payments_received);
        navSalesReturns = findViewById(R.id.nav_sales_returns);

        // Individual menu items - Purchases
        navVendors = findViewById(R.id.nav_vendors);
        navPurchaseOrders = findViewById(R.id.nav_purchase_orders);
        navPurchaseReceives = findViewById(R.id.nav_purchase_receives);
        navBills = findViewById(R.id.nav_bills);
        navPaymentsMade = findViewById(R.id.nav_payments_made);
        navVendorCredits = findViewById(R.id.nav_vendor_credits);
    }

    private void setupTopBarListeners() {
        if (menuBtn != null) {
            menuBtn.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.openDrawer(GravityCompat.START);
            });
        }
        if (notificationBtn != null) notificationBtn.setOnClickListener(v -> handleNotificationClick());
        if (tvStoreName != null) tvStoreName.setOnClickListener(v -> handleHomeClick());
    }

    private void handleNotificationClick() {
        // Add animation to notification icon
        if (notificationBtn != null) {
            // Scale animation for visual feedback
            notificationBtn.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        notificationBtn.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        }

        // Navigate to Notifications Activity
        navigateToNotifications();
    }

    private void initializeNotificationService() {
        // Create and initialize the notification service
        NotificationService notificationService = NotificationService.getInstance(this);

        // Create notification channels
        // This is already done in the NotificationService constructor

        // Schedule periodic notification fetching
        notificationService.scheduleNotificationFetching();

        // Schedule periodic threshold checking for low stock and expiry notifications
        notificationService.scheduleThresholdChecking();
    }

    private void handleHomeClick() {
        // Add subtle animation to title
        if (tvStoreName != null) {
            tvStoreName.animate()
                    .alpha(0.7f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        tvStoreName.animate()
                                .alpha(1.0f)
                                .setDuration(150)
                                .start();
                    })
                    .start();
        }

        // Refresh the home dashboard
        refreshHomeDashboard();
    }

    private void navigateToNotifications() {
        Intent intent = new Intent(this, NotificationsActivity.class);
        intent.putExtra("store_id", storeId);
        intent.putExtra("store_name", storeName);
        startActivity(intent);
    }

    private void refreshHomeDashboard() {
        // Close any open drawers
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        // Collapse any open dropdown menus
        collapseAllDropdowns();

        // Refresh dashboard data
        fetchDashboardData();

        // Optional: Add a refresh animation to dashboard cards
        animateDashboardRefresh();
    }

    private void animateDashboardRefresh() {
        // Add a subtle animation to indicate refresh
        View[] dashboardViews = {tvToBePacked, tvToBeShipped, tvToBeDelivered,
                tvToBeInvoiced, tvInHand, tvToBeReceived};

        for (int i = 0; i < dashboardViews.length; i++) {
            if (dashboardViews[i] != null) {
                dashboardViews[i].animate()
                        .alpha(0.5f)
                        .setDuration(200)
                        .setStartDelay(i * 50) // Stagger the animation
                        .withEndAction(() -> {
                            // After fade out, fade back in
                            for (View view : dashboardViews) {
                                if (view != null) {
                                    view.animate()
                                            .alpha(1.0f)
                                            .setDuration(300)
                                            .start();
                                }
                            }
                        })
                        .start();
            }
        }
    }

    private void initializeDropdownStates() {
        // Initially hide all submenus and set icons to collapsed state
        if (inventorySubmenu != null) {
            inventorySubmenu.setVisibility(View.GONE);
        }
        if (salesSubmenu != null) {
            salesSubmenu.setVisibility(View.GONE);
        }
        if (purchasesSubmenu != null) {
            purchasesSubmenu.setVisibility(View.GONE);
        }

        // Set initial icon rotations with null checks
        if (inventoryExpandIcon != null) {
            inventoryExpandIcon.setRotation(0);
        }
        if (salesExpandIcon != null) {
            salesExpandIcon.setRotation(0);
        }
        if (purchasesExpandIcon != null) {
            purchasesExpandIcon.setRotation(0);
        }
    }

    private void setupNavigation() {
        // Main navigation clicks - Enhanced Home handler with null check
        if (navHome != null) {
            navHome.setOnClickListener(v -> handleNavigationClick("Home"));
        }


        // Dropdown toggles with enhanced functionality and null checks
        if (navInventory != null) {
            navInventory.setOnClickListener(v -> {
                if (inventorySubmenu != null && inventoryExpandIcon != null) {
                    toggleDropdown(inventorySubmenu, inventoryExpandIcon, () -> {
                        isInventoryExpanded = !isInventoryExpanded;
                        // Close other dropdowns when one is opened
                        if (isInventoryExpanded) {
                            closeOtherDropdowns("inventory");
                        }
                    });
                }
            });
        }

        if (navSales != null) {
            navSales.setOnClickListener(v -> {
                if (salesSubmenu != null && salesExpandIcon != null) {
                    toggleDropdown(salesSubmenu, salesExpandIcon, () -> {
                        isSalesExpanded = !isSalesExpanded;
                        if (isSalesExpanded) {
                            closeOtherDropdowns("sales");
                        }
                    });
                }
            });
        }

        if (navPurchases != null) {
            navPurchases.setOnClickListener(v -> {
                if (purchasesSubmenu != null && purchasesExpandIcon != null) {
                    toggleDropdown(purchasesSubmenu, purchasesExpandIcon, () -> {
                        isPurchasesExpanded = !isPurchasesExpanded;
                        if (isPurchasesExpanded) {
                            closeOtherDropdowns("purchases");
                        }
                    });
                }
            });
        }

        if (navReports != null) {
            navReports.setOnClickListener(v -> handleNavigationClick("Reports"));
        }

        // Direct navigation clicks with null checks
        if (navSettings != null) {
            navSettings.setOnClickListener(v -> handleNavigationClick("Settings"));
        }
        if (navLogout != null) {
            navLogout.setOnClickListener(v -> handleLogout());
        }

        // Inventory submenu clicks with null checks
        if (navItems != null) {
            navItems.setOnClickListener(v -> handleNavigationClick("Items"));
        }
        if (navItemGroups != null) {
            navItemGroups.setOnClickListener(v -> handleNavigationClick("Item Groups"));
        }
        if (navInventoryAdjustments != null) {
            navInventoryAdjustments.setOnClickListener(v -> handleNavigationClick("Inventory Adjustments"));
        }
        if (navBatchDetails != null) {
            navBatchDetails.setOnClickListener(v -> handleNavigationClick("Batch Details"));
        }

        // Sales submenu clicks with null checks
        if (navCustomers != null) {
            navCustomers.setOnClickListener(v -> handleNavigationClick("Customers"));
        }
        if (navSalesOrders != null) {
            navSalesOrders.setOnClickListener(v -> handleNavigationClick("Sales Orders"));
        }
        if (navShipments != null) {
            navShipments.setOnClickListener(v -> handleNavigationClick("Shipments"));
        }
        if (navInvoices != null) {
            navInvoices.setOnClickListener(v -> handleNavigationClick("Invoices"));
        }
        if (navPaymentsReceived != null) {
            navPaymentsReceived.setOnClickListener(v -> handleNavigationClick("Payments Received"));
        }
        if (navSalesReturns != null) {
            navSalesReturns.setOnClickListener(v -> handleNavigationClick("Sales Returns"));
        }

        // Purchases submenu clicks with null checks
        if (navVendors != null) {
            navVendors.setOnClickListener(v -> handleNavigationClick("Vendors"));
        }
        if (navPurchaseOrders != null) {
            navPurchaseOrders.setOnClickListener(v -> handleNavigationClick("Purchase Orders"));
        }
        if (navPurchaseReceives != null) {
            navPurchaseReceives.setOnClickListener(v -> handleNavigationClick("Purchase Receives"));
        }
        if (navBills != null) {
            navBills.setOnClickListener(v -> handleNavigationClick("Bills"));
        }
        if (navPaymentsMade != null) {
            navPaymentsMade.setOnClickListener(v -> handleNavigationClick("Payments Made"));
        }
        if (navVendorCredits != null) {
            navVendorCredits.setOnClickListener(v -> handleNavigationClick("Vendor Credits"));
        }
    }

    private void toggleDropdown(LinearLayout submenu, ImageView expandIcon, Runnable callback) {
        if (submenu.getVisibility() == View.VISIBLE) {
            // Hide submenu with animation
            collapseSubmenu(submenu, expandIcon);
        } else {
            // Show submenu with animation
            expandSubmenu(submenu, expandIcon);
        }

        // Execute callback to update state
        if (callback != null) {
            callback.run();
        }
    }

    private void expandSubmenu(LinearLayout submenu, ImageView expandIcon) {
        // Show the submenu
        if (submenu != null) {
            submenu.setVisibility(View.VISIBLE);
        }

        // Rotate icon animation
        if (expandIcon != null) {
            RotateAnimation rotateAnimation = new RotateAnimation(
                    0f, 180f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotateAnimation.setDuration(300);
            rotateAnimation.setFillAfter(true);
            expandIcon.startAnimation(rotateAnimation);
        }
    }

    private void collapseSubmenu(LinearLayout submenu, ImageView expandIcon) {
        // Hide the submenu
        if (submenu != null) {
            submenu.setVisibility(View.GONE);
        }

        // Rotate icon animation back
        if (expandIcon != null) {
            RotateAnimation rotateAnimation = new RotateAnimation(
                    180f, 0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotateAnimation.setDuration(300);
            rotateAnimation.setFillAfter(true);
            expandIcon.startAnimation(rotateAnimation);
        }
    }

    private void closeOtherDropdowns(String currentDropdown) {
        switch (currentDropdown) {
            case "inventory":
                if (isSalesExpanded && salesSubmenu != null && salesExpandIcon != null) {
                    collapseSubmenu(salesSubmenu, salesExpandIcon);
                    isSalesExpanded = false;
                }
                if (isPurchasesExpanded && purchasesSubmenu != null && purchasesExpandIcon != null) {
                    collapseSubmenu(purchasesSubmenu, purchasesExpandIcon);
                    isPurchasesExpanded = false;
                }
                break;
            case "sales":
                if (isInventoryExpanded && inventorySubmenu != null && inventoryExpandIcon != null) {
                    collapseSubmenu(inventorySubmenu, inventoryExpandIcon);
                    isInventoryExpanded = false;
                }
                if (isPurchasesExpanded && purchasesSubmenu != null && purchasesExpandIcon != null) {
                    collapseSubmenu(purchasesSubmenu, purchasesExpandIcon);
                    isPurchasesExpanded = false;
                }
                break;
            case "purchases":
                if (isInventoryExpanded && inventorySubmenu != null && inventoryExpandIcon != null) {
                    collapseSubmenu(inventorySubmenu, inventoryExpandIcon);
                    isInventoryExpanded = false;
                }
                if (isSalesExpanded && salesSubmenu != null && salesExpandIcon != null) {
                    collapseSubmenu(salesSubmenu, salesExpandIcon);
                    isSalesExpanded = false;
                }
                break;
        }
    }

    private void handleNavigationClick(String menuItem) {
        // Close the drawer after a short delay to allow user to see the selection
        if (drawerLayout != null) {
            drawerLayout.postDelayed(() -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }, 150);
        }

        // Navigate to separate activities based on menu item
        switch (menuItem) {
            case "Home":
                // Enhanced home handling - refresh dashboard and reset UI
                refreshHomeDashboard();
                break;
            case "Items":
                Intent itemsIntent = new Intent(this, ItemsActivity.class);
                itemsIntent.putExtra("store_id", storeId);
                itemsIntent.putExtra("store_name", storeName);
                startActivity(itemsIntent);
                break;
            case "Item Groups":
                Intent itemGroupsIntent = new Intent(this, ItemGroupsActivity.class);
                itemGroupsIntent.putExtra("store_id", storeId);
                itemGroupsIntent.putExtra("store_name", storeName);
                startActivity(itemGroupsIntent);
                break;
            case "Inventory Adjustments":
                Intent inventoryAdjustmentsIntent = new Intent(this, InventoryAdjustmentsActivity.class);
                inventoryAdjustmentsIntent.putExtra("store_id", storeId);
                inventoryAdjustmentsIntent.putExtra("store_name", storeName);
                startActivity(inventoryAdjustmentsIntent);
                break;
            case "Batch Details":
                Intent batchListIntent = new Intent(this, BatchListActivity.class);
                batchListIntent.putExtra("store_id", storeId);
                batchListIntent.putExtra("store_name", storeName);
                startActivity(batchListIntent);
                break;
            case "Customers":
                Intent customersIntent = new Intent(this, CustomersActivity.class);
                customersIntent.putExtra("store_id", storeId);
                customersIntent.putExtra("store_name", storeName);
                startActivity(customersIntent);
                break;
            case "Sales Orders":
                Intent salesOrdersIntent = new Intent(this, SalesOrdersActivity.class);
                salesOrdersIntent.putExtra("store_id", storeId);
                salesOrdersIntent.putExtra("store_name", storeName);
                startActivity(salesOrdersIntent);
                break;

            case "Shipments":
                Intent shipmentsIntent = new Intent(this, ShipmentActivity.class);
                shipmentsIntent.putExtra("store_id", storeId);
                shipmentsIntent.putExtra("store_name", storeName);
                startActivity(shipmentsIntent);
                break;
            case "Invoices":
                try {
                    Intent invoicesIntent = new Intent(this, Class.forName("com.example.stockpilot.models.invoices"));
                    invoicesIntent.putExtra("store_id", storeId);
                    invoicesIntent.putExtra("store_name", storeName);
                    startActivity(invoicesIntent);
                } catch (ClassNotFoundException e) {
                    Toast.makeText(this, "Invoices activity not found", Toast.LENGTH_SHORT).show();
                }
                break;

            case "Sales Returns":
                try {
                    Intent salesReturnsIntent = new Intent(this, Class.forName("com.example.stockpilot.models.sales_return"));
                    salesReturnsIntent.putExtra("store_id", storeId);
                    salesReturnsIntent.putExtra("store_name", storeName);
                    startActivity(salesReturnsIntent);
                } catch (ClassNotFoundException e) {
                    Toast.makeText(this, "Sales Returns activity not found", Toast.LENGTH_SHORT).show();
                }
                break;

            case "Purchase Orders":
                Intent purchaseOrdersIntent = new Intent(this, PurchaseOrdersActivity.class);
                purchaseOrdersIntent.putExtra("store_id", storeId);
                purchaseOrdersIntent.putExtra("store_name", storeName);
                startActivity(purchaseOrdersIntent);
                break;
            // In the method handleNavigationClick inside AdminHomeActivity.java

            case "Vendors":
                Intent vendorsIntent = new Intent(this, VendorsActivity.class);
                vendorsIntent.putExtra("store_id", storeId);
                vendorsIntent.putExtra("store_name", storeName);
                startActivity(vendorsIntent);
                break;

            case "Bills":
                Intent billsIntent = new Intent(this, Bills.class);
                billsIntent.putExtra("store_id", storeId);
                billsIntent.putExtra("store_name", storeName);
                startActivity(billsIntent);
                break;

            case "Reports":
                Intent reportsIntent = new Intent(this, ReportsActivity.class); // Unified reports activity
                reportsIntent.putExtra("store_id", storeId);
                reportsIntent.putExtra("store_name", storeName);
                startActivity(reportsIntent);
                break;
            case "Settings":
                Intent settingsIntent = new Intent(this, Settings.class);
                settingsIntent.putExtra("store_id", storeId);
                settingsIntent.putExtra("store_name", storeName);
                startActivity(settingsIntent);
                break;
            default:
                break;
        }
    }

    private void collapseAllDropdowns() {
        if (isInventoryExpanded && inventorySubmenu != null && inventoryExpandIcon != null) {
            collapseSubmenu(inventorySubmenu, inventoryExpandIcon);
            isInventoryExpanded = false;
        }
        if (isSalesExpanded && salesSubmenu != null && salesExpandIcon != null) {
            collapseSubmenu(salesSubmenu, salesExpandIcon);
            isSalesExpanded = false;
        }
        if (isPurchasesExpanded && purchasesSubmenu != null && purchasesExpandIcon != null) {
            collapseSubmenu(purchasesSubmenu, purchasesExpandIcon);
            isPurchasesExpanded = false;
        }
    }

    private void handleLogout() {
        // Clear user session
        if (userSession != null) {
            userSession.clearSession();
        }

        // Clear any stored preferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Close drawer
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        // Navigate to login activity
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void fetchDashboardData() {
        if (storeId == null || storeId.isEmpty()) {
            setDefaultDashboardValues();
            return;
        }
        
        showDashboardLoading(false);
        setDefaultDashboardValues();
    }
    
    // Add helper method for loading indicators
    private void showDashboardLoading(boolean isLoading) {
        // Implement loading indicators for dashboard cards
        float alpha = isLoading ? 0.5f : 1.0f;
        if (tvToBePacked != null) tvToBePacked.setAlpha(alpha);
        if (tvToBeShipped != null) tvToBeShipped.setAlpha(alpha);
        if (tvToBeDelivered != null) tvToBeDelivered.setAlpha(alpha);
        if (tvToBeInvoiced != null) tvToBeInvoiced.setAlpha(alpha);
        if (tvInHand != null) tvInHand.setAlpha(alpha);
        if (tvToBeReceived != null) tvToBeReceived.setAlpha(alpha);
    }
    private void setDefaultDashboardValues() {
        String defaultValue = "0";
        if (tvToBePacked != null) tvToBePacked.setText(defaultValue);
        if (tvToBeShipped != null) tvToBeShipped.setText(defaultValue);
        if (tvToBeDelivered != null) tvToBeDelivered.setText(defaultValue);
        if (tvToBeInvoiced != null) tvToBeInvoiced.setText(defaultValue);
        if (tvInHand != null) tvInHand.setText(defaultValue);
        if (tvToBeReceived != null) tvToBeReceived.setText(defaultValue);
    }
    
    // Helper method to safely get string values from Map
    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        if (data == null) return defaultValue;
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }


}
