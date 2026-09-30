from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, ConfigDict

class BehaviourEventResponse(BaseModel):
    id: str
    app_id: str
    app_name: Optional[str] = None
    event_type: str
    description: str
    timestamp: datetime
    baseline_value: float
    observed_value: float
    deviation: float
    severity: str  # NORMAL, ELEVATED, ANOMALOUS

    model_config = ConfigDict(from_attributes=True)

class BehaviourComparison(BaseModel):
    metric_name: str
    baseline: float
    observed: float
    deviation_percent: float
    classification: str  # NORMAL, ELEVATED, ANOMALOUS
    explanation: str

class AppBehaviourReport(BaseModel):
    app_id: str
    baseline_destinations_per_hour: float
    observed_destinations_per_hour: float
    baseline_events_per_hour: float
    observed_events_per_hour: float
    deviation_percent: float
    classification: str  # NORMAL, ELEVATED, ANOMALOUS
    explanation: str
    relevant_events: List[BehaviourEventResponse] = []

    model_config = ConfigDict(from_attributes=True)
