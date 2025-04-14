package com.example.multitradex.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.multitradex.R;
import com.example.multitradex.models.Business;
import com.example.multitradex.models.User;
import com.example.multitradex.viewmodel.UserViewModel;

public class EditBusinessProfileDialogFragment extends DialogFragment {

    private EditText nameEditText, gstEditText, addressEditText;
    private AutoCompleteTextView typeDropdown;
    private Button saveButton, cancelButton;
    private UserViewModel userViewModel;
    private Business currentBusiness;

    public static EditBusinessProfileDialogFragment newInstance(Business business) {
        EditBusinessProfileDialogFragment fragment = new EditBusinessProfileDialogFragment();
        Bundle args = new Bundle();
        args.putString("businessId", business.getBusinessId());
        args.putString("userId", business.getUserId());
        args.putString("name", business.getBusinessName());
        args.putString("type", business.getBusinessType());
        args.putString("gst", business.getGstNumber());
        args.putString("address", business.getAddress());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.fragment_edit_business_profile_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameEditText = view.findViewById(R.id.editBusinessName);
        typeDropdown = view.findViewById(R.id.editBusinessType);
        gstEditText = view.findViewById(R.id.editGstNumber);
        addressEditText = view.findViewById(R.id.editBusinessAddress);
        saveButton = view.findViewById(R.id.saveBusinessProfileButton);
        cancelButton = view.findViewById(R.id.cancelBusinessProfileButton);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Set dropdown options from string-array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.business_types,
                android.R.layout.simple_dropdown_item_1line
        );
        typeDropdown.setAdapter(adapter);
        typeDropdown.setThreshold(1);

        // force dropdown show on click
        typeDropdown.setFocusable(false);
        typeDropdown.setFocusableInTouchMode(false);
        typeDropdown.setOnClickListener(v -> typeDropdown.showDropDown());

        if (getArguments() != null) {
            String userId = getArguments().getString("userId");
            String name = getArguments().getString("name");
            String type = getArguments().getString("type"); // e.g., "Wholesaler"
            String gst = getArguments().getString("gst");
            String address = getArguments().getString("address");

            currentBusiness = new Business(userId, name, type, gst, address);
            currentBusiness.setBusinessId(getArguments().getString("businessId"));

            nameEditText.setText(name);
            gstEditText.setText(gst);
            addressEditText.setText(address);

            if (!TextUtils.isEmpty(type)) {
                // ✨ Sets the dropdown value properly from Firestore or Room
                typeDropdown.setText(capitalize(type), false);
            }
        }


        cancelButton.setOnClickListener(v -> dismiss());

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String type = typeDropdown.getText().toString().trim();
            String gst = gstEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(type)) {
                Toast.makeText(getContext(), "Business name and type are required", Toast.LENGTH_SHORT).show();
                return;
            }

            currentBusiness.setBusinessName(name);
            currentBusiness.setBusinessType(type);
            currentBusiness.setGstNumber(gst);
            currentBusiness.setAddress(address);

            // Save updated business info
            userViewModel.saveBusiness(currentBusiness);

            // Update role in user object immediately and save
            User updatedUser = new User(
                    currentBusiness.getUserId(),
                    "", // name is unchanged, will be overridden
                    "", // email is unchanged, will be overridden
                    type.toLowerCase()
            );

            // Instead of observing, get the user once and override only role
            userViewModel.getUser(currentBusiness.getUserId()).observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    updatedUser.setName(user.getName());
                    updatedUser.setEmail(user.getEmail());
                    userViewModel.saveUser(updatedUser);
                }
            });

            // role will also be updated in user model:
            userViewModel.getUser(currentBusiness.getUserId()).observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    user.setRole(type.toLowerCase());
                    userViewModel.saveUser(user);
                }
            });

            Toast.makeText(getContext(), "Business profile updated", Toast.LENGTH_SHORT).show();
            view.postDelayed(this::dismiss, 500);
        });

    }

    private String capitalize(String text) {
        if (TextUtils.isEmpty(text)) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}