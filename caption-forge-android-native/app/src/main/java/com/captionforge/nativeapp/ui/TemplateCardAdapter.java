package com.captionforge.nativeapp.ui;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        public String name;
        public CharSequence sampleSpannable;
        public CaptionStyle style;

        public TemplateItem(String id, String name, CharSequence sample, CaptionStyle style) {
            this.id = id;
            this.name = name;
            this.sampleSpannable = sample;
            this.style = style;
        }
    }

    private final List<TemplateItem> items = new ArrayList<>();
    private String selectedId = "elevate";
    private final OnTemplateSelectedListener listener;

    public TemplateCardAdapter(OnTemplateSelectedListener listener) {
        this.listener = listener;
        initItems();
    }

    private void initItems() {
        // 1. Basic Subtitles
        items.add(new TemplateItem("basic_subtitles", "Basic Subtitles", "Basic subtitles", CaptionStyle.createBasicSubtitlesPreset()));

        // 2. Elevate
        SpannableString elevate = new SpannableString("ELEVATE THE WORDS");
        elevate.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        elevate.setSpan(new ForegroundColorSpan(Color.WHITE), 8, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        items.add(new TemplateItem("elevate", "Elevate", elevate, CaptionStyle.createElevatePreset()));

        // 3. One Word
        SpannableString oneWord = new SpannableString("GO");
        oneWord.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        items.add(new TemplateItem("one_word", "One Word", oneWord, CaptionStyle.createOneWordPreset()));

        // 4. Two Word
        SpannableString twoWord = new SpannableString("show two");
        twoWord.setSpan(new ForegroundColorSpan(Color.parseColor("#22C55E")), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        items.add(new TemplateItem("two_word", "Two Word", twoWord, CaptionStyle.createTwoWordPreset()));

        // 5. Word Color Change
        SpannableString wordColor = new SpannableString("Highlight the words");
        wordColor.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordColor.setSpan(new ForegroundColorSpan(Color.WHITE), 10, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        items.add(new TemplateItem("word_color_change", "Word Color Change", wordColor, CaptionStyle.createWordColorChangePreset()));

        // 6. Word Background Change
        SpannableString wordBg = new SpannableString("Highlight the background of");
        wordBg.setSpan(new BackgroundColorSpan(Color.parseColor("#38BDF8")), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordBg.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        items.add(new TemplateItem("word_bg_change", "Word Background Change", wordBg, CaptionStyle.createWordBackgroundChangePreset()));

        // 7. CapCut Cyber Glow
        SpannableString cyber = new SpannableString("CYBER GLOW POP");
        cyber.setSpan(new ForegroundColorSpan(Color.parseColor("#F43F5E")), 0, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cyber.setSpan(new ForegroundColorSpan(Color.parseColor("#06B6D4")), 11, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        items.add(new TemplateItem("capcut_cyber_glow", "CapCut Glow", cyber, CaptionStyle.createCapCutCyberGlowPreset()));

        // 8. MrBeast Impact
        SpannableString beast = new SpannableString("MR BEAST 100K");
        beast.setSpan(new ForegroundColorSpan(Color.parseColor("#22C55E")), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        beast.setSpan(new ForegroundColorSpan(Color.parseColor("#FACC15")), 9, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        items.add(new TemplateItem("mr_beast", "MrBeast Impact", beast, CaptionStyle.createMrBeastPreset()));

        // 9. Ali Abdaal Clean
        SpannableString abdaal = new SpannableString("Aesthetic minimal");
        abdaal.setSpan(new ForegroundColorSpan(Color.parseColor("#FEF3C7")), 0, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        items.add(new TemplateItem("ali_abdaal", "Ali Abdaal Clean", abdaal, CaptionStyle.createAliAbdaalPreset()));

        // 10. Red Punch
        SpannableString redPunch = new SpannableString("RED PUNCH BOX");
        redPunch.setSpan(new ForegroundColorSpan(Color.parseColor("#EF4444")), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        redPunch.setSpan(new ForegroundColorSpan(Color.WHITE), 10, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        items.add(new TemplateItem("red_punch", "Red Punch", redPunch, CaptionStyle.createRedPunchPreset()));

        // 11. Typewriter Neon
        SpannableString typew = new SpannableString("Typewriter Neon");
        typew.setSpan(new ForegroundColorSpan(Color.parseColor("#10B981")), 0, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        items.add(new TemplateItem("typewriter_neon", "Typewriter Neon", typew, CaptionStyle.createTypewriterNeonPreset()));

        // 12. Golden Luxury
        SpannableString gold = new SpannableString("GOLDEN LUXURY");
        gold.setSpan(new ForegroundColorSpan(Color.parseColor("#F59E0B")), 0, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        items.add(new TemplateItem("golden_luxury", "Golden Luxury", gold, CaptionStyle.createGoldenLuxuryPreset()));
    }

    public void setSelectedId(String id) {
        this.selectedId = id;
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
        TemplateItem item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvPreview.setText(item.sampleSpannable);

        boolean isSelected = item.id.equalsIgnoreCase(selectedId);
        holder.ivCheckmark.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.card.setCardBackgroundColor(isSelected ? Color.parseColor("#1E232A") : Color.parseColor("#2A2C32"));

        holder.itemView.setOnClickListener(v -> {
            selectedId = item.id;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onTemplateSelected(item.style);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TemplateViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        ImageView ivCheckmark;
        TextView tvPreview;
        TextView tvName;

        TemplateViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardTemplate);
            ivCheckmark = itemView.findViewById(R.id.ivCheckmark);
            tvPreview = itemView.findViewById(R.id.tvTemplatePreview);
            tvName = itemView.findViewById(R.id.tvTemplateName);
        }
    }
}
