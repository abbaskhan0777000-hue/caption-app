package com.captionforge.nativeapp.ui;

import android.Manifest;
import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.api.GroqTranscriber;
import com.captionforge.nativeapp.audio.AudioExtractor;
import com.captionforge.nativeapp.engine.NativeVideoBurner;
import com.captionforge.nativeapp.model.CaptionStyle;
import com.captionforge.nativeapp.model.WordCaption;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.captionforge.nativeapp.CrashHandler;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQ_CODE = 100;

    private ExoPlayer player;
    private PlayerView playerView;
    private CaptionOverlayView captionOverlay;
    private Uri currentVideoUri;

    private List<WordCaption> words = new ArrayList<>();
    private CaptionStyle currentStyle = new CaptionStyle();

    // UI Elements
    private TextView tvTimeCode;
    private ProgressBar videoProgressBar;
    private LinearLayout layoutEmptyPrompt;
    private LinearLayout layoutTranscribeLoading;
    private TextView tvTranscribeStatus;
    private RecyclerView rvSegmentChips;
    private ChipSegmentAdapter chipAdapter;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                long pos = player.getCurrentPosition();
                long dur = player.getDuration();
                if (dur > 0) {
                    videoProgressBar.setProgress((int) ((pos * 1000) / dur));
                    tvTimeCode.setText(String.format(Locale.US, "%02d:%02d - %02d:%02d",
                            (pos / 1000) / 60, (pos / 1000) % 60,
                            (dur / 1000) / 60, (dur / 1000) % 60));
                }
                captionOverlay.updatePlaybackTime(pos / 1000.0);
            }
            progressHandler.postDelayed(this, 16);
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

    private final ActivityResultLauncher<Intent> editCaptionsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && EditCaptionsActivity.resultWords != null) {
                    this.words = new ArrayList<>(EditCaptionsActivity.resultWords);
                    captionOverlay.setWords(words);
                    updateSegmentChips();
                    Toast.makeText(this, "Captions updated!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CrashHandler.init(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPreviousCrash();
        initViews();
        setupPlayer();
        checkPermissions();
        setupBottomTools();
    }

    private void checkPreviousCrash() {
        try {
            File file = new File(getFilesDir(), "last_crash.txt");
            if (file.exists()) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                }
                file.delete();
                String crashLog = sb.toString();
                if (!crashLog.trim().isEmpty()) {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("⚠️ Error Log Recorded")
                            .setMessage(crashLog.length() > 600 ? crashLog.substring(0, 600) + "..." : crashLog)
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        } catch (Exception ignored) {}
    }

    private ImageView btnCenterPlay;
    private ImageView btnBottomPlayPause;

    private void initViews() {
        playerView = findViewById(R.id.playerView);
        captionOverlay = findViewById(R.id.captionOverlay);
        tvTimeCode = findViewById(R.id.tvTimeCode);
        videoProgressBar = findViewById(R.id.videoProgressBar);
        layoutEmptyPrompt = findViewById(R.id.layoutEmptyPrompt);
        layoutTranscribeLoading = findViewById(R.id.layoutTranscribeLoading);
        tvTranscribeStatus = findViewById(R.id.tvTranscribeStatus);

        btnCenterPlay = findViewById(R.id.btnCenterPlay);
        btnBottomPlayPause = findViewById(R.id.btnBottomPlayPause);

        rvSegmentChips = findViewById(R.id.rvSegmentChips);
        chipAdapter = new ChipSegmentAdapter(chunk -> {
            if (player != null && chunk != null) {
                player.seekTo((long) (chunk.start * 1000));
                player.play();
            }
        });
        rvSegmentChips.setAdapter(chipAdapter);

        // Top Bar Actions
        findViewById(R.id.btnBack).setOnClickListener(v -> videoPickerLauncher.launch("video/*"));
        findViewById(R.id.btnExport).setOnClickListener(v -> showExportDialog());

        // Empty prompt button & container
        View btnPrompt = findViewById(R.id.btnSelectVideoPrompt);
        if (btnPrompt != null) {
            btnPrompt.setOnClickListener(v -> videoPickerLauncher.launch("video/*"));
        }
        if (layoutEmptyPrompt != null) {
            layoutEmptyPrompt.setOnClickListener(v -> videoPickerLauncher.launch("video/*"));
        }

        // Play/Pause button click listeners
        btnCenterPlay.setOnClickListener(v -> togglePlayPause());
        btnBottomPlayPause.setOnClickListener(v -> togglePlayPause());
        captionOverlay.setOnOverlayTapListener(this::togglePlayPause);
    }

    private void togglePlayPause() {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
                btnCenterPlay.setVisibility(View.VISIBLE);
                btnBottomPlayPause.setImageResource(R.drawable.ic_play_white);
            } else {
                player.play();
                btnCenterPlay.setVisibility(View.GONE);
                btnBottomPlayPause.setImageResource(R.drawable.ic_pause_white);
            }
        }
    }

    private void setupPlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setUseController(false);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    player.seekTo(0);
                    player.pause();
                    btnCenterPlay.setVisibility(View.VISIBLE);
                    btnBottomPlayPause.setImageResource(R.drawable.ic_play_white);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    btnCenterPlay.setVisibility(View.GONE);
                    btnBottomPlayPause.setImageResource(R.drawable.ic_pause_white);
                } else {
                    btnCenterPlay.setVisibility(View.VISIBLE);
                    btnBottomPlayPause.setImageResource(R.drawable.ic_play_white);
                }
            }
        });

        playerView.setOnClickListener(v -> togglePlayPause());
        progressHandler.post(updateProgressRunnable);
    }

    private void setupBottomTools() {
        // 1. Edit Captions
        findViewById(R.id.tabEditCaptions).setOnClickListener(v -> {
            if (words == null || words.isEmpty()) {
                Toast.makeText(this, "No captions to edit. Please import a video.", Toast.LENGTH_SHORT).show();
                return;
            }
            EditCaptionsActivity.inputWords = new ArrayList<>(words);
            EditCaptionsActivity.wordsPerChunk = currentStyle.wordsPerChunk;
            Intent intent = new Intent(this, EditCaptionsActivity.class);
            editCaptionsLauncher.launch(intent);
        });

        // 2. Templates
        findViewById(R.id.tabTemplates).setOnClickListener(v -> {
            if (isFinishing() || isDestroyed()) return;
            try {
                TemplatesBottomSheet sheet = TemplatesBottomSheet.newInstance(selectedStyle -> {
                    this.currentStyle = selectedStyle;
                    captionOverlay.setStyle(currentStyle);
                    updateSegmentChips();
                });
                sheet.show(getSupportFragmentManager(), "TemplatesSheet");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 3. Styles
        findViewById(R.id.tabStyles).setOnClickListener(v -> {
            if (isFinishing() || isDestroyed()) return;
            try {
                StylesBottomSheet sheet = StylesBottomSheet.newInstance(currentStyle, updatedStyle -> {
                    this.currentStyle = updatedStyle;
                    captionOverlay.setStyle(currentStyle);
                    updateSegmentChips();
                });
                sheet.show(getSupportFragmentManager(), "StylesSheet");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateSegmentChips() {
        if (words != null && !words.isEmpty()) {
            chipAdapter.setWords(words, currentStyle.wordsPerChunk);
            rvSegmentChips.setVisibility(View.VISIBLE);
        } else {
            rvSegmentChips.setVisibility(View.GONE);
        }
    }

    private void loadVideo(Uri uri) {
        this.currentVideoUri = uri;
        layoutEmptyPrompt.setVisibility(View.GONE);

        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        // Auto transcribe with Whisper AI
        startAutoTranscription(uri);
    }

    private void startAutoTranscription(Uri videoUri) {
        layoutTranscribeLoading.setVisibility(View.VISIBLE);
        tvTranscribeStatus.setText("Extracting audio stream...");

        AudioExtractor.extractAudioWav(this, videoUri, new AudioExtractor.ExtractCallback() {
            @Override
            public void onSuccess(File wavFile) {
                runOnUiThread(() -> tvTranscribeStatus.setText("Generating AI Captions with Whisper..."));

                GroqTranscriber.transcribe(MainActivity.this, wavFile, new GroqTranscriber.TranscribeCallback() {
                    @Override
                    public void onSuccess(List<WordCaption> transcribedWords) {
                        runOnUiThread(() -> {
                            layoutTranscribeLoading.setVisibility(View.GONE);
                            words = transcribedWords;
                            captionOverlay.setWords(words);
                            updateSegmentChips();
                            Toast.makeText(MainActivity.this, "AI Transcribed " + words.size() + " words!", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            layoutTranscribeLoading.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Transcription note: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    layoutTranscribeLoading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Audio error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showExportDialog() {
        if (isFinishing() || isDestroyed()) return;
        if (currentVideoUri == null) {
            Toast.makeText(this, "Please choose a video first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_export, null);
            RadioGroup rgResolution = dialogView.findViewById(R.id.rgResolution);
            View layoutProgress = dialogView.findViewById(R.id.layoutExportProgressContainer);
            TextView tvExportPercent = dialogView.findViewById(R.id.tvExportPercent);
            ProgressBar pbExport = dialogView.findViewById(R.id.pbExport);
            TextView tvExportStatus = dialogView.findViewById(R.id.tvExportStatus);
            MaterialButton btnStartRender = dialogView.findViewById(R.id.btnStartRender);

            AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            btnStartRender.setOnClickListener(v -> {
                String resolution = (rgResolution.getCheckedRadioButtonId() == R.id.rb1080p) ? "1080p" : "720p";

                btnStartRender.setEnabled(false);
                btnStartRender.setVisibility(View.GONE);
                layoutProgress.setVisibility(View.VISIBLE);
                tvExportPercent.setText("0%");
                pbExport.setProgress(0);
                tvExportStatus.setText("Generating crisp hardware overlays...");

                NativeVideoBurner.burnCaptionsToGallery(
                        MainActivity.this,
                        currentVideoUri,
                        words,
                        currentStyle,
                        resolution,
                        new NativeVideoBurner.BurnCallback() {
                            @Override
                            public void onProgress(int percentage) {
                                runOnUiThread(() -> {
                                    if (isFinishing() || isDestroyed()) return;
                                    pbExport.setProgress(percentage);
                                    tvExportPercent.setText(percentage + "%");
                                    if (percentage < 20) {
                                        tvExportStatus.setText("Generating hardware overlay frames...");
                                    } else if (percentage < 95) {
                                        tvExportStatus.setText("Encoding GPU video stream (" + percentage + "%)...");
                                    } else {
                                        tvExportStatus.setText("Saving to Gallery...");
                                    }
                                });
                            }

                            @Override
                            public void onSuccess(String galleryLocation) {
                                runOnUiThread(() -> {
                                    if (isFinishing() || isDestroyed()) return;
                                    dialog.dismiss();
                                    Toast.makeText(MainActivity.this, "🎉 Saved to Gallery! " + galleryLocation, Toast.LENGTH_LONG).show();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    if (isFinishing() || isDestroyed()) return;
                                    btnStartRender.setEnabled(true);
                                    btnStartRender.setVisibility(View.VISIBLE);
                                    layoutProgress.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this, "Export error: " + error, Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                );
            });

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                }, PERMISSION_REQ_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSION_REQ_CODE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressHandler.removeCallbacks(updateProgressRunnable);
        if (player != null) {
            player.release();
        }
    }
}
