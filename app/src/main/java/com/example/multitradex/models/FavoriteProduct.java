package com.example.multitradex.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_product_table")
public class FavoriteProduct {

    @PrimaryKey
    @NonNull
    private String id; // Format: userId_productId (for uniqueness)

    private String userId;
    private String productId;

    public FavoriteProduct(@NonNull String id, String userId, String productId) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
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
}
