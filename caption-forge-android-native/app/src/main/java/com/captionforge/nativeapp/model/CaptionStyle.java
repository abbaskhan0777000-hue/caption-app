package com.captionforge.nativeapp.model;

import android.graphics.Color;

public class CaptionStyle {
    public String presetId;
    public String presetName;
    public String fontFamily;
    public int fontSize; // in sp/dp (e.g. 24 in UI)
    public int textColor;
    public int highlightColor;
    public int strokeColor;
    public float strokeWidth;
    public int backgroundColor;
    public int highlightBgColor;
    public boolean hasShadow;
    public int shadowColor;
    public String animationPreset; // "karaoke", "pop", "bounce", "fade", "clean"
    public float positionYPercent; // 10% to 90%
    public int wordsPerChunk; // Word count per visibility (1 to 8)

    // Advanced Text & Effect Controls from Modern Studio
    public boolean isItalic;
    public boolean isBold;
    public boolean isUnderlined;
    public String textAlign; // "left", "center", "right"
    public String verticalAlign; // "top", "center", "bottom"
    public boolean singleLine;
    public boolean hasOutline;

    public CaptionStyle() {
        // Default: Elevate
        this.presetId = "elevate";
        this.presetName = "Elevate";
        this.fontFamily = "sans-serif-black";
        this.fontSize = 24;
        this.textColor = Color.WHITE;
        this.highlightColor = Color.parseColor("#FACC15"); // Glowing Yellow
        this.strokeColor = Color.BLACK;
        this.strokeWidth = 10f;
        this.backgroundColor = Color.TRANSPARENT;
        this.highlightBgColor = Color.TRANSPARENT;
        this.hasShadow = true;
        this.shadowColor = Color.parseColor("#80000000");
        this.animationPreset = "karaoke";
        this.positionYPercent = 75f;
        this.wordsPerChunk = 3;

        this.isItalic = false;
        this.isBold = true;
        this.isUnderlined = false;
        this.textAlign = "center";
        this.verticalAlign = "bottom";
        this.singleLine = false;
        this.hasOutline = true;
    }

    // 1. Basic Subtitles (Screenshot 4)
    public static CaptionStyle createBasicSubtitlesPreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "basic_subtitles";
        s.presetName = "Basic Subtitles";
        s.fontFamily = "sans-serif-bold";
        s.fontSize = 20;
        s.textColor = Color.WHITE;
        s.highlightColor = Color.WHITE;
        s.strokeColor = Color.parseColor("#80000000");
        s.strokeWidth = 6f;
        s.backgroundColor = Color.TRANSPARENT;
        s.highlightBgColor = Color.TRANSPARENT;
        s.animationPreset = "clean";
        s.wordsPerChunk = 4;
        return s;
    }

    // 2. Elevate (Screenshot 4)
    public static CaptionStyle createElevatePreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "elevate";
        s.presetName = "Elevate";
        s.fontFamily = "sans-serif-black";
        s.fontSize = 24;
        s.textColor = Color.WHITE;
        s.highlightColor = Color.parseColor("#FACC15"); // Vibrant Yellow
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 12f;
        s.backgroundColor = Color.TRANSPARENT;
        s.highlightBgColor = Color.TRANSPARENT;
        s.animationPreset = "karaoke";
        s.wordsPerChunk = 3;
        return s;
    }

    // 3. One Word Punch (Screenshot 4)
    public static CaptionStyle createOneWordPreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "one_word";
        s.presetName = "One Word";
        s.fontFamily = "sans-serif-black";
        s.fontSize = 28;
        s.textColor = Color.parseColor("#FACC15");
        s.highlightColor = Color.parseColor("#FACC15");
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 14f;
        s.backgroundColor = Color.TRANSPARENT;
        s.highlightBgColor = Color.TRANSPARENT;
        s.animationPreset = "pop";
        s.wordsPerChunk = 1;
        s.singleLine = true;
        return s;
    }

    // 4. Two Word Sync (Screenshot 4)
    public static CaptionStyle createTwoWordPreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "two_word";
        s.presetName = "Two Word";
        s.fontFamily = "sans-serif-black";
        s.fontSize = 26;
        s.textColor = Color.parseColor("#22C55E"); // Neon Green
        s.highlightColor = Color.parseColor("#22C55E");
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 12f;
        s.backgroundColor = Color.TRANSPARENT;
        s.highlightBgColor = Color.TRANSPARENT;
        s.animationPreset = "pop";
        s.wordsPerChunk = 2;
        s.singleLine = true;
        return s;
    }

    // 5. Word Color Change (Screenshot 4)
    public static CaptionStyle createWordColorChangePreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "word_color_change";
        s.presetName = "Word Color Change";
        s.fontFamily = "sans-serif-bold";
        s.fontSize = 22;
        s.textColor = Color.WHITE;
        s.highlightColor = Color.parseColor("#FACC15");
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 8f;
        s.backgroundColor = Color.TRANSPARENT;
        s.highlightBgColor = Color.TRANSPARENT;
        s.animationPreset = "karaoke";
        s.wordsPerChunk = 4;
        return s;
    }

    // 6. Word Background Change (Screenshot 4)
    public static CaptionStyle createWordBackgroundChangePreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "word_bg_change";
        s.presetName = "Word Background Change";
        s.fontFamily = "sans-serif-black";
        s.fontSize = 22;
        s.textColor = Color.WHITE;
        s.highlightColor = Color.WHITE;
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 6f;
        s.backgroundColor = Color.TRANSPARENT;
        s.highlightBgColor = Color.parseColor("#38BDF8"); // Blue active box
        s.animationPreset = "karaoke";
        s.wordsPerChunk = 4;
        return s;
    }

    // 7. CapCut Cyber Glow (CapCut Trend)
    public static CaptionStyle createCapCutCyberGlowPreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "capcut_cyber_glow";
        s.presetName = "CapCut Glow";
        s.fontFamily = "monospace";
        s.fontSize = 24;
        s.textColor = Color.parseColor("#06B6D4"); // Cyan
        s.highlightColor = Color.parseColor("#F43F5E"); // Hot Pink Glow
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 10f;
        s.backgroundColor = Color.parseColor("#CC000000"); // Dark Back Box
        s.highlightBgColor = Color.TRANSPARENT;
        s.animationPreset = "karaoke";
        s.wordsPerChunk = 3;
        return s;
    }

    // 8. MrBeast Impact (Viral Trend)
    public static CaptionStyle createMrBeastPreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "mr_beast";
        s.presetName = "MrBeast Impact";
        s.fontFamily = "sans-serif-black";
        s.fontSize = 28;
        s.textColor = Color.WHITE;
        s.highlightColor = Color.parseColor("#22C55E"); // Neon Green
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 16f;
        s.backgroundColor = Color.TRANSPARENT;
        s.highlightBgColor = Color.TRANSPARENT;
        s.animationPreset = "pop";
        s.wordsPerChunk = 2;
        return s;
    }

    // 9. Ali Abdaal Minimal (Clean Aesthetic)
    public static CaptionStyle createAliAbdaalPreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "ali_abdaal";
        s.presetName = "Ali Abdaal Clean";
        s.fontFamily = "serif";
        s.fontSize = 20;
        s.textColor = Color.parseColor("#FEF3C7"); // Warm Cream
        s.highlightColor = Color.parseColor("#F59E0B"); // Warm Amber
        s.strokeColor = Color.parseColor("#4D000000");
        s.strokeWidth = 4f;
        s.backgroundColor = Color.TRANSPARENT;
        s.highlightBgColor = Color.TRANSPARENT;
        s.animationPreset = "clean";
        s.wordsPerChunk = 4;
        return s;
    }

    // 10. Red Punch / Beast Box (Viral Trend)
    public static CaptionStyle createRedPunchPreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "red_punch";
        s.presetName = "Red Punch";
        s.fontFamily = "sans-serif-black";
        s.fontSize = 24;
        s.textColor = Color.WHITE;
        s.highlightColor = Color.parseColor("#EF4444"); // Vibrant Red
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 10f;
        s.backgroundColor = Color.parseColor("#E6000000");
        s.highlightBgColor = Color.TRANSPARENT;
        s.animationPreset = "pop";
        s.wordsPerChunk = 3;
        return s;
    }

    // 11. Typewriter Neon (CapCut Retro)
    public static CaptionStyle createTypewriterNeonPreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "typewriter_neon";
        s.presetName = "Typewriter Neon";
        s.fontFamily = "monospace";
        s.fontSize = 22;
        s.textColor = Color.parseColor("#A7F3D0"); // Mint
        s.highlightColor = Color.parseColor("#10B981"); // Emerald
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 8f;
        s.backgroundColor = Color.TRANSPARENT;
        s.highlightBgColor = Color.TRANSPARENT;
        s.animationPreset = "clean";
        s.wordsPerChunk = 4;
        return s;
    }

    // 12. Golden Luxury (CapCut Elegant)
    public static CaptionStyle createGoldenLuxuryPreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "golden_luxury";
        s.presetName = "Golden Luxury";
        s.fontFamily = "serif";
        s.fontSize = 24;
        s.textColor = Color.parseColor("#FDE68A"); // Soft Gold
        s.highlightColor = Color.parseColor("#F59E0B"); // Rich Gold
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 10f;
        s.backgroundColor = Color.TRANSPARENT;
        s.highlightBgColor = Color.TRANSPARENT;
        s.animationPreset = "karaoke";
        s.wordsPerChunk = 3;
        return s;
    }
}
