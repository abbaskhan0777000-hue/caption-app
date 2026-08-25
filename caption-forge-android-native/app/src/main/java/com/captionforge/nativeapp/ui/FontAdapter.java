package com.captionforge.nativeapp.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captionforge.nativeapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.ViewHolder> {
    private final List<String> fonts = Arrays.asList(
            "sans-serif-black",
            "sans-serif-bold",
            "sans-serif-condensed",
            "sans-serif-medium",
            "monospace",
            "serif",
            "casual",
            "cursive"
    );
    private final List<String> fontDisplayNames = Arrays.asList(
            "Impact / Anton",
            "Montserrat Bold",
            "Bebas Neue",
            "Poppins / Inter",
            "Cyberpunk Mono",
            "Cinzel Serif",
            "Fredoka Bubble",
            "Righteous Pop"
    );

    private final OnFontSelectedListener listener;
    private int selectedIndex = 0;

    public interface OnFontSelectedListener {
        void onFontSelected(String fontFamily);
    }

    public FontAdapter(OnFontSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String displayName = fontDisplayNames.get(position);
        String fontFamily = fonts.get(position);

        holder.btnFontChip.setText(displayName);
        holder.btnFontChip.setTypeface(Typeface.create(fontFamily, Typeface.BOLD));

        boolean isSelected = position == selectedIndex;
        if (isSelected) {
            holder.btnFontChip.setTextColor(Color.parseColor("#FBBF24"));
            holder.btnFontChip.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FBBF24")));
        } else {
            holder.btnFontChip.setTextColor(Color.parseColor("#CBD5E1"));
            holder.btnFontChip.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#334155")));
        }

        holder.btnFontChip.setOnClickListener(v -> {
            int prev = selectedIndex;
            selectedIndex = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedIndex);
            listener.onFontSelected(fontFamily);
        });
    }

    @Override
    public int getItemCount() {
        return fonts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialButton btnFontChip;

        ViewHolder(View itemView) {
            super(itemView);
            btnFontChip = itemView.findViewById(R.id.btnFontChip);
        }
    }
}
