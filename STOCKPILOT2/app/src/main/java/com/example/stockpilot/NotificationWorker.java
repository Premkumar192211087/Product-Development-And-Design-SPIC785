
package com.example.stockpilot;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

 

public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";
    
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting notification check");
        
        // Get user and store info from UserSession
        UserSession userSession = UserSession.getInstance(getApplicationContext());
        String userId = userSession.getUserId();
        String storeId = userSession.getStoreId();
        
        // Check if user is logged in
        if (!userSession.isLoggedIn() || userId.isEmpty() || storeId.isEmpty()) {
            Log.e(TAG, "User not logged in or missing user/store ID");
            return Result.failure();
        }
        
        Log.d(TAG, "Data layer removed");
        return Result.success();
    }
}
