package com.example.multitradex.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.multitradex.R;
import com.example.multitradex.viewmodel.CheckoutSharedViewModel;

public class CheckoutFragment extends Fragment {

    private EditText shippingName, shippingAddress, shippingCity, shippingPostalCode;
    private RadioGroup paymentMethodGroup;
    private RadioButton paymentCreditCard, paymentCOD, paymentPayPal, paymentGooglePay, paymentApplePay;
    private Button reviewOrderButton;

    private CheckoutSharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        shippingName = view.findViewById(R.id.shippingName);
        shippingAddress = view.findViewById(R.id.shippingAddress);
        shippingCity = view.findViewById(R.id.shippingCity);
        shippingPostalCode = view.findViewById(R.id.shippingPostalCode);
        paymentMethodGroup = view.findViewById(R.id.paymentMethodGroup);
        paymentCreditCard = view.findViewById(R.id.paymentCreditCard);
        paymentCOD = view.findViewById(R.id.paymentCOD);
        paymentPayPal = view.findViewById(R.id.paymentPayPal);
        paymentGooglePay = view.findViewById(R.id.paymentGooglePay);
        paymentApplePay = view.findViewById(R.id.paymentApplePay);
        reviewOrderButton = view.findViewById(R.id.reviewOrderButton);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(CheckoutSharedViewModel.class);

        reviewOrderButton.setOnClickListener(v -> {
            if (validateForm()) {
                String name = shippingName.getText().toString().trim();
                String address = shippingAddress.getText().toString().trim();
                String city = shippingCity.getText().toString().trim();
                String postal = shippingPostalCode.getText().toString().trim();
                String method = "";

                if (paymentCreditCard.isChecked()) method = "Credit Card";
                else if (paymentCOD.isChecked()) method = "Cash on Delivery";
                else if (paymentPayPal.isChecked()) method = "PayPal";
                else if (paymentGooglePay.isChecked()) method = "Google Pay";
                else if (paymentApplePay.isChecked()) method = "Apple Pay";

                sharedViewModel.setShippingInfo(name, address, city, postal);
                sharedViewModel.setPaymentMethod(method);

                Toast.makeText(requireContext(), "Navigating to Order Summary...", Toast.LENGTH_SHORT).show();

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new OrderSummaryFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (TextUtils.isEmpty(shippingName.getText())) {
            shippingName.setError("Name required");
            isValid = false;
        }
        if (TextUtils.isEmpty(shippingAddress.getText())) {
            shippingAddress.setError("Address required");
            isValid = false;
        }
        if (TextUtils.isEmpty(shippingCity.getText())) {
            shippingCity.setError("City required");
            isValid = false;
        }
        if (TextUtils.isEmpty(shippingPostalCode.getText())) {
            shippingPostalCode.setError("Postal Code required");
            isValid = false;
        }
        if (paymentMethodGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Please select a payment method", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }
}
