package com.captionforge.nativeapp.ui;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.captionforge.nativeapp.R;
import com.captionforge.nativeapp.engine.FontManager;
import com.captionforge.nativeapp.model.CaptionStyle;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class StylesBottomSheet extends BottomSheetDialogFragment {

    public interface OnStylesApplyListener {
        void onApply(CaptionStyle updatedStyle);
    }

    private CaptionStyle initialStyle;
    private CaptionStyle workingStyle;
    private OnStylesApplyListener applyListener;

    private TextView tvLivePreview;
    private TextView tvSelectedFontName;
    private TextView tvFontSizeValue;
    private TextView tvWordCountValue;

    private View panelStyles;
    private View panelEffects;
    private View subIndicatorStyles;
    private View subIndicatorEffects;
    private TextView tvTabStylesSubLabel;
    private TextView tvTabEffectsSubLabel;

    private View btnItalic;
    private View btnBold;
    private View btnUnderlined;
    private TextView tvItalicLabel;
    private TextView tvBoldLabel;
    private TextView tvUnderlineLabel;

    private View btnAlignLeft;
    private View btnAlignCenter;
    private View btnAlignRight;

    private View btnVAlignTop;
    private View btnVAlignCenter;
    private View btnVAlignBottom;

    private View boxColorText;
    private View boxColorActiveWord;
    private View boxColorTextBg;
    private View boxColorActiveWordBg;
    private View barOutlineColor;

    private SwitchCompat switchTextBg;
    private SwitchCompat switchActiveWordBg;
    private SwitchCompat switchOutline;
    private SwitchCompat switchShadow;
    private SwitchCompat switchSingleLine;

    private final String[] colorNameOptions = {
            "Yellow Vibrant (#FACC15)", "Pure White (#FFFFFF)", "Neon Green (#22C55E)", "Electric Cyan (#38BDF8)",
            "Hot Pink (#EC4899)", "Sunset Orange (#F97316)", "Fiery Red (#EF4444)", "Royal Purple (#8B5CF6)",
            "Pitch Black (#000000)", "Transparent"
    };

    private final int[] colorValueOptions = {
            Color.parseColor("#FACC15"), Color.WHITE, Color.parseColor("#22C55E"), Color.parseColor("#38BDF8"),
            Color.parseColor("#EC4899"), Color.parseColor("#F97316"), Color.parseColor("#EF4444"), Color.parseColor("#8B5CF6"),
            Color.BLACK, Color.TRANSPARENT
    };

    private final ActivityResultLauncher<String[]> fontPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null && getContext() != null) {
                    FontManager.FontItem imported = FontManager.importCustomFont(getContext(), uri);
                    if (imported != null) {
                        workingStyle.fontFamily = imported.fontIdentifier;
                        if (tvSelectedFontName != null) {
                            tvSelectedFontName.setText(imported.displayName);
                        }
                        updatePreview();
                        Toast.makeText(getContext(), "🎉 Imported custom font: " + imported.displayName, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Could not import font. Please select a valid .ttf or .otf file.", Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

    public static StylesBottomSheet newInstance(CaptionStyle style, OnStylesApplyListener listener) {
        StylesBottomSheet sheet = new StylesBottomSheet();
        sheet.initialStyle = style;
        sheet.workingStyle = (style != null) ? cloneStyle(style) : new CaptionStyle();
        sheet.applyListener = listener;
        return sheet;
    }

    private static CaptionStyle cloneStyle(CaptionStyle s) {
        CaptionStyle c = new CaptionStyle();
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
        setupVerticalAlignmentButtons();
        setupColorPickers();
        setupEffectsSwitches();

        View btnCancel = v.findViewById(R.id.btnCancelStyles);
        if (btnCancel != null) btnCancel.setOnClickListener(view -> dismiss());

        View btnApply = v.findViewById(R.id.btnApplyStyles);
        if (btnApply != null) {
            btnApply.setOnClickListener(view -> {
                if (applyListener != null) {
                    applyListener.onApply(workingStyle);
                }
                dismiss();
            });
        }

        updatePreview();
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
        View tabStyles = v.findViewById(R.id.tabStylesSubHeader);
        if (tabStyles != null) {
            tabStyles.setOnClickListener(view -> {
                if (panelStyles != null) panelStyles.setVisibility(View.VISIBLE);
                if (panelEffects != null) panelEffects.setVisibility(View.GONE);
                if (subIndicatorStyles != null) subIndicatorStyles.setBackgroundColor(Color.parseColor("#0F172A"));
                if (subIndicatorEffects != null) subIndicatorEffects.setBackgroundColor(Color.parseColor("#E2E8F0"));
                if (tvTabStylesSubLabel != null) tvTabStylesSubLabel.setTextColor(Color.parseColor("#0F172A"));
                if (tvTabEffectsSubLabel != null) tvTabEffectsSubLabel.setTextColor(Color.parseColor("#94A3B8"));
            });
        }

        View tabEffects = v.findViewById(R.id.tabEffectsSubHeader);
        if (tabEffects != null) {
            tabEffects.setOnClickListener(view -> {
                if (panelStyles != null) panelStyles.setVisibility(View.GONE);
                if (panelEffects != null) panelEffects.setVisibility(View.VISIBLE);
                if (subIndicatorStyles != null) subIndicatorStyles.setBackgroundColor(Color.parseColor("#E2E8F0"));
                if (subIndicatorEffects != null) subIndicatorEffects.setBackgroundColor(Color.parseColor("#0F172A"));
                if (tvTabStylesSubLabel != null) tvTabStylesSubLabel.setTextColor(Color.parseColor("#94A3B8"));
                if (tvTabEffectsSubLabel != null) tvTabEffectsSubLabel.setTextColor(Color.parseColor("#0F172A"));
            });
        }
    }

    private void setupFontPicker(View v) {
        if (workingStyle.fontFamily != null && tvSelectedFontName != null) {
            String cleanName = workingStyle.fontFamily.substring(workingStyle.fontFamily.lastIndexOf('/') + 1)
                    .replace(".ttf", "").replace(".otf", "").replace("sans-serif-", "");
            tvSelectedFontName.setText(cleanName);
        } else if (tvSelectedFontName != null) {
            tvSelectedFontName.setText("Montserrat Black");
        }

        View rowFontPicker = v.findViewById(R.id.rowFontPicker);
        if (rowFontPicker != null) {
            rowFontPicker.setOnClickListener(view -> {
                if (getContext() == null) return;
                List<FontManager.FontItem> fonts = FontManager.getAllFonts(getContext());

                String[] options = new String[fonts.size() + 1];
                options[0] = "➕ Import Custom Font (.ttf / .otf)";
                for (int i = 0; i < fonts.size(); i++) {
                    options[i + 1] = fonts.get(i).displayName;
                }

                new AlertDialog.Builder(getContext())
                        .setTitle("Choose / Import Font")
                        .setItems(options, (dialog, which) -> {
                            if (which == 0) {
                                fontPickerLauncher.launch(new String[]{"*/*"});
                            } else {
                                FontManager.FontItem selected = fonts.get(which - 1);
                                workingStyle.fontFamily = selected.fontIdentifier;
                                if (tvSelectedFontName != null) {
                                    tvSelectedFontName.setText(selected.displayName);
                                }
                                updatePreview();
                            }
                        })
                        .show();
            });
        }
    }

    private void setupSteppers(View v) {
        if (tvFontSizeValue != null) {
            tvFontSizeValue.setText(String.valueOf(workingStyle.fontSize));
        }
        View btnMinus = v.findViewById(R.id.btnSizeMinus);
        if (btnMinus != null) {
            btnMinus.setOnClickListener(view -> {
                if (workingStyle.fontSize > 12) {
                    workingStyle.fontSize -= 2;
                    if (tvFontSizeValue != null) tvFontSizeValue.setText(String.valueOf(workingStyle.fontSize));
                    updatePreview();
                }
            });
        }
        View btnPlus = v.findViewById(R.id.btnSizePlus);
        if (btnPlus != null) {
            btnPlus.setOnClickListener(view -> {
                if (workingStyle.fontSize < 80) {
                    workingStyle.fontSize += 2;
                    if (tvFontSizeValue != null) tvFontSizeValue.setText(String.valueOf(workingStyle.fontSize));
                    updatePreview();
                }
            });
        }

        if (tvWordCountValue != null) {
            tvWordCountValue.setText(String.valueOf(workingStyle.wordsPerChunk));
        }
        View btnWordMinus = v.findViewById(R.id.btnWordCountMinus);
        if (btnWordMinus != null) {
            btnWordMinus.setOnClickListener(view -> {
                if (workingStyle.wordsPerChunk > 1) {
                    workingStyle.wordsPerChunk--;
                    if (tvWordCountValue != null) tvWordCountValue.setText(String.valueOf(workingStyle.wordsPerChunk));
                }
            });
        }
        View btnWordPlus = v.findViewById(R.id.btnWordCountPlus);
        if (btnWordPlus != null) {
            btnWordPlus.setOnClickListener(view -> {
                if (workingStyle.wordsPerChunk < 8) {
                    workingStyle.wordsPerChunk++;
                    if (tvWordCountValue != null) tvWordCountValue.setText(String.valueOf(workingStyle.wordsPerChunk));
                }
            });
        }
    }

    private void setupFormatButtons() {
        if (btnItalic != null) {
            btnItalic.setOnClickListener(v -> {
                workingStyle.isItalic = !workingStyle.isItalic;
                updateFormatUI();
                updatePreview();
            });
        }

        if (btnBold != null) {
            btnBold.setOnClickListener(v -> {
                workingStyle.isBold = !workingStyle.isBold;
                updateFormatUI();
                updatePreview();
            });
        }

        if (btnUnderlined != null) {
            btnUnderlined.setOnClickListener(v -> {
                workingStyle.isUnderlined = !workingStyle.isUnderlined;
                updateFormatUI();
                updatePreview();
            });
        }

        updateFormatUI();
    }

    private void updateFormatUI() {
        if (btnItalic != null) {
            btnItalic.setBackgroundResource(workingStyle.isItalic ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        }
        if (tvItalicLabel != null) {
            tvItalicLabel.setTextColor(workingStyle.isItalic ? Color.WHITE : Color.parseColor("#0F172A"));
        }

        if (btnBold != null) {
            btnBold.setBackgroundResource(workingStyle.isBold ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        }
        if (tvBoldLabel != null) {
            tvBoldLabel.setTextColor(workingStyle.isBold ? Color.WHITE : Color.parseColor("#0F172A"));
        }

        if (btnUnderlined != null) {
            btnUnderlined.setBackgroundResource(workingStyle.isUnderlined ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        }
        if (tvUnderlineLabel != null) {
            tvUnderlineLabel.setTextColor(workingStyle.isUnderlined ? Color.WHITE : Color.parseColor("#0F172A"));
        }
    }

    private void setupAlignmentButtons() {
        if (btnAlignLeft != null) {
            btnAlignLeft.setOnClickListener(v -> {
                workingStyle.textAlign = "left";
                updateAlignUI();
            });
        }
        if (btnAlignCenter != null) {
            btnAlignCenter.setOnClickListener(v -> {
                workingStyle.textAlign = "center";
                updateAlignUI();
            });
        }
        if (btnAlignRight != null) {
            btnAlignRight.setOnClickListener(v -> {
                workingStyle.textAlign = "right";
                updateAlignUI();
            });
        }

        updateAlignUI();
    }

    private void updateAlignUI() {
        if (btnAlignLeft != null) {
            btnAlignLeft.setBackgroundResource("left".equalsIgnoreCase(workingStyle.textAlign) ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        }
        if (btnAlignCenter != null) {
            btnAlignCenter.setBackgroundResource("center".equalsIgnoreCase(workingStyle.textAlign) ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        }
        if (btnAlignRight != null) {
            btnAlignRight.setBackgroundResource("right".equalsIgnoreCase(workingStyle.textAlign) ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        }
    }

    private void setupColorPickers() {
        if (boxColorText != null) {
            boxColorText.setOnClickListener(v -> showColorPickerDialog("Text Color", color -> {
                workingStyle.textColor = color;
                boxColorText.setBackgroundColor(color);
                updatePreview();
            }));
        }

        if (boxColorActiveWord != null) {
            boxColorActiveWord.setOnClickListener(v -> showColorPickerDialog("Active Word Color", color -> {
                workingStyle.highlightColor = color;
                boxColorActiveWord.setBackgroundColor(color);
                updatePreview();
            }));
        }

        if (boxColorTextBg != null) {
            boxColorTextBg.setOnClickListener(v -> showColorPickerDialog("Text Background Color", color -> {
                workingStyle.backgroundColor = color;
                boxColorTextBg.setBackgroundColor(color);
                updatePreview();
            }));
        }

        if (boxColorActiveWordBg != null) {
            boxColorActiveWordBg.setOnClickListener(v -> showColorPickerDialog("Active Word Background", color -> {
                workingStyle.highlightBgColor = color;
                boxColorActiveWordBg.setBackgroundColor(color);
                updatePreview();
            }));
        }

        if (barOutlineColor != null) {
            barOutlineColor.setOnClickListener(v -> showColorPickerDialog("Outline Color", color -> {
                workingStyle.strokeColor = color;
                barOutlineColor.setBackgroundColor(color);
                updatePreview();
            }));
        }
    }

    private void setupEffectsSwitches() {
        if (switchTextBg != null) {
            switchTextBg.setChecked(workingStyle.backgroundColor != 0 && workingStyle.backgroundColor != Color.TRANSPARENT);
            switchTextBg.setOnCheckedChangeListener((btn, checked) -> {
                if (!checked) workingStyle.backgroundColor = Color.TRANSPARENT;
                else if (workingStyle.backgroundColor == 0 || workingStyle.backgroundColor == Color.TRANSPARENT) {
                    workingStyle.backgroundColor = Color.parseColor("#B3000000");
                }
                updatePreview();
            });
        }

        if (switchActiveWordBg != null) {
            switchActiveWordBg.setChecked(workingStyle.highlightBgColor != 0 && workingStyle.highlightBgColor != Color.TRANSPARENT);
            switchActiveWordBg.setOnCheckedChangeListener((btn, checked) -> {
                if (!checked) workingStyle.highlightBgColor = Color.TRANSPARENT;
                else if (workingStyle.highlightBgColor == 0 || workingStyle.highlightBgColor == Color.TRANSPARENT) {
                    workingStyle.highlightBgColor = Color.parseColor("#FACC15");
                }
                updatePreview();
            });
        }

        if (switchOutline != null) {
            switchOutline.setChecked(workingStyle.hasOutline);
            switchOutline.setOnCheckedChangeListener((btn, checked) -> {
                workingStyle.hasOutline = checked;
                updatePreview();
            });
        }

        if (switchShadow != null) {
            switchShadow.setChecked(workingStyle.hasShadow);
            switchShadow.setOnCheckedChangeListener((btn, checked) -> {
                workingStyle.hasShadow = checked;
                updatePreview();
            });
        }

        if (switchSingleLine != null) {
            switchSingleLine.setChecked(workingStyle.singleLine || workingStyle.wordsPerChunk == 1);
            switchSingleLine.setOnCheckedChangeListener((btn, checked) -> {
                workingStyle.singleLine = checked;
                if (checked) workingStyle.wordsPerChunk = 1;
                if (tvWordCountValue != null) tvWordCountValue.setText(String.valueOf(workingStyle.wordsPerChunk));
                updatePreview();
            });
        }
    }

    private void setupVerticalAlignmentButtons() {
        if (btnVAlignTop != null) {
            btnVAlignTop.setOnClickListener(view -> {
                workingStyle.verticalAlign = "top";
                updateVAlignUI();
            });
        }
        if (btnVAlignCenter != null) {
            btnVAlignCenter.setOnClickListener(view -> {
                workingStyle.verticalAlign = "center";
                updateVAlignUI();
            });
        }
        if (btnVAlignBottom != null) {
            btnVAlignBottom.setOnClickListener(view -> {
                workingStyle.verticalAlign = "bottom";
                updateVAlignUI();
            });
        }

        updateVAlignUI();
    }

    private interface OnColorSelected {
        void onSelected(int color);
    }

    private void showColorPickerDialog(String title, OnColorSelected listener) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setItems(colorNameOptions, (dialog, which) -> {
                    listener.onSelected(colorValueOptions[which]);
                })
                .show();
    }

    private void updateVAlignUI() {
        if (btnVAlignTop != null) {
            btnVAlignTop.setBackgroundResource("top".equalsIgnoreCase(workingStyle.verticalAlign) ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        }
        if (btnVAlignCenter != null) {
            btnVAlignCenter.setBackgroundResource("center".equalsIgnoreCase(workingStyle.verticalAlign) ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        }
        if (btnVAlignBottom != null) {
            btnVAlignBottom.setBackgroundResource("bottom".equalsIgnoreCase(workingStyle.verticalAlign) ? R.drawable.bg_dark_pill : R.drawable.bg_white_button);
        }
    }

    private void updatePreview() {
        if (tvLivePreview == null || getContext() == null) return;

        int typefaceStyle = Typeface.NORMAL;
        if (workingStyle.isBold && workingStyle.isItalic) {
            typefaceStyle = Typeface.BOLD_ITALIC;
        } else if (workingStyle.isBold) {
            typefaceStyle = Typeface.BOLD;
        } else if (workingStyle.isItalic) {
            typefaceStyle = Typeface.ITALIC;
        }

        Typeface tf = FontManager.getTypeface(getContext(), workingStyle.fontFamily, typefaceStyle);
        tvLivePreview.setTypeface(tf);
        tvLivePreview.setTextSize(Math.max(16, workingStyle.fontSize));
        tvLivePreview.setTextColor(workingStyle.highlightColor != 0 ? workingStyle.highlightColor : Color.parseColor("#FACC15"));

        if (workingStyle.isUnderlined) {
            tvLivePreview.setPaintFlags(tvLivePreview.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        } else {
            tvLivePreview.setPaintFlags(tvLivePreview.getPaintFlags() & (~android.graphics.Paint.UNDERLINE_TEXT_FLAG));
        }
    }
}
