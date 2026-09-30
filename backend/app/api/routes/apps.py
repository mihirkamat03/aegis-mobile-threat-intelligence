from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import desc, asc

from app.core.database import get_db
from app.models.app import Application
from app.models.permission import Permission
from app.models.domain import Domain
from app.schemas.app import (
    ApplicationListItem,
    ApplicationDetailResponse,
    EvidenceGraphResponse
)
from app.schemas.permission import PermissionResponse
from app.schemas.domain import DomainResponse
from app.schemas.threat import ThreatResponse
from app.schemas.behaviour import BehaviourEventResponse, AppBehaviourReport
from app.schemas.risk import RiskAssessmentResponse, RiskContributor
from app.schemas.finding import CorrelatedFinding, EvidenceItem, RiskHistoryItem
from app.services.risk_engine import risk_engine
from app.services.correlation import correlation_engine
from app.services.normalizer import normalizer

router = APIRouter(prefix="/apps", tags=["Applications"])

@router.get("", response_model=List[ApplicationListItem])
def list_applications(
    search: Optional[str] = Query(None, description="Search query for name or package"),
    severity: Optional[str] = Query(None, description="Filter by severity: SAFE, LOW, SUSPICIOUS, HIGH, CRITICAL"),
    sort_by: str = Query("risk_score", description="Field to sort by: risk_score, name, last_seen"),
    order: str = Query("desc", description="Sort direction: asc or desc"),
    db: Session = Depends(get_db)
):
    query = db.query(Application)

    if search:
        search_pattern = f"%{search.strip().lower()}%"
        query = query.filter(
            (Application.name.ilike(search_pattern)) | 
            (Application.package_name.ilike(search_pattern))
        )

    if severity:
        query = query.filter(Application.severity == severity.upper())

    # Sorting
    sort_column = getattr(Application, sort_by, Application.risk_score)
    if order.lower() == "asc":
        query = query.order_by(asc(sort_column))
    else:
        query = query.order_by(desc(sort_column))

    apps = query.all()
    results: List[ApplicationListItem] = []

    for app in apps:
        primary_threat = app.threats[0].title if app.threats else None
        sens_count = sum(1 for p in app.permissions if p.sensitivity in ["SENSITIVE", "ANOMALY"])
        susp_count = sum(1 for d in app.domains if d.reputation in ["SUSPICIOUS", "C2_INDICATOR", "C2 Host"])
        results.append(ApplicationListItem(
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

    return results

@router.get("/{app_id}", response_model=ApplicationDetailResponse)
def get_application_detail(app_id: str, db: Session = Depends(get_db)):
    app = db.query(Application).filter(Application.id == app_id).first()
    if not app:
        raise HTTPException(status_code=404, detail=f"Application '{app_id}' not found")

    # Permissions
    perm_responses = [
        PermissionResponse(
            id=p.id,
            app_id=p.app_id,
            permission_name=p.permission_name,
            permission_category=p.permission_category,
            sensitivity=p.sensitivity,
            last_used=p.last_used,
            usage_context=p.usage_context
        ) for p in app.permissions
    ]

    # Domains
    dom_responses = [
        DomainResponse(
            id=d.id,
            app_id=d.app_id,
            domain=d.domain,
            reputation=d.reputation,
            confidence=d.confidence,
            threat_status=d.threat_status,
            first_seen=d.first_seen,
            last_seen=d.last_seen
        ) for d in app.domains
    ]

    # Threats
    threat_responses = [
        ThreatResponse(
            id=t.id,
            app_id=t.app_id,
            domain_id=t.domain_id,
            app_name=app.name,
            title=t.title,
            severity=t.severity,
            description=t.description,
            status=t.status,
            detected_at=t.detected_at
        ) for t in app.threats
    ]

    # Behaviour Events
    behav_responses = [
        BehaviourEventResponse(
            id=b.id,
            app_id=b.app_id,
            app_name=app.name,
            event_type=b.event_type,
            description=b.description,
            timestamp=b.timestamp,
            baseline_value=b.baseline_value,
            observed_value=b.observed_value,
            deviation=b.deviation,
            severity=b.severity
        ) for b in app.behaviour_events
    ]

    # Risk Assessment
    latest_risk = None
    if app.risk_assessments:
        last_ra = app.risk_assessments[-1]
        # Re-evaluate contributors dynamically
        calculated = risk_engine.calculate_app_risk(
            app_id=app.id,
            permissions=app.permissions,
            domains=app.domains,
            behaviour_events=app.behaviour_events,
            threats=app.threats
        )
        latest_risk = RiskAssessmentResponse(
            app_id=app.id,
            score=last_ra.total_score,
            severity=last_ra.severity,
            permission_score=last_ra.permission_score,
            network_score=last_ra.network_score,
            threat_intel_score=last_ra.threat_intel_score,
            behaviour_score=last_ra.behaviour_score,
            contributors=calculated.contributors,
            explanation=last_ra.explanation,
            calculated_at=last_ra.created_at
        )
    else:
        latest_risk = risk_engine.calculate_app_risk(
            app_id=app.id,
            permissions=app.permissions,
            domains=app.domains,
            behaviour_events=app.behaviour_events,
            threats=app.threats
        )

    return ApplicationDetailResponse(
        id=app.id,
        name=app.name,
        package_name=app.package_name,
        version=app.version,
        install_source=app.install_source,
        risk_score=app.risk_score,
        severity=app.severity,
        first_seen=app.first_seen,
        last_seen=app.last_seen,
        permissions=perm_responses,
        domains=dom_responses,
        threats=threat_responses,
        behaviour_events=behav_responses,
        latest_risk=latest_risk
    )

@router.get("/{app_id}/permissions", response_model=List[PermissionResponse])
def get_application_permissions(app_id: str, db: Session = Depends(get_db)):
    app = db.query(Application).filter(Application.id == app_id).first()
    if not app:
        raise HTTPException(status_code=404, detail=f"Application '{app_id}' not found")
    return [
        PermissionResponse(
            id=p.id,
            app_id=p.app_id,
            permission_name=p.permission_name,
            permission_category=p.permission_category,
            sensitivity=p.sensitivity,
            last_used=p.last_used,
            usage_context=p.usage_context
        ) for p in app.permissions
    ]

@router.get("/{app_id}/domains", response_model=List[DomainResponse])
def get_application_domains(app_id: str, db: Session = Depends(get_db)):
    app = db.query(Application).filter(Application.id == app_id).first()
    if not app:
        raise HTTPException(status_code=404, detail=f"Application '{app_id}' not found")
    return [
        DomainResponse(
            id=d.id,
            app_id=d.app_id,
            domain=d.domain,
            reputation=d.reputation,
            confidence=d.confidence,
            threat_status=d.threat_status,
            first_seen=d.first_seen,
            last_seen=d.last_seen
        ) for d in app.domains
    ]

@router.get("/{app_id}/risk", response_model=RiskAssessmentResponse)
def get_application_risk(app_id: str, db: Session = Depends(get_db)):
    app = db.query(Application).filter(Application.id == app_id).first()
    if not app:
        raise HTTPException(status_code=404, detail=f"Application '{app_id}' not found")

    return risk_engine.calculate_app_risk(
        app_id=app.id,
        permissions=app.permissions,
        domains=app.domains,
        behaviour_events=app.behaviour_events,
        threats=app.threats
    )

@router.get("/{app_id}/risk-history", response_model=List[RiskHistoryItem])
def get_application_risk_history(app_id: str, db: Session = Depends(get_db)):
    app = db.query(Application).filter(Application.id == app_id).first()
    if not app:
        raise HTTPException(status_code=404, detail=f"Application '{app_id}' not found")

    history: List[RiskHistoryItem] = []
    sorted_ra = sorted(app.risk_assessments, key=lambda r: r.created_at)
    if sorted_ra:
        for idx, ra in enumerate(sorted_ra):
            trigger = "Initial Scan" if idx == 0 else f"Telemetry Update #{idx}"
            history.append(RiskHistoryItem(
                timestamp=ra.created_at,
                score=ra.total_score,
                severity=ra.severity,
                trigger_event=trigger,
                explanation=ra.explanation
            ))
    else:
        history.append(RiskHistoryItem(
            timestamp=app.last_seen,
            score=app.risk_score,
            severity=app.severity,
            trigger_event="Baseline Assessment",
            explanation="Initial baseline posture evaluation."
        ))
    return history

@router.get("/{app_id}/findings", response_model=List[CorrelatedFinding])
def get_application_findings(app_id: str, db: Session = Depends(get_db)):
    app = db.query(Application).filter(Application.id == app_id).first()
    if not app:
        raise HTTPException(status_code=404, detail=f"Application '{app_id}' not found")

    signals = []
    for p in app.permissions:
        signals.append(normalizer.normalize_permission(p, app.id))
    for d in app.domains:
        signals.append(normalizer.normalize_domain(d, app.id))
    for t in app.threats:
        signals.append(normalizer.normalize_threat(t, app.id))
    for b in app.behaviour_events:
        signals.append(normalizer.normalize_behaviour(b, app.id))
    signals.append(normalizer.normalize_app_metadata(app))

    finding = correlation_engine.correlate_signals(app.id, app.name, signals)
    return [finding]

@router.get("/{app_id}/evidence", response_model=List[EvidenceItem])
def get_application_evidence(app_id: str, db: Session = Depends(get_db)):
    app = db.query(Application).filter(Application.id == app_id).first()
    if not app:
        raise HTTPException(status_code=404, detail=f"Application '{app_id}' not found")

    signals = []
    for p in app.permissions:
        signals.append(normalizer.normalize_permission(p, app.id))
    for d in app.domains:
        signals.append(normalizer.normalize_domain(d, app.id))
    for t in app.threats:
        signals.append(normalizer.normalize_threat(t, app.id))
    for b in app.behaviour_events:
        signals.append(normalizer.normalize_behaviour(b, app.id))
    signals.append(normalizer.normalize_app_metadata(app))

    finding = correlation_engine.correlate_signals(app.id, app.name, signals)
    return finding.evidence

@router.get("/{app_id}/behaviour", response_model=AppBehaviourReport)
def get_application_behaviour(app_id: str, db: Session = Depends(get_db)):
    app = db.query(Application).filter(Application.id == app_id).first()
    if not app:
        raise HTTPException(status_code=404, detail=f"Application '{app_id}' not found")

    net_events = [b for b in app.behaviour_events if "NETWORK" in b.event_type or "DESTINATION" in b.event_type]
    base_dest = net_events[0].baseline_value if net_events else 3.0
    obs_dest = net_events[0].observed_value if net_events else float(len(app.domains))
    
    base_events = 2.0
    obs_events = float(len(app.behaviour_events))

    if base_dest > 0:
        dev_percent = round(((obs_dest - base_dest) / base_dest) * 100.0, 1)
    else:
        dev_percent = 0.0

    if dev_percent >= 150.0:
        classification = "ANOMALOUS"
        explanation = f"Significant behavioural anomaly: network destinations spiked by +{dev_percent}% above established baseline."
    elif dev_percent >= 50.0:
        classification = "ELEVATED"
        explanation = f"Elevated drift: observed destinations increased by {dev_percent}%."
    else:
        classification = "NORMAL"
        explanation = "Telemetry operating within normal historical boundaries."

    event_responses = [
        BehaviourEventResponse(
            id=b.id,
            app_id=b.app_id,
            app_name=app.name,
            event_type=b.event_type,
            description=b.description,
            timestamp=b.timestamp,
            baseline_value=b.baseline_value,
            observed_value=b.observed_value,
            deviation=b.deviation,
            severity=b.severity
        ) for b in app.behaviour_events
    ]

    return AppBehaviourReport(
        app_id=app.id,
        baseline_destinations_per_hour=base_dest,
        observed_destinations_per_hour=obs_dest,
        baseline_events_per_hour=base_events,
        observed_events_per_hour=obs_events,
        deviation_percent=dev_percent,
        classification=classification,
        explanation=explanation,
        relevant_events=event_responses
    )

@router.get("/{app_id}/evidence-graph", response_model=EvidenceGraphResponse)
def get_application_evidence_graph(app_id: str, db: Session = Depends(get_db)):
    app = db.query(Application).filter(Application.id == app_id).first()
    if not app:
        raise HTTPException(status_code=404, detail=f"Application '{app_id}' not found")

    return correlation_engine.build_evidence_graph(
        app_id=app.id,
        app_name=app.name,
        permissions=app.permissions,
        domains=app.domains,
        threats=app.threats,
        behaviour_events=app.behaviour_events,
        risk_score=app.risk_score
    )
