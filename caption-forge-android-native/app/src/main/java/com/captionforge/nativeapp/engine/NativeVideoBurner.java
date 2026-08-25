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

                // 2. Extract Exact Video Width & Height to match Overlay 1:1
                int outWidth = 720;
                int outHeight = 1280;

                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(tempInput.getAbsolutePath());
                    String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                    String rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                    retriever.release();

                    if (widthStr != null && heightStr != null) {
                        int rawW = Integer.parseInt(widthStr);
                        int rawH = Integer.parseInt(heightStr);
                        int rotation = (rotationStr != null) ? Integer.parseInt(rotationStr) : 0;
                        if (rotation == 90 || rotation == 270) {
                            outWidth = rawH;
                            outHeight = rawW;
                        } else {
                            outWidth = rawW;
                            outHeight = rawH;
                        }
                    }
                } catch (Exception ignored) {
                    outWidth = "1080p".equalsIgnoreCase(resolution) ? 1080 : 720;
                    outHeight = "1080p".equalsIgnoreCase(resolution) ? 1920 : 1280;
                }

                Log.d(TAG, "Target video dimensions: " + outWidth + "x" + outHeight);

                // 2. Generate Hardware Bitmap Overlays for every caption event
                if (words != null && !words.isEmpty()) {
                    List<AssGenerator.CaptionChunk> chunks = AssGenerator.chunkWords(words, style.wordsPerChunk);
                    int frameIndex = 0;

                    for (AssGenerator.CaptionChunk chunk : chunks) {
                        if (chunk.words.isEmpty()) continue;

                        if ("karaoke".equalsIgnoreCase(style.animationPreset) || "pop".equalsIgnoreCase(style.animationPreset)) {
                            for (WordCaption activeWord : chunk.words) {
                                Bitmap bmp = renderCaptionBitmap(outWidth, outHeight, chunk, activeWord, style);
                                File imgFile = new File(overlaysDir, "frame_" + frameIndex + ".png");
                                try (FileOutputStream fos = new FileOutputStream(imgFile)) {
                                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                }
                                bmp.recycle();

                                double start = Math.max(0, activeWord.getStart());
                                double end = activeWord.getEnd() + 0.1;
                                frames.add(new OverlayFrame(imgFile, start, end));
                                frameIndex++;
                            }
                        } else {
                            Bitmap bmp = renderCaptionBitmap(outWidth, outHeight, chunk, null, style);
                            File imgFile = new File(overlaysDir, "frame_" + frameIndex + ".png");
                            try (FileOutputStream fos = new FileOutputStream(imgFile)) {
                                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            }
                            bmp.recycle();

                            double start = Math.max(0, chunk.start);
                            double end = chunk.end + 0.25;
                            frames.add(new OverlayFrame(imgFile, start, end));
                            frameIndex++;
                        }
                    }
                }

                tempOutput = new File(cacheDir, "rendered_" + System.currentTimeMillis() + ".mp4");

                // 3. Assemble FFmpeg Hardware Encoding Command
                List<String> baseArgs = new ArrayList<>();
                baseArgs.add("-y");
                baseArgs.add("-i");
                baseArgs.add(tempInput.getAbsolutePath());

                for (OverlayFrame frame : frames) {
                    baseArgs.add("-i");
                    baseArgs.add(frame.imageFile.getAbsolutePath());
                }

                if (!frames.isEmpty()) {
                    StringBuilder filter = new StringBuilder();
                    String lastStream = "0:v";

                    for (int i = 0; i < frames.size(); i++) {
                        OverlayFrame frame = frames.get(i);
                        String currentStream = (i == frames.size() - 1) ? "outv" : ("v" + (i + 1));
                        int inputIdx = i + 1;
                        filter.append(String.format(Locale.US,
                                "[%s][%d:v]overlay=0:0:enable='between(t,%.2f,%.2f)'[%s]",
                                lastStream, inputIdx, frame.startTime, frame.endTime, currentStream));

                        if (i < frames.size() - 1) {
                            filter.append(";");
                        }
                        lastStream = currentStream;
                    }

                    baseArgs.add("-filter_complex");
                    baseArgs.add(filter.toString());
                    baseArgs.add("-map");
                    baseArgs.add("[outv]");
                } else {
                    baseArgs.add("-map");
                    baseArgs.add("0:v");
                }

                baseArgs.add("-map");
                baseArgs.add("0:a?");

                // Candidate Video Encoders: Android Hardware MediaCodec GPU -> OpenH264 -> MPEG4
                String[][] candidateCodecs = new String[][]{
                        {"-c:v", "h264_mediacodec", "-b:v", "5M", "-pix_fmt", "yuv420p"},
                        {"-c:v", "libopenh264", "-b:v", "5M", "-pix_fmt", "yuv420p"},
                        {"-c:v", "mpeg4", "-q:v", "3", "-pix_fmt", "yuv420p"}
                };

                FFmpegSession lastSession = null;
                boolean success = false;

                for (String[] codecArgs : candidateCodecs) {
                    if (tempOutput.exists()) {
                        tempOutput.delete();
                    }
                    List<String> fullCmd = new ArrayList<>(baseArgs);
                    for (String arg : codecArgs) {
                        fullCmd.add(arg);
                    }
                    fullCmd.add("-c:a");
                    fullCmd.add("aac");
                    fullCmd.add("-b:a");
                    fullCmd.add("192k");
                    fullCmd.add(tempOutput.getAbsolutePath());

                    Log.d(TAG, "Trying encoder: " + codecArgs[1]);
                    lastSession = FFmpegKit.executeWithArguments(fullCmd.toArray(new String[0]));

                    if (ReturnCode.isSuccess(lastSession.getReturnCode())) {
                        success = true;
                        Log.i(TAG, "Render Succeeded with encoder: " + codecArgs[1]);
                        break;
                    } else {
                        Log.w(TAG, "Encoder " + codecArgs[1] + " failed, trying fallback encoder...");
                    }
                }

                if (success) {
                    Log.i(TAG, "Render Succeeded. Inserting to MediaStore Gallery...");
                    String savedName = saveToGallery(context, tempOutput);
                    cleanup(tempInput, tempOutput, frames, overlaysDir);
                    callback.onSuccess(savedName);
                } else {
                    String fullLog = lastSession != null ? lastSession.getAllLogsAsString() : "Unknown error";
                    if (fullLog == null || fullLog.trim().isEmpty()) {
                        fullLog = lastSession != null ? lastSession.getOutput() : "Unknown error";
                    }
                    String errorDetail = (fullLog != null && fullLog.length() > 600)
                            ? fullLog.substring(fullLog.length() - 600)
                            : (fullLog != null ? fullLog : "Unknown error");

                    Log.e(TAG, "All video encoders failed: " + fullLog);
                    cleanup(tempInput, tempOutput, frames, overlaysDir);
                    callback.onError("Encoding error: " + errorDetail);
                }

            } catch (Exception e) {
                Log.e(TAG, "Burn error", e);
                cleanup(tempInput, tempOutput, frames, overlaysDir);
                callback.onError("Render exception: " + e.getMessage());
            }
        }).start();
    }

    private static Bitmap renderCaptionBitmap(int width, int height, AssGenerator.CaptionChunk chunk, WordCaption activeWord, CaptionStyle style) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float textSizePx = (style.fontSize / 720f) * width * 0.95f;
        Typeface tf = Typeface.create(style.fontFamily, Typeface.BOLD);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(textSizePx);
        textPaint.setColor(style.textColor);
        textPaint.setTypeface(tf);

        Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setTextAlign(Paint.Align.CENTER);
        strokePaint.setTextSize(textSizePx);
        strokePaint.setColor(style.strokeColor);
        strokePaint.setStrokeWidth(style.strokeWidth * (width / 720f) * 1.5f);
        strokePaint.setTypeface(tf);

        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setTextAlign(Paint.Align.CENTER);
        highlightPaint.setTextSize(textSizePx);
        highlightPaint.setColor(style.highlightColor);
        highlightPaint.setTypeface(tf);

        Paint bgBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgBoxPaint.setStyle(Paint.Style.FILL);

        float centerY = (style.positionYPercent / 100f) * height;

        StringBuilder fullLine = new StringBuilder();
        for (WordCaption w : chunk.words) {
            fullLine.append(w.getWord().toUpperCase()).append(" ");
        }
        String fullText = fullLine.toString().trim();
        float totalLineWidth = textPaint.measureText(fullText);

        if (style.backgroundColor != 0 && style.backgroundColor != Color.TRANSPARENT) {
            bgBoxPaint.setColor(style.backgroundColor);
            Rect bounds = new Rect();
            textPaint.getTextBounds(fullText, 0, fullText.length(), bounds);
            float padX = 32f * (width / 720f);
            float padY = 20f * (width / 720f);
            RectF boxRect = new RectF(
                    (width - totalLineWidth) / 2f - padX,
                    centerY + bounds.top - padY,
                    (width + totalLineWidth) / 2f + padX,
                    centerY + bounds.bottom + padY
            );
            canvas.drawRoundRect(boxRect, 20f, 20f, bgBoxPaint);
        }

        float startX = (width - totalLineWidth) / 2f;
        float currentX = startX;

        for (WordCaption w : chunk.words) {
            String wordText = w.getWord().toUpperCase();
            float wordWidth = textPaint.measureText(wordText + " ");
            boolean isActive = (activeWord != null && activeWord == w);
            Paint fillPaint = isActive ? highlightPaint : textPaint;

            float drawX = currentX + (textPaint.measureText(wordText) / 2f);

            if (style.strokeWidth > 0) {
                canvas.drawText(wordText, drawX, centerY, strokePaint);
            }
            canvas.drawText(wordText, drawX, centerY, fillPaint);

            currentX += wordWidth;
        }

        return bitmap;
    }

    private static void cleanup(File tempInput, File tempOutput, List<OverlayFrame> frames, File overlaysDir) {
        if (tempInput != null && tempInput.exists()) tempInput.delete();
        if (tempOutput != null && tempOutput.exists()) tempOutput.delete();
        if (frames != null) {
            for (OverlayFrame f : frames) {
                if (f.imageFile != null && f.imageFile.exists()) {
                    f.imageFile.delete();
                }
            }
        }
        if (overlaysDir != null && overlaysDir.exists()) {
            overlaysDir.delete();
        }
    }

    private static String saveToGallery(Context context, File sourceFile) throws Exception {
        String displayName = "CaptionForge_" + System.currentTimeMillis() + ".mp4";
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, displayName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.TITLE, displayName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/CaptionForge");
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
        }

        Uri collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Uri itemUri = resolver.insert(collection, values);

        if (itemUri == null) {
            throw new Exception("Failed to create MediaStore entry.");
        }

        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = resolver.openOutputStream(itemUri)) {
            if (out == null) {
                throw new Exception("Failed to open output stream for Gallery.");
            }
            byte[] buf = new byte[1024 * 64];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear();
            values.put(MediaStore.Video.Media.IS_PENDING, 0);
            resolver.update(itemUri, values, null, null);
        }

        return "Movies/CaptionForge/" + displayName;
    }
}
