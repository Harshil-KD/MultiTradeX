package com.example.multitradex.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "payment_table")
public class Payment implements Serializable {

    @PrimaryKey
    @NonNull
    private String paymentId;

    private String orderId;
    private String userId;
    private String paymentMethod;  // e.g., "CreditCard", "CashOnDelivery"
    private String paymentStatus;  // e.g., "SUCCESS", "FAILED", "PENDING"
    private double amountPaid;
    private long timestamp;
    private String transactionId;  // Mock transaction ID
    private String failureReason;  // Optional

    public Payment() {}

    public Payment(@NonNull String paymentId, String orderId, String userId, String paymentMethod,
                   String paymentStatus, double amountPaid, long timestamp,
                   String transactionId, String failureReason) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.amountPaid = amountPaid;
        this.timestamp = timestamp;
        this.transactionId = transactionId;
        this.failureReason = failureReason;
    }

    @NonNull
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(@NonNull String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
