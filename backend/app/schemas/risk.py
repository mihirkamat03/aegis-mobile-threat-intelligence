from datetime import datetime, timezone
from typing import List, Optional
from pydantic import BaseModel, Field, ConfigDict

class RiskContributor(BaseModel):
    type: str = Field(..., description="Category: permission, network, threat_intel, behaviour")
    name: str = Field(..., description="Signal identifier or entity name")
    impact: int = Field(..., ge=0, le=100, description="Additive contribution points")
    reason: Optional[str] = None

class RiskAssessmentResponse(BaseModel):
    app_id: str
    score: int = Field(..., ge=0, le=100)
    severity: str = Field(..., description="SAFE, LOW, SUSPICIOUS, HIGH, CRITICAL")
    permission_score: int
    network_score: int
    threat_intel_score: int
    behaviour_score: int
    contributors: List[RiskContributor] = []
    explanation: str
    calculated_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))

    model_config = ConfigDict(from_attributes=True)
