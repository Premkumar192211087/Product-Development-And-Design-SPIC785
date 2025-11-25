package com.example.stockpilot;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface ApiService {
    @GET("stores/{storeId}/products")
    Call<List<Map<String, Object>>> getProductSelection(@Path("storeId") String storeId);

    @GET("stores/{storeId}/products")
    Call<ApiResponse<List<Map<String, Object>>>> getProducts(
            @Path("storeId") String storeId,
            @Query("search") String search,
            @Query("category") String category,
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("sort_by") String sortBy,
            @Query("sort_dir") String sortDir
    );

    @POST("customers/fetch")
    Call<org.json.JSONObject> fetchCustomers(@Query("action") String action, @Query("store_id") int storeId);

    @POST("sales")
    Call<org.json.JSONObject> addSale(@Body RequestBody body);

    @POST("shipments")
    Call<Map<String, Object>> addShipment(@Body RequestBody body);

    @POST("vendors/details")
    Call<Map<String, Object>> getVendorDetails(@Body Map<String, String> params);

    @PUT("vendors/{id}")
    Call<Vendor> updateVendor(@Path("id") String id, @Body RequestBody body);

    @POST("reports/purchase")
    Call<Map<String, Object>> getPurchaseReports(@Query("store_id") String storeId, @Query("from") String from, @Query("to") String to);

    @POST("profile/get")
    Call<Map<String, Object>> getProfile(@Body Map<String, String> params);

    @PUT("profile/update")
    Call<Map<String, Object>> updateProfile(@Body RequestBody body);

    @POST("staff")
    Call<ApiResponse> addStaff(@Body RequestBody body);

    @POST("auth/change-password")
    Call<Map<String, Object>> changePassword(@Body Map<String, String> params);

    @GET("stores/{storeId}/bills")
    Call<ApiResponse> getBills(@Path("storeId") String storeId);

    @GET("bills/{id}")
    Call<Map<String, Object>> getBillDetails(@Path("id") String billId);

    @GET("stores/{storeId}/notifications")
    Call<ApiResponse<Map<String, Object>>> getNotifications(@Path("storeId") String storeId);

    @PUT("notifications/delete")
    Call<ApiResponse> deleteNotification(@Body Map<String, String> requestBody);

    @PUT("notifications/mark-read")
    Call<ApiResponse> markNotificationAsRead(@Body Map<String, String> requestBody);

    @GET("purchase-orders/{id}")
    Call<Map<String, Object>> getPurchaseOrderDetails(@Path("id") String id);

    @PUT("purchase-orders/{id}")
    Call<Map<String, Object>> updatePurchaseOrder(@Path("id") String id, @Body Map<String, Object> body);

    @GET("reports/sales")
    Call<Map<String, Object>> getSalesReports(@QueryMap Map<String, String> params);

    @GET("reports/inventory")
    Call<Map<String, Object>> getInventoryReports(@QueryMap Map<String, String> params);

    @Multipart
    @POST("products")
    Call<Item> createItem(@PartMap Map<String, RequestBody> params, @Part MultipartBody.Part image);

    @POST("inventory/movements")
    Call<ApiResponse<Map<String, Object>>> addInventoryMovement(@Body Map<String, String> body);

    @GET("stores/{storeId}/categories")
    Call<ApiResponse<List<Map<String, Object>>>> getCategories(@Path("storeId") String storeId);

    @POST("categories")
    Call<ApiResponse<Map<String, Object>>> addCategory(@Body Map<String, String> body);

    @PUT("categories/{id}")
    Call<ApiResponse<Map<String, Object>>> updateCategory(@Path("id") String id, @Body Map<String, String> body);

    @DELETE("categories/{id}")
    Call<ApiResponse<Map<String, Object>>> deleteCategory(@Path("id") String id);

    @GET("stores/{storeId}/vendors")
    Call<ApiResponse<List<Map<String, Object>>>> getVendors(@Path("storeId") String storeId);

    @GET("vendors/{id}")
    Call<ApiResponse<Map<String, Object>>> getVendor(@Path("id") String id);

    @POST("vendors")
    Call<ApiResponse<Map<String, Object>>> addVendor(@Body Map<String, String> body);

    @DELETE("vendors/{id}")
    Call<ApiResponse<Map<String, Object>>> deleteVendor(@Path("id") String id);

    @GET("stores/{storeId}/customers")
    Call<ApiResponse<List<Map<String, Object>>>> getCustomers(
            @Path("storeId") String storeId,
            @Query("search") String search,
            @Query("status") String status
    );

    @POST("customers")
    Call<ApiResponse<Map<String, Object>>> addCustomer(@Body Map<String, String> body);

    @PUT("customers/{id}")
    Call<ApiResponse<Map<String, Object>>> updateCustomer(@Path("id") String id, @Body Map<String, String> body);

    @DELETE("customers/{id}")
    Call<ApiResponse<Map<String, Object>>> deleteCustomer(@Path("id") String id);

    @PUT("customers/{id}/status")
    Call<ApiResponse<Map<String, Object>>> toggleCustomerStatus(@Path("id") String id, @Body Map<String, String> body);

    @GET("stores/{storeId}/purchase-orders")
    Call<ApiResponse<List<Map<String, Object>>>> getPurchaseOrders(
            @Path("storeId") String storeId,
            @Query("status") String status,
            @Query("from") String from,
            @Query("to") String to
    );

    @POST("purchase-orders")
    Call<ApiResponse<Map<String, Object>>> addPurchaseOrder(@Body Map<String, Object> body);

    @DELETE("purchase-orders/{id}")
    Call<ApiResponse<Map<String, Object>>> deletePurchaseOrder(@Path("id") String id);

    @GET("stores/{storeId}/bills")
    Call<ApiResponse<List<Map<String, Object>>>> listBills(@Path("storeId") String storeId);

    @POST("bills")
    Call<ApiResponse<Map<String, Object>>> addBill(@Body Map<String, Object> body);

    @GET("stores/{storeId}/sales")
    Call<ApiResponse<List<Map<String, Object>>>> getSales(
            @Path("storeId") String storeId,
            @Query("from") String from,
            @Query("to") String to,
            @Query("method") String method,
            @Query("status") String status
    );

    @GET("stores/{storeId}/shipments")
    Call<ApiResponse<List<Map<String, Object>>>> getShipments(
            @Path("storeId") String storeId,
            @Query("status") String status,
            @Query("from") String from,
            @Query("to") String to
    );

    @GET("stores/{storeId}/inventory/movements")
    Call<ApiResponse<List<Map<String, Object>>>> getInventoryMovements(
            @Path("storeId") String storeId,
            @Query("type") String type,
            @Query("from") String from,
            @Query("to") String to
    );

    @PUT("notifications/{id}")
    Call<ApiResponse<Map<String, Object>>> updateNotification(@Path("id") String id, @Body Map<String, String> body);

    @GET("stores/{storeId}/reports/financial")
    Call<Map<String, Object>> getFinancialReports(@Path("storeId") String storeId, @Query("range") String range, @Query("from") String from, @Query("to") String to);
}