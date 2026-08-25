package com.captionforge.nativeapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.model.CaptionStyle;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TemplatesBottomSheet extends BottomSheetDialogFragment {

    public interface OnTemplateApplyListener {
        void onApply(CaptionStyle style);
        void onOpenStyles();
    }

    private CaptionStyle currentStyle;
    private CaptionStyle selectedStyle;
    private OnTemplateApplyListener applyListener;

    public static TemplatesBottomSheet newInstance(CaptionStyle style, OnTemplateApplyListener listener) {
        TemplatesBottomSheet sheet = new TemplatesBottomSheet();
        sheet.currentStyle = style;
        sheet.selectedStyle = style;
        sheet.applyListener = listener;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_templates, container, false);

        RecyclerView rvGrid = view.findViewById(R.id.rvTemplatesGrid);
        rvGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));

        TemplateCardAdapter adapter = new TemplateCardAdapter(style -> {
            this.selectedStyle = style;
        });

        if (currentStyle != null && currentStyle.presetId != null) {
            adapter.setSelectedId(currentStyle.presetId);
        }
        rvGrid.setAdapter(adapter);

        // Switch to Effects/Styles tab
        view.findViewById(R.id.tabEffectsHeader).setOnClickListener(v -> {
            dismiss();
            if (applyListener != null) {
                applyListener.onOpenStyles();
            }
        });

        // Cancel
        view.findViewById(R.id.btnCancelTemplate).setOnClickListener(v -> dismiss());

        // Apply
        view.findViewById(R.id.btnApplyTemplate).setOnClickListener(v -> {
            if (applyListener != null && selectedStyle != null) {
                applyListener.onApply(selectedStyle);
            }
            dismiss();
        });

        return view;
    }
}
