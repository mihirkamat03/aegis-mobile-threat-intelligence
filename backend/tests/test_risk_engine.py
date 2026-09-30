import pytest
from app.services.risk_engine import risk_engine, classify_severity

def test_classify_severity_boundaries():
    assert classify_severity(0) == "SAFE"
    assert classify_severity(29) == "SAFE"
    assert classify_severity(30) == "LOW"
    assert classify_severity(49) == "LOW"
    assert classify_severity(50) == "SUSPICIOUS"
    assert classify_severity(69) == "SUSPICIOUS"
    assert classify_severity(70) == "HIGH"
    assert classify_severity(84) == "HIGH"
    assert classify_severity(85) == "CRITICAL"
    assert classify_severity(100) == "CRITICAL"

def test_risk_calculation_empty_app():
    risk = risk_engine.calculate_app_risk(
        app_id="app_safe_test",
        permissions=[],
        domains=[],
        behaviour_events=[],
        threats=[]
    )
    assert risk.score == 0
    assert risk.severity == "SAFE"
    assert risk.permission_score == 0
    assert risk.network_score == 0
    assert risk.threat_intel_score == 0
    assert risk.behaviour_score == 0
    assert len(risk.contributors) == 0
    assert "Optimal security posture" in risk.explanation

def test_risk_calculation_critical_composite():
    class DummyPerm:
        permission_name = "RECORD_AUDIO"
        permission_category = "Microphone"
        sensitivity = "ANOMALY"
        usage_context = "Background while screen locked"

    class DummyDomain:
        domain = "c2.malicious-host.org"
        reputation = "C2_INDICATOR"
        confidence = 0.98

    class DummyThreat:
        title = "Confirmed Audio Spyware"
        severity = "CRITICAL"
        status = "ACTIVE"
        description = "Known spyware trojan pattern"

    class DummyBehaviour:
        event_type = "NETWORK_SPIKE"
        severity = "ANOMALOUS"
        deviation = 450.0

    risk = risk_engine.calculate_app_risk(
        app_id="app_spyware_test",
        permissions=[DummyPerm()],
        domains=[DummyDomain()],
        behaviour_events=[DummyBehaviour()],
        threats=[DummyThreat()]
    )

    assert risk.score >= 85
    assert risk.severity == "CRITICAL"
    assert risk.permission_score == 25
    assert risk.network_score == 35
    assert risk.threat_intel_score == 30
    assert risk.behaviour_score == 25
    assert len(risk.contributors) == 4
    assert risk.score <= 100  # Clamped to max 100
    assert "High risk flagged" in risk.explanation
