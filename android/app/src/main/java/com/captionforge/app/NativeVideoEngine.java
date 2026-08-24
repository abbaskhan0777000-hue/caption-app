package com.captionforge.app;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
            try {
                // 1. Save ASS subtitle file to app cache
                File cacheDir = context.getCacheDir();
                File subFile = new File(cacheDir, "captions_" + System.currentTimeMillis() + ".ass");
                
                try (FileOutputStream fos = new FileOutputStream(subFile)) {
                    fos.write(assContent.getBytes());
                }

                // 2. Prepare destination in public Movies / CaptionForge gallery folder
                File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                File outDir = new File(moviesDir, "CaptionForge");
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }

                File outFile = new File(outDir, "CaptionForge_" + System.currentTimeMillis() + ".mp4");
                String outputPath = outFile.getAbsolutePath();

                // 3. Construct optimized Native C++ FFmpeg command
                // Uses ultrafast libx264 profile with direct hardware-accelerated subtitle rasterization
                String ffmpegCommand = String.format(
                        "-y -i \"%s\" -vf \"ass=%s\" -c:v libx264 -preset ultrafast -crf 21 -c:a aac -b:a 192k \"%s\"",
                        videoPath,
                        subFile.getAbsolutePath(),
                        outputPath
                );

                Log.d(TAG, "Executing Native FFmpeg: " + ffmpegCommand);

                // 4. Run FFmpegKit Native Session
                FFmpegSession session = FFmpegKit.execute(ffmpegCommand);

                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    Log.i(TAG, "Hardware Render Successful: " + outputPath);
                    
                    // Scan file so it shows instantly in Android Gallery / Photos
                    MediaScannerConnection.scanFile(
                            context,
                            new String[]{outputPath},
                            new String[]{"video/mp4"},
                            null
                    );

                    // Clean up temp sub file
                    subFile.delete();

                    callback.onSuccess(outputPath);
                } else {
                    String logs = session.getAllLogsAsString();
                    Log.e(TAG, "FFmpeg failed: " + logs);
                    subFile.delete();
                    callback.onError("Native video rendering failed: " + session.getFailStackTrace());
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception in NativeVideoEngine", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }
}
