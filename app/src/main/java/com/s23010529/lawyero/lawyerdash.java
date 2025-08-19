package com.s23010529.lawyero;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class lawyerdash extends AppCompatActivity {

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI elements
    private TextView userTextView, typeTextView;
    private Button addButton;
    private LinearLayout reviewContainer;
    private ImageButton profileButton;
    private LinearLayout reminderContainer;

    private static final String TAG = "LawyerDash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lawyerdash);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        userTextView = findViewById(R.id.user);
        typeTextView = findViewById(R.id.type);
        addButton = findViewById(R.id.add);
        reviewContainer = findViewById(R.id.reviewContainer);
        profileButton = findViewById(R.id.profile);
        reminderContainer = findViewById(R.id.reminderContainer);

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        //  Load Lawyer Profile
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String username = snapshot.getString("username");
                        String type = snapshot.getString("type");

                        userTextView.setText(username != null ? username : "No Name");
                        typeTextView.setText(type != null ? type : "No Type");

                        Log.d(TAG, "Profile loaded: " + snapshot.getData());
                    } else {
                        Log.w(TAG, "Profile document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading profile", e);
                });

        // Load Reminders
        db.collection("reminders")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    reminderContainer.removeAllViews();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String date = doc.getString("date");
                        String time = doc.getString("time");
                        String detail = doc.getString("detail");

                        // Create a TextView for each reminder
                        TextView reminderView = new TextView(this);
                        reminderView.setText("📅 " + (date != null ? date : "N/A") +
                                " 🕒 " + (time != null ? time : "N/A") +
                                "\n" + (detail != null ? detail : "No details"));
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

                    Log.d(TAG, "Reminders loaded: " + querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading reminders", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading reminders", e);
                });

        // Load Reviews
        db.collection("reviews")
                .whereEqualTo("lawyerId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    reviewContainer.removeAllViews();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String reviewer = doc.getString("username"); // reviewer's name
                        String comment = doc.getString("text");      // review text

                        // Create a TextView for each review
                        TextView reviewView = new TextView(this);
                        reviewView.setText("👤 " + (reviewer != null ? reviewer : "Anonymous") +
                                "\n💬 " + (comment != null ? comment : "No comment"));
                        reviewView.setTextSize(16f);
                        reviewView.setPadding(30, 30, 30, 30);
                        reviewView.setTextColor(getResources().getColor(android.R.color.black));
                        reviewView.setBackgroundResource(R.drawable.box);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(20, 20, 20, 20);
                        reviewView.setLayoutParams(params);

                        reviewContainer.addView(reviewView);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading reviews", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading reviews", e);
                });

        // Navigate to Add Reminder activity
        addButton.setOnClickListener(v -> {
            startActivity(new Intent(this, lawAppoinment.class));
        });

        // Navigate to Profile activity
        profileButton.setOnClickListener(v -> {
            startActivity(new Intent(this, lawprofile.class));
        });
    }
}
