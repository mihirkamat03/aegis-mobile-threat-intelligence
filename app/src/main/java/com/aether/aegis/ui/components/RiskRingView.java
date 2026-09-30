package com.aether.aegis.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.aether.aegis.R;
import com.aether.aegis.data.model.Severity;

public class RiskRingView extends View {

    private Paint trackPaint;
    private Paint progressPaint;
    private Paint glowPaint;
    private Paint tickPaint;
    private Paint scoreTextPaint;
    private Paint labelTextPaint;
    private Paint subLabelTextPaint;

    private RectF arcBounds;
    private float strokeWidth;
    private float density;

    private int score = 0;
    private int currentAnimatedScore = 0;
    private float currentSweepAngle = 0f;
    private ValueAnimator animator;

    private int colorSafe;
    private int colorWarning;
    private int colorDanger;
    private int colorCritical;
    private int colorTrack;
    private int colorTextPrimary;
    private int colorTextSecondary;
    private int colorTextMuted;

    public RiskRingView(Context context) {
        super(context);
        init();
    }

    public RiskRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RiskRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        colorSafe = ContextCompat.getColor(getContext(), R.color.aegis_safe);
        colorWarning = ContextCompat.getColor(getContext(), R.color.aegis_warning);
        colorDanger = ContextCompat.getColor(getContext(), R.color.aegis_danger);
        colorCritical = ContextCompat.getColor(getContext(), R.color.aegis_critical);
        colorTrack = ContextCompat.getColor(getContext(), R.color.aegis_track);
        colorTextPrimary = ContextCompat.getColor(getContext(), R.color.aegis_text_primary);
        colorTextSecondary = ContextCompat.getColor(getContext(), R.color.aegis_text_secondary);
        colorTextMuted = ContextCompat.getColor(getContext(), R.color.aegis_text_muted);

        density = getResources().getDisplayMetrics().density;
        strokeWidth = density * 11f;

        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setColor(colorTrack);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(strokeWidth + (5f * density));
        glowPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeWidth(1.2f * density);
        tickPaint.setColor(colorTrack);
        tickPaint.setAlpha(180);

        scoreTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scoreTextPaint.setTextAlign(Paint.Align.CENTER);
        scoreTextPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        scoreTextPaint.setColor(colorTextPrimary);

        labelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelTextPaint.setTextAlign(Paint.Align.CENTER);
        labelTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        labelTextPaint.setLetterSpacing(0.14f);

        subLabelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subLabelTextPaint.setTextAlign(Paint.Align.CENTER);
        subLabelTextPaint.setColor(colorTextMuted);
        subLabelTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        subLabelTextPaint.setLetterSpacing(0.06f);

        arcBounds = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = strokeWidth + (12f * density);
        float size = Math.min(w, h);
        float left = (w - size) / 2f + padding;
        float top = (h - size) / 2f + padding;
        float right = left + size - (padding * 2f);
        float bottom = top + size - (padding * 2f);
        arcBounds.set(left, top, right, bottom);

        scoreTextPaint.setTextSize(size * 0.25f);
        labelTextPaint.setTextSize(size * 0.075f);
        subLabelTextPaint.setTextSize(size * 0.052f);
    }

    public void setScore(int newScore, boolean animate) {
        int clamped = Math.max(0, Math.min(100, newScore));
        if (score == clamped && currentAnimatedScore == clamped) {
            return; // Avoid unnecessary re-animation on scroll
        }

        int previousScore = currentAnimatedScore;
        score = clamped;
        updateSemanticColor(score);

        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        if (animate) {
            animator = ValueAnimator.ofInt(previousScore, score);
            animator.setDuration(750); // fast and professional 750ms
            animator.setInterpolator(new DecelerateInterpolator(1.8f));
            animator.addUpdateListener(animation -> {
                currentAnimatedScore = (int) animation.getAnimatedValue();
                currentSweepAngle = (currentAnimatedScore / 100f) * 280f;
                updateSemanticColor(currentAnimatedScore);
                invalidate();
            });
            animator.start();
        } else {
            currentAnimatedScore = score;
            currentSweepAngle = (score / 100f) * 280f;
            updateSemanticColor(score);
            invalidate();
        }
    }

    private void updateSemanticColor(int val) {
        int activeColor;
        if (val >= 80) {
            activeColor = colorCritical;
        } else if (val >= 60) {
            activeColor = colorDanger;
        } else if (val >= 40) {
            activeColor = colorWarning;
        } else {
            activeColor = colorSafe;
        }
        progressPaint.setColor(activeColor);
        glowPaint.setColor(activeColor);
        glowPaint.setAlpha(36); // Subtle light theme outer glow
        labelTextPaint.setColor(activeColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float startAngle = 130f;
        float sweepMax = 280f;

        float centerX = arcBounds.centerX();
        float centerY = arcBounds.centerY();
        float radius = arcBounds.width() / 2f;

        // 1. Draw precision instrument tick marks around perimeter
        float tickInnerRadius = radius + (strokeWidth / 2f) + (4f * density);
        float tickOuterRadius = tickInnerRadius + (3.5f * density);
        for (int i = 0; i <= 20; i++) {
            float angleDeg = startAngle + (i * (sweepMax / 20f));
            double angleRad = Math.toRadians(angleDeg);
            float cos = (float) Math.cos(angleRad);
            float sin = (float) Math.sin(angleRad);

            float x1 = centerX + (tickInnerRadius * cos);
            float y1 = centerY + (tickInnerRadius * sin);
            float x2 = centerX + (tickOuterRadius * cos);
            float y2 = centerY + (tickOuterRadius * sin);

            canvas.drawLine(x1, y1, x2, y2, tickPaint);
        }

        // 2. Draw base track
        canvas.drawArc(arcBounds, startAngle, sweepMax, false, trackPaint);

        // 3. Draw subtle halo & active arc
        if (currentSweepAngle > 0) {
            canvas.drawArc(arcBounds, startAngle, currentSweepAngle, false, glowPaint);
            canvas.drawArc(arcBounds, startAngle, currentSweepAngle, false, progressPaint);
        }

        // 4. Score number in center
        String scoreStr = String.valueOf(currentAnimatedScore);
        float scoreBaseline = centerY + (scoreTextPaint.getTextSize() * 0.18f);
        canvas.drawText(scoreStr, centerX, scoreBaseline, scoreTextPaint);

        // 5. Severity label below score
        Severity severity = Severity.fromScore(currentAnimatedScore);
        String labelStr = severity.getLabel();
        float labelBaseline = scoreBaseline + (labelTextPaint.getTextSize() * 1.5f);
        canvas.drawText(labelStr, centerX, labelBaseline, labelTextPaint);

        // 6. Overall Security Risk sub-label (for larger instances)
        if (arcBounds.width() > 100 * density) {
            float subLabelBaseline = labelBaseline + (subLabelTextPaint.getTextSize() * 1.6f);
            canvas.drawText("Overall Security Risk", centerX, subLabelBaseline, subLabelTextPaint);
        }
    }
}
