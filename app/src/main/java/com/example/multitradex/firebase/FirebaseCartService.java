package com.example.multitradex.firebase;

import android.util.Log;

import com.example.multitradex.models.CartItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class FirebaseCartService {

    private static final String TAG = "FirebaseCartService";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference cartRef = db.collection("cart_items");

    public void insertCartItem(CartItem item, OnSuccessListener<CartItem> listener, OnFailureListener failureListener) {
        DocumentReference docRef = db.collection("cart_items").document();
        String generatedId = docRef.getId();
        item.setCartItemId(generatedId);

        docRef.set(item)
                .addOnSuccessListener(aVoid -> {
                    docRef.get().addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            CartItem insertedItem = snapshot.toObject(CartItem.class);
                            if (insertedItem != null) {
                                listener.onSuccess(insertedItem); // 🔁 send back full object
                            }
                        }
                    });
                })
                .addOnFailureListener(failureListener);
    }



    public void updateCartItem(CartItem item) {
        cartRef.document(item.getCartItemId())
                .set(item, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Cart item updated: " + item.getCartItemId()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update cart item", e));
    }

    public void deleteCartItem(String cartItemId) {
        cartRef.document(cartItemId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Cart item deleted: " + cartItemId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete cart item", e));
    }

    public void deleteCartForUser(String userId) {
        cartRef.whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    Log.d(TAG, "All cart items deleted for user: " + userId);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete user cart items", e));
    }

    public void getCartItems(String userId, CartItemsCallback callback) {
        cartRef.whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CartItem> cartItems = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        CartItem item = doc.toObject(CartItem.class);
                        cartItems.add(item);
                    }
                    callback.onSuccess(cartItems);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface CartItemsCallback {
        void onSuccess(List<CartItem> cartItems);
        void onFailure(Exception e);
    }
}
