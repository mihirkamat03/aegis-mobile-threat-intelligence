package com.aether.aegis;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.aether.aegis.data.repository.AegisRepository;
import com.aether.aegis.databinding.ActivityMainBinding;
import com.aether.aegis.ui.activity.ActivityFragment;
import com.aether.aegis.ui.apps.AppsFragment;
import com.aether.aegis.ui.motion.MotionUtils;
import com.aether.aegis.ui.overview.OverviewFragment;
import com.aether.aegis.ui.settings.SettingsFragment;
import com.aether.aegis.ui.threats.ThreatsFragment;

public class MainActivity extends AppCompatActivity implements AegisRepository.DataChangeListener {

    private ActivityMainBinding binding;

    private final OverviewFragment overviewFragment = new OverviewFragment();
    private final AppsFragment appsFragment = new AppsFragment();
    private final ThreatsFragment threatsFragment = new ThreatsFragment();
    private final ActivityFragment activityFragment = new ActivityFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();

    private Fragment currentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
        setupDemoButton();

        // Register repository listener for global status bar
        AegisRepository.getInstance().addListener(this);
        updateGlobalStatusUI();

        // Default tab: Overview
        if (savedInstanceState == null) {
            loadFragment(overviewFragment);
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_overview) {
                loadFragment(overviewFragment);
                return true;
            } else if (itemId == R.id.nav_apps) {
                loadFragment(appsFragment);
                return true;
            } else if (itemId == R.id.nav_threats) {
                loadFragment(threatsFragment);
                return true;
            } else if (itemId == R.id.nav_activity) {
                loadFragment(activityFragment);
                return true;
            } else if (itemId == R.id.nav_settings) {
                loadFragment(settingsFragment);
                return true;
            }
            return false;
        });
    }

    private void setupDemoButton() {
        updateDemoButtonUI();

        binding.btnDemoScenario.setOnClickListener(v -> {
            AegisRepository repo = AegisRepository.getInstance();
            if (repo.getDemoStep() >= 5) {
                repo.resetToDefaultState();
                Toast.makeText(this, "Demo state reset to clean baseline", Toast.LENGTH_SHORT).show();
            } else {
                repo.advanceDemoStep();
                Toast.makeText(this, repo.getDemoStepDescription(), Toast.LENGTH_SHORT).show();
            }
            updateDemoButtonUI();
        });

        binding.btnDemoScenario.setOnLongClickListener(v -> {
            AegisRepository.getInstance().resetToDefaultState();
            Toast.makeText(this, "Demo state reset to clean baseline", Toast.LENGTH_SHORT).show();
            updateDemoButtonUI();
            return true;
        });

        binding.tvLiveStatus.setOnClickListener(v -> {
            Toast.makeText(this, "Checking AEGIS intelligence sync...", Toast.LENGTH_SHORT).show();
            AegisRepository.getInstance().trySyncWithBackend(() -> {
                updateGlobalStatusUI();
                Toast.makeText(this, AegisRepository.getInstance().getBackendStatusLabel(), Toast.LENGTH_SHORT).show();
            });
        });
        MotionUtils.addPressFeedback(binding.btnDemoScenario);
    }

    private void updateDemoButtonUI() {
        AegisRepository repo = AegisRepository.getInstance();
        binding.btnDemoScenario.setText(repo.getDemoButtonLabel());
        if (repo.getDemoStep() >= 5) {
            binding.btnDemoScenario.setIconResource(R.drawable.ic_reset);
        } else {
            binding.btnDemoScenario.setIconResource(R.drawable.ic_play);
        }
    }

    private void updateGlobalStatusUI() {
        AegisRepository repo = AegisRepository.getInstance();
        if (repo.isBackendOnline()) {
            binding.tvLiveStatus.setText("● LIVE");
            binding.tvLiveStatus.setBackgroundResource(R.drawable.bg_badge_safe);
            binding.tvLiveStatus.setTextColor(ContextCompat.getColor(this, R.color.aegis_safe));
        } else {
            binding.tvLiveStatus.setText("● DEMO");
            binding.tvLiveStatus.setBackgroundResource(R.drawable.bg_badge_warning);
            binding.tvLiveStatus.setTextColor(ContextCompat.getColor(this, R.color.aegis_warning));
        }
    }

    public void navigateToApps() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_apps);
    }

    private void loadFragment(Fragment fragment) {
        if (fragment == currentFragment) return;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        currentFragment = fragment;
    }

    @Override
    public void onDataChanged() {
        updateGlobalStatusUI();
        updateDemoButtonUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AegisRepository.getInstance().removeListener(this);
    }
}
