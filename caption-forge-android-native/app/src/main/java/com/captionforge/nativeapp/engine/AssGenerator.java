package com.captionforge.nativeapp.engine;

import com.captionforge.nativeapp.model.CaptionStyle;
import com.captionforge.nativeapp.model.WordCaption;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AssGenerator {

    public static String hexToAssColor(int color, int alpha) {
        int a = (255 - alpha) & 0xFF; // In ASS 00 is opaque, FF is transparent
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return String.format(Locale.US, "&H%02X%02X%02X%02X&", a, b, g, r);
    }

    public static String formatAssTime(double seconds) {
        int totalCs = (int) Math.max(0, Math.round(seconds * 100));
        int cs = totalCs % 100;
        int totalSec = totalCs / 100;
        int s = totalSec % 60;
        int totalMin = totalSec / 60;
        int m = totalMin % 60;
        int h = totalMin / 60;
        return String.format(Locale.US, "%d:%02d:%02d.%02d", h, m, s, cs);
    }

    public static class CaptionChunk {
        public List<WordCaption> words = new ArrayList<>();
        public double start;
        public double end;
    }

    public static List<CaptionChunk> chunkWords(List<WordCaption> words, int wordsPerChunk) {
        List<CaptionChunk> chunks = new ArrayList<>();
        if (words == null || words.isEmpty()) return chunks;

        CaptionChunk current = new CaptionChunk();
        for (int i = 0; i < words.size(); i++) {
            WordCaption word = words.get(i);
            WordCaption prev = current.words.isEmpty() ? null : current.words.get(current.words.size() - 1);

            boolean isPause = prev != null && (word.getStart() - prev.getEnd() > 0.6);
            boolean isFull = current.words.size() >= wordsPerChunk;

            if (!current.words.isEmpty() && (isPause || isFull)) {
                current.start = current.words.get(0).getStart();
                current.end = current.words.get(current.words.size() - 1).getEnd();
                chunks.add(current);
                current = new CaptionChunk();
            }
            current.words.add(word);
        }

        if (!current.words.isEmpty()) {
            current.start = current.words.get(0).getStart();
            current.end = current.words.get(current.words.size() - 1).getEnd();
            chunks.add(current);
        }

        return chunks;
    }

    public static String generateAss(List<WordCaption> words, CaptionStyle style, int videoWidth, int videoHeight) {
        String primaryColor = hexToAssColor(style.textColor, 255);
        String secondaryColor = hexToAssColor(style.highlightColor, 255);
        String outlineColor = hexToAssColor(style.strokeColor, 255);
        String shadowColor = hexToAssColor(style.shadowColor, 128);

        int posY = (int) ((style.positionYPercent / 100.0) * videoHeight);
        int marginV = videoHeight - posY;

        boolean hasBg = style.backgroundColor != 0 && style.backgroundColor != android.graphics.Color.TRANSPARENT;
        int borderStyle = hasBg ? 3 : 1;
        String backColor = hasBg ? hexToAssColor(style.backgroundColor, 217) : shadowColor;

        int fontScale = (int) (style.fontSize * 1.5);
        int outline = (int) style.strokeWidth;
        int shadow = style.hasShadow ? 3 : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("[Script Info]\n");
        sb.append("Title: CaptionForge Subtitles\n");
        sb.append("ScriptType: v4.00+\n");
        sb.append("PlayResX: ").append(videoWidth).append("\n");
        sb.append("PlayResY: ").append(videoHeight).append("\n");
        sb.append("WrapStyle: 0\n");
        sb.append("ScaledBorderAndShadow: yes\n\n");

        sb.append("[V4+ Styles]\n");
        sb.append("Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\n");
        sb.append(String.format(Locale.US, "Style: CaptionDefault,%s,%d,%s,%s,%s,%s,1,0,0,0,100,100,1,0,%d,%d,%d,2,30,30,%d,1\n\n",
                style.fontFamily, fontScale, primaryColor, secondaryColor, outlineColor, backColor, borderStyle, outline, shadow, marginV));

        sb.append("[Events]\n");
        sb.append("Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n");

        List<CaptionChunk> chunks = chunkWords(words, style.wordsPerChunk);
        for (CaptionChunk chunk : chunks) {
            String startTime = formatAssTime(chunk.start);
            String endTime = formatAssTime(chunk.end + 0.15);

            StringBuilder line = new StringBuilder();
            if ("karaoke".equalsIgnoreCase(style.animationPreset)) {
                for (WordCaption w : chunk.words) {
                    int durationCs = (int) Math.max(5, Math.round((w.getEnd() - w.getStart()) * 100));
                    line.append("{\\k").append(durationCs).append("}").append(w.getWord().toUpperCase()).append(" ");
                }
            } else if ("pop".equalsIgnoreCase(style.animationPreset)) {
                for (WordCaption w : chunk.words) {
                    line.append("{\\t(0,120,\\fscx118\\fscy118)\\t(120,250,\\fscx100\\fscy100)}").append(w.getWord().toUpperCase()).append(" ");
                }
            } else {
                for (WordCaption w : chunk.words) {
                    line.append(w.getWord().toUpperCase()).append(" ");
                }
            }

            sb.append(String.format(Locale.US, "Dialogue: 0,%s,%s,CaptionDefault,,0,0,0,,%s\n", startTime, endTime, line.toString().trim()));
        }

        return sb.toString();
    }
}
