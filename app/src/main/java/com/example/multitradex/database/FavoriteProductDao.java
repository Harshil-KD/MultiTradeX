package com.example.multitradex.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;

import com.example.multitradex.models.FavoriteProduct;

import java.util.List;

@Dao
public interface FavoriteProductDao {

    // ✅ Insert a favorite product (or replace if duplicate)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(FavoriteProduct favoriteProduct);

    // ✅ Delete a favorite product
    @Delete
    void deleteFavorite(FavoriteProduct favoriteProduct);

    // ✅ Delete by userId and productId
    @Query("DELETE FROM favorite_product_table WHERE userId = :userId AND productId = :productId")
    void deleteByUserAndProduct(String userId, String productId);

    // ✅ Get all favorite product IDs for a specific user
    @Query("SELECT productId FROM favorite_product_table WHERE userId = :userId")
    LiveData<List<String>> getFavoriteProductIdsByUser(String userId);

    // ✅ Check if a specific product is favorited by the user
    @Query("SELECT COUNT(*) FROM favorite_product_table WHERE userId = :userId AND productId = :productId")
    LiveData<Integer> isProductFavorited(String userId, String productId);

    // ✅ Get all FavoriteProduct objects for a user (if needed for syncing)
    @Query("SELECT * FROM favorite_product_table WHERE userId = :userId")
    LiveData<List<FavoriteProduct>> getAllFavoritesByUser(String userId);
}
