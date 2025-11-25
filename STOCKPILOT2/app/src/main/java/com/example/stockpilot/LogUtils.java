package com.example.stockpilot;

import android.util.Log;

/**
 * Utility class for centralized logging in the StockPilot application.
 * This class provides methods for logging different types of messages (debug, info, warning, error)
 * with consistent formatting and behavior across the application.
 */
public class LogUtils {
    private static final boolean LOGGING_ENABLED = true;
    
    /**
     * Log a debug message
     * 
     * @param tag The tag to identify the source of the log
     * @param message The message to log
     */
    public static void debug(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.d(tag, message);
        }
    }
    
    /**
     * Log an info message
     * 
     * @param tag The tag to identify the source of the log
     * @param message The message to log
     */
    public static void info(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.i(tag, message);
        }
    }
    
    /**
     * Log a warning message
     * 
     * @param tag The tag to identify the source of the log
     * @param message The message to log
     */
    public static void warning(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.w(tag, message);
        }
    }
    
    /**
     * Log an error message
     * 
     * @param tag The tag to identify the source of the log
     * @param message The message to log
     */
    public static void error(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.e(tag, message);
        }
    }
    
    /**
     * Log an error message with an exception
     * 
     * @param tag The tag to identify the source of the log
     * @param message The message to log
     * @param throwable The exception to log
     */
    public static void error(String tag, String message, Throwable throwable) {
        if (LOGGING_ENABLED) {
            Log.e(tag, message, throwable);
        }
    }
    
    /**
     * Log an exception with a default error message
     * 
     * @param tag The tag to identify the source of the log
     * @param throwable The exception to log
     */
    public static void exception(String tag, Throwable throwable) {
        if (LOGGING_ENABLED) {
            Log.e(tag, "Exception: " + throwable.getMessage(), throwable);
        }
    }
    
    /**
     * Log a network error with detailed information
     * 
     * @param tag The tag to identify the source of the log
     * @param message The message describing the context of the error
     * @param throwable The exception that occurred
     * @param url The URL that was being accessed (optional)
     */
    public static void networkError(String tag, String message, Throwable throwable, String url) {
        if (LOGGING_ENABLED) {
            StringBuilder sb = new StringBuilder(message);
            if (url != null && !url.isEmpty()) {
                sb.append(" - URL: ").append(url);
            }
            Log.e(tag, sb.toString(), throwable);
        }
    }
    
    /**
     * Log an API error response
     * 
     * @param tag The tag to identify the source of the log
     * @param responseCode The HTTP response code
     * @param responseBody The response body (if available)
     */
    public static void apiError(String tag, int responseCode, String responseBody) {
        if (LOGGING_ENABLED) {
            Log.e(tag, "API Error - Code: " + responseCode + ", Response: " + responseBody);
        }
    }
}
