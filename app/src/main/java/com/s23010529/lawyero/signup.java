package com.s23010529.lawyero;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText, cpasswordEditText;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Link UI elements
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        cpasswordEditText = findViewById(R.id.cpassword);
        signUpButton = findViewById(R.id.signUp);

        // Set click listener for signup button
        signUpButton.setOnClickListener(v -> validateAndRegister());
    }

    /**
     * Validates input fields and creates a new Firebase user
     */
    private void validateAndRegister() {
        String name = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = cpasswordEditText.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(name)) {
            usernameEditText.setError("Username is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            cpasswordEditText.setError("Passwords do not match");
            return;
        }

        // Create Firebase user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // Save user data in Firestore
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("username", name);
                        userMap.put("email", email);

                        db.collection("users").document(uid)
                                .set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(signup.this, "Registered successfully", Toast.LENGTH_SHORT).show();

                                    // Navigate to role selection screen
                                    Intent intent = new Intent(signup.this, role.class);
                                    intent.putExtra("uid", uid);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(signup.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        // Handle registration errors
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            emailEditText.setError("Email already registered");
                        } else {
                            Toast.makeText(signup.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
