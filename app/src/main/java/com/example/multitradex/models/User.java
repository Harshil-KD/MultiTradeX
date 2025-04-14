package com.example.multitradex.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.database.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@Entity(tableName = "users") // Room Database Table
public class User {
    @PrimaryKey(autoGenerate = false) // Use Firebase userId as primary key
    @NonNull
    private String userId;

    private String name;
    private String email;
    private String role; // buyer, seller, admin

    @ServerTimestamp // Firestore Timestamp
    private Date createdAt;

    // Empty constructor required for Firestore and Room
    public User() {
    }

    @Ignore
    public User(@NonNull  String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Exclude // Firestore should not store this
    public boolean isSeller() {
        return "seller".equalsIgnoreCase(role) || "both".equalsIgnoreCase(role);
    }
}