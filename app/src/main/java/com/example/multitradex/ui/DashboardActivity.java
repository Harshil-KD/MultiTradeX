package com.example.multitradex.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.multitradex.R;
import com.example.multitradex.ui.fragments.BrowseFragment;
import com.example.multitradex.ui.fragments.CartFragment;
import com.example.multitradex.ui.fragments.DashboardFragment;
import com.example.multitradex.ui.fragments.MyProductsFragment;
import com.example.multitradex.ui.fragments.ProfileFragment;
import com.example.multitradex.ui.fragments.UploadProductFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private BottomNavigationView bottomNav;
    private FloatingActionButton fabUpload;

    private String userRole = "individual"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bottomNav = findViewById(R.id.bottomNav);
        fabUpload = findViewById(R.id.fabUploadProduct);

        // Get user role from Intent (passed from LoginActivity or elsewhere)
        if (getIntent() != null && getIntent().hasExtra("userRole")) {
            userRole = getIntent().getStringExtra("userRole");
            Log.d(TAG, "User Role: " + userRole);
        }

        // Set default fragment
        loadFragment(new DashboardFragment());

        // Setup navigation listener
        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Show/hide FAB and tab based on role
        if (userRole.equals("retailer") || userRole.equals("wholesaler")) {
            fabUpload.setVisibility(View.VISIBLE);
        } else {
            // Hide "My Products" tab for individual
            bottomNav.getMenu().removeItem(R.id.nav_my_products);
            fabUpload.setVisibility(View.GONE);
        }

        fabUpload.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new UploadProductFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = new DashboardFragment();
        } else if (itemId == R.id.nav_browse) {
            selectedFragment = new BrowseFragment();
        } else if (itemId == R.id.nav_my_products) {
            selectedFragment = new MyProductsFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        }else if (itemId == R.id.nav_cart) {
            selectedFragment = new CartFragment();
        }

        if (selectedFragment != null) {
            return loadFragment(selectedFragment);
        }

        return false;
    }

    private boolean loadFragment(Fragment fragment) {
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Fragment loading failed", e);
            return false;
        }
    }
}
