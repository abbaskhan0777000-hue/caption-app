package com.captionforge.nativeapp.model;

import android.graphics.Color;

public class CaptionStyle {
    public String presetId;
    public String presetName;
    public String fontFamily;
    public int fontSize; // in sp/dp
    public int textColor;
    public int highlightColor;
    public int strokeColor;
    public float strokeWidth;
    public int backgroundColor;
    public boolean hasShadow;
    public int shadowColor;
    public String animationPreset; // "karaoke", "pop", "bounce", "fade", "typewriter", "clean"
    public float positionYPercent; // 10% to 90%
    public int wordsPerChunk;

    public CaptionStyle() {
        // Default Hormozi / CapCut Viral Style
        this.presetId = "hormozi_pop";
        this.presetName = "Hormozi Pop";
        this.fontFamily = "sans-serif-black";
        this.fontSize = 65;
        this.textColor = Color.WHITE;
        this.highlightColor = Color.parseColor("#FBBF24"); // Glowing Yellow
        this.strokeColor = Color.BLACK;
        this.strokeWidth = 10f;
        this.backgroundColor = Color.TRANSPARENT;
        this.hasShadow = true;
        this.shadowColor = Color.parseColor("#80000000");
        this.animationPreset = "karaoke";
        this.positionYPercent = 75f;
        this.wordsPerChunk = 4;
    }

    public static CaptionStyle createHormoziPreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "hormozi_pop";
        s.presetName = "Hormozi Pop";
        s.fontFamily = "sans-serif-black";
        s.fontSize = 75;
        s.textColor = Color.WHITE;
        s.highlightColor = Color.parseColor("#10B981"); // Neon Green
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 12f;
        s.backgroundColor = Color.parseColor("#D9000000"); // Dark Box
        s.animationPreset = "pop";
        return s;
    }

    public static CaptionStyle createCapCutKaraokePreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "capcut_karaoke";
        s.presetName = "CapCut Karaoke";
        s.fontFamily = "sans-serif-bold";
        s.fontSize = 65;
        s.textColor = Color.WHITE;
        s.highlightColor = Color.parseColor("#FBBF24"); // Yellow
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 10f;
        s.backgroundColor = Color.TRANSPARENT;
        s.animationPreset = "karaoke";
        return s;
    }

    public static CaptionStyle createCyberpunkNeonPreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "cyberpunk_neon";
        s.presetName = "Cyberpunk Neon";
        s.fontFamily = "monospace";
        s.fontSize = 60;
        s.textColor = Color.parseColor("#06B6D4"); // Cyan
        s.highlightColor = Color.parseColor("#EC4899"); // Pink
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 8f;
        s.backgroundColor = Color.TRANSPARENT;
        s.animationPreset = "pop";
        return s;
    }

    public static CaptionStyle createBeastModePreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "beast_mode";
        s.presetName = "Beast Mode";
        s.fontFamily = "sans-serif-black";
        s.fontSize = 80;
        s.textColor = Color.WHITE;
        s.highlightColor = Color.parseColor("#EF4444"); // Red Punch
        s.strokeColor = Color.BLACK;
        s.strokeWidth = 14f;
        s.backgroundColor = Color.TRANSPARENT;
        s.animationPreset = "bounce";
        return s;
    }

    public static CaptionStyle createCleanFadePreset() {
        CaptionStyle s = new CaptionStyle();
        s.presetId = "clean_fade";
        s.presetName = "Clean Minimal";
        s.fontFamily = "sans-serif-medium";
        s.fontSize = 50;
        s.textColor = Color.WHITE;
        s.highlightColor = Color.WHITE;
        s.strokeColor = Color.parseColor("#66000000");
        s.strokeWidth = 4f;
        s.backgroundColor = Color.TRANSPARENT;
        s.animationPreset = "fade";
        return s;
    }
}
