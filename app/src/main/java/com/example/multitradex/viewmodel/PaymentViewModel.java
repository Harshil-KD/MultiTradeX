package com.example.multitradex.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.multitradex.models.Payment;
import com.example.multitradex.repository.PaymentRepository;

import java.util.List;

public class PaymentViewModel extends AndroidViewModel {
    private static final String TAG = "PaymentViewModel";
    private final PaymentRepository repository;
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public PaymentViewModel(@NonNull Application application) {
        super(application);
        repository = new PaymentRepository(application);
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public void insertPayment(Payment payment) {
        repository.insertPayment(payment);
        statusMessage.postValue("Payment inserted");
    }

    public void insertPayments(List<Payment> payments) {
        repository.insertPayments(payments);
        statusMessage.postValue("Payments inserted");
    }

    public void updatePayment(Payment payment) {
        repository.updatePayment(payment);
        statusMessage.postValue("Payment updated");
    }

    public void deletePayment(Payment payment) {
        repository.deletePayment(payment);
        statusMessage.postValue("Payment deleted");
    }

    public void deletePayments(List<Payment> payments) {
        repository.deletePayments(payments);
        statusMessage.postValue("Payments deleted");
    }

    public LiveData<List<Payment>> getPaymentsByUserId(String userId) {
        return repository.getPaymentsByUserId(userId);
    }
}