from datetime import datetime, timedelta
from typing import List
from sqlalchemy.orm import Session

from app.models.app import Application
from app.models.permission import Permission
from app.models.domain import Domain
from app.models.threat import Threat
from app.models.behaviour import BehaviourEvent
from app.models.risk import RiskAssessment
from app.services.risk_engine import risk_engine

def seed_database(db: Session):
    # Clear existing data to ensure clean idempotency
    db.query(RiskAssessment).delete()
    db.query(BehaviourEvent).delete()
    db.query(Threat).delete()
    db.query(Domain).delete()
    db.query(Permission).delete()
    db.query(Application).delete()
    db.commit()

    from datetime import timezone
    now = datetime.now(timezone.utc).replace(tzinfo=None)

    # Application 1: QuickPDF Reader (CRITICAL - Sideloaded Spyware)
    app1 = Application(
        id="app_quickpdf",
        name="QuickPDF Reader",
        package_name="com.quickpdf.reader",
        version="2.4.1",
        install_source="Sideloaded APK",
        risk_score=84,
        severity="CRITICAL",
        first_seen=now - timedelta(days=2),
        last_seen=now
    )
    db.add(app1)

    p1_1 = Permission(id="p_qp_1", app_id="app_quickpdf", permission_name="RECORD_AUDIO", permission_category="Microphone", sensitivity="ANOMALY", last_used="4m ago", usage_context="Active while screen was OFF without user interaction")
    p1_2 = Permission(id="p_qp_2", app_id="app_quickpdf", permission_name="ACCESS_FINE_LOCATION", permission_category="Location", sensitivity="ANOMALY", last_used="12m ago", usage_context="Background periodic polling detected")
    p1_3 = Permission(id="p_qp_3", app_id="app_quickpdf", permission_name="READ_EXTERNAL_STORAGE", permission_category="Storage", sensitivity="STANDARD", last_used="2m ago", usage_context="Document indexing")
    db.add_all([p1_1, p1_2, p1_3])

    d1_1 = Domain(id="d_qp_1", app_id="app_quickpdf", domain="sync-audio-cdn.hopto.org", reputation="C2_INDICATOR", confidence=0.96, threat_status="Matched AbuseIPDB C2 pulse (Confidence: 96%)", first_seen=now - timedelta(hours=6), last_seen=now)
    d1_2 = Domain(id="d_qp_2", app_id="app_quickpdf", domain="telemetry-collector.dynv6.net", reputation="SUSPICIOUS", confidence=0.88, threat_status="Dynamic DNS host observed", first_seen=now - timedelta(hours=4), last_seen=now)
    d1_3 = Domain(id="d_qp_3", app_id="app_quickpdf", domain="fonts.googleapis.com", reputation="CLEAN", confidence=0.0, threat_status="Verified Google CDN", first_seen=now - timedelta(days=1), last_seen=now)
    db.add_all([d1_1, d1_2, d1_3])

    t1_1 = Threat(id="t_qp_1", app_id="app_quickpdf", domain_id="d_qp_1", title="Unauthorized C2 Audio Exfiltration", severity="CRITICAL", description="Microphone stream correlated with outbound dynamic DNS destination matching known AbuseIPDB C2 pulse.", status="ACTIVE", detected_at=now - timedelta(minutes=15))
    db.add(t1_1)

    b1_1 = BehaviourEvent(id="b_qp_1", app_id="app_quickpdf", event_type="NETWORK_DESTINATION_CHANGE", description="Destinations spiked from 3/hr baseline to 14/hr during device standby", timestamp=now - timedelta(minutes=10), baseline_value=3.0, observed_value=14.0, deviation=366.7, severity="ANOMALOUS")
    b1_2 = BehaviourEvent(id="b_qp_2", app_id="app_quickpdf", event_type="BACKGROUND_ACTIVITY", description="RECORD_AUDIO invoked without foreground UI activity", timestamp=now - timedelta(minutes=15), baseline_value=0.0, observed_value=5.0, deviation=500.0, severity="ANOMALOUS")
    db.add_all([b1_1, b1_2])

    r1 = RiskAssessment(
        id="r_qp_1",
        app_id="app_quickpdf",
        permission_score=25,
        network_score=35,
        threat_intel_score=30,
        behaviour_score=25,
        total_score=84,
        severity="CRITICAL",
        explanation="High risk flagged due to multi-signal correlation: RECORD_AUDIO, sync-audio-cdn.hopto.org, and destination spike. Background invocation combined with untrusted dynamic DNS host indicates targeted spyware."
    )
    db.add(r1)

    # Application 2: FlashClean Pro (HIGH - Aggressive Profiler)
    app2 = Application(
        id="app_flashclean",
        name="FlashClean Pro",
        package_name="com.flashclean.pro",
        version="1.1.0",
        install_source="Google Play",
        risk_score=72,
        severity="HIGH",
        first_seen=now - timedelta(days=5),
        last_seen=now
    )
    db.add(app2)

    p2_1 = Permission(id="p_fc_1", app_id="app_flashclean", permission_name="READ_PHONE_STATE", permission_category="Phone", sensitivity="SENSITIVE", last_used="10m ago", usage_context="Reads IMEI & SIM serial")
    p2_2 = Permission(id="p_fc_2", app_id="app_flashclean", permission_name="ACCESS_COARSE_LOCATION", permission_category="Location", sensitivity="SENSITIVE", last_used="30m ago", usage_context="Periodic background geofence check")
    db.add_all([p2_1, p2_2])

    d2_1 = Domain(id="d_fc_1", app_id="app_flashclean", domain="ad-track-metric.biz", reputation="SUSPICIOUS", confidence=0.72, threat_status="EasyList / Quad9 privacy warning", first_seen=now - timedelta(days=2), last_seen=now)
    db.add(d2_1)

    t2_1 = Threat(id="t_fc_1", app_id="app_flashclean", domain_id="d_fc_1", title="Persistent IMEI Device Fingerprinting", severity="HIGH", description="Hardware identifiers polled 14 times while device was idle and transmitted to commercial tracking network.", status="ACTIVE", detected_at=now - timedelta(minutes=45))
    db.add(t2_1)

    b2_1 = BehaviourEvent(id="b_fc_1", app_id="app_flashclean", event_type="UNUSUAL_FREQUENCY", description="Hardware identifiers queried 14 times per hour", timestamp=now - timedelta(minutes=40), baseline_value=1.0, observed_value=14.0, deviation=1300.0, severity="ANOMALOUS")
    db.add(b2_1)

    r2 = RiskAssessment(
        id="r_fc_1",
        app_id="app_flashclean",
        permission_score=20,
        network_score=20,
        threat_intel_score=15,
        behaviour_score=20,
        total_score=72,
        severity="HIGH",
        explanation="Elevated threat from commercial fingerprinting: READ_PHONE_STATE invoked repeatedly and transmitted to ad-track-metric.biz."
    )
    db.add(r2)

    # Application 3: FastVPN Free (HIGH - Unregistered Proxy)
    app3 = Application(
        id="app_fastvpn",
        name="FastVPN Free",
        package_name="com.freefast.vpn",
        version="3.0.2",
        install_source="Sideloaded APK",
        risk_score=76,
        severity="HIGH",
        first_seen=now - timedelta(days=3),
        last_seen=now
    )
    db.add(app3)
    p3_1 = Permission(id="p_fv_1", app_id="app_fastvpn", permission_name="BIND_VPN_SERVICE", permission_category="Network", sensitivity="SENSITIVE", last_used="1h ago", usage_context="Intercepts device IP packets")
    db.add(p3_1)
    d3_1 = Domain(id="d_fv_1", app_id="app_fastvpn", domain="tunnel-proxy.ru-cloud.net", reputation="SUSPICIOUS", confidence=0.82, threat_status="Unregistered foreign proxy gateway", first_seen=now - timedelta(days=1), last_seen=now)
    db.add(d3_1)
    t3_1 = Threat(id="t_fv_1", app_id="app_fastvpn", domain_id="d_fv_1", title="Unencrypted Gateway Redirection", severity="HIGH", description="Traffic routed through unverified proxy infrastructure.", status="ACTIVE", detected_at=now - timedelta(hours=2))
    db.add(t3_1)
    b3_1 = BehaviourEvent(id="b_fv_1", app_id="app_fastvpn", event_type="NETWORK_DESTINATION_CHANGE", description="Re-routed DNS queries to untrusted external resolver", timestamp=now - timedelta(hours=2), baseline_value=1.0, observed_value=8.0, deviation=700.0, severity="ELEVATED")
    db.add(b3_1)
    r3 = RiskAssessment(id="r_fv_1", app_id="app_fastvpn", permission_score=20, network_score=25, threat_intel_score=20, behaviour_score=15, total_score=76, severity="HIGH", explanation="High risk due to unverified VPN proxy redirection.")
    db.add(r3)

    # Application 4: WallpaperHD Pro (SUSPICIOUS - Excessive Permissions)
    app4 = Application(
        id="app_wallpaper",
        name="WallpaperHD 4K",
        package_name="com.hdwallpapers.live",
        version="2.0.0",
        install_source="Google Play",
        risk_score=58,
        severity="SUSPICIOUS",
        first_seen=now - timedelta(days=10),
        last_seen=now
    )
    db.add(app4)
    p4_1 = Permission(id="p_wp_1", app_id="app_wallpaper", permission_name="READ_CONTACTS", permission_category="Contacts", sensitivity="SENSITIVE", last_used="3h ago", usage_context="Contacts queried by wallpaper tool")
    p4_2 = Permission(id="p_wp_2", app_id="app_wallpaper", permission_name="ACCESS_FINE_LOCATION", permission_category="Location", sensitivity="SENSITIVE", last_used="5h ago", usage_context="Location read for regional wallpapers")
    db.add_all([p4_1, p4_2])
    d4_1 = Domain(id="d_wp_1", app_id="app_wallpaper", domain="content-delivery.wallpaper-serve.com", reputation="CLEAN", confidence=0.0, threat_status="Standard content CDN", first_seen=now - timedelta(days=10), last_seen=now)
    db.add(d4_1)
    b4_1 = BehaviourEvent(id="b_wp_1", app_id="app_wallpaper", event_type="PERMISSION_USAGE", description="Contacts database read during background sync", timestamp=now - timedelta(hours=3), baseline_value=0.0, observed_value=1.0, deviation=100.0, severity="ELEVATED")
    db.add(b4_1)
    r4 = RiskAssessment(id="r_wp_1", app_id="app_wallpaper", permission_score=30, network_score=5, threat_intel_score=0, behaviour_score=15, total_score=58, severity="SUSPICIOUS", explanation="Suspicious permission requests (Contacts & Location) for a wallpaper utility.")
    db.add(r4)

    # Application 5: BatteryBooster (SUSPICIOUS - Background Keepalive)
    app5 = Application(
        id="app_battery",
        name="BatterySaver Pro",
        package_name="com.battery.saverpro",
        version="4.1.2",
        install_source="Google Play",
        risk_score=52,
        severity="SUSPICIOUS",
        first_seen=now - timedelta(days=15),
        last_seen=now
    )
    db.add(app5)
    p5_1 = Permission(id="p_bs_1", app_id="app_battery", permission_name="REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", permission_category="System", sensitivity="SENSITIVE", last_used="1d ago", usage_context="Maintains persistent wake-lock")
    db.add(p5_1)
    d5_1 = Domain(id="d_bs_1", app_id="app_battery", domain="analytics.batteryboost.tech", reputation="CLEAN", confidence=0.0, threat_status="Analytics endpoint", first_seen=now - timedelta(days=15), last_seen=now)
    db.add(d5_1)
    b5_1 = BehaviourEvent(id="b_bs_1", app_id="app_battery", event_type="BACKGROUND_ACTIVITY", description="Constant wake-lock maintained while device was idle", timestamp=now - timedelta(hours=5), baseline_value=1.0, observed_value=6.0, deviation=500.0, severity="ELEVATED")
    db.add(b5_1)
    r5 = RiskAssessment(id="r_bs_1", app_id="app_battery", permission_score=20, network_score=10, threat_intel_score=0, behaviour_score=20, total_score=52, severity="SUSPICIOUS", explanation="Persistent background wake-locks and frequent telemetry pings.")
    db.add(r5)

    # Application 6: WeatherLive (LOW - Minor Baseline Shift)
    app6 = Application(
        id="app_weather",
        name="WeatherLive",
        package_name="com.weather.live",
        version="4.8.2",
        install_source="Google Play",
        risk_score=32,
        severity="LOW",
        first_seen=now - timedelta(days=30),
        last_seen=now
    )
    db.add(app6)
    p6_1 = Permission(id="p_wl_1", app_id="app_weather", permission_name="ACCESS_FINE_LOCATION", permission_category="Location", sensitivity="SENSITIVE", last_used="1h ago", usage_context="Hourly forecast refreshes")
    db.add(p6_1)
    d6_1 = Domain(id="d_wl_1", app_id="app_weather", domain="api.weatherlive-service.com", reputation="CLEAN", confidence=0.0, threat_status="Clean Weather API", first_seen=now - timedelta(days=30), last_seen=now)
    db.add(d6_1)
    b6_1 = BehaviourEvent(id="b_wl_1", app_id="app_weather", event_type="NETWORK_DESTINATION_CHANGE", description="Periodic background poll for weather radar cache", timestamp=now - timedelta(hours=1), baseline_value=2.0, observed_value=3.0, deviation=50.0, severity="NORMAL")
    db.add(b6_1)
    r6 = RiskAssessment(id="r_wl_1", app_id="app_weather", permission_score=15, network_score=0, threat_intel_score=0, behaviour_score=5, total_score=32, severity="LOW", explanation="Operating within expected parameters with routine location lookups for weather forecasts.")
    db.add(r6)

    # Application 7: StepCounter Fit (LOW - Periodic Sync)
    app7 = Application(
        id="app_stepcounter",
        name="StepCounter Fit",
        package_name="com.fitness.stepcounter",
        version="1.5.0",
        install_source="Google Play",
        risk_score=35,
        severity="LOW",
        first_seen=now - timedelta(days=20),
        last_seen=now
    )
    db.add(app7)
    p7_1 = Permission(id="p_sc_1", app_id="app_stepcounter", permission_name="ACTIVITY_RECOGNITION", permission_category="Sensors", sensitivity="STANDARD", last_used="30m ago", usage_context="Pedometer step tracking")
    db.add(p7_1)
    d7_1 = Domain(id="d_sc_1", app_id="app_stepcounter", domain="sync.stepcounterfit.io", reputation="CLEAN", confidence=0.0, threat_status="Verified User Fitness Sync", first_seen=now - timedelta(days=20), last_seen=now)
    db.add(d7_1)
    r7 = RiskAssessment(id="r_sc_1", app_id="app_stepcounter", permission_score=10, network_score=5, threat_intel_score=0, behaviour_score=5, total_score=35, severity="LOW", explanation="Legitimate fitness sensor access with encrypted profile synchronization.")
    db.add(r7)

    # Application 8: Signal Messenger (SAFE - End-to-End Encrypted)
    app8 = Application(
        id="app_signal",
        name="Signal",
        package_name="org.thoughtcrime.securesms",
        version="6.42.3",
        install_source="Google Play",
        risk_score=12,
        severity="SAFE",
        first_seen=now - timedelta(days=60),
        last_seen=now
    )
    db.add(app8)
    p8_1 = Permission(id="p_sg_1", app_id="app_signal", permission_name="CAMERA", permission_category="Camera", sensitivity="SENSITIVE", last_used="3h ago", usage_context="Direct user-initiated video call")
    p8_2 = Permission(id="p_sg_2", app_id="app_signal", permission_name="RECORD_AUDIO", permission_category="Microphone", sensitivity="SENSITIVE", last_used="3h ago", usage_context="Direct user-initiated voice message")
    db.add_all([p8_1, p8_2])
    d8_1 = Domain(id="d_sg_1", app_id="app_signal", domain="chat.signal.org", reputation="CLEAN", confidence=0.0, threat_status="Verified End-to-End Endpoint", first_seen=now - timedelta(days=60), last_seen=now)
    db.add(d8_1)
    r8 = RiskAssessment(id="r_sg_1", app_id="app_signal", permission_score=10, network_score=0, threat_intel_score=0, behaviour_score=0, total_score=12, severity="SAFE", explanation="Robust encryption, zero unverified communication, and permissions are exclusively user-invoked.")
    db.add(r8)

    # Application 9: Spotify (SAFE - Verified Media Player)
    app9 = Application(
        id="app_spotify",
        name="Spotify Music",
        package_name="com.spotify.music",
        version="8.9.12",
        install_source="Google Play",
        risk_score=18,
        severity="SAFE",
        first_seen=now - timedelta(days=90),
        last_seen=now
    )
    db.add(app9)
    d9_1 = Domain(id="d_sp_1", app_id="app_spotify", domain="audio-ak-spotify-com.akamaized.net", reputation="CLEAN", confidence=0.0, threat_status="Verified Akamai Audio CDN", first_seen=now - timedelta(days=90), last_seen=now)
    db.add(d9_1)
    r9 = RiskAssessment(id="r_sp_1", app_id="app_spotify", permission_score=5, network_score=0, threat_intel_score=0, behaviour_score=5, total_score=18, severity="SAFE", explanation="Standard media streaming over authenticated content delivery networks.")
    db.add(r9)

    # Application 10: Simple Calculator (SAFE - Zero Network / Permissions)
    app10 = Application(
        id="app_calculator",
        name="Simple Calculator",
        package_name="com.android.calculator2",
        version="1.0.0",
        install_source="System Preinstalled",
        risk_score=0,
        severity="SAFE",
        first_seen=now - timedelta(days=120),
        last_seen=now
    )
    db.add(app10)
    r10 = RiskAssessment(id="r_calc_1", app_id="app_calculator", permission_score=0, network_score=0, threat_intel_score=0, behaviour_score=0, total_score=0, severity="SAFE", explanation="Zero dangerous permissions declared and zero network sockets opened.")
    db.add(r10)

    db.commit()
    print("Database successfully seeded with 10 structured applications!")
