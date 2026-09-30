from datetime import datetime
from sqlalchemy import Column, String, Float, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from app.core.database import Base

class Domain(Base):
    __tablename__ = "domains"

    id = Column(String(64), primary_key=True, index=True)
    domain = Column(String(256), nullable=False, index=True)
    app_id = Column(String(64), ForeignKey("applications.id", ondelete="CASCADE"), nullable=False, index=True)
    reputation = Column(String(64), nullable=False, default="CLEAN")  # CLEAN, SUSPICIOUS, C2_INDICATOR
    confidence = Column(Float, nullable=False, default=0.0)
    threat_status = Column(String(64), nullable=True)
    first_seen = Column(DateTime, default=datetime.utcnow)
    last_seen = Column(DateTime, default=datetime.utcnow)

    application = relationship("Application", back_populates="domains")
    threats = relationship("Threat", back_populates="domain")
