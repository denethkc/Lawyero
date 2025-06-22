package com.s23010529.lawyero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;

public class login extends AppCompatActivity {

    private EditText umail, pass;
    private Button logIn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private String tempEmail, tempPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI
        umail = findViewById(R.id.umail);
        pass = findViewById(R.id.pass);
        logIn = findViewById(R.id.logIn);

        // Biometric setup
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                if (!TextUtils.isEmpty(tempEmail) && !TextUtils.isEmpty(tempPassword)) {
                    performLogin(tempEmail, tempPassword);
                } else {
                    Toast.makeText(login.this, "Please enter credentials first", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(login.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(login.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Login")
                .setSubtitle("Authenticate with fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        logIn.setOnClickListener(view -> validateAndLogin());
    }

    private void validateAndLogin() {
        String email = umail.getText().toString().trim();
        String password = pass.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            umail.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            umail.setError("Enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            pass.setError("Password is required");
            return;
        }

        // Check fingerprint preference
        SharedPreferences prefs = getSharedPreferences("LawyeroPrefs", MODE_PRIVATE);
        boolean isFingerprintEnabled = prefs.getBoolean("finger_enabled", false);

        if (isFingerprintEnabled) {
            BiometricManager biometricManager = BiometricManager.from(this);
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                tempEmail = email;
                tempPassword = password;
                biometricPrompt.authenticate(promptInfo);
            } else {
                Toast.makeText(this, "Biometric not supported on this device", Toast.LENGTH_SHORT).show();
            }
        } else {
            performLogin(email, password);
        }
    }

    private void performLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        navigateBasedOnRole(uid);
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            umail.setError("No account found with this email");
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            pass.setError("Incorrect password");
                        } else {
                            Toast.makeText(login.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void navigateBasedOnRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        if ("customer".equals(role)) {
                            startActivity(new Intent(login.this, cusome.class));
                        } else if ("lawyer".equals(role)) {
                            startActivity(new Intent(login.this, lawyerdash.class));
                        } else {
                            Toast.makeText(login.this, "User role not set.", Toast.LENGTH_SHORT).show();
                        }

                        finish();
                    } else {
                        Toast.makeText(login.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(login.this, "Error fetching role: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
