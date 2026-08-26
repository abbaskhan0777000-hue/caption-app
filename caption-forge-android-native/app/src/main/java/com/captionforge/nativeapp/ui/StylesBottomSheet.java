package com.captionforge.nativeapp.ui;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.model.CaptionStyle;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class StylesBottomSheet extends BottomSheetDialogFragment {

    public interface OnStylesApplyListener {
        void onApply(CaptionStyle style);
    }

    private CaptionStyle workingStyle;
    private OnStylesApplyListener applyListener;

    private TextView tvLivePreview;
    private TextView tvSelectedFontName;
    private TextView tvFontSizeValue;
    private TextView tvWordCountValue;

    private LinearLayout btnItalic, btnBold, btnUnderlined;
    private TextView tvItalicLabel, tvBoldLabel, tvUnderlineLabel;

    private LinearLayout btnAlignLeft, btnAlignCenter, btnAlignRight;
    private LinearLayout btnVAlignTop, btnVAlignCenter, btnVAlignBottom;

    private View boxColorText, boxColorActiveWord, boxColorTextBg, boxColorActiveWordBg, barOutlineColor;
    private SwitchCompat switchTextBg, switchActiveWordBg, switchOutline, switchShadow, switchSingleLine;
    private LinearLayout panelStyles, panelEffects;
    private View subIndicatorStyles, subIndicatorEffects;
    private TextView tvTabStylesSubLabel, tvTabEffectsSubLabel;

    private final String[] fontNames = new String[]{
            "Montserrat Black", "Impact", "Bebas Neue", "Poppins", "Inter", "Fredoka One", "Cinzel Bold", "Righteous", "Arial Black"
    };
    private final String[] fontFamilies = new String[]{
            "sans-serif-black", "sans-serif-black", "sans-serif-condensed-light", "sans-serif", "sans-serif-medium", "casual", "serif", "cursive", "sans-serif-black"
    };

    private final String[] colorNameOptions = new String[]{
            "White (#FFFFFF)", "Yellow (#FACC15)", "Cyan (#38BDF8)", "Green (#22C55E)", "Pink (#EC4899)", "Red (#EF4444)", "Black (#000000)", "Purple (#8B5CF6)", "Orange (#F97316)"
    };
    private final int[] colorValueOptions = new int[]{
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#FACC15"),
            Color.parseColor("#38BDF8"),
            Color.parseColor("#22C55E"),
            Color.parseColor("#EC4899"),
            Color.parseColor("#EF4444"),
            Color.parseColor("#000000"),
            Color.parseColor("#8B5CF6"),
            Color.parseColor("#F97316")
    };

    public static StylesBottomSheet newInstance(CaptionStyle style, OnStylesApplyListener listener) {
        StylesBottomSheet sheet = new StylesBottomSheet();
        sheet.workingStyle = cloneStyle(style);
        sheet.applyListener = listener;
        return sheet;
    }

    private static CaptionStyle cloneStyle(CaptionStyle s) {
        CaptionStyle c = new CaptionStyle();
        if (s != null) {
            c.presetId = s.presetId;
            c.presetName = s.presetName;
            c.fontFamily = s.fontFamily;
            c.fontSize = s.fontSize;
            c.textColor = s.textColor;
            c.highlightColor = s.highlightColor;
            c.strokeColor = s.strokeColor;
            c.strokeWidth = s.strokeWidth;
            c.backgroundColor = s.backgroundColor;
            c.highlightBgColor = s.highlightBgColor;
            c.hasShadow = s.hasShadow;
            c.shadowColor = s.shadowColor;
            c.animationPreset = s.animationPreset;
            c.positionYPercent = s.positionYPercent;
            c.wordsPerChunk = s.wordsPerChunk;
            c.isItalic = s.isItalic;
            c.isBold = s.isBold;
            c.isUnderlined = s.isUnderlined;
            c.textAlign = s.textAlign;
            c.verticalAlign = s.verticalAlign;
            c.singleLine = s.singleLine;
            c.hasOutline = s.hasOutline;
        }
        return c;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_styles, container, false);

        initViews(v);
        setupTabs(v);
        setupFontPicker(v);
        setupSteppers(v);
        setupFormatButtons();
        setupAlignmentButtons();
        setupColorsAndEffects(v);
        updatePreview();

        v.findViewById(R.id.btnCancelStyles).setOnClickListener(view -> dismiss());
        v.findViewById(R.id.btnApplyStyles).setOnClickListener(view -> {
            if (applyListener != null) {
                applyListener.onApply(workingStyle);
            }
            dismiss();
        });

        return v;
    }

    private void initViews(View v) {
        tvLivePreview = v.findViewById(R.id.tvLivePreview);
        tvSelectedFontName = v.findViewById(R.id.tvSelectedFontName);
        tvFontSizeValue = v.findViewById(R.id.tvSizeValue);
        tvWordCountValue = v.findViewById(R.id.tvWordCountValue);

        panelStyles = v.findViewById(R.id.panelTabStyles);
        panelEffects = v.findViewById(R.id.panelTabEffects);
        subIndicatorStyles = v.findViewById(R.id.subIndicatorStyles);
        subIndicatorEffects = v.findViewById(R.id.subIndicatorEffects);
        tvTabStylesSubLabel = v.findViewById(R.id.tvTabStylesSubLabel);
        tvTabEffectsSubLabel = v.findViewById(R.id.tvTabEffectsSubLabel);

        btnItalic = v.findViewById(R.id.btnItalic);
        btnBold = v.findViewById(R.id.btnBold);
        btnUnderlined = v.findViewById(R.id.btnUnderlined);
        tvItalicLabel = v.findViewById(R.id.tvItalicLabel);
        tvBoldLabel = v.findViewById(R.id.tvBoldLabel);
        tvUnderlineLabel = v.findViewById(R.id.tvUnderlineLabel);

        btnAlignLeft = v.findViewById(R.id.btnAlignLeft);
        btnAlignCenter = v.findViewById(R.id.btnAlignCenter);
        btnAlignRight = v.findViewById(R.id.btnAlignRight);

        btnVAlignTop = v.findViewById(R.id.btnVAlignTop);
        btnVAlignCenter = v.findViewById(R.id.btnVAlignCenter);
        btnVAlignBottom = v.findViewById(R.id.btnVAlignBottom);

        boxColorText = v.findViewById(R.id.boxColorText);
        boxColorActiveWord = v.findViewById(R.id.boxColorActiveWord);
        boxColorTextBg = v.findViewById(R.id.boxColorTextBg);
        boxColorActiveWordBg = v.findViewById(R.id.boxColorActiveWordBg);
        barOutlineColor = v.findViewById(R.id.barOutlineColor);

        switchTextBg = v.findViewById(R.id.switchTextBg);
        switchActiveWordBg = v.findViewById(R.id.switchActiveWordBg);
        switchOutline = v.findViewById(R.id.switchOutline);
        switchShadow = v.findViewById(R.id.switchShadow);
        switchSingleLine = v.findViewById(R.id.switchSingleLine);
    }

    private void setupTabs(View v) {
        v.findViewById(R.id.tabStylesSubHeader).setOnClickListener(view -> {
            panelStyles.setVisibility(View.VISIBLE);
            panelEffects.setVisibility(View.GONE);
            subIndicatorStyles.setBackgroundColor(Color.parseColor("#0F172A"));
            subIndicatorEffects.setBackgroundColor(Color.parseColor("#E2E8F0"));
            tvTabStylesSubLabel.setTextColor(Color.parseColor("#0F172A"));
            tvTabEffectsSubLabel.setTextColor(Color.parseColor("#94A3B8"));
        });

        v.findViewById(R.id.tabEffectsSubHeader).setOnClickListener(view -> {
            panelStyles.setVisibility(View.GONE);
            panelEffects.setVisibility(View.VISIBLE);
            subIndicatorStyles.setBackgroundColor(Color.parseColor("#E2E8F0"));
            subIndicatorEffects.setBackgroundColor(Color.parseColor("#0F172A"));
            tvTabStylesSubLabel.setTextColor(Color.parseColor("#94A3B8"));
            tvTabEffectsSubLabel.setTextColor(Color.parseColor("#0F172A"));
        });
    }

    private void setupFontPicker(View v) {
        tvSelectedFontName.setText("Montserrat Black");
        v.findViewById(R.id.rowFontPicker).setOnClickListener(view -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Choose Font")
                    .setItems(fontNames, (dialog, which) -> {
                        workingStyle.fontFamily = fontFamilies[which];
                        tvSelectedFontName.setText(fontNames[which]);
                        updatePreview();
                    })
                    .show();
        });
    }

    private void setupSteppers(View v) {
        // Font Size Stepper
        if (tvFontSizeValue != null) {
            tvFontSizeValue.setText(String.valueOf(workingStyle.fontSize));
        }
        v.findViewById(R.id.btnSizeMinus).setOnClickListener(view -> {
            if (workingStyle.fontSize > 12) {
                workingStyle.fontSize -= 2;
                if (tvFontSizeValue != null) tvFontSizeValue.setText(String.valueOf(workingStyle.fontSize));
                updatePreview();
            }
        });
        v.findViewById(R.id.btnSizePlus).setOnClickListener(view -> {
            if (workingStyle.fontSize < 80) {
                workingStyle.fontSize += 2;
                if (tvFontSizeValue != null) tvFontSizeValue.setText(String.valueOf(workingStyle.fontSize));
                updatePreview();
            }
        });

        // Word Count Stepper
        if (tvWordCountValue != null) {
            tvWordCountValue.setText(String.valueOf(workingStyle.wordsPerChunk));
        }
        v.findViewById(R.id.btnWordCountMinus).setOnClickListener(view -> {
            if (workingStyle.wordsPerChunk > 1) {
                workingStyle.wordsPerChunk--;
                if (tvWordCountValue != null) tvWordCountValue.setText(String.valueOf(workingStyle.wordsPerChunk));
            }
        });
        v.findViewById(R.id.btnWordCountPlus).setOnClickListener(view -> {
            if (workingStyle.wordsPerChunk < 8) {
                workingStyle.wordsPerChunk++;
                if (tvWordCountValue != null) tvWordCountValue.setText(String.valueOf(workingStyle.wordsPerChunk));
            }
        });
    }

    private void setupFormatButtons() {
        btnItalic.setOnClickListener(v -> {
            workingStyle.isItalic = !workingStyle.isItalic;
            updateFormatUI();
            updatePreview();
        });

        btnBold.setOnClickListener(v -> {
            workingStyle.isBold = !workingStyle.isBold;
            updateFormatUI();
            updatePreview();
        });

        btnUnderlined.setOnClickListener(v -> {
            workingStyle.isUnderlined = !workingStyle.isUnderlined;
            updateFormatUI();
            updatePreview();
        });

        updateFormatUI();
    }

    private void updateFormatUI() {
        btnItalic.setBackgroundResource(workingStyle.isItalic ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        tvItalicLabel.setTextColor(workingStyle.isItalic ? Color.WHITE : Color.parseColor("#64748B"));

        btnBold.setBackgroundResource(workingStyle.isBold ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        tvBoldLabel.setTextColor(workingStyle.isBold ? Color.WHITE : Color.parseColor("#64748B"));

        btnUnderlined.setBackgroundResource(workingStyle.isUnderlined ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        tvUnderlineLabel.setTextColor(workingStyle.isUnderlined ? Color.WHITE : Color.parseColor("#64748B"));
    }

    private void setupAlignmentButtons() {
        btnAlignLeft.setOnClickListener(v -> {
            workingStyle.textAlign = "left";
            updateAlignmentUI();
            updatePreview();
        });

        btnAlignCenter.setOnClickListener(v -> {
            workingStyle.textAlign = "center";
            updateAlignmentUI();
            updatePreview();
        });

        btnAlignRight.setOnClickListener(v -> {
            workingStyle.textAlign = "right";
            updateAlignmentUI();
            updatePreview();
        });

        updateAlignmentUI();
    }

    private void updateAlignmentUI() {
        btnAlignLeft.setBackgroundResource("left".equalsIgnoreCase(workingStyle.textAlign) ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        btnAlignCenter.setBackgroundResource("center".equalsIgnoreCase(workingStyle.textAlign) ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        btnAlignRight.setBackgroundResource("right".equalsIgnoreCase(workingStyle.textAlign) ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
    }

    private void setupColorsAndEffects(View v) {
        // Text Color
        if (boxColorText != null) {
            boxColorText.setBackgroundColor(workingStyle.textColor != 0 ? workingStyle.textColor : Color.WHITE);
        }
        v.findViewById(R.id.rowTextColor).setOnClickListener(view -> {
            showColorPickerDialog("Choose Text Color", color -> {
                workingStyle.textColor = color;
                if (boxColorText != null) boxColorText.setBackgroundColor(color);
                updatePreview();
            });
        });

        // Active Word Color
        if (boxColorActiveWord != null) {
            boxColorActiveWord.setBackgroundColor(workingStyle.highlightColor != 0 ? workingStyle.highlightColor : Color.parseColor("#FACC15"));
        }
        v.findViewById(R.id.rowActiveWordColor).setOnClickListener(view -> {
            showColorPickerDialog("Choose Active Word Color", color -> {
                workingStyle.highlightColor = color;
                if (boxColorActiveWord != null) boxColorActiveWord.setBackgroundColor(color);
                updatePreview();
            });
        });

        // Text Background Switch & Color
        switchTextBg.setChecked(workingStyle.backgroundColor != 0 && workingStyle.backgroundColor != Color.TRANSPARENT);
        switchTextBg.setOnCheckedChangeListener((btn, isChecked) -> {
            workingStyle.backgroundColor = isChecked ? Color.parseColor("#99000000") : Color.TRANSPARENT;
            if (boxColorTextBg != null) boxColorTextBg.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updatePreview();
        });

        // Active Word Background Switch & Color
        switchActiveWordBg.setChecked(workingStyle.highlightBgColor != 0 && workingStyle.highlightBgColor != Color.TRANSPARENT);
        switchActiveWordBg.setOnCheckedChangeListener((btn, isChecked) -> {
            workingStyle.highlightBgColor = isChecked ? Color.parseColor("#FACC15") : Color.TRANSPARENT;
            if (boxColorActiveWordBg != null) boxColorActiveWordBg.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updatePreview();
        });

        // Outline Switch & Bar
        switchOutline.setChecked(workingStyle.hasOutline);
        switchOutline.setOnCheckedChangeListener((btn, isChecked) -> {
            workingStyle.hasOutline = isChecked;
            updatePreview();
        });
        if (barOutlineColor != null) {
            barOutlineColor.setOnClickListener(view -> {
                showColorPickerDialog("Choose Outline Color", color -> {
                    workingStyle.strokeColor = color;
                    barOutlineColor.setBackgroundColor(color);
                    updatePreview();
                });
            });
        }

        // Shadow Switch
        switchShadow.setChecked(workingStyle.hasShadow);
        switchShadow.setOnCheckedChangeListener((btn, isChecked) -> {
            workingStyle.hasShadow = isChecked;
            updatePreview();
        });

        // Single Line Switch
        switchSingleLine.setChecked(workingStyle.singleLine);
        switchSingleLine.setOnCheckedChangeListener((btn, isChecked) -> {
            workingStyle.singleLine = isChecked;
            if (isChecked) workingStyle.wordsPerChunk = 1;
            updatePreview();
        });

        // Vertical Alignment
        btnVAlignTop.setOnClickListener(view -> {
            workingStyle.verticalAlign = "top";
            updateVAlignUI();
        });
        btnVAlignCenter.setOnClickListener(view -> {
            workingStyle.verticalAlign = "center";
            updateVAlignUI();
        });
        btnVAlignBottom.setOnClickListener(view -> {
            workingStyle.verticalAlign = "bottom";
            updateVAlignUI();
        });

        updateVAlignUI();
    }

    private interface OnColorSelected {
        void onSelected(int color);
    }

    private void showColorPickerDialog(String title, OnColorSelected listener) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setItems(colorNameOptions, (dialog, which) -> {
                    listener.onSelected(colorValueOptions[which]);
                })
                .show();
    }

    private void updateVAlignUI() {
        btnVAlignTop.setBackgroundResource("top".equalsIgnoreCase(workingStyle.verticalAlign) ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        btnVAlignCenter.setBackgroundResource("center".equalsIgnoreCase(workingStyle.verticalAlign) ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        btnVAlignBottom.setBackgroundResource("bottom".equalsIgnoreCase(workingStyle.verticalAlign) ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
    }

    private void updatePreview() {
        if (tvLivePreview == null) return;

        int typefaceStyle = Typeface.NORMAL;
        if (workingStyle.isBold && workingStyle.isItalic) {
            typefaceStyle = Typeface.BOLD_ITALIC;
        } else if (workingStyle.isBold) {
            typefaceStyle = Typeface.BOLD;
        } else if (workingStyle.isItalic) {
            typefaceStyle = Typeface.ITALIC;
        }

        tvLivePreview.setTypeface(Typeface.create(workingStyle.fontFamily, typefaceStyle));
        tvLivePreview.setTextSize(Math.max(16, workingStyle.fontSize));
        tvLivePreview.setTextColor(workingStyle.highlightColor != 0 ? workingStyle.highlightColor : Color.parseColor("#FACC15"));

        if (workingStyle.isUnderlined) {
            tvLivePreview.setPaintFlags(tvLivePreview.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        } else {
            tvLivePreview.setPaintFlags(tvLivePreview.getPaintFlags() & (~android.graphics.Paint.UNDERLINE_TEXT_FLAG));
        }
    }
}
