package com.example.multitradex.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.multitradex.models.Payment;

import java.util.List;

@Dao
public interface PaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPayment(Payment payment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPayments(List<Payment> payments);

    @Update
    void updatePayment(Payment payment);

    @Query("UPDATE payment_table SET paymentStatus = :status WHERE orderId = :orderId")
    void updatePaymentStatusByOrderId(String orderId, String status);

    @Query("UPDATE payment_table SET paymentStatus = :status WHERE paymentId = :paymentId")
    void updatePaymentStatusByPaymentId(String paymentId, String status);

    @Query("UPDATE payment_table SET paymentStatus = :status WHERE userId = :userId")
    void updatePaymentStatusByUserId(String userId, String status);

    @Delete
    void deletePayment(Payment payment);

    @Delete
    void deletePayments(List<Payment> payments);

    @Query("DELETE FROM payment_table WHERE orderId = :orderId")
    void deletePaymentByOrderId(String orderId);

    @Query("DELETE FROM payment_table WHERE paymentId = :paymentId")
    void deletePaymentByPaymentId(String paymentId);

    @Query("DELETE FROM payment_table WHERE userId = :userId")
    void deletePaymentByUserId(String userId);

    @Query("SELECT * FROM payment_table WHERE userId = :userId")
    List<Payment> getPaymentsByUserId(String userId);

    @Query("SELECT * FROM payment_table WHERE orderId = :orderId")
    List<Payment> getPaymentsByOrderId(String orderId);

    @Query("SELECT * FROM payment_table WHERE paymentId = :paymentId")
    Payment getPaymentByPaymentId(String paymentId);

    @Query("SELECT * FROM payment_table WHERE paymentMethod = :method")
    List<Payment> getPaymentsByMethod(String method);

    @Query("SELECT * FROM payment_table WHERE paymentStatus = :status")
    List<Payment> getPaymentsByStatus(String status);

    @Query("SELECT * FROM payment_table WHERE amountPaid = :amount")
    List<Payment> getPaymentsByAmount(double amount);

    @Query("SELECT * FROM payment_table WHERE transactionId = :transactionId")
    List<Payment> getPaymentsByTransactionId(String transactionId);

    @Query("SELECT * FROM payment_table WHERE timestamp = :timestamp")
    List<Payment> getPaymentsByTimestamp(long timestamp);
}