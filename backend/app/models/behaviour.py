from datetime import datetime
from sqlalchemy import Column, String, Float, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from app.core.database import Base

class BehaviourEvent(Base):
    __tablename__ = "behaviour_events"

    id = Column(String(64), primary_key=True, index=True)
    app_id = Column(String(64), ForeignKey("applications.id", ondelete="CASCADE"), nullable=False, index=True)
    event_type = Column(String(64), nullable=False)  # NETWORK_DESTINATION_CHANGE, PERMISSION_USAGE, BACKGROUND_ACTIVITY
    description = Column(String(512), nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow)
    baseline_value = Column(Float, nullable=False, default=0.0)
    observed_value = Column(Float, nullable=False, default=0.0)
    deviation = Column(Float, nullable=False, default=0.0)  # Percentage or numeric delta
    severity = Column(String(16), nullable=False, default="NORMAL")  # NORMAL, ELEVATED, ANOMALOUS

    application = relationship("Application", back_populates="behaviour_events")
