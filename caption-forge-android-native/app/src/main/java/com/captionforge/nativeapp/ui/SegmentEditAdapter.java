package com.captionforge.nativeapp.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.engine.AssGenerator;
import com.captionforge.nativeapp.model.WordCaption;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SegmentEditAdapter extends RecyclerView.Adapter<SegmentEditAdapter.SegmentViewHolder> {

    public static class SegmentModel {
        public double start;
        public double end;
        public String text;

        public SegmentModel(double start, double end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }
    }

    private final List<SegmentModel> segments = new ArrayList<>();

    public SegmentEditAdapter(List<AssGenerator.CaptionChunk> chunks) {
        if (chunks != null) {
            for (AssGenerator.CaptionChunk c : chunks) {
                StringBuilder sb = new StringBuilder();
                for (WordCaption w : c.words) {
                    sb.append(w.getWord()).append(" ");
                }
                segments.add(new SegmentModel(c.start, c.end, sb.toString().trim()));
            }
        }
    }

    public List<WordCaption> buildWordList() {
        List<WordCaption> result = new ArrayList<>();
        for (SegmentModel s : segments) {
            String[] rawWords = s.text.trim().split("\\s+");
            if (rawWords.length == 0 || s.text.trim().isEmpty()) continue;

            double duration = Math.max(0.1, s.end - s.start);
            double step = duration / rawWords.length;

            for (int i = 0; i < rawWords.length; i++) {
                double wStart = s.start + (i * step);
                double wEnd = wStart + step;
                result.add(new WordCaption(rawWords[i], wStart, wEnd));
            }
        }
        return result;
    }

    @NonNull
    @Override
    public SegmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_caption_segment, parent, false);
        return new SegmentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SegmentViewHolder holder, int position) {
        SegmentModel item = segments.get(position);

        holder.tvTimeRange.setText(formatTimeRange(item.start, item.end));
        holder.etText.setText(item.text);

        holder.etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    segments.get(pos).text = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Delete Segment
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && segments.size() > 1) {
                segments.remove(pos);
                notifyItemRemoved(pos);
            }
        });

        // Add a Line
        holder.btnAddLine.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                double newStart = item.end;
                double newEnd = item.end + 2.0;
                segments.add(pos + 1, new SegmentModel(newStart, newEnd, "New caption"));
                notifyItemInserted(pos + 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return segments.size();
    }

    private String formatTimeRange(double startSec, double endSec) {
        return String.format(Locale.US, "%01d:%02d.%03d - %01d:%02d.%03d",
                (int) (startSec / 60), (int) (startSec % 60), (int) ((startSec * 1000) % 1000),
                (int) (endSec / 60), (int) (endSec % 60), (int) ((endSec * 1000) % 1000)
        );
    }

    static class SegmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimeRange;
        ImageView btnDelete;
        EditText etText;
        View btnAddLine;

        SegmentViewHolder(View itemView) {
            super(itemView);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            btnDelete = itemView.findViewById(R.id.btnDeleteSegment);
            etText = itemView.findViewById(R.id.etSegmentText);
            btnAddLine = itemView.findViewById(R.id.btnAddLineDivider);
        }
    }
}
