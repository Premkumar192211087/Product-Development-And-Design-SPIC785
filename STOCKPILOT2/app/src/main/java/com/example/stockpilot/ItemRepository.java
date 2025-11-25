package com.example.stockpilot;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stockpilot.Item;
 
import com.example.stockpilot.ApiResponse;
import com.example.stockpilot.Constants;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.json.JSONObject;

public class ItemRepository {
    private static final String TAG = "ItemRepository";
    
    private static ItemRepository instance;

    private ItemRepository() {
        
    }

    public static synchronized ItemRepository getInstance() {
        if (instance == null) {
            instance = new ItemRepository();
        }
        return instance;
    }

    public LiveData<ApiResponse<List<Item>>> getItems(String storeId, String status, String sortBy, String sortOrder) {
        MutableLiveData<ApiResponse<List<Item>>> itemsLiveData = new MutableLiveData<>();
        itemsLiveData.postValue(ApiResponse.loading());
        ApiUrls.getApiService().getProducts(
                storeId,
                null,
                null,
                null,
                null,
                sortBy,
                sortOrder
        ).enqueue(new retrofit2.Callback<ApiResponse<List<java.util.Map<String, Object>>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<List<java.util.Map<String, Object>>>> call, retrofit2.Response<ApiResponse<List<java.util.Map<String, Object>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.List<java.util.Map<String, Object>> raw = response.body().getData();
                    java.util.List<Item> items = new java.util.ArrayList<>();
                    if (raw != null) {
                        for (java.util.Map<String, Object> m : raw) {
                            int id = parseInt(m.get("id"));
                            String name = asString(m.get("product_name"));
                            String sku = asString(m.get("sku"));
                            double qty = parseDouble(m.get("stock_quantity"));
                            double price = parseDouble(m.get("selling_price"));
                            Item it = new Item(id, name, sku, qty, price, null, "");
                            items.add(it);
                        }
                    }
                    itemsLiveData.postValue(ApiResponse.success(items));
                } else {
                    itemsLiveData.postValue(ApiResponse.error(Constants.ERROR_SERVER));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<java.util.Map<String, Object>>>> call, Throwable t) {
                itemsLiveData.postValue(ApiResponse.error(Constants.ERROR_NETWORK));
            }
        });
        return itemsLiveData;
    }

    private RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    public LiveData<ApiResponse<Item>> addItem(Item item, MultipartBody.Part imagePart) {
        MutableLiveData<ApiResponse<Item>> resultLiveData = new MutableLiveData<>();
        java.util.Map<String, RequestBody> params = new java.util.HashMap<>();
        params.put("product_name", createPartFromString(item.getProductName()));
        params.put("sku", createPartFromString(item.getSku()));
        params.put("category", createPartFromString(item.getCategory()));
        params.put("price", createPartFromString(String.valueOf(item.getPrice())));
        params.put("quantity", createPartFromString(String.valueOf(item.getQuantity())));
        params.put("barcode", createPartFromString(""));

        ApiUrls.getApiService().createItem(params, imagePart).enqueue(new retrofit2.Callback<Item>() {
            @Override
            public void onResponse(retrofit2.Call<Item> call, retrofit2.Response<Item> response) {
                if (response.isSuccessful() && response.body() != null) {
                    resultLiveData.postValue(ApiResponse.success(response.body()));
                } else {
                    resultLiveData.postValue(ApiResponse.error(Constants.ERROR_SERVER));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Item> call, Throwable t) {
                resultLiveData.postValue(ApiResponse.error(Constants.ERROR_NETWORK));
            }
        });

        return resultLiveData;
    }

    public LiveData<ApiResponse<Item>> updateItem(String itemId, Item item) {
        MutableLiveData<ApiResponse<Item>> resultLiveData = new MutableLiveData<>();
        // Placeholder; server provides PUT /products/{id}
        resultLiveData.postValue(ApiResponse.error("Not implemented"));

        return resultLiveData;
    }

    public LiveData<ApiResponse<Boolean>> deleteItem(String itemId) {
        MutableLiveData<ApiResponse<Boolean>> resultLiveData = new MutableLiveData<>();
        // Placeholder; server provides DELETE /products/{id}
        resultLiveData.postValue(ApiResponse.error("Not implemented"));

        return resultLiveData;
    }

    private int parseInt(Object v) { if (v instanceof Number) return ((Number)v).intValue(); try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return 0; } }
    private double parseDouble(Object v) { if (v instanceof Number) return ((Number)v).doubleValue(); try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return 0; } }
    private String asString(Object v) { return v == null ? "" : String.valueOf(v); }
}
