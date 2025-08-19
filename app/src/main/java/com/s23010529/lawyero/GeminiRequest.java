package com.s23010529.lawyero;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;


public class GeminiRequest {

    // API key for Google Gemini
    private static final String API_KEY = "AIzaSyAujxopSUdEMHKPyMzFNplponXO0X9-_w4";

    // Gemini API endpoint (Gemini 2.0 Flash model)
    private static final String URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    // OkHttp client instance to send requests
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Callback interface to return the response or error asynchronously.
     */
    public interface GeminiCallback {
        void onResponse(String response); // Called when AI response is successful
        void onError(String error);       // Called when an error occurs
    }

    /**
     * Send a prompt to the Gemini API and return the response via callback.
     *
     * @param prompt   The text prompt entered by user
     * @param callback The callback to handle AI response or error
     */
    public void sendMessage(String prompt, GeminiCallback callback) {
        try {
            // Build JSON request body in Gemini's expected format
            JSONObject part = new JSONObject();
            part.put("text", prompt); // user input

            JSONArray parts = new JSONArray();
            parts.put(part);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("contents", contents);

            // Convert JSON into HTTP request body
            RequestBody body = RequestBody.create(
                    bodyJson.toString(),
                    MediaType.parse("application/json")
            );

            // Build the HTTP POST request
            Request request = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .build();

            // Execute the request asynchronously
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    // If network request fails
                    runOnMain(() -> callback.onError("❌ Network error: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        // Handle HTTP errors (e.g., 400, 500)
                        runOnMain(() -> callback.onError("❌ HTTP error: " + response.code()));
                        return;
                    }

                    try {
                        // Parse response JSON
                        String res = response.body().string();
                        JSONObject json = new JSONObject(res);

                        // Gemini responses are inside "candidates[0].content.parts[0].text"
                        JSONArray candidates = json.getJSONArray("candidates");
                        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                        JSONArray resultParts = content.getJSONArray("parts");
                        String reply = resultParts.getJSONObject(0).getString("text");

                        // Return the AI reply via callback on main thread
                        runOnMain(() -> callback.onResponse(reply));
                    } catch (Exception e) {
                        // If JSON parsing fails
                        runOnMain(() -> callback.onError("❌ Parsing error: " + e.getMessage()));
                    }
                }
            });

        } catch (Exception e) {
            // Handle JSON building errors
            callback.onError("❌ JSON error: " + e.getMessage());
        }
    }

    /**
     * Run a task on the main (UI) thread
     * This is required because OkHttp callbacks run on a background thread.
     */
    private void runOnMain(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }
}
