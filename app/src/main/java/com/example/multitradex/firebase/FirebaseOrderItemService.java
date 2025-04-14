package com.example.multitradex.firebase;

import android.util.Log;

import com.example.multitradex.models.OrderItem;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FirebaseOrderItemService {

    private static final String TAG = "FirebaseOrderItemService";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference itemRef = db.collection("order_items");

    public interface OrderItemCallback {
        void onSuccess(List<OrderItem> items);
        void onFailure(Exception e);
    }

    public void insertOrderItem(OrderItem item, Consumer<OrderItem> onComplete) {
        // Generate itemId if not already present
        if (item.getItemId() == null || item.getItemId().isEmpty()) {
            String generatedId = itemRef.document().getId();
            item.setItemId(generatedId);
        }

        itemRef.document(item.getItemId())
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "OrderItem saved with ID: " + item.getItemId());
                    onComplete.accept(item);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving OrderItem to Firestore", e);
                    // Still notify completion with null (optional - depends on your logic)
                    onComplete.accept(null);
                });
    }

    public void insertOrderItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            Log.w(TAG, "insertOrderItems: No items to insert.");
            return;
        }

        for (OrderItem item : items) {
            insertOrderItem(item, updatedItem -> {
                if (updatedItem != null) {
                    Log.d(TAG, "Order item inserted with Firestore ID: " + updatedItem.getItemId());
                } else {
                    Log.e(TAG, "Order item insert failed (null returned from Firestore)");
                }
            });
        }
    }


    public void updateOrderItem(OrderItem item) {
        if (item.getItemId() == null) {
            Log.e(TAG, "Cannot update OrderItem: itemId is null");
            return;
        }
        itemRef.document(item.getItemId()).set(item)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Order item updated: " + item.getItemId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating order item", e));
    }

    public void deleteOrderItem(String itemId) {
        itemRef.document(itemId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Order item deleted: " + itemId))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting order item", e));
    }

    public void deleteOrderItemsByField(String field, Object value) {
        itemRef.whereEqualTo(field, value).get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) doc.getReference().delete();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting items by field", e));
    }

    public void getOrderItemsByField(String field, Object value, OrderItemCallback callback) {
        itemRef.whereEqualTo(field, value).get()
                .addOnSuccessListener(snapshot -> {
                    List<OrderItem> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot)
                        list.add(doc.toObject(OrderItem.class));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllOrderItems(OrderItemCallback callback) {
        itemRef.get()
                .addOnSuccessListener(snapshot -> {
                    List<OrderItem> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot)
                        list.add(doc.toObject(OrderItem.class));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }
}