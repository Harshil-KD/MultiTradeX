package com.example.multitradex.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CheckoutSharedViewModel extends ViewModel {

    // Shipping Info
    private final MutableLiveData<String> shippingName = new MutableLiveData<>();
    private final MutableLiveData<String> shippingAddress = new MutableLiveData<>();
    private final MutableLiveData<String> shippingCity = new MutableLiveData<>();
    private final MutableLiveData<String> shippingPostalCode = new MutableLiveData<>();

    // Payment Method
    private final MutableLiveData<String> paymentMethod = new MutableLiveData<>();

    public void setShippingInfo(String name, String address, String city, String postalCode) {
        shippingName.setValue(name);
        shippingAddress.setValue(address);
        shippingCity.setValue(city);
        shippingPostalCode.setValue(postalCode);
    }

    public void setPaymentMethod(String method) {
        paymentMethod.setValue(method);
    }

    public LiveData<String> getShippingName() { return shippingName; }
    public LiveData<String> getShippingAddress() { return shippingAddress; }
    public LiveData<String> getShippingCity() { return shippingCity; }
    public LiveData<String> getShippingPostalCode() { return shippingPostalCode; }
    public LiveData<String> getPaymentMethod() { return paymentMethod; }

    public Map<String, String> getFullCheckoutInfo() {
        Map<String, String> data = new HashMap<>();
        data.put("shippingName", shippingName.getValue());
        data.put("shippingAddress", shippingAddress.getValue());
        data.put("shippingCity", shippingCity.getValue());
        data.put("shippingPostalCode", shippingPostalCode.getValue());
        data.put("paymentMethod", paymentMethod.getValue());
        return data;
    }

    public void clearAll() {
        shippingName.setValue(null);
        shippingAddress.setValue(null);
        shippingCity.setValue(null);
        shippingPostalCode.setValue(null);
        paymentMethod.setValue(null);
    }
}
