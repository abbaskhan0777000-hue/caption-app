package com.captionforge.nativeapp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import com.captionforge.nativeapp.engine.AssGenerator;
import com.captionforge.nativeapp.engine.FontManager;
import com.captionforge.nativeapp.model.CaptionStyle;
import com.captionforge.nativeapp.model.WordCaption;

import java.util.ArrayList;
import java.util.List;

public class CaptionOverlayView extends View {

    public interface OnOverlayTapListener {
        void onOverlayTap();
    }

    private List<WordCaption> words = new ArrayList<>();
    private CaptionStyle style = new CaptionStyle();
    private double currentPlaybackTime = 0.0;
    private OnOverlayTapListener tapListener;

    private Paint textPaint;
    private Paint strokePaint;
    private Paint highlightPaint;
    private Paint bgBoxPaint;
    private Paint highlightBgPaint;
    private Paint boundingBoxPaint;

    private float touchDownX = 0;
    private float touchDownY = 0;
    private long touchDownTime = 0;
    private boolean isDragging = false;
    private float currentScaleSize = 24f;
    private ScaleGestureDetector scaleGestureDetector;

    public CaptionOverlayView(Context context) {
        super(context);
        init(context);
    }

    public CaptionOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgBoxPaint.setStyle(Paint.Style.FILL);
        highlightBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightBgPaint.setStyle(Paint.Style.FILL);

        boundingBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boundingBoxPaint.setStyle(Paint.Style.STROKE);
        boundingBoxPaint.setColor(Color.parseColor("#38BDF8"));
        boundingBoxPaint.setStrokeWidth(2.5f);
        boundingBoxPaint.setPathEffect(new DashPathEffect(new float[]{12, 8}, 0));

        // Precision Pinch-to-Zoom Gesture Detector (CapCut & InShot style)
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                currentScaleSize = style.fontSize;
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float factor = detector.getScaleFactor();
                currentScaleSize *= factor;
                currentScaleSize = Math.max(12f, Math.min(80f, currentScaleSize));
                style.fontSize = Math.round(currentScaleSize);
                invalidate();
                return true;
            }
        });
    }

    public void setWords(List<WordCaption> words) {
        this.words = words != null ? words : new ArrayList<>();
        invalidate();
    }

    public void setStyle(CaptionStyle style) {
        this.style = style != null ? style : new CaptionStyle();
        this.currentScaleSize = this.style.fontSize;
        invalidate();
    }

    public CaptionStyle getStyle() {
        return style;
    }

    public void setOnOverlayTapListener(OnOverlayTapListener listener) {
        this.tapListener = listener;
    }

    public void updatePlaybackTime(double seconds) {
        this.currentPlaybackTime = seconds;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);

        if (scaleGestureDetector.isInProgress()) {
            isDragging = false;
            return true;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchDownX = event.getX();
                touchDownY = event.getY();
                touchDownTime = System.currentTimeMillis();
                isDragging = true;
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isDragging && event.getPointerCount() == 1 && getHeight() > 0) {
                    float newPercent = (event.getY() / (float) getHeight()) * 100f;
                    style.positionYPercent = Math.max(10f, Math.min(90f, newPercent));
                    style.verticalAlign = "custom";
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                isDragging = false;
                long clickDuration = System.currentTimeMillis() - touchDownTime;
                float distX = Math.abs(event.getX() - touchDownX);
                float distY = Math.abs(event.getY() - touchDownY);

                if (clickDuration < 250 && distX < 20 && distY < 20) {
                    if (tapListener != null) {
                        tapListener.onOverlayTap();
                    }
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                invalidate();
                return true;
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (words == null || words.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        // Find active caption chunk with exact millisecond precision
        List<AssGenerator.CaptionChunk> chunks = AssGenerator.chunkWords(words, style.wordsPerChunk);
        AssGenerator.CaptionChunk activeChunk = null;

        for (AssGenerator.CaptionChunk chunk : chunks) {
            if (currentPlaybackTime >= chunk.start && currentPlaybackTime <= chunk.end + 0.05) {
                activeChunk = chunk;
                break;
            }
        }

        // If player is paused and between chunks, show first chunk so user can pinch/drag
        if (activeChunk == null && isDragging && !chunks.isEmpty()) {
            activeChunk = chunks.get(0);
        }

        if (activeChunk == null) return;

        // Configure Typeface & Styling
        int typefaceStyle = Typeface.NORMAL;
        if (style.isBold && style.isItalic) {
            typefaceStyle = Typeface.BOLD_ITALIC;
        } else if (style.isBold) {
            typefaceStyle = Typeface.BOLD;
        } else if (style.isItalic) {
            typefaceStyle = Typeface.ITALIC;
        }

        Typeface tf = FontManager.getTypeface(getContext(), style.fontFamily, typefaceStyle);

        float density = getResources().getDisplayMetrics().density;
        float textSizePx = style.fontSize * density * 1.0f;

        textPaint.setTextSize(textSizePx);
        textPaint.setColor(style.textColor);
        textPaint.setTypeface(tf);
        textPaint.setUnderlineText(style.isUnderlined);

        strokePaint.setTextSize(textSizePx);
        strokePaint.setColor(style.strokeColor);
        strokePaint.setStrokeWidth(style.hasOutline ? style.strokeWidth * (density * 0.4f) : 0);
        strokePaint.setTypeface(tf);

        highlightPaint.setTextSize(textSizePx);
        highlightPaint.setColor(style.highlightColor);
        highlightPaint.setTypeface(tf);
        highlightPaint.setUnderlineText(style.isUnderlined);

        // Vertical Placement
        float centerY;
        if ("top".equalsIgnoreCase(style.verticalAlign)) {
            centerY = height * 0.18f;
        } else if ("center".equalsIgnoreCase(style.verticalAlign)) {
            centerY = height * 0.50f;
        } else if ("bottom".equalsIgnoreCase(style.verticalAlign)) {
            centerY = height * 0.78f;
        } else {
            centerY = (style.positionYPercent / 100f) * height;
        }

        // Template-Specific Dynamic Inter-Word Spacing (Eliminates background box bleed)
        boolean hasActiveWordBox = (style.highlightBgColor != 0 && style.highlightBgColor != Color.TRANSPARENT);
        float extraSpace = hasActiveWordBox ? (14f * density) : (3f * density);
        float spaceWidth = textPaint.measureText(" ") + extraSpace;

        // Calculate exact total line width with custom per-template spacing
        float totalLineWidth = 0;
        for (int i = 0; i < activeChunk.words.size(); i++) {
            WordCaption w = activeChunk.words.get(i);
            totalLineWidth += textPaint.measureText(w.getWord().toUpperCase());
            if (i < activeChunk.words.size() - 1) {
                totalLineWidth += spaceWidth;
            }
        }

        // Auto-scale down if text exceeds 92% of screen width (Prevents edge clipping)
        float maxAllowedWidth = width * 0.92f;
        if (totalLineWidth > maxAllowedWidth && totalLineWidth > 0) {
            float fitScale = maxAllowedWidth / totalLineWidth;
            textSizePx *= fitScale;
            textPaint.setTextSize(textSizePx);
            strokePaint.setTextSize(textSizePx);
            strokePaint.setStrokeWidth(style.hasOutline ? (style.strokeWidth * (density * 0.4f) * fitScale) : 0);
            highlightPaint.setTextSize(textSizePx);
            spaceWidth *= fitScale;

            totalLineWidth = 0;
            for (int i = 0; i < activeChunk.words.size(); i++) {
                WordCaption w = activeChunk.words.get(i);
                totalLineWidth += textPaint.measureText(w.getWord().toUpperCase());
                if (i < activeChunk.words.size() - 1) {
                    totalLineWidth += spaceWidth;
                }
            }
        }

        // Horizontal Alignment
        float startX;
        if ("left".equalsIgnoreCase(style.textAlign)) {
            startX = 20f * density;
        } else if ("right".equalsIgnoreCase(style.textAlign)) {
            startX = width - totalLineWidth - (20f * density);
        } else {
            startX = (width - totalLineWidth) / 2f;
        }

        // Background Box for entire phrase if enabled
        if (style.backgroundColor != 0 && style.backgroundColor != Color.TRANSPARENT) {
            bgBoxPaint.setColor(style.backgroundColor);
            Rect bounds = new Rect();
            textPaint.getTextBounds("A", 0, 1, bounds);
            float padX = 16f * density;
            float padY = 10f * density;
            RectF boxRect = new RectF(
                    startX - padX,
                    centerY + bounds.top - padY,
                    startX + totalLineWidth + padX,
                    centerY + bounds.bottom + padY
            );
            canvas.drawRoundRect(boxRect, 10f * density, 10f * density, bgBoxPaint);
        }

        // Animation presets: "pop", "bounce", "glow", "fade", "karaoke", "clean"
        String animPreset = style.animationPreset != null ? style.animationPreset.toLowerCase() : "karaoke";
        boolean isPop = animPreset.equals("pop");
        boolean isBounce = animPreset.equals("bounce");
        boolean isGlow = animPreset.equals("glow");
        boolean isFade = animPreset.equals("fade");

        float currentX = startX;

        for (WordCaption w : activeChunk.words) {
            String wordText = w.getWord().toUpperCase();
            float wordWidth = textPaint.measureText(wordText);

            boolean isActive = currentPlaybackTime >= w.getStart() && currentPlaybackTime <= w.getEnd();
            Paint currentFillPaint = isActive ? highlightPaint : textPaint;

            // Fade mode: subtle alpha on upcoming words, 100% on active
            if (isFade) {
                currentFillPaint.setAlpha(isActive ? 255 : 150);
            } else {
                currentFillPaint.setAlpha(255);
            }

            Rect wBounds = new Rect();
            textPaint.getTextBounds(wordText, 0, wordText.length(), wBounds);

            // Compute dynamic animation transforms for active word
            boolean needsTransform = isActive && (isPop || isBounce);
            if (needsTransform) {
                double dur = Math.max(0.05, w.getEnd() - w.getStart());
                double progress = Math.min(1.0, Math.max(0.0, (currentPlaybackTime - w.getStart()) / dur));

                float wordCenterX = currentX + (wordWidth / 2f);
                float wordCenterY = centerY + (wBounds.top + wBounds.bottom) / 2f;

                canvas.save();

                if (isPop) {
                    float scale;
                    if (progress < 0.25) {
                        scale = 1.0f + (float) (progress / 0.25) * 0.15f; // 1.0 -> 1.15
                    } else {
                        scale = 1.15f - (float) ((progress - 0.25) / 0.75) * 0.05f; // 1.15 -> 1.10
                    }
                    canvas.scale(scale, scale, wordCenterX, wordCenterY);
                } else if (isBounce) {
                    float bounceY = (float) (Math.sin(progress * Math.PI) * (-4.5f * density));
                    canvas.translate(0, bounceY);
                }
            }

            // Highlight word background box with isolated non-overlapping padding
            if (isActive && hasActiveWordBox) {
                highlightBgPaint.setColor(style.highlightBgColor);
                float hPadX = 6f * density;
                float hPadY = 6f * density;
                RectF wBox = new RectF(
                        currentX - hPadX,
                        centerY + wBounds.top - hPadY,
                        currentX + wordWidth + hPadX,
                        centerY + wBounds.bottom + hPadY
                );
                canvas.drawRoundRect(wBox, 6f * density, 6f * density, highlightBgPaint);
            }

            // Stroke outline
            if (style.hasOutline && style.strokeWidth > 0) {
                canvas.drawText(wordText, currentX, centerY, strokePaint);
            }

            // Glow / Shadow Animation
            if (isActive && isGlow) {
                int glowColor = (style.shadowColor != 0 && style.shadowColor != Color.TRANSPARENT) ? style.shadowColor : style.highlightColor;
                currentFillPaint.setShadowLayer(8f * density, 0, 0, glowColor);
            } else if (style.hasShadow) {
                currentFillPaint.setShadowLayer(4f * density, 2f * density, 2f * density, style.shadowColor);
            }

            // Text Fill
            canvas.drawText(wordText, currentX, centerY, currentFillPaint);
            currentFillPaint.clearShadowLayer();

            if (needsTransform) {
                canvas.restore();
            }

            currentX += (wordWidth + spaceWidth);
        }

        // Dashed bounding outline box when pinching or dragging
        if (isDragging || scaleGestureDetector.isInProgress()) {
            Rect bounds = new Rect();
            textPaint.getTextBounds("A", 0, 1, bounds);
            float pad = 12f * density;
            RectF boundRect = new RectF(
                    startX - pad,
                    centerY + bounds.top - pad,
                    startX + totalLineWidth + pad,
                    centerY + bounds.bottom + pad
            );
            canvas.drawRoundRect(boundRect, 8f * density, 8f * density, boundingBoxPaint);
        }
    }
}
