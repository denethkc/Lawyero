package com.s23010529.lawyero;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class lawyerdash extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView userTextView, typeTextView;
    private Button addButton;
    private ImageButton profileButton;
    private LinearLayout reminderContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lawyerdash);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI
        userTextView = findViewById(R.id.user);
        typeTextView = findViewById(R.id.type);
        addButton = findViewById(R.id.add);
        profileButton = findViewById(R.id.profile);
        reminderContainer = findViewById(R.id.reminderContainer);  // the LinearLayout inside ScrollView

        String uid = mAuth.getCurrentUser().getUid();

        // Load lawyer profile
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        userTextView.setText(snapshot.getString("username"));
                        typeTextView.setText(snapshot.getString("type"));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                );

        // Load reminders from Firestore
        db.collection("reminders")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    reminderContainer.removeAllViews(); // clear old

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String date = doc.getString("date");
                        String time = doc.getString("time");
                        String detail = doc.getString("detail");

                        TextView reminderView = new TextView(this);
                        reminderView.setText("📅 " + date + " 🕒 " + time + "\n" + detail);
                        reminderView.setTextSize(16f);
                        reminderView.setPadding(30, 30, 30, 30);
                        reminderView.setTextColor(getResources().getColor(android.R.color.black));
                        reminderView.setBackgroundResource(R.drawable.box);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(20, 20, 20, 20);
                        reminderView.setLayoutParams(params);

                        reminderContainer.addView(reminderView);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading reminders", Toast.LENGTH_SHORT).show()
                );

        // Button: add reminder
        addButton.setOnClickListener(v ->
                startActivity(new Intent(this, lawAppoinment.class)));

        // Button: profile
        profileButton.setOnClickListener(v ->
                startActivity(new Intent(this, lawprofile.class)));
    }
}
