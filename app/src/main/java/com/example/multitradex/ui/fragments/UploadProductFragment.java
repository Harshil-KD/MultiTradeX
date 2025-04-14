package com.example.multitradex.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.multitradex.R;
import com.example.multitradex.models.Product;
import com.example.multitradex.network.ImgurApiService;
import com.example.multitradex.network.RetrofitClient;
import com.example.multitradex.repository.ProductRepository;
import com.example.multitradex.utils.AppConstants;
import com.example.multitradex.utils.FileUtils;
import com.example.multitradex.utils.ImgurConstants;
import com.google.gson.Gson;

import java.io.File;
import java.util.*;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadProductFragment extends Fragment {

    private static final String TAG = "UploadProductFragment";
    private static final int PICK_IMAGE_REQUEST = 1001;

    // UI
    private EditText productNameEditText, productDescriptionEditText, productStockEditText, productRetailPriceEditText;
    private EditText price1to5EditText, price6to10EditText, price11plusEditText;
    private EditText productCategoryEditText;
    private Spinner availableForSpinner;
    private Button uploadImageButton, submitProductButton;
    private ProgressBar uploadProgressBar;

    private final List<String> uploadedImageUrls = new ArrayList<>();
    private String currentUserId;
    private ProductRepository productRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_product, container, false);

        // UI Init
        productNameEditText = view.findViewById(R.id.productNameEditText);
        productDescriptionEditText = view.findViewById(R.id.productDescriptionEditText);
        productStockEditText = view.findViewById(R.id.productStockEditText);
        productRetailPriceEditText = view.findViewById(R.id.productRetailPriceEditText);
        productCategoryEditText = view.findViewById(R.id.productCategoryEditText);
        availableForSpinner = view.findViewById(R.id.availableForSpinner);
        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        submitProductButton = view.findViewById(R.id.submitProductButton);
        uploadProgressBar = view.findViewById(R.id.uploadProgressBar);
        price1to5EditText = view.findViewById(R.id.price1to5EditText);
        price6to10EditText = view.findViewById(R.id.price6to10EditText);
        price11plusEditText = view.findViewById(R.id.price11plusEditText);

        // Setup
        SharedPreferences preferences = requireContext().getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        currentUserId = preferences.getString(AppConstants.KEY_USER_ID, "unknown_user");

        productRepository = new ProductRepository(requireContext());

        // Upload Image
        uploadImageButton.setOnClickListener(v -> openImagePicker());

        // Submit Product
        submitProductButton.setOnClickListener(v -> {
            try {
                submitProductButton.setEnabled(false);
                uploadProgressBar.setVisibility(View.VISIBLE);

                // Input values
                String name = productNameEditText.getText().toString().trim();
                String description = productDescriptionEditText.getText().toString().trim();
                String category = productCategoryEditText.getText().toString().trim();
                String stockStr = productStockEditText.getText().toString().trim();
                String retailPriceStr = productRetailPriceEditText.getText().toString().trim();
                Object availableObj = availableForSpinner.getSelectedItem();

                if (category.isEmpty() || availableObj == null) {
                    Toast.makeText(getContext(), "Please enter category and select availability", Toast.LENGTH_SHORT).show();
                    resetProgress();
                    return;
                }

                String availableFor = availableObj.toString();

                if (name.isEmpty() || description.isEmpty() || stockStr.isEmpty() || retailPriceStr.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    resetProgress();
                    return;
                }

                if (uploadedImageUrls.isEmpty()) {
                    Toast.makeText(getContext(), "Please upload at least one product image", Toast.LENGTH_SHORT).show();
                    resetProgress();
                    return;
                }

                int stock = Integer.parseInt(stockStr);
                double retailPrice = Double.parseDouble(retailPriceStr);

                Map<String, Double> wholesalePricing = new HashMap<>();
                if (!price1to5EditText.getText().toString().trim().isEmpty())
                    wholesalePricing.put("1-5", Double.parseDouble(price1to5EditText.getText().toString().trim()));
                if (!price6to10EditText.getText().toString().trim().isEmpty())
                    wholesalePricing.put("6-10", Double.parseDouble(price6to10EditText.getText().toString().trim()));
                if (!price11plusEditText.getText().toString().trim().isEmpty())
                    wholesalePricing.put("11+", Double.parseDouble(price11plusEditText.getText().toString().trim()));

                String productId = UUID.randomUUID().toString();

                Product product = new Product(
                        productId,
                        name,
                        description,
                        category,
                        new Gson().toJson(uploadedImageUrls),
                        retailPrice,
                        new Gson().toJson(wholesalePricing),
                        stock,
                        availableFor,
                        currentUserId
                );

                productRepository.saveProduct(product);
                Toast.makeText(getContext(), "Product published successfully!", Toast.LENGTH_SHORT).show();
                clearForm();

            } catch (Exception e) {
                Log.e(TAG, "Error publishing product", e);
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                resetProgress();
            }
        });

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    uploadImageToImgur(imageUri);
                }
            } else if (data.getData() != null) {
                uploadImageToImgur(data.getData());
            }
        }
    }

    private void uploadImageToImgur(Uri imageUri) {
        try {
            File imageFile = FileUtils.createTempFileFromUri(requireContext(), imageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

            ImgurApiService api = RetrofitClient.getClient().create(ImgurApiService.class);
            Call<ResponseBody> call = api.uploadImage("Client-ID " + ImgurConstants.CLIENT_ID, body);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String json = response.body().string();
                            String link = extractImageUrl(json);
                            uploadedImageUrls.add(link);
                            Log.d(TAG, "Image uploaded to Imgur: " + link);
                            Toast.makeText(getContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Upload failed: " + response.errorBody().string());
                            Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Exception parsing Imgur response", e);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e(TAG, "Upload error", t);
                    Toast.makeText(getContext(), "Upload error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Image upload failed", e);
            Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractImageUrl(String json) {
        int start = json.indexOf("\"link\":\"") + 8;
        int end = json.indexOf("\"", start);
        return json.substring(start, end).replace("\\/", "/");
    }

    private void clearForm() {
        productNameEditText.setText("");
        productDescriptionEditText.setText("");
        productStockEditText.setText("");
        productRetailPriceEditText.setText("");
        productCategoryEditText.setText("");
        price1to5EditText.setText("");
        price6to10EditText.setText("");
        price11plusEditText.setText("");
        availableForSpinner.setSelection(0);
        uploadedImageUrls.clear();
    }

    private void resetProgress() {
        uploadProgressBar.setVisibility(View.GONE);
        submitProductButton.setEnabled(true);
    }
}