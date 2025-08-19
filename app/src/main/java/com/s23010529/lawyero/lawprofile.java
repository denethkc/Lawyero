package com.s23010529.lawyero;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class lawprofile extends AppCompatActivity {

    private EditText editName, editType, editAbout, editContact, editLocation;
    private Switch fingerprintSwitch;
    private Button btnSave, btnLocation, btnSignOut, btnDelete;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // SharedPreferences constants
    private static final String PREFS_NAME = "LawyeroPrefs";
    private static final String FINGERPRINT_ENABLED = "finger_enabled";
    private static final int MAP_REQUEST_CODE = 1001;

    // Biometric authentication
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lawprofile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        editName = findViewById(R.id.editname);
        editType = findViewById(R.id.editType);
        editAbout = findViewById(R.id.editme);
        editContact = findViewById(R.id.editcontact);
        editLocation = findViewById(R.id.editlocation);
        fingerprintSwitch = findViewById(R.id.finger);
        btnSave = findViewById(R.id.save);
        btnLocation = findViewById(R.id.chooselocation);
        btnSignOut = findViewById(R.id.signout);
        btnDelete = findViewById(R.id.delete);

        // Prevent manual typing for location
        editLocation.setFocusable(false);
        editLocation.setClickable(false);

        // Load fingerprint setting from SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        fingerprintSwitch.setChecked(prefs.getBoolean(FINGERPRINT_ENABLED, false));

        // Setup Biometric authentication
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                fingerprintSwitch.setChecked(true);
                prefs.edit().putBoolean(FINGERPRINT_ENABLED, true).apply();
                Toast.makeText(getApplicationContext(), "Fingerprint enabled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                fingerprintSwitch.setChecked(false);
                Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                fingerprintSwitch.setChecked(false);
                Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Biometric prompt configuration
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Enable Fingerprint")
                .setSubtitle("Authenticate to enable fingerprint login")
                .setNegativeButtonText("Cancel")
                .build();

        // Toggle fingerprint switch listener
        fingerprintSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Check if device supports biometrics
                BiometricManager biometricManager = BiometricManager.from(this);
                if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                        == BiometricManager.BIOMETRIC_SUCCESS) {
                    biometricPrompt.authenticate(promptInfo);
                } else {
                    fingerprintSwitch.setChecked(false);
                    Toast.makeText(this, "Biometric not supported", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Disable fingerprint login
                prefs.edit().putBoolean(FINGERPRINT_ENABLED, false).apply();
                Toast.makeText(this, "Fingerprint disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Load user profile data from Firestore
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            editName.setText(snapshot.getString("username"));
                            editType.setText(snapshot.getString("type"));
                            editAbout.setText(snapshot.getString("about"));
                            editContact.setText(snapshot.getString("contact"));
                            editLocation.setText(snapshot.getString("location"));
                            Boolean fp = snapshot.getBoolean("fingerprint_enabled");
                            fingerprintSwitch.setChecked(fp != null && fp);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
        }

        // Save profile updates to Firestore
        btnSave.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = mAuth.getCurrentUser().getUid();

            // Load location from SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String location = prefs.getString("selected_location", editLocation.getText().toString().trim());
            float lat = prefs.getFloat("selected_lat", 0.0f);
            float lng = prefs.getFloat("selected_lng", 0.0f);

            // Build user profile data
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", editName.getText().toString().trim());
            userData.put("type", editType.getText().toString().trim());
            userData.put("about", editAbout.getText().toString().trim());
            userData.put("contact", editContact.getText().toString().trim());
            userData.put("location", location);
            userData.put("latitude", lat);
            userData.put("longitude", lng);
            userData.put("fingerprint_enabled", fingerprintSwitch.isChecked());

            // Update Firestore user document
            db.collection("users").document(uid)
                    .update(userData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Open map screen for selecting location
        btnLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, map.class);
            startActivityForResult(intent, MAP_REQUEST_CODE);
        });

        // Sign out user
        btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, login.class); // Go back to login screen
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Delete account with confirmation dialog
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your account?")
                    .setPositiveButton("Yes, delete", (dialog, which) -> deleteAccount())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    /**
     * Delete user account from Firestore and FirebaseAuth
     */
    private void deleteAccount() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        // Delete Firestore user data first
        db.collection("users").document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Then delete FirebaseAuth account
                    mAuth.getCurrentUser().delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error deleting from Auth: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error deleting data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Handle result from map activity (location selection)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String location = data.getStringExtra("location");
            double latitude = data.getDoubleExtra("latitude", 0.0);
            double longitude = data.getDoubleExtra("longitude", 0.0);

            if (location != null) {
                editLocation.setText(location + "\nLat: " + latitude + ", Lng: " + longitude);
            }

            // Save location in SharedPreferences for later use
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("selected_location", location);
            editor.putFloat("selected_lat", (float) latitude);
            editor.putFloat("selected_lng", (float) longitude);
            editor.apply();
        }
    }
}
