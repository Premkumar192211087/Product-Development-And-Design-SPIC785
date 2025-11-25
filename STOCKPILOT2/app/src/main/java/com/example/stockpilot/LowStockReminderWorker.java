package com.example.stockpilot;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class LowStockReminderWorker extends Worker {

    public LowStockReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String productName = getInputData().getString("productName");

        if (productName != null) {
            // Send reminder notification
            showReminderNotification(productName);
        }

        return Result.success();
    }

    private void showReminderNotification(String productName) {
        Context context = getApplicationContext();
        Toast.makeText(context, "⏳ Reminder: " + productName + " is still low on stock! Please restock.", Toast.LENGTH_LONG).show();
        Log.d("LowStockReminder", "Reminder sent for: " + productName);
    }
}

