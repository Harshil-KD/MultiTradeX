package com.example.multitradex.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.multitradex.firebase.FirebaseOrderItemService;
import com.example.multitradex.models.OrderItem;
import com.example.multitradex.repository.OrderItemRepository;

import java.util.List;

public class OrderItemViewModel extends AndroidViewModel {
    private static final String TAG = "OrderItemViewModel";
    private final OrderItemRepository repository;
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public OrderItemViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderItemRepository(application);
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public void insertOrderItem(OrderItem item) {
        Log.d(TAG, "Inserting single order item...");
        repository.insertOrderItem(item);
        statusMessage.postValue("Order item insert requested");
    }

    public void insertOrderItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            Log.w(TAG, "No order items to insert.");
            statusMessage.postValue("No order items to insert");
            return;
        }

        Log.d(TAG, "Inserting multiple order items (" + items.size() + ")");
        repository.insertOrderItems(items);
        statusMessage.postValue("Order items insert requested");
    }


    public void updateOrderItem(OrderItem item) {
        Log.d(TAG, "Updating order item: " + item.getItemId());
        repository.updateOrderItem(item);
        statusMessage.postValue("Order item update requested");
    }

    public void deleteOrderItem(OrderItem item) {
        Log.d(TAG, "Deleting order item: " + item.getItemId());
        repository.deleteOrderItem(item);
        statusMessage.postValue("Order item delete requested");
    }

    public void deleteOrderItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            Log.w(TAG, "No order items to delete.");
            return;
        }

        Log.d(TAG, "Deleting multiple order items...");
        repository.deleteOrderItems(items);
        statusMessage.postValue("Order items delete requested");
    }

    // 🔹 Observe all OrderItems (for Dashboard)
    public LiveData<List<OrderItem>> getAllOrderItemsLive() {
        return repository.getAllOrderItemsLive();
    }

    // 🔹 Sync call (e.g., from coroutine, background thread)
    public List<OrderItem> getAllOrderItemsSync() {
        return repository.getAllOrderItemsSync();
    }

    // 🔹 Fetch from Firebase using callback
    public void getAllOrderItemsFromFirebase(FirebaseOrderItemService.OrderItemCallback callback) {
        repository.getAllOrderItemsFromFirebase(callback);
    }

    public LiveData<List<OrderItem>> getOrderItemsByOrderId(String orderId) {
        Log.d(TAG, "Fetching order items for orderId: " + orderId);
        return repository.getOrderItemsByOrderId(orderId);
    }
}
