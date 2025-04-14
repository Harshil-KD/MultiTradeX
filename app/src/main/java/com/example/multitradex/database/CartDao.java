package com.example.multitradex.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.multitradex.models.CartItem;

import java.util.List;

@Dao
public interface CartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CartItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CartItem> items);

    @Update
    void update(CartItem item);

    @Delete
    void delete(CartItem item);

    @Query("DELETE FROM cart_table WHERE userId = :userId")
    void clearCart(String userId);

    @Query("SELECT * FROM cart_table WHERE userId = :userId")
    LiveData<List<CartItem>> getCartItems(String userId);

    @Query("SELECT * FROM cart_table WHERE userId = :userId AND productId = :productId LIMIT 1")
    CartItem getCartItemByProduct(String userId, String productId);
}