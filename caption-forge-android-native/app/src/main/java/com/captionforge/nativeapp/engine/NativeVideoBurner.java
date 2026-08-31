package com.captionforge.nativeapp.engine;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;
import com.captionforge.nativeapp.model.CaptionStyle;
import com.captionforge.nativeapp.model.WordCaption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class NativeVideoBurner {
    private static final String TAG = "NativeVideoBurner";

    public interface BurnCallback {
        void onProgress(int percentage);
        void onSuccess(String galleryLocation);
        void onError(String error);
    }

    private static class OverlayFrame {
        File imageFile;
        double startTime;
        double endTime;

        OverlayFrame(File file, double start, double end) {
            this.imageFile = file;
            this.startTime = start;
            this.endTime = end;
        }
    }

    public static void burnCaptionsToGallery(
            Context context,
            Uri inputVideoUri,
            List<WordCaption> words,
            CaptionStyle style,
            String resolution,
            BurnCallback callback
    ) {
        new Thread(() -> {
            File tempInput = null;
            File tempOutput = null;
            List<OverlayFrame> frames = new ArrayList<>();
            File overlaysDir = null;

            try {
                File cacheDir = context.getCacheDir();
                overlaysDir = new File(cacheDir, "overlays_" + System.currentTimeMillis());
                overlaysDir.mkdirs();

                // 1. Copy video from Uri to app internal cache
                tempInput = new File(cacheDir, "input_render_" + System.currentTimeMillis() + ".mp4");
                try (InputStream in = context.getContentResolver().openInputStream(inputVideoUri);
                     OutputStream out = new FileOutputStream(tempInput)) {
                    byte[] buffer = new byte[1024 * 64];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                }

                // 2. Extract Exact Video Width, Height, and Duration for 100% sync & progress
                int outWidth = 720;
                int outHeight = 1280;
                long totalVideoDurationMs = 10000;

                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(tempInput.getAbsolutePath());
                    String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                    String rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                    String durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    retriever.release();

                    if (durStr != null) {
                        totalVideoDurationMs = Long.parseLong(durStr);
                    }

                    int rawW = 720, rawH = 1280;
                    if (widthStr != null && heightStr != null) {
                        int w = Integer.parseInt(widthStr);
                        int h = Integer.parseInt(heightStr);
                        int rotation = (rotationStr != null) ? Integer.parseInt(rotationStr) : 0;
                        if (rotation == 90 || rotation == 270) {
                            rawW = h;
                            rawH = w;
                        } else {
                            rawW = w;
                            rawH = h;
                        }
                    }

                    // True Resolution Calculation based on User's Choice (720p vs 1080p)
                    boolean is1080p = "1080p".equalsIgnoreCase(resolution);
                    if (is1080p) {
                        if (rawH >= rawW) { // Vertical or Square video
                            outWidth = 1080;
                            outHeight = (int) Math.round(1080.0 * rawH / rawW);
                        } else { // Landscape / Horizontal video
                            outHeight = 1080;
                            outWidth = (int) Math.round(1080.0 * rawW / rawH);
                        }
                    } else { // 720p
                        if (rawH >= rawW) {
                            outWidth = 720;
                            outHeight = (int) Math.round(720.0 * rawH / rawW);
                        } else {
                            outHeight = 720;
                            outWidth = (int) Math.round(720.0 * rawW / rawH);
                        }
                    }

                    // Ensure dimensions are even numbers (Required by H.264 video encoders)
                    outWidth = (outWidth / 2) * 2;
                    outHeight = (outHeight / 2) * 2;

                } catch (Exception ignored) {
                    boolean is1080p = "1080p".equalsIgnoreCase(resolution);
                    outWidth = is1080p ? 1080 : 720;
                    outHeight = is1080p ? 1920 : 1280;
                }

                Log.d(TAG, "Target video (" + resolution + "): " + outWidth + "x" + outHeight + " Duration: " + totalVideoDurationMs + "ms");
                callback.onProgress(5);

                // 3. Generate Hardware Bitmap Overlays with EXACT Millisecond Timestamps (No artificial padding!)
                if (words != null && !words.isEmpty()) {
                    List<AssGenerator.CaptionChunk> chunks = AssGenerator.chunkWords(words, style.wordsPerChunk);
                    int totalChunks = chunks.size();
                    int frameIndex = 0;

                    for (int cIdx = 0; cIdx < chunks.size(); cIdx++) {
                        AssGenerator.CaptionChunk chunk = chunks.get(cIdx);
                        if (chunk.words.isEmpty()) continue;

                        // Next chunk start for natural pause bridging (held up to max 3.5s)
                        double nextChunkStart = (cIdx < chunks.size() - 1) ? chunks.get(cIdx + 1).start : (chunk.end + 2.5);
                        double chunkVisibleEnd = Math.min(nextChunkStart, chunk.end + 3.5);

                        if (!"clean".equalsIgnoreCase(style.animationPreset)) {
                            for (int wIdx = 0; wIdx < chunk.words.size(); wIdx++) {
                                WordCaption activeWord = chunk.words.get(wIdx);
                                Bitmap bmp = renderCaptionBitmap(context, outWidth, outHeight, chunk, activeWord, style);
                                File imgFile = new File(overlaysDir, "frame_" + frameIndex + ".png");
                                try (FileOutputStream fos = new FileOutputStream(imgFile)) {
                                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                }
                                bmp.recycle();

                                double start = (wIdx == 0) ? Math.max(0, chunk.start) : Math.max(0, activeWord.getStart());
                                double end;
                                if (wIdx < chunk.words.size() - 1) {
                                    end = Math.max(start + 0.05, chunk.words.get(wIdx + 1).getStart());
                                } else {
                                    end = Math.max(start + 0.05, chunkVisibleEnd);
                                }
                                frames.add(new OverlayFrame(imgFile, start, end));
                                frameIndex++;
                            }
                        } else {
                            Bitmap bmp = renderCaptionBitmap(context, outWidth, outHeight, chunk, null, style);
                            File imgFile = new File(overlaysDir, "frame_" + frameIndex + ".png");
                            try (FileOutputStream fos = new FileOutputStream(imgFile)) {
                                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            }
                            bmp.recycle();

                            double start = Math.max(0, chunk.start);
                            double end = Math.max(start + 0.05, chunkVisibleEnd);
                            frames.add(new OverlayFrame(imgFile, start, end));
                            frameIndex++;
                        }
                    }
                    callback.onProgress(18);
                } else {
                    Log.i(TAG, "No words provided, exporting direct stream copy...");
                }

                // 4. Encode & Burn Overlays into Video
                callback.onProgress(20);
                tempOutput = new File(cacheDir, "rendered_" + System.currentTimeMillis() + ".mp4");
                List<String> baseArgs = new ArrayList<>();
                baseArgs.add("-y");
                baseArgs.add("-i");
                baseArgs.add(tempInput.getAbsolutePath());

                for (OverlayFrame frame : frames) {
                    baseArgs.add("-i");
                    baseArgs.add(frame.imageFile.getAbsolutePath());
                }

                // Build Filter Complex: 1. Scale Video -> 2. Overlay Frames
                StringBuilder filterComplex = new StringBuilder();
                filterComplex.append("[0:v]scale=").append(outWidth).append(":").append(outHeight).append("[vscaled]");

                if (!frames.isEmpty()) {
                    String lastOutput = "[vscaled]";
                    for (int i = 0; i < frames.size(); i++) {
                        OverlayFrame frame = frames.get(i);
                        String currentOutput = (i == frames.size() - 1) ? "[vout]" : ("[v" + (i + 1) + "]");
                        filterComplex.append(";")
                                .append(lastOutput)
                                .append("[")
                                .append(i + 1)
                                .append(":v]overlay=0:0:enable='between(t,")
                                .append(String.format(Locale.US, "%.3f", frame.startTime))
                                .append(",")
                                .append(String.format(Locale.US, "%.3f", frame.endTime))
                                .append(")'")
                                .append(currentOutput);
                        lastOutput = currentOutput;
                    }
                } else {
                    filterComplex.append(";[vscaled]null[vout]");
                }

                baseArgs.add("-filter_complex");
                baseArgs.add(filterComplex.toString());
                baseArgs.add("-map");
                baseArgs.add("[vout]");
                baseArgs.add("-map");
                baseArgs.add("0:a?");

                boolean is1080p = "1080p".equalsIgnoreCase(resolution);
                String videoBitrate = is1080p ? "8M" : "3.5M";

                String[][] candidateCodecs = new String[][]{
                        {"-c:v", "h264_mediacodec", "-b:v", videoBitrate, "-pix_fmt", "yuv420p", "-g", "30", "-keyint_min", "30"},
                        {"-c:v", "libopenh264", "-b:v", videoBitrate, "-pix_fmt", "yuv420p", "-g", "30", "-keyint_min", "30"},
                        {"-c:v", "mpeg4", "-q:v", is1080p ? "2" : "4", "-pix_fmt", "yuv420p", "-g", "30"}
                };

                boolean success = false;
                String finalError = "";
                final long finalDurMs = Math.max(1000, totalVideoDurationMs);

                for (String[] codecArgs : candidateCodecs) {
                    if (tempOutput.exists()) tempOutput.delete();
                    List<String> fullCmd = new ArrayList<>(baseArgs);
                    for (String arg : codecArgs) fullCmd.add(arg);
                    
                    // Audio Standardization & Real-Time Sync (WhatsApp / Social Media Compatibility)
                    fullCmd.add("-c:a"); fullCmd.add("aac");
                    fullCmd.add("-b:a"); fullCmd.add("192k");
                    fullCmd.add("-ar"); fullCmd.add("44100");
                    fullCmd.add("-ac"); fullCmd.add("2");
                    fullCmd.add("-af"); fullCmd.add("aresample=async=1");
                    fullCmd.add("-avoid_negative_ts"); fullCmd.add("make_zero");
                    fullCmd.add("-movflags"); fullCmd.add("+faststart");
                    fullCmd.add(tempOutput.getAbsolutePath());

                    Log.d(TAG, "Trying encoder with real-time statistics: " + codecArgs[1]);

                    AtomicBoolean sessionSuccess = new AtomicBoolean(false);
                    CountDownLatch latch = new CountDownLatch(1);

                    FFmpegSession session = FFmpegKit.executeWithArgumentsAsync(
                            fullCmd.toArray(new String[0]),
                            completedSession -> {
                                if (ReturnCode.isSuccess(completedSession.getReturnCode())) {
                                    sessionSuccess.set(true);
                                } else {
                                    Log.w(TAG, "Encode session failed with return code: " + completedSession.getReturnCode());
                                }
                                latch.countDown();
                            },
                            log -> {},
                            statistics -> {
                                if (statistics != null) {
                                    double timeSec = statistics.getTime() / 1000.0;
                                    double totalSec = finalDurMs / 1000.0;
                                    int renderPct = (int) ((timeSec / totalSec) * 75.0);
                                    callback.onProgress(Math.min(96, 20 + renderPct));
                                }
                            }
                    );

                    try { latch.await(); } catch (InterruptedException e) { Log.e(TAG, "Interrupted", e); }

                    if (sessionSuccess.get()) {
                        success = true;
                        break;
                    } else {
                        finalError = session != null ? session.getAllLogsAsString() : "Encoder failed";
                    }
                }

                if (success) {
                    callback.onProgress(98);
                    String savedName = saveToGallery(context, tempOutput);
                    callback.onProgress(100);
                    cleanup(tempInput, tempOutput, frames, overlaysDir);
                    callback.onSuccess(savedName);
                } else {
                    cleanup(tempInput, tempOutput, frames, overlaysDir);
                    callback.onError("Video export failed: " + finalError);
                }

            } catch (Exception e) {
                cleanup(tempInput, tempOutput, frames, overlaysDir);
                callback.onError("Hardware burn error: " + e.getMessage());
            }
        }).start();
    }

    private static Bitmap renderCaptionBitmap(
            Context context,
            int videoWidth,
            int videoHeight,
            AssGenerator.CaptionChunk chunk,
            WordCaption activeWord,
            CaptionStyle style
    ) {
        Bitmap bitmap = Bitmap.createBitmap(videoWidth, videoHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float scaleFactor = (float) videoWidth / 720f;
        float textSizePx = style.fontSize * scaleFactor * 2.1f;

        int typefaceStyle = Typeface.NORMAL;
        if (style.isBold && style.isItalic) typefaceStyle = Typeface.BOLD_ITALIC;
        else if (style.isBold) typefaceStyle = Typeface.BOLD;
        else if (style.isItalic) typefaceStyle = Typeface.ITALIC;

        Typeface tf = FontManager.getTypeface(context, style.fontFamily, typefaceStyle);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSizePx);
        textPaint.setColor(style.textColor);
        textPaint.setTypeface(tf);
        textPaint.setUnderlineText(style.isUnderlined);

        Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setTextSize(textSizePx);
        strokePaint.setColor(style.strokeColor);
        strokePaint.setStrokeWidth(style.hasOutline ? style.strokeWidth * scaleFactor * 0.9f : 0);
        strokePaint.setTypeface(tf);

        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setTextSize(textSizePx);
        highlightPaint.setColor(style.highlightColor);
        highlightPaint.setTypeface(tf);
        highlightPaint.setUnderlineText(style.isUnderlined);

        Paint bgBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgBoxPaint.setStyle(Paint.Style.FILL);

        Paint highlightBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightBgPaint.setStyle(Paint.Style.FILL);

        // Vertical Placement
        float centerY;
        if ("top".equalsIgnoreCase(style.verticalAlign)) {
            centerY = videoHeight * 0.18f;
        } else if ("center".equalsIgnoreCase(style.verticalAlign)) {
            centerY = videoHeight * 0.50f;
        } else {
            centerY = (style.positionYPercent / 100f) * videoHeight;
        }

        // Template-Specific Dynamic Inter-Word Spacing (Eliminates background box bleed)
        boolean hasActiveWordBox = (style.highlightBgColor != 0 && style.highlightBgColor != Color.TRANSPARENT);
        float extraSpace = hasActiveWordBox ? (28f * scaleFactor) : (6f * scaleFactor);
        float spaceWidth = textPaint.measureText(" ") + extraSpace;

        // Calculate exact total line width with custom per-template spacing
        float totalLineWidth = 0;
        for (int i = 0; i < chunk.words.size(); i++) {
            WordCaption w = chunk.words.get(i);
            totalLineWidth += textPaint.measureText(w.getWord().toUpperCase());
            if (i < chunk.words.size() - 1) {
                totalLineWidth += spaceWidth;
            }
        }

        // Auto-scale down if text exceeds 92% of video width (Prevents edge clipping)
        float maxAllowedWidth = videoWidth * 0.92f;
        if (totalLineWidth > maxAllowedWidth && totalLineWidth > 0) {
            float fitScale = maxAllowedWidth / totalLineWidth;
            textSizePx *= fitScale;
            textPaint.setTextSize(textSizePx);
            strokePaint.setTextSize(textSizePx);
            strokePaint.setStrokeWidth(style.hasOutline ? (style.strokeWidth * scaleFactor * 0.9f * fitScale) : 0);
            highlightPaint.setTextSize(textSizePx);
            spaceWidth *= fitScale;

            totalLineWidth = 0;
            for (int i = 0; i < chunk.words.size(); i++) {
                WordCaption w = chunk.words.get(i);
                totalLineWidth += textPaint.measureText(w.getWord().toUpperCase());
                if (i < chunk.words.size() - 1) {
                    totalLineWidth += spaceWidth;
                }
            }
        }

        float startX;
        if ("left".equalsIgnoreCase(style.textAlign)) {
            startX = 40f * scaleFactor;
        } else if ("right".equalsIgnoreCase(style.textAlign)) {
            startX = videoWidth - totalLineWidth - (40f * scaleFactor);
        } else {
            startX = (videoWidth - totalLineWidth) / 2f;
        }

        // Phrase Background
        if (style.backgroundColor != 0 && style.backgroundColor != Color.TRANSPARENT) {
            bgBoxPaint.setColor(style.backgroundColor);
            Rect bounds = new Rect();
            textPaint.getTextBounds("A", 0, 1, bounds);
            float padX = 24f * scaleFactor;
            float padY = 16f * scaleFactor;
            RectF box = new RectF(
                    startX - padX,
                    centerY + bounds.top - padY,
                    startX + totalLineWidth + padX,
                    centerY + bounds.bottom + padY
            );
            canvas.drawRoundRect(box, 16f * scaleFactor, 16f * scaleFactor, bgBoxPaint);
        }

        // Animation presets: "pop", "bounce", "glow", "fade", "karaoke", "clean"
        String animPreset = style.animationPreset != null ? style.animationPreset.toLowerCase() : "karaoke";
        boolean isPop = animPreset.equals("pop");
        boolean isBounce = animPreset.equals("bounce");
        boolean isGlow = animPreset.equals("glow");
        boolean isFade = animPreset.equals("fade");

        float currentX = startX;

        for (WordCaption w : chunk.words) {
            String wordText = w.getWord().toUpperCase();
            float wordWidth = textPaint.measureText(wordText);

            boolean isActive = (activeWord != null && activeWord == w);
            Paint fillPaint = isActive ? highlightPaint : textPaint;

            // Fade mode: subtle alpha on upcoming words, 100% on active
            if (isFade) {
                fillPaint.setAlpha(isActive ? 255 : 150);
            } else {
                fillPaint.setAlpha(255);
            }

            Rect wBounds = new Rect();
            textPaint.getTextBounds(wordText, 0, wordText.length(), wBounds);

            // Pop / Bounce Animation Scale Transform for active word
            boolean needsTransform = isActive && (isPop || isBounce);
            if (needsTransform) {
                float wordCenterX = currentX + (wordWidth / 2f);
                float wordCenterY = centerY + (wBounds.top + wBounds.bottom) / 2f;
                canvas.save();
                if (isPop) {
                    canvas.scale(1.12f, 1.12f, wordCenterX, wordCenterY);
                } else if (isBounce) {
                    canvas.translate(0, -6f * scaleFactor);
                }
            }

            // Highlight word background box with isolated non-overlapping padding
            if (isActive && hasActiveWordBox) {
                highlightBgPaint.setColor(style.highlightBgColor);
                float hPadX = 10f * scaleFactor;
                float hPadY = 10f * scaleFactor;
                RectF wBox = new RectF(
                        currentX - hPadX,
                        centerY + wBounds.top - hPadY,
                        currentX + wordWidth + hPadX,
                        centerY + wBounds.bottom + hPadY
                );
                canvas.drawRoundRect(wBox, 10f * scaleFactor, 10f * scaleFactor, highlightBgPaint);
            }

            // Outline
            if (style.hasOutline && strokePaint.getStrokeWidth() > 0) {
                canvas.drawText(wordText, currentX, centerY, strokePaint);
            }

            // Glow / Shadow Animation
            if (isActive && isGlow) {
                int glowColor = (style.shadowColor != 0 && style.shadowColor != Color.TRANSPARENT) ? style.shadowColor : style.highlightColor;
                fillPaint.setShadowLayer(10f * scaleFactor, 0, 0, glowColor);
            } else if (style.hasShadow) {
                fillPaint.setShadowLayer(6f * scaleFactor, 3f * scaleFactor, 3f * scaleFactor, style.shadowColor);
            }

            // Text
            canvas.drawText(wordText, currentX, centerY, fillPaint);
            fillPaint.clearShadowLayer();

            if (needsTransform) {
                canvas.restore();
            }

            currentX += (wordWidth + spaceWidth);
        }

        return bitmap;
    }

    private static String saveToGallery(Context context, File videoFile) throws Exception {
        String fileName = "CaptionForge_" + System.currentTimeMillis() + ".mp4";
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/CaptionForge");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
        }

        Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            throw new Exception("Failed to create MediaStore entry");
        }

        try (InputStream in = new FileInputStream(videoFile);
             OutputStream out = resolver.openOutputStream(uri)) {
            byte[] buffer = new byte[1024 * 64];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear();
            values.put(MediaStore.Video.Media.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
        }

        return "Movies/CaptionForge/" + fileName;
    }

    private static void cleanup(File input, File output, List<OverlayFrame> frames, File overlaysDir) {
        if (input != null && input.exists()) input.delete();
        if (output != null && output.exists()) output.delete();
        if (frames != null) {
            for (OverlayFrame f : frames) {
                if (f.imageFile != null && f.imageFile.exists()) f.imageFile.delete();
            }
        }
        if (overlaysDir != null && overlaysDir.exists()) {
            overlaysDir.delete();
        }
    }
}
