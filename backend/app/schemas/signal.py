from datetime import datetime, timezone
from typing import Dict, Any, Optional
from enum import Enum
from pydantic import BaseModel, Field, ConfigDict

class SignalType(str, Enum):
    PERMISSION = "PERMISSION"
    DOMAIN = "DOMAIN"
    THREAT_INTELLIGENCE = "THREAT_INTELLIGENCE"
    BEHAVIOUR = "BEHAVIOUR"
    APP_METADATA = "APP_METADATA"

class NormalizedSignal(BaseModel):
    id: str
    type: SignalType
    app_id: str
    timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
    severity: str = "SAFE"  # SAFE, LOW, SUSPICIOUS, HIGH, CRITICAL, or NORMAL, ELEVATED, ANOMALOUS
    confidence: float = Field(default=0.5, ge=0.0, le=1.0)
    description: str
    metadata: Dict[str, Any] = Field(default_factory=dict)

    model_config = ConfigDict(from_attributes=True)
