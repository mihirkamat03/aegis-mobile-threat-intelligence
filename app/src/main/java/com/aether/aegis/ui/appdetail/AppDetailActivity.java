package com.aether.aegis.ui.appdetail;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aether.aegis.R;
import com.aether.aegis.adapters.DomainAdapter;
import com.aether.aegis.adapters.PermissionAdapter;
import com.aether.aegis.adapters.RecommendationAdapter;
import com.aether.aegis.data.model.AppInfo;
import com.aether.aegis.data.model.EvidenceNode;
import com.aether.aegis.data.repository.AegisRepository;
import com.aether.aegis.databinding.ActivityAppDetailBinding;

public class AppDetailActivity extends AppCompatActivity implements AegisRepository.DataChangeListener {

    public static final String EXTRA_PACKAGE_NAME = "extra_package_name";

    private ActivityAppDetailBinding binding;
    private AppInfo appInfo;

    private PermissionAdapter permissionAdapter;
    private DomainAdapter domainAdapter;
    private RecommendationAdapter recommendationAdapter;

    private enum ActiveCategoryTab {
        PERMISSIONS,
        NETWORK,
        RECOMMENDATIONS
    }

    private ActiveCategoryTab currentTab = ActiveCategoryTab.PERMISSIONS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
        if (packageName == null) packageName = "com.quickpdf.reader";

        appInfo = AegisRepository.getInstance().getAppByPackageName(packageName);
        if (appInfo == null) {
            Toast.makeText(this, "Application telemetry not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupEvidenceGraph();
        setupCategoryTabs();
        populateAppDetails();

        // Staggered entrance animations
        com.aether.aegis.ui.motion.MotionUtils.staggerViews(
                binding.ivDetailAppIcon,
                binding.tvDetailAppName,
                binding.detailRiskRing,
                binding.tvDiagnosticExplanation,
                binding.evidenceGraphView,
                binding.tvSelectedNodeDetail,
                binding.tabPerms,
                binding.rvCategoryItems
        );

        // Touch press feedback on category tabs
        com.aether.aegis.ui.motion.MotionUtils.addPressFeedback(
                binding.tabPerms,
                binding.tabNetwork,
                binding.tabRecs
        );

        AegisRepository.getInstance().addListener(this);
    }

    private void setupToolbar() {
        binding.detailToolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, R.anim.fragment_exit);
        });
    }

    private void populateAppDetails() {
        binding.tvDetailAppName.setText(appInfo.getName());
        binding.tvDetailPackage.setText(appInfo.getPackageName() + " · v" + appInfo.getVersion());
        binding.tvInstallSourceBadge.setText(appInfo.getInstallSource());

        binding.detailRiskRing.setScore(appInfo.getRiskScore(), true);
        binding.tvDiagnosticExplanation.setText(appInfo.getExplanation());

        boolean isQuickPdf = "com.quickpdf.reader".equals(appInfo.getPackageName());
        if (isQuickPdf && appInfo.getRiskScore() >= 40) {
            binding.tvCorrelationBadge.setVisibility(View.VISIBLE);
            binding.llRiskBreakdown.setVisibility(View.VISIBLE);
            binding.dividerRiskBreakdown.setVisibility(View.VISIBLE);
            binding.llRiskEvolution.setVisibility(View.VISIBLE);
            binding.dividerRiskEvolution.setVisibility(View.VISIBLE);

            binding.tvBreakdownTotal.setText(appInfo.getRiskScore() + " / 100");

            int score = appInfo.getRiskScore();
            binding.pillStep0.setAlpha(score == 28 ? 1.0f : 0.4f);
            binding.pillStep1.setAlpha(score == 43 ? 1.0f : 0.4f);
            binding.pillStep2.setAlpha(score == 61 ? 1.0f : 0.4f);
            binding.pillStep3.setAlpha(score == 72 ? 1.0f : 0.4f);
            binding.pillStep4.setAlpha(score >= 84 ? 1.0f : 0.4f);
        } else {
            binding.tvCorrelationBadge.setVisibility(appInfo.getRiskScore() >= 60 ? View.VISIBLE : View.GONE);
            binding.llRiskBreakdown.setVisibility(View.GONE);
            binding.dividerRiskBreakdown.setVisibility(View.GONE);
            binding.llRiskEvolution.setVisibility(View.GONE);
            binding.dividerRiskEvolution.setVisibility(View.GONE);
        }

        refreshCategoryList();
    }

    private void setupEvidenceGraph() {
        binding.evidenceGraphView.setGraphData(appInfo.getEvidenceNodes(), appInfo.getEvidenceEdges());
        binding.evidenceGraphView.setOnNodeSelectedListener(node -> {
            String detailText = "Selected: " + node.getLabel() + " · " + node.getSubtitle() + " [" + node.getSeverity().getLabel() + "]";
            binding.tvSelectedNodeDetail.setText(detailText);
        });
    }

    private void setupCategoryTabs() {
        permissionAdapter = new PermissionAdapter();
        domainAdapter = new DomainAdapter(domain -> {
            AegisRepository.getInstance().blockDomain(domain.getDomain());
            Toast.makeText(this, "Blocked " + domain.getDomain() + " on-device", Toast.LENGTH_SHORT).show();
        });
        recommendationAdapter = new RecommendationAdapter(rec -> {
            rec.setCompleted(true);
            recommendationAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Executed: " + rec.getTitle(), Toast.LENGTH_SHORT).show();
        });

        binding.rvCategoryItems.setLayoutManager(new LinearLayoutManager(this));

        binding.tabPerms.setOnClickListener(v -> switchTab(ActiveCategoryTab.PERMISSIONS));
        binding.tabNetwork.setOnClickListener(v -> switchTab(ActiveCategoryTab.NETWORK));
        binding.tabRecs.setOnClickListener(v -> switchTab(ActiveCategoryTab.RECOMMENDATIONS));

        switchTab(ActiveCategoryTab.PERMISSIONS);
    }

    private void switchTab(ActiveCategoryTab tab) {
        currentTab = tab;
        int selectedBg = R.drawable.bg_chip_selected;
        int unselectedBg = R.drawable.bg_chip_unselected;
        int selectedText = getResources().getColor(R.color.white);
        int unselectedText = getResources().getColor(R.color.aegis_text_secondary);

        binding.tabPerms.setBackgroundResource(tab == ActiveCategoryTab.PERMISSIONS ? selectedBg : unselectedBg);
        binding.tabPerms.setTextColor(tab == ActiveCategoryTab.PERMISSIONS ? selectedText : unselectedText);

        binding.tabNetwork.setBackgroundResource(tab == ActiveCategoryTab.NETWORK ? selectedBg : unselectedBg);
        binding.tabNetwork.setTextColor(tab == ActiveCategoryTab.NETWORK ? selectedText : unselectedText);

        binding.tabRecs.setBackgroundResource(tab == ActiveCategoryTab.RECOMMENDATIONS ? selectedBg : unselectedBg);
        binding.tabRecs.setTextColor(tab == ActiveCategoryTab.RECOMMENDATIONS ? selectedText : unselectedText);

        refreshCategoryList();
    }

    private void refreshCategoryList() {
        switch (currentTab) {
            case PERMISSIONS:
                binding.rvCategoryItems.setAdapter(permissionAdapter);
                permissionAdapter.updateList(appInfo.getPermissions());
                break;
            case NETWORK:
                binding.rvCategoryItems.setAdapter(domainAdapter);
                domainAdapter.updateList(appInfo.getDomains());
                break;
            case RECOMMENDATIONS:
                binding.rvCategoryItems.setAdapter(recommendationAdapter);
                recommendationAdapter.updateList(appInfo.getRecommendations());
                break;
        }
    }

    @Override
    public void onDataChanged() {
        appInfo = AegisRepository.getInstance().getAppByPackageName(appInfo.getPackageName());
        if (appInfo != null) {
            populateAppDetails();
            binding.evidenceGraphView.setGraphData(appInfo.getEvidenceNodes(), appInfo.getEvidenceEdges());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AegisRepository.getInstance().removeListener(this);
        binding = null;
    }
}
