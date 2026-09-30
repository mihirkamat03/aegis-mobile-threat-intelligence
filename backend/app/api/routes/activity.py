from typing import List, Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from sqlalchemy import desc

from app.core.database import get_db
from app.models.behaviour import BehaviourEvent
from app.schemas.behaviour import BehaviourEventResponse

router = APIRouter(prefix="/activity", tags=["Activity & Behaviour"])

@router.get("", response_model=List[BehaviourEventResponse])
def list_activity_events(
    app_id: Optional[str] = Query(None, description="Filter by application ID"),
    severity: Optional[str] = Query(None, description="Filter by severity: NORMAL, ELEVATED, ANOMALOUS"),
    limit: int = Query(50, ge=1, le=100, description="Max number of events to return"),
    db: Session = Depends(get_db)
):
    query = db.query(BehaviourEvent)

    if app_id:
        query = query.filter(BehaviourEvent.app_id == app_id)

    if severity:
        query = query.filter(BehaviourEvent.severity == severity.upper())

    events = query.order_by(desc(BehaviourEvent.timestamp)).limit(limit).all()

    return [
        BehaviourEventResponse(
            id=b.id,
            app_id=b.app_id,
            app_name=b.application.name if b.application else "Unknown App",
            event_type=b.event_type,
            description=b.description,
            timestamp=b.timestamp,
            baseline_value=b.baseline_value,
            observed_value=b.observed_value,
            deviation=b.deviation,
            severity=b.severity
        ) for b in events
    ]
