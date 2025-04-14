package com.example.multitradex.firebase;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.multitradex.models.Order;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseOrderService {

    private static final String TAG = "FirebaseOrderService";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference orderRef = db.collection("orders");

    public interface OrderCallback {
        void onSuccess(List<Order> orders);
        void onFailure(Exception e);
    }

    public void insertOrder(Order order) {
        orderRef.document(order.getOrderId()).set(order)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Order saved: " + order.getOrderId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving order", e));
    }

    public void insertOrders(List<Order> orders) {
        for (Order order : orders) insertOrder(order);
    }

    public void updateOrder(Order order) {
        insertOrder(order);
    }

    public void deleteOrder(String orderId) {
        orderRef.document(orderId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Order deleted: " + orderId))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting order", e));
    }

    public void deleteOrders(List<String> orderIds) {
        for (String id : orderIds) deleteOrder(id);
    }

    public void getOrdersByField(String field, Object value, OrderCallback callback) {
        orderRef.whereEqualTo(field, value).get()
                .addOnSuccessListener(snapshot -> {
                    List<Order> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) list.add(doc.toObject(Order.class));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllOrders(OrderCallback callback) {
        orderRef.get()
                .addOnSuccessListener(snapshot -> {
                    List<Order> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) list.add(doc.toObject(Order.class));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getOrdersPaginated(int limit, @Nullable DocumentSnapshot lastDoc, OrderCallback callback) {
        Query query = orderRef.orderBy("timestamp").limit(limit);
        if (lastDoc != null) query = query.startAfter(lastDoc);

        query.get()
                .addOnSuccessListener(snapshot -> {
                    List<Order> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments())
                        list.add(doc.toObject(Order.class));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }
}