package com.example.stockpilot;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stockpilot.Vendor;
 
import com.example.stockpilot.ApiResponse;
import com.example.stockpilot.Constants;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorRepository {
    

    public VendorRepository() {
        
    }

    public LiveData<ApiResponse<List<Vendor>>> getVendors(String storeId, String searchQuery, String sortBy, String sortOrder) {
        MutableLiveData<ApiResponse<List<Vendor>>> vendorsLiveData = new MutableLiveData<>();
        vendorsLiveData.setValue(ApiResponse.loading());

        ApiUrls.getApiService().getVendors(storeId).enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Map<String, Object>>>> call, Response<ApiResponse<List<Map<String, Object>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Map<String, Object>> raw = response.body().getData();
                    List<Vendor> vendors = new java.util.ArrayList<>();
                    if (raw != null) {
                        for (Map<String, Object> m : raw) {
                            String id = asString(m.get("id"));
                            String name = asString(m.get("vendor_name"));
                            String email = asString(m.get("email"));
                            String phone = asString(m.get("phone"));
                            String address = asString(m.get("address"));
                            String city = asString(m.get("city"));
                            String state = asString(m.get("state"));
                            String zip = asString(m.get("zip_code"));
                            String country = asString(m.get("country"));
                            String status = asString(m.get("status"));
                            Vendor v = new Vendor(id, name, "", email, phone, address, city, state, zip, country);
                            v.setStatus(status);
                            vendors.add(v);
                        }
                    }
                    vendorsLiveData.setValue(ApiResponse.success(vendors));
                } else {
                    vendorsLiveData.setValue(ApiResponse.error(Constants.ERROR_SERVER));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Map<String, Object>>>> call, Throwable t) {
                vendorsLiveData.setValue(ApiResponse.error(Constants.ERROR_NETWORK));
            }
        });

        return vendorsLiveData;
    }

    public LiveData<ApiResponse<Vendor>> addVendor(String storeId, Vendor vendor) {
        MutableLiveData<ApiResponse<Vendor>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(ApiResponse.loading());

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("storeId", storeId);
            jsonObject.put("name", vendor.getName());
            jsonObject.put("phone", vendor.getPhone());
            jsonObject.put("email", vendor.getEmail());
            jsonObject.put("address", vendor.getAddress());
            
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"), jsonObject.toString());
            Map<String, String> body = new HashMap<>();
            body.put("store_id", storeId);
            body.put("vendor_name", vendor.getName());
            body.put("email", vendor.getEmail());
            body.put("phone", vendor.getPhone());
            body.put("address", vendor.getAddress());
            ApiUrls.getApiService().addVendor(body).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Map<String, Object> m = response.body().getData();
                        Vendor v = new Vendor(asString(m.get("id")), asString(m.get("vendor_name")), "", asString(m.get("email")), asString(m.get("phone")), asString(m.get("address")), asString(m.get("city")), asString(m.get("state")), asString(m.get("zip_code")), asString(m.get("country")));
                        v.setStatus(asString(m.get("status")));
                        resultLiveData.setValue(ApiResponse.success(v));
                    } else {
                        resultLiveData.setValue(ApiResponse.error(Constants.ERROR_SERVER));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                    resultLiveData.setValue(ApiResponse.error(Constants.ERROR_NETWORK));
                }
            });
        } catch (Exception e) {
            resultLiveData.setValue(ApiResponse.error(e.getMessage()));
        }

        return resultLiveData;
    }

    public LiveData<ApiResponse<Vendor>> updateVendor(String vendorId, Vendor vendor) {
        MutableLiveData<ApiResponse<Vendor>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(ApiResponse.loading());

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", vendor.getName());
            jsonObject.put("phone", vendor.getPhone());
            jsonObject.put("email", vendor.getEmail());
            jsonObject.put("address", vendor.getAddress());
            
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"), jsonObject.toString());
            ApiUrls.getApiService().updateVendor(vendorId, requestBody).enqueue(new Callback<Vendor>() {
                @Override
                public void onResponse(Call<Vendor> call, Response<Vendor> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        resultLiveData.setValue(ApiResponse.success(response.body()));
                    } else {
                        resultLiveData.setValue(ApiResponse.error(Constants.ERROR_SERVER));
                    }
                }

                @Override
                public void onFailure(Call<Vendor> call, Throwable t) {
                    resultLiveData.setValue(ApiResponse.error(Constants.ERROR_NETWORK));
                }
            });
        } catch (Exception e) {
            resultLiveData.setValue(ApiResponse.error(e.getMessage()));
        }

        return resultLiveData;
    }

    public LiveData<ApiResponse<Boolean>> deleteVendor(String vendorId) {
        MutableLiveData<ApiResponse<Boolean>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(ApiResponse.loading());

        ApiUrls.getApiService().deleteVendor(vendorId).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    resultLiveData.setValue(ApiResponse.success(true));
                } else {
                    resultLiveData.setValue(ApiResponse.error(Constants.ERROR_SERVER));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                resultLiveData.setValue(ApiResponse.error(Constants.ERROR_NETWORK));
            }
        });

        return resultLiveData;
    }
        public void fetchVendors(String storeId) {
            // Implementation to fetch vendors based on the storeId
            // Example: Make a network request or query a database
            System.out.println("Fetching vendors for store ID: " + storeId);
    }

    private int parseInt(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return 0; }
    }

    private String asString(Object v) { return v == null ? "" : String.valueOf(v); }
}