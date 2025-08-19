package com.s23010529.lawyero;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class find extends AppCompatActivity {

    private LinearLayout lawyerContainer;  // Layout where lawyer cards will be added
    private FirebaseFirestore db;
    private SearchView searchView;         // Search bar for filtering lawyers by name
    private ImageButton filterBtn;

    private List<Lawyer> allLawyers = new ArrayList<>(); // Store all lawyers retrieved from Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);

        // Initialize UI elements
        lawyerContainer = findViewById(R.id.lawyerContainer);
        searchView = findViewById(R.id.searchView);
        filterBtn = findViewById(R.id.filterButton);
        db = FirebaseFirestore.getInstance();

        // Load lawyers from Firestore
        loadLawyers();

        // Listen for search input (filter lawyers by name in real time)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterByName(query); // Filter when user presses enter
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterByName(newText); // Filter as user types
                return true;
            }
        });

        // Open popup filter when filter button is clicked
        filterBtn.setOnClickListener(v -> showFilterPopup());
    }

    private void loadLawyers() {
        db.collection("users")
                .whereEqualTo("role", "lawyer")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allLawyers.clear(); // Clear old list
                    for (DocumentSnapshot doc : snapshot) {
                        // Get lawyer details from Firestore document
                        String id = doc.getId();
                        String name = doc.getString("username");
                        String type = doc.getString("type");
                        String about = doc.getString("about");

                        // Add lawyer to list
                        allLawyers.add(new Lawyer(name, type, about, id));
                    }
                    // Display lawyers on screen
                    displayLawyers(allLawyers);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading lawyers", Toast.LENGTH_SHORT).show());
    }

    /**
     * Filter lawyers by name (case insensitive)
     */
    private void filterByName(String query) {
        List<Lawyer> filtered = new ArrayList<>();
        for (Lawyer lawyer : allLawyers) {
            if (lawyer.getName() != null &&
                    lawyer.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(lawyer);
            }
        }
        displayLawyers(filtered);
    }

    /**
     * Show popup for filtering lawyers by type
     */
    private void showFilterPopup() {
        // Inflate popup layout
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_filter, null);
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // Get popup UI elements
        EditText typeInput = popupView.findViewById(R.id.typeInput);
        Button applyBtn = popupView.findViewById(R.id.applyFilterBtn);

        // Apply filter when button clicked
        applyBtn.setOnClickListener(view -> {
            String type = typeInput.getText().toString().trim().toLowerCase();
            List<Lawyer> filteredList = new ArrayList<>();
            for (Lawyer lawyer : allLawyers) {
                if (lawyer.getType() != null &&
                        lawyer.getType().toLowerCase().contains(type)) {
                    filteredList.add(lawyer);
                }
            }
            displayLawyers(filteredList);
            popupWindow.dismiss(); // Close popup after applying filter
        });

        // Show popup in center of the screen
        popupWindow.showAtLocation(lawyerContainer, 0, 0, 0);
    }

    /**
     * Display a list of lawyers as cards inside the container
     */
    private void displayLawyers(List<Lawyer> lawyers) {
        lawyerContainer.removeAllViews(); // Clear old views
        for (Lawyer l : lawyers) {
            addLawyerCard(l.getName(), l.getType(), l.getAbout(), l.getId());
        }
    }

    /**
     * Create a card view for a single lawyer and add it to the container
     */
    private void addLawyerCard(String name, String type, String about, String lawyerId) {
        // Inflate card layout
        View card = LayoutInflater.from(this).inflate(R.layout.item_lawyer_card, lawyerContainer, false);

        // Get card text views
        TextView nameView = card.findViewById(R.id.nameText);
        TextView typeView = card.findViewById(R.id.typeText);
        TextView aboutView = card.findViewById(R.id.aboutText);

        // Set lawyer info
        nameView.setText("👨‍⚖️ Name: " + name);
        typeView.setText("📂 Type: " + type);
        aboutView.setText("📝 About: " + about);

        // Navigate to lawyer profile page when card is clicked
        card.setOnClickListener(v -> {
            Intent intent = new Intent(find.this, lawReview.class);
            intent.putExtra("lawyerId", lawyerId); // Pass lawyerId to profile page
            startActivity(intent);
        });

        // Add card to container
        lawyerContainer.addView(card);
    }
}
