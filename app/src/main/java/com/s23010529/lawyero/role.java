package com.s23010529.lawyero;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class role extends AppCompatActivity {

    private Button customer, lawyer;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);

        db = FirebaseFirestore.getInstance();

        // Get UID passed from signup screen
        uid = getIntent().getStringExtra("uid");

        customer = findViewById(R.id.customer);
        lawyer = findViewById(R.id.lawyer);

        customer.setOnClickListener(v -> saveRoleAndGoToLogin("customer"));
        lawyer.setOnClickListener(v -> saveRoleAndGoToLogin("lawyer"));
    }

    private void saveRoleAndGoToLogin(String role) {
        if (uid == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(uid)
                .update("role", role) // <- use update instead of set
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(role.this, "Role saved successfully", Toast.LENGTH_SHORT).show();

                    // Navigate to login screen
                    Intent intent = new Intent(role.this, login.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(role.this, "Failed to save role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
