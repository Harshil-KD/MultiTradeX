package com.example.multitradex.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.multitradex.models.Business;

import java.util.List;

@Dao
public interface BusinessDao {

    // ✅ EXISTING METHODS (do not touch)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBusiness(Business business);

    @Query("SELECT * FROM business_table WHERE userId = :userId LIMIT 1")
    Business getBusinessByUserId(String userId);

    @Query("SELECT * FROM business_table")
    List<Business> getAllBusinesses();

    @Query("DELETE FROM business_table")
    void deleteAllBusinesses();

    // ✅ ADDITIONAL METHODS (added below without breaking the existing ones)

    // --- INSERT, UPDATE, DELETE with annotations ---
    @Update
    void updateBusiness(Business business);

    @Delete
    void deleteBusiness(Business business);

    // --- LiveData versions of GET ---
    @Query("SELECT * FROM business_table")
    LiveData<List<Business>> getAllBusinessesLive();

    @Query("SELECT * FROM business_table WHERE userId = :userId LIMIT 1")
    LiveData<Business> getBusinessByUserIdLive(String userId);

    // --- GET by businessId ---
    @Query("SELECT * FROM business_table WHERE businessId = :businessId LIMIT 1")
    Business getBusinessByBusinessId(String businessId);

    @Query("SELECT * FROM business_table WHERE businessId = :businessId LIMIT 1")
    LiveData<Business> getBusinessByBusinessIdLive(String businessId);

    // --- GET by businessName (partial match) ---
    @Query("SELECT * FROM business_table WHERE businessName LIKE '%' || :businessName || '%'")
    LiveData<List<Business>> getBusinessesByName(String businessName);

    // --- GET by gstNumber ---
    @Query("SELECT * FROM business_table WHERE gstNumber = :gstNumber")
    LiveData<List<Business>> getBusinessesByGstNumber(String gstNumber);

    // --- GET by businessType ---
    @Query("SELECT * FROM business_table WHERE businessType = :businessType")
    LiveData<List<Business>> getBusinessesByType(String businessType);
}
