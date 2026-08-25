package com.captionforge.app;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NativeVideoEngine {
    private static final String TAG = "NativeVideoEngine";

    public interface RenderCallback {
        void onProgress(int percentage);
        void onSuccess(String outputPath);
        void onError(String errorMessage);
    }

    /**
     * Burns styled ASS subtitles directly into video using Native C++ ARM64 Hardware FFmpeg
     */
    public static void burnCaptions(
            Context context,
            String videoPath,
            String assContent,
            RenderCallback callback
    ) {
        new Thread(() -> {
            File subFile = null;
            File tempOutFile = null;
            try {
                if (videoPath == null || videoPath.trim().isEmpty()) {
                    callback.onError("Video input file is missing.");
                    return;
                }

                File inputFile = new File(videoPath);
                if (!inputFile.exists() || inputFile.length() == 0) {
                    callback.onError("Video input file does not exist on device cache: " + videoPath);
                    return;
                }

                // 1. Save ASS subtitle file to app cache
                File cacheDir = context.getCacheDir();
                subFile = new File(cacheDir, "captions_" + System.currentTimeMillis() + ".ass");
                try (FileOutputStream fos = new FileOutputStream(subFile)) {
                    fos.write(assContent.getBytes("UTF-8"));
                }

                // 2. Prepare temp output in app internal cache (never blocked by Android Scoped Storage)
                tempOutFile = new File(cacheDir, "rendered_" + System.currentTimeMillis() + ".mp4");
                String tempOutputPath = tempOutFile.getAbsolutePath();

                // 3. Construct optimized Native C++ FFmpeg command
                // Uses ultrafast libx264 with hardware subtitle rasterization
                String ffmpegCommand = String.format(
                        "-y -i \"%s\" -vf \"ass=%s\" -c:v libx264 -preset ultrafast -crf 21 -c:a aac -b:a 192k \"%s\"",
                        videoPath,
                        subFile.getAbsolutePath(),
                        tempOutputPath
                );

                Log.d(TAG, "Executing Native FFmpeg: " + ffmpegCommand);

                // 4. Run FFmpegKit Native Session
                FFmpegSession session = FFmpegKit.execute(ffmpegCommand);

                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    Log.i(TAG, "Hardware Render Succeeded in cache: " + tempOutputPath);

                    // 5. Save to Android Public MediaStore (Photos / Gallery / Movies)
                    String finalGalleryPath = saveVideoToGallery(context, tempOutFile);

                    // Clean up temp files
                    if (subFile != null && subFile.exists()) subFile.delete();
                    if (tempOutFile != null && tempOutFile.exists()) tempOutFile.delete();

                    callback.onSuccess(finalGalleryPath);
                } else {
                    String logs = session.getAllLogsAsString();
                    if (logs == null || logs.trim().isEmpty()) {
                        logs = session.getOutput();
                    }
                    Log.e(TAG, "FFmpeg failed: " + logs);
                    if (subFile != null && subFile.exists()) subFile.delete();
                    if (tempOutFile != null && tempOutFile.exists()) tempOutFile.delete();
                    callback.onError("FFmpeg encoding failed: " + (logs != null ? logs : "Unknown error"));
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception in NativeVideoEngine", e);
                if (subFile != null && subFile.exists()) subFile.delete();
                if (tempOutFile != null && tempOutFile.exists()) tempOutFile.delete();
                callback.onError("Video engine error: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Saves rendered video file into Android's public MediaStore (Movies/CaptionForge)
     * Compatible with Android 10, 11, 12, 13, 14, 15 Scoped Storage
     */
    private static String saveVideoToGallery(Context context, File sourceFile) throws Exception {
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
            throw new IOException("Failed to create MediaStore record.");
        }

        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = resolver.openOutputStream(itemUri)) {
            if (out == null) {
                throw new IOException("Failed to open MediaStore output stream.");
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
