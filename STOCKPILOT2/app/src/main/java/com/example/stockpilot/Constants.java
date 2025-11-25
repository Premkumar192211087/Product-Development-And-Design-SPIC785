package com.example.stockpilot;

public class Constants {
    // Shared Preferences Keys
    public static final String SHARED_PREFS = "StockPilotPrefs";
    public static final String STORE_ID = "store_id";
    public static final String STORE_NAME = "store_name";
    public static final String USER_ID = "user_id";
    public static final String USERNAME = "username";
    public static final String ROLE = "role";
    public static final String KEY_TOKEN = "token";
    
    // API URLs
    public static final String API_URL = "api_url";
    public static final String DEFAULT_API_URL = "https://api.stockpilot.co/api/";

    
    // Request Codes
    public static final int REQUEST_ADD_SHIPMENT = 1001;
    public static final int REQUEST_EDIT_SHIPMENT = 1002;
    public static final int REQUEST_CODE_ADD_ITEM = 100;
    public static final int REQUEST_CODE_EDIT_ITEM = 101;
    public static final int REQUEST_CODE_ADD_VENDOR = 102;
    public static final int REQUEST_CODE_EDIT_VENDOR = 103;
    
    // Status Values
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SHIPPED = "shipped";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_ALL = "All";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_INACTIVE = "Inactive";
    
    // Sort Options
    public static final String SORT_BY_NAME = "name";
    public static final String SORT_BY_PRICE = "price";
    public static final String SORT_BY_QUANTITY = "quantity";
    public static final String SORT_ASC = "asc";
    public static final String SORT_DESC = "desc";
    
    // Order Types
    public static final String ORDER_TYPE_SALES = "sales_order";
    public static final String ORDER_TYPE_PURCHASE_RETURN = "purchase_return";
    public static final String ORDER_TYPE_OTHER = "other";
    
    // Error Messages
    public static final String ERROR_NETWORK = "Network error. Please check your connection.";
    public static final String ERROR_SERVER = "Server error. Please try again later.";
    public static final String ERROR_TIMEOUT = "Request timed out. Please try again.";
    public static final String ERROR_UNKNOWN = "An unknown error occurred.";
}
