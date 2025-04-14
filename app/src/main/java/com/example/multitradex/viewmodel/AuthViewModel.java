    package com.example.multitradex.viewmodel;

    import android.app.Application;
    import android.util.Log;

    import androidx.annotation.NonNull;
    import androidx.lifecycle.AndroidViewModel;
    import androidx.lifecycle.LiveData;
    import androidx.lifecycle.MutableLiveData;

    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;

    public class AuthViewModel extends AndroidViewModel {

        private static final String TAG = "AuthViewModel";

        private final FirebaseAuth firebaseAuth;
        private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
        private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
        private final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();

        public AuthViewModel(@NonNull Application application) {
            super(application);
            firebaseAuth = FirebaseAuth.getInstance();
            checkIfUserLoggedIn(); // Check auth state on ViewModel creation
        }

        // ✅ Register user
        public void registerUser(String email, String password) {
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = authResult.getUser();
                            if (user != null) {
                                Log.d(TAG, "Registration successful: " + user.getUid());
                                userLiveData.postValue(user);
                                isLoggedIn.postValue(true);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Registration failed", e);
                            errorLiveData.postValue(e.getMessage());
                            isLoggedIn.postValue(false);
                        });
            } catch (Exception e) {
                Log.e(TAG, "Exception during registration", e);
                errorLiveData.postValue("Unexpected error during registration");
                isLoggedIn.postValue(false);
            }
        }

        // ✅ Login user
        public void loginUser(String email, String password) {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = authResult.getUser();
                            if (user != null) {
                                Log.d(TAG, "Login successful: " + user.getUid());
                                userLiveData.postValue(user);
                                isLoggedIn.postValue(true);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Login failed", e);
                            errorLiveData.postValue(e.getMessage());
                            isLoggedIn.postValue(false);
                        });
            } catch (Exception e) {
                Log.e(TAG, "Exception during login", e);
                errorLiveData.postValue("Unexpected error during login");
                isLoggedIn.postValue(false);
            }
        }

        // ✅ Logout user
        public void logout() {
            firebaseAuth.signOut();
            userLiveData.postValue(null);
            isLoggedIn.postValue(false);
            Log.d(TAG, "User logged out");
        }

        // ✅ Check if user is already logged in on app launch
        public void checkIfUserLoggedIn() {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                userLiveData.postValue(currentUser);
                isLoggedIn.postValue(true);
                Log.d(TAG, "User already logged in: " + currentUser.getUid());
            } else {
                isLoggedIn.postValue(false);
            }
        }

        // ✅ Exposed LiveData for observing from UI
        public LiveData<FirebaseUser> getUserLiveData() {
            return userLiveData;
        }

        public LiveData<String> getErrorLiveData() {
            return errorLiveData;
        }

        public LiveData<Boolean> getIsLoggedIn() {
            return isLoggedIn;
        }
    }
