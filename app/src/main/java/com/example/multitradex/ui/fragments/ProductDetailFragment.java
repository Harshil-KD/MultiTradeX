package com.example.multitradex.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.multitradex.R;
import com.example.multitradex.adapters.ImageCarouselAdapter;
import com.example.multitradex.enums.ProductDetailMode;
import com.example.multitradex.models.Business;
import com.example.multitradex.models.Product;
import com.example.multitradex.ui.dialogs.ConfirmDeleteDialogFragment;
import com.example.multitradex.ui.dialogs.EditProductDialogFragment;
import com.example.multitradex.utils.AppConstants;
import com.example.multitradex.viewmodel.ProductViewModel;
import com.example.multitradex.viewmodel.UserViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import com.example.multitradex.models.CartItem;
import com.example.multitradex.viewmodel.CartViewModel;
import com.google.android.material.snackbar.Snackbar;


public class ProductDetailFragment extends Fragment implements EditProductDialogFragment.OnProductUpdatedListener {

    private static final String TAG = "ProductDetailFragment";

    private final ProductDetailMode mode;
    private Product selectedProduct;
    private ProductViewModel productViewModel;
    private UserViewModel userViewModel;
    private CartViewModel cartViewModel;

    private String currentUserId;

    private ViewPager2 imageCarouselViewPager;
    private TextView nameTextView, categoryTextView, descriptionTextView,
            availableForTextView, sellerBusinessTextView, stockTextView,
            priceTextView, staticRetailPriceText, staticWholesalePriceText;

    private NumberPicker quantityPicker;
    private Button addToCart, reviewButton, editButton, deleteButton;
    private View quantitySection, staticPriceLayout;

    public ProductDetailFragment(Product product, ProductDetailMode mode) {
        this.selectedProduct = product;
        this.mode = mode;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);
        bindViews(view);

        SharedPreferences prefs = requireContext().getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getString(AppConstants.KEY_USER_ID, "");

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);


        populateProductDetails();
        setupQuantityPicker();
        setupButtonActions();

        return view;
    }

    private void bindViews(View view) {
        imageCarouselViewPager = view.findViewById(R.id.imageCarouselViewPager);
        nameTextView = view.findViewById(R.id.productNameTextView);
        categoryTextView = view.findViewById(R.id.categoryTextView);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        availableForTextView = view.findViewById(R.id.availableForTextView);
        sellerBusinessTextView = view.findViewById(R.id.sellerBusinessNameTextView);
        stockTextView = view.findViewById(R.id.stockTextView);
        quantityPicker = view.findViewById(R.id.quantityPicker);
        priceTextView = view.findViewById(R.id.priceTextView);
        addToCart = view.findViewById(R.id.addToCart);
        reviewButton = view.findViewById(R.id.reviewButton);
        editButton = view.findViewById(R.id.editProductButton);
        deleteButton = view.findViewById(R.id.deleteProductButton);
        quantitySection = view.findViewById(R.id.quantitySection);
        staticPriceLayout = view.findViewById(R.id.staticPriceLayout);
        staticRetailPriceText = view.findViewById(R.id.staticRetailPriceText);
        staticWholesalePriceText = view.findViewById(R.id.staticWholesalePriceText);
    }

    private void populateProductDetails() {
        try {
            nameTextView.setText(selectedProduct.getName());
            categoryTextView.setText("Category: " + selectedProduct.getCategory());
            descriptionTextView.setText(selectedProduct.getDescription());
            availableForTextView.setText("Available for: " + selectedProduct.getAvailableFor());
            stockTextView.setText("In Stock: " + selectedProduct.getStock());

            // Load seller's business name
            userViewModel.getBusinessByUserId(selectedProduct.getSellerId()).observe(getViewLifecycleOwner(), business -> {
                if (business != null) {
                    sellerBusinessTextView.setText("Sold by: " + business.getBusinessName());
                } else {
                    sellerBusinessTextView.setText("Sold by: Unknown Business");
                }
            });

            setupImageCarousel();

            updatePrice(quantityPicker.getValue());

            if (mode == ProductDetailMode.SELLER_VIEW) {
                renderSellerView();
            } else {
                renderBuyerView();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error populating product details", e);
        }
    }

    private void setupQuantityPicker() {
        try {
            int stock = selectedProduct.getStock();
            if (stock <= 0) {
                quantityPicker.setMinValue(0);
                quantityPicker.setMaxValue(0);
                quantityPicker.setValue(0);
                quantityPicker.setEnabled(false);
                addToCart.setEnabled(false);
                Toast.makeText(getContext(), "Out of stock", Toast.LENGTH_SHORT).show();
            } else {
                quantityPicker.setEnabled(true);
                quantityPicker.setMinValue(1);
                quantityPicker.setMaxValue(stock);
                quantityPicker.setValue(1);
                quantityPicker.setOnValueChangedListener((picker, oldVal, newVal) -> updatePrice(newVal));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up quantity picker", e);
        }
    }

    private void updatePrice(int quantity) {
        try {
            double retailPrice = selectedProduct.getPriceRetail();
            String wholesaleJson = selectedProduct.getPriceWholesale();
            double unitPrice = retailPrice;

            if (quantity > 1 && wholesaleJson != null && !wholesaleJson.isEmpty()) {
                JSONObject json = new JSONObject(wholesaleJson);
                if (quantity <= 5 && json.has("1-5")) unitPrice = json.getDouble("1-5");
                else if (quantity <= 10 && json.has("6-10")) unitPrice = json.getDouble("6-10");
                else if (json.has("11+")) unitPrice = json.getDouble("11+");
            }

            double total = unitPrice * quantity;
            priceTextView.setText("Price: $" + String.format("%.2f", total));

        } catch (Exception e) {
            Log.e(TAG, "Error updating price", e);
        }
    }

    private void setupImageCarousel() {
        try {
            List<String> imageUrls = selectedProduct.getImageUrls();
            ImageCarouselAdapter adapter = new ImageCarouselAdapter(requireContext(), imageUrls);
            imageCarouselViewPager.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up image carousel", e);
        }
    }

    private void setupButtonActions() {
        addToCart.setOnClickListener(v -> {
            int qty = quantityPicker.getValue();
            int availableStock = selectedProduct.getStock();

            if (qty > availableStock) {
                Toast.makeText(getContext(), "Quantity exceeds stock", Toast.LENGTH_SHORT).show();
                return;
            }

            cartViewModel.getCartItems(currentUserId).observe(getViewLifecycleOwner(), cartItems -> {
                boolean alreadyExists = false;

                for (CartItem item : cartItems) {
                    if (item.getProductId().equals(selectedProduct.getId())) {
                        alreadyExists = true;
                        break;
                    }
                }

                if (alreadyExists) {
                    showSnackbar("Product already in cart");
                } else {
                    List<String> imageUrls = selectedProduct.getImageUrls();
                    String imageUrl = imageUrls.isEmpty() ? "" : imageUrls.get(0);

                    double unitPrice = selectedProduct.getPriceRetail();
                    try {
                        String wholesaleJson = selectedProduct.getPriceWholesale();
                        if (qty > 1 && wholesaleJson != null && !wholesaleJson.isEmpty()) {
                            JSONObject json = new JSONObject(wholesaleJson);
                            if (qty <= 5 && json.has("1-5")) unitPrice = json.getDouble("1-5");
                            else if (qty <= 10 && json.has("6-10")) unitPrice = json.getDouble("6-10");
                            else if (json.has("11+")) unitPrice = json.getDouble("11+");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error reading wholesale pricing", e);
                    }

                    String cartItemId = "cart_" + java.util.UUID.randomUUID().toString().substring(0, 10);

                    CartItem cartItem = new CartItem(
                            "", // ID will be generated by Firestore
                            currentUserId,
                            selectedProduct.getId(),
                            selectedProduct.getName(),
                            imageUrl,
                            unitPrice,
                            qty,
                            System.currentTimeMillis()
                    );


                    cartViewModel.insertCartItem(cartItem);
                    showSnackbar("Added to cart successfully");
                }
            });
        });



        reviewButton.setOnClickListener(v -> Toast.makeText(getContext(), "Navigate to review", Toast.LENGTH_SHORT).show());

        editButton.setOnClickListener(v -> {
            EditProductDialogFragment dialog = EditProductDialogFragment.newInstance(selectedProduct, updatedProduct -> {
                productViewModel.updateProduct(updatedProduct);
                selectedProduct = updatedProduct;
                populateProductDetails();
                Toast.makeText(getContext(), "Product updated", Toast.LENGTH_SHORT).show();
            });
            dialog.show(getParentFragmentManager(), "EditProductDialog");
        });

        deleteButton.setOnClickListener(v -> {
            new ConfirmDeleteDialogFragment(() -> {
                productViewModel.deleteProduct(selectedProduct.getId());
                Toast.makeText(getContext(), "Product deleted", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Product deleted: " + selectedProduct.getId());
                requireActivity().onBackPressed();
            }).show(getParentFragmentManager(), "ConfirmDeleteDialog");
        });
    }

    private void renderSellerView() {
        animateVisibility(editButton, true);
        animateVisibility(deleteButton, true);
        animateVisibility(addToCart, false);
        animateVisibility(reviewButton, false);
        animateVisibility(quantitySection, false);
        animateVisibility(sellerBusinessTextView, false);
        animateVisibility(priceTextView, false);

        staticPriceLayout.setVisibility(View.VISIBLE);
        staticRetailPriceText.setText("Retail Price: $" + String.format("%.2f", selectedProduct.getPriceRetail()));

        try {
            JSONObject json = new JSONObject(selectedProduct.getPriceWholesale());
            staticWholesalePriceText.setText("Wholesale:\n 1-5: $" + json.optString("1-5", "N/A")
                    + "\n 6-10: $" + json.optString("6-10", "N/A")
                    + "\n 11+: $" + json.optString("11+", "N/A"));
        } catch (JSONException e) {
            staticWholesalePriceText.setText("Wholesale: Unavailable");
        }
    }

    private void renderBuyerView() {
        animateVisibility(editButton, false);
        animateVisibility(deleteButton, false);
        animateVisibility(addToCart, true);
        animateVisibility(reviewButton, true);
    }

    private void animateVisibility(View view, boolean show) {
        view.animate().alpha(show ? 1f : 0f).setDuration(200).withEndAction(() -> {
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        }).start();
    }

    @Override
    public void onProductUpdated(Product updatedProduct) {
        this.selectedProduct = updatedProduct;
        populateProductDetails();
    }

    private void showSnackbar(String message) {
        View rootView = requireActivity().findViewById(android.R.id.content);
        if (rootView != null) {
            com.google.android.material.snackbar.Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getResources().getColor(R.color.purple_500))
                    .setTextColor(getResources().getColor(android.R.color.white))
                    .show();
        }
    }

}
