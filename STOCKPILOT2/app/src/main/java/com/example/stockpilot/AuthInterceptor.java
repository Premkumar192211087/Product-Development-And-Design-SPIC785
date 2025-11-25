package com.example.stockpilot;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    public interface TokenProvider {
        String getToken();
    }

    private static volatile TokenProvider tokenProvider;

    public static void setTokenProvider(TokenProvider provider) {
        tokenProvider = provider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = tokenProvider != null ? tokenProvider.getToken() : null;
        if (token != null && !token.isEmpty()) {
            Request newRequest = original.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }
        return chain.proceed(original);
    }
}