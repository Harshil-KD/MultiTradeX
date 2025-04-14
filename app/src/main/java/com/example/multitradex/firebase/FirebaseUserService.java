package com.example.multitradex.firebase;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.multitradex.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseUserService {

    private static final String TAG = "FirebaseUserService";
    private static final String COLLECTION = "users";

    // Save or update user
    public static void saveUserToFirestore(User user) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User saved in Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving user", e));
    }

    // Get user by ID
    public static void getUserFromFirestore(String userId, OnUserFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        callback.onUserFetched(user);
                    } else {
                        callback.onUserFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user", e);
                    callback.onUserFetched(null);
                });
    }

    // Get all users
    public static void getAllUsers(OnUserListFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        list.add(doc.toObject(User.class));
                    }
                    callback.onUserListFetched(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching all users", e);
                    callback.onUserListFetched(null);
                });
    }

    // Get users by name (partial match)
    public static void getUsersByName(String name, OnUserListFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> filtered = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        User user = doc.toObject(User.class);
                        if (user.getName() != null &&
                                user.getName().toLowerCase().contains(name.toLowerCase())) {
                            filtered.add(user);
                        }
                    }
                    callback.onUserListFetched(filtered);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error filtering users by name", e);
                    callback.onUserListFetched(null);
                });
    }

    // Get users by email (exact match)
    public static void getUsersByEmail(String email, OnUserListFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> filtered = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        filtered.add(doc.toObject(User.class));
                    }
                    callback.onUserListFetched(filtered);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error filtering users by email", e);
                    callback.onUserListFetched(null);
                });
    }

    // Get users by role (exact match)
    public static void getUsersByRole(String role, OnUserListFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .whereEqualTo("role", role)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> filtered = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        filtered.add(doc.toObject(User.class));
                    }
                    callback.onUserListFetched(filtered);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error filtering users by role", e);
                    callback.onUserListFetched(null);
                });
    }


    // Delete user
    public static void deleteUser(String userId, OnUserDeleteCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted from Firestore");
                    callback.onUserDeleted(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting user", e);
                    callback.onUserDeleted(false);
                });
    }

    // Callbacks
    public interface OnUserFetchCallback {
        void onUserFetched(@Nullable User user);
    }

    public interface OnUserListFetchCallback {
        void onUserListFetched(@Nullable List<User> users);
    }

    public interface OnUserDeleteCallback {
        void onUserDeleted(boolean success);
    }

    public static void listenToUserChanges(OnUserListFetchCallback callback) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Snapshot listener failed", error);
                        return;
                    }
                    if (snapshot != null && !snapshot.isEmpty()) {
                        List<User> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshot) {
                            userList.add(doc.toObject(User.class));
                        }
                        callback.onUserListFetched(userList);
                    }
                });
    }

}
