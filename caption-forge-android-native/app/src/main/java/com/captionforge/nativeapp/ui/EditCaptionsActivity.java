package com.captionforge.nativeapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.engine.AssGenerator;
import com.captionforge.nativeapp.model.WordCaption;

import java.util.ArrayList;
import java.util.List;

public class EditCaptionsActivity extends AppCompatActivity {

    public static List<WordCaption> inputWords = new ArrayList<>();
    public static int wordsPerChunk = 3;
    public static List<WordCaption> resultWords = new ArrayList<>();

    private SegmentEditAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_captions);

        ImageView btnBack = findViewById(R.id.btnBackEdit);
        TextView btnApply = findViewById(R.id.btnApplyEdit);
        RecyclerView rvList = findViewById(R.id.rvSegmentsList);

        btnBack.setOnClickListener(v -> finish());

        List<AssGenerator.CaptionChunk> chunks = AssGenerator.chunkWords(inputWords, wordsPerChunk);
        adapter = new SegmentEditAdapter(chunks);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(adapter);

        btnApply.setOnClickListener(v -> {
            resultWords = adapter.buildWordList();
            setResult(Activity.RESULT_OK);
            finish();
        });
    }
}
