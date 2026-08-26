package com.captionforge.nativeapp.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.model.CaptionStyle;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class TemplatesBottomSheet extends BottomSheetDialogFragment {

    public interface OnTemplateApplyListener {
        void onApply(CaptionStyle style);
    }

    private CaptionStyle selectedStyle;
    private OnTemplateApplyListener applyListener;

    private TextView chipLegacy, chipModern, chipViral, chipBold, chipMinimal, chipCool, chipSplitView;
    private final List<TextView> allChips = new ArrayList<>();
    private TemplateCardAdapter adapter;

    public static TemplatesBottomSheet newInstance(OnTemplateApplyListener listener) {
        TemplatesBottomSheet sheet = new TemplatesBottomSheet();
        sheet.applyListener = listener;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_templates, container, false);

        initViews(view);
        setupRecyclerView(view);
        setupCategoryChips();

        view.findViewById(R.id.btnCancelTemplates).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnApplyTemplates).setOnClickListener(v -> {
            if (applyListener != null && selectedStyle != null) {
                applyListener.onApply(selectedStyle);
            }
            dismiss();
        });

        return view;
    }

    private void initViews(View view) {
        chipLegacy = view.findViewById(R.id.chipCatLegacy);
        chipModern = view.findViewById(R.id.chipCatModern);
        chipViral = view.findViewById(R.id.chipCatViral);
        chipBold = view.findViewById(R.id.chipCatBold);
        chipMinimal = view.findViewById(R.id.chipCatMinimal);
        chipCool = view.findViewById(R.id.chipCatCool);
        chipSplitView = view.findViewById(R.id.chipCatSplitView);

        allChips.clear();
        allChips.add(chipLegacy);
        allChips.add(chipModern);
        allChips.add(chipViral);
        allChips.add(chipBold);
        allChips.add(chipMinimal);
        allChips.add(chipCool);
        allChips.add(chipSplitView);
    }

    private void setupRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rvTemplatesGrid);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TemplateCardAdapter(style -> {
            this.selectedStyle = style;
        });
        rv.setAdapter(adapter);
    }

    private void setupCategoryChips() {
        chipLegacy.setOnClickListener(v -> selectCategory("Legacy", chipLegacy));
        chipModern.setOnClickListener(v -> selectCategory("Modern", chipModern));
        chipViral.setOnClickListener(v -> selectCategory("Viral", chipViral));
        chipBold.setOnClickListener(v -> selectCategory("Bold", chipBold));
        chipMinimal.setOnClickListener(v -> selectCategory("Minimal", chipMinimal));
        chipCool.setOnClickListener(v -> selectCategory("Cool", chipCool));
        chipSplitView.setOnClickListener(v -> selectCategory("Split view", chipSplitView));

        selectCategory("Legacy", chipLegacy);
    }

    private void selectCategory(String category, TextView activeChip) {
        for (TextView chip : allChips) {
            if (chip == activeChip) {
                chip.setBackgroundResource(R.drawable.bg_dark_pill);
                chip.setTextColor(Color.WHITE);
            } else {
                chip.setBackgroundResource(R.drawable.bg_white_button);
                chip.setTextColor(Color.parseColor("#64748B"));
            }
        }

        if (adapter != null) {
            adapter.filterByCategory(category);
        }
    }
}
