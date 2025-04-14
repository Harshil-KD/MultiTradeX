package com.example.multitradex.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multitradex.R;
import com.example.multitradex.adapters.CartAdapter;
import com.example.multitradex.models.CartItem;
import com.example.multitradex.utils.AppConstants;
import com.example.multitradex.viewmodel.CartViewModel;
import com.example.multitradex.viewmodel.CheckoutSharedViewModel;

import java.util.List;

public class OrderSummaryFragment extends Fragment {

    private CartViewModel cartViewModel;
    private CheckoutSharedViewModel sharedViewModel;
    private RecyclerView orderSummaryRecyclerView;
    private TextView shippingDetailsTextView, paymentMethodTextView, orderTotalTextView;
    private Button confirmOrderButton;
    private CartAdapter adapter;
    private String currentUserId;
    private List<CartItem> currentCartItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderSummaryRecyclerView = view.findViewById(R.id.orderSummaryRecyclerView);
        shippingDetailsTextView = view.findViewById(R.id.shippingDetailsTextView);
        paymentMethodTextView = view.findViewById(R.id.paymentMethodTextView);
        orderTotalTextView = view.findViewById(R.id.orderTotalTextView);
        confirmOrderButton = view.findViewById(R.id.confirmOrderButton);

        SharedPreferences prefs = requireContext().getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getString(AppConstants.KEY_USER_ID, "");

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CheckoutSharedViewModel.class);

        setupRecyclerView();
        observeCartItems();
        loadCheckoutDetails();
        setupConfirmButton();
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(requireContext(), new CartAdapter.CartInteractionListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQty) { /* no-op in summary */ }
            @Override
            public void onRemoveItem(CartItem item) { /* no-op in summary */ }
        });
        orderSummaryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderSummaryRecyclerView.setAdapter(adapter);
    }

    private void observeCartItems() {
        cartViewModel.getCartItems(currentUserId).observe(getViewLifecycleOwner(), cartItems -> {
            currentCartItems = cartItems;
            adapter.setCartItemList(cartItems);
            updateTotal(cartItems);
        });
    }

    private void updateTotal(List<CartItem> items) {
        double total = 0;
        for (CartItem item : items) {
            total += item.getUnitPrice() * item.getQuantity();
        }
        orderTotalTextView.setText("Total: $" + String.format("%.2f", total));
    }

    private void loadCheckoutDetails() {
        sharedViewModel.getShippingName().observe(getViewLifecycleOwner(), name -> {
            String address = sharedViewModel.getShippingAddress().getValue();
            String city = sharedViewModel.getShippingCity().getValue();
            String postal = sharedViewModel.getShippingPostalCode().getValue();

            String formatted = name + ", " + address + ", " + city + " - " + postal;
            shippingDetailsTextView.setText("Shipping: " + formatted);
        });

        sharedViewModel.getPaymentMethod().observe(getViewLifecycleOwner(), method -> {
            paymentMethodTextView.setText("Payment Method: " + method);
        });
    }

    private void setupConfirmButton() {
        confirmOrderButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new PaymentFragment())
                    .addToBackStack(null)
                    .commit();
        });

    }
}
