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
    private static final String REPLAN_PROMPT = "Re-plan the trip due to bad weather conditions. ";

    public interface GptApiCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public static void getChatCompletion(String prompt, String userMessage, GptApiCallback callback) {
        Log.d("PLAN", "[getChatCompletion] START");
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", "gpt-4o");
            jsonObject.put("messages", new JSONArray()
                .put(new JSONObject()
                    .put("role", "system")
                    .put("content", prompt))
                .put(new JSONObject()
                    .put("role", "user")
                    .put("content", userMessage)));
            jsonObject.put("max_tokens", 150);
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
            Log.d("PLAN", "[getChatCompletion]"+e.getMessage());
            return;
        }

        Log.d("PLAN", "[getChatCompletion] Data ready");

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Request request = new Request.Builder()
                .url(GPT_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        Log.d("PLAN", "[getChatCompletion] Got response");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(e.getMessage());
                Log.d("PLAN", "[getChatCompletion]"+e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("PLAN", "[getChatCompletion] success");
                    callback.onSuccess(response.body().string());
                } else {
                    Log.d("PLAN", "[getChatCompletion] failed: "+response.message());
                    callback.onFailure(response.message());
                }
            }
        });
    }

    public static void getTripPlan(String tripData, GptApiCallback callback) {
        getChatCompletion(DEFAULT_PROMPT, tripData, callback);
    }

   
       
}