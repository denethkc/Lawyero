package com.s23010529.lawyero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class cusAppoinment extends AppCompatActivity {

    private CalendarView calendarView;
    private EditText reminderInput;
    private Button addBtn;
    private LinearLayout reminderListLayout;

    private String selectedDate;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cus_appoinment);

        calendarView = findViewById(R.id.calendarView2);
        reminderInput = findViewById(R.id.reminderInput);
        addBtn = findViewById(R.id.add);
        reminderListLayout = findViewById(R.id.reminderListLayout);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set default date and load reminders
        selectedDate = formatDate(calendarView.getDate());
        loadRemindersForDate(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = formatDate(year, month, dayOfMonth);
            loadRemindersForDate(selectedDate);
        });

        addBtn.setOnClickListener(v -> {
            String reminderText = reminderInput.getText().toString().trim();
            if (!reminderText.isEmpty()) {
                Map<String, Object> reminder = new HashMap<>();
                reminder.put("text", reminderText);
                reminder.put("date", selectedDate);
                reminder.put("uid", uid);
                reminder.put("timestamp", FieldValue.serverTimestamp());

                db.collection("reminders")
                        .add(reminder)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show();
                            reminderInput.setText("");
                            loadRemindersForDate(selectedDate); // Refresh list
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to add reminder", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Please enter a reminder", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    private String formatDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return formatDate(calendar.getTimeInMillis());
    }

    private void loadRemindersForDate(String date) {
        reminderListLayout.removeAllViews();

        db.collection("reminders")
                .whereEqualTo("uid", uid)
                .whereEqualTo("date", date)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        addReminderCard("No reminders for this day.");
                    } else {
                        for (DocumentSnapshot doc : querySnapshot) {
                            String text = doc.getString("text");
                            addReminderCard(text);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load reminders", Toast.LENGTH_SHORT).show());
    }

    private void addReminderCard(String text) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_reminder_card, reminderListLayout, false);
        TextView reminderText = cardView.findViewById(R.id.reminderText);
        reminderText.setText("🔔 " + text);
        reminderListLayout.addView(cardView);
    }
}
