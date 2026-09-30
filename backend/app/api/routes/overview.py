from typing import List
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import desc

from app.core.database import get_db
from app.models.app import Application
from app.models.threat import Threat
from app.models.behaviour import BehaviourEvent
from app.schemas.app import OverviewResponse, ApplicationListItem
from app.schemas.behaviour import BehaviourEventResponse

router = APIRouter(prefix="/overview", tags=["Overview"])

@router.get("", response_model=OverviewResponse)
def get_device_overview(db: Session = Depends(get_db)):
    apps = db.query(Application).all()
    monitored_apps_count = len(apps)

    active_threats = db.query(Threat).filter(Threat.status == "ACTIVE").all()
    active_threats_count = len(active_threats)

    # Compute overall, threat, and privacy risks
    if apps:
        overall_risk = int(sum(a.risk_score for a in apps) / len(apps))
        # Top risky apps push the score higher
        max_risk = max(a.risk_score for a in apps)
        overall_risk = int((overall_risk + max_risk) / 2)
    else:
        overall_risk = 0

    # Threat risk calculated from active threats severity
    critical_threats = sum(1 for t in active_threats if t.severity == "CRITICAL")
    high_threats = sum(1 for t in active_threats if t.severity == "HIGH")
    threat_risk = min(100, (critical_threats * 35) + (high_threats * 20))

    # Privacy risk calculated from suspicious domains & sensitive permissions
    privacy_risk = min(100, int(overall_risk * 0.85) + (critical_threats * 10))

    if overall_risk >= 70:
        device_status = "CRITICAL"
    elif overall_risk >= 50:
        device_status = "ELEVATED"
    elif overall_risk >= 30:
        device_status = "MONITORING"
    else:
        device_status = "SECURE"

    # Top risky apps sorted by risk_score desc
    sorted_apps = sorted(apps, key=lambda x: x.risk_score, reverse=True)[:5]
    top_risky_apps: List[ApplicationListItem] = []
    for app in sorted_apps:
        primary_threat = app.threats[0].title if app.threats else None
        sens_count = sum(1 for p in app.permissions if p.sensitivity in ["SENSITIVE", "ANOMALY"])
        susp_count = sum(1 for d in app.domains if d.reputation in ["SUSPICIOUS", "C2_INDICATOR", "C2 Host"])
        top_risky_apps.append(ApplicationListItem(
            id=app.id,
            name=app.name,
            package_name=app.package_name,
            version=app.version,
            install_source=app.install_source,
            risk_score=app.risk_score,
            severity=app.severity,
            primary_threat=primary_threat,
            sensitive_permissions_count=sens_count,
            suspicious_domains_count=susp_count,
            last_seen=app.last_seen
        ))

    # Recent activity
    recent_events = (
        db.query(BehaviourEvent)
        .order_by(desc(BehaviourEvent.timestamp))
        .limit(10)
        .all()
    )
    activity_items: List[BehaviourEventResponse] = []
    for ev in recent_events:
        app_name = ev.application.name if ev.application else "Unknown App"
        activity_items.append(BehaviourEventResponse(
            id=ev.id,
            app_id=ev.app_id,
            app_name=app_name,
            event_type=ev.event_type,
            description=ev.description,
            timestamp=ev.timestamp,
            baseline_value=ev.baseline_value,
            observed_value=ev.observed_value,
            deviation=ev.deviation,
            severity=ev.severity
        ))

    return OverviewResponse(
        device_status=device_status,
        overall_risk=overall_risk,
        threat_risk=threat_risk,
        privacy_risk=privacy_risk,
        active_threats_count=active_threats_count,
        monitored_apps_count=monitored_apps_count,
        top_risky_apps=top_risky_apps,
        recent_activity=activity_items
    )
