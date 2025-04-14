package com.example.multitradex.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.multitradex.database.AppDatabase;
import com.example.multitradex.database.OrderItemDao;
import com.example.multitradex.firebase.FirebaseOrderItemService;
import com.example.multitradex.models.OrderItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderItemRepository {
    private static final String TAG = "OrderItemRepository";
    private final OrderItemDao orderItemDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final FirebaseOrderItemService firebaseService = new FirebaseOrderItemService();

    public OrderItemRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        orderItemDao = db.orderItemDao();
    }

    public void insertOrderItem(OrderItem item) {
        firebaseService.insertOrderItem(item, updatedItem -> {
            if (updatedItem != null) {
                executor.execute(() -> {
                    orderItemDao.insertOrderItem(updatedItem); // Save with Firestore ID
                    Log.d(TAG, "Inserted OrderItem into Room: " + updatedItem.getItemId());
                });
            } else {
                Log.e(TAG, "Failed to insert OrderItem: Firestore insert returned null");
            }
        });
    }

    public void insertOrderItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            Log.w(TAG, "insertOrderItems called with empty list");
            return;
        }

        for (OrderItem item : items) {
            firebaseService.insertOrderItem(item, updatedItem -> {
                if (updatedItem != null) {
                    executor.execute(() -> {
                        orderItemDao.insertOrderItem(updatedItem); // Save only after Firestore returns ID
                        Log.d(TAG, "Inserted OrderItem into Room: " + updatedItem.getItemId());
                    });
                } else {
                    Log.e(TAG, "Failed to insert OrderItem: Firestore insert returned null");
                }
            });
        }
    }



    public void updateOrderItem(OrderItem item) {
        executor.execute(() -> orderItemDao.updateOrderItem(item));
        firebaseService.updateOrderItem(item);
    }

    public void deleteOrderItem(OrderItem item) {
        executor.execute(() -> orderItemDao.deleteOrderItem(item));
        firebaseService.deleteOrderItem(item.getItemId());
    }

    public void deleteOrderItems(List<OrderItem> items) {
        executor.execute(() -> orderItemDao.deleteOrderItems(items));
        if (items != null && !items.isEmpty()) {
            firebaseService.deleteOrderItemsByField("orderId", items.get(0).getOrderId());
        }
    }


    // 🔹 ROOM: For LiveData observation
    public LiveData<List<OrderItem>> getAllOrderItemsLive() {
        return orderItemDao.getAllOrderItemsLive(); // Make sure this exists in DAO
    }

    // 🔹 ROOM: For sync call (e.g., from ViewModel)
    public List<OrderItem> getAllOrderItemsSync() {
        return orderItemDao.getAllOrderItems(); // existing synchronous method
    }

    // 🔹 FIREBASE: Async with callback
    public void getAllOrderItemsFromFirebase(FirebaseOrderItemService.OrderItemCallback callback) {
        firebaseService.getAllOrderItems(callback);
    }

    public LiveData<List<OrderItem>> getOrderItemsByOrderId(String orderId) {
        MutableLiveData<List<OrderItem>> liveData = new MutableLiveData<>();

        firebaseService.getOrderItemsByField("orderId", orderId, new FirebaseOrderItemService.OrderItemCallback() {
            @Override
            public void onSuccess(List<OrderItem> items) {
                executor.execute(() -> {
                    orderItemDao.insertOrderItems(items);
                    liveData.postValue(items);
                    Log.d(TAG, "Fetched and cached order items from Firestore");
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching order items", e);
            }
        });

        return liveData;
    }
}
