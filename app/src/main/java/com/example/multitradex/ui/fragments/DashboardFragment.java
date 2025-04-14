package com.example.multitradex.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multitradex.R;
import com.example.multitradex.adapters.OrderAdapter;
import com.example.multitradex.adapters.ProductAdapter;
import com.example.multitradex.enums.ProductDetailMode;
import com.example.multitradex.models.FavoriteProduct;
import com.example.multitradex.models.Order;
import com.example.multitradex.models.OrderItem;
import com.example.multitradex.models.Product;
import com.example.multitradex.utils.AppConstants;
import com.example.multitradex.viewmodel.FavoriteProductViewModel;
import com.example.multitradex.viewmodel.OrderItemViewModel;
import com.example.multitradex.viewmodel.OrderViewModel;
import com.example.multitradex.viewmodel.PaymentViewModel;
import com.example.multitradex.viewmodel.ProductViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DashboardFragment extends Fragment {

    private TextView textGreeting, textUserRole, textStatTitle1, textStatValue1, textStatValue2, textNoOrders, textFavoriteTitle;
    private RecyclerView recyclerRecentOrders, recyclerFavorites;

    private OrderViewModel orderViewModel;
    private PaymentViewModel paymentViewModel;
    private ProductViewModel productViewModel;
    private FavoriteProductViewModel favoriteViewModel;

    private String userId, userName, userRole;
    private final List<Order> userOrders = new ArrayList<>();
    private final List<Product> favoriteProducts = new ArrayList<>();
    private final Set<String> favoriteProductIds = new HashSet<>();

    private OrderAdapter orderAdapter;
    private ProductAdapter favoriteAdapter;

    public DashboardFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        getUserSession();
        setupRecyclerView();
        initViewModels();
        observeOrders();
        setupGreetingAndRole();
        observeRevenueIfSeller();
        observeFavorites();
    }

    private void initViews(View view) {
        textGreeting = view.findViewById(R.id.textGreeting);
        textUserRole = view.findViewById(R.id.textUserRole);
        textStatTitle1 = view.findViewById(R.id.textStatTitle1);
        textStatValue1 = view.findViewById(R.id.textStatValue1);
        textStatValue2 = view.findViewById(R.id.textStatValue2);
        textNoOrders = view.findViewById(R.id.textNoOrders);
        textFavoriteTitle = view.findViewById(R.id.textFavoriteTitle);
        recyclerRecentOrders = view.findViewById(R.id.recyclerRecentOrders);
        recyclerFavorites = view.findViewById(R.id.recyclerFavorites);
    }

    private void getUserSession() {
        SharedPreferences prefs = requireContext().getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        userId = prefs.getString(AppConstants.KEY_USER_ID, "");
        userName = prefs.getString(AppConstants.KEY_USER_NAME, "User");
        userRole = prefs.getString(AppConstants.KEY_USER_ROLE, "individual");
    }

    private void setupGreetingAndRole() {
        textGreeting.setText("Welcome back, " + userName + " 👋");
        textUserRole.setText("Role: " + capitalize(userRole));
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(requireContext(), userOrders, userRole, order -> {
            OrderDetailsFragment detailsFragment = new OrderDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("orderId", order.getOrderId());
            detailsFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerRecentOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerRecentOrders.setAdapter(orderAdapter);

        favoriteAdapter = new ProductAdapter(
                requireContext(),
                new ArrayList<>(),
                product -> {
                    ProductDetailFragment fragment = new ProductDetailFragment(product, ProductDetailMode.BUYER_VIEW);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit();
                },
                (product, isNowFavorite) -> {
                    FavoriteProductViewModel viewModel = new ViewModelProvider(this).get(FavoriteProductViewModel.class);
                    FavoriteProduct fav = new FavoriteProduct(userId + "_" + product.getId(), userId, product.getId());
                    if (isNowFavorite) {
                        viewModel.addFavorite(fav);
                    } else {
                        viewModel.removeFavorite(fav);
                    }
                },
                favoriteProductIds
        );

        recyclerFavorites.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerFavorites.setAdapter(favoriteAdapter);
    }

    private void initViewModels() {
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        paymentViewModel = new ViewModelProvider(this).get(PaymentViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteProductViewModel.class);
    }

    private void observeOrders() {
        orderViewModel.getOrdersByUser(userId).observe(getViewLifecycleOwner(), orders -> {
            userOrders.clear();
            if (orders != null && !orders.isEmpty()) {
                userOrders.addAll(orders);
                textNoOrders.setVisibility(View.GONE);
            } else {
                textNoOrders.setVisibility(View.VISIBLE);
            }
            textStatValue2.setText(String.valueOf(userOrders.size()));
            orderAdapter.notifyDataSetChanged();
        });
    }

    private void observeRevenueIfSeller() {
        if (userRole.equalsIgnoreCase("retailer") || userRole.equalsIgnoreCase("wholesaler")) {
            textStatTitle1.setText("Total Revenue");

            OrderItemViewModel orderItemViewModel = new ViewModelProvider(this).get(OrderItemViewModel.class);
            orderItemViewModel.getAllOrderItemsLive().observe(getViewLifecycleOwner(), orderItems -> {
                productViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
                    Map<String, Product> productMap = new HashMap<>();
                    for (Product product : products) {
                        productMap.put(product.getId(), product);
                    }
                    double totalRevenue = 0.0;
                    for (OrderItem item : orderItems) {
                        Product product = productMap.get(item.getProductId());
                        if (product != null && product.getSellerId().equals(userId)) {
                            totalRevenue += item.getSubtotal();
                        }
                    }
                    textStatValue1.setText("$" + String.format("%.2f", totalRevenue));
                });
            });
        } else {
            textStatTitle1.setText("Products Bought");
            textStatValue1.setText("N/A");
        }
    }

    private void observeFavorites() {
        favoriteViewModel.getFavoriteProductIds(userId).observe(getViewLifecycleOwner(), favoriteIds -> {
            favoriteProductIds.clear();
            favoriteProductIds.addAll(favoriteIds);

            if (favoriteIds.isEmpty()) {
                textFavoriteTitle.setVisibility(View.GONE);
                recyclerFavorites.setVisibility(View.GONE);
                favoriteAdapter.setProductList(new ArrayList<>());
                return;
            }

            productViewModel.getProductsByIds(new ArrayList<>(favoriteIds)).observe(getViewLifecycleOwner(), fetched -> {
                favoriteAdapter.setProductList(fetched);
                if (fetched != null && !fetched.isEmpty()) {
                    textFavoriteTitle.setVisibility(View.VISIBLE);
                    recyclerFavorites.setVisibility(View.VISIBLE);
                } else {
                    textFavoriteTitle.setVisibility(View.GONE);
                    recyclerFavorites.setVisibility(View.GONE);
                }
            });
        });
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}