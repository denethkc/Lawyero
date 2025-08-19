package com.s23010529.lawyero;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class role extends AppCompatActivity {

    private Button customer, lawyer;         // Buttons to select role
    private FirebaseFirestore db;            // Firestore instance
    private String uid;                      // Current user's UID passed from signup

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);

        db = FirebaseFirestore.getInstance();

        // Get UID passed from signup activity
        uid = getIntent().getStringExtra("uid");

        // Initialize buttons
        customer = findViewById(R.id.customer);
        lawyer = findViewById(R.id.lawyer);

        // Set click listeners for role selection
        customer.setOnClickListener(v -> saveRoleAndGoToLogin("customer"));
        lawyer.setOnClickListener(v -> saveRoleAndGoToLogin("lawyer"));
    }

    private void saveRoleAndGoToLogin(String role) {
        if (uid == null) {
            // Safety check in case UID is missing
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update user's role in Firestore (update keeps other fields intact)
        db.collection("users").document(uid)
                .update("role", role)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(role.this, "Role saved successfully", Toast.LENGTH_SHORT).show();

                    // Navigate to login screen after saving role
                    Intent intent = new Intent(role.this, login.class);
                    startActivity(intent);
                    finish(); // Close current activity
                })
                .addOnFailureListener(e -> {
                    // Handle Firestore update failure
                    Toast.makeText(role.this, "Failed to save role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
