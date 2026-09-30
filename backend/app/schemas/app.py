from datetime import datetime
from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field, ConfigDict

from app.schemas.permission import PermissionResponse
from app.schemas.domain import DomainResponse
from app.schemas.threat import ThreatResponse
from app.schemas.behaviour import BehaviourEventResponse
from app.schemas.risk import RiskAssessmentResponse

class ApplicationBase(BaseModel):
    name: str
    package_name: str
    version: str
    install_source: str
    risk_score: int
    severity: str

class ApplicationListItem(ApplicationBase):
    id: str
    primary_threat: Optional[str] = None
    sensitive_permissions_count: int = 0
    suspicious_domains_count: int = 0
    last_seen: datetime

    model_config = ConfigDict(from_attributes=True)

class ApplicationDetailResponse(ApplicationBase):
    id: str
    first_seen: datetime
    last_seen: datetime
    permissions: List[PermissionResponse] = []
    domains: List[DomainResponse] = []
    threats: List[ThreatResponse] = []
    behaviour_events: List[BehaviourEventResponse] = []
    latest_risk: Optional[RiskAssessmentResponse] = None

    model_config = ConfigDict(from_attributes=True)

class EvidenceNodeSchema(BaseModel):
    id: str
    type: str = Field(..., description="APP, PERMISSION, DOMAIN, THREAT_INTEL, RISK")
    label: str
    subtitle: Optional[str] = None
    severity: str = "SAFE"
    metadata: Dict[str, Any] = {}

class EvidenceEdgeSchema(BaseModel):
    source: str
    target: str
    relationship: str
    strength: float = 1.0
    is_threat_path: bool = False

class EvidenceGraphResponse(BaseModel):
    app_id: str
    app_name: str
    nodes: List[EvidenceNodeSchema]
    edges: List[EvidenceEdgeSchema]
    summary: str

class OverviewResponse(BaseModel):
    device_status: str
    overall_risk: int
    threat_risk: int
    privacy_risk: int
    active_threats_count: int
    monitored_apps_count: int
    top_risky_apps: List[ApplicationListItem]
    recent_activity: List[BehaviourEventResponse]
