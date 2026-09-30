import pytest
from app.services.behaviour_engine import behaviour_engine

def test_behaviour_normal_metric():
    res = behaviour_engine.compare_metric("Background Network Calls", baseline=10.0, observed=12.0)
    assert res.classification == "NORMAL"
    assert res.deviation_percent == 20.0
    assert "within normal baseline limits" in res.explanation

def test_behaviour_elevated_metric():
    res = behaviour_engine.compare_metric("Location Requests", baseline=10.0, observed=16.0)
    assert res.classification == "ELEVATED"
    assert res.deviation_percent == 60.0
    assert "Elevated activity observed" in res.explanation

def test_behaviour_anomalous_metric():
    res = behaviour_engine.compare_metric("Microphone Invocations", baseline=2.0, observed=10.0)
    assert res.classification == "ANOMALOUS"
    assert res.deviation_percent == 400.0
    assert "Significant behavioural anomaly detected" in res.explanation

def test_behaviour_zero_baseline():
    res = behaviour_engine.compare_metric("Camera Invocations", baseline=0.0, observed=5.0)
    assert res.classification == "NORMAL" or res.classification == "ELEVATED"
    assert res.deviation_percent == 100.0
