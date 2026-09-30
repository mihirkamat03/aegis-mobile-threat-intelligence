package com.aether.aegis;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.aether.aegis.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable launchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Subtle entrance micro-animation (smooth & purposeful)
        binding.llSplashContent.setAlpha(0.2f);
        binding.llSplashContent.setScaleX(0.94f);
        binding.llSplashContent.setScaleY(0.94f);

        binding.llSplashContent.animate()
                .alpha(1.0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator(1.6f))
                .start();

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
