package com.example.multitradex.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.multitradex.database.AppDatabase;
import com.example.multitradex.database.CartDao;
import com.example.multitradex.firebase.FirebaseCartService;
import com.example.multitradex.models.CartItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartRepository {

    private final CartDao cartDao;
    private final FirebaseCartService firebaseService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private final Map<String, Runnable> pendingUpdates = new HashMap<>();


    private final Application application;

    public CartRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application.getApplicationContext());
        this.cartDao = db.cartDao();
        this.firebaseService = new FirebaseCartService();
    }

    public LiveData<List<CartItem>> getCartItems(String userId) {
        return cartDao.getCartItems(userId);
    }

    public void insertCartItem(CartItem item) {
        firebaseService.insertCartItem(item,
                insertedItem -> executor.execute(() -> {
                    cartDao.insert(insertedItem);
                    Log.d("CartRepository", "CartItem saved to both Firestore and Room");
                }),
                e -> Log.e("CartRepository", "Error inserting cart item", e)
        );
    }

    public void updateCartItem(CartItem item) {
        // ✅ Update local Room instantly
        executor.execute(() -> cartDao.update(item));

        // 🔁 Cancel any previous pending update for this item
        String key = item.getCartItemId();
        if (pendingUpdates.containsKey(key)) {
            debounceHandler.removeCallbacks(pendingUpdates.get(key));
        }

        // 🕒 Schedule Firestore update after delay
        Runnable task = () -> {
            firebaseService.updateCartItem(item);
            Log.d("CartRepository", "Debounced Firestore update for: " + key);
        };

        pendingUpdates.put(key, task);
        debounceHandler.postDelayed(task, 600); // Delay (in ms)
    }


    public void deleteCartItem(CartItem item) {
        executor.execute(() -> {
            cartDao.delete(item);
            firebaseService.deleteCartItem(item.getCartItemId());
            Log.d("CartRepository", "CartItem deleted from both Room and Firestore");
        });
    }

    public void clearCart(String userId) {
        executor.execute(() -> {
            cartDao.clearCart(userId);
            firebaseService.deleteCartForUser(userId);
            Log.d("CartRepository", "All cart items cleared for user: " + userId);
        });
    }

    public void syncCartFromFirebase(String userId) {
        firebaseService.getCartItems(userId, new FirebaseCartService.CartItemsCallback() {
            @Override
            public void onSuccess(List<CartItem> cartItems) {
                executor.execute(() -> {
                    cartDao.clearCart(userId);
                    cartDao.insertAll(cartItems);
                    Log.d("CartRepository", "Cart synced from Firestore to Room for user: " + userId);
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("CartRepository", "Failed to sync cart from Firestore", e);
            }
        });
    }
}
