from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict

class ThreatBase(BaseModel):
    title: str
    severity: str  # SAFE, LOW, SUSPICIOUS, HIGH, CRITICAL
    description: str
    status: str = "ACTIVE"

class ThreatResponse(ThreatBase):
    id: str
    app_id: str
    domain_id: Optional[str] = None
    app_name: Optional[str] = None
    detected_at: datetime

    model_config = ConfigDict(from_attributes=True)
