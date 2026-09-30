import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert data["service"] == "AEGIS Backend"
    assert "version" in data

def test_overview_endpoint():
    response = client.get("/api/v1/overview")
    assert response.status_code == 200
    data = response.json()
    assert "device_status" in data
    assert "overall_risk" in data
    assert "threat_risk" in data
    assert "privacy_risk" in data
    assert "monitored_apps_count" in data
    assert data["monitored_apps_count"] > 0
    assert len(data["top_risky_apps"]) > 0

def test_apps_list_endpoint():
    response = client.get("/api/v1/apps")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) >= 5

def test_apps_list_filter_search():
    response = client.get("/api/v1/apps?search=quickpdf")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 1
    assert "QuickPDF" in data[0]["name"]

def test_apps_list_filter_severity():
    response = client.get("/api/v1/apps?severity=CRITICAL")
    assert response.status_code == 200
    data = response.json()
    assert len(data) >= 1
    assert all(a["severity"] == "CRITICAL" for a in data)

def test_app_detail_found():
    response = client.get("/api/v1/apps/app_quickpdf")
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == "app_quickpdf"
    assert data["name"] == "QuickPDF Reader"
    assert len(data["permissions"]) > 0
    assert len(data["domains"]) > 0
    assert len(data["threats"]) > 0
    assert data["latest_risk"] is not None

def test_app_detail_not_found():
    response = client.get("/api/v1/apps/non_existent_id_xyz")
    assert response.status_code == 404
    assert "not found" in response.json()["detail"].lower()

def test_app_permissions():
    response = client.get("/api/v1/apps/app_quickpdf/permissions")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) >= 1
    assert any(p["permission_name"] == "RECORD_AUDIO" for p in data)

def test_app_domains():
    response = client.get("/api/v1/apps/app_quickpdf/domains")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) >= 1
    assert any("hopto.org" in d["domain"] for d in data)

def test_app_risk_assessment():
    response = client.get("/api/v1/apps/app_quickpdf/risk")
    assert response.status_code == 200
    data = response.json()
    assert data["app_id"] == "app_quickpdf"
    assert data["score"] >= 70
    assert len(data["contributors"]) > 0
    assert len(data["explanation"]) > 0

def test_app_behaviour():
    response = client.get("/api/v1/apps/app_quickpdf/behaviour")
    assert response.status_code == 200
    data = response.json()
    assert "baseline_destinations_per_hour" in data
    assert "observed_destinations_per_hour" in data
    assert "classification" in data
    assert len(data["relevant_events"]) >= 1

def test_app_evidence_graph():
    response = client.get("/api/v1/apps/app_quickpdf/evidence-graph")
    assert response.status_code == 200
    data = response.json()
    assert data["app_id"] == "app_quickpdf"
    assert len(data["nodes"]) >= 3
    assert len(data["edges"]) >= 2
    assert "summary" in data

def test_threats_endpoint():
    response = client.get("/api/v1/threats")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) > 0

def test_threats_severity_filter():
    response = client.get("/api/v1/threats?severity=CRITICAL")
    assert response.status_code == 200
    data = response.json()
    assert all(t["severity"] == "CRITICAL" for t in data)

def test_activity_endpoint():
    response = client.get("/api/v1/activity")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) > 0

def test_domains_endpoint():
    response = client.get("/api/v1/domains")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) > 0
