from typing import List, Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from sqlalchemy import desc

from app.core.database import get_db
from app.models.threat import Threat
from app.schemas.threat import ThreatResponse

router = APIRouter(prefix="/threats", tags=["Threats"])

@router.get("", response_model=List[ThreatResponse])
def list_threats(
    severity: Optional[str] = Query(None, description="Filter by severity: CRITICAL, HIGH, SUSPICIOUS, LOW"),
    status: Optional[str] = Query(None, description="Filter by status: ACTIVE, RESOLVED, INVESTIGATING"),
    app_id: Optional[str] = Query(None, description="Filter by application ID"),
    db: Session = Depends(get_db)
):
    query = db.query(Threat)

    if severity:
        query = query.filter(Threat.severity == severity.upper())

    if status:
        query = query.filter(Threat.status == status.upper())

    if app_id:
        query = query.filter(Threat.app_id == app_id)

    threats = query.order_by(desc(Threat.detected_at)).all()

    return [
        ThreatResponse(
            id=t.id,
            app_id=t.app_id,
            domain_id=t.domain_id,
            app_name=t.application.name if t.application else "Unknown App",
            title=t.title,
            severity=t.severity,
            description=t.description,
            status=t.status,
            detected_at=t.detected_at
        ) for t in threats
    ]
