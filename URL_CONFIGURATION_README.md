# URL Configuration Guide for STOCKPILOT2

## Overview

This guide explains how to use the centralized URL configuration system in the STOCKPILOT2 application. All HTTP URLs have been moved to a single file (`ApiUrls.java`) to make it easier to change the IP address for all endpoints at once.

## How to Change the IP Address

To change the IP address for all API endpoints, simply modify the `IP_ADDRESS` constant in the `ApiUrls` class:

```java
// File: app/src/main/java/com/example/stockpilot/ApiUrls.java

// Change this value to update all endpoints
private static final String IP_ADDRESS = "10.27.236.206";
```

## How to Use ApiUrls in Your Code

Instead of hardcoding URLs in your activities or fragments, use the constants defined in the `ApiUrls` class.

### Example: Before

```java
private static final String LOGIN_URL = "http://10.27.236.206/API/login_post.php";

// Making a request
Request request = new Request.Builder()
    .url(LOGIN_URL)
    .post(requestBody)
    .build();
```

### Example: After

```java
// Import the ApiUrls class
import com.example.stockpilot.ApiUrls;

// Using the constant
Request request = new Request.Builder()
    .url(ApiUrls.LOGIN_POST)
    .post(requestBody)
    .build();
```

## Available URL Constants

The `ApiUrls` class contains constants for all API endpoints used in the application. Here are some examples:

- `ApiUrls.LOGIN_POST` - Login endpoint
- `ApiUrls.SIGN_UP` - Registration endpoint
- `ApiUrls.ITEMS` - Items endpoint
- `ApiUrls.SHIPMENTS` - Shipments endpoint

For a complete list, refer to the `ApiUrls.java` file.

## Helper Methods

The `ApiUrls` class also provides helper methods for common URL operations:

```java
// Get the base URL
String baseUrl = ApiUrls.getBaseUrl();

// Get the current IP address
String ipAddress = ApiUrls.getIpAddress();

// Get a URL with query parameters
String shipmentItemsUrl = ApiUrls.getShipmentItemsUrl(shipmentId);
String vendorsUrl = ApiUrls.getVendorsUrl(storeId);
```

## Best Practices

1. Always use the constants from `ApiUrls` instead of hardcoding URLs
2. If you need a new endpoint, add it to the `ApiUrls` class
3. For endpoints with dynamic parameters, create helper methods in the `ApiUrls` class
4. Use `ApiUrls.getBaseUrl()` if you need to construct a custom URL

## Troubleshooting

If you encounter connection issues after changing the IP address:

1. Verify that the new IP address is correct and accessible
2. Check that you're not using any hardcoded URLs in your code
3. Ensure that the device has network connectivity
4. Verify that the server is running and accessible at the specified IP address