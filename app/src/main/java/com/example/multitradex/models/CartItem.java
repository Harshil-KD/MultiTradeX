package com.example.multitradex.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "cart_table")
public class CartItem implements Serializable {

    @PrimaryKey(autoGenerate = false)
    @NonNull
    private String cartItemId; // Unique per row (UUID or combination of user+product)

    private String userId;     // Current logged-in user
    private String productId;  // FK reference to product
    private String productName;
    private String imageUrl;
    private double unitPrice;
    private int quantity;
    private long addedAt;      // Unix timestamp

    public CartItem() {}

    public CartItem(@NonNull String cartItemId, String userId, String productId, String productName,
                    String imageUrl, double unitPrice, int quantity, long addedAt) {
        this.cartItemId = cartItemId;
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.addedAt = addedAt;
    }

    @NonNull
    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(@NonNull String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }
}
