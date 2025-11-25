# StockPilot Error Logging System

## Overview

This document explains the new error logging system implemented in the StockPilot application. The system provides a centralized way to log errors to Logcat with consistent formatting and behavior across the application.

## Components

1. **LogUtils.java** - Core logging utility that provides methods for logging different types of messages (debug, info, warning, error) with consistent formatting.

2. **ErrorLogger.java** - Higher-level utility that provides simplified methods for logging errors and exceptions to Logcat.

3. **ErrorLoggerUpdater.java** - Reference class with examples of how to update existing code to use the new error logging system.

4. **update_error_logging.bat** - Batch script that adds the necessary import statements to all Java files in the project.

## How to Use

### Step 1: Run the Batch Script

Double-click on `update_error_logging.bat` in the project root directory. This will add the necessary import statements to all Java files in the project.

### Step 2: Update Error Logging Code

For each Java file that contains error handling code, update the code to use the new ErrorLogger class. Here are the common patterns to replace:

#### Simple Error Logging

```java
// Before
Log.e(TAG, "Error message");

// After
ErrorLogger.logError(TAG, "Error message");
```

#### Error Logging with Exception

```java
// Before
Log.e(TAG, "Error message", exception);

// After
ErrorLogger.logError(TAG, "Error message", exception);
```

#### Network Error Logging

```java
// Before
Log.e(TAG, "Network error: " + e.getMessage(), e);

// After
ErrorLogger.logNetworkError(TAG, "Network error", e, url);
```

#### API Error Logging

```java
// Before
Log.e(TAG, "API error - Code: " + response.code());

// After
ErrorLogger.logApiError(TAG, response.code(), responseBody);
// Or with more context
ErrorLogger.logApiError(TAG, "API request failed", errorMessage, responseCode);
```

#### Info Logging

```java
// Before
Log.d(TAG, "Informational message");
// or
Log.i(TAG, "Informational message");

// After
ErrorLogger.info(TAG, "Informational message");
```

### Step 3: Add TAG Constant if Missing

If a Java file doesn't have a TAG constant, add one at the class level:

```java
private static final String TAG = "ClassName";
```

Replace "ClassName" with the actual name of the class.

## Benefits

1. **Consistency** - All error logs have the same format and behavior across the application.

2. **Centralized Control** - You can enable/disable logging for the entire application by changing a single flag in LogUtils.java.

3. **Enhanced Information** - The new system automatically includes additional information such as URLs for network errors and response bodies for API errors.

4. **Easier Debugging** - With consistent tags and formats, it's easier to filter and search logs in Logcat.

## Implementation Examples

The error logging system has been implemented in several key activities:

1. `LoginActivity.java`
2. `SalesReportsActivity.java`
3. `InventoryReportsActivity.java`
4. `AddAdjustmentActivity.java`
5. `AddCustomerActivity.java`

See these files for real-world examples of how to use the error logging system.

Additionally, see `ErrorLoggerUpdater.java` for more examples of how to update existing code.

## Support

If you have any questions or issues with the new error logging system, please contact the development team.