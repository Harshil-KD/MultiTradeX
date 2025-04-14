package com.example.multitradex.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;


@Entity(tableName = "business_table")
public class Business {

    @PrimaryKey(autoGenerate = false)
    @NonNull
    private String businessId;
    private String userId;
    private String businessName;
    private String businessType;
    private String gstNumber;
    private String address;

    public Business() {
    }

    @Ignore
    public Business(@NonNull String userId, String businessName, String businessType, String gstNumber, String address) {
        this.userId = userId;
        this.businessId = userId;
        this.businessName = businessName;
        this.businessType = businessType;
        this.gstNumber = gstNumber;
        this.address = address;
    }

    @NonNull
    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
