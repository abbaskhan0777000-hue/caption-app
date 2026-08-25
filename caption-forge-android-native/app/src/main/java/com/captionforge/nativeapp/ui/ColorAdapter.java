package com.captionforge.nativeapp.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captionforge.nativeapp.R;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {
    private final List<Integer> colors;
    private final OnColorSelectedListener listener;
    private int selectedIndex = 0;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public ColorAdapter(List<Integer> colors, OnColorSelectedListener listener) {
        this.colors = colors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color_circle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int color = colors.get(position);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        bg.setStroke(2, Color.parseColor("#334155"));
        holder.viewColorCircle.setBackground(bg);

        boolean isSelected = position == selectedIndex;
        holder.ivSelectedCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedIndex;
            selectedIndex = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedIndex);
            listener.onColorSelected(color);
        });
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewColorCircle;
        ImageView ivSelectedCheck;

        ViewHolder(View itemView) {
            super(itemView);
            viewColorCircle = itemView.findViewById(R.id.viewColorCircle);
            ivSelectedCheck = itemView.findViewById(R.id.ivSelectedCheck);
        }
    }
}
