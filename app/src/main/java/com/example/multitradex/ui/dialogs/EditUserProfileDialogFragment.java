package com.example.multitradex.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.multitradex.R;
import com.example.multitradex.models.User;
import com.example.multitradex.viewmodel.UserViewModel;

public class EditUserProfileDialogFragment extends DialogFragment {

    private EditText nameEditText;
    private Button saveButton, cancelButton;
    private UserViewModel userViewModel;
    private User currentUser;

    public static EditUserProfileDialogFragment newInstance(User user) {
        EditUserProfileDialogFragment fragment = new EditUserProfileDialogFragment();
        Bundle args = new Bundle();
        args.putString("userId", user.getUserId());
        args.putString("name", user.getName());
        args.putString("email", user.getEmail());
        args.putString("role", user.getRole());
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
        return inflater.inflate(R.layout.fragment_edit_user_profile_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameEditText = view.findViewById(R.id.editUserName);
        saveButton = view.findViewById(R.id.saveUserProfileButton);
        cancelButton = view.findViewById(R.id.cancelUserProfileButton);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        if (getArguments() != null) {
            String userId = getArguments().getString("userId");
            String name = getArguments().getString("name");
            String email = getArguments().getString("email");
            String role = getArguments().getString("role");
            currentUser = new User(userId, name, email, role);
            nameEditText.setText(name);
        }

        cancelButton.setOnClickListener(v -> dismiss());

        saveButton.setOnClickListener(v -> {
            String updatedName = nameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(updatedName)) {
                Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            currentUser.setName(updatedName);
            userViewModel.saveUser(currentUser);

            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();

            // Optional smoother close with delay (while save happens in background)
            view.postDelayed(this::dismiss, 500);
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

}
