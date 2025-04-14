package com.example.multitradex.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.multitradex.R;
import com.example.multitradex.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Layout already exists

        // 🎬 Apply animations to logo and app name
        ImageView splashLogo = findViewById(R.id.splashLogo);
        TextView appNameText = findViewById(R.id.appNameText);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        splashLogo.startAnimation(fadeIn);
        appNameText.startAnimation(slideUp);

        // ⏳ Delayed redirection after SPLASH_DELAY
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // User is logged in, fetch cached role from SharedPreferences
                SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
                String role = prefs.getString(AppConstants.KEY_USER_ROLE, null);

                Log.d(TAG, "User logged in, UID: " + currentUser.getUid() + ", Cached Role: " + role);

                // Fallback to "individual" if role not cached (failsafe)
                String finalRole = role != null ? role : "individual";

                Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
                intent.putExtra("userRole", finalRole.toLowerCase());
                startActivity(intent);
            } else {
                // No Firebase session → go to login screen
                Log.d(TAG, "No user logged in");
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }

            finish(); // Always close splash screen
        }, SPLASH_DELAY);
    }
}
