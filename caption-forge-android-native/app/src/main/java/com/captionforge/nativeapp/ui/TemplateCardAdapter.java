package com.captionforge.nativeapp.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.model.CaptionStyle;

import java.util.ArrayList;
import java.util.List;

public class TemplateCardAdapter extends RecyclerView.Adapter<TemplateCardAdapter.TemplateViewHolder> {

    public interface OnTemplateSelectedListener {
        void onTemplateSelected(CaptionStyle style);
    }

    public static class TemplateItem {
        public String id;
        public String category;
        public String name;
        public CharSequence sampleSpannable;
        public CaptionStyle style;

        public TemplateItem(String id, String category, String name, CharSequence sample, CaptionStyle style) {
            this.id = id;
            this.category = category;
            this.name = name;
            this.sampleSpannable = sample;
            this.style = style;
        }
    }

    private final List<TemplateItem> allItems = new ArrayList<>();
    private final List<TemplateItem> displayedItems = new ArrayList<>();
    private String currentCategory = "Legacy";
    private String selectedId = "leg_karaoke";
    private final OnTemplateSelectedListener listener;

    public TemplateCardAdapter(OnTemplateSelectedListener listener) {
        this.listener = listener;
        initAllTemplates();
        filterByCategory("Legacy");
    }

    private void initAllTemplates() {
        // ==========================================
        // 1. LEGACY (Classic Subtitle Styles)
        // ==========================================
        // 1.1 Classic Karaoke
        SpannableString leg1 = new SpannableString("ELEVATE YOUR WORDS");
        leg1.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg1.setSpan(new ForegroundColorSpan(Color.WHITE), 8, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg1.setSpan(new StyleSpan(Typeface.BOLD), 0, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg1 = new CaptionStyle();
        sLeg1.presetId = "leg_karaoke";
        sLeg1.presetName = "Classic Karaoke";
        sLeg1.fontFamily = "sans-serif-black";
        sLeg1.textColor = Color.WHITE;
        sLeg1.highlightColor = Color.parseColor("#FACC15");
        sLeg1.hasOutline = true;
        sLeg1.strokeColor = Color.BLACK;
        sLeg1.strokeWidth = 10f;
        sLeg1.wordsPerChunk = 3;
        allItems.add(new TemplateItem("leg_karaoke", "Legacy", "CLASSIC KARAOKE", leg1, sLeg1));

        // 1.2 Navy White Pill
        SpannableString leg2 = new SpannableString("SMART CONTENT CREATION");
        leg2.setSpan(new BackgroundColorSpan(Color.WHITE), 0, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg2.setSpan(new ForegroundColorSpan(Color.parseColor("#1E3A8A")), 6, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg2 = new CaptionStyle();
        sLeg2.presetId = "leg_navy_pill";
        sLeg2.presetName = "Navy White Pill";
        sLeg2.backgroundColor = Color.WHITE;
        sLeg2.highlightColor = Color.BLACK;
        sLeg2.textColor = Color.parseColor("#1E3A8A");
        sLeg2.wordsPerChunk = 3;
        allItems.add(new TemplateItem("leg_navy_pill", "Legacy", "NAVY WHITE PILL", leg2, sLeg2));

        // 1.3 Yellow Highlight Box
        SpannableString leg3 = new SpannableString("HIGHLIGHT KEY PHRASES");
        leg3.setSpan(new BackgroundColorSpan(Color.BLACK), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg3.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg3.setSpan(new ForegroundColorSpan(Color.WHITE), 10, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg3 = new CaptionStyle();
        sLeg3.presetId = "leg_yellow_box";
        sLeg3.presetName = "Yellow Highlight Box";
        sLeg3.highlightBgColor = Color.BLACK;
        sLeg3.highlightColor = Color.parseColor("#FACC15");
        sLeg3.textColor = Color.WHITE;
        sLeg3.wordsPerChunk = 3;
        allItems.add(new TemplateItem("leg_yellow_box", "Legacy", "YELLOW HIGHLIGHT BOX", leg3, sLeg3));

        // 1.4 Clean Minimal White
        SpannableString leg4 = new SpannableString("Simple clear storytelling");
        leg4.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 25, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg4 = new CaptionStyle();
        sLeg4.presetId = "leg_clean_white";
        sLeg4.presetName = "Clean Minimal White";
        sLeg4.fontFamily = "sans-serif";
        sLeg4.textColor = Color.WHITE;
        sLeg4.highlightColor = Color.parseColor("#E2E8F0");
        sLeg4.hasOutline = false;
        sLeg4.hasShadow = true;
        sLeg4.shadowColor = Color.parseColor("#99000000");
        sLeg4.wordsPerChunk = 4;
        allItems.add(new TemplateItem("leg_clean_white", "Legacy", "CLEAN MINIMAL WHITE", leg4, sLeg4));

        // 1.5 Royal Blue Pill
        SpannableString leg5 = new SpannableString("EXPRESS WITH CONFIDENCE");
        leg5.setSpan(new BackgroundColorSpan(Color.WHITE), 0, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg5.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg5.setSpan(new ForegroundColorSpan(Color.parseColor("#2563EB")), 8, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg5 = new CaptionStyle();
        sLeg5.presetId = "leg_royal_pill";
        sLeg5.presetName = "Royal Blue Pill";
        sLeg5.backgroundColor = Color.WHITE;
        sLeg5.highlightColor = Color.parseColor("#FACC15");
        sLeg5.textColor = Color.parseColor("#2563EB");
        sLeg5.wordsPerChunk = 3;
        allItems.add(new TemplateItem("leg_royal_pill", "Legacy", "ROYAL BLUE PILL", leg5, sLeg5));

        // 1.6 Dark Obsidian Pill
        SpannableString leg6 = new SpannableString("THE SECRETS OF SUCCESS");
        leg6.setSpan(new BackgroundColorSpan(Color.BLACK), 0, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg6.setSpan(new ForegroundColorSpan(Color.parseColor("#FDE047")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg6.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg6 = new CaptionStyle();
        sLeg6.presetId = "leg_obsidian_pill";
        sLeg6.presetName = "Dark Obsidian Pill";
        sLeg6.backgroundColor = Color.BLACK;
        sLeg6.highlightColor = Color.parseColor("#FDE047");
        sLeg6.textColor = Color.WHITE;
        sLeg6.wordsPerChunk = 4;
        allItems.add(new TemplateItem("leg_obsidian_pill", "Legacy", "DARK OBSIDIAN PILL", leg6, sLeg6));

        // ==========================================
        // 2. MODERN (Sleek, Clean & Aesthetic)
        // ==========================================
        // 2.1 Ali Abdaal Aesthetic
        SpannableString mod1 = new SpannableString("Aesthetic productivity habits");
        mod1.setSpan(new ForegroundColorSpan(Color.parseColor("#FEF3C7")), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod1.setSpan(new ForegroundColorSpan(Color.parseColor("#F8FAFC")), 10, 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMod1 = new CaptionStyle();
        sMod1.presetId = "mod_abdaal";
        sMod1.presetName = "Ali Abdaal Aesthetic";
        sMod1.fontFamily = "sans-serif-medium";
        sMod1.highlightColor = Color.parseColor("#FEF3C7");
        sMod1.textColor = Color.parseColor("#F8FAFC");
        sMod1.hasOutline = false;
        sMod1.hasShadow = true;
        sMod1.shadowColor = Color.parseColor("#80000000");
        sMod1.wordsPerChunk = 3;
        allItems.add(new TemplateItem("mod_abdaal", "Modern", "ALI ABDAAL AESTHETIC", mod1, sMod1));

        // 2.2 Electric Cyan Clean
        SpannableString mod2 = new SpannableString("THE FUTURE OF AI");
        mod2.setSpan(new ForegroundColorSpan(Color.parseColor("#38BDF8")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod2.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMod2 = new CaptionStyle();
        sMod2.presetId = "mod_cyan_clean";
        sMod2.presetName = "Electric Cyan Clean";
        sMod2.fontFamily = "sans-serif-medium";
        sMod2.highlightColor = Color.parseColor("#38BDF8");
        sMod2.textColor = Color.WHITE;
        sMod2.hasShadow = true;
        sMod2.wordsPerChunk = 4;
        allItems.add(new TemplateItem("mod_cyan_clean", "Modern", "ELECTRIC CYAN CLEAN", mod2, sMod2));

        // 2.3 Velvet Violet
        SpannableString mod3 = new SpannableString("PREMIUM STUDIO QUALITY");
        mod3.setSpan(new BackgroundColorSpan(Color.parseColor("#7C3AED")), 0, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod3.setSpan(new ForegroundColorSpan(Color.parseColor("#FBBF24")), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod3.setSpan(new ForegroundColorSpan(Color.WHITE), 8, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMod3 = new CaptionStyle();
        sMod3.presetId = "mod_velvet_violet";
        sMod3.presetName = "Velvet Violet";
        sMod3.backgroundColor = Color.parseColor("#7C3AED");
        sMod3.highlightColor = Color.parseColor("#FBBF24");
        sMod3.textColor = Color.WHITE;
        sMod3.wordsPerChunk = 3;
        allItems.add(new TemplateItem("mod_velvet_violet", "Modern", "VELVET VIOLET", mod3, sMod3));

        // 2.4 Emerald Growth
        SpannableString mod4 = new SpannableString("SCALING TO ONE MILLION");
        mod4.setSpan(new ForegroundColorSpan(Color.parseColor("#10B981")), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod4.setSpan(new ForegroundColorSpan(Color.WHITE), 8, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMod4 = new CaptionStyle();
        sMod4.presetId = "mod_emerald_growth";
        sMod4.presetName = "Emerald Growth";
        sMod4.fontFamily = "sans-serif-black";
        sMod4.highlightColor = Color.parseColor("#10B981");
        sMod4.textColor = Color.WHITE;
        sMod4.hasOutline = true;
        sMod4.strokeColor = Color.parseColor("#064E3B");
        sMod4.strokeWidth = 8f;
        sMod4.wordsPerChunk = 4;
        allItems.add(new TemplateItem("mod_emerald_growth", "Modern", "EMERALD GROWTH", mod4, sMod4));

        // 2.5 Sunset Fade Italic
        SpannableString mod5 = new SpannableString("Unstoppable daily momentum");
        mod5.setSpan(new ForegroundColorSpan(Color.parseColor("#FB923C")), 0, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod5.setSpan(new ForegroundColorSpan(Color.WHITE), 12, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod5.setSpan(new StyleSpan(Typeface.ITALIC), 0, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMod5 = new CaptionStyle();
        sMod5.presetId = "mod_sunset_fade";
        sMod5.presetName = "Sunset Fade Italic";
        sMod5.isItalic = true;
        sMod5.highlightColor = Color.parseColor("#FB923C");
        sMod5.textColor = Color.WHITE;
        sMod5.hasShadow = true;
        sMod5.wordsPerChunk = 3;
        allItems.add(new TemplateItem("mod_sunset_fade", "Modern", "SUNSET FADE ITALIC", mod5, sMod5));

        // 2.6 Glassmorphism Cyan
        SpannableString mod6 = new SpannableString("TRANSPARENT GLASS UI");
        mod6.setSpan(new BackgroundColorSpan(Color.parseColor("#66000000")), 0, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod6.setSpan(new ForegroundColorSpan(Color.parseColor("#22D3EE")), 0, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod6.setSpan(new ForegroundColorSpan(Color.WHITE), 12, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMod6 = new CaptionStyle();
        sMod6.presetId = "mod_glass_cyan";
        sMod6.presetName = "Glassmorphism Cyan";
        sMod6.backgroundColor = Color.parseColor("#66000000");
        sMod6.highlightColor = Color.parseColor("#22D3EE");
        sMod6.textColor = Color.WHITE;
        sMod6.wordsPerChunk = 3;
        allItems.add(new TemplateItem("mod_glass_cyan", "Modern", "GLASSMORPHISM CYAN", mod6, sMod6));

        // ==========================================
        // 3. VIRAL (TikTok & Reels High-Retention)
        // ==========================================
        // 3.1 Hormozi Fire
        SpannableString vir1 = new SpannableString("MAKE $100K PER MONTH");
        vir1.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        vir1.setSpan(new ForegroundColorSpan(Color.parseColor("#EF4444")), 5, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sVir1 = new CaptionStyle();
        sVir1.presetId = "vir_hormozi";
        sVir1.presetName = "Hormozi Fire";
        sVir1.fontFamily = "sans-serif-black";
        sVir1.highlightColor = Color.parseColor("#FACC15");
        sVir1.textColor = Color.parseColor("#EF4444");
        sVir1.hasOutline = true;
        sVir1.strokeColor = Color.BLACK;
        sVir1.strokeWidth = 14f;
        sVir1.wordsPerChunk = 2;
        allItems.add(new TemplateItem("vir_hormozi", "Viral", "HORMOZI FIRE", vir1, sVir1));

        // 3.2 MrBeast Impact
        SpannableString vir2 = new SpannableString("SURVIVED 100 DAYS HERE!");
        vir2.setSpan(new ForegroundColorSpan(Color.parseColor("#22C55E")), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        vir2.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 9, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sVir2 = new CaptionStyle();
        sVir2.presetId = "vir_mrbeast";
        sVir2.presetName = "MrBeast Impact";
        sVir2.fontFamily = "sans-serif-black";
        sVir2.highlightColor = Color.parseColor("#22C55E");
        sVir2.textColor = Color.parseColor("#FACC15");
        sVir2.hasOutline = true;
        sVir2.strokeColor = Color.BLACK;
        sVir2.strokeWidth = 14f;
        sVir2.wordsPerChunk = 2;
        allItems.add(new TemplateItem("vir_mrbeast", "Viral", "MR BEAST IMPACT", vir2, sVir2));

        // 3.3 TikTok 1-Word Punch
        SpannableString vir3 = new SpannableString("BOOM!");
        vir3.setSpan(new BackgroundColorSpan(Color.parseColor("#EF4444")), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        vir3.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sVir3 = new CaptionStyle();
        sVir3.presetId = "vir_1word_punch";
        sVir3.presetName = "TikTok 1-Word Punch";
        sVir3.fontFamily = "sans-serif-black";
        sVir3.fontSize = 28;
        sVir3.highlightBgColor = Color.parseColor("#EF4444");
        sVir3.highlightColor = Color.WHITE;
        sVir3.textColor = Color.WHITE;
        sVir3.wordsPerChunk = 1;
        sVir3.singleLine = true;
        allItems.add(new TemplateItem("vir_1word_punch", "Viral", "TIKTOK 1-WORD PUNCH", vir3, sVir3));

        // 3.4 Bebas Condensed Hook
        SpannableString vir4 = new SpannableString("HOW TO GO VIRAL FAST");
        vir4.setSpan(new ForegroundColorSpan(Color.parseColor("#00F2FE")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        vir4.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sVir4 = new CaptionStyle();
        sVir4.presetId = "vir_bebas_hook";
        sVir4.presetName = "Bebas Condensed Hook";
        sVir4.fontFamily = "sans-serif-condensed-light";
        sVir4.highlightColor = Color.parseColor("#00F2FE");
        sVir4.textColor = Color.WHITE;
        sVir4.hasOutline = true;
        sVir4.strokeColor = Color.parseColor("#0F172A");
        sVir4.strokeWidth = 10f;
        sVir4.wordsPerChunk = 3;
        allItems.add(new TemplateItem("vir_bebas_hook", "Viral", "BEBAS CONDENSED HOOK", vir4, sVir4));

        // 3.5 Purple Beast Pill
        SpannableString vir5 = new SpannableString("UNLOCK YOUR SUPERPOWER");
        vir5.setSpan(new BackgroundColorSpan(Color.parseColor("#8B5CF6")), 0, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        vir5.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        vir5.setSpan(new ForegroundColorSpan(Color.WHITE), 7, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sVir5 = new CaptionStyle();
        sVir5.presetId = "vir_purple_beast";
        sVir5.presetName = "Purple Beast Pill";
        sVir5.backgroundColor = Color.parseColor("#8B5CF6");
        sVir5.highlightColor = Color.parseColor("#FACC15");
        sVir5.textColor = Color.WHITE;
        sVir5.wordsPerChunk = 3;
        allItems.add(new TemplateItem("vir_purple_beast", "Viral", "PURPLE BEAST PILL", vir5, sVir5));

        // 3.6 GaryVee Hustle
        SpannableString vir6 = new SpannableString("STOP OVERTHINKING NOW");
        vir6.setSpan(new ForegroundColorSpan(Color.parseColor("#A3E635")), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        vir6.setSpan(new ForegroundColorSpan(Color.WHITE), 5, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sVir6 = new CaptionStyle();
        sVir6.presetId = "vir_garyvee";
        sVir6.presetName = "GaryVee Hustle";
        sVir6.fontFamily = "sans-serif-black";
        sVir6.highlightColor = Color.parseColor("#A3E635");
        sVir6.textColor = Color.WHITE;
        sVir6.hasOutline = true;
        sVir6.strokeColor = Color.BLACK;
        sVir6.strokeWidth = 12f;
        sVir6.wordsPerChunk = 3;
        allItems.add(new TemplateItem("vir_garyvee", "Viral", "GARYVEE HUSTLE", vir6, sVir6));

        // ==========================================
        // 4. BOLD (Heavy & High-Energy)
        // ==========================================
        // 4.1 Red Alert Strike
        SpannableString bld1 = new SpannableString("DON'T MAKE THIS MISTAKE");
        bld1.setSpan(new ForegroundColorSpan(Color.parseColor("#DC2626")), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bld1.setSpan(new ForegroundColorSpan(Color.WHITE), 6, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sBld1 = new CaptionStyle();
        sBld1.presetId = "bld_red_alert";
        sBld1.presetName = "Red Alert Strike";
        sBld1.fontFamily = "sans-serif-black";
        sBld1.highlightColor = Color.parseColor("#DC2626");
        sBld1.textColor = Color.WHITE;
        sBld1.hasOutline = true;
        sBld1.strokeColor = Color.BLACK;
        sBld1.strokeWidth = 14f;
        sBld1.wordsPerChunk = 4;
        allItems.add(new TemplateItem("bld_red_alert", "Bold", "RED ALERT STRIKE", bld1, sBld1));

        // 4.2 Cinematic Gold
        SpannableString bld2 = new SpannableString("BUILD LASTING WEALTH");
        bld2.setSpan(new ForegroundColorSpan(Color.parseColor("#EAB308")), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bld2.setSpan(new ForegroundColorSpan(Color.parseColor("#FEF08A")), 6, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sBld2 = new CaptionStyle();
        sBld2.presetId = "bld_gold_rush";
        sBld2.presetName = "Cinematic Gold";
        sBld2.fontFamily = "sans-serif-black";
        sBld2.highlightColor = Color.parseColor("#EAB308");
        sBld2.textColor = Color.parseColor("#FEF08A");
        sBld2.hasOutline = true;
        sBld2.strokeColor = Color.parseColor("#78350F");
        sBld2.strokeWidth = 8f;
        sBld2.hasShadow = true;
        sBld2.shadowColor = Color.BLACK;
        sBld2.wordsPerChunk = 3;
        allItems.add(new TemplateItem("bld_gold_rush", "Bold", "CINEMATIC GOLD", bld2, sBld2));

        // 4.3 Cyberpunk 2077
        SpannableString bld3 = new SpannableString("HACK THE MATRIX TODAY");
        bld3.setSpan(new ForegroundColorSpan(Color.parseColor("#EC4899")), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bld3.setSpan(new ForegroundColorSpan(Color.parseColor("#06B6D4")), 5, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sBld3 = new CaptionStyle();
        sBld3.presetId = "bld_cyberpunk";
        sBld3.presetName = "Cyberpunk 2077";
        sBld3.fontFamily = "sans-serif-black";
        sBld3.highlightColor = Color.parseColor("#EC4899");
        sBld3.textColor = Color.parseColor("#06B6D4");
        sBld3.hasOutline = true;
        sBld3.strokeColor = Color.BLACK;
        sBld3.strokeWidth = 12f;
        sBld3.wordsPerChunk = 4;
        allItems.add(new TemplateItem("bld_cyberpunk", "Bold", "CYBERPUNK 2077", bld3, sBld3));

        // 4.4 Heavyweight Boxer
        SpannableString bld4 = new SpannableString("NEVER EVER GIVE UP");
        bld4.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bld4.setSpan(new ForegroundColorSpan(Color.WHITE), 6, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sBld4 = new CaptionStyle();
        sBld4.presetId = "bld_heavyweight";
        sBld4.presetName = "Heavyweight Boxer";
        sBld4.fontFamily = "sans-serif-black";
        sBld4.fontSize = 26;
        sBld4.highlightColor = Color.parseColor("#FACC15");
        sBld4.textColor = Color.WHITE;
        sBld4.hasOutline = true;
        sBld4.strokeColor = Color.BLACK;
        sBld4.strokeWidth = 14f;
        sBld4.wordsPerChunk = 2;
        allItems.add(new TemplateItem("bld_heavyweight", "Bold", "HEAVYWEIGHT BOXER", bld4, sBld4));

        // 4.5 Electric Voltage
        SpannableString bld5 = new SpannableString("MAXIMUM ENERGY OUTPUT");
        bld5.setSpan(new ForegroundColorSpan(Color.parseColor("#EAB308")), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bld5.setSpan(new ForegroundColorSpan(Color.parseColor("#A855F7")), 8, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sBld5 = new CaptionStyle();
        sBld5.presetId = "bld_voltage";
        sBld5.presetName = "Electric Voltage";
        sBld5.fontFamily = "sans-serif-black";
        sBld5.highlightColor = Color.parseColor("#EAB308");
        sBld5.textColor = Color.parseColor("#A855F7");
        sBld5.hasOutline = true;
        sBld5.strokeColor = Color.BLACK;
        sBld5.strokeWidth = 10f;
        sBld5.wordsPerChunk = 3;
        allItems.add(new TemplateItem("bld_voltage", "Bold", "ELECTRIC VOLTAGE", bld5, sBld5));

        // ==========================================
        // 5. MINIMAL (Subtle & Sophisticated)
        // ==========================================
        // 5.1 Minimalist Whisper
        SpannableString min1 = new SpannableString("The art of quiet focus");
        min1.setSpan(new ForegroundColorSpan(Color.parseColor("#FEF08A")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min1.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMin1 = new CaptionStyle();
        sMin1.presetId = "min_whisper";
        sMin1.presetName = "Minimalist Whisper";
        sMin1.fontFamily = "sans-serif";
        sMin1.highlightColor = Color.parseColor("#FEF08A");
        sMin1.textColor = Color.WHITE;
        sMin1.hasOutline = false;
        sMin1.hasShadow = true;
        sMin1.shadowColor = Color.parseColor("#66000000");
        sMin1.wordsPerChunk = 3;
        allItems.add(new TemplateItem("min_whisper", "Minimal", "MINIMALIST WHISPER", min1, sMin1));

        // 5.2 Lavender Breeze
        SpannableString min2 = new SpannableString("CREATE MEANINGFUL WORK");
        min2.setSpan(new BackgroundColorSpan(Color.parseColor("#DDD6FE")), 0, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min2.setSpan(new ForegroundColorSpan(Color.parseColor("#1E1B4B")), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min2.setSpan(new ForegroundColorSpan(Color.parseColor("#4338CA")), 7, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMin2 = new CaptionStyle();
        sMin2.presetId = "min_lavender";
        sMin2.presetName = "Lavender Breeze";
        sMin2.backgroundColor = Color.parseColor("#DDD6FE");
        sMin2.highlightColor = Color.parseColor("#1E1B4B");
        sMin2.textColor = Color.parseColor("#4338CA");
        sMin2.wordsPerChunk = 3;
        allItems.add(new TemplateItem("min_lavender", "Minimal", "LAVENDER BREEZE", min2, sMin2));

        // 5.3 Nordic Ice
        SpannableString min3 = new SpannableString("Simplicity in every detail");
        min3.setSpan(new ForegroundColorSpan(Color.parseColor("#93C5FD")), 0, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min3.setSpan(new ForegroundColorSpan(Color.WHITE), 11, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMin3 = new CaptionStyle();
        sMin3.presetId = "min_nordic_ice";
        sMin3.presetName = "Nordic Ice";
        sMin3.highlightColor = Color.parseColor("#93C5FD");
        sMin3.textColor = Color.WHITE;
        sMin3.hasOutline = false;
        sMin3.wordsPerChunk = 4;
        allItems.add(new TemplateItem("min_nordic_ice", "Minimal", "NORDIC ICE", min3, sMin3));

        // 5.4 Subtle Black Pill
        SpannableString min4 = new SpannableString("Daily mindful journaling");
        min4.setSpan(new BackgroundColorSpan(Color.BLACK), 0, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min4.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min4.setSpan(new ForegroundColorSpan(Color.WHITE), 6, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMin4 = new CaptionStyle();
        sMin4.presetId = "min_black_pill";
        sMin4.presetName = "Subtle Black Pill";
        sMin4.backgroundColor = Color.BLACK;
        sMin4.highlightColor = Color.parseColor("#FACC15");
        sMin4.textColor = Color.WHITE;
        sMin4.wordsPerChunk = 3;
        allItems.add(new TemplateItem("min_black_pill", "Minimal", "SUBTLE BLACK PILL", min4, sMin4));

        // 5.5 Monochrome Studio
        SpannableString min5 = new SpannableString("BLACK AND WHITE FOCUS");
        min5.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min5.setSpan(new ForegroundColorSpan(Color.parseColor("#94A3B8")), 6, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMin5 = new CaptionStyle();
        sMin5.presetId = "min_monochrome";
        sMin5.presetName = "Monochrome Studio";
        sMin5.highlightColor = Color.WHITE;
        sMin5.textColor = Color.parseColor("#94A3B8");
        sMin5.hasOutline = false;
        sMin5.wordsPerChunk = 4;
        allItems.add(new TemplateItem("min_monochrome", "Minimal", "MONOCHROME STUDIO", min5, sMin5));

        // ==========================================
        // 6. COOL (Neon, Pop & Synthwave)
        // ==========================================
        // 6.1 Neon Tokyo
        SpannableString cl1 = new SpannableString("NEON LIGHTS IN SHIBUYA");
        cl1.setSpan(new ForegroundColorSpan(Color.parseColor("#F43F5E")), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl1.setSpan(new ForegroundColorSpan(Color.parseColor("#06B6D4")), 5, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sCl1 = new CaptionStyle();
        sCl1.presetId = "cl_neon_tokyo";
        sCl1.presetName = "Neon Tokyo";
        sCl1.fontFamily = "sans-serif-black";
        sCl1.highlightColor = Color.parseColor("#F43F5E");
        sCl1.textColor = Color.parseColor("#06B6D4");
        sCl1.hasOutline = true;
        sCl1.strokeColor = Color.BLACK;
        sCl1.strokeWidth = 10f;
        sCl1.hasShadow = true;
        sCl1.shadowColor = Color.parseColor("#F43F5E");
        sCl1.wordsPerChunk = 4;
        allItems.add(new TemplateItem("cl_neon_tokyo", "Cool", "NEON TOKYO", cl1, sCl1));

        // 6.2 Hot Pink Pop
        SpannableString cl2 = new SpannableString("TRENDING ON SOCIAL MEDIA");
        cl2.setSpan(new BackgroundColorSpan(Color.parseColor("#EC4899")), 0, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl2.setSpan(new ForegroundColorSpan(Color.WHITE), 9, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sCl2 = new CaptionStyle();
        sCl2.presetId = "cl_pink_pop";
        sCl2.presetName = "Hot Pink Pop";
        sCl2.backgroundColor = Color.parseColor("#EC4899");
        sCl2.highlightColor = Color.BLACK;
        sCl2.textColor = Color.WHITE;
        sCl2.wordsPerChunk = 4;
        allItems.add(new TemplateItem("cl_pink_pop", "Cool", "HOT PINK POP", cl2, sCl2));

        // 6.3 Ice & Fire
        SpannableString cl3 = new SpannableString("FREEZING COLD VS HOT");
        cl3.setSpan(new ForegroundColorSpan(Color.parseColor("#38BDF8")), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl3.setSpan(new ForegroundColorSpan(Color.parseColor("#FB923C")), 9, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sCl3 = new CaptionStyle();
        sCl3.presetId = "cl_ice_fire";
        sCl3.presetName = "Ice & Fire";
        sCl3.fontFamily = "sans-serif-black";
        sCl3.highlightColor = Color.parseColor("#38BDF8");
        sCl3.textColor = Color.parseColor("#FB923C");
        sCl3.hasOutline = true;
        sCl3.strokeColor = Color.BLACK;
        sCl3.strokeWidth = 10f;
        sCl3.wordsPerChunk = 4;
        allItems.add(new TemplateItem("cl_ice_fire", "Cool", "ICE & FIRE", cl3, sCl3));

        // 6.4 Retro 80s Synthwave
        SpannableString cl4 = new SpannableString("SYNTHWAVE SUNSET VIBES");
        cl4.setSpan(new ForegroundColorSpan(Color.parseColor("#FDE047")), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl4.setSpan(new ForegroundColorSpan(Color.parseColor("#E11D48")), 10, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl4.setSpan(new StyleSpan(Typeface.ITALIC), 0, 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sCl4 = new CaptionStyle();
        sCl4.presetId = "cl_synthwave";
        sCl4.presetName = "Retro 80s Synthwave";
        sCl4.isItalic = true;
        sCl4.highlightColor = Color.parseColor("#FDE047");
        sCl4.textColor = Color.parseColor("#E11D48");
        sCl4.hasShadow = true;
        sCl4.shadowColor = Color.parseColor("#581C87");
        sCl4.wordsPerChunk = 3;
        allItems.add(new TemplateItem("cl_synthwave", "Cool", "RETRO 80S SYNTHWAVE", cl4, sCl4));

        // 6.5 Ocean Wave
        SpannableString cl5 = new SpannableString("DEEP BLUE OCEAN FLOW");
        cl5.setSpan(new BackgroundColorSpan(Color.parseColor("#1D4ED8")), 0, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl5.setSpan(new ForegroundColorSpan(Color.parseColor("#67E8F9")), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl5.setSpan(new ForegroundColorSpan(Color.WHITE), 5, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sCl5 = new CaptionStyle();
        sCl5.presetId = "cl_ocean_wave";
        sCl5.presetName = "Ocean Wave";
        sCl5.backgroundColor = Color.parseColor("#1D4ED8");
        sCl5.highlightColor = Color.parseColor("#67E8F9");
        sCl5.textColor = Color.WHITE;
        sCl5.wordsPerChunk = 4;
        allItems.add(new TemplateItem("cl_ocean_wave", "Cool", "OCEAN WAVE", cl5, sCl5));

        // 6.6 Acid Lime Pop
        SpannableString cl6 = new SpannableString("EXTREME VIRAL FORMULA");
        cl6.setSpan(new ForegroundColorSpan(Color.parseColor("#84CC16")), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl6.setSpan(new ForegroundColorSpan(Color.parseColor("#EA580C")), 8, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sCl6 = new CaptionStyle();
        sCl6.presetId = "cl_acid_lime";
        sCl6.presetName = "Acid Lime Pop";
        sCl6.fontFamily = "sans-serif-black";
        sCl6.highlightColor = Color.parseColor("#84CC16");
        sCl6.textColor = Color.parseColor("#EA580C");
        sCl6.hasOutline = true;
        sCl6.strokeColor = Color.BLACK;
        sCl6.strokeWidth = 10f;
        sCl6.wordsPerChunk = 3;
        allItems.add(new TemplateItem("cl_acid_lime", "Cool", "ACID LIME POP", cl6, sCl6));

        // ==========================================
        // 7. SPLIT VIEW (2-Line Multi-Structure)
        // ==========================================
        // 7.1 Dynamic Dual
        SpannableString sp1 = new SpannableString("MASTER YOUR MIND\nCONQUER THE WORLD");
        sp1.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp1.setSpan(new ForegroundColorSpan(Color.WHITE), 7, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sSp1 = new CaptionStyle();
        sSp1.presetId = "sp_dynamic_dual";
        sSp1.presetName = "Dynamic Dual";
        sSp1.fontFamily = "sans-serif-black";
        sSp1.highlightColor = Color.parseColor("#FACC15");
        sSp1.textColor = Color.WHITE;
        sSp1.hasOutline = true;
        sSp1.strokeColor = Color.BLACK;
        sSp1.strokeWidth = 10f;
        sSp1.wordsPerChunk = 3;
        allItems.add(new TemplateItem("sp_dynamic_dual", "Split view", "DYNAMIC DUAL 2-LINE", sp1, sSp1));

        // 7.2 Cyan Strike 2-Line
        SpannableString sp2 = new SpannableString("START TODAY\nNOT TOMORROW");
        sp2.setSpan(new ForegroundColorSpan(Color.parseColor("#38BDF8")), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp2.setSpan(new ForegroundColorSpan(Color.parseColor("#EF4444")), 12, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sSp2 = new CaptionStyle();
        sSp2.presetId = "sp_cyan_strike";
        sSp2.presetName = "Cyan Strike 2-Line";
        sSp2.fontFamily = "sans-serif-black";
        sSp2.highlightColor = Color.parseColor("#38BDF8");
        sSp2.textColor = Color.parseColor("#EF4444");
        sSp2.hasOutline = true;
        sSp2.strokeColor = Color.BLACK;
        sSp2.strokeWidth = 12f;
        sSp2.wordsPerChunk = 2;
        allItems.add(new TemplateItem("sp_cyan_strike", "Split view", "CYAN STRIKE 2-LINE", sp2, sSp2));

        // 7.3 Minimal Dark Box 2-Line
        SpannableString sp3 = new SpannableString("Focus on what matters\nIgnore all the noise");
        sp3.setSpan(new BackgroundColorSpan(Color.parseColor("#B3000000")), 0, 42, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp3.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp3.setSpan(new ForegroundColorSpan(Color.WHITE), 6, 42, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sSp3 = new CaptionStyle();
        sSp3.presetId = "sp_dark_box";
        sSp3.presetName = "Minimal Dark Box 2-Line";
        sSp3.backgroundColor = Color.parseColor("#B3000000");
        sSp3.highlightColor = Color.parseColor("#FACC15");
        sSp3.textColor = Color.WHITE;
        sSp3.wordsPerChunk = 4;
        allItems.add(new TemplateItem("sp_dark_box", "Split view", "MINIMAL DARK BOX 2-LINE", sp3, sSp3));

        // 7.4 Emerald Tech 2-Line
        SpannableString sp4 = new SpannableString("INNOVATION IN TECH\nTHE NEXT DECADE");
        sp4.setSpan(new ForegroundColorSpan(Color.parseColor("#10B981")), 0, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp4.setSpan(new ForegroundColorSpan(Color.WHITE), 11, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sSp4 = new CaptionStyle();
        sSp4.presetId = "sp_emerald_tech";
        sSp4.presetName = "Emerald Tech 2-Line";
        sSp4.fontFamily = "sans-serif-black";
        sSp4.highlightColor = Color.parseColor("#10B981");
        sSp4.textColor = Color.WHITE;
        sSp4.hasOutline = true;
        sSp4.strokeColor = Color.BLACK;
        sSp4.wordsPerChunk = 3;
        allItems.add(new TemplateItem("sp_emerald_tech", "Split view", "EMERALD TECH 2-LINE", sp4, sSp4));
    }

    public void filterByCategory(String category) {
        this.currentCategory = category;
        displayedItems.clear();
        for (TemplateItem item : allItems) {
            if (item.category.equalsIgnoreCase(category)) {
                displayedItems.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_template_card, parent, false);
        return new TemplateViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        TemplateItem item = displayedItems.get(position);
        holder.tvName.setText(item.name);
        holder.tvPreview.setText(item.sampleSpannable);

        boolean isSelected = item.id.equals(selectedId);
        if (isSelected) {
            holder.card.setCardBackgroundColor(Color.parseColor("#1E293B"));
            holder.tvName.setTextColor(Color.parseColor("#38BDF8"));
        } else {
            holder.card.setCardBackgroundColor(Color.parseColor("#18202F"));
            holder.tvName.setTextColor(Color.parseColor("#64748B"));
        }

        holder.card.setOnClickListener(v -> {
            selectedId = item.id;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onTemplateSelected(item.style);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayedItems.size();
    }

    public static class TemplateViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView tvName;
        TextView tvPreview;

        public TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardTemplate);
            tvName = itemView.findViewById(R.id.tvTemplateName);
            tvPreview = itemView.findViewById(R.id.tvTemplatePreview);
        }
    }
}
