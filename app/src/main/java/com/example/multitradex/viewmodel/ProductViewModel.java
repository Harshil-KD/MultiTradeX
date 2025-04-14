package com.example.multitradex.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.multitradex.models.Product;
import com.example.multitradex.repository.ProductRepository;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {

    private static final String TAG = "ProductViewModel";
    private final ProductRepository repository;

    // ✅ LiveData for status feedback
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);

        // 🔁 Start listening to Firestore real-time updates
        repository.listenToRealtimeProducts();
    }

    public void syncFirestoreToRoom() {
        try {
            repository.syncProductsFromFirestoreAlways();
            statusMessage.postValue("Synced Firestore to Room");
        } catch (Exception e) {
            Log.e(TAG, "Error syncing Firestore to Room", e);
            statusMessage.postValue("Sync failed: " + e.getMessage());
        }
    }

    public void syncIfRoomIsEmpty() {
        try {
            repository.syncProductsFromFirestoreIfNeeded();
            statusMessage.postValue("Synced if Room was empty");
        } catch (Exception e) {
            Log.e(TAG, "Error checking Room and syncing if empty", e);
            statusMessage.postValue("Conditional sync failed");
        }
    }

    public void saveProduct(Product product) {
        try {
            repository.saveProduct(product);
            statusMessage.postValue("Product saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving product", e);
            statusMessage.postValue("Failed to save product");
        }
    }

    public void saveProducts(List<Product> products) {
        try {
            repository.saveProducts(products);
            statusMessage.postValue("Products saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving product list", e);
            statusMessage.postValue("Failed to save product list");
        }
    }

    public void updateProduct(Product product) {
        try {
            repository.updateProduct(product);
            statusMessage.postValue("Product updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating product", e);
            statusMessage.postValue("Failed to update product");
        }
    }

    public void deleteProduct(String productId) {
        try {
            repository.deleteProduct(productId);
            statusMessage.postValue("Product deleted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error deleting product", e);
            statusMessage.postValue("Failed to delete product");
        }
    }

    public void deleteAllProducts() {
        try {
            repository.deleteAllProducts();
            statusMessage.postValue("All products deleted");
        } catch (Exception e) {
            Log.e(TAG, "Error deleting all products", e);
            statusMessage.postValue("Failed to delete all products");
        }
    }

    public void deleteProducts(List<Product> products) {
        try {
            repository.deleteProducts(products);
            statusMessage.postValue("Products deleted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error deleting product list", e);
            statusMessage.postValue("Failed to delete product list");
        }
    }

    public LiveData<List<Product>> getAllProducts() {
        try {
            return repository.getAllProducts();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all products", e);
            statusMessage.postValue("Error fetching all products");
            return null;
        }
    }

    public Product getProductByIdSync(String productId) {
        return repository.getProductByIdSync(productId); // from Room
    }


    public LiveData<List<Product>> getProductsBySeller(String sellerId) {
        try {
            return repository.getProductsBySeller(sellerId);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching products by seller", e);
            statusMessage.postValue("Error fetching products by seller");
            return null;
        }
    }

    public LiveData<List<Product>> getProductsBySellerAndCategory(String sellerId, String category) {
        try {
            return repository.getProductsBySellerAndCategory(sellerId, category);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching products by seller and category", e);
            statusMessage.postValue("Error fetching by seller/category");
            return null;
        }
    }

    public LiveData<List<Product>> getProductsByCategory(String category) {
        try {
            return repository.getProductsByCategory(category);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching products by category", e);
            statusMessage.postValue("Error fetching by category");
            return null;
        }
    }

    public LiveData<List<Product>> getProductsByName(String name) {
        try {
            return repository.getProductsByName(name);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching products by name", e);
            statusMessage.postValue("Error fetching by name");
            return null;
        }
    }

    public LiveData<List<Product>> getProductsByAvailability(String availableFor) {
        try {
            return repository.getProductsByAvailability(availableFor);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching products by availability", e);
            statusMessage.postValue("Error fetching by availability");
            return null;
        }
    }

    public LiveData<List<Product>> getProductsByRetailPriceRange(double minPrice, double maxPrice) {
        try {
            return repository.getProductsByRetailPriceRange(minPrice, maxPrice);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching products by price range", e);
            statusMessage.postValue("Error fetching by price range");
            return null;
        }
    }

    public LiveData<List<Product>> getProductsByIds(List<String> ids) {
        return repository.getProductsByIds(ids);
    }

    public void fetchProductsByIdsFromFirestore(List<String> ids, ProductRepository.ProductFetchCallback callback) {
        repository.fetchProductsByIdsFromFirebase(ids, callback);
    }


    public void startRealtimeSync() {
        repository.listenToRealtimeProducts();
    }

}
