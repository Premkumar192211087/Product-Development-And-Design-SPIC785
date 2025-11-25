package com.example.stockpilot;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Apply Material3 dynamic color if available (Android 12+)
        DynamicColors.applyToActivitiesIfAvailable(this);
        String apiBase = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE)
                .getString(Constants.API_URL, Constants.DEFAULT_API_URL);
        ApiUrls.setBaseUrl(apiBase);
        AuthInterceptor.setTokenProvider(() -> UserSession.getInstance(getApplicationContext()).getToken());
    }
}