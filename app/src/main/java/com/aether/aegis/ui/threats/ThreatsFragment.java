package com.aether.aegis.ui.threats;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aether.aegis.R;
import com.aether.aegis.adapters.ThreatAdapter;
import com.aether.aegis.data.model.Severity;
import com.aether.aegis.data.model.Threat;
import com.aether.aegis.data.repository.AegisRepository;
import com.aether.aegis.databinding.FragmentThreatsBinding;
import com.aether.aegis.ui.appdetail.AppDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class ThreatsFragment extends Fragment implements AegisRepository.DataChangeListener {

    private FragmentThreatsBinding binding;
    private ThreatAdapter threatAdapter;
    private String currentFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentThreatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        threatAdapter = new ThreatAdapter(threat -> {
            Intent intent = new Intent(getContext(), AppDetailActivity.class);
            intent.putExtra(AppDetailActivity.EXTRA_PACKAGE_NAME, threat.getPackageName());
            startActivity(intent);
        });

        binding.rvThreatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvThreatsList.setAdapter(threatAdapter);

        setupFilters();
        filterAndDisplay();

        AegisRepository.getInstance().addListener(this);
    }

    private void setupFilters() {
        binding.chipThreatAll.setOnClickListener(v -> setFilter("ALL"));
        binding.chipThreatCritical.setOnClickListener(v -> setFilter("CRITICAL"));
        binding.chipThreatHigh.setOnClickListener(v -> setFilter("HIGH"));
        binding.chipThreatResolved.setOnClickListener(v -> setFilter("RESOLVED"));
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

        binding.chipThreatAll.setBackgroundResource(currentFilter.equals("ALL") ? selectedBg : unselectedBg);
        binding.chipThreatAll.setTextColor(currentFilter.equals("ALL") ? selectedText : unselectedText);

        binding.chipThreatCritical.setBackgroundResource(currentFilter.equals("CRITICAL") ? selectedBg : unselectedBg);
        binding.chipThreatCritical.setTextColor(currentFilter.equals("CRITICAL") ? selectedText : unselectedText);

        binding.chipThreatHigh.setBackgroundResource(currentFilter.equals("HIGH") ? selectedBg : unselectedBg);
        binding.chipThreatHigh.setTextColor(currentFilter.equals("HIGH") ? selectedText : unselectedText);

        binding.chipThreatResolved.setBackgroundResource(currentFilter.equals("RESOLVED") ? selectedBg : unselectedBg);
        binding.chipThreatResolved.setTextColor(currentFilter.equals("RESOLVED") ? selectedText : unselectedText);
    }

    private void filterAndDisplay() {
        List<Threat> allThreats = AegisRepository.getInstance().getThreats();
        List<Threat> filtered = new ArrayList<>();

        int activeCount = 0;
        int resolvedCount = 0;

        for (Threat t : allThreats) {
            if ("RESOLVED".equalsIgnoreCase(t.getStatus())) {
                resolvedCount++;
            } else {
                activeCount++;
            }

            if (currentFilter.equals("ALL")) {
                filtered.add(t);
            } else if (currentFilter.equals("CRITICAL") && t.getSeverity() == Severity.CRITICAL) {
                filtered.add(t);
            } else if (currentFilter.equals("HIGH") && t.getSeverity() == Severity.HIGH) {
                filtered.add(t);
            } else if (currentFilter.equals("RESOLVED") && "RESOLVED".equalsIgnoreCase(t.getStatus())) {
                filtered.add(t);
            }
        }

        binding.tvThreatCounter.setText(activeCount + " Active Threats · " + resolvedCount + " Resolved");
        threatAdapter.updateList(filtered);

        if (filtered.isEmpty()) {
            binding.layoutThreatsEmpty.setVisibility(View.VISIBLE);
            binding.rvThreatsList.setVisibility(View.GONE);
        } else {
            binding.layoutThreatsEmpty.setVisibility(View.GONE);
            binding.rvThreatsList.setVisibility(View.VISIBLE);
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
