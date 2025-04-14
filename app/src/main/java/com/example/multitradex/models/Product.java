package com.example.multitradex.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(tableName = "product_table")
public class Product implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    private String description;
    private String category;
    private String imageUrlsJson; // JSON array of Imgur-hosted image URLs
    private double priceRetail;
    private String priceWholesale; // JSON string: {"1-5": 100.0, "6-10": 90.0, "11+": 80.0}
    private int stock;
    private String availableFor; // B2B, B2C, Both
    private String sellerId;

    public Product() {}

    @Ignore
    public Product(@NonNull String id, String name, String description, String category,
                   String imageUrlsJson, double priceRetail, String priceWholesale,
                   int stock, String availableFor, String sellerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageUrlsJson = imageUrlsJson;
        this.priceRetail = priceRetail;
        this.priceWholesale = priceWholesale;
        this.stock = stock;
        this.availableFor = availableFor;
        this.sellerId = sellerId;
    }

    // Getters and Setters
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrlsJson() {
        return imageUrlsJson;
    }

    public void setImageUrlsJson(String imageUrlsJson) {
        this.imageUrlsJson = imageUrlsJson;
    }

    public double getPriceRetail() {
        return priceRetail;
    }

    public void setPriceRetail(double priceRetail) {
        this.priceRetail = priceRetail;
    }

    public String getPriceWholesale() {
        return priceWholesale;
    }

    public void setPriceWholesale(String priceWholesale) {
        this.priceWholesale = priceWholesale;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getAvailableFor() {
        return availableFor;
    }

    public void setAvailableFor(String availableFor) {
        this.availableFor = availableFor;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    // JSON helpers

    public Map<String, Double> getWholesalePricing() {
        if (priceWholesale == null || priceWholesale.isEmpty()) return new HashMap<>();
        return new Gson().fromJson(priceWholesale, new TypeToken<Map<String, Double>>() {}.getType());
    }

    public void setWholesalePricing(Map<String, Double> pricingMap) {
        this.priceWholesale = new Gson().toJson(pricingMap);
    }

    public List<String> getImageUrls() {
        if (imageUrlsJson == null || imageUrlsJson.isEmpty()) return new ArrayList<>();
        return new Gson().fromJson(imageUrlsJson, new TypeToken<List<String>>() {}.getType());
    }

    public void setImageUrls(List<String> urls) {
        this.imageUrlsJson = new Gson().toJson(urls);
    }

    // Validation helper
    public boolean isValid() {
        return name != null && !name.isEmpty()
                && priceRetail >= 0
                && stock >= 0;
    }

    // Logging helper
    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", priceRetail=" + priceRetail +
                ", priceWholesale=" + getWholesalePricing() +
                ", availableFor='" + availableFor + '\'' +
                ", sellerId='" + sellerId + '\'' +
                '}';
    }
}
