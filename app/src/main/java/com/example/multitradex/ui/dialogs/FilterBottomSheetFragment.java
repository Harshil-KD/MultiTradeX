package com.example.multitradex.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.multitradex.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Set;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    public interface OnFilterAppliedListener {
        void onFilterApplied(String search, String minPrice, String maxPrice, String category, String availability);
    }

    private OnFilterAppliedListener listener;

    private EditText searchEditText, minPriceEditText, maxPriceEditText;
    private ChipGroup categoryChipGroup, availabilityChipGroup;
    private Button applyFiltersButton;

    private Set<String> categoryOptions;

    public FilterBottomSheetFragment(Set<String> categoryOptions) {
        this.categoryOptions = categoryOptions;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        searchEditText = view.findViewById(R.id.searchEditText);
        minPriceEditText = view.findViewById(R.id.minPriceEditText);
        maxPriceEditText = view.findViewById(R.id.maxPriceEditText);
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        availabilityChipGroup = view.findViewById(R.id.availabilityChipGroup);
        applyFiltersButton = view.findViewById(R.id.applyFiltersButton);

        setupCategoryChips();
        setupAvailabilityChips();

        applyFiltersButton.setOnClickListener(v -> {
            String search = searchEditText.getText().toString().trim();
            String minPrice = minPriceEditText.getText().toString().trim();
            String maxPrice = maxPriceEditText.getText().toString().trim();
            String category = getSelectedChipText(categoryChipGroup);
            String availability = getSelectedChipText(availabilityChipGroup);
            if (listener != null) {
                listener.onFilterApplied(search, minPrice, maxPrice, category, availability);
            }
            dismiss();
        });

        return view;
    }

    public void setListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }


    private void setupCategoryChips() {
        categoryChipGroup.removeAllViews();
        for (String cat : categoryOptions) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_selector);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_selector));
            categoryChipGroup.addView(chip);
        }
    }

    private void setupAvailabilityChips() {
        String[] availability = {"All", "B2B", "B2C", "Both"};
        for (String option : availability) {
            Chip chip = new Chip(requireContext());
            chip.setText(option);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_selector);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_selector));
            availabilityChipGroup.addView(chip);
        }
    }

    private String getSelectedChipText(ChipGroup group) {
        int id = group.getCheckedChipId();
        if (id == View.NO_ID) return "All";
        Chip chip = group.findViewById(id);
        return chip != null ? chip.getText().toString() : "All";
    }
}