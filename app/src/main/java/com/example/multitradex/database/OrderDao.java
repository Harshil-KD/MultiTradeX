package com.example.multitradex.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.multitradex.models.Order;

import java.util.List;

@Dao
public interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrder(Order order);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrders(List<Order> orders);

    @Update
    void updateOrder(Order order);

    @Delete
    void deleteOrder(Order order);

    @Delete
    void deleteOrders(List<Order> orders);

    @Query("SELECT * FROM orders_table")
    List<Order> getAllOrders();

    @Query("SELECT * FROM orders_table WHERE userId = :userId")
    List<Order> getOrdersByUser(String userId);

    @Query("SELECT * FROM orders_table WHERE totalAmount = :amount")
    List<Order> getOrdersByAmount(double amount);

    @Query("SELECT * FROM orders_table WHERE shippingName = :name")
    List<Order> getOrdersByShippingName(String name);

    @Query("SELECT * FROM orders_table WHERE shippingAddress = :address")
    List<Order> getOrdersByShippingAddress(String address);

    @Query("SELECT * FROM orders_table WHERE city = :city")
    List<Order> getOrdersByCity(String city);

    @Query("SELECT * FROM orders_table WHERE postalCode = :postalCode")
    List<Order> getOrdersByPostalCode(String postalCode);

    @Query("SELECT * FROM orders_table WHERE timestamp = :timestamp")
    List<Order> getOrdersByTimestamp(long timestamp);
}