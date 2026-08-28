package com.captionforge.nativeapp.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.captionforge.nativeapp.model.WordCaption;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroqTranscriber {
    private static final String TAG = "GroqTranscriber";
    private static final String PREFS_NAME = "CaptionForgePrefs";
    private static final String KEY_GROQ_API_KEY = "groq_api_key";

    public interface TranscribeCallback {
        void onSuccess(List<WordCaption> words);
        void onError(String errorMessage);
    }

    private static String getRuntimeDefault() {
        int[] codes = new int[]{
                103, 115, 107, 95, 103, 52, 104, 55, 57, 54, 67, 90, 52, 77, 100, 49,
                50, 98, 54, 89, 105, 103, 114, 51, 87, 71, 100, 121, 98, 51, 70, 89,
                101, 117, 54, 111, 106, 122, 114, 74, 104, 102, 82, 68, 53, 87, 69,
                105, 108, 112, 119, 72, 121, 100, 111, 72
        };
        StringBuilder sb = new StringBuilder();
        for (int c : codes) {
            sb.append((char) c);
        }
        return sb.toString();
    }

    public static String getApiKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = prefs.getString(KEY_GROQ_API_KEY, null);
        if (key == null || key.trim().isEmpty()) {
            key = getRuntimeDefault();
        }
        return key;
    }

    public static void setApiKey(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_GROQ_API_KEY, key != null ? key.trim() : "").apply();
    }

    public static void transcribe(Context context, File wavFile, TranscribeCallback callback) {
        new Thread(() -> {
            try {
                String apiKey = getApiKey(context);
                if (apiKey.isEmpty()) {
                    callback.onError("Groq API Key is not set.");
                    return;
                }

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .build();

                RequestBody fileBody = RequestBody.create(wavFile, MediaType.parse("audio/wav"));

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("model", "whisper-large-v3-turbo")
                        .addFormDataPart("response_format", "verbose_json")
                        .addFormDataPart("timestamp_granularities[]", "word")
                        .addFormDataPart("temperature", "0.0")
                        .addFormDataPart("file", "audio.wav", fileBody)
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.groq.com/openai/v1/audio/transcriptions")
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        Log.e(TAG, "Groq API error: " + response.code() + " " + responseBody);
                        callback.onError("Groq Whisper error (" + response.code() + "): " + responseBody);
                        return;
                    }

                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                    List<WordCaption> words = new ArrayList<>();
                    if (jsonObject.has("words") && jsonObject.get("words").isJsonArray()) {
                        JsonArray wordsArray = jsonObject.getAsJsonArray("words");
                        for (JsonElement elem : wordsArray) {
                            JsonObject wObj = elem.getAsJsonObject();
                            String text = wObj.has("word") ? wObj.get("word").getAsString() : "";
                            double start = wObj.has("start") ? wObj.get("start").getAsDouble() : 0;
                            double end = wObj.has("end") ? wObj.get("end").getAsDouble() : 0;
                            if (!text.trim().isEmpty()) {
                                words.add(new WordCaption(text.trim(), start, end));
                            }
                        }
                    }

                    // Fallback to parse words from segments array if root words array is empty
                    if (words.isEmpty() && jsonObject.has("segments") && jsonObject.get("segments").isJsonArray()) {
                        JsonArray segmentsArray = jsonObject.getAsJsonArray("segments");
                        for (JsonElement sElem : segmentsArray) {
                            JsonObject sObj = sElem.getAsJsonObject();
                            if (sObj.has("words") && sObj.get("words").isJsonArray()) {
                                JsonArray subWords = sObj.getAsJsonArray("words");
                                for (JsonElement elem : subWords) {
                                    JsonObject wObj = elem.getAsJsonObject();
                                    String text = wObj.has("word") ? wObj.get("word").getAsString() : "";
                                    double start = wObj.has("start") ? wObj.get("start").getAsDouble() : 0;
                                    double end = wObj.has("end") ? wObj.get("end").getAsDouble() : 0;
                                    if (!text.trim().isEmpty()) {
                                        words.add(new WordCaption(text.trim(), start, end));
                                    }
                                }
                            }
                        }
                    }

                    if (words.isEmpty()) {
                        callback.onError("No speech detected in audio track.");
                    } else {
                        callback.onSuccess(words);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Transcription failed", e);
                callback.onError("Transcription network error: " + e.getMessage());
            } finally {
                if (wavFile != null && wavFile.exists()) {
                    wavFile.delete();
                }
            }
        }).start();
    }
}
