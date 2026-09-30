import pytest
from app.services.correlation import correlation_engine

def test_correlation_multi_signal():
    class DummyPerm:
        sensitivity = "ANOMALY"

    class DummyDomain:
        reputation = "C2_INDICATOR"

    class DummyBehaviour:
        severity = "ANOMALY"
        deviation = 200.0

    class DummyThreat:
        status = "ACTIVE"

    # All 4 signals
    res = correlation_engine.correlate_app_signals(
        app_id="app_test_1",
        app_name="Test Spyware",
        permissions=[DummyPerm()],
        domains=[DummyDomain()],
        behaviour_events=[DummyBehaviour()],
        threats=[DummyThreat()],
        risk_score=90
    )
    assert res["severity"] == "CRITICAL"
    assert res["evidence_count"] >= 4
    assert res["confidence"] >= 0.90
    assert "Spyware" in res["finding"]

def test_correlation_safe_baseline():
    res = correlation_engine.correlate_app_signals(
        app_id="app_test_safe",
        app_name="Test Safe App",
        permissions=[],
        domains=[],
        behaviour_events=[],
        threats=[],
        risk_score=5
    )
    assert res["severity"] == "SAFE"
    assert res["evidence_count"] == 0
    assert "Baseline" in res["finding"]

def test_evidence_graph_generation():
    class DummyPerm:
        permission_name = "RECORD_AUDIO"
        sensitivity = "ANOMALY"
        usage_context = "Background capture"
        permission_category = "Microphone"

    class DummyDomain:
        domain = "c2.bad-destination.com"
        reputation = "C2_INDICATOR"
        confidence = 0.95

    graph = correlation_engine.build_evidence_graph(
        app_id="app_test_graph",
        app_name="Spyware Test",
        permissions=[DummyPerm()],
        domains=[DummyDomain()],
        threats=[],
        risk_score=88
    )

    node_types = {n.type for n in graph.nodes}
    assert "APP" in node_types
    assert "PERMISSION" in node_types
    assert "DOMAIN" in node_types
    assert "RISK" in node_types
    assert len(graph.edges) >= 3

    # Threat intel node generated for C2 domain
    assert "THREAT_INTELLIGENCE" in node_types or "THREAT_INTEL" in node_types
    threat_path_edges = [e for e in graph.edges if e.is_threat_path]
    assert len(threat_path_edges) >= 2
