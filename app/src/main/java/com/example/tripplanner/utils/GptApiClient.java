package com.example.tripplanner.utils;

import android.util.Log;

import com.example.tripplanner.BuildConfig;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class GptApiClient {
    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = BuildConfig.GPT_API_KEY;
    private static final String DEFAULT_PROMPT = "Please plan the trip to ";

    public interface GptApiCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public static void getTripPlan(String prompt, GptApiCallback callback) {
        Log.d("PLAN", "1");
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", "gpt-3.5-turbo"); // Ensure the model is correct
            jsonObject.put("messages", new JSONArray().put(new JSONObject()
                .put("role", "user")
                .put("content", prompt)));
            jsonObject.put("max_tokens", 150);
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
            Log.d("PLAN", "2");
            return;
        }

        Log.d("PLAN", "3");

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Request request = new Request.Builder()
                .url(GPT_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        Log.d("PLAN", "4");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e.getMessage());
                Log.d("PLAN", "5");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("PLAN", "success");
                    callback.onSuccess(response.body().string());
                } else {
                    Log.d("PLAN", "6");
                    Log.d("PLAN", String.valueOf(response));
                    callback.onFailure(response.message());
                }
            }
        });
    }
}