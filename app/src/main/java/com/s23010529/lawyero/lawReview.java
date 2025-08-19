package com.s23010529.lawyero;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class lawReview extends AppCompatActivity {


    private TextView nameView, typeView, descView, contactDetail;
    private ImageButton mapBtn, addReviewBtn;
    private LinearLayout reviewContainer;

    // Firestore database instance
    private FirebaseFirestore db;

    // Lawyer info
    private String lawyerId;
    private String lawyerName;
    private double latitude, longitude; // for maps

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_law_review);

        // Initialize UI elements
        nameView = findViewById(R.id.name);
        typeView = findViewById(R.id.type);
        descView = findViewById(R.id.description);
        contactDetail = findViewById(R.id.contactDetail);
        mapBtn = findViewById(R.id.map);
        addReviewBtn = findViewById(R.id.add);
        reviewContainer = findViewById(R.id.reviewContainer);

        db = FirebaseFirestore.getInstance();

        // Get lawyerId passed via intent
        lawyerId = getIntent().getStringExtra("lawyerId");

        // Load lawyer details and reviews if lawyerId is valid
        if (lawyerId != null) {
            loadLawyerDetails();
            loadReviews();
        }

        // Open map activity when map button is clicked
        mapBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, cusMap.class);
            intent.putExtra("lat", latitude);
            intent.putExtra("lng", longitude);
            intent.putExtra("name", lawyerName);
            startActivity(intent);
        });

        // Show popup to add a review
        addReviewBtn.setOnClickListener(v -> showReviewPopup());
    }

    /**
     * Load lawyer details from Firestore and display in UI
     */
    private void loadLawyerDetails() {
        db.collection("users").document(lawyerId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        lawyerName = doc.getString("username");
                        String type = doc.getString("type");
                        String about = doc.getString("about");

                        // Load latitude and longitude if present
                        if (doc.contains("latitude")) {
                            latitude = doc.getDouble("latitude");
                        }
                        if (doc.contains("longitude")) {
                            longitude = doc.getDouble("longitude");
                        }

                        // Parse contact info (phone, email, address)
                        String contact = doc.getString("contact");
                        String phone = "", email = "", address = "";

                        if (contact != null) {
                            String[] lines = contact.split("\n");
                            for (String line : lines) {
                                line = line.trim();
                                if (line.startsWith("Phone")) {
                                    phone = line.replace("Phone -", "").trim();
                                } else if (line.startsWith("E-mail")) {
                                    email = line.replace("E-mail -", "").trim();
                                } else if (line.startsWith("Address")) {
                                    address = line.replace("Address -", "").trim();
                                }
                            }
                        }

                        // Update UI
                        nameView.setText(lawyerName);
                        typeView.setText(type);
                        descView.setText(about);

                        // Build and display contact info
                        StringBuilder contactInfo = new StringBuilder();
                        if (!phone.isEmpty()) contactInfo.append("📞 Phone: ").append(phone).append("\n");
                        if (!email.isEmpty()) contactInfo.append("📧 Email: ").append(email).append("\n");
                        if (!address.isEmpty()) contactInfo.append("📍 Address: ").append(address);

                        contactDetail.setText(contactInfo.length() > 0 ? contactInfo.toString() : "No contact info available");
                    }
                })
                .addOnFailureListener(e -> {
                    contactDetail.setText("Failed to load details");
                });
    }

    /**
     * Load reviews for this lawyer and add to review container
     */
    private void loadReviews() {
        reviewContainer.removeAllViews(); // clear existing reviews

        db.collection("reviews")
                .whereEqualTo("lawyerId", lawyerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        String user = doc.getString("username");
                        String text = doc.getString("text");
                        addReviewCard(user, text);
                    }
                });
    }

    /**
     * Create and add a review card to the review container
     */
    private void addReviewCard(String username, String text) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_review_card, reviewContainer, false);
        TextView userView = card.findViewById(R.id.reviewUser);
        TextView textView = card.findViewById(R.id.reviewText);

        userView.setText("👤 " + username);
        textView.setText("💬 " + text);

        reviewContainer.addView(card);
    }

    /**
     * Show a popup window to allow the user to add a review
     */
    private void showReviewPopup() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_add_review, null);
        EditText reviewInput = popupView.findViewById(R.id.reviewInput);
        Button postBtn = popupView.findViewById(R.id.postBtn);

        // Create a popup window
        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        // Post button click listener
        postBtn.setOnClickListener(v -> {
            String text = reviewInput.getText().toString().trim();
            if (!text.isEmpty()) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Get current user's name from Firestore
                db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                    String username = userDoc.getString("username");

                    // Create review object
                    Map<String, Object> review = new HashMap<>();
                    review.put("lawyerId", lawyerId);
                    review.put("userId", uid);
                    review.put("username", username);
                    review.put("text", text);

                    // Save review to Firestore
                    db.collection("reviews").add(review)
                            .addOnSuccessListener(r -> {
                                Toast.makeText(this, "Review added", Toast.LENGTH_SHORT).show();
                                loadReviews(); // refresh reviews
                                popupWindow.dismiss();
                            });
                });
            }
        });

        popupWindow.showAtLocation(reviewContainer, 0, 0, 0);
    }
}
