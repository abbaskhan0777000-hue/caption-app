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

    @Override
    public int getTheme() {
        return com.google.android.material.R.style.Theme_MaterialComponents_DayNight_BottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.bottom_sheet_templates, container, false);

            initViews(view);
            setupRecyclerView(view);
            setupCategoryChips();

            View btnCancel = view.findViewById(R.id.btnCancelTemplates);
            if (btnCancel != null) btnCancel.setOnClickListener(v -> dismiss());

            View btnApply = view.findViewById(R.id.btnApplyTemplates);
            if (btnApply != null) {
                btnApply.setOnClickListener(v -> {
                    CaptionStyle toApply = (selectedStyle != null) ? selectedStyle : (adapter != null ? adapter.getSelectedStyle() : null);
                    if (applyListener != null && toApply != null) {
                        applyListener.onApply(toApply);
                    }
                    dismiss();
                });
            }

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            return new View(requireContext());
        }
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
        if (chipLegacy != null) allChips.add(chipLegacy);
        if (chipModern != null) allChips.add(chipModern);
        if (chipViral != null) allChips.add(chipViral);
        if (chipBold != null) allChips.add(chipBold);
        if (chipMinimal != null) allChips.add(chipMinimal);
        if (chipCool != null) allChips.add(chipCool);
        if (chipSplitView != null) allChips.add(chipSplitView);
    }

    private void setupRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rvTemplatesGrid);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new TemplateCardAdapter(style -> {
                this.selectedStyle = style;
            });
            this.selectedStyle = adapter.getSelectedStyle();
            rv.setAdapter(adapter);
        }
    }

    private void setupCategoryChips() {
        if (chipLegacy != null) chipLegacy.setOnClickListener(v -> selectCategory("Legacy", chipLegacy));
        if (chipModern != null) chipModern.setOnClickListener(v -> selectCategory("Modern", chipModern));
        if (chipViral != null) chipViral.setOnClickListener(v -> selectCategory("Viral", chipViral));
        if (chipBold != null) chipBold.setOnClickListener(v -> selectCategory("Bold", chipBold));
        if (chipMinimal != null) chipMinimal.setOnClickListener(v -> selectCategory("Minimal", chipMinimal));
        if (chipCool != null) chipCool.setOnClickListener(v -> selectCategory("Cool", chipCool));
        if (chipSplitView != null) chipSplitView.setOnClickListener(v -> selectCategory("Split view", chipSplitView));

        if (chipLegacy != null) selectCategory("Legacy", chipLegacy);
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
