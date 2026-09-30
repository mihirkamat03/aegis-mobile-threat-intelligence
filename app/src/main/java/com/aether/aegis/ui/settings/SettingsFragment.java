package com.aether.aegis.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aether.aegis.data.repository.AegisRepository;
import com.aether.aegis.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.switchLocalDns.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "Enabled: Local C2 blocking active" : "Disabled";
            Toast.makeText(getContext(), "Local DNS Firewall " + status, Toast.LENGTH_SHORT).show();
        });

        binding.switchThreatFeeds.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "Active (AbuseIPDB, OTX, Quad9)" : "Paused";
            Toast.makeText(getContext(), "Threat Feeds " + status, Toast.LENGTH_SHORT).show();
        });

        binding.btnClearHistory.setOnClickListener(v -> {
            AegisRepository.getInstance().resetToDefaultState();
            Toast.makeText(getContext(), "Local behavioural cache purged successfully.", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
