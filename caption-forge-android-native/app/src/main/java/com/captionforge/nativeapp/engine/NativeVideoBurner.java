package com.captionforge.nativeapp.engine;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
import java.util.List;

public class NativeVideoBurner {
    private static final String TAG = "NativeVideoBurner";

    public interface BurnCallback {
        void onProgress(int percentage);
        void onSuccess(String galleryLocation);
        void onError(String error);
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
            File subFile = null;
            File tempInput = null;
            File tempOutput = null;

            try {
                // 1. Copy video from Uri to app internal cache
                File cacheDir = context.getCacheDir();
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

                // 2. Generate .ASS Subtitle File
                int outWidth = "1080p".equalsIgnoreCase(resolution) ? 1080 : 720;
                int outHeight = "1080p".equalsIgnoreCase(resolution) ? 1920 : 1280;

                String assContent = AssGenerator.generateAss(words, style, outWidth, outHeight);
                subFile = new File(cacheDir, "captions_" + System.currentTimeMillis() + ".ass");
                try (FileOutputStream fos = new FileOutputStream(subFile)) {
                    fos.write(assContent.getBytes("UTF-8"));
                }

                // 3. Prepare Internal Destination
                tempOutput = new File(cacheDir, "rendered_" + System.currentTimeMillis() + ".mp4");

                // 4. Construct FFmpeg command
                String ffmpegCommand = String.format(
                        "-y -i \"%s\" -vf \"ass=%s\" -c:v libx264 -preset ultrafast -crf 21 -c:a aac -b:a 192k \"%s\"",
                        tempInput.getAbsolutePath(),
                        subFile.getAbsolutePath(),
                        tempOutput.getAbsolutePath()
                );

                Log.d(TAG, "Running Native FFmpeg: " + ffmpegCommand);

                FFmpegSession session = FFmpegKit.execute(ffmpegCommand);

                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    Log.i(TAG, "Render Succeeded. Inserting to MediaStore Gallery...");

                    String savedName = saveToGallery(context, tempOutput);

                    // Clean up temp files
                    if (subFile != null && subFile.exists()) subFile.delete();
                    if (tempInput != null && tempInput.exists()) tempInput.delete();
                    if (tempOutput != null && tempOutput.exists()) tempOutput.delete();

                    callback.onSuccess(savedName);
                } else {
                    String logs = session.getAllLogsAsString();
                    if (logs == null || logs.trim().isEmpty()) {
                        logs = session.getOutput();
                    }
                    Log.e(TAG, "FFmpeg failed: " + logs);

                    if (subFile != null && subFile.exists()) subFile.delete();
                    if (tempInput != null && tempInput.exists()) tempInput.delete();
                    if (tempOutput != null && tempOutput.exists()) tempOutput.delete();

                    callback.onError("Encoding failed: " + (logs != null ? logs : "Unknown error"));
                }

            } catch (Exception e) {
                Log.e(TAG, "Burn error", e);
                if (subFile != null && subFile.exists()) subFile.delete();
                if (tempInput != null && tempInput.exists()) tempInput.delete();
                if (tempOutput != null && tempOutput.exists()) tempOutput.delete();
                callback.onError("Render exception: " + e.getMessage());
            }
        }).start();
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
