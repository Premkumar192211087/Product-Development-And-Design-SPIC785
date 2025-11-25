package com.example.stockpilot;

import android.util.Log;

/**
 * Utility class for error logging in the StockPilot application.
 * This class provides methods for logging errors and exceptions to Logcat
 * with consistent formatting and behavior across the application.
 */
public class ErrorLogger {
    private static final String DEFAULT_TAG = "StockPilot";
    
    /**
     * Log an info message with a custom tag
     * 
     * @param tag The tag to identify the source of the log
     * @param message The info message to log
     */
    public static void info(String tag, String message) {
        LogUtils.info(tag, message);
    }
    
    /**
     * Log an info message with the default tag
     * 
     * @param message The info message to log
     */
    public static void info(String message) {
        LogUtils.info(DEFAULT_TAG, message);
    }
    
    /**
     * Log an error message with a custom tag
     * 
     * @param tag The tag to identify the source of the log
     * @param message The error message to log
     */
    public static void logError(String tag, String message) {
        LogUtils.error(tag, message);
    }
    
    /**
     * Log an error message with the default tag
     * 
     * @param message The error message to log
     */
    public static void logError(String message) {
        LogUtils.error(DEFAULT_TAG, message);
    }
    
    /**
     * Log an error message and exception with a custom tag
     * 
     * @param tag The tag to identify the source of the log
     * @param message The error message to log
     * @param throwable The exception to log
     */
    public static void logError(String tag, String message, Throwable throwable) {
        LogUtils.error(tag, message, throwable);
    }
    
    /**
     * Log an error message and exception with the default tag
     * 
     * @param message The error message to log
     * @param throwable The exception to log
     */
    public static void logError(String message, Throwable throwable) {
        LogUtils.error(DEFAULT_TAG, message, throwable);
    }
    
    /**
     * Log an exception with a custom tag
     * 
     * @param tag The tag to identify the source of the log
     * @param throwable The exception to log
     */
    public static void logException(String tag, Throwable throwable) {
        LogUtils.exception(tag, throwable);
    }
    
    /**
     * Log an exception with the default tag
     * 
     * @param throwable The exception to log
     */
    public static void logException(Throwable throwable) {
        LogUtils.exception(DEFAULT_TAG, throwable);
    }
    
    /**
     * Log a network error with a custom tag
     * 
     * @param tag The tag to identify the source of the log
     * @param message The error message to log
     * @param throwable The exception to log
     * @param url The URL that was being accessed
     */
    public static void logNetworkError(String tag, String message, Throwable throwable, String url) {
        LogUtils.networkError(tag, message, throwable, url);
    }
    
    /**
     * Log a network error with the default tag
     * 
     * @param message The error message to log
     * @param throwable The exception to log
     * @param url The URL that was being accessed
     */
    public static void logNetworkError(String message, Throwable throwable, String url) {
        LogUtils.networkError(DEFAULT_TAG, message, throwable, url);
    }
    
    /**
     * Log an API error with a custom tag
     * 
     * @param tag The tag to identify the source of the log
     * @param responseCode The HTTP response code
     * @param responseBody The response body
     */
    public static void logApiError(String tag, int responseCode, String responseBody) {
        LogUtils.apiError(tag, responseCode, responseBody);
    }
    
    /**
     * Log an API error with the default tag
     * 
     * @param responseCode The HTTP response code
     * @param responseBody The response body
     */
    public static void logApiError(int responseCode, String responseBody) {
        LogUtils.apiError(DEFAULT_TAG, responseCode, responseBody);
    }
    
    /**
     * Log an API error with a custom tag and message
     * 
     * @param tag The tag to identify the source of the log
     * @param message The error message to log
     * @param errorDetails Additional error details
     * @param responseCode The HTTP response code
     */
    public static void logApiError(String tag, String message, String errorDetails, int responseCode) {
        LogUtils.error(tag, message + " - Details: " + errorDetails + " - Code: " + responseCode);
    }
    public static void logJsonError(String tag, String message, Throwable throwable, String responseBody) {
        String logTag = tag != null ? tag : DEFAULT_TAG;
        android.util.Log.e(logTag, message);
        if (throwable != null) {
            android.util.Log.e(logTag, "Exception: ", throwable);
        }
        if (responseBody != null) {
            android.util.Log.e(logTag, "Response Body: " + responseBody);
        }
    }

}
