package com.captionforge.nativeapp.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.api.GroqTranscriber;
import com.captionforge.nativeapp.audio.AudioExtractor;
import com.captionforge.nativeapp.engine.NativeVideoBurner;
import com.captionforge.nativeapp.model.CaptionStyle;
import com.captionforge.nativeapp.model.WordCaption;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQ_CODE = 100;

    private ExoPlayer player;
    private PlayerView playerView;
    private CaptionOverlayView captionOverlay;
    private Uri currentVideoUri;

    private List<WordCaption> words = new ArrayList<>();
    private CaptionStyle currentStyle = new CaptionStyle();

    // UI Elements
    private ImageButton btnPlayPause;
    private SeekBar seekBar;
    private TextView tvTimeCode;
    private LinearLayout layoutEmptyPrompt;
    private LinearLayout layoutTranscribeLoading;
    private TextView tvTranscribeStatus;

    // Panels
    private View panelPresets, panelFontSize, panelColors, panelTranscript;
    private Button tabPresets, tabFontSize, tabColors, tabTranscript, tabTranscribe;
    private EditText etFontSizeNumber;
    private TranscriptAdapter transcriptAdapter;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying()) {
                long pos = player.getCurrentPosition();
                long dur = player.getDuration();
                if (dur > 0) {
                    seekBar.setProgress((int) ((pos * 1000) / dur));
                    tvTimeCode.setText(String.format(Locale.US, "%02d:%02d / %02d:%02d",
                            (pos / 1000) / 60, (pos / 1000) % 60,
                            (dur / 1000) / 60, (dur / 1000) % 60));
                }
                captionOverlay.updatePlaybackTime(pos / 1000.0);
            }
            progressHandler.postDelayed(this, 30);
        }
    };

    private final ActivityResultLauncher<String> videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    loadVideo(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        initViews();
        setupPlayer();
        setupPanels();
        setupTabs();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQ_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQ_CODE);
            }
        }
    }

    private void initViews() {
        playerView = findViewById(R.id.playerView);
        captionOverlay = findViewById(R.id.captionOverlay);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        seekBar = findViewById(R.id.seekBar);
        tvTimeCode = findViewById(R.id.tvTimeCode);
        layoutEmptyPrompt = findViewById(R.id.layoutEmptyPrompt);
        layoutTranscribeLoading = findViewById(R.id.layoutTranscribeLoading);
        tvTranscribeStatus = findViewById(R.id.tvTranscribeStatus);

        panelPresets = findViewById(R.id.panelPresets);
        panelFontSize = findViewById(R.id.panelFontSize);
        panelColors = findViewById(R.id.panelColors);
        panelTranscript = findViewById(R.id.panelTranscript);

        tabPresets = findViewById(R.id.tabPresets);
        tabFontSize = findViewById(R.id.tabFontSize);
        tabColors = findViewById(R.id.tabColors);
        tabTranscript = findViewById(R.id.tabTranscript);
        tabTranscribe = findViewById(R.id.tabTranscribe);

        findViewById(R.id.btnSelectVideo).setOnClickListener(v -> videoPickerLauncher.launch("video/*"));
        findViewById(R.id.btnSettings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.btnExport).setOnClickListener(v -> showExportDialog());

        btnPlayPause.setOnClickListener(v -> togglePlayPause());

        seekBar.setMax(1000);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && player != null && player.getDuration() > 0) {
                    long seekPos = (progress * player.getDuration()) / 1000;
                    player.seekTo(seekPos);
                    captionOverlay.updatePlaybackTime(seekPos / 1000.0);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
    }

    private void setupPlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                btnPlayPause.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
            }
        });
    }

    private void loadVideo(Uri uri) {
        currentVideoUri = uri;
        layoutEmptyPrompt.setVisibility(View.GONE);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        // Auto trigger Groq Whisper transcription
        startAutoCaption();
    }

    private void togglePlayPause() {
        if (player == null) return;
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    private void startAutoCaption() {
        if (currentVideoUri == null) {
            Toast.makeText(this, "Please select a video first!", Toast.LENGTH_SHORT).show();
            return;
        }

        layoutTranscribeLoading.setVisibility(View.VISIBLE);
        tvTranscribeStatus.setText("Extracting audio from video...");

        AudioExtractor.extractAudioWav(this, currentVideoUri, new AudioExtractor.ExtractCallback() {
            @Override
            public void onSuccess(File wavFile) {
                runOnUiThread(() -> tvTranscribeStatus.setText("Transcribing with Groq Whisper AI..."));

                GroqTranscriber.transcribe(MainActivity.this, wavFile, new GroqTranscriber.TranscribeCallback() {
                    @Override
                    public void onSuccess(List<WordCaption> resultWords) {
                        runOnUiThread(() -> {
                            layoutTranscribeLoading.setVisibility(View.GONE);
                            words = resultWords;
                            captionOverlay.setWords(words);
                            if (transcriptAdapter != null) {
                                transcriptAdapter.setWords(words);
                            }
                            Toast.makeText(MainActivity.this, "✅ Transcribed " + words.size() + " words!", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            layoutTranscribeLoading.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    layoutTranscribeLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Audio extraction failed: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupPanels() {
        // 1. Presets RecyclerView
        RecyclerView rvPresets = findViewById(R.id.rvPresets);
        PresetAdapter presetAdapter = new PresetAdapter(preset -> {
            currentStyle = preset;
            captionOverlay.setStyle(currentStyle);
            updateFontSizeInput(currentStyle.fontSize);
        });
        rvPresets.setAdapter(presetAdapter);

        // 2. Font & Numeric Size
        etFontSizeNumber = findViewById(R.id.etFontSizeNumber);
        etFontSizeNumber.setText(String.valueOf(currentStyle.fontSize));

        findViewById(R.id.btnSizeMinus).setOnClickListener(v -> changeFontSize(-5));
        findViewById(R.id.btnSizePlus).setOnClickListener(v -> changeFontSize(5));

        findViewById(R.id.chipSize40).setOnClickListener(v -> setExactFontSize(40));
        findViewById(R.id.chipSize65).setOnClickListener(v -> setExactFontSize(65));
        findViewById(R.id.chipSize90).setOnClickListener(v -> setExactFontSize(90));
        findViewById(R.id.chipSize120).setOnClickListener(v -> setExactFontSize(120));
        findViewById(R.id.chipSize160).setOnClickListener(v -> setExactFontSize(160));
        findViewById(R.id.chipSize200).setOnClickListener(v -> setExactFontSize(200));

        etFontSizeNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int size = Integer.parseInt(s.toString());
                    if (size >= 15 && size <= 300) {
                        currentStyle.fontSize = size;
                        captionOverlay.setStyle(currentStyle);
                    }
                } catch (Exception ignored) {}
            }
        });

        RecyclerView rvFonts = findViewById(R.id.rvFonts);
        FontAdapter fontAdapter = new FontAdapter(fontFamily -> {
            currentStyle.fontFamily = fontFamily;
            captionOverlay.setStyle(currentStyle);
        });
        rvFonts.setAdapter(fontAdapter);

        // 3. Colors Panel
        RecyclerView rvHighlight = findViewById(R.id.rvHighlightColors);
        List<Integer> highlightColors = Arrays.asList(
                Color.parseColor("#FBBF24"), // Yellow
                Color.parseColor("#10B981"), // Neon Green
                Color.parseColor("#EC4899"), // Hot Pink
                Color.parseColor("#06B6D4"), // Cyan
                Color.parseColor("#EF4444"), // Red
                Color.WHITE
        );
        rvHighlight.setAdapter(new ColorAdapter(highlightColors, color -> {
            currentStyle.highlightColor = color;
            captionOverlay.setStyle(currentStyle);
        }));

        RecyclerView rvText = findViewById(R.id.rvTextColors);
        List<Integer> textColors = Arrays.asList(
                Color.WHITE,
                Color.parseColor("#E2E8F0"),
                Color.parseColor("#000000"),
                Color.parseColor("#FBBF24"),
                Color.parseColor("#06B6D4")
        );
        rvText.setAdapter(new ColorAdapter(textColors, color -> {
            currentStyle.textColor = color;
            captionOverlay.setStyle(currentStyle);
        }));

        // 4. Transcript Editor
        RecyclerView rvTranscript = findViewById(R.id.rvTranscript);
        transcriptAdapter = new TranscriptAdapter(new TranscriptAdapter.OnWordActionListener() {
            @Override
            public void onSeekToWord(double seconds) {
                if (player != null) {
                    player.seekTo((long) (seconds * 1000));
                    captionOverlay.updatePlaybackTime(seconds);
                }
            }

            @Override
            public void onWordEdited(int index, String newText) {
                if (index < words.size()) {
                    words.get(index).setWord(newText);
                    captionOverlay.setWords(words);
                }
            }
        });
        rvTranscript.setAdapter(transcriptAdapter);
    }

    private void changeFontSize(int delta) {
        int newSize = Math.max(15, Math.min(300, currentStyle.fontSize + delta));
        setExactFontSize(newSize);
    }

    private void setExactFontSize(int size) {
        currentStyle.fontSize = size;
        updateFontSizeInput(size);
        captionOverlay.setStyle(currentStyle);
    }

    private void updateFontSizeInput(int size) {
        if (etFontSizeNumber != null) {
            etFontSizeNumber.setText(String.valueOf(size));
        }
    }

    private void setupTabs() {
        tabPresets.setOnClickListener(v -> switchTab(panelPresets, tabPresets));
        tabFontSize.setOnClickListener(v -> switchTab(panelFontSize, tabFontSize));
        tabColors.setOnClickListener(v -> switchTab(panelColors, tabColors));
        tabTranscript.setOnClickListener(v -> switchTab(panelTranscript, tabTranscript));
        tabTranscribe.setOnClickListener(v -> startAutoCaption());
    }

    private void switchTab(View activePanel, Button activeTab) {
        panelPresets.setVisibility(View.GONE);
        panelFontSize.setVisibility(View.GONE);
        panelColors.setVisibility(View.GONE);
        panelTranscript.setVisibility(View.GONE);

        tabPresets.setTextColor(getColor(R.color.text_slate_400));
        tabFontSize.setTextColor(getColor(R.color.text_slate_400));
        tabColors.setTextColor(getColor(R.color.text_slate_400));
        tabTranscript.setTextColor(getColor(R.color.text_slate_400));

        activePanel.setVisibility(View.VISIBLE);
        activeTab.setTextColor(getColor(R.color.accent_yellow));
    }

    private void showExportDialog() {
        if (currentVideoUri == null) {
            Toast.makeText(this, "Please select a video first!", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_export, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        RadioGroup rgRes = dialogView.findViewById(R.id.rgResolution);
        ProgressBar pb = dialogView.findViewById(R.id.pbExportProgress);
        TextView tvStatus = dialogView.findViewById(R.id.tvExportStatus);
        MaterialButton btnStart = dialogView.findViewById(R.id.btnStartExport);

        btnStart.setOnClickListener(v -> {
            String res = rgRes.getCheckedRadioButtonId() == R.id.rb1080p ? "1080p" : "720p";
            pb.setVisibility(View.VISIBLE);
            pb.setIndeterminate(true);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText("⚡ GPU Hardware Encoding in progress...");
            btnStart.setEnabled(false);

            NativeVideoBurner.burnCaptionsToGallery(
                    this,
                    currentVideoUri,
                    words,
                    currentStyle,
                    res,
                    new NativeVideoBurner.BurnCallback() {
                        @Override
                        public void onProgress(int percentage) {
                            runOnUiThread(() -> tvStatus.setText("Rendering: " + percentage + "%"));
                        }

                        @Override
                        public void onSuccess(String galleryLocation) {
                            runOnUiThread(() -> {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "✅ Saved to Gallery: " + galleryLocation, Toast.LENGTH_LONG).show();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                pb.setVisibility(View.GONE);
                                tvStatus.setText("Error: " + error);
                                btnStart.setEnabled(true);
                                Toast.makeText(MainActivity.this, "Export error: " + error, Toast.LENGTH_LONG).show();
                            });
                        }
                    }
            );
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressHandler.post(updateProgressRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressHandler.removeCallbacks(updateProgressRunnable);
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
