package com.s23010529.lawyero;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class cusAppoinment extends AppCompatActivity {

    // UI elements
    private CalendarView calendarView;
    private EditText reminderInput;
    private Button addBtn;
    private LinearLayout reminderListLayout;

    // Data variables
    private String selectedDate;
    private FirebaseFirestore db;
    private String uid;

    private static final String TAG = "cusAppoinment"; // Tag for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cus_appoinment);

        // Initialize UI components
        calendarView = findViewById(R.id.calendarView2);
        reminderInput = findViewById(R.id.reminderInput);
        addBtn = findViewById(R.id.add);
        reminderListLayout = findViewById(R.id.reminderListLayout);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get current logged-in user UID
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            // If no user logged in, exit activity
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set default date (today) and load reminders for it
        selectedDate = formatDate(calendarView.getDate());
        loadRemindersForDate(selectedDate);

        // Handle calendar date change
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = formatDate(year, month, dayOfMonth);
            loadRemindersForDate(selectedDate); // Refresh reminders for new date
        });

        // Add reminder when button is clicked
        addBtn.setOnClickListener(v -> {
            String reminderText = reminderInput.getText().toString().trim();
            if (!reminderText.isEmpty()) {
                // Create reminder data
                Map<String, Object> reminder = new HashMap<>();
                reminder.put("text", reminderText);
                reminder.put("date", selectedDate);
                reminder.put("uid", uid);
                reminder.put("timestamp", FieldValue.serverTimestamp()); // for sorting

                // Save reminder in Firestore
                db.collection("reminders")
                        .add(reminder)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Reminder added with ID: " + docRef.getId());
                            reminderInput.setText(""); // clear input
                            loadRemindersForDate(selectedDate); // reload list
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error adding reminder", e);
                            Toast.makeText(this, "Failed to add reminder", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Please enter a reminder", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Format millis → yyyy-MM-dd
    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return sdf.format(new Date(millis));
    }

    // Format year, month, day → yyyy-MM-dd
    private String formatDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return formatDate(calendar.getTimeInMillis());
    }

    // Load reminders from Firestore for the selected date
    private void loadRemindersForDate(String date) {
        reminderListLayout.removeAllViews(); // clear old reminders

        Log.d(TAG, "Loading reminders for UID: " + uid + " Date: " + date);

        db.collection("reminders")
                .whereEqualTo("uid", uid)   // Only reminders of logged-in user
                .whereEqualTo("date", date) // Only for selected date
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "Documents found: " + querySnapshot.size());
                    if (querySnapshot.isEmpty()) {
                        // No reminders → show message
                        addReminderCard("No reminders for this day.");
                    } else {
                        // Add each reminder as a card
                        for (DocumentSnapshot doc : querySnapshot) {
                            String text = doc.getString("text");
                            Log.d(TAG, "Reminder loaded: " + text);
                            addReminderCard(text);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load reminders", e);
                    Toast.makeText(this, "Failed to load reminders", Toast.LENGTH_SHORT).show();
                });
    }

    // Add a reminder card to the list
    private void addReminderCard(String text) {
        // Inflate reminder card layout
        View cardView = LayoutInflater.from(this)
                .inflate(R.layout.item_reminder_card, reminderListLayout, false);

        // Set reminder text with bell emoji
        TextView reminderText = cardView.findViewById(R.id.reminderText);
        reminderText.setText("🔔 " + text);

        // Add the card to the list
        reminderListLayout.addView(cardView);
    }
}
