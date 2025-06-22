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
        setContentView(R.layout.activity_ask_ai);

        userInput = findViewById(R.id.userInput);
        chatLayout = findViewById(R.id.chatLayout);
        chatScroll = findViewById(R.id.chatScroll);
        ImageButton sendButton = findViewById(R.id.sendBtn);

        GeminiRequest gemini = new GeminiRequest();

        sendButton.setOnClickListener(v -> {
            String prompt = userInput.getText().toString().trim();
            if (!prompt.isEmpty()) {
                addMessage("You: " + prompt);
                userInput.setText("");

                gemini.sendMessage(prompt, new GeminiRequest.GeminiCallback() {
                    @Override
                    public void onResponse(String message) {
                        runOnUiThread(() -> addMessage("AI: " + message));
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> addMessage("Error: " + error));
                    }
                });
            }
        });
    }

    private void addMessage(String text) {
        TextView messageView = new TextView(this);
        messageView.setText(text);
        messageView.setTextSize(16);
        messageView.setTextColor(getResources().getColor(android.R.color.white));
        messageView.setPadding(20, 16, 20, 16);
        messageView.setBackgroundResource(R.drawable.box); // make sure this exists

        chatLayout.addView(messageView);
        chatScroll.post(() -> chatScroll.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
