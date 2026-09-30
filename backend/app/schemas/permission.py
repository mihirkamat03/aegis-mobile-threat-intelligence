from typing import Optional
from pydantic import BaseModel, ConfigDict

class PermissionBase(BaseModel):
    permission_name: str
    permission_category: str
    sensitivity: str  # STANDARD, SENSITIVE, ANOMALY
    last_used: Optional[str] = None
    usage_context: Optional[str] = None

class PermissionResponse(PermissionBase):
    id: str
    app_id: str

    model_config = ConfigDict(from_attributes=True)
