package com.aether.aegis.data.mock;

import com.aether.aegis.data.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockDataProvider {

    public static List<AppInfo> getInitialApps() {
        List<AppInfo> apps = new ArrayList<>();

        // App 1: QuickPDF Reader (Target of the demo attack scenario)
        List<PermissionInfo> pdfPerms = new ArrayList<>();
        pdfPerms.add(new PermissionInfo("RECORD_AUDIO", "Microphone", true, "Active 4m ago while screen was OFF", true, "4m ago"));
        pdfPerms.add(new PermissionInfo("ACCESS_FINE_LOCATION", "Location", true, "Background periodic polling detected", true, "12m ago"));
        pdfPerms.add(new PermissionInfo("READ_EXTERNAL_STORAGE", "Storage", true, "Legitimate document indexing", false, "2m ago"));

        List<DomainInfo> pdfDomains = new ArrayList<>();
        pdfDomains.add(new DomainInfo("sync-audio-cdn.hopto.org", "185.220.101.42", "Known C2 Indicator", "QuickPDF Reader", "2m ago", "AbuseIPDB C2 Confidence 96%", "96%", false));
        pdfDomains.add(new DomainInfo("telemetry-collector.dynv6.net", "194.26.29.112", "Dynamic DNS Host", "QuickPDF Reader", "5m ago", "OTX AlienVault Pulse Match", "88%", false));
        pdfDomains.add(new DomainInfo("fonts.googleapis.com", "142.250.190.42", "Verified CDN", "QuickPDF Reader", "1h ago", "Clean", "0%", false));

        List<BehaviourEvent> pdfEvents = new ArrayList<>();
        pdfEvents.add(new BehaviourEvent("e-101", "10:44 PM", "Risk Score Escalation: 24 → 84", "Correlation engine connected microphone access with verified C2 endpoint.", Severity.CRITICAL, "QuickPDF Reader", 24, 84));
        pdfEvents.add(new BehaviourEvent("e-102", "10:43 PM", "Threat Intelligence Correlation", "sync-audio-cdn.hopto.org matched AbuseIPDB C2 feed (confidence 96%).", Severity.HIGH, "QuickPDF Reader", 0, 0));
        pdfEvents.add(new BehaviourEvent("e-103", "10:43 PM", "Unusual Network Exfiltration", "Transferred 18.2 MB over non-standard port 8443 to untrusted destination.", Severity.HIGH, "QuickPDF Reader", 0, 0));
        pdfEvents.add(new BehaviourEvent("e-104", "10:42 PM", "Background Microphone Invocation", "Microphone sensor opened without active foreground UI activity.", Severity.SUSPICIOUS, "QuickPDF Reader", 0, 0));

        List<Recommendation> pdfRecs = new ArrayList<>();
        pdfRecs.add(new Recommendation("rec-1", "Revoke Microphone Permission", "QuickPDF does not require background microphone access for viewing documents.", "ANDROID_SETTINGS", "package:com.quickpdf.reader", false));
        pdfRecs.add(new Recommendation("rec-2", "Block C2 Destination via Local DNS", "Instantly sever connection to sync-audio-cdn.hopto.org on-device.", "DNS_BLOCK", "sync-audio-cdn.hopto.org", false));
        pdfRecs.add(new Recommendation("rec-3", "Consider Uninstalling Application", "Sideloaded package exhibiting coordinated spyware characteristics.", "QUARANTINE", "com.quickpdf.reader", false));

        List<EvidenceNode> pdfNodes = new ArrayList<>();
        pdfNodes.add(new EvidenceNode("n-app", EvidenceNode.NodeType.APP, "QuickPDF", "v2.4.1 (Sideloaded)", Severity.CRITICAL));
        pdfNodes.add(new EvidenceNode("n-perm", EvidenceNode.NodeType.PERMISSION, "RECORD_AUDIO", "Background Anomaly", Severity.HIGH));
        pdfNodes.add(new EvidenceNode("n-domain", EvidenceNode.NodeType.DOMAIN, "hopto.org C2", "Dynamic DNS Socket", Severity.HIGH));
        pdfNodes.add(new EvidenceNode("n-intel", EvidenceNode.NodeType.THREAT_INTEL, "AbuseIPDB Match", "Confidence 96%", Severity.CRITICAL));
        pdfNodes.add(new EvidenceNode("n-risk", EvidenceNode.NodeType.RISK, "Critical Threat", "Correlated Spyware", Severity.CRITICAL));

        List<EvidenceEdge> pdfEdges = new ArrayList<>();
        pdfEdges.add(new EvidenceEdge("n-app", "n-perm", "Requests", true));
        pdfEdges.add(new EvidenceEdge("n-app", "n-domain", "Opens Socket", true));
        pdfEdges.add(new EvidenceEdge("n-domain", "n-intel", "Resolves To", true));
        pdfEdges.add(new EvidenceEdge("n-intel", "n-risk", "Amplifies", true));
        pdfEdges.add(new EvidenceEdge("n-perm", "n-risk", "Correlates", true));

        AppInfo quickPdf = new AppInfo(
                "app-1",
                "QuickPDF Reader",
                "com.quickpdf.reader",
                "2.4.1",
                "Sideloaded APK",
                28,
                Severity.LOW,
                15,
                25,
                "Baseline secure: QuickPDF Reader is operating within normal parameters. Storage access is standard for document readers; no anomalous network egress detected.",
                "Normal Operation",
                true,
                pdfPerms,
                pdfDomains,
                pdfEvents,
                pdfRecs,
                pdfNodes,
                pdfEdges
        );
        apps.add(quickPdf);

        // App 2: FlashClean Pro (Aggressive Adware & Data Tracker)
        List<PermissionInfo> cleanPerms = new ArrayList<>();
        cleanPerms.add(new PermissionInfo("READ_PHONE_STATE", "Phone", true, "Reads IMEI & Carrier info", true, "10m ago"));
        cleanPerms.add(new PermissionInfo("ACCESS_COARSE_LOCATION", "Location", true, "Monitored geo-fencing", false, "30m ago"));

        List<DomainInfo> cleanDomains = new ArrayList<>();
        cleanDomains.add(new DomainInfo("ad-track-metric.biz", "104.21.44.18", "Aggressive Tracker", "FlashClean Pro", "10m ago", "EasyList Privacy Flag", "72%", false));

        List<BehaviourEvent> cleanEvents = new ArrayList<>();
        cleanEvents.add(new BehaviourEvent("e-201", "10:35 PM", "Device Identifier Fingerprinted", "IMEI and SIM serial read repeatedly.", Severity.SUSPICIOUS, "FlashClean Pro", 0, 0));

        List<Recommendation> cleanRecs = new ArrayList<>();
        cleanRecs.add(new Recommendation("rec-201", "Restrict Phone State Access", "Utility app does not need hardware identifier access.", "ANDROID_SETTINGS", "package:com.flashclean.pro", false));

        AppInfo flashClean = new AppInfo(
                "app-2",
                "FlashClean Pro",
                "com.flashclean.pro",
                "1.1.0",
                "Google Play",
                68,
                Severity.HIGH,
                62,
                74,
                "FlashClean Pro exhibits aggressive fingerprinting behavior by polling hardware identifiers in the background and transmitting device profiles to tracking networks.",
                "Aggressive Fingerprinting",
                false,
                cleanPerms,
                cleanDomains,
                cleanEvents,
                cleanRecs,
                new ArrayList<>(),
                new ArrayList<>()
        );
        apps.add(flashClean);

        // App 3: WeatherLive (Slight Privacy Drift)
        List<PermissionInfo> weatherPerms = new ArrayList<>();
        weatherPerms.add(new PermissionInfo("ACCESS_FINE_LOCATION", "Location", true, "Forecast updates", false, "1h ago"));

        List<DomainInfo> weatherDomains = new ArrayList<>();
        weatherDomains.add(new DomainInfo("api.weatherlive-service.com", "172.67.140.21", "Legitimate Weather API", "WeatherLive", "1h ago", "Clean", "0%", false));

        AppInfo weather = new AppInfo(
                "app-3",
                "WeatherLive",
                "com.weather.live",
                "4.8.2",
                "Google Play",
                32,
                Severity.SAFE,
                15,
                42,
                "WeatherLive functions within expected baseline boundaries with minor background location queries for hourly weather refreshes.",
                "Periodic Background Poll",
                false,
                weatherPerms,
                weatherDomains,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        apps.add(weather);

        // App 4: Signal Messenger (Clean)
        List<PermissionInfo> signalPerms = new ArrayList<>();
        signalPerms.add(new PermissionInfo("CAMERA", "Camera", true, "Video calls (User initiated)", false, "3h ago"));
        signalPerms.add(new PermissionInfo("RECORD_AUDIO", "Microphone", true, "Voice notes (User initiated)", false, "3h ago"));

        List<DomainInfo> signalDomains = new ArrayList<>();
        signalDomains.add(new DomainInfo("chat.signal.org", "13.249.99.12", "Verified End-to-End Endpoint", "Signal", "15m ago", "Clean", "0%", false));

        AppInfo signal = new AppInfo(
                "app-4",
                "Signal",
                "org.thoughtcrime.securesms",
                "6.42.3",
                "Google Play",
                12,
                Severity.SAFE,
                5,
                18,
                "Strong encryption, zero unverified domain communication, and permissions are exclusively invoked during direct user interaction.",
                "Optimal Security Posture",
                false,
                signalPerms,
                signalDomains,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        apps.add(signal);

        // App 5: Spotify
        AppInfo spotify = new AppInfo(
                "app-5",
                "Spotify Music",
                "com.spotify.music",
                "8.9.12",
                "Google Play",
                18,
                Severity.SAFE,
                10,
                24,
                "Compliant media streaming application communicating with authenticated audio delivery networks.",
                "Verified Audio CDN",
                false,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        apps.add(spotify);

        return apps;
    }

    public static List<Threat> getInitialThreats() {
        List<Threat> threats = new ArrayList<>();
        threats.add(new Threat(
                "t-1",
                "Unauthorized C2 Audio Exfiltration",
                Severity.CRITICAL,
                "QuickPDF Reader",
                "com.quickpdf.reader",
                "10:44 PM",
                4,
                "ACTIVE",
                "Microphone stream correlated with outbound dynamic DNS destination matching known AbuseIPDB C2 pulse.",
                "Revoke microphone permission and block sync-audio-cdn.hopto.org"
        ));

        threats.add(new Threat(
                "t-2",
                "Persistent IMEI Device Fingerprinting",
                Severity.HIGH,
                "FlashClean Pro",
                "com.flashclean.pro",
                "10:35 PM",
                2,
                "ACTIVE",
                "Hardware identifiers polled 14 times while device was idle and transmitted to profiling host.",
                "Restrict Phone State permission in Android Settings"
        ));

        threats.add(new Threat(
                "t-3",
                "Unencrypted Background Telemetry",
                Severity.SUSPICIOUS,
                "WeatherLive",
                "com.weather.live",
                "09:12 PM",
                1,
                "RESOLVED",
                "Cleartext HTTP request observed for regional forecast cache.",
                "Update to latest version or enforce HTTPS"
        ));

        return threats;
    }

    public static List<BehaviourEvent> getInitialTimeline() {
        List<BehaviourEvent> events = new ArrayList<>();
        events.add(new BehaviourEvent("ev-1", "10:44 PM", "Risk Score Escalation: 24 → 84", "AEGIS correlation engine connected 4 independent signals into confirmed threat.", Severity.CRITICAL, "QuickPDF Reader", 24, 84));
        events.add(new BehaviourEvent("ev-2", "10:43 PM", "Threat Intelligence Match: AbuseIPDB", "Domain sync-audio-cdn.hopto.org matched C2 confidence 96%.", Severity.HIGH, "QuickPDF Reader", 0, 0));
        events.add(new BehaviourEvent("ev-3", "10:43 PM", "New Network Endpoint Contacted", "QuickPDF opened socket to 185.220.101.42 (Non-standard port 8443).", Severity.SUSPICIOUS, "QuickPDF Reader", 0, 0));
        events.add(new BehaviourEvent("ev-4", "10:42 PM", "Background Sensor Invocation", "RECORD_AUDIO invoked while device display was in standby.", Severity.SUSPICIOUS, "QuickPDF Reader", 0, 0));
        events.add(new BehaviourEvent("ev-5", "10:35 PM", "Device Profiling Beacon", "FlashClean Pro queried IMEI and SIM subscription metadata.", Severity.SUSPICIOUS, "FlashClean Pro", 0, 0));
        events.add(new BehaviourEvent("ev-6", "09:40 PM", "Periodic Baseline Verification", "48 installed packages evaluated against baseline signatures.", Severity.SAFE, "AEGIS Engine", 0, 0));
        return events;
    }
}
