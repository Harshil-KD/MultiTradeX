package com.example.multitradex.firebase;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.multitradex.models.Business;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseBusinessService {

    private static final String TAG = "FirebaseBusinessService";
    private static final String COLLECTION_NAME = "businesses";

    // ✅ Save or update business
    public static void saveBusinessToFirestore(Business business) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .document(business.getBusinessId())
                .set(business)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Business saved in Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving business", e));
    }

    // ✅ Get by businessId
    public static void getBusinessById(String businessId, OnBusinessFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .document(businessId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Business business = snapshot.toObject(Business.class);
                        callback.onBusinessFetched(business);
                    } else {
                        callback.onBusinessFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching business by ID", e);
                    callback.onBusinessFetched(null);
                });
    }

    // ✅ Get by userId
    public static void getBusinessByUserId(String userId, OnBusinessFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Business business = snapshot.getDocuments().get(0).toObject(Business.class);
                        callback.onBusinessFetched(business);
                    } else {
                        callback.onBusinessFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching business by userId", e);
                    callback.onBusinessFetched(null);
                });
    }

    // ✅ Get all businesses
    public static void getAllBusinesses(OnBusinessListFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Business> businesses = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        businesses.add(doc.toObject(Business.class));
                    }
                    callback.onBusinessListFetched(businesses);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching all businesses", e);
                    callback.onBusinessListFetched(null);
                });
    }

    // ✅ Get by business name (contains)
    public static void getBusinessesByName(String name, OnBusinessListFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Business> filtered = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Business business = doc.toObject(Business.class);
                        if (business.getBusinessName() != null &&
                                business.getBusinessName().toLowerCase().contains(name.toLowerCase())) {
                            filtered.add(business);
                        }
                    }
                    callback.onBusinessListFetched(filtered);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error filtering businesses by name", e);
                    callback.onBusinessListFetched(null);
                });
    }

    // ✅ Get by GST number
    public static void getBusinessesByGstNumber(String gstNumber, OnBusinessListFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .whereEqualTo("gstNumber", gstNumber)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Business> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        list.add(doc.toObject(Business.class));
                    }
                    callback.onBusinessListFetched(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching by GST number", e);
                    callback.onBusinessListFetched(null);
                });
    }

    // ✅ Get by business type
    public static void getBusinessesByType(String businessType, OnBusinessListFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .whereEqualTo("businessType", businessType)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Business> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        list.add(doc.toObject(Business.class));
                    }
                    callback.onBusinessListFetched(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching by business type", e);
                    callback.onBusinessListFetched(null);
                });
    }

    // ✅ Delete a business
    public static void deleteBusiness(String businessId, OnBusinessDeleteCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .document(businessId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Business deleted successfully");
                    callback.onBusinessDeleted(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting business", e);
                    callback.onBusinessDeleted(false);
                });
    }

    // ✅ Callback Interfaces
    public interface OnBusinessFetchCallback {
        void onBusinessFetched(@Nullable Business business);
    }

    public interface OnBusinessListFetchCallback {
        void onBusinessListFetched(@Nullable List<Business> businessList);
    }

    public interface OnBusinessDeleteCallback {
        void onBusinessDeleted(boolean success);
    }

    public static void listenToBusinessChanges(OnBusinessListFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Business listener failed", error);
                        return;
                    }
                    if (snapshot != null && !snapshot.isEmpty()) {
                        List<Business> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshot) {
                            list.add(doc.toObject(Business.class));
                        }
                        callback.onBusinessListFetched(list);
                    }
                });
    }

}
