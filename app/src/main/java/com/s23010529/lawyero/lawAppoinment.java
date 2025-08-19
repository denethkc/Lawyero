package com.s23010529.lawyero;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;


public class lawAppoinment extends AppCompatActivity {


    private CalendarView calendarView;
    private EditText reminderInput;
    private Button btnAddReminder;

    // Variables
    private String selectedDate;         // Holds the currently selected date
    private FirebaseFirestore db;        // Firestore database instance
    private FirebaseAuth mAuth;          // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_law_appoinment);

        // Initialize UI elements
        calendarView = findViewById(R.id.calendarView);
        reminderInput = findViewById(R.id.reminderInput);
        btnAddReminder = findViewById(R.id.btnAddReminder);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Set today's date as default selected date
        selectedDate = formatDate(new Date());

        // Listen for date selection from the calendar
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDate = formatDate(cal.getTime()); // Update selected date
        });

        // Handle Add Reminder button click
        btnAddReminder.setOnClickListener(v -> {
            String detail = reminderInput.getText().toString().trim();

            // Validate input
            if (detail.isEmpty()) {
                Toast.makeText(this, "Please enter a reminder", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensure user is logged in before saving reminder
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get user ID and current time
            String uid = mAuth.getCurrentUser().getUid();
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            // Create reminder data
            Map<String, Object> reminder = new HashMap<>();
            reminder.put("uid", uid);          // User who created reminder
            reminder.put("date", selectedDate); // Appointment date
            reminder.put("time", time);        // Time reminder was added
            reminder.put("detail", detail);    // Reminder details

            // Save reminder to Firestore
            db.collection("reminders")
                    .add(reminder)
                    .addOnSuccessListener(doc -> {
                        // Clear input field after success
                        reminderInput.setText("");
                        Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    /**
     * Format a Date object into a string (yyyy-MM-dd)
     *
     * @param date - Date object
     * @return formatted date string
     */
    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
    }
}
