package com.aether.aegis;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.aether.aegis.databinding.ActivitySplashBinding;
import com.aether.aegis.ui.motion.MotionUtils;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable launchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Luxurious spring entrance on shield emblem
        binding.flSplashEmblem.setScaleX(0.7f);
        binding.flSplashEmblem.setScaleY(0.7f);
        binding.flSplashEmblem.setAlpha(0.0f);
        binding.flSplashEmblem.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(650)
                .setInterpolator(new OvershootInterpolator(1.35f))
                .start();

        // Staggered entrance for wordmark, tagline, and footer
        MotionUtils.staggerViews(
                binding.tvSplashLogo,
                binding.tvSplashTagline,
                binding.tvSplashFooter
        );

        // Transition to Overview (MainActivity) after 1.2 seconds max
        launchRunnable = () -> {
            if (!isFinishing() && !isDestroyed()) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        };

        handler.postDelayed(launchRunnable, 1200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (launchRunnable != null) {
            handler.removeCallbacks(launchRunnable);
        }
        binding = null;
    }
}
