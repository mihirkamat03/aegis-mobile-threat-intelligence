package com.aether.aegis.ui.motion;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class MotionUtils {

    /**
     * Staggered entrance animation for screen elements.
     * Elements gracefully assemble with alpha, upward translation, and micro-scale.
     */
    public static void staggerViews(View... views) {
        staggerViewsWithOffset(40, 55, views);
    }

    public static void staggerViewsWithOffset(long baseDelay, long interval, View... views) {
        if (views == null) return;

        for (int i = 0; i < views.length; i++) {
            final View view = views[i];
            if (view == null) continue;

            view.setAlpha(0f);
            view.setTranslationY(24f);
            view.setScaleX(0.96f);
            view.setScaleY(0.96f);

            long delay = baseDelay + (i * interval);

            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(360)
                    .setStartDelay(delay)
                    .setInterpolator(new DecelerateInterpolator(1.8f))
                    .start();
        }
    }

    /**
     * Micro-interaction: Adds subtle scale/touch feedback to buttons or cards.
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void addPressFeedback(View... views) {
        if (views == null) return;

        for (View view : views) {
            if (view == null) continue;

            view.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate()
                                .scaleX(0.965f)
                                .scaleY(0.965f)
                                .setDuration(120)
                                .setInterpolator(new DecelerateInterpolator())
                                .start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(180)
                                .setInterpolator(new OvershootInterpolator(1.6f))
                                .start();
                        break;
                }
                return false; // Allow click listener to proceed
            });
        }
    }

    /**
     * Micro-interaction: Gentle pulsing animation for active threats or critical indicators.
     */
    public static ObjectAnimator startGentlePulse(View view) {
        if (view == null) return null;

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.06f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.06f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.85f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha);
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
        return animator;
    }
}
