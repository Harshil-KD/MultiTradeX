package com.example.multitradex.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.multitradex.database.AppDatabase;
import com.example.multitradex.database.PaymentDao;
import com.example.multitradex.firebase.FirebasePaymentService;
import com.example.multitradex.models.Payment;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentRepository {
    private static final String TAG = "PaymentRepository";
    private final PaymentDao paymentDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final FirebasePaymentService firebaseService = new FirebasePaymentService();

    public PaymentRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        paymentDao = db.paymentDao();
    }

    public void insertPayment(Payment payment) {
        executor.execute(() -> paymentDao.insertPayment(payment));
        firebaseService.insertPayment(payment);
    }

    public void insertPayments(List<Payment> payments) {
        executor.execute(() -> paymentDao.insertPayments(payments));
        firebaseService.insertPayments(payments);
    }

    public void updatePayment(Payment payment) {
        executor.execute(() -> paymentDao.updatePayment(payment));
        firebaseService.updatePayment(payment);
    }

    public void deletePayment(Payment payment) {
        executor.execute(() -> paymentDao.deletePayment(payment));
        firebaseService.deletePayment(payment.getPaymentId());
    }

    public void deletePayments(List<Payment> payments) {
        executor.execute(() -> paymentDao.deletePayments(payments));
        firebaseService.deletePaymentsByField("orderId", payments.get(0).getOrderId());
    }

    public LiveData<List<Payment>> getPaymentsByUserId(String userId) {
        MutableLiveData<List<Payment>> liveData = new MutableLiveData<>();
        firebaseService.getPaymentsByField("userId", userId, new FirebasePaymentService.PaymentCallback() {
            @Override
            public void onSuccess(List<Payment> payments) {
                executor.execute(() -> {
                    paymentDao.insertPayments(payments);
                    liveData.postValue(payments);
                });
            }
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching payments", e);
            }
        });
        return liveData;
    }
}