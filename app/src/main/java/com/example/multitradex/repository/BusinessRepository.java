package com.example.multitradex.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.multitradex.database.AppDatabase;
import com.example.multitradex.database.BusinessDao;
import com.example.multitradex.firebase.FirebaseBusinessService;
import com.example.multitradex.models.Business;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusinessRepository {

    private static final String TAG = "BusinessRepository";

    private final BusinessDao businessDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BusinessRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        businessDao = db.businessDao();
    }

    // Insert or update (sync Room and Firebase)
    public void saveBusiness(Business business) {
        executor.execute(() -> {
            businessDao.insertBusiness(business); // Room
            Log.d(TAG, "Business saved to Room");
        });
        FirebaseBusinessService.saveBusinessToFirestore(business); // Firestore
    }

    // Update (same as save since insert uses REPLACE)
    public void updateBusiness(Business business) {
        saveBusiness(business);
    }

    // Delete from both Room and Firebase
    public void deleteBusiness(Business business) {
        executor.execute(() -> {
            businessDao.deleteBusiness(business); // Room
            Log.d(TAG, "Business deleted from Room");
        });
        FirebaseBusinessService.deleteBusiness(business.getBusinessId(), success -> {
            if (success) {
                Log.d(TAG, "Business deleted from Firestore");
            }
        });
    }

    // Delete all businesses locally (can optionally call Firestore clear if needed)
    public void deleteAllBusinesses() {
        executor.execute(businessDao::deleteAllBusinesses);
    }

    // Sync one business by ID (Firestore -> Room)
    public void syncBusinessFromFirestoreIfNeeded(String businessId) {
        executor.execute(() -> {
            Business local = businessDao.getBusinessByBusinessId(businessId);
            if (local == null) {
                FirebaseBusinessService.getBusinessById(businessId, remote -> {
                    if (remote != null) {
                        executor.execute(() -> businessDao.insertBusiness(remote));
                        Log.d(TAG, "Synced business from Firestore to Room");
                    }
                });
            }
        });
    }

    // Sync all businesses from Firestore to Room
    public void syncAllBusinessesFromFirestore() {
        FirebaseBusinessService.getAllBusinesses(businessList -> {
            if (businessList != null) {
                executor.execute(() -> {
                    for (Business b : businessList) {
                        businessDao.insertBusiness(b);
                    }
                    Log.d(TAG, "All businesses synced from Firestore to Room");
                });
            }
        });
    }

    // ----------- ROOM GETTERS (LiveData) -----------

    public LiveData<Business> getBusinessByUserIdLive(String userId) {
        return businessDao.getBusinessByUserIdLive(userId);
    }

    public LiveData<Business> getBusinessByBusinessIdLive(String businessId) {
        return businessDao.getBusinessByBusinessIdLive(businessId);
    }

    public LiveData<List<Business>> getBusinessesByName(String name) {
        return businessDao.getBusinessesByName(name);
    }

    public LiveData<List<Business>> getBusinessesByGstNumber(String gstNumber) {
        return businessDao.getBusinessesByGstNumber(gstNumber);
    }

    public LiveData<List<Business>> getBusinessesByType(String type) {
        return businessDao.getBusinessesByType(type);
    }

    public LiveData<List<Business>> getAllBusinessesLive() {
        return businessDao.getAllBusinessesLive();
    }

    // ----------- ROOM GETTERS (Non-LiveData) -----------

    public Business getBusinessByUserId(String userId) {
        return businessDao.getBusinessByUserId(userId);
    }

    public Business getBusinessByBusinessId(String businessId) {
        return businessDao.getBusinessByBusinessId(businessId);
    }

    public List<Business> getAllBusinesses() {
        return businessDao.getAllBusinesses();
    }

    // ----------- FIRESTORE GETTERS -----------

    public void getBusinessFromFirestore(String businessId, FirebaseBusinessService.OnBusinessFetchCallback callback) {
        FirebaseBusinessService.getBusinessById(businessId, callback);
    }

    public void getBusinessByUserIdFromFirestore(String userId, FirebaseBusinessService.OnBusinessFetchCallback callback) {
        FirebaseBusinessService.getBusinessByUserId(userId, callback);
    }

    public void getBusinessesByNameFromFirestore(String name, FirebaseBusinessService.OnBusinessListFetchCallback callback) {
        FirebaseBusinessService.getBusinessesByName(name, callback);
    }

    public void getBusinessesByGstNumberFromFirestore(String gstNumber, FirebaseBusinessService.OnBusinessListFetchCallback callback) {
        FirebaseBusinessService.getBusinessesByGstNumber(gstNumber, callback);
    }

    public void getBusinessesByTypeFromFirestore(String type, FirebaseBusinessService.OnBusinessListFetchCallback callback) {
        FirebaseBusinessService.getBusinessesByType(type, callback);
    }

    public void getAllBusinessesFromFirestore(FirebaseBusinessService.OnBusinessListFetchCallback callback) {
        FirebaseBusinessService.getAllBusinesses(callback);
    }

    public void startBusinessRealtimeSync() {
        FirebaseBusinessService.listenToBusinessChanges(businessList -> {
            if (businessList != null) {
                executor.execute(() -> {
                    for (Business business : businessList) {
                        businessDao.insertBusiness(business);
                    }
                    Log.d(TAG, "Realtime sync: Businesses updated in Room");
                });
            }
        });
    }

}
