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

    private String selectedDate;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_law_appoinment);

        // UI elements
        calendarView = findViewById(R.id.calendarView);
        reminderInput = findViewById(R.id.reminderInput);
        btnAddReminder = findViewById(R.id.btnAddReminder);

        // Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Set today's date as default
        selectedDate = formatDate(new Date());

        // Listen for date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDate = formatDate(cal.getTime());
        });

        // Handle Add button
        btnAddReminder.setOnClickListener(v -> {
            String detail = reminderInput.getText().toString().trim();

            if (detail.isEmpty()) {
                Toast.makeText(this, "Please enter a reminder", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = mAuth.getCurrentUser().getUid();
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            Map<String, Object> reminder = new HashMap<>();
            reminder.put("uid", uid);
            reminder.put("date", selectedDate);
            reminder.put("time", time);
            reminder.put("detail", detail);

            db.collection("reminders")
                    .add(reminder)
                    .addOnSuccessListener(doc -> {
                        reminderInput.setText("");
                        Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
    }
}
