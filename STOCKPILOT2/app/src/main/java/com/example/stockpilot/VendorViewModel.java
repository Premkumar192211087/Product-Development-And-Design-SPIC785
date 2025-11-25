package com.example.stockpilot;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

public class VendorViewModel extends BaseViewModel {
    private final VendorRepository vendorRepository;
    
    private final MutableLiveData<String> storeId = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MutableLiveData<String> sortBy = new MutableLiveData<>();
    private final MutableLiveData<String> sortOrder = new MutableLiveData<>();
    
    private final LiveData<ApiResponse<List<Vendor>>> vendorsResponse;
    
    public VendorViewModel() {
        vendorRepository = new VendorRepository();
        
        // Transform inputs into API response
        vendorsResponse = Transformations.switchMap(storeId, id -> 
            Transformations.switchMap(searchQuery, query -> 
                Transformations.switchMap(sortBy, sort -> 
                    Transformations.switchMap(sortOrder, order -> 
                        vendorRepository.getVendors(id, query, sort, order)))));
        
        // Observe API response to update loading and error states
        observeApiResponse(vendorsResponse);
    }
    
    // Getters for LiveData
    public LiveData<ApiResponse<List<Vendor>>> getVendorsResponse() {
        return vendorsResponse;
    }
    
    public LiveData<List<Vendor>> getVendors() {
        return Transformations.map(vendorsResponse, ApiResponse::getData);
    }
    
    // Setters for input parameters
    public void setStoreId(String id) {
        if (id != null && !id.equals(storeId.getValue())) {
            storeId.setValue(id);
        }
    }
    
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }
    
    public void setSortBy(String sort) {
        sortBy.setValue(sort);
    }
    
    public void setSortOrder(String order) {
        sortOrder.setValue(order);
    }
    
    // CRUD operations
    public LiveData<ApiResponse<Vendor>> addVendor(String storeId, Vendor vendor) {
        setLoading(true);
        LiveData<ApiResponse<Vendor>> response = vendorRepository.addVendor(storeId, vendor);
        observeApiResponse(response);
        return response;
    }
    
    public LiveData<ApiResponse<Vendor>> updateVendor(String vendorId, Vendor vendor) {
        setLoading(true);
        LiveData<ApiResponse<Vendor>> response = vendorRepository.updateVendor(vendorId, vendor);
        observeApiResponse(response);
        return response;
    }
    
    public LiveData<ApiResponse<Boolean>> deleteVendor(String vendorId) {
        setLoading(true);
        LiveData<ApiResponse<Boolean>> response = vendorRepository.deleteVendor(vendorId);
        observeApiResponse(response);
        return response;
    }
    
    // Refresh data
    public void refreshVendors() {
        String currentStoreId = storeId.getValue();
        if (currentStoreId != null) {
            storeId.setValue(currentStoreId);
        }
    }
    private void observeApiResponse(LiveData<?> response) {
        response.observeForever(apiResponse -> {
            if (apiResponse instanceof ApiResponse) {
                ApiResponse<?> apiResp = (ApiResponse<?>) apiResponse;
                if (apiResp.isLoading()) {
                    setLoading(true);
                } else {
                    setLoading(false);
                }
            }
        });
    }

    public boolean isLoading() {
        return true;
    }

    // Method to load vendors
    public void loadVendors(String id) {
        setStoreId(id);
        // Trigger the repository to fetch vendors
        vendorRepository.fetchVendors(id);
    }
}