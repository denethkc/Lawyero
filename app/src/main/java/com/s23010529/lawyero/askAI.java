package com.s23010529.lawyero;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class askAI extends AppCompatActivity {

    private EditText userInput;
    private LinearLayout chatLayout;
    private ScrollView chatScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_ai); // Load the UI layout

        // Initialize UI elements from XML
        userInput = findViewById(R.id.userInput);
        chatLayout = findViewById(R.id.chatLayout);
        chatScroll = findViewById(R.id.chatScroll);
        ImageButton sendButton = findViewById(R.id.sendBtn);

        // Create GeminiRequest object (class that handles AI requests)
        GeminiRequest gemini = new GeminiRequest();

        // Handle send button click
        sendButton.setOnClickListener(v -> {
            String prompt = userInput.getText().toString().trim(); // Get user input
            if (!prompt.isEmpty()) {
                addMessage("You: " + prompt);  // Show user's message in chat
                userInput.setText("");

                // Send the prompt to AI
                gemini.sendMessage(prompt, new GeminiRequest.GeminiCallback() {
                    @Override
                    public void onResponse(String message) {
                        // On success, display AI response in chat
                        runOnUiThread(() -> addMessage("AI: " + message));
                    }

                    @Override
                    public void onError(String error) {
                        // On error, show error message in chat
                        runOnUiThread(() -> addMessage("Error: " + error));
                    }
                });
            }
        });
    }

    // Method to add a new message (user or AI) to the chat layout
    private void addMessage(String text) {
        TextView messageView = new TextView(this);  // Create new text view for the message
        messageView.setText(text);
        messageView.setTextSize(16);
        messageView.setTextColor(getResources().getColor(android.R.color.white));
        messageView.setPadding(20, 16, 20, 16);
        messageView.setBackgroundResource(R.drawable.box); // Custom background for chat bubbles

        chatLayout.addView(messageView); // Add message to chat layout
        chatScroll.post(() -> chatScroll.fullScroll(ScrollView.FOCUS_DOWN)); // Auto-scroll to bottom
    }
}
