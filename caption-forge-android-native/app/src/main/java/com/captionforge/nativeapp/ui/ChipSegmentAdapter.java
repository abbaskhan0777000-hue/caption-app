package com.captionforge.nativeapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.engine.AssGenerator;
import com.captionforge.nativeapp.model.WordCaption;

import java.util.ArrayList;
import java.util.List;

public class ChipSegmentAdapter extends RecyclerView.Adapter<ChipSegmentAdapter.ChipViewHolder> {

    public interface OnChipClickListener {
        void onChipClick(AssGenerator.CaptionChunk chunk);
    }

    private List<AssGenerator.CaptionChunk> chunks = new ArrayList<>();
    private final OnChipClickListener listener;

    public ChipSegmentAdapter(OnChipClickListener listener) {
        this.listener = listener;
    }

    public void setWords(List<WordCaption> words, int wordsPerChunk) {
        this.chunks = AssGenerator.chunkWords(words, wordsPerChunk);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chip_segment, parent, false);
        return new ChipViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        AssGenerator.CaptionChunk chunk = chunks.get(position);
        StringBuilder sb = new StringBuilder();
        for (WordCaption w : chunk.words) {
            sb.append(w.getWord()).append(" ");
        }
        holder.tvText.setText(sb.toString().trim());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChipClick(chunk);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chunks.size();
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;

        ChipViewHolder(View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvChipText);
        }
    }
}
