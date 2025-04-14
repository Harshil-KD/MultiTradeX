package com.example.multitradex.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.multitradex.models.Order;
import com.example.multitradex.repository.OrderRepository;

import java.util.List;

public class OrderViewModel extends AndroidViewModel {
    private static final String TAG = "OrderViewModel";
    private final OrderRepository repository;
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public OrderViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public void insertOrder(Order order) {
        repository.insertOrder(order);
        statusMessage.postValue("Order inserted");
    }

    public void insertOrders(List<Order> orders) {
        repository.insertOrders(orders);
        statusMessage.postValue("Orders inserted");
    }

    public void updateOrder(Order order) {
        repository.updateOrder(order);
        statusMessage.postValue("Order updated");
    }

    public void deleteOrder(Order order) {
        repository.deleteOrder(order);
        statusMessage.postValue("Order deleted");
    }

    public void deleteOrders(List<Order> orders) {
        repository.deleteOrders(orders);
        statusMessage.postValue("Orders deleted");
    }

    public LiveData<List<Order>> getOrdersByUser(String userId) {
        return repository.getOrdersByUser(userId);
    }

    public LiveData<List<Order>> getAllOrders() {
        return repository.getAllOrders();
    }
}