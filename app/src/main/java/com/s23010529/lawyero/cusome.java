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

    private TextView greeting;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView askImage, findImage, appointmentImage;
    private LinearLayout lawyerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cusome);

        greeting = findViewById(R.id.greeting);
        askImage = findViewById(R.id.ask);
        findImage = findViewById(R.id.find);
        appointmentImage = findViewById(R.id.appoinment); // ⬅️ new line
        lawyerContainer = findViewById(R.id.lawyerContainer);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Show greeting from Firestore
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.getString("username");
                    greeting.setText((username != null && !username.isEmpty()) ? "Hello, " + username : "Hello!");
                })
                .addOnFailureListener(e -> {
                    greeting.setText("Hello!");
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                });

        // 🔁 Click listeners
        askImage.setOnClickListener(v -> startActivity(new Intent(this, askAI.class)));
        findImage.setOnClickListener(v -> startActivity(new Intent(this, find.class)));

        // 🔥 Navigate to cusAppoinment page
        appointmentImage.setOnClickListener(v -> startActivity(new Intent(this, cusAppoinment.class)));

        // 🔥 Load featured lawyers
        loadLawyers();

        // ⛳ Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // mark as selected

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_ai) {
                startActivity(new Intent(this, askAI.class));
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(this, cusMap.class));
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
                    lawyerContainer.removeAllViews();
                    for (DocumentSnapshot doc : query) {
                        String name = doc.getString("username");
                        String type = doc.getString("type");
                        String about = doc.getString("about");

                        if (name != null && type != null && about != null) {
                            addLawyerCard(name, type, about);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading lawyers", Toast.LENGTH_SHORT).show());
    }

    private void addLawyerCard(String name, String type, String about) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_lawyer_card, lawyerContainer, false);

        TextView nameView = card.findViewById(R.id.nameText);
        TextView typeView = card.findViewById(R.id.typeText);
        TextView aboutView = card.findViewById(R.id.aboutText);

        nameView.setText("👨‍⚖️ Name: " + name);
        typeView.setText("📂 Type: " + type);
        aboutView.setText("📝 About: " + about);

        lawyerContainer.addView(card);
    }
}
