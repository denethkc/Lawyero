package com.s23010529.lawyero;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class GeminiRequest {

    private static final String API_KEY = "AIzaSyAujxopSUdEMHKPyMzFNplponXO0X9-_w4";
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    private final OkHttpClient client = new OkHttpClient();

    public interface GeminiCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public void sendMessage(String prompt, GeminiCallback callback) {
        try {
            // Build request body
            JSONObject part = new JSONObject();
            part.put("text", prompt);

            JSONArray parts = new JSONArray();
            parts.put(part);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("contents", contents);

            RequestBody body = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

            // Create request
            Request request = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .build();

            // Make the call
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnMain(() -> callback.onError("❌ Network error: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnMain(() -> callback.onError("❌ HTTP error: " + response.code()));
                        return;
                    }

                    try {
                        String res = response.body().string();
                        JSONObject json = new JSONObject(res);
                        JSONArray candidates = json.getJSONArray("candidates");
                        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                        JSONArray resultParts = content.getJSONArray("parts");
                        String reply = resultParts.getJSONObject(0).getString("text");

                        runOnMain(() -> callback.onResponse(reply));
                    } catch (Exception e) {
                        runOnMain(() -> callback.onError("❌ Parsing error: " + e.getMessage()));
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("❌ JSON error: " + e.getMessage());
        }
    }

    private void runOnMain(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }
}
