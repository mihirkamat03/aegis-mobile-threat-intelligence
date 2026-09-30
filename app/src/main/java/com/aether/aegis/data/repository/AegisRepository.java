package com.aether.aegis.data.repository;

import com.aether.aegis.data.api.AegisApiClient;
import com.aether.aegis.data.mock.MockDataProvider;
import com.aether.aegis.data.model.AppInfo;
import com.aether.aegis.data.model.BehaviourEvent;
import com.aether.aegis.data.model.DomainInfo;
import com.aether.aegis.data.model.Severity;
import com.aether.aegis.data.model.Threat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AegisRepository {
    private static AegisRepository instance;

    public interface DataChangeListener {
        void onDataChanged();
    }

    private final List<DataChangeListener> listeners = new ArrayList<>();
    private final List<AppInfo> apps = new ArrayList<>();
    private final List<Threat> threats = new ArrayList<>();
    private final List<BehaviourEvent> timeline = new ArrayList<>();

    private int deviceRiskScore = 34;
    private int threatRiskScore = 20;
    private int privacyRiskScore = 35;
    private int demoStep = 0;
    private boolean isDemoRunning = false;
    private boolean isBackendOnline = false;

    private AegisRepository() {
        resetToDefaultState();
        trySyncWithBackend(null);
    }

    public static synchronized AegisRepository getInstance() {
        if (instance == null) {
            instance = new AegisRepository();
        }
        return instance;
    }

    public void addListener(DataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged();
        }
    }

    public int getDemoStep() {
        return demoStep;
    }

    public String getDemoButtonLabel() {
        switch (demoStep) {
            case 0: return "Start Demo";
            case 1: return "Next (2/5: Drift)";
            case 2: return "Next (3/5: Host)";
            case 3: return "Next (4/5: Intel)";
            case 4: return "Correlate (5/5)";
            default: return "Reset Demo";
        }
    }

    public String getDemoStepDescription() {
        switch (demoStep) {
            case 0: return "Baseline Secure / Normal Monitoring (Risk 28)";
            case 1: return "Step 1/5: Microphone Access in Background (Risk 43)";
            case 2: return "Step 2/5: Destination Spike 3 → 14/hr (Risk 61)";
            case 3: return "Step 3/5: Untrusted Dynamic DNS Contacted (Risk 72)";
            case 4: return "Step 4/5: AbuseIPDB C2 Pulse Corroborated (Risk 84)";
            case 5: return "Step 5/5: Multi-Signal Correlation Complete (Risk 84)";
            default: return "Demo Active";
        }
    }

    public void resetToDefaultState() {
        demoStep = 0;
        isDemoRunning = false;

        apps.clear();
        apps.addAll(MockDataProvider.getInitialApps());

        AppInfo quickPdf = getAppByPackageName("com.quickpdf.reader");
        if (quickPdf != null) {
            quickPdf.setRiskScore(28);
            quickPdf.setSeverity(Severity.LOW);
            quickPdf.setThreatScore(15);
            quickPdf.setPrivacyScore(25);
            quickPdf.setExplanation("Baseline secure: QuickPDF Reader is operating within normal parameters. Storage access is standard for document readers; no anomalous network egress detected.");
        }

        threats.clear();
        threats.addAll(MockDataProvider.getInitialThreats());
        for (Threat t : threats) {
            if ("com.quickpdf.reader".equals(t.getPackageName())) {
                t.setStatus("MONITORED");
            }
        }

        timeline.clear();
        timeline.addAll(MockDataProvider.getInitialTimeline());

        deviceRiskScore = 34;
        threatRiskScore = 20;
        privacyRiskScore = 35;

        notifyListeners();

        if (isBackendOnline) {
            AegisApiClient.getInstance().resetDemo(new AegisApiClient.ApiCallback<org.json.JSONObject>() {
                @Override
                public void onSuccess(org.json.JSONObject result) {}
                @Override
                public void onFailure(Exception error) {}
            });
        }
    }

    public void advanceDemoStep() {
        if (demoStep >= 5) {
            resetToDefaultState();
            return;
        }

        demoStep++;
        isDemoRunning = true;
        AppInfo quickPdf = getAppByPackageName("com.quickpdf.reader");

        switch (demoStep) {
            case 1:
                if (quickPdf != null) {
                    quickPdf.setRiskScore(43);
                    quickPdf.setSeverity(Severity.LOW);
                    quickPdf.setThreatScore(20);
                    quickPdf.setPrivacyScore(45);
                    quickPdf.setExplanation("Step 1: Isolated background capability invocation: RECORD_AUDIO accessed while screen locked. No active network destination yet confirmed.");
                }
                deviceRiskScore = 44;
                timeline.add(0, new BehaviourEvent(
                        "demo-step-1-" + System.currentTimeMillis(),
                        "Just Now",
                        "DEMO (1/5): Background Microphone Access",
                        "RECORD_AUDIO invoked while screen off. Capability opened without foreground UI.",
                        Severity.SUSPICIOUS,
                        "QuickPDF Reader",
                        28,
                        43
                ));
                break;

            case 2:
                if (quickPdf != null) {
                    quickPdf.setRiskScore(61);
                    quickPdf.setSeverity(Severity.SUSPICIOUS);
                    quickPdf.setThreatScore(30);
                    quickPdf.setPrivacyScore(58);
                    quickPdf.setExplanation("Step 2: Behavioral anomaly detected. Network destinations spiked from 3/hr baseline to 14/hr immediately following background audio invocation.");
                }
                deviceRiskScore = 58;
                timeline.add(0, new BehaviourEvent(
                        "demo-step-2-" + System.currentTimeMillis(),
                        "Just Now",
                        "DEMO (2/5): Telemetry Destination Spike",
                        "Traffic destinations spiked to 14/hr (+367% deviation from baseline).",
                        Severity.SUSPICIOUS,
                        "QuickPDF Reader",
                        43,
                        61
                ));
                break;

            case 3:
                if (quickPdf != null) {
                    quickPdf.setRiskScore(72);
                    quickPdf.setSeverity(Severity.HIGH);
                    quickPdf.setThreatScore(60);
                    quickPdf.setPrivacyScore(65);
                    quickPdf.setExplanation("Step 3: Untrusted dynamic DNS host contacted: sync-audio-cdn.hopto.org observed receiving periodic telemetry beacons.");
                }
                deviceRiskScore = 68;
                timeline.add(0, new BehaviourEvent(
                        "demo-step-3-" + System.currentTimeMillis(),
                        "Just Now",
                        "DEMO (3/5): Untrusted Dynamic DNS Contacted",
                        "QuickPDF initiated sockets to dynamic DNS host sync-audio-cdn.hopto.org.",
                        Severity.HIGH,
                        "QuickPDF Reader",
                        61,
                        72
                ));
                break;

            case 4:
                if (quickPdf != null) {
                    quickPdf.setRiskScore(84);
                    quickPdf.setSeverity(Severity.HIGH);
                    quickPdf.setThreatScore(85);
                    quickPdf.setPrivacyScore(76);
                    quickPdf.setExplanation("Step 4: Threat Intelligence corroboration: sync-audio-cdn.hopto.org matched known AbuseIPDB C2 pulse (Confidence: 96%).");
                }
                deviceRiskScore = 80;
                threatRiskScore = 84;
                privacyRiskScore = 68;
                timeline.add(0, new BehaviourEvent(
                        "demo-step-4-" + System.currentTimeMillis(),
                        "Just Now",
                        "DEMO (4/5): AbuseIPDB C2 Pulse Corroborated",
                        "Domain sync-audio-cdn.hopto.org matched active AbuseIPDB C2 indicator pulse (96% confidence).",
                        Severity.CRITICAL,
                        "QuickPDF Reader",
                        72,
                        84
                ));
                break;

            case 5:
                if (quickPdf != null) {
                    quickPdf.setRiskScore(84);
                    quickPdf.setSeverity(Severity.CRITICAL);
                    quickPdf.setThreatScore(94);
                    quickPdf.setPrivacyScore(88);
                    quickPdf.setExplanation("DEMO CORRELATION COMPLETE: High-confidence multi-signal correlation. Background RECORD_AUDIO, 367% network destination spike, untrusted dynamic DNS host, and 96% AbuseIPDB threat pulse confirmed malicious audio exfiltration.");
                }
                deviceRiskScore = 86;
                threatRiskScore = 92;
                privacyRiskScore = 78;
                for (Threat t : threats) {
                    if ("com.quickpdf.reader".equals(t.getPackageName())) {
                        t.setStatus("ACTIVE (CORRELATED)");
                        break;
                    }
                }
                timeline.add(0, new BehaviourEvent(
                        "demo-step-5-" + System.currentTimeMillis(),
                        "Just Now",
                        "DEMO (5/5): Correlated Multi-Signal Spyware",
                        "Multi-signal correlation escalated QuickPDF Reader to CRITICAL. Immediate remediation advised.",
                        Severity.CRITICAL,
                        "QuickPDF Reader",
                        84,
                        84
                ));
                break;
        }

        notifyListeners();

        if (isBackendOnline) {
            AegisApiClient.getInstance().advanceDemo(new AegisApiClient.ApiCallback<org.json.JSONObject>() {
                @Override
                public void onSuccess(org.json.JSONObject result) {}
                @Override
                public void onFailure(Exception error) {}
            });
        }
    }

    public void runDemoAttackScenario() {
        advanceDemoStep();
    }

    public List<AppInfo> getApps() {
        return Collections.unmodifiableList(apps);
    }

    public AppInfo getAppByPackageName(String packageName) {
        for (AppInfo app : apps) {
            if (app.getPackageName().equals(packageName)) {
                return app;
            }
        }
        return null;
    }

    public AppInfo getAppById(String id) {
        for (AppInfo app : apps) {
            if (app.getId().equals(id)) {
                return app;
            }
        }
        return null;
    }

    public List<Threat> getThreats() {
        return Collections.unmodifiableList(threats);
    }

    public List<BehaviourEvent> getTimeline() {
        return Collections.unmodifiableList(timeline);
    }

    public int getDeviceRiskScore() {
        return deviceRiskScore;
    }

    public int getThreatRiskScore() {
        return threatRiskScore;
    }

    public int getPrivacyRiskScore() {
        return privacyRiskScore;
    }

    public boolean isDemoRunning() {
        return isDemoRunning;
    }

    public boolean isBackendOnline() {
        return isBackendOnline;
    }

    public String getBackendStatusLabel() {
        return isBackendOnline ? "LIVE BACKEND" : "DEMO MODE (OFFLINE)";
    }

    public void trySyncWithBackend(final Runnable onComplete) {
        AegisApiClient.getInstance().checkHealth(new AegisApiClient.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isOk) {
                isBackendOnline = Boolean.TRUE.equals(isOk);
                if (isBackendOnline) {
                    AegisApiClient.getInstance().fetchOverview(new AegisApiClient.ApiCallback<AegisApiClient.OverviewData>() {
                        @Override
                        public void onSuccess(AegisApiClient.OverviewData result) {
                            deviceRiskScore = result.overallRisk;
                            threatRiskScore = result.threatRisk;
                            privacyRiskScore = result.privacyRisk;
                            notifyListeners();
                            if (onComplete != null) onComplete.run();
                        }

                        @Override
                        public void onFailure(Exception error) {
                            if (onComplete != null) onComplete.run();
                        }
                    });
                } else {
                    if (onComplete != null) onComplete.run();
                }
            }

            @Override
            public void onFailure(Exception error) {
                isBackendOnline = false;
                if (onComplete != null) onComplete.run();
            }
        });
    }

    public void resolveThreat(String threatId) {
        for (Threat t : threats) {
            if (t.getId().equals(threatId)) {
                t.setStatus("RESOLVED");
                break;
            }
        }
        deviceRiskScore = Math.max(18, deviceRiskScore - 20);
        threatRiskScore = Math.max(15, threatRiskScore - 25);
        notifyListeners();
    }

    public void blockDomain(String domain) {
        for (AppInfo app : apps) {
            for (DomainInfo d : app.getDomains()) {
                if (d.getDomain().equalsIgnoreCase(domain)) {
                    d.setBlocked(true);
                }
            }
            if ("com.quickpdf.reader".equals(app.getPackageName())) {
                app.setRiskScore(48);
                app.setSeverity(Severity.SUSPICIOUS);
                app.setThreatScore(35);
            }
        }

        for (Threat t : threats) {
            if (t.getDescription().contains(domain) || "com.quickpdf.reader".equals(t.getPackageName())) {
                t.setStatus("RESOLVED (BLOCKED)");
            }
        }

        timeline.add(0, new BehaviourEvent(
                "block-" + System.currentTimeMillis(),
                "Just Now",
                "Action Taken: Domain Blocked (Simulated)",
                "Traffic to " + domain + " severed. QuickPDF risk de-escalated to 48.",
                Severity.SAFE,
                "AEGIS Firewall",
                84,
                48
        ));

        deviceRiskScore = Math.max(28, deviceRiskScore - 30);
        threatRiskScore = Math.max(25, threatRiskScore - 35);
        notifyListeners();
    }
}
