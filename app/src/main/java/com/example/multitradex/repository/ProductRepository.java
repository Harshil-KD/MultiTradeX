package com.example.multitradex.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.multitradex.database.AppDatabase;
import com.example.multitradex.database.ProductDao;
import com.example.multitradex.firebase.FirebaseProductService;
import com.example.multitradex.models.Product;
import com.example.multitradex.utils.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductRepository {

    private static final String TAG = "ProductRepository";

    private final ProductDao productDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private final FirebaseProductService firebaseProductService;

    public ProductRepository(Context context) {
        try {
            AppDatabase db = AppDatabase.getDatabase(context);
            this.productDao = db.productDao();
            this.firebaseProductService = new FirebaseProductService();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }

    public void saveProduct(Product product) {
        try {
            executor.execute(() -> {
                try {
                    productDao.insertProduct(product);
                    Log.d(TAG, "Inserted product in Room: " + product.getId());
                } catch (Exception e) {
                    Log.e(TAG, "Error inserting product into Room", e);
                }
            });
            FirebaseProductService.saveProductToFirestore(product);
        } catch (Exception e) {
            Log.e(TAG, "Error saving product to Firestore", e);
        }
    }

    public void saveProducts(List<Product> products) {
        try {
            executor.execute(() -> {
                try {
                    productDao.insertProducts(products);
                    Log.d(TAG, "Inserted " + products.size() + " products into Room");
                } catch (Exception e) {
                    Log.e(TAG, "Error inserting product list into Room", e);
                }
            });
            FirebaseProductService.saveProductsToFirestore(products);
        } catch (Exception e) {
            Log.e(TAG, "Error saving product list to Firestore", e);
        }
    }

    public void updateProduct(Product product) {
        try {
            executor.execute(() -> {
                try {
                    productDao.updateProduct(product);
                    Log.d(TAG, "Updated product in Room: " + product.getId());
                } catch (Exception e) {
                    Log.e(TAG, "Error updating product in Room", e);
                }
            });
            FirebaseProductService.saveProductToFirestore(product);
        } catch (Exception e) {
            Log.e(TAG, "Error updating product in Firestore", e);
        }
    }

    public void deleteProduct(String productId) {
        try {
            executor.execute(() -> {
                try {
                    productDao.deleteProductById(productId);
                    Log.d(TAG, "Deleted product from Room: " + productId);
                } catch (Exception e) {
                    Log.e(TAG, "Error deleting product from Room", e);
                }
            });
            FirebaseProductService.deleteProductFromFirestore(productId);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting product from Firestore", e);
        }
    }

    public void deleteAllProducts() {
        try {
            executor.execute(() -> {
                try {
                    productDao.deleteAllProducts();
                    Log.d(TAG, "All products deleted from Room");
                } catch (Exception e) {
                    Log.e(TAG, "Error deleting all products from Room", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error deleting all products", e);
        }
    }

    public void deleteProducts(List<Product> products) {
        try {
            executor.execute(() -> {
                try {
                    productDao.deleteProducts(products);
                    Log.d(TAG, "Deleted product list from Room");
                } catch (Exception e) {
                    Log.e(TAG, "Error deleting products from Room", e);
                }
            });

            List<String> ids = new ArrayList<>();
            for (Product p : products) ids.add(p.getId());

            FirebaseProductService.deleteProductsFromFirestore(ids);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting product list from Firestore", e);
        }
    }

    public void syncProductsFromFirestoreAlways() {
        try {
            FirebaseProductService.getAllProductsFromFirestore(fetched -> {
                if (fetched != null) {
                    executor.execute(() -> {
                        try {
                            productDao.deleteAllProducts();
                            productDao.insertProducts(fetched);
                            Log.d(TAG, "Room updated from Firestore with " + fetched.size() + " products.");
                        } catch (Exception e) {
                            Log.e(TAG, "Error syncing Room from Firestore", e);
                        }
                    });
                } else {
                    Log.w(TAG, "Firestore returned null or empty during sync.");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception during syncProductsFromFirestoreAlways", e);
        }
    }

    public void syncProductsFromFirestoreIfNeeded() {
        try {
            executor.execute(() -> {
                try {
                    List<Product> local = productDao.getAllProductsOnce();
                    if (local == null || local.isEmpty()) {
                        Log.d(TAG, "Room is empty. Syncing from Firestore...");
                        syncProductsFromFirestoreAlways();
                    } else {
                        Log.d(TAG, "Room has " + local.size() + " products. Syncing to update...");
                        syncProductsFromFirestoreAlways();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error checking or syncing Room", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception during syncProductsFromFirestoreIfNeeded", e);
        }
    }

    public LiveData<List<Product>> getAllProducts() {
        try {
            return productDao.getAllProductsLive();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all products", e);
            return null;
        }
    }

    public Product getProductByIdSync(String productId) {
        return productDao.getProductById(productId);
    }

    public LiveData<List<Product>> getProductsBySeller(String sellerId) {
        try {
            return productDao.getProductsBySellerLive(sellerId);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching products by seller", e);
            return null;
        }
    }

    public LiveData<List<Product>> getProductsBySellerAndCategory(String sellerId, String category) {
        try {
            return productDao.getProductsBySellerAndCategoryLive(sellerId, category);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching products by seller and category", e);
            return null;
        }
    }

    public LiveData<List<Product>> getProductsByCategory(String category) {
        return new LiveDataAdapter(() -> {
            try {
                return productDao.getProductsByCategory(category);
            } catch (Exception e) {
                Log.e(TAG, "Error fetching products by category", e);
                return null;
            }
        });
    }

    public LiveData<List<Product>> getProductsByRetailPriceRange(double min, double max) {
        return new LiveDataAdapter(() -> {
            try {
                return productDao.getProductsByRetailPriceRange(min, max);
            } catch (Exception e) {
                Log.e(TAG, "Error fetching products by price range", e);
                return null;
            }
        });
    }

    public LiveData<List<Product>> getProductsByName(String name) {
        return new LiveDataAdapter(() -> {
            try {
                return productDao.getProductsByName(name);
            } catch (Exception e) {
                Log.e(TAG, "Error fetching products by name", e);
                return null;
            }
        });
    }

    public LiveData<List<Product>> getProductsByAvailability(String availableFor) {
        return new LiveDataAdapter(() -> {
            try {
                return productDao.getProductsByAvailability(availableFor);
            } catch (Exception e) {
                Log.e(TAG, "Error fetching products by availability", e);
                return null;
            }
        });
    }

    public LiveData<List<Product>> getProductsByIds(List<String> ids) {
        return productDao.getProductsByIds(ids);
    }

    public void fetchProductsByIdsFromFirebase(List<String> ids, ProductFetchCallback callback) {
        firebaseProductService.fetchProductsByIds(ids, new FirebaseProductService.OnProductsFetchedListener() {
            @Override
            public void onSuccess(List<Product> products) {
                executor.execute(() -> {
                    productDao.insertProducts(products); // Save to Room
                    callback.onFetchComplete();
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFetchFailed(e);
            }
        });
    }

    private static class LiveDataAdapter extends LiveData<List<Product>> {
        LiveDataAdapter(QueryRunner queryRunner) {
            ExecutorService exec = Executors.newSingleThreadExecutor();
            exec.execute(() -> {
                try {
                    postValue(queryRunner.run());
                } catch (Exception e) {
                    Log.e(TAG, "Error loading LiveData from Room query", e);
                    postValue(null);
                }
            });
        }
    }

    public void listenToRealtimeProducts() {
        FirebaseProductService.listenToProducts(new ValueEventListener<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                if (debounceRunnable != null) {
                    handler.removeCallbacks(debounceRunnable);
                }

                debounceRunnable = () -> executor.execute(() -> {
                    try {
                        productDao.insertProducts(products);
                        Log.d(TAG, "Room updated with real-time Firestore data.");
                    } catch (Exception e) {
                        Log.e(TAG, "Error inserting real-time products", e);
                    }
                });

                handler.postDelayed(debounceRunnable, 500); // Debounce delay: 500ms
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Real-time sync failed: " + error);
            }
        });
    }

    private interface QueryRunner {
        List<Product> run();
    }

    public interface ProductFetchCallback {
        void onFetchComplete();
        void onFetchFailed(Exception e);
    }
}