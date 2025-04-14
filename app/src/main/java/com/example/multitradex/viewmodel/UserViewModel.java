package com.example.multitradex.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.multitradex.models.Business;
import com.example.multitradex.models.User;
import com.example.multitradex.repository.BusinessRepository;
import com.example.multitradex.repository.UserRepository;

import java.util.List;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        businessRepository = new BusinessRepository(application);
    }

    // ---------------------- USER LOGIC ----------------------

    public LiveData<User> getUser(String userId) {
        return userRepository.getUser(userId);
    }

    public void saveUser(User user) {
        userRepository.saveUser(user);
    }

    public void deleteAllUsers() {
        userRepository.deleteAllUsers();
    }

    // ---------------------- BUSINESS LOGIC ----------------------

    // Insert or update business (Room + Firebase)
    public void saveBusiness(Business business) {
        businessRepository.saveBusiness(business);
    }

    // Update is same as save in repository
    public void updateBusiness(Business business) {
        businessRepository.updateBusiness(business);
    }

    // Delete business (Room + Firebase)
    public void deleteBusiness(Business business) {
        businessRepository.deleteBusiness(business);
    }

    // Delete all businesses from Room (optional Firestore bulk delete not included)
    public void deleteAllBusinesses() {
        businessRepository.deleteAllBusinesses();
    }

    // Sync one business from Firestore if not in Room
    public void syncBusinessIfNeeded(String businessId) {
        businessRepository.syncBusinessFromFirestoreIfNeeded(businessId);
    }

    // Sync all businesses from Firestore to Room
    public void syncAllBusinessesFromFirestore() {
        businessRepository.syncAllBusinessesFromFirestore();
    }

    // ----------- GETTERS FROM ROOM (LiveData) -----------

    public LiveData<Business> getBusinessByUserId(String userId) {
        return businessRepository.getBusinessByUserIdLive(userId);
    }

    public LiveData<Business> getBusinessByBusinessId(String businessId) {
        return businessRepository.getBusinessByBusinessIdLive(businessId);
    }

    public LiveData<List<Business>> getBusinessesByName(String name) {
        return businessRepository.getBusinessesByName(name);
    }

    public LiveData<List<Business>> getBusinessesByGstNumber(String gstNumber) {
        return businessRepository.getBusinessesByGstNumber(gstNumber);
    }

    public LiveData<List<Business>> getBusinessesByType(String type) {
        return businessRepository.getBusinessesByType(type);
    }

    public LiveData<List<Business>> getAllBusinesses() {
        return businessRepository.getAllBusinessesLive();
    }

    public void startRealtimeSync() {
        userRepository.startUserRealtimeSync();
        businessRepository.startBusinessRealtimeSync();
    }
}
