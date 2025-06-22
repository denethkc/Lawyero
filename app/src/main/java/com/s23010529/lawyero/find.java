package com.s23010529.lawyero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class find extends AppCompatActivity {

    private LinearLayout lawyerContainer;
    private FirebaseFirestore db;
    private SearchView searchView;
    private ImageButton filterBtn;

    private List<Lawyer> allLawyers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);

        lawyerContainer = findViewById(R.id.lawyerContainer);
        searchView = findViewById(R.id.searchView);
        filterBtn = findViewById(R.id.filterButton); // ✅ Add this in activity_find.xml
        db = FirebaseFirestore.getInstance();

        loadLawyers();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterByName(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterByName(newText);
                return true;
            }
        });

        filterBtn.setOnClickListener(v -> showFilterPopup());
    }

    private void loadLawyers() {
        db.collection("users")
                .whereEqualTo("role", "lawyer")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allLawyers.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        String name = doc.getString("username");
                        String type = doc.getString("type");
                        String about = doc.getString("about");
                        allLawyers.add(new Lawyer(name, type, about));
                    }
                    displayLawyers(allLawyers);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading lawyers", Toast.LENGTH_SHORT).show());
    }

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

    private void showFilterPopup() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_filter, null);
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        EditText typeInput = popupView.findViewById(R.id.typeInput);
        Button applyBtn = popupView.findViewById(R.id.applyFilterBtn);

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
            popupWindow.dismiss();
        });

        popupWindow.showAtLocation(lawyerContainer, 0, 0, 0);
    }

    private void displayLawyers(List<Lawyer> lawyers) {
        lawyerContainer.removeAllViews();
        for (Lawyer l : lawyers) {
            addLawyerCard(l.getName(), l.getType(), l.getAbout());
        }
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

