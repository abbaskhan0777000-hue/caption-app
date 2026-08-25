package com.captionforge.nativeapp.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.model.WordCaption;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TranscriptAdapter extends RecyclerView.Adapter<TranscriptAdapter.ViewHolder> {
    private List<WordCaption> words = new ArrayList<>();
    private final OnWordActionListener listener;

    public interface OnWordActionListener {
        void onSeekToWord(double seconds);
        void onWordEdited(int index, String newText);
    }

    public TranscriptAdapter(OnWordActionListener listener) {
        this.listener = listener;
    }

    public void setWords(List<WordCaption> words) {
        this.words = words != null ? words : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transcript_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WordCaption word = words.get(position);

        int min = (int) (word.getStart() / 60);
        double sec = word.getStart() % 60;
        holder.tvRowTime.setText(String.format(Locale.US, "%02d:%04.1f", min, sec));

        holder.etRowWord.setText(word.getWord());

        holder.btnRowSeek.setOnClickListener(v -> {
            listener.onSeekToWord(word.getStart());
        });

        holder.etRowWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getAdapterPosition();
                if (pos >= 0 && pos < words.size()) {
                    words.get(pos).setWord(s.toString());
                    listener.onWordEdited(pos, s.toString());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRowTime;
        EditText etRowWord;
        ImageButton btnRowSeek;

        ViewHolder(View itemView) {
            super(itemView);
            tvRowTime = itemView.findViewById(R.id.tvRowTime);
            etRowWord = itemView.findViewById(R.id.etRowWord);
            btnRowSeek = itemView.findViewById(R.id.btnRowSeek);
        }
    }
}
