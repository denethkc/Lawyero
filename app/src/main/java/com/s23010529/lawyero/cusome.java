package com.s23010529.lawyero;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class cusome extends AppCompatActivity {

    // UI elements
    private TextView greeting;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView askImage, findImage, appointmentImage; // Home shortcuts
    private LinearLayout lawyerContainer;     // Layout to display lawyer cards

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cusome);

        // Initialize UI
        greeting = findViewById(R.id.greeting);
        askImage = findViewById(R.id.ask);
        findImage = findViewById(R.id.find);
        appointmentImage = findViewById(R.id.appoinment);
        lawyerContainer = findViewById(R.id.lawyerContainer);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // get username from Firestore and show greeting
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.getString("username");
                    greeting.setText((username != null && !username.isEmpty())
                            ? "Hello, " + username
                            : "Hello!");
                })
                .addOnFailureListener(e -> {
                    greeting.setText("Hello!");
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                });

        // 🔹 Home screen shortcuts
        askImage.setOnClickListener(v -> startActivity(new Intent(this, askAI.class)));
        findImage.setOnClickListener(v -> startActivity(new Intent(this, find.class)));
        appointmentImage.setOnClickListener(v -> startActivity(new Intent(this, cusAppoinment.class)));

        // 🔹 Load featured lawyers into the container
        loadLawyers();

        // 🔹 Bottom navigation bar setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // highlight "home"

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true; // already on home
            } else if (id == R.id.nav_ai) {
                startActivity(new Intent(this, askAI.class));
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, searchMap.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, cusProfile.class));
                return true;
            }
            return false;
        });
    }

    private void loadLawyers() {
        db.collection("users")
                .whereEqualTo("role", "lawyer")
                .get()
                .addOnSuccessListener(query -> {
                    lawyerContainer.removeAllViews(); // clear old cards
                    for (DocumentSnapshot doc : query) {
                        String id = doc.getId(); // Lawyer ID for navigation
                        String name = doc.getString("username");
                        String type = doc.getString("type");
                        String about = doc.getString("about");

                        if (name != null && type != null && about != null) {
                            addLawyerCard(id, name, type, about);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading lawyers", Toast.LENGTH_SHORT).show()
                );
    }

    private void addLawyerCard(String lawyerId, String name, String type, String about) {
        // Inflate the lawyer card layout
        View card = LayoutInflater.from(this).inflate(R.layout.item_lawyer_card, lawyerContainer, false);

        // Set card data
        TextView nameView = card.findViewById(R.id.nameText);
        TextView typeView = card.findViewById(R.id.typeText);
        TextView aboutView = card.findViewById(R.id.aboutText);

        nameView.setText("👨‍⚖️ Name: " + name);
        typeView.setText("📂 Type: " + type);
        aboutView.setText("📝 About: " + about);

        // Navigate to lawReview screen with lawyerId
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, lawReview.class);
            intent.putExtra("lawyerId", lawyerId); // pass selected lawyerId
            startActivity(intent);
        });

        // Add card to the container
        lawyerContainer.addView(card);
    }

}
