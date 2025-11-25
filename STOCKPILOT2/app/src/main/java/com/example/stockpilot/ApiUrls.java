package com.example.stockpilot;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiUrls {
    private static volatile ApiService apiService;
    private static volatile String baseUrl = Constants.DEFAULT_API_URL;

    public static void setBaseUrl(String url) {
        if (url != null && !url.isEmpty()) {
            baseUrl = url.endsWith("/") ? url : url + "/";
            apiService = null;
        }
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            synchronized (ApiUrls.class) {
                if (apiService == null) {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new AuthInterceptor())
                            .build();
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .build();
                    apiService = retrofit.create(ApiService.class);
                }
            }
        }
        return apiService;
    }
}