from typing import List, Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from sqlalchemy import desc

from app.core.database import get_db
from app.models.domain import Domain
from app.schemas.domain import DomainResponse

router = APIRouter(prefix="/domains", tags=["Network & Domains"])

@router.get("", response_model=List[DomainResponse])
def list_domains(
    app_id: Optional[str] = Query(None, description="Filter by application ID"),
    reputation: Optional[str] = Query(None, description="Filter by reputation: CLEAN, SUSPICIOUS, C2_INDICATOR"),
    db: Session = Depends(get_db)
):
    query = db.query(Domain)

    if app_id:
        query = query.filter(Domain.app_id == app_id)

    if reputation:
        query = query.filter(Domain.reputation == reputation)

    domains = query.order_by(desc(Domain.confidence)).all()

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
        ) for d in domains
    ]
