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
                // Clamp size between 12sp and 80sp
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

                // Quick tap detection to toggle Play/Pause
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

        Typeface tf = Typeface.create(style.fontFamily, typefaceStyle);

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

        // Measure line width
        StringBuilder fullLine = new StringBuilder();
        for (WordCaption w : activeChunk.words) {
            fullLine.append(w.getWord().toUpperCase()).append(" ");
        }
        String fullText = fullLine.toString().trim();
        float totalLineWidth = textPaint.measureText(fullText);

        // Alignment start X
        float startX;
        if ("left".equalsIgnoreCase(style.textAlign)) {
            startX = 30f * density;
        } else if ("right".equalsIgnoreCase(style.textAlign)) {
            startX = width - totalLineWidth - (30f * density);
        } else {
            startX = (width - totalLineWidth) / 2f;
        }

        // Background Box for entire phrase if enabled
        if (style.backgroundColor != 0 && style.backgroundColor != Color.TRANSPARENT) {
            bgBoxPaint.setColor(style.backgroundColor);
            Rect bounds = new Rect();
            textPaint.getTextBounds(fullText, 0, fullText.length(), bounds);
            float padX = 18f * density;
            float padY = 10f * density;
            RectF boxRect = new RectF(
                    startX - padX,
                    centerY + bounds.top - padY,
                    startX + totalLineWidth + padX,
                    centerY + bounds.bottom + padY
            );
            canvas.drawRoundRect(boxRect, 10f * density, 10f * density, bgBoxPaint);
        }

        // Draw each word
        float currentX = startX;
        for (WordCaption w : activeChunk.words) {
            String wordText = w.getWord().toUpperCase();
            float wordWidth = textPaint.measureText(wordText);
            float spaceWidth = textPaint.measureText(" ");

            boolean isActive = currentPlaybackTime >= w.getStart() && currentPlaybackTime <= w.getEnd();
            Paint currentFillPaint = isActive ? highlightPaint : textPaint;

            // Highlight word background box (e.g. Word Background Change preset)
            if (isActive && style.highlightBgColor != 0 && style.highlightBgColor != Color.TRANSPARENT) {
                highlightBgPaint.setColor(style.highlightBgColor);
                Rect wBounds = new Rect();
                textPaint.getTextBounds(wordText, 0, wordText.length(), wBounds);
                float hPadX = 8f * density;
                float hPadY = 6f * density;
                RectF wBox = new RectF(
                        currentX - hPadX,
                        centerY + wBounds.top - hPadY,
                        currentX + wordWidth + hPadX,
                        centerY + wBounds.bottom + hPadY
                );
                canvas.drawRoundRect(wBox, 8f * density, 8f * density, highlightBgPaint);
            }

            // Stroke outline
            if (style.hasOutline && style.strokeWidth > 0) {
                canvas.drawText(wordText, currentX, centerY, strokePaint);
            }

            // Shadow
            if (style.hasShadow) {
                currentFillPaint.setShadowLayer(4f * density, 2f * density, 2f * density, style.shadowColor);
            }

            // Text Fill
            canvas.drawText(wordText, currentX, centerY, currentFillPaint);
            currentFillPaint.clearShadowLayer();

            currentX += (wordWidth + spaceWidth);
        }

        // When user is dragging or pinching, draw dashed bounding outline box (CapCut/InShot style)
        if (isDragging || scaleGestureDetector.isInProgress()) {
            Rect bounds = new Rect();
            textPaint.getTextBounds(fullText, 0, fullText.length(), bounds);
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
