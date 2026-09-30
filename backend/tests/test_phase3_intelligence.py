import pytest
from datetime import datetime, timezone, timedelta
from fastapi.testclient import TestClient

from app.main import app
from app.core.config import settings
from app.schemas.signal import NormalizedSignal, SignalType
from app.services.normalizer import normalizer
from app.services.correlation import correlation_engine
from app.services.explanation import explainability_engine
from app.services.recommendation import recommendation_engine
from app.services.behaviour_engine import behaviour_engine
from app.services.risk_engine import risk_engine

client = TestClient(app)

def test_permission_scoring():
    class DummyPerm:
        id = "p_mic"
        permission_name = "RECORD_AUDIO"
        permission_category = "Microphone"
        sensitivity = "ANOMALY"
        usage_context = "Background capture"

    sig = normalizer.normalize_permission(DummyPerm(), "app_test")
    assert sig.type == SignalType.PERMISSION
    assert sig.severity == "HIGH"
    assert sig.metadata["category"] == "HIGH"
    assert sig.confidence >= 0.85

def test_negative_sensitive_perm_not_critical():
    """Negative case: A sensitive permission by itself must NOT create a critical finding."""
    class DummyPerm:
        id = "p_mic"
        permission_name = "RECORD_AUDIO"
        permission_category = "Microphone"
        sensitivity = "SENSITIVE"
        usage_context = "Declared permission"

    sig = normalizer.normalize_permission(DummyPerm(), "app_test")
    finding = correlation_engine.correlate_signals("app_test", "Audio App", [sig])
    
    assert finding.severity != "CRITICAL"
    assert finding.severity in ["LOW", "SAFE"]
    assert finding.score <= 30  # Typically 15 points
    assert finding.score < 85

def test_domain_scoring():
    class DummyDomain:
        id = "d_c2"
        domain = "c2.threat-beacon.net"
        reputation = "C2_INDICATOR"
        confidence = 0.94
        threat_status = "Matched AbuseIPDB C2 pulse"

    sig = normalizer.normalize_domain(DummyDomain(), "app_test")
    assert sig.type == SignalType.DOMAIN
    assert sig.severity == "CRITICAL"
    assert sig.confidence == 0.94
    assert sig.metadata["reputation"] == "C2_INDICATOR"

def test_behaviour_deviation_engine():
    res = behaviour_engine.compare_metric("Network Destinations", baseline=3.0, observed=14.0)
    assert res.classification == "ANOMALOUS"
    assert res.deviation_percent == 366.7
    assert "Significant behavioural anomaly" in res.explanation

def test_temporal_correlation_within_window():
    now = datetime.now(timezone.utc)
    s1 = NormalizedSignal(
        id="s1",
        type=SignalType.PERMISSION,
        app_id="app_1",
        timestamp=now - timedelta(minutes=5),
        description="Mic access"
    )
    s2 = NormalizedSignal(
        id="s2",
        type=SignalType.DOMAIN,
        app_id="app_1",
        timestamp=now,
        description="Outbound dynamic DNS"
    )
    assert correlation_engine.evaluate_temporal_correlation([s1, s2], window_minutes=15) is True

def test_temporal_correlation_outside_window():
    now = datetime.now(timezone.utc)
    s1 = NormalizedSignal(
        id="s1",
        type=SignalType.PERMISSION,
        app_id="app_1",
        timestamp=now - timedelta(minutes=45),
        description="Mic access"
    )
    s2 = NormalizedSignal(
        id="s2",
        type=SignalType.DOMAIN,
        app_id="app_1",
        timestamp=now,
        description="Outbound dynamic DNS"
    )
    assert correlation_engine.evaluate_temporal_correlation([s1, s2], window_minutes=15) is False

def test_permission_plus_behaviour_rule_c():
    now = datetime.now(timezone.utc)
    s1 = NormalizedSignal(
        id="s1",
        type=SignalType.PERMISSION,
        app_id="app_c",
        timestamp=now - timedelta(minutes=2),
        severity="HIGH",
        description="Mic access",
        metadata={"permission_name": "RECORD_AUDIO"}
    )
    s2 = NormalizedSignal(
        id="s2",
        type=SignalType.BEHAVIOUR,
        app_id="app_c",
        timestamp=now,
        severity="ANOMALOUS",
        description="Telemetry destination spike",
        metadata={"deviation": 300.0}
    )
    finding = correlation_engine.correlate_signals("app_c", "Rule C App", [s1, s2])
    assert finding.confidence >= 0.70
    assert len(finding.evidence) >= 2

def test_domain_plus_threat_intel_rule_d():
    now = datetime.now(timezone.utc)
    s1 = NormalizedSignal(
        id="s1",
        type=SignalType.DOMAIN,
        app_id="app_d",
        timestamp=now - timedelta(minutes=1),
        severity="CRITICAL",
        description="Connected to bad-host.com",
        metadata={"domain": "bad-host.com", "reputation": "C2_INDICATOR"}
    )
    s2 = NormalizedSignal(
        id="s2",
        type=SignalType.THREAT_INTELLIGENCE,
        app_id="app_d",
        timestamp=now,
        severity="CRITICAL",
        description="Known trojan pulse",
        metadata={"title": "Trojan.Android.C2", "status": "ACTIVE"}
    )
    finding = correlation_engine.correlate_signals("app_d", "Rule D App", [s1, s2])
    assert finding.confidence >= 0.80
    assert any(e.signal_type == "DOMAIN" for e in finding.evidence)
    assert any(e.signal_type == "THREAT_INTELLIGENCE" for e in finding.evidence)

def test_multi_signal_correlation_rule_e():
    now = datetime.now(timezone.utc)
    signals = [
        NormalizedSignal(id="s1", type=SignalType.PERMISSION, app_id="app_e", timestamp=now - timedelta(minutes=4), severity="HIGH", description="RECORD_AUDIO invoked", metadata={"permission_name": "RECORD_AUDIO"}),
        NormalizedSignal(id="s2", type=SignalType.DOMAIN, app_id="app_e", timestamp=now - timedelta(minutes=3), severity="CRITICAL", description="Connected to C2 host", metadata={"domain": "sync.c2.org", "reputation": "C2_INDICATOR"}),
        NormalizedSignal(id="s3", type=SignalType.BEHAVIOUR, app_id="app_e", timestamp=now - timedelta(minutes=2), severity="ANOMALOUS", description="Destination spike", metadata={"deviation": 350.0}),
        NormalizedSignal(id="s4", type=SignalType.THREAT_INTELLIGENCE, app_id="app_e", timestamp=now, severity="CRITICAL", description="AbuseIPDB pulse match", metadata={"title": "Confirmed C2", "status": "ACTIVE"})
    ]
    finding = correlation_engine.correlate_signals("app_e", "MultiSignal App", signals)
    assert finding.score >= 80
    assert finding.severity in ["HIGH", "CRITICAL"]
    assert finding.confidence >= 0.90
    assert finding.evidence_count >= 4
    assert len(finding.evidence) >= 4

def test_explanation_generation():
    signals = [
        NormalizedSignal(id="s1", type=SignalType.PERMISSION, app_id="app_exp", timestamp=datetime.now(timezone.utc), severity="HIGH", description="Mic access", metadata={"permission_name": "RECORD_AUDIO", "usage_context": "Background"}),
        NormalizedSignal(id="s2", type=SignalType.DOMAIN, app_id="app_exp", timestamp=datetime.now(timezone.utc), severity="CRITICAL", description="C2 destination", metadata={"domain": "c2.bad.org", "reputation": "C2_INDICATOR", "threat_status": "AbuseIPDB pulse"})
    ]
    res = explainability_engine.generate_explanation("Test App", signals, score=65, severity="SUSPICIOUS", confidence=0.75, is_temporally_correlated=True)
    assert "Test App" in res["summary"]
    assert len(res["contributing_signals"]) == 2
    assert len(res["evidence"]) == 2
    assert "risk_impact" in res
    assert res["risk_impact"]["total_score"] == 65

def test_recommendations_action_mapping():
    rec_crit = recommendation_engine.get_recommendation(
        severity="CRITICAL",
        has_sensitive_perm=True,
        has_suspicious_domain=True,
        has_behaviour_anomaly=True,
        has_threat_intel=True,
        app_name="Malicious App"
    )
    assert rec_crit["action"] == recommendation_engine.ACTION_CONSIDER_REMOVING_APP
    assert "Consider Uninstalling" in rec_crit["title"]

    rec_low = recommendation_engine.get_recommendation(
        severity="LOW",
        has_sensitive_perm=False,
        has_suspicious_domain=False,
        has_behaviour_anomaly=False,
        has_threat_intel=False,
        app_name="Safe App"
    )
    assert rec_low["action"] == recommendation_engine.ACTION_MONITOR_APPLICATION

def test_evidence_graph_nodes_and_edges():
    class DummyPerm:
        permission_name = "RECORD_AUDIO"
        sensitivity = "ANOMALY"
        usage_context = "Background while locked"
        permission_category = "Microphone"

    class DummyDomain:
        domain = "c2.bad-destination.com"
        reputation = "C2_INDICATOR"
        confidence = 0.95
        threat_status = "Matched C2"

    graph = correlation_engine.build_evidence_graph(
        app_id="app_graph_test",
        app_name="Graph Spyware",
        permissions=[DummyPerm()],
        domains=[DummyDomain()],
        threats=[],
        behaviour_events=[],
        risk_score=85
    )

    types = {n.type for n in graph.nodes}
    assert "APP" in types
    assert "PERMISSION" in types
    assert "DOMAIN" in types
    assert "THREAT_INTELLIGENCE" in types
    assert "FINDING" in types
    assert "RISK" in types

    relationships = {e.relationship for e in graph.edges}
    assert "APP_HAS_PERMISSION" in relationships
    assert "APP_CONTACTED_DOMAIN" in relationships
    assert "DOMAIN_MATCHED_THREAT" in relationships
    assert "SIGNAL_CONTRIBUTES_TO_FINDING" in relationships
    assert "FINDING_HAS_RISK" in relationships
    assert all(hasattr(e, "strength") for e in graph.edges)

def test_risk_history_api():
    r = client.get("/api/v1/apps/app_quickpdf/risk-history")
    assert r.status_code == 200
    data = r.json()
    assert isinstance(data, list)
    assert len(data) >= 1
    assert "score" in data[0]
    assert "severity" in data[0]

def test_findings_and_evidence_api():
    r_find = client.get("/api/v1/apps/app_quickpdf/findings")
    assert r_find.status_code == 200
    findings = r_find.json()
    assert len(findings) >= 1
    assert findings[0]["score"] >= 70
    assert findings[0]["evidence_count"] > 0

    r_ev = client.get("/api/v1/apps/app_quickpdf/evidence")
    assert r_ev.status_code == 200
    evidence = r_ev.json()
    assert len(evidence) >= 1
    assert "contribution" in evidence[0]

def test_behaviour_api_endpoint():
    r = client.get("/api/v1/apps/app_quickpdf/behaviour")
    assert r.status_code == 200
    report = r.json()
    assert "baseline_destinations_per_hour" in report
    assert "observed_destinations_per_hour" in report
    assert "deviation_percent" in report
    assert report["classification"] in ["NORMAL", "ELEVATED", "ANOMALOUS"]
    assert len(report["relevant_events"]) >= 1

def test_demo_scenario_full_lifecycle():
    # 1. Start demo -> Step 1 (Risk 43)
    r1 = client.post("/api/v1/demo/start")
    assert r1.status_code == 200
    assert r1.json()["current_step"] == 1
    assert r1.json()["current_risk"] == 43

    # 2. Step 2 -> Risk 61
    r2 = client.post("/api/v1/demo/step")
    assert r2.status_code == 200
    assert r2.json()["current_step"] == 2
    assert r2.json()["current_risk"] == 61

    # 3. Step 3 -> Risk 72
    r3 = client.post("/api/v1/demo/step")
    assert r3.status_code == 200
    assert r3.json()["current_step"] == 3
    assert r3.json()["current_risk"] == 72

    # 4. Step 4 -> Risk 84
    r4 = client.post("/api/v1/demo/step")
    assert r4.status_code == 200
    assert r4.json()["current_step"] == 4
    assert r4.json()["current_risk"] == 84

    # 5. Step 5 -> Complete Correlation
    r5 = client.post("/api/v1/demo/step")
    assert r5.status_code == 200
    assert r5.json()["current_step"] == 5

    # 6. Check Status
    r_stat = client.get("/api/v1/demo/status")
    assert r_stat.status_code == 200
    assert r_stat.json()["current_step"] == 5

    # 7. Reset -> Baseline Risk 28 (LOW)
    r_reset = client.post("/api/v1/demo/reset")
    assert r_reset.status_code == 200
    assert r_reset.json()["current_step"] == 0
    assert r_reset.json()["current_risk"] == 28
    assert r_reset.json()["severity"] == "LOW"
