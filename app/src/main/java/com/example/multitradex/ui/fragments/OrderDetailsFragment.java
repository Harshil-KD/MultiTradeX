package com.example.multitradex.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.multitradex.R;
import com.example.multitradex.adapters.OrderItemAdapter;
import com.example.multitradex.models.Order;
import com.example.multitradex.utils.DateUtils;
import com.example.multitradex.viewmodel.OrderItemViewModel;
import com.example.multitradex.viewmodel.OrderViewModel;
import com.google.android.material.appbar.MaterialToolbar;

public class OrderDetailsFragment extends Fragment {

    private TextView textOrderId, textOrderDate, textTotalAmount, textPaymentStatus, textShippingInfo;
    private RecyclerView recyclerOrderItems;

    private OrderViewModel orderViewModel;
    private OrderItemViewModel orderItemViewModel;

    private String orderId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textOrderId = view.findViewById(R.id.textOrderId);
        textOrderDate = view.findViewById(R.id.textOrderDate);
        textTotalAmount = view.findViewById(R.id.textTotalAmount);
        textPaymentStatus = view.findViewById(R.id.textPaymentStatus);
        textShippingInfo = view.findViewById(R.id.textShippingInfo);
        recyclerOrderItems = view.findViewById(R.id.recyclerOrderItems);

        orderId = getArguments() != null ? getArguments().getString("orderId") : null;
        if (orderId == null) {
            Toast.makeText(requireContext(), "Order ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        orderItemViewModel = new ViewModelProvider(this).get(OrderItemViewModel.class);

        loadOrderDetails();
        loadOrderItems();

        MaterialToolbar toolbar = view.findViewById(R.id.orderDetailsToolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());


    }

    private void loadOrderDetails() {
        orderViewModel.getAllOrders().observe(getViewLifecycleOwner(), orders -> {
            for (Order order : orders) {
                if (order.getOrderId().equals(orderId)) {
                    textOrderId.setText("Order ID: " + order.getOrderId());
                    textOrderDate.setText("Date: " + DateUtils.formatTimestamp(order.getTimestamp()));
                    textTotalAmount.setText("Total: $" + String.format("%.2f", order.getTotalAmount()));
                    textPaymentStatus.setText("Status: " + order.getPaymentStatus());

                    String shipping = order.getShippingName() + ", " + order.getShippingAddress() + ", " +
                            order.getCity() + " - " + order.getPostalCode();
                    textShippingInfo.setText("Shipping To: " + shipping);
                    break;
                }
            }
        });
    }

    private void loadOrderItems() {
        orderItemViewModel.getOrderItemsByOrderId(orderId).observe(getViewLifecycleOwner(), items -> {
            recyclerOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerOrderItems.setAdapter(new OrderItemAdapter(requireContext(), items));
        });
    }
}
