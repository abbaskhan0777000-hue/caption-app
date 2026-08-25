package com.captionforge.nativeapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupPlayer();
        checkPermissions();
        setupBottomTools();
    }

    private void initViews() {
        playerView = findViewById(R.id.playerView);
        captionOverlay = findViewById(R.id.captionOverlay);
        tvTimeCode = findViewById(R.id.tvTimeCode);
        videoProgressBar = findViewById(R.id.videoProgressBar);
        layoutEmptyPrompt = findViewById(R.id.layoutEmptyPrompt);
        layoutTranscribeLoading = findViewById(R.id.layoutTranscribeLoading);
        tvTranscribeStatus = findViewById(R.id.tvTranscribeStatus);

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

        // Empty prompt button
        findViewById(R.id.btnSelectVideoPrompt).setOnClickListener(v -> videoPickerLauncher.launch("video/*"));

        // Video container click toggles play/pause
        playerView.setOnClickListener(v -> {
            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.play();
                }
            }
        });
    }

    private void setupPlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);

        captionOverlay.setStyle(currentStyle);
        progressHandler.post(updateProgressRunnable);
    }

    private void setupBottomTools() {
        // 1. Edit Captions
        findViewById(R.id.tabEditCaptions).setOnClickListener(v -> {
            if (words == null || words.isEmpty()) {
                Toast.makeText(this, "Please select a video with captions first", Toast.LENGTH_SHORT).show();
                return;
            }
            EditCaptionsActivity.inputWords = new ArrayList<>(words);
            EditCaptionsActivity.wordsPerChunk = currentStyle.wordsPerChunk;
            Intent intent = new Intent(this, EditCaptionsActivity.class);
            editCaptionsLauncher.launch(intent);
        });

        // 2. Templates
        findViewById(R.id.tabTemplates).setOnClickListener(v -> {
            TemplatesBottomSheet sheet = TemplatesBottomSheet.newInstance(currentStyle, new TemplatesBottomSheet.OnTemplateApplyListener() {
                @Override
                public void onApply(CaptionStyle style) {
                    currentStyle = style;
                    captionOverlay.setStyle(currentStyle);
                    updateSegmentChips();
                }

                @Override
                public void onOpenStyles() {
                    openStylesDialog();
                }
            });
            sheet.show(getSupportFragmentManager(), "TemplatesBottomSheet");
        });

        // 3. Styles
        findViewById(R.id.tabStyles).setOnClickListener(v -> openStylesDialog());
    }

    private void openStylesDialog() {
        StylesBottomSheet sheet = StylesBottomSheet.newInstance(currentStyle, style -> {
            this.currentStyle = style;
            captionOverlay.setStyle(currentStyle);
            updateSegmentChips();
        });
        sheet.show(getSupportFragmentManager(), "StylesBottomSheet");
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

        AudioExtractor.extractAudioToWav(this, videoUri, new AudioExtractor.ExtractCallback() {
            @Override
            public void onSuccess(File wavFile) {
                runOnUiThread(() -> tvTranscribeStatus.setText("Generating AI Captions with Whisper..."));

                GroqTranscriber.transcribeAudioWithKey(MainActivity.this, wavFile, null, new GroqTranscriber.TranscribeCallback() {
                    @Override
                    public void onSuccess(List<WordCaption> transcribedWords, String fullText) {
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
        if (currentVideoUri == null) {
            Toast.makeText(this, "Please choose a video first", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_export, null);
        RadioGroup rgResolution = dialogView.findViewById(R.id.rgResolution);
        ProgressBar pbExport = dialogView.findViewById(R.id.pbExport);
        TextView tvExportStatus = dialogView.findViewById(R.id.tvExportStatus);
        MaterialButton btnStartRender = dialogView.findViewById(R.id.btnStartRender);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnStartRender.setOnClickListener(v -> {
            String resolution = (rgResolution.getCheckedRadioButtonId() == R.id.rb1080p) ? "1080p" : "720p";

            btnStartRender.setEnabled(false);
            pbExport.setVisibility(View.VISIBLE);
            tvExportStatus.setVisibility(View.VISIBLE);
            tvExportStatus.setText("Burning hardware GPU overlays...");

            NativeVideoBurner.burnCaptionsToGallery(
                    MainActivity.this,
                    currentVideoUri,
                    words,
                    currentStyle,
                    resolution,
                    new NativeVideoBurner.BurnCallback() {
                        @Override
                        public void onProgress(int percentage) {
                            runOnUiThread(() -> pbExport.setProgress(percentage));
                        }

                        @Override
                        public void onSuccess(String galleryLocation) {
                            runOnUiThread(() -> {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "Saved to Gallery! " + galleryLocation, Toast.LENGTH_LONG).show();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                btnStartRender.setEnabled(true);
                                pbExport.setVisibility(View.GONE);
                                tvExportStatus.setText("Export error: " + error);
                                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                            });
                        }
                    }
            );
        });

        dialog.show();
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
