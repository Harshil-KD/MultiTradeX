package com.example.multitradex.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.multitradex.R;
import com.example.multitradex.models.Product;

import org.json.JSONObject;

public class EditProductDialogFragment extends DialogFragment {

    public interface OnProductUpdatedListener {
        void onProductUpdated(Product updatedProduct);
    }

    private Product product;
    private OnProductUpdatedListener listener;

    public static EditProductDialogFragment newInstance(Product product, OnProductUpdatedListener listener) {
        EditProductDialogFragment fragment = new EditProductDialogFragment();
        fragment.product = product;
        fragment.listener = listener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_product, null);

        EditText nameInput = dialogView.findViewById(R.id.editNameEditText);
        EditText descInput = dialogView.findViewById(R.id.editDescriptionEditText);
        EditText categoryInput = dialogView.findViewById(R.id.editCategoryEditText);
        EditText stockInput = dialogView.findViewById(R.id.editStockEditText);
        EditText retailInput = dialogView.findViewById(R.id.editRetailPriceEditText);
        EditText price1to5 = dialogView.findViewById(R.id.editWholesalePrice1to5EditText);
        EditText price6to10 = dialogView.findViewById(R.id.editWholesalePrice6to10EditText);
        EditText price11plus = dialogView.findViewById(R.id.editWholesalePrice11PlusEditText);

        // Set current values
        nameInput.setText(product.getName());
        descInput.setText(product.getDescription());
        categoryInput.setText(product.getCategory());
        stockInput.setText(String.valueOf(product.getStock()));
        retailInput.setText(String.valueOf(product.getPriceRetail()));

        try {
            JSONObject wholesaleJson = new JSONObject(product.getPriceWholesale());
            price1to5.setText(wholesaleJson.optString("1-5", ""));
            price6to10.setText(wholesaleJson.optString("6-10", ""));
            price11plus.setText(wholesaleJson.optString("11+", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Product")
                .setView(dialogView)
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button updateBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            updateBtn.setEnabled(false);

            TextWatcher watcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateBtn.setEnabled(isValid(nameInput, descInput, categoryInput, stockInput, retailInput, price1to5, price6to10, price11plus));
                }
                @Override public void afterTextChanged(Editable s) {}
            };

            nameInput.addTextChangedListener(watcher);
            descInput.addTextChangedListener(watcher);
            categoryInput.addTextChangedListener(watcher);
            stockInput.addTextChangedListener(watcher);
            retailInput.addTextChangedListener(watcher);
            price1to5.addTextChangedListener(watcher);
            price6to10.addTextChangedListener(watcher);
            price11plus.addTextChangedListener(watcher);

            updateBtn.setOnClickListener(v -> {
                try {
                    product.setName(nameInput.getText().toString().trim());
                    product.setDescription(descInput.getText().toString().trim());
                    product.setCategory(categoryInput.getText().toString().trim());
                    product.setStock(Integer.parseInt(stockInput.getText().toString().trim()));
                    product.setPriceRetail(Double.parseDouble(retailInput.getText().toString().trim()));

                    JSONObject newWholesale = new JSONObject();
                    newWholesale.put("1-5", Double.parseDouble(price1to5.getText().toString().trim()));
                    newWholesale.put("6-10", Double.parseDouble(price6to10.getText().toString().trim()));
                    newWholesale.put("11+", Double.parseDouble(price11plus.getText().toString().trim()));
                    product.setPriceWholesale(newWholesale.toString());

                    listener.onProductUpdated(product);
                    dialog.dismiss();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return dialog;
    }

    private boolean isValid(EditText... inputs) {
        for (EditText input : inputs) {
            if (input.getText().toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
