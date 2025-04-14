package com.example.multitradex.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.multitradex.R;
import com.example.multitradex.models.Business;
import com.example.multitradex.models.User;
import com.example.multitradex.viewmodel.AuthViewModel;
import com.example.multitradex.viewmodel.UserViewModel;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText nameEditText, emailEditText, passwordEditText;
    private EditText businessNameEditText, gstNumberEditText, addressEditText;
    private Spinner businessTypeSpinner;
    private RadioGroup roleGroup;
    private LinearLayout businessLayout;
    private Button registerButton, backToLoginButton;

    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;

    private String selectedRole = "individual";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        roleGroup = findViewById(R.id.roleRadioGroup);
        businessLayout = findViewById(R.id.businessDetailsLayout);

        businessNameEditText = findViewById(R.id.businessNameEditText);
        businessTypeSpinner = findViewById(R.id.businessTypeSpinner);
        gstNumberEditText = findViewById(R.id.gstNumberEditText);
        addressEditText = findViewById(R.id.addressEditText);

        registerButton = findViewById(R.id.registerButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);

        // Init ViewModels
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Handle role change UI
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.retailerRadio || checkedId == R.id.wholesalerRadio) {
                businessLayout.setVisibility(View.VISIBLE);
            } else {
                businessLayout.setVisibility(View.GONE);
            }

            selectedRole = checkedId == R.id.individualRadio ? "individual"
                    : checkedId == R.id.retailerRadio ? "retailer" : "wholesaler";
        });

        // Register click
        registerButton.setOnClickListener(v -> {
            try {
                String name = nameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (roleGroup.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
                    return;
                }

                if ((selectedRole.equals("retailer") || selectedRole.equals("wholesaler"))
                        && (businessNameEditText.getText().toString().trim().isEmpty()
                        || gstNumberEditText.getText().toString().trim().isEmpty()
                        || addressEditText.getText().toString().trim().isEmpty())) {
                    Toast.makeText(this, "Please fill all business details", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "Registering user: " + email + " | Role: " + selectedRole);
                authViewModel.registerUser(email, password);

            } catch (Exception e) {
                Log.e(TAG, "Error triggering registration", e);
                Toast.makeText(this, "Unexpected error. Try again.", Toast.LENGTH_SHORT).show();
            }
        });

        // Registration success listener
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                try {
                    String uid = firebaseUser.getUid();
                    String name = nameEditText.getText().toString().trim();
                    String email = emailEditText.getText().toString().trim();

                    // ✅ Save user to Firestore + Room via ViewModel
                    User user = new User(uid, name, email, selectedRole);
                    userViewModel.saveUser(user);
                    Log.d(TAG, "User saved: " + uid);

                    // ✅ Save business only if applicable
                    if (selectedRole.equals("retailer") || selectedRole.equals("wholesaler")) {
                        String businessName = businessNameEditText.getText().toString().trim();
                        String businessType = businessTypeSpinner.getSelectedItem().toString();
                        String gstNumber = gstNumberEditText.getText().toString().trim();
                        String address = addressEditText.getText().toString().trim();

                        Business business = new Business(uid, businessName, businessType, gstNumber, address);
                        userViewModel.saveBusiness(business);
                        Log.d(TAG, "Business saved for user: " + uid);
                    }

                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    finish(); // back to login

                } catch (Exception e) {
                    Log.e(TAG, "Post-registration error", e);
                    Toast.makeText(this, "Error after registration", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Auth error listener
        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Log.e(TAG, "Registration failed: " + error);
                Toast.makeText(this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
            }
        });

        // Back to login
        backToLoginButton.setOnClickListener(v -> finish());
    }
}
