package com.example.multitradex.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.multitradex.models.FavoriteProduct;
import com.example.multitradex.repository.FavoriteProductRepository;

import java.util.List;

public class FavoriteProductViewModel extends AndroidViewModel {

    private static final String TAG = "FavoriteProductViewModel";

    private final FavoriteProductRepository repository;

    public FavoriteProductViewModel(@NonNull Application application) {
        super(application);
        repository = new FavoriteProductRepository(application);
    }

    // ✅ Add a product to favorites
    public void addFavorite(FavoriteProduct favoriteProduct) {
        try {
            repository.addFavorite(favoriteProduct);
        } catch (Exception e) {
            Log.e(TAG, "Error adding favorite", e);
        }
    }

    // ✅ Remove a favorite by object
    public void removeFavorite(FavoriteProduct favoriteProduct) {
        try {
            repository.removeFavorite(favoriteProduct);
        } catch (Exception e) {
            Log.e(TAG, "Error removing favorite", e);
        }
    }

    // ✅ Remove by userId + productId
    public void removeFavoriteById(String userId, String productId) {
        try {
            repository.removeFavorite(userId, productId);
        } catch (Exception e) {
            Log.e(TAG, "Error removing favorite by ID", e);
        }
    }

    // ✅ Get LiveData of product IDs favorited by a user
    public LiveData<List<String>> getFavoriteProductIds(String userId) {
        return repository.getFavoriteProductIdsByUser(userId);
    }

    // ✅ Check if a product is favorited by a user
    public LiveData<Integer> isProductFavorited(String userId, String productId) {
        return repository.isProductFavorited(userId, productId);
    }
}
