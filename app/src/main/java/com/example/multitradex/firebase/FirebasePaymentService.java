package com.example.multitradex.firebase;

import android.util.Log;

import com.example.multitradex.models.Payment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebasePaymentService {

    private static final String TAG = "FirebasePaymentService";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference paymentRef = db.collection("payments");

    public interface PaymentCallback {
        void onSuccess(List<Payment> payments);
        void onFailure(Exception e);
    }

    public void insertPayment(Payment payment) {
        paymentRef.document(payment.getPaymentId()).set(payment)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Payment saved: " + payment.getPaymentId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving payment", e));
    }

    public void insertPayments(List<Payment> payments) {
        for (Payment payment : payments) insertPayment(payment);
    }

    public void updatePayment(Payment payment) {
        insertPayment(payment);
    }

    public void deletePayment(String paymentId) {
        paymentRef.document(paymentId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Payment deleted: " + paymentId))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting payment", e));
    }

    public void deletePaymentsByField(String field, Object value) {
        paymentRef.whereEqualTo(field, value).get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) doc.getReference().delete();
                });
    }

    public void updatePaymentStatusByField(String field, Object value, String newStatus) {
        paymentRef.whereEqualTo(field, value).get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Payment payment = doc.toObject(Payment.class);
                        payment.setPaymentStatus(newStatus);
                        doc.getReference().set(payment);
                    }
                });
    }

    public void getPaymentsByField(String field, Object value, PaymentCallback callback) {
        paymentRef.whereEqualTo(field, value).get()
                .addOnSuccessListener(snapshot -> {
                    List<Payment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot)
                        list.add(doc.toObject(Payment.class));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllPayments(PaymentCallback callback) {
        paymentRef.get()
                .addOnSuccessListener(snapshot -> {
                    List<Payment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot)
                        list.add(doc.toObject(Payment.class));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getPaymentsPaginated(int limit, DocumentSnapshot lastDoc, PaymentCallback callback) {
        Query query = paymentRef.orderBy("timestamp").limit(limit);
        if (lastDoc != null) query = query.startAfter(lastDoc);

        query.get()
                .addOnSuccessListener(snapshot -> {
                    List<Payment> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments())
                        list.add(doc.toObject(Payment.class));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }
}