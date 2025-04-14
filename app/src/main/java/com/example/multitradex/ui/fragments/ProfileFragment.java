package com.example.multitradex.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.multitradex.R;
import com.example.multitradex.models.Business;
import com.example.multitradex.models.User;
import com.example.multitradex.ui.LoginActivity;
import com.example.multitradex.ui.dialogs.EditBusinessProfileDialogFragment;
import com.example.multitradex.ui.dialogs.EditUserProfileDialogFragment;
import com.example.multitradex.utils.AppConstants;
import com.example.multitradex.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private TextView userNameText, userEmailText, userRoleText;
    private TextView businessNameText, businessTypeText, gstNumberText, businessAddressText;
    private View businessCard;
    private Button editUserButton, editBusinessButton, logoutButton;

    private UserViewModel userViewModel;

    private String userId;
    private String role;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // UI Components
        userNameText = view.findViewById(R.id.userNameText);
        userEmailText = view.findViewById(R.id.userEmailText);
        userRoleText = view.findViewById(R.id.userRoleText);
        editUserButton = view.findViewById(R.id.editUserButton);

        businessCard = view.findViewById(R.id.businessCard);
        businessNameText = view.findViewById(R.id.businessNameText);
        businessTypeText = view.findViewById(R.id.businessTypeText);
        gstNumberText = view.findViewById(R.id.gstNumberText);
        businessAddressText = view.findViewById(R.id.businessAddressText);
        editBusinessButton = view.findViewById(R.id.editBusinessButton);

        logoutButton = view.findViewById(R.id.logoutButton);

        // ViewModel
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Load session
        SharedPreferences preferences = requireActivity().getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        userId = preferences.getString(AppConstants.KEY_USER_ID, null);
        role = preferences.getString(AppConstants.KEY_USER_ROLE, "individual");

        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Observe user info
        userViewModel.getUser(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                userNameText.setText(user.getName());
                userEmailText.setText(user.getEmail());
                userRoleText.setText(user.getRole());

                editUserButton.setOnClickListener(v -> {
                    EditUserProfileDialogFragment dialog = EditUserProfileDialogFragment.newInstance(user);
                    dialog.show(getParentFragmentManager(), "EditUserProfileDialog");
                });
            }
        });

        // Show business section if role = retailer or wholesaler
        if (role.equals("retailer") || role.equals("wholesaler")) {
            businessCard.setVisibility(View.VISIBLE);
            userViewModel.getBusinessByUserId(userId).observe(getViewLifecycleOwner(), business -> {
                if (business != null) {
                    businessNameText.setText(business.getBusinessName());
                    businessTypeText.setText(business.getBusinessType());
                    gstNumberText.setText(business.getGstNumber());
                    businessAddressText.setText(business.getAddress());

                    editBusinessButton.setOnClickListener(v -> {
                        EditBusinessProfileDialogFragment dialog = EditBusinessProfileDialogFragment.newInstance(business);
                        dialog.show(getParentFragmentManager(), "EditBusinessProfileDialog");
                    });
                }
            });
        }

        logoutButton.setOnClickListener(v -> handleLogout());

        return view;
    }

    private void handleLogout() {
        try {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences.Editor editor = requireActivity().getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Logout failed", e);
            Toast.makeText(requireContext(), "Error during logout", Toast.LENGTH_SHORT).show();
        }
    }
}