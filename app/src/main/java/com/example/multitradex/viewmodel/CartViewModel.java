package com.example.multitradex.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.multitradex.models.CartItem;
import com.example.multitradex.repository.CartRepository;

import java.util.List;

public class CartViewModel extends AndroidViewModel {

    private final CartRepository repository;

    public CartViewModel(@NonNull Application application) {
        super(application);
        repository = new CartRepository(application);
    }

    public LiveData<List<CartItem>> getCartItems(String userId) {
        return repository.getCartItems(userId);
    }

    public void insertCartItem(CartItem item) {
        repository.insertCartItem(item);
    }

    public void updateCartItem(CartItem item) {
        repository.updateCartItem(item);
    }

    public void deleteCartItem(CartItem item) {
        repository.deleteCartItem(item);
    }

    public void clearCart(String userId) {
        repository.clearCart(userId);
    }

    public void syncCartFromFirebase(String userId) {
        repository.syncCartFromFirebase(userId);
    }
}
