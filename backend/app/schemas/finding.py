from datetime import datetime, timezone
from typing import List, Dict, Any, Optional
from pydantic import BaseModel, Field, ConfigDict

class EvidenceItem(BaseModel):
    id: str
    finding_id: str
    signal_type: str  # PERMISSION, DOMAIN, THREAT_INTELLIGENCE, BEHAVIOUR, TEMPORAL
    title: str
    description: str
    contribution: int = Field(..., description="Risk score points contributed")
    timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
    source: str = "AEGIS Telemetry"
    confidence: float = Field(default=0.8, ge=0.0, le=1.0)

    model_config = ConfigDict(from_attributes=True)

class CorrelatedFinding(BaseModel):
    id: str
    app_id: str
    title: str
    severity: str  # LOW, SUSPICIOUS, HIGH, CRITICAL
    score: int = Field(..., ge=0, le=100)
    confidence: float = Field(..., ge=0.0, le=1.0)
    first_detected: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
    last_updated: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
    status: str = "ACTIVE"
    explanation: str
    recommended_action: str
    evidence_count: int
    evidence: List[EvidenceItem] = []

    model_config = ConfigDict(from_attributes=True)

class RiskHistoryItem(BaseModel):
    timestamp: datetime
    score: int
    severity: str
    trigger_event: Optional[str] = None
    explanation: Optional[str] = None

    model_config = ConfigDict(from_attributes=True)
