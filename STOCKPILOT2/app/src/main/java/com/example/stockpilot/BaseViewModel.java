package com.example.stockpilot;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.stockpilot.ApiResponse;

public class BaseViewModel extends ViewModel {
    protected final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    protected final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    protected <T> void handleResponse(ApiResponse<T> response, MutableLiveData<T> data) {
        isLoading.setValue(false);
        
        if (response.isSuccess()) {
            if (data != null) {
                data.setValue(response.getData());
            }
        } else {
            errorMessage.setValue(response.getErrorMessage());
        }
    }
    
    protected void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }
    
    protected void setError(String message) {
        errorMessage.setValue(message);
        isLoading.setValue(false);
    }
}