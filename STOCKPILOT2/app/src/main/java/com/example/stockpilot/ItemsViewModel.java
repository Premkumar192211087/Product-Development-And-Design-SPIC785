package com.example.stockpilot;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

public class ItemsViewModel extends BaseViewModel {
    private final ItemRepository itemRepository;
    private final MutableLiveData<String> storeId = new MutableLiveData<>();
    private final MutableLiveData<String> status = new MutableLiveData<>("All");
    private final MutableLiveData<String> sortBy = new MutableLiveData<>("product_name");
    private final MutableLiveData<String> sortOrder = new MutableLiveData<>("ASC");

    private final LiveData<ApiResponse<List<Item>>> itemsResponse;
    private final LiveData<List<Item>> items;

    public ItemsViewModel() {
        itemRepository = ItemRepository.getInstance();
        itemsResponse = Transformations.switchMap(storeId, id ->
            Transformations.switchMap(status, s ->
                Transformations.switchMap(sortBy, sb ->
                    Transformations.switchMap(sortOrder, so -> {
                        setLoading(true);
                        return itemRepository.getItems(id, s, sb, so);
                    })
                )
            )
        );
        items = Transformations.map(itemsResponse, response -> {
            handleResponse(response, null);
            return response.isSuccess() ? response.getData() : null;
        });
    }

    public LiveData<List<Item>> getItems() {
        return items;
    }

    public void loadItems(String storeId) {
        this.storeId.setValue(storeId);
    }

    public void loadItems(String storeId, String status, String sortBy, String sortOrder) {
        this.storeId.setValue(storeId);
        this.status.setValue(status);
        this.sortBy.setValue(sortBy);
        this.sortOrder.setValue(sortOrder);
    }

    public void setStatus(String status) {
        this.status.setValue(status);
    }

    public void setSortBy(String sortBy) {
        this.sortBy.setValue(sortBy);
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder.setValue(sortOrder);
    }
}