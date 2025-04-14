package com.example.multitradex.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multitradex.R;
import com.example.multitradex.adapters.ProductAdapter;
import com.example.multitradex.enums.ProductDetailMode;
import com.example.multitradex.models.Product;
import com.example.multitradex.utils.AppConstants;
import com.example.multitradex.viewmodel.ProductViewModel;

import java.util.ArrayList;

public class MyProductsFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "MyProductsFragment";

    private ProductViewModel productViewModel;
    private ProductAdapter productAdapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView noProductsText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_products, container, false);

        recyclerView = view.findViewById(R.id.myProductsRecyclerView);
        progressBar = view.findViewById(R.id.myProductsProgressBar);
        noProductsText = view.findViewById(R.id.noProductsTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        productAdapter = new ProductAdapter(requireContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(productAdapter);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        SharedPreferences prefs = requireContext().getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        String currentUserId = prefs.getString(AppConstants.KEY_USER_ID, null);

        productViewModel.syncFirestoreToRoom();

        Log.d(TAG, "Current User ID from SharedPreferences: " + currentUserId);

        if (currentUserId != null && !currentUserId.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            productViewModel.getProductsBySeller(currentUserId).observe(getViewLifecycleOwner(), products -> {
                progressBar.setVisibility(View.GONE);

                if (products != null) {
                    Log.d(TAG, "Fetched " + products.size() + " products for user ID: " + currentUserId);
                } else {
                    Log.w(TAG, "Fetched null product list for user ID: " + currentUserId);
                }

                if (products != null && !products.isEmpty()) {
                    noProductsText.setVisibility(View.GONE);
                    productAdapter.setProductList(products);
                } else {
                    noProductsText.setVisibility(View.VISIBLE);
                    productAdapter.setProductList(new ArrayList<>());
                }
            });
        } else {
            Log.e(TAG, "No user ID found in SharedPreferences");
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @Override
    public void onProductClick(Product product) {
        if (product != null) {
            Log.d(TAG, "Clicked on product: " + product.getName());
            ProductDetailFragment fragment = new ProductDetailFragment(product, ProductDetailMode.SELLER_VIEW);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

}
