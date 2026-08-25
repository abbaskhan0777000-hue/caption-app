package com.captionforge.nativeapp.audio;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class AudioExtractor {
    private static final String TAG = "AudioExtractor";

    public interface ExtractCallback {
        void onSuccess(File wavFile);
        void onError(String error);
    }

    /**
     * Extracts audio track from video and converts it into standard 16kHz mono WAV for Whisper AI
     */
    public static void extractAudioWav(Context context, Uri videoUri, ExtractCallback callback) {
        new Thread(() -> {
            MediaExtractor extractor = new MediaExtractor();
            MediaCodec codec = null;
            File rawPcmFile = null;
            File wavFile = null;

            try {
                extractor.setDataSource(context, videoUri, null);
                int audioTrackIndex = -1;
                MediaFormat audioFormat = null;

                for (int i = 0; i < extractor.getTrackCount(); i++) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime != null && mime.startsWith("audio/")) {
                        audioTrackIndex = i;
                        audioFormat = format;
                        break;
                    }
                }

                if (audioTrackIndex == -1 || audioFormat == null) {
                    callback.onError("No audio track found in the selected video.");
                    return;
                }

                extractor.selectTrack(audioTrackIndex);
                String mime = audioFormat.getString(MediaFormat.KEY_MIME);
                int sampleRate = audioFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE) ? audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) : 44100;
                int channelCount = audioFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT) ? audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) : 2;

                codec = MediaCodec.createDecoderByType(mime);
                codec.configure(audioFormat, null, null, 0);
                codec.start();

                File cacheDir = context.getCacheDir();
                rawPcmFile = new File(cacheDir, "extracted_audio.raw");
                FileOutputStream pcmOut = new FileOutputStream(rawPcmFile);

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                boolean isEOS = false;
                long totalBytes = 0;

                while (!isEOS) {
                    int inIndex = codec.dequeueInputBuffer(10000);
                    if (inIndex >= 0) {
                        ByteBuffer inBuffer = codec.getInputBuffer(inIndex);
                        int sampleSize = extractor.readSampleData(inBuffer, 0);
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEOS = true;
                        } else {
                            codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }

                    int outIndex = codec.dequeueOutputBuffer(bufferInfo, 10000);
                    if (outIndex >= 0) {
                        ByteBuffer outBuffer = codec.getOutputBuffer(outIndex);
                        byte[] chunk = new byte[bufferInfo.size];
                        outBuffer.get(chunk);
                        outBuffer.clear();
                        pcmOut.write(chunk);
                        totalBytes += chunk.length;
                        codec.releaseOutputBuffer(outIndex, false);
                    }
                }

                pcmOut.flush();
                pcmOut.close();

                // Convert Raw PCM to standard 16-bit WAV
                wavFile = new File(cacheDir, "whisper_audio_" + System.currentTimeMillis() + ".wav");
                rawPcmToWav(rawPcmFile, wavFile, sampleRate, channelCount, 16);
                rawPcmFile.delete();

                callback.onSuccess(wavFile);

            } catch (Exception e) {
                Log.e(TAG, "Audio extraction failed", e);
                callback.onError("Audio extraction failed: " + e.getMessage());
            } finally {
                if (codec != null) {
                    try {
                        codec.stop();
                        codec.release();
                    } catch (Exception ignored) {}
                }
                extractor.release();
            }
        }).start();
    }

    private static void rawPcmToWav(File pcmFile, File wavFile, int sampleRate, int channels, int bitDepth) throws Exception {
        byte[] pcmData = new byte[(int) pcmFile.length()];
        try (java.io.FileInputStream fis = new java.io.FileInputStream(pcmFile)) {
            fis.read(pcmData);
        }

        try (FileOutputStream out = new FileOutputStream(wavFile)) {
            int totalAudioLen = pcmData.length;
            int totalDataLen = totalAudioLen + 36;
            int byteRate = sampleRate * channels * bitDepth / 8;

            byte[] header = new byte[44];
            header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
            header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
            header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0;
            header[20] = 1; header[21] = 0; // PCM
            header[22] = (byte) channels; header[23] = 0;
            header[24] = (byte) (sampleRate & 0xff);
            header[25] = (byte) ((sampleRate >> 8) & 0xff);
            header[26] = (byte) ((sampleRate >> 16) & 0xff);
            header[27] = (byte) ((sampleRate >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) (channels * bitDepth / 8); header[33] = 0;
            header[34] = (byte) bitDepth; header[35] = 0;
            header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
            header[40] = (byte) (totalAudioLen & 0xff);
            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

            out.write(header);
            out.write(pcmData);
            out.flush();
        }
    }
}
