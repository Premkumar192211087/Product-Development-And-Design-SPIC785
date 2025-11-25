package com.example.stockpilot;

import android.util.Log;

/**
 * Utility class to help update all Java files in the project to use the ErrorLogger class.
 * This class is meant to be used as a reference for manual updates or by a script that
 * processes all Java files in the project.
 */
public class ErrorLoggerUpdater {

    /**
     * Instructions for updating Java files to use ErrorLogger:
     * 
     * 1. Add import statement:
     *    import com.example.stockpilot.utils.ErrorLogger;
     * 
     * 2. Replace direct Log.e calls with ErrorLogger.logError:
     *    - Log.e(TAG, "message") → ErrorLogger.logError(TAG, "message")
     *    - Log.e(TAG, "message", exception) → ErrorLogger.logError(TAG, "message", exception)
     * 
     * 3. Replace network error logging:
     *    - Log.e(TAG, "Network error: " + e.getMessage(), e) → 
     *      ErrorLogger.logNetworkError(TAG, "Network error", e, url)
     * 
     * 4. Replace API error logging:
     *    - Log.e(TAG, "API error: " + response.code()) → 
     *      ErrorLogger.logApiError(TAG, response.code(), responseBody)
     * 
     * 5. Add TAG constant if missing:
     *    private static final String TAG = "ClassName";
     */
    
    // Example conversion methods (for reference)
    
    /**
     * Example of converting a simple error log
     */
    public static void exampleSimpleErrorLog(String tag, String message) {
        // Before
        Log.e(tag, message);
        
        // After
        ErrorLogger.logError(tag, message);
    }
    
    /**
     * Example of converting an error log with exception
     */
    public static void exampleErrorLogWithException(String tag, String message, Exception e) {
        // Before
        Log.e(tag, message, e);
        
        // After
        ErrorLogger.logError(tag, message, e);
    }
    
    /**
     * Example of converting a network error log
     */
    public static void exampleNetworkErrorLog(String tag, String message, Exception e, String url) {
        // Before
        Log.e(tag, "Network error: " + message, e);
        
        // After
        ErrorLogger.logNetworkError(tag, message, e, url);
    }
    
    /**
     * Example of converting an API error log
     */
    public static void exampleApiErrorLog(String tag, int responseCode, String responseBody) {
        // Before
        Log.e(tag, "API error - Code: " + responseCode);
        
        // After
        ErrorLogger.logApiError(tag, responseCode, responseBody);
    }
}
