package com.example.multitradex.firebase;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.multitradex.models.Product;
import com.example.multitradex.utils.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FirebaseProductService {

    private static final String TAG = "FirebaseProductService";
    private static final String COLLECTION_NAME = "products";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final CollectionReference productRef = db.collection(COLLECTION_NAME);

    public static ListenerRegistration listenToAllProducts(OnProductListFetchCallback callback) {
        return productRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e(TAG, "Real-time listener error", error);
                callback.onProductListFetched(null);
                return;
            }

            List<Product> productList = new ArrayList<>();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots) {
                    try {
                        productList.add(doc.toObject(Product.class));
                    } catch (Exception e) {
                        Log.e(TAG, "Error converting document to Product", e);
                    }
                }
                Log.d(TAG, "Realtime update received: " + productList.size() + " products");
            }

            callback.onProductListFetched(productList);
        });
    }

    public static void getProductsPaginated(int limit, @Nullable DocumentSnapshot lastDoc, OnProductPaginationCallback callback) {
        try {
            Query query = productRef.orderBy("name").limit(limit);
            if (lastDoc != null) {
                query = query.startAfter(lastDoc);
            }

            query.get().addOnSuccessListener(snapshot -> {
                List<Product> productList = new ArrayList<>();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    try {
                        productList.add(doc.toObject(Product.class));
                    } catch (Exception e) {
                        Log.e(TAG, "Error converting document to Product", e);
                    }
                }
                DocumentSnapshot lastVisible = snapshot.isEmpty() ? null : snapshot.getDocuments().get(snapshot.size() - 1);

                Log.d(TAG, "Paginated fetch: " + productList.size() + " products");
                callback.onPaginatedResult(productList, lastVisible);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error in paginated fetch", e);
                callback.onPaginatedResult(null, null);
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception in getProductsPaginated", e);
            callback.onPaginatedResult(null, null);
        }
    }

    public static void saveProductToFirestore(Product product) {
        try {
            if (product == null || product.getId() == null) {
                Log.e(TAG, "Product or Product ID is null. Cannot save.");
                return;
            }

            productRef.document(product.getId())
                    .set(product)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Product saved/updated in Firestore: " + product.getId()))
                    .addOnFailureListener(e -> Log.e(TAG, "Error saving product to Firestore", e));

        } catch (Exception e) {
            Log.e(TAG, "Exception in saveProductToFirestore", e);
        }
    }

    public static void saveProductsToFirestore(List<Product> productList) {
        try {
            if (productList == null || productList.isEmpty()) {
                Log.e(TAG, "Product list is empty. Skipping batch insert.");
                return;
            }

            for (Product product : productList) {
                saveProductToFirestore(product);
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception in saveProductsToFirestore", e);
        }
    }

    public static void deleteProductFromFirestore(String productId) {
        try {
            productRef.document(productId)
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Product deleted from Firestore: " + productId))
                    .addOnFailureListener(e -> Log.e(TAG, "Error deleting product from Firestore", e));

        } catch (Exception e) {
            Log.e(TAG, "Exception in deleteProductFromFirestore", e);
        }
    }

    public static void deleteProductsFromFirestore(List<String> productIds) {
        try {
            if (productIds == null || productIds.isEmpty()) {
                Log.e(TAG, "No product IDs provided for deletion.");
                return;
            }

            for (String productId : productIds) {
                deleteProductFromFirestore(productId);
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception in deleteProductsFromFirestore", e);
        }
    }

    public static void getAllProductsFromFirestore(OnProductListFetchCallback callback) {
        try {
            productRef.get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Product> productList = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            try {
                                productList.add(doc.toObject(Product.class));
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document to Product", e);
                            }
                        }
                        Log.d(TAG, "Fetched all products: " + productList.size());
                        callback.onProductListFetched(productList);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching all products", e);
                        callback.onProductListFetched(null);
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception in getAllProductsFromFirestore", e);
            callback.onProductListFetched(null);
        }
    }

    public void fetchProductsByIds(List<String> productIds, OnProductsFetchedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereIn("id", productIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            products.add(product);
                        }
                    }
                    listener.onSuccess(products);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseProductService", "Error fetching products by IDs", e);
                    listener.onFailure(e);
                });
    }

    public interface OnProductsFetchedListener {
        void onSuccess(List<Product> products);
        void onFailure(Exception e);
    }


    public static void listenToProducts(ValueEventListener<List<Product>> listener) {
        db.collection(COLLECTION_NAME)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        listener.onError(e != null ? e.getMessage() : "Unknown error");
                        return;
                    }

                    List<Product> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) list.add(p);
                    }

                    listener.onSuccess(list);
                });
    }


    public interface OnProductListFetchCallback {
        void onProductListFetched(List<Product> products);
    }

    public interface OnProductPaginationCallback {
        void onPaginatedResult(List<Product> products, @Nullable DocumentSnapshot lastVisible);
    }
}