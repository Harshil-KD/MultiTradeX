package com.example.multitradex.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.multitradex.models.Product;

import java.util.List;

@Dao
public interface ProductDao {

    String TAG = "ProductDao";

    // ---------------- Insert ----------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProduct(Product product);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProducts(List<Product> products);

    // ---------------- Update ----------------

    @Update
    void updateProduct(Product product);

    @Update
    void updateProducts(List<Product> products);

    // ---------------- Delete ----------------

    @Delete
    void deleteProduct(Product product);

    @Delete
    void deleteProducts(List<Product> products);

    @Query("DELETE FROM product_table")
    void deleteAllProducts();

    @Query("DELETE FROM product_table WHERE id = :productId")
    void deleteProductById(String productId);

    // ---------------- Basic Queries ----------------

    @Query("SELECT * FROM product_table")
    List<Product> getAllProductsOnce();

    @Query("SELECT * FROM product_table")
    LiveData<List<Product>> getAllProductsLive();

    @Query("SELECT * FROM product_table WHERE id = :productId LIMIT 1")
    Product getProductById(String productId);

    @Query("SELECT * FROM product_table WHERE id IN (:ids)")
    LiveData<List<Product>> getProductsByIds(List<String> ids);

    // ---------------- Seller Queries ----------------

    @Query("SELECT * FROM product_table WHERE sellerId = :userId")
    List<Product> getProductsBySeller(String userId);

    @Query("SELECT * FROM product_table WHERE sellerId = :userId")
    LiveData<List<Product>> getProductsBySellerLive(String userId);

    @Query("SELECT * FROM product_table WHERE sellerId = :sellerId AND category = :category")
    List<Product> getProductsBySellerAndCategory(String sellerId, String category);

    @Query("SELECT * FROM product_table WHERE sellerId = :sellerId AND category = :category")
    LiveData<List<Product>> getProductsBySellerAndCategoryLive(String sellerId, String category);

    // ---------------- Advanced Queries ----------------

    @Query("SELECT * FROM product_table WHERE category = :category")
    List<Product> getProductsByCategory(String category);

    @Query("SELECT * FROM product_table WHERE priceRetail BETWEEN :minPrice AND :maxPrice")
    List<Product> getProductsByRetailPriceRange(double minPrice, double maxPrice);

    @Query("SELECT * FROM product_table WHERE name LIKE '%' || :name || '%'")
    List<Product> getProductsByName(String name);

    @Query("SELECT * FROM product_table WHERE availableFor = :availableFor")
    List<Product> getProductsByAvailability(String availableFor);
}
