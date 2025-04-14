package com.example.multitradex.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.stream.Collectors;

import com.example.multitradex.database.AppDatabase;
import com.example.multitradex.database.OrderDao;
import com.example.multitradex.firebase.FirebaseOrderService;
import com.example.multitradex.models.Order;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderRepository {
    private static final String TAG = "OrderRepository";
    private final OrderDao orderDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final FirebaseOrderService firebaseService = new FirebaseOrderService();

    public OrderRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        orderDao = db.orderDao();
    }

    public void insertOrder(Order order) {
        executor.execute(() -> orderDao.insertOrder(order));
        firebaseService.insertOrder(order);
    }

    public void insertOrders(List<Order> orders) {
        executor.execute(() -> orderDao.insertOrders(orders));
        firebaseService.insertOrders(orders);
    }

    public void updateOrder(Order order) {
        executor.execute(() -> orderDao.updateOrder(order));
        firebaseService.updateOrder(order);
    }

    public void deleteOrder(Order order) {
        executor.execute(() -> orderDao.deleteOrder(order));
        firebaseService.deleteOrder(order.getOrderId());
    }

    public void deleteOrders(List<Order> orders) {
        executor.execute(() -> orderDao.deleteOrders(orders));
        firebaseService.deleteOrders(
                orders.stream().map(Order::getOrderId).collect(Collectors.toList())
        );
    }

    public LiveData<List<Order>> getOrdersByUser(String userId) {
        MutableLiveData<List<Order>> liveData = new MutableLiveData<>();
        firebaseService.getOrdersByField("userId", userId, new FirebaseOrderService.OrderCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                executor.execute(() -> {
                    orderDao.insertOrders(orders);
                    liveData.postValue(orders);
                });
            }
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching orders from Firestore", e);
            }
        });
        return liveData;
    }

    public LiveData<List<Order>> getAllOrders() {
        MutableLiveData<List<Order>> liveData = new MutableLiveData<>();
        firebaseService.getAllOrders(new FirebaseOrderService.OrderCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                executor.execute(() -> {
                    orderDao.insertOrders(orders);
                    liveData.postValue(orders);
                });
            }
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching all orders", e);
            }
        });
        return liveData;
    }
}
