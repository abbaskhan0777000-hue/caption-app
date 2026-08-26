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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String selectedId = "legacy_1";
    private final OnTemplateSelectedListener listener;

    public TemplateCardAdapter(OnTemplateSelectedListener listener) {
        this.listener = listener;
        initAllTemplates();
        filterByCategory("Legacy");
    }

    private void initAllTemplates() {
        // ==========================================
        // 1. LEGACY (Screenshot 1 & 4)
        // ==========================================
        // 1.1 THE QUICK (Uppercase bold, yellow active, white other, black outline)
        SpannableString leg1 = new SpannableString("THE QUICK");
        leg1.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg1.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg1.setSpan(new StyleSpan(Typeface.BOLD), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg1 = new CaptionStyle();
        sLeg1.presetId = "legacy_1";
        sLeg1.presetName = "Legacy Uppercase";
        sLeg1.fontFamily = "sans-serif-black";
        sLeg1.textColor = Color.WHITE;
        sLeg1.highlightColor = Color.parseColor("#FACC15");
        sLeg1.hasOutline = true;
        sLeg1.strokeColor = Color.BLACK;
        sLeg1.strokeWidth = 10f;
        sLeg1.isBold = true;
        sLeg1.wordsPerChunk = 2;
        allItems.add(new TemplateItem("legacy_1", "Legacy", "Legacy Uppercase", leg1, sLeg1));

        // 1.2 The quick (Title case, yellow active, white other, black outline)
        SpannableString leg2 = new SpannableString("The quick");
        leg2.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg2.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg2.setSpan(new StyleSpan(Typeface.BOLD), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg2 = new CaptionStyle();
        sLeg2.presetId = "legacy_2";
        sLeg2.presetName = "Legacy Title";
        sLeg2.fontFamily = "sans-serif-black";
        sLeg2.textColor = Color.WHITE;
        sLeg2.highlightColor = Color.parseColor("#FACC15");
        sLeg2.hasOutline = true;
        sLeg2.strokeColor = Color.BLACK;
        sLeg2.strokeWidth = 8f;
        sLeg2.wordsPerChunk = 2;
        allItems.add(new TemplateItem("legacy_2", "Legacy", "Legacy Title", leg2, sLeg2));

        // 1.3 The quick brown fox (White background, black active, dark blue text)
        SpannableString leg3 = new SpannableString("The quick brown fox");
        leg3.setSpan(new BackgroundColorSpan(Color.WHITE), 0, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg3.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg3.setSpan(new ForegroundColorSpan(Color.parseColor("#1E40AF")), 4, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg3 = new CaptionStyle();
        sLeg3.presetId = "legacy_3";
        sLeg3.presetName = "White Pill Navy";
        sLeg3.fontFamily = "sans-serif-black";
        sLeg3.backgroundColor = Color.WHITE;
        sLeg3.textColor = Color.parseColor("#1E40AF");
        sLeg3.highlightColor = Color.BLACK;
        sLeg3.wordsPerChunk = 4;
        allItems.add(new TemplateItem("legacy_3", "Legacy", "White Pill Navy", leg3, sLeg3));

        // 1.4 The quick brown (White background, yellow active, royal blue text)
        SpannableString leg4 = new SpannableString("The quick brown");
        leg4.setSpan(new BackgroundColorSpan(Color.WHITE), 0, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg4.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg4.setSpan(new ForegroundColorSpan(Color.parseColor("#2563EB")), 4, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg4 = new CaptionStyle();
        sLeg4.presetId = "legacy_4";
        sLeg4.presetName = "White Pill Yellow Blue";
        sLeg4.backgroundColor = Color.WHITE;
        sLeg4.textColor = Color.parseColor("#2563EB");
        sLeg4.highlightColor = Color.parseColor("#FACC15");
        sLeg4.wordsPerChunk = 3;
        allItems.add(new TemplateItem("legacy_4", "Legacy", "White Pill Yellow Blue", leg4, sLeg4));

        // 1.5 The quick (Black background, yellow active, white text)
        SpannableString leg5 = new SpannableString("The quick");
        leg5.setSpan(new BackgroundColorSpan(Color.BLACK), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg5.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg5.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg5 = new CaptionStyle();
        sLeg5.presetId = "legacy_5";
        sLeg5.presetName = "Black Pill Yellow";
        sLeg5.backgroundColor = Color.BLACK;
        sLeg5.textColor = Color.WHITE;
        sLeg5.highlightColor = Color.parseColor("#FACC15");
        sLeg5.wordsPerChunk = 2;
        allItems.add(new TemplateItem("legacy_5", "Legacy", "Black Pill Yellow", leg5, sLeg5));

        // 1.6 The quick (Active word black background box)
        SpannableString leg6 = new SpannableString("The quick");
        leg6.setSpan(new BackgroundColorSpan(Color.BLACK), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg6.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg6.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg6 = new CaptionStyle();
        sLeg6.presetId = "legacy_6";
        sLeg6.presetName = "Active Word Box";
        sLeg6.highlightBgColor = Color.BLACK;
        sLeg6.highlightColor = Color.parseColor("#FACC15");
        sLeg6.textColor = Color.WHITE;
        sLeg6.wordsPerChunk = 2;
        allItems.add(new TemplateItem("legacy_6", "Legacy", "Active Word Box", leg6, sLeg6));

        // 1.7 THE (Single word green bold)
        SpannableString leg7 = new SpannableString("THE");
        leg7.setSpan(new ForegroundColorSpan(Color.parseColor("#22C55E")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        leg7.setSpan(new StyleSpan(Typeface.BOLD), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sLeg7 = new CaptionStyle();
        sLeg7.presetId = "legacy_7";
        sLeg7.presetName = "Green Single Word";
        sLeg7.fontFamily = "sans-serif-black";
        sLeg7.textColor = Color.parseColor("#22C55E");
        sLeg7.highlightColor = Color.parseColor("#22C55E");
        sLeg7.wordsPerChunk = 1;
        allItems.add(new TemplateItem("legacy_7", "Legacy", "Green Single Word", leg7, sLeg7));

        // ==========================================
        // 2. MODERN (Clean, Sans-Serif, Glass)
        // ==========================================
        SpannableString mod1 = new SpannableString("The quick brown");
        mod1.setSpan(new ForegroundColorSpan(Color.parseColor("#38BDF8")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod1.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMod1 = new CaptionStyle();
        sMod1.presetId = "modern_1";
        sMod1.presetName = "Modern Cyan Clean";
        sMod1.fontFamily = "sans-serif-medium";
        sMod1.textColor = Color.WHITE;
        sMod1.highlightColor = Color.parseColor("#38BDF8");
        sMod1.hasShadow = true;
        sMod1.hasOutline = false;
        sMod1.wordsPerChunk = 3;
        allItems.add(new TemplateItem("modern_1", "Modern", "Modern Cyan Clean", mod1, sMod1));

        SpannableString mod2 = new SpannableString("THE QUICK");
        mod2.setSpan(new ForegroundColorSpan(Color.parseColor("#8B5CF6")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod2.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMod2 = new CaptionStyle();
        sMod2.presetId = "modern_2";
        sMod2.presetName = "Modern Purple Pop";
        sMod2.fontFamily = "sans-serif-black";
        sMod2.textColor = Color.WHITE;
        sMod2.highlightColor = Color.parseColor("#8B5CF6");
        sMod2.wordsPerChunk = 2;
        allItems.add(new TemplateItem("modern_2", "Modern", "Modern Purple Pop", mod2, sMod2));

        // ==========================================
        // 3. VIRAL (Screenshot 2)
        // ==========================================
        // 3.1 The quick brown fox (Purple pill, yellow active)
        SpannableString vir1 = new SpannableString("The quick brown fox");
        vir1.setSpan(new BackgroundColorSpan(Color.parseColor("#8B5CF6")), 0, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        vir1.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        vir1.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sVir1 = new CaptionStyle();
        sVir1.presetId = "viral_1";
        sVir1.presetName = "Purple Pill Viral";
        sVir1.backgroundColor = Color.parseColor("#8B5CF6");
        sVir1.highlightColor = Color.parseColor("#FACC15");
        sVir1.textColor = Color.WHITE;
        sVir1.wordsPerChunk = 4;
        allItems.add(new TemplateItem("viral_1", "Viral", "Purple Pill Viral", vir1, sVir1));

        // 3.2 THE QUICK (Hormozi style: Yellow active, Red secondary, thick black stroke)
        SpannableString vir2 = new SpannableString("THE QUICK");
        vir2.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        vir2.setSpan(new ForegroundColorSpan(Color.parseColor("#EF4444")), 4, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sVir2 = new CaptionStyle();
        sVir2.presetId = "viral_2";
        sVir2.presetName = "Hormozi Fire";
        sVir2.fontFamily = "sans-serif-black";
        sVir2.highlightColor = Color.parseColor("#FACC15");
        sVir2.textColor = Color.parseColor("#EF4444");
        sVir2.hasOutline = true;
        sVir2.strokeColor = Color.BLACK;
        sVir2.strokeWidth = 12f;
        sVir2.wordsPerChunk = 2;
        allItems.add(new TemplateItem("viral_2", "Viral", "Hormozi Fire", vir2, sVir2));

        // 3.3 The (Orange active in black square)
        SpannableString vir3 = new SpannableString("The");
        vir3.setSpan(new BackgroundColorSpan(Color.BLACK), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        vir3.setSpan(new ForegroundColorSpan(Color.parseColor("#F97316")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sVir3 = new CaptionStyle();
        sVir3.presetId = "viral_3";
        sVir3.presetName = "Orange Box Single";
        sVir3.highlightBgColor = Color.BLACK;
        sVir3.highlightColor = Color.parseColor("#F97316");
        sVir3.wordsPerChunk = 1;
        allItems.add(new TemplateItem("viral_3", "Viral", "Orange Box Single", vir3, sVir3));

        // 3.4 THE QUICK BROWN (Bebas condensed, cyan active)
        SpannableString vir4 = new SpannableString("THE QUICK BROWN");
        vir4.setSpan(new ForegroundColorSpan(Color.parseColor("#38BDF8")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        vir4.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sVir4 = new CaptionStyle();
        sVir4.presetId = "viral_4";
        sVir4.presetName = "Cyan Condensed";
        sVir4.fontFamily = "sans-serif-condensed-light";
        sVir4.highlightColor = Color.parseColor("#38BDF8");
        sVir4.textColor = Color.WHITE;
        sVir4.hasOutline = true;
        sVir4.strokeColor = Color.parseColor("#1E3A8A");
        sVir4.wordsPerChunk = 3;
        allItems.add(new TemplateItem("viral_4", "Viral", "Cyan Condensed", vir4, sVir4));

        // 3.5 THE (Beast Mode Green)
        SpannableString vir5 = new SpannableString("THE");
        vir5.setSpan(new ForegroundColorSpan(Color.parseColor("#22C55E")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sVir5 = new CaptionStyle();
        sVir5.presetId = "viral_5";
        sVir5.presetName = "Beast Mode";
        sVir5.fontFamily = "sans-serif-black";
        sVir5.highlightColor = Color.parseColor("#22C55E");
        sVir5.textColor = Color.parseColor("#22C55E");
        sVir5.hasOutline = true;
        sVir5.strokeColor = Color.BLACK;
        sVir5.strokeWidth = 14f;
        sVir5.wordsPerChunk = 1;
        allItems.add(new TemplateItem("viral_5", "Viral", "Beast Mode", vir5, sVir5));

        // ==========================================
        // 4. BOLD (Heavy Typography)
        // ==========================================
        SpannableString bld1 = new SpannableString("THE QUICK");
        bld1.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bld1.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sBld1 = new CaptionStyle();
        sBld1.presetId = "bold_1";
        sBld1.presetName = "Impact Yellow Bold";
        sBld1.fontFamily = "sans-serif-black";
        sBld1.highlightColor = Color.parseColor("#FACC15");
        sBld1.textColor = Color.WHITE;
        sBld1.hasOutline = true;
        sBld1.strokeColor = Color.BLACK;
        sBld1.wordsPerChunk = 2;
        allItems.add(new TemplateItem("bold_1", "Bold", "Impact Yellow Bold", bld1, sBld1));

        SpannableString bld2 = new SpannableString("THE QUICK BROWN");
        bld2.setSpan(new ForegroundColorSpan(Color.parseColor("#EF4444")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        bld2.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sBld2 = new CaptionStyle();
        sBld2.presetId = "bold_2";
        sBld2.presetName = "Red Strike Bold";
        sBld2.fontFamily = "sans-serif-black";
        sBld2.highlightColor = Color.parseColor("#EF4444");
        sBld2.textColor = Color.WHITE;
        sBld2.hasOutline = true;
        sBld2.strokeColor = Color.BLACK;
        sBld2.wordsPerChunk = 3;
        allItems.add(new TemplateItem("bold_2", "Bold", "Red Strike Bold", bld2, sBld2));

        // ==========================================
        // 5. MINIMAL (Screenshot 3)
        // ==========================================
        SpannableString min1 = new SpannableString("The quick");
        min1.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min1.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMin1 = new CaptionStyle();
        sMin1.presetId = "minimal_1";
        sMin1.presetName = "Minimal Light";
        sMin1.fontFamily = "sans-serif";
        sMin1.highlightColor = Color.parseColor("#FACC15");
        sMin1.textColor = Color.WHITE;
        sMin1.hasOutline = false;
        sMin1.wordsPerChunk = 2;
        allItems.add(new TemplateItem("minimal_1", "Minimal", "Minimal Light", min1, sMin1));

        SpannableString min2 = new SpannableString("THE QUICK BROWN");
        min2.setSpan(new BackgroundColorSpan(Color.parseColor("#C084FC")), 0, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min2.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMin2 = new CaptionStyle();
        sMin2.presetId = "minimal_2";
        sMin2.presetName = "Lavender Minimal";
        sMin2.backgroundColor = Color.parseColor("#C084FC");
        sMin2.highlightColor = Color.BLACK;
        sMin2.textColor = Color.WHITE;
        sMin2.wordsPerChunk = 3;
        allItems.add(new TemplateItem("minimal_2", "Minimal", "Lavender Minimal", min2, sMin2));

        SpannableString min3 = new SpannableString("The quick brown");
        min3.setSpan(new ForegroundColorSpan(Color.parseColor("#38BDF8")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min3.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMin3 = new CaptionStyle();
        sMin3.presetId = "minimal_3";
        sMin3.presetName = "Soft Cyan Minimal";
        sMin3.highlightColor = Color.parseColor("#38BDF8");
        sMin3.textColor = Color.WHITE;
        sMin3.hasOutline = false;
        sMin3.wordsPerChunk = 3;
        allItems.add(new TemplateItem("minimal_3", "Minimal", "Soft Cyan Minimal", min3, sMin3));

        SpannableString min4 = new SpannableString("The quick brown fox jumps");
        min4.setSpan(new BackgroundColorSpan(Color.BLACK), 0, 25, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min4.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        min4.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 25, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMin4 = new CaptionStyle();
        sMin4.presetId = "minimal_4";
        sMin4.presetName = "Black Pill Full";
        sMin4.backgroundColor = Color.BLACK;
        sMin4.highlightColor = Color.parseColor("#FACC15");
        sMin4.textColor = Color.WHITE;
        sMin4.wordsPerChunk = 5;
        allItems.add(new TemplateItem("minimal_4", "Minimal", "Black Pill Full", min4, sMin4));

        SpannableString min5 = new SpannableString("The");
        min5.setSpan(new ForegroundColorSpan(Color.parseColor("#38BDF8")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sMin5 = new CaptionStyle();
        sMin5.presetId = "minimal_5";
        sMin5.presetName = "Minimal Single";
        sMin5.highlightColor = Color.parseColor("#38BDF8");
        sMin5.textColor = Color.parseColor("#38BDF8");
        sMin5.wordsPerChunk = 1;
        allItems.add(new TemplateItem("minimal_5", "Minimal", "Minimal Single", min5, sMin5));

        // ==========================================
        // 6. COOL (Screenshot 5)
        // ==========================================
        SpannableString cl1 = new SpannableString("The quick");
        cl1.setSpan(new ForegroundColorSpan(Color.parseColor("#38BDF8")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl1.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 4, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sCl1 = new CaptionStyle();
        sCl1.presetId = "cool_1";
        sCl1.presetName = "Cyan Yellow Contrast";
        sCl1.fontFamily = "sans-serif-black";
        sCl1.highlightColor = Color.parseColor("#38BDF8");
        sCl1.textColor = Color.parseColor("#FACC15");
        sCl1.hasOutline = true;
        sCl1.strokeColor = Color.BLACK;
        sCl1.wordsPerChunk = 2;
        allItems.add(new TemplateItem("cool_1", "Cool", "Cyan Yellow Contrast", cl1, sCl1));

        SpannableString cl2 = new SpannableString("THE");
        cl2.setSpan(new ForegroundColorSpan(Color.parseColor("#F97316")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sCl2 = new CaptionStyle();
        sCl2.presetId = "cool_2";
        sCl2.presetName = "Neon Orange Glow";
        sCl2.fontFamily = "sans-serif-black";
        sCl2.highlightColor = Color.parseColor("#F97316");
        sCl2.textColor = Color.parseColor("#F97316");
        sCl2.hasShadow = true;
        sCl2.shadowColor = Color.parseColor("#F97316");
        sCl2.wordsPerChunk = 1;
        allItems.add(new TemplateItem("cool_2", "Cool", "Neon Orange Glow", cl2, sCl2));

        SpannableString cl3 = new SpannableString("The quick");
        cl3.setSpan(new BackgroundColorSpan(Color.parseColor("#EC4899")), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl3.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl3.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sCl3 = new CaptionStyle();
        sCl3.presetId = "cool_3";
        sCl3.presetName = "Pink Pill Cool";
        sCl3.backgroundColor = Color.parseColor("#EC4899");
        sCl3.highlightColor = Color.BLACK;
        sCl3.textColor = Color.WHITE;
        sCl3.wordsPerChunk = 2;
        allItems.add(new TemplateItem("cool_3", "Cool", "Pink Pill Cool", cl3, sCl3));

        SpannableString cl4 = new SpannableString("The quick brown fox jumps over");
        cl4.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl4.setSpan(new ForegroundColorSpan(Color.parseColor("#F97316")), 4, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl4.setSpan(new StyleSpan(Typeface.ITALIC), 0, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sCl4 = new CaptionStyle();
        sCl4.presetId = "cool_4";
        sCl4.presetName = "Italic Sunset";
        sCl4.isItalic = true;
        sCl4.highlightColor = Color.parseColor("#FACC15");
        sCl4.textColor = Color.parseColor("#F97316");
        sCl4.wordsPerChunk = 6;
        allItems.add(new TemplateItem("cool_4", "Cool", "Italic Sunset", cl4, sCl4));

        SpannableString cl5 = new SpannableString("The quick brown fox jumps");
        cl5.setSpan(new BackgroundColorSpan(Color.parseColor("#2563EB")), 0, 25, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl5.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cl5.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 25, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sCl5 = new CaptionStyle();
        sCl5.presetId = "cool_5";
        sCl5.presetName = "Royal Blue Box";
        sCl5.backgroundColor = Color.parseColor("#2563EB");
        sCl5.highlightColor = Color.parseColor("#FACC15");
        sCl5.textColor = Color.WHITE;
        sCl5.wordsPerChunk = 5;
        allItems.add(new TemplateItem("cool_5", "Cool", "Royal Blue Box", cl5, sCl5));

        // ==========================================
        // 7. SPLIT VIEW (Multi-line)
        // ==========================================
        SpannableString sp1 = new SpannableString("The quick\nbrown fox");
        sp1.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp1.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sSp1 = new CaptionStyle();
        sSp1.presetId = "split_1";
        sSp1.presetName = "2-Line Split Yellow";
        sSp1.highlightColor = Color.parseColor("#FACC15");
        sSp1.textColor = Color.WHITE;
        sSp1.wordsPerChunk = 4;
        allItems.add(new TemplateItem("split_1", "Split view", "2-Line Split Yellow", sp1, sSp1));

        SpannableString sp2 = new SpannableString("THE QUICK\nBROWN FOX");
        sp2.setSpan(new ForegroundColorSpan(Color.parseColor("#38BDF8")), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp2.setSpan(new ForegroundColorSpan(Color.WHITE), 4, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CaptionStyle sSp2 = new CaptionStyle();
        sSp2.presetId = "split_2";
        sSp2.presetName = "2-Line Bold Cyan";
        sSp2.fontFamily = "sans-serif-black";
        sSp2.highlightColor = Color.parseColor("#38BDF8");
        sSp2.textColor = Color.WHITE;
        sSp2.hasOutline = true;
        sSp2.strokeColor = Color.BLACK;
        sSp2.wordsPerChunk = 4;
        allItems.add(new TemplateItem("split_2", "Split view", "2-Line Bold Cyan", sp2, sSp2));
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
        holder.tvPreview.setText(item.sampleSpannable);

        boolean isSelected = item.id.equals(selectedId);
        if (isSelected) {
            holder.card.setCardBackgroundColor(Color.parseColor("#1E232A"));
            holder.card.setForeground(holder.itemView.getContext().getDrawable(R.drawable.bg_dark_pill));
        } else {
            holder.card.setCardBackgroundColor(Color.parseColor("#2A2C32"));
            holder.card.setForeground(null);
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
        TextView tvPreview;

        public TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardTemplate);
            tvPreview = itemView.findViewById(R.id.tvTemplatePreview);
        }
    }
}
