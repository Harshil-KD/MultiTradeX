package com.example.multitradex.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.multitradex.R;
import com.example.multitradex.models.User;
import com.example.multitradex.utils.AppConstants;
import com.example.multitradex.viewmodel.AuthViewModel;
import com.example.multitradex.viewmodel.UserViewModel;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signupText, forgotPasswordText;

    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init Views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupText = findViewById(R.id.signupText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        // Init ViewModels
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Login Button Click
        loginButton.setOnClickListener(v -> {
            try {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                authViewModel.loginUser(email, password);
                Log.d(TAG, "Login attempt with email: " + email);

            } catch (Exception e) {
                Log.e(TAG, "Login error", e);
                Toast.makeText(this, "Unexpected error occurred", Toast.LENGTH_SHORT).show();
            }
        });

        // Register Click
        signupText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // ✅ Login Success Observer
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                String uid = firebaseUser.getUid();
                Log.d(TAG, "Login successful. UID: " + uid);

                // ✅ Sync user from Firestore → Room
                userViewModel.startRealtimeSync();

                // ✅ Observe user from Room after sync
                userViewModel.getUser(uid).observe(this, user -> {
                    if (user != null) {
                        Log.d(TAG, "User loaded: " + user.getUserId() + " | " + user.getName());

                        String role = user.getRole();
                        if (role == null || role.isEmpty()) {
                            Toast.makeText(this, "User role not set. Contact support.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // ✅ Save user session in SharedPreferences
                        SharedPreferences preferences = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(AppConstants.KEY_USER_ID, user.getUserId());
                        editor.putString(AppConstants.KEY_USER_NAME, user.getName());
                        editor.putString(AppConstants.KEY_USER_EMAIL, user.getEmail());
                        editor.putString(AppConstants.KEY_USER_ROLE, role);
                        editor.apply();

                        // ✅ Navigate to dashboard
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        intent.putExtra("userRole", role.toLowerCase());
                        startActivity(intent);
                        finish();

                    } else {
                        Log.e(TAG, "User record not found in Room (sync failed)");
                        Toast.makeText(this, "User data not found!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // Login Error Observer
        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Log.e(TAG, "Firebase Auth Error: " + error);
                Toast.makeText(this, "Login failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
