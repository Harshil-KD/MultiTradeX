package com.example.multitradex.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.multitradex.database.AppDatabase;
import com.example.multitradex.database.FavoriteProductDao;
import com.example.multitradex.models.FavoriteProduct;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteProductRepository {

    private static final String TAG = "FavoriteProductRepo";

    private final FavoriteProductDao favoriteProductDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public FavoriteProductRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        favoriteProductDao = db.favoriteProductDao();
    }

    // Insert
    public void addFavorite(FavoriteProduct favoriteProduct) {
        executor.execute(() -> {
            try {
                favoriteProductDao.insertFavorite(favoriteProduct);
                Log.d(TAG, "Favorite added: " + favoriteProduct.getProductId());
            } catch (Exception e) {
                Log.e(TAG, "Error adding favorite", e);
            }
        });
    }

    // Delete by object
    public void removeFavorite(FavoriteProduct favoriteProduct) {
        executor.execute(() -> {
            try {
                favoriteProductDao.deleteFavorite(favoriteProduct);
                Log.d(TAG, "Favorite removed: " + favoriteProduct.getProductId());
            } catch (Exception e) {
                Log.e(TAG, "Error removing favorite", e);
            }
        });
    }

    // Delete by ID
    // Delete by ID
    public void removeFavorite(String userId, String productId) {
        executor.execute(() -> {
            try {
                favoriteProductDao.deleteByUserAndProduct(userId, productId);
                Log.d(TAG, "Favorite removed by userId/productId: " + productId);
            } catch (Exception e) {
                Log.e(TAG, "Error removing favorite by ID", e);
            }
        });
    }

    // Returns only the product IDs that a user has marked as favorite
    public LiveData<List<String>> getFavoriteProductIdsByUser(String userId) {
        return favoriteProductDao.getFavoriteProductIdsByUser(userId);
    }


    // Check if a product is favorited by a user (returns LiveData<Integer>)
    public LiveData<Integer> isProductFavorited(String userId, String productId) {
        return favoriteProductDao.isProductFavorited(userId, productId);
    }

}
