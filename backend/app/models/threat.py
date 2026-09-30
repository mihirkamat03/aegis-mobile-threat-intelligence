from datetime import datetime
from sqlalchemy import Column, String, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from app.core.database import Base

class Threat(Base):
    __tablename__ = "threats"

    id = Column(String(64), primary_key=True, index=True)
    title = Column(String(256), nullable=False)
    severity = Column(String(16), nullable=False, default="HIGH")  # SAFE, LOW, SUSPICIOUS, HIGH, CRITICAL
    app_id = Column(String(64), ForeignKey("applications.id", ondelete="CASCADE"), nullable=False, index=True)
    domain_id = Column(String(64), ForeignKey("domains.id", ondelete="SET NULL"), nullable=True)
    description = Column(String(512), nullable=False)
    status = Column(String(32), nullable=False, default="ACTIVE")  # ACTIVE, RESOLVED, INVESTIGATING
    detected_at = Column(DateTime, default=datetime.utcnow)

    application = relationship("Application", back_populates="threats")
    domain = relationship("Domain", back_populates="threats")
