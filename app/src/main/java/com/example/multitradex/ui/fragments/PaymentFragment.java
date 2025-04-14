package com.example.multitradex.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.multitradex.R;
import com.example.multitradex.models.*;
import com.example.multitradex.utils.AppConstants;
import com.example.multitradex.viewmodel.*;
import com.google.firebase.firestore.*;

import java.util.*;

public class PaymentFragment extends Fragment {

    private static final String TAG = "PaymentFragment";

    private TextView paymentMethodText;
    private LinearLayout creditCardSection, otherSection;
    private EditText cardNumberInput, expiryInput, cvvInput;
    private Button confirmPaymentButton;
    private ProgressBar paymentProgressBar;
    private View loadingOverlay;

    private CheckoutSharedViewModel sharedViewModel;
    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private OrderItemViewModel orderItemViewModel;
    private PaymentViewModel paymentViewModel;
    private ProductViewModel productViewModel;

    private List<CartItem> cartItems = new ArrayList<>();
    private String userId;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        paymentMethodText = view.findViewById(R.id.paymentMethodLabel);
        creditCardSection = view.findViewById(R.id.cardPaymentLayout);
        otherSection = view.findViewById(R.id.otherPaymentLayout);
        cardNumberInput = view.findViewById(R.id.inputCardNumber);
        expiryInput = view.findViewById(R.id.inputExpiry);
        cvvInput = view.findViewById(R.id.inputCVV);
        confirmPaymentButton = view.findViewById(R.id.payNowButton);
        paymentProgressBar = view.findViewById(R.id.paymentProgressBar);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(CheckoutSharedViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(requireActivity()).get(OrderViewModel.class);
        orderItemViewModel = new ViewModelProvider(requireActivity()).get(OrderItemViewModel.class);
        paymentViewModel = new ViewModelProvider(requireActivity()).get(PaymentViewModel.class);
        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        userId = requireContext()
                .getSharedPreferences(AppConstants.PREF_NAME, 0)
                .getString(AppConstants.KEY_USER_ID, "");

        setupUI();
        fetchCartItems();
        setupConfirmAction();
    }

    private void setupUI() {
        String method = sharedViewModel.getPaymentMethod().getValue();
        paymentMethodText.setText("Payment Method: " + method);

        creditCardSection.setVisibility(View.GONE);
        otherSection.setVisibility(View.GONE);

        if ("Credit Card".equalsIgnoreCase(method)) {
            creditCardSection.setVisibility(View.VISIBLE);
        } else {
            otherSection.setVisibility(View.VISIBLE);
        }
    }

    private void fetchCartItems() {
        cartViewModel.getCartItems(userId).observe(getViewLifecycleOwner(), items -> {
            cartItems.clear();
            if (items != null) {
                cartItems.addAll(items);
                confirmPaymentButton.setEnabled(true);
                confirmPaymentButton.setAlpha(1f);
            } else {
                Toast.makeText(getContext(), "No items in cart", Toast.LENGTH_SHORT).show();
                confirmPaymentButton.setEnabled(false);
                confirmPaymentButton.setAlpha(0.5f);
            }
        });
    }

    private void showLoading(boolean show) {
        paymentProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        confirmPaymentButton.setAlpha(show ? 0.5f : 1f);
        confirmPaymentButton.setEnabled(!show);
    }

    private void setupConfirmAction() {
        confirmPaymentButton.setOnClickListener(v -> {
            if (!validatePaymentForm()) return;

            showLoading(true);
            double totalAmount = calculateTotal();

            try {
                // 1. Prepare order
                DocumentReference orderRef = db.collection("orders").document();
                Order order = new Order(
                        orderRef.getId(),
                        userId,
                        totalAmount,
                        System.currentTimeMillis(),
                        "SUCCESS",
                        sharedViewModel.getShippingName().getValue(),
                        sharedViewModel.getShippingAddress().getValue(),
                        sharedViewModel.getShippingCity().getValue(),
                        sharedViewModel.getShippingPostalCode().getValue()
                );

                // 2. Prepare batch
                WriteBatch batch = db.batch();
                batch.set(orderRef, order);

                // 3. Add OrderItems
                List<OrderItem> orderItems = new ArrayList<>();
                for (CartItem item : cartItems) {
                    DocumentReference itemRef = db.collection("order_items").document();
                    String generatedId = itemRef.getId(); // pre-generate Firestore ID

                    OrderItem orderItem = new OrderItem(
                            generatedId,
                            order.getOrderId(),
                            item.getProductId(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getUnitPrice() * item.getQuantity()
                    );

                    batch.set(itemRef, orderItem);
                    orderItems.add(orderItem); // Now this ID is valid for Firestore + Room
                }


                // 4. Create Payment
                DocumentReference paymentRef = db.collection("payments").document();
                Payment payment = new Payment(
                        paymentRef.getId(),
                        order.getOrderId(),
                        userId,
                        sharedViewModel.getPaymentMethod().getValue(),
                        "SUCCESS",
                        totalAmount,
                        System.currentTimeMillis(),
                        "TXN-" + System.currentTimeMillis(),
                        null
                );
                batch.set(paymentRef, payment);

                // 5. Commit Batch
                batch.commit().addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Batch committed successfully");
                    orderViewModel.insertOrder(order);
                    paymentViewModel.insertPayment(payment);
                    orderItemViewModel.insertOrderItems(orderItems);
                    cartViewModel.clearCart(userId);
                    updateStock();
                    showLoading(false);
                    showSuccessDialog(order.getOrderId());
                }).addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Batch failed", e);
                    Toast.makeText(getContext(), "Payment failed, please try again", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                showLoading(false);
                Log.e(TAG, "Unexpected error in checkout", e);
                Toast.makeText(getContext(), "Unexpected error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStock() {
        productViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            if (products == null || products.isEmpty()) return;

            Map<String, Product> map = new HashMap<>();
            for (Product p : products) map.put(p.getId(), p);

            for (CartItem item : cartItems) {
                Product product = map.get(item.getProductId());
                if (product != null) {
                    int newStock = product.getStock() - item.getQuantity();
                    product.setStock(Math.max(newStock, 0));
                    productViewModel.updateProduct(product);
                }
            }
        });
    }

    private boolean validatePaymentForm() {
        String method = sharedViewModel.getPaymentMethod().getValue();
        if ("Credit Card".equalsIgnoreCase(method)) {
            if (TextUtils.isEmpty(cardNumberInput.getText())) {
                cardNumberInput.setError("Required");
                return false;
            }
            if (TextUtils.isEmpty(expiryInput.getText())) {
                expiryInput.setError("Required");
                return false;
            }
            if (TextUtils.isEmpty(cvvInput.getText())) {
                cvvInput.setError("Required");
                return false;
            }
        }
        return true;
    }

    private double calculateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getUnitPrice() * item.getQuantity();
        }
        return total;
    }

    private void showSuccessDialog(String orderId) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Order Confirmed")
                .setMessage("Order ID: " + orderId)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, new DashboardFragment())
                            .commit();
                }).show();
    }
}
