package com.s23010529.lawyero;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class cusProfile extends AppCompatActivity {

    private EditText userNameEditText, emailEditText;
    private Switch fingerprintSwitch;
    private Button btnSave, btnSignOut, btnDelete, btnAdmin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cus_profile);

        // UI references
        userNameEditText = findViewById(R.id.userName);
        emailEditText = findViewById(R.id.email);
        fingerprintSwitch = findViewById(R.id.switch1);
        btnSave = findViewById(R.id.save);
        btnSignOut = findViewById(R.id.signout);
        btnDelete = findViewById(R.id.delete);
        btnAdmin = findViewById(R.id.admin);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            loadUserData();
        }

        // Save button updates Firestore
        btnSave.setOnClickListener(v -> {
            String newName = userNameEditText.getText().toString().trim();
            String newEmail = emailEditText.getText().toString().trim();

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Name and Email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("username", newName);
            updates.put("email", newEmail);

            db.collection("users").document(uid)
                    .update(updates)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Sign out
        btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, login.class));
            finish();
        });

        // Delete account with confirmation
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your account?")
                    .setPositiveButton("Yes, delete", (dialog, which) -> deleteAccount())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Contact admin button (optional implementation)
        btnAdmin.setOnClickListener(v ->
                Toast.makeText(this, "Contacting admin...", Toast.LENGTH_SHORT).show());

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, cusome.class));
                return true;
            } else if (id == R.id.nav_ai) {
                startActivity(new Intent(this, askAI.class));
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, cusMap.class));
                return true;
            }
            return id == R.id.nav_profile;
        });
    }

    private void loadUserData() {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        userNameEditText.setText(snapshot.getString("username"));
                        emailEditText.setText(snapshot.getString("email"));
                        // Optional: fingerprint state
                        Boolean finger = snapshot.getBoolean("fingerprint_enabled");
                        fingerprintSwitch.setChecked(finger != null && finger);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(uid)
                .delete()
                .addOnSuccessListener(unused -> {
                    user.delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Auth deletion failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firestore deletion failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
