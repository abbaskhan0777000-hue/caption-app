package com.captionforge.nativeapp.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.api.GroqTranscriber;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton btnBack = findViewById(R.id.btnSettingsBack);
        EditText etApiKey = findViewById(R.id.etApiKey);
        MaterialButton btnSave = findViewById(R.id.btnSaveApiKey);
        TextView tvStatus = findViewById(R.id.tvKeyStatus);

        btnBack.setOnClickListener(v -> finish());

        String currentKey = GroqTranscriber.getApiKey(this);
        etApiKey.setText(currentKey);

        if (currentKey != null && !currentKey.isEmpty()) {
            tvStatus.setText("✅ Groq API Key is active");
            tvStatus.setTextColor(getColor(R.color.accent_green));
        } else {
            tvStatus.setText("⚠️ Key is missing");
            tvStatus.setTextColor(getColor(R.color.accent_yellow));
        }

        btnSave.setOnClickListener(v -> {
            String newKey = etApiKey.getText().toString().trim();
            GroqTranscriber.setApiKey(this, newKey);
            Toast.makeText(this, "API Key saved successfully!", Toast.LENGTH_SHORT).show();
            tvStatus.setText("✅ Groq API Key is active");
            tvStatus.setTextColor(getColor(R.color.accent_green));
        });
    }
}
