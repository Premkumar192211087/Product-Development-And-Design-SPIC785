
package com.example.stockpilot;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stockpilot.R;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set window flags for immersive splash screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set layout
        setContentView(R.layout.activity_main);

        // Setup notification worker
        setupNotificationWorker();

        // Redirect to Login activity after 3 seconds
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class); // Use 'loginpage.class'
            startActivity(intent);
            finish(); // Close MainActivity
        }, 3000); // 3 seconds delay
    }
    
    // Add this method to your MainActivity or Application class
    private void setupNotificationWorker() {
        // Define constraints
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
        
        // Create a periodic work request that runs every 15 minutes
        PeriodicWorkRequest notificationWorkRequest =
            new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        
        // Enqueue the work request
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "notification_check",
                ExistingPeriodicWorkPolicy.REPLACE,
                notificationWorkRequest
            );
        
        Log.d("MainActivity", "Notification worker scheduled");
    }
}
