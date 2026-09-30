from datetime import datetime
from sqlalchemy import Column, String, Integer, ForeignKey, DateTime, Text
from sqlalchemy.orm import relationship
from app.core.database import Base

class RiskAssessment(Base):
    __tablename__ = "risk_assessments"

    id = Column(String(64), primary_key=True, index=True)
    app_id = Column(String(64), ForeignKey("applications.id", ondelete="CASCADE"), nullable=False, index=True)
    permission_score = Column(Integer, nullable=False, default=0)
    network_score = Column(Integer, nullable=False, default=0)
    threat_intel_score = Column(Integer, nullable=False, default=0)
    behaviour_score = Column(Integer, nullable=False, default=0)
    total_score = Column(Integer, nullable=False, default=0)
    severity = Column(String(16), nullable=False, default="SAFE")
    explanation = Column(Text, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    application = relationship("Application", back_populates="risk_assessments")
