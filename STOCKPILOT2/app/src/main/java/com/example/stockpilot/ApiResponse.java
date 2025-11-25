
package com.example.stockpilot;

/**
 * A generic class that holds a result with its status: success, error, or loading.
 * @param <T> Type of the data
 */
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }
    
    private final Status responseStatus;
    private final String errorMessage;

    private ApiResponse(Status responseStatus, T data, String errorMessage) {
        this.responseStatus = responseStatus;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(Status.SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> error(String errorMessage) {
        return new ApiResponse<>(Status.ERROR, null, errorMessage);
    }

    public static <T> ApiResponse<T> loading() {
        return new ApiResponse<>(Status.LOADING, null, null);
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status) || responseStatus == Status.SUCCESS;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isLoading() {
        return responseStatus == Status.LOADING;
    }
    
    // Add missing getter methods
    public String getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message != null ? message : errorMessage;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setData(T data) {
        this.data = data;
    }

    public String getNotifications() {
        return message;
    }

    public String deleteNotification() {
        return message;
    }

    public String markNotificationAsRead() {
        return message;
    }
}
