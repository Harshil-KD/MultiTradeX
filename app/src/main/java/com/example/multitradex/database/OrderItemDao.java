package com.example.multitradex.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.multitradex.models.OrderItem;

import java.util.List;

@Dao
public interface OrderItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrderItem(OrderItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrderItems(List<OrderItem> items);

    @Update
    void updateOrderItem(OrderItem item);

    @Query("UPDATE order_items SET quantity = :quantity WHERE orderId = :orderId")
    void updateQuantityByOrderId(String orderId, int quantity);

    @Delete
    void deleteOrderItem(OrderItem item);

    @Delete
    void deleteOrderItems(List<OrderItem> items);

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    void deleteOrderItemsByOrderId(String orderId);

    @Query("SELECT * FROM order_items")
    List<OrderItem> getAllOrderItems();


    @Query("SELECT * FROM order_items")
    LiveData<List<OrderItem>> getAllOrderItemsLive();


    @Query("SELECT * FROM order_items WHERE itemId = :itemId")
    OrderItem getOrderItemByItemId(String itemId);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    List<OrderItem> getOrderItemsByOrderId(String orderId);

    @Query("SELECT * FROM order_items WHERE productId = :productId")
    List<OrderItem> getOrderItemsByProductId(String productId);

    @Query("SELECT * FROM order_items WHERE productName = :productName")
    List<OrderItem> getOrderItemsByProductName(String productName);
}
