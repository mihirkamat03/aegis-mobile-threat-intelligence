from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict

class DomainBase(BaseModel):
    domain: str
    reputation: str  # CLEAN, SUSPICIOUS, C2_INDICATOR
    confidence: float
    threat_status: Optional[str] = None

class DomainResponse(DomainBase):
    id: str
    app_id: str
    first_seen: datetime
    last_seen: datetime

    model_config = ConfigDict(from_attributes=True)
