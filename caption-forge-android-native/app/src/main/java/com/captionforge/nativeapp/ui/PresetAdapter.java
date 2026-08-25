package com.captionforge.nativeapp.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.model.CaptionStyle;

import java.util.ArrayList;
import java.util.List;

public class PresetAdapter extends RecyclerView.Adapter<PresetAdapter.ViewHolder> {
    private final List<CaptionStyle> presets = new ArrayList<>();
    private final OnPresetSelectedListener listener;
    private int selectedIndex = 0;

    public interface OnPresetSelectedListener {
        void onPresetSelected(CaptionStyle style);
    }

    public PresetAdapter(OnPresetSelectedListener listener) {
        this.listener = listener;
        presets.add(CaptionStyle.createHormoziPreset());
        presets.add(CaptionStyle.createCapCutKaraokePreset());
        presets.add(CaptionStyle.createCyberpunkNeonPreset());
        presets.add(CaptionStyle.createBeastModePreset());
        presets.add(CaptionStyle.createCleanFadePreset());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preset_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CaptionStyle preset = presets.get(position);
        holder.tvPresetName.setText(preset.presetName);
        holder.tvPresetPreview.setTextColor(preset.highlightColor);

        boolean isSelected = position == selectedIndex;
        holder.itemView.setBackgroundColor(isSelected ? Color.parseColor("#334155") : Color.TRANSPARENT);

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedIndex;
            selectedIndex = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedIndex);
            listener.onPresetSelected(preset);
        });
    }

    @Override
    public int getItemCount() {
        return presets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPresetBadge;
        TextView tvPresetName;
        TextView tvPresetPreview;

        ViewHolder(View itemView) {
            super(itemView);
            tvPresetBadge = itemView.findViewById(R.id.tvPresetBadge);
            tvPresetName = itemView.findViewById(R.id.tvPresetName);
            tvPresetPreview = itemView.findViewById(R.id.tvPresetPreview);
        }
    }
}
