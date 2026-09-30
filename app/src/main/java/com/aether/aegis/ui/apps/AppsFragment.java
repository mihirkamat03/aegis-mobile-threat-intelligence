package com.aether.aegis.ui.apps;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aether.aegis.R;
import com.aether.aegis.adapters.AppAdapter;
import com.aether.aegis.data.model.AppInfo;
import com.aether.aegis.data.model.Severity;
import com.aether.aegis.data.repository.AegisRepository;
import com.aether.aegis.databinding.FragmentAppsBinding;
import com.aether.aegis.ui.appdetail.AppDetailActivity;

import com.aether.aegis.ui.motion.MotionUtils;

import java.util.ArrayList;
import java.util.List;

public class AppsFragment extends Fragment implements AegisRepository.DataChangeListener {

    private FragmentAppsBinding binding;
    private AppAdapter appAdapter;
    private String currentFilter = "ALL";
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAppsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appAdapter = new AppAdapter(app -> {
            Intent intent = new Intent(getContext(), AppDetailActivity.class);
            intent.putExtra(AppDetailActivity.EXTRA_PACKAGE_NAME, app.getPackageName());
            startActivity(intent);
        });

        binding.rvAppsList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAppsList.setAdapter(appAdapter);

        setupFilters();
        setupSearch();
        filterAndDisplay();

        // Staggered entrance animation
        MotionUtils.staggerViews(
                binding.etSearchApps,
                binding.chipAll,
                binding.tvAppStatsCounter,
                binding.rvAppsList
        );

        // Touch feedback on filter chips
        MotionUtils.addPressFeedback(
                binding.chipAll,
                binding.chipHighRisk,
                binding.chipPrivacyDrift,
                binding.chipThreatMatched
        );

        AegisRepository.getInstance().addListener(this);
    }

    private void setupFilters() {
        binding.chipAll.setOnClickListener(v -> setFilter("ALL"));
        binding.chipHighRisk.setOnClickListener(v -> setFilter("HIGH_RISK"));
        binding.chipPrivacyDrift.setOnClickListener(v -> setFilter("PRIVACY_DRIFT"));
        binding.chipThreatMatched.setOnClickListener(v -> setFilter("THREAT_MATCHED"));
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateChipStyles();
        filterAndDisplay();
    }

    private void updateChipStyles() {
        int selectedBg = R.drawable.bg_chip_selected;
        int unselectedBg = R.drawable.bg_chip_unselected;
        int selectedText = getResources().getColor(R.color.white);
        int unselectedText = getResources().getColor(R.color.aegis_text_secondary);

        binding.chipAll.setBackgroundResource(currentFilter.equals("ALL") ? selectedBg : unselectedBg);
        binding.chipAll.setTextColor(currentFilter.equals("ALL") ? selectedText : unselectedText);

        binding.chipHighRisk.setBackgroundResource(currentFilter.equals("HIGH_RISK") ? selectedBg : unselectedBg);
        binding.chipHighRisk.setTextColor(currentFilter.equals("HIGH_RISK") ? selectedText : unselectedText);

        binding.chipPrivacyDrift.setBackgroundResource(currentFilter.equals("PRIVACY_DRIFT") ? selectedBg : unselectedBg);
        binding.chipPrivacyDrift.setTextColor(currentFilter.equals("PRIVACY_DRIFT") ? selectedText : unselectedText);

        binding.chipThreatMatched.setBackgroundResource(currentFilter.equals("THREAT_MATCHED") ? selectedBg : unselectedBg);
        binding.chipThreatMatched.setTextColor(currentFilter.equals("THREAT_MATCHED") ? selectedText : unselectedText);
    }

    private void setupSearch() {
        binding.etSearchApps.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                searchQuery = s.toString().trim().toLowerCase();
                filterAndDisplay();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterAndDisplay() {
        List<AppInfo> allApps = AegisRepository.getInstance().getApps();
        List<AppInfo> filtered = new ArrayList<>();

        for (AppInfo app : allApps) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    app.getName().toLowerCase().contains(searchQuery) ||
                    app.getPackageName().toLowerCase().contains(searchQuery);

            if (!matchesSearch) continue;

            if (currentFilter.equals("ALL")) {
                filtered.add(app);
            } else if (currentFilter.equals("HIGH_RISK")) {
                if (app.getSeverity() == Severity.HIGH || app.getSeverity() == Severity.CRITICAL) {
                    filtered.add(app);
                }
            } else if (currentFilter.equals("PRIVACY_DRIFT")) {
                if (app.getPrivacyScore() >= 50) {
                    filtered.add(app);
                }
            } else if (currentFilter.equals("THREAT_MATCHED")) {
                if (app.getThreatScore() >= 50) {
                    filtered.add(app);
                }
            }
        }

        appAdapter.updateList(filtered);

        if (filtered.isEmpty()) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.rvAppsList.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.rvAppsList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDataChanged() {
        if (isAdded()) {
            filterAndDisplay();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AegisRepository.getInstance().removeListener(this);
        binding = null;
    }
}
