package com.aether.aegis.ui.overview;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aether.aegis.MainActivity;
import com.aether.aegis.R;
import com.aether.aegis.adapters.ActivityAdapter;
import com.aether.aegis.adapters.AppAdapter;
import com.aether.aegis.data.model.AppInfo;
import com.aether.aegis.data.model.BehaviourEvent;
import com.aether.aegis.data.repository.AegisRepository;
import com.aether.aegis.databinding.FragmentOverviewBinding;
import com.aether.aegis.ui.appdetail.AppDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class OverviewFragment extends Fragment implements AegisRepository.DataChangeListener {

    private FragmentOverviewBinding binding;
    private AppAdapter riskyAppsAdapter;
    private ActivityAdapter activityAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOverviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAdapters();
        bindData();

        AegisRepository.getInstance().addListener(this);

        binding.btnInvestigateNow.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AppDetailActivity.class);
            intent.putExtra(AppDetailActivity.EXTRA_PACKAGE_NAME, "com.quickpdf.reader");
            startActivity(intent);
        });

        binding.btnQuickBlock.setOnClickListener(v -> {
            AegisRepository.getInstance().blockDomain("sync-audio-cdn.hopto.org");
            Toast.makeText(getContext(), "C2 Domain blocked via AEGIS Local DNS", Toast.LENGTH_SHORT).show();
        });

        binding.tvViewAllApps.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToApps();
            }
        });
    }

    private void setupAdapters() {
        riskyAppsAdapter = new AppAdapter(app -> {
            Intent intent = new Intent(getContext(), AppDetailActivity.class);
            intent.putExtra(AppDetailActivity.EXTRA_PACKAGE_NAME, app.getPackageName());
            startActivity(intent);
        });
        binding.rvTopRiskyApps.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTopRiskyApps.setAdapter(riskyAppsAdapter);

        activityAdapter = new ActivityAdapter();
        binding.rvOverviewActivity.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOverviewActivity.setAdapter(activityAdapter);
    }

    private void bindData() {
        AegisRepository repo = AegisRepository.getInstance();
        if (getContext() == null) return;

        int riskScore = repo.getDeviceRiskScore();
        binding.riskRingOverview.setScore(riskScore, true);

        // Dynamic Status according to strict cybersecurity thresholds
        if (riskScore >= 80) {
            binding.tvDeviceStatus.setText("IMMEDIATE ACTION REQUIRED");
            binding.tvDeviceStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.aegis_critical));
            binding.tvDeviceStatus.setBackgroundResource(R.drawable.bg_badge_danger);
            binding.tvActiveThreatSummary.setText("4 correlated signals detected · Spyware confirmed");
        } else if (riskScore >= 65) {
            binding.tvDeviceStatus.setText("THREAT DETECTED");
            binding.tvDeviceStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.aegis_danger));
            binding.tvDeviceStatus.setBackgroundResource(R.drawable.bg_badge_danger);
            binding.tvActiveThreatSummary.setText("Untrusted network host & telemetry anomalies active");
        } else if (riskScore >= 45) {
            binding.tvDeviceStatus.setText("ATTENTION REQUIRED");
            binding.tvDeviceStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.aegis_warning));
            binding.tvDeviceStatus.setBackgroundResource(R.drawable.bg_badge_warning);
            binding.tvActiveThreatSummary.setText("Elevated sensor & network drift detected");
        } else {
            binding.tvDeviceStatus.setText("DEVICE PROTECTED");
            binding.tvDeviceStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.aegis_safe));
            binding.tvDeviceStatus.setBackgroundResource(R.drawable.bg_badge_safe);
            binding.tvActiveThreatSummary.setText("All installed applications operating within baseline");
        }

        // Threat Risk Card
        int threatScore = repo.getThreatRiskScore();
        binding.tvThreatScore.setText(String.valueOf(threatScore));
        binding.pbThreatRisk.setProgress(threatScore);
        if (threatScore >= 60) {
            binding.pbThreatRisk.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.progress_bar_horizontal_danger));
            binding.tvThreatScore.setTextColor(ContextCompat.getColor(getContext(), R.color.aegis_danger));
            binding.tvThreatDelta.setText("↑ " + (threatScore - 20) + " from baseline");
        } else {
            binding.pbThreatRisk.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.progress_bar_horizontal_warning));
            binding.tvThreatScore.setTextColor(ContextCompat.getColor(getContext(), R.color.aegis_warning));
            binding.tvThreatDelta.setText("Standard baseline envelope");
        }

        // Privacy Risk Card
        int privacyScore = repo.getPrivacyRiskScore();
        binding.tvPrivacyScore.setText(String.valueOf(privacyScore));
        binding.pbPrivacyRisk.setProgress(privacyScore);
        if (privacyScore >= 60) {
            binding.pbPrivacyRisk.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.progress_bar_horizontal_warning));
            binding.tvPrivacyScore.setTextColor(ContextCompat.getColor(getContext(), R.color.aegis_warning));
            binding.tvPrivacyDelta.setText("↑ " + (privacyScore - 35) + " from baseline");
        } else {
            binding.pbPrivacyRisk.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.progress_bar_horizontal_safe));
            binding.tvPrivacyScore.setTextColor(ContextCompat.getColor(getContext(), R.color.aegis_safe));
            binding.tvPrivacyDelta.setText("Optimal privacy posture");
        }

        // Correlated Threat Investigation Card
        AppInfo targetApp = repo.getAppByPackageName("com.quickpdf.reader");
        if (targetApp != null && (targetApp.getRiskScore() >= 40 || repo.isDemoRunning())) {
            binding.cardImmediateAction.setVisibility(View.VISIBLE);
            binding.tvCorrelatedAppName.setText(targetApp.getName());
            binding.tvImmediateActionDesc.setText(targetApp.getExplanation());
            binding.tvCorrelatedThreatBadge.setText(targetApp.getRiskScore() + " / 100 · " + targetApp.getSeverity().getLabel());

            // Highlight evidence chips based on step progression
            int step = repo.getDemoStep();
            binding.chipMic.setAlpha(step >= 1 ? 1.0f : 0.35f);
            binding.chipBehaviour.setAlpha(step >= 2 ? 1.0f : 0.35f);
            binding.chipDomain.setAlpha(step >= 3 ? 1.0f : 0.35f);
            binding.chipIntel.setAlpha(step >= 4 ? 1.0f : 0.35f);
        } else {
            // Keep card accessible but benign
            binding.cardImmediateAction.setVisibility(View.VISIBLE);
            binding.tvCorrelatedAppName.setText("Baseline Integrity Verification");
            binding.tvImmediateActionDesc.setText("Continuous temporal correlation engine active. No anomalous signal convergence detected.");
            binding.tvCorrelatedThreatBadge.setText("28 / 100 · LOW");
            binding.chipMic.setAlpha(0.35f);
            binding.chipBehaviour.setAlpha(0.35f);
            binding.chipDomain.setAlpha(0.35f);
            binding.chipIntel.setAlpha(0.35f);
        }

        // Top Risky Apps (filter score >= 40 or top apps)
        List<AppInfo> riskyApps = new ArrayList<>();
        for (AppInfo app : repo.getApps()) {
            if (app.getRiskScore() >= 40) {
                riskyApps.add(app);
            }
        }
        if (riskyApps.isEmpty()) {
            // Show first 2 apps if all clean
            for (int i = 0; i < Math.min(2, repo.getApps().size()); i++) {
                riskyApps.add(repo.getApps().get(i));
            }
        }
        riskyAppsAdapter.updateList(riskyApps);

        // Recent 3 activity events
        List<BehaviourEvent> allEvents = repo.getTimeline();
        List<BehaviourEvent> topEvents = new ArrayList<>();
        for (int i = 0; i < Math.min(3, allEvents.size()); i++) {
            topEvents.add(allEvents.get(i));
        }
        activityAdapter.updateList(topEvents);
    }

    @Override
    public void onDataChanged() {
        if (isAdded()) {
            bindData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AegisRepository.getInstance().removeListener(this);
        binding = null;
    }
}
