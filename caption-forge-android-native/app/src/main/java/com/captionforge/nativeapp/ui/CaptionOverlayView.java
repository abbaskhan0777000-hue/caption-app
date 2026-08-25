package com.captionforge.nativeapp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.captionforge.nativeapp.engine.AssGenerator;
import com.captionforge.nativeapp.model.CaptionStyle;
import com.captionforge.nativeapp.model.WordCaption;

import java.util.ArrayList;
import java.util.List;

public class CaptionOverlayView extends View {
    private List<WordCaption> words = new ArrayList<>();
    private CaptionStyle style = new CaptionStyle();
    private double currentPlaybackTime = 0.0;

    private Paint textPaint;
    private Paint strokePaint;
    private Paint highlightPaint;
    private Paint bgBoxPaint;

    private float lastTouchY = 0;
    private boolean isDragging = false;

    public CaptionOverlayView(Context context) {
        super(context);
        init();
    }

    public CaptionOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setTextAlign(Paint.Align.CENTER);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setTextAlign(Paint.Align.CENTER);

        bgBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgBoxPaint.setStyle(Paint.Style.FILL);
    }

    public void setWords(List<WordCaption> words) {
        this.words = words != null ? words : new ArrayList<>();
        invalidate();
    }

    public void setStyle(CaptionStyle style) {
        this.style = style != null ? style : new CaptionStyle();
        invalidate();
    }

    public void updatePlaybackTime(double seconds) {
        this.currentPlaybackTime = seconds;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchY = event.getY();
                isDragging = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isDragging && getHeight() > 0) {
                    float newPercent = (event.getY() / getHeight()) * 100f;
                    style.positionYPercent = Math.max(15f, Math.min(85f, newPercent));
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (words == null || words.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        // Find active chunk
        List<AssGenerator.CaptionChunk> chunks = AssGenerator.chunkWords(words, style.wordsPerChunk);
        AssGenerator.CaptionChunk activeChunk = null;

        for (AssGenerator.CaptionChunk chunk : chunks) {
            if (currentPlaybackTime >= chunk.start && currentPlaybackTime <= chunk.end + 0.3) {
                activeChunk = chunk;
                break;
            }
        }

        if (activeChunk == null) return;

        // Configure Paints
        float textSizePx = style.fontSize * getResources().getDisplayMetrics().density * 0.5f;
        Typeface tf = Typeface.create(style.fontFamily, Typeface.BOLD);

        textPaint.setTextSize(textSizePx);
        textPaint.setColor(style.textColor);
        textPaint.setTypeface(tf);

        strokePaint.setTextSize(textSizePx);
        strokePaint.setColor(style.strokeColor);
        strokePaint.setStrokeWidth(style.strokeWidth * 0.6f);
        strokePaint.setTypeface(tf);

        highlightPaint.setTextSize(textSizePx);
        highlightPaint.setColor(style.highlightColor);
        highlightPaint.setTypeface(tf);

        float centerY = (style.positionYPercent / 100f) * height;

        // Measure full line width
        StringBuilder fullLine = new StringBuilder();
        for (WordCaption w : activeChunk.words) {
            fullLine.append(w.getWord().toUpperCase()).append(" ");
        }
        String fullText = fullLine.toString().trim();
        float totalLineWidth = textPaint.measureText(fullText);

        // Draw Background Box if present
        if (style.backgroundColor != 0 && style.backgroundColor != Color.TRANSPARENT) {
            bgBoxPaint.setColor(style.backgroundColor);
            Rect bounds = new Rect();
            textPaint.getTextBounds(fullText, 0, fullText.length(), bounds);
            float padX = 24f;
            float padY = 16f;
            RectF boxRect = new RectF(
                    (width - totalLineWidth) / 2f - padX,
                    centerY + bounds.top - padY,
                    (width + totalLineWidth) / 2f + padX,
                    centerY + bounds.bottom + padY
            );
            canvas.drawRoundRect(boxRect, 16f, 16f, bgBoxPaint);
        }

        // Draw word by word with active highlight
        float startX = (width - totalLineWidth) / 2f;
        float currentX = startX;

        for (WordCaption w : activeChunk.words) {
            String wordText = w.getWord().toUpperCase();
            float wordWidth = textPaint.measureText(wordText + " ");

            boolean isActive = currentPlaybackTime >= w.getStart() && currentPlaybackTime <= w.getEnd() + 0.1;
            Paint currentFillPaint = isActive ? highlightPaint : textPaint;

            float drawX = currentX + (textPaint.measureText(wordText) / 2f);

            // 1. Draw Stroke
            if (style.strokeWidth > 0) {
                canvas.drawText(wordText, drawX, centerY, strokePaint);
            }

            // 2. Draw Fill
            canvas.drawText(wordText, drawX, centerY, currentFillPaint);

            currentX += wordWidth;
        }
    }
}
