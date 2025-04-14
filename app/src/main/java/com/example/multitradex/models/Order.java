package com.example.multitradex.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "orders_table")
public class Order implements Serializable {

    @PrimaryKey
    @NonNull
    private String orderId;

    private String userId;
    private double totalAmount;
    private long timestamp; // Unix time
    private String paymentStatus; // e.g., "SUCCESS", "PENDING", "FAILED"

    // Optional: for address/shipping
    private String shippingName;
    private String shippingAddress;
    private String city;
    private String postalCode;

    public Order() {}

    public Order(@NonNull String orderId, String userId, double totalAmount, long timestamp,
                 String paymentStatus, String shippingName, String shippingAddress,
                 String city, String postalCode) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.timestamp = timestamp;
        this.paymentStatus = paymentStatus;
        this.shippingName = shippingName;
        this.shippingAddress = shippingAddress;
        this.city = city;
        this.postalCode = postalCode;
    }

    @NonNull
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(@NonNull String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getShippingName() {
        return shippingName;
    }

    public void setShippingName(String shippingName) {
        this.shippingName = shippingName;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
