package com.example.multitradex.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.multitradex.database.AppDatabase;
import com.example.multitradex.database.UserDao;
import com.example.multitradex.firebase.FirebaseUserService;
import com.example.multitradex.models.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    private static final String TAG = "UserRepository";

    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        userDao = db.userDao();
    }

    // ------------------ Save or Update ------------------

    public void saveUser(User user) {
        executor.execute(() -> {
            userDao.insertUser(user);
            Log.d(TAG, "User saved to Room");
        });
        FirebaseUserService.saveUserToFirestore(user);
    }

    public void updateUser(User user) {
        saveUser(user);
    }

    // ------------------ Delete ------------------

    public void deleteUser(User user) {
        executor.execute(() -> {
            userDao.deleteUser(user);
            Log.d(TAG, "User deleted from Room");
        });
        FirebaseUserService.deleteUser(user.getUserId(), success -> {
            if (success) {
                Log.d(TAG, "User deleted from Firestore");
            }
        });
    }

    public void deleteAllUsers() {
        executor.execute(userDao::deleteAllUsers);
    }

    // ------------------ Sync ------------------

    public void syncUserIfNeeded(String userId) {
        executor.execute(() -> {
            User local = userDao.getUserById(userId);
            if (local == null) {
                FirebaseUserService.getUserFromFirestore(userId, fetched -> {
                    if (fetched != null) {
                        executor.execute(() -> userDao.insertUser(fetched));
                        Log.d(TAG, "User synced from Firestore to Room");
                    }
                });
            }
        });
    }

    public void syncAllUsersFromFirestore() {
        FirebaseUserService.getAllUsers(userList -> {
            if (userList != null) {
                executor.execute(() -> {
                    for (User u : userList) {
                        userDao.insertUser(u);
                    }
                    Log.d(TAG, "All users synced from Firestore to Room");
                });
            }
        });
    }

    // ------------------ Room Queries (LiveData) ------------------

    public LiveData<User> getUser(String userId) {
        return userDao.getUserByIdLive(userId);
    }

    public LiveData<List<User>> getAllUsersLive() {
        return userDao.getAllUsersLive();
    }

    public LiveData<List<User>> getUsersByName(String name) {
        return userDao.getUsersByName(name);
    }

    public LiveData<List<User>> getUsersByRole(String role) {
        return userDao.getUsersByRole(role);
    }

    public LiveData<List<User>> getUsersByEmail(String email) {
        return userDao.getUsersByEmail(email);
    }

    // ------------------ Room Queries (Direct) ------------------

    public User getUserDirect(String userId) {
        return userDao.getUserById(userId);
    }

    public List<User> getAllUsersDirect() {
        return userDao.getAllUsers();
    }

    // ------------------ Firestore Queries (Async) ------------------

    public void getUserFromFirestore(String userId, FirebaseUserService.OnUserFetchCallback callback) {
        FirebaseUserService.getUserFromFirestore(userId, callback);
    }

    public void getAllUsersFromFirestore(FirebaseUserService.OnUserListFetchCallback callback) {
        FirebaseUserService.getAllUsers(callback);
    }

    public void getUsersByNameFromFirestore(String name, FirebaseUserService.OnUserListFetchCallback callback) {
        FirebaseUserService.getUsersByName(name, callback);
    }

    public void getUsersByEmailFromFirestore(String email, FirebaseUserService.OnUserListFetchCallback callback) {
        FirebaseUserService.getUsersByEmail(email, callback);
    }

    public void getUsersByRoleFromFirestore(String role, FirebaseUserService.OnUserListFetchCallback callback) {
        FirebaseUserService.getUsersByRole(role, callback);
    }

    public void startUserRealtimeSync() {
        FirebaseUserService.listenToUserChanges(userList -> {
            if (userList != null) {
                executor.execute(() -> {
                    for (User user : userList) {
                        userDao.insertUser(user);
                    }
                    Log.d(TAG, "Realtime sync: Users updated in Room");
                });
            }
        });
    }


}
