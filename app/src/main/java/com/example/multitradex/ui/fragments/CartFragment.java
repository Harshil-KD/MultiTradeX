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

import java.util.List;

public class CartFragment extends Fragment {

    private CartViewModel cartViewModel;
    private CartAdapter adapter;
    private RecyclerView cartRecyclerView;
    private TextView totalPriceTextView;
    private Button checkoutButton;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        cartRecyclerView = view.findViewById(R.id.cartRecyclerView);
        totalPriceTextView = view.findViewById(R.id.totalPriceTextView);
        checkoutButton = view.findViewById(R.id.checkoutButton);

        SharedPreferences prefs = requireContext().getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getString(AppConstants.KEY_USER_ID, "");

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        setupRecyclerView();
        observeCartItems();
        setupCheckout();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(requireContext(), new CartAdapter.CartInteractionListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQty) {
                item.setQuantity(newQty);
                cartViewModel.updateCartItem(item);
            }

            @Override
            public void onRemoveItem(CartItem item) {
                cartViewModel.deleteCartItem(item);
            }
        });

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartRecyclerView.setAdapter(adapter);
    }

    private void observeCartItems() {
        cartViewModel.getCartItems(currentUserId).observe(getViewLifecycleOwner(), cartItems -> {
            adapter.setCartItemList(cartItems);
            updateTotal(cartItems);
        });
    }

    private void updateTotal(List<CartItem> items) {
        double total = 0;
        for (CartItem item : items) {
            total += item.getUnitPrice() * item.getQuantity();
        }
        totalPriceTextView.setText("Total: $" + String.format("%.2f", total));
    }

    private void setupCheckout() {
        checkoutButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Proceeding to Checkout", Toast.LENGTH_SHORT).show();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new CheckoutFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

}
