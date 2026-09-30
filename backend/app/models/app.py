from datetime import datetime
from sqlalchemy import Column, String, Integer, DateTime
from sqlalchemy.orm import relationship
from app.core.database import Base

class Application(Base):
    __tablename__ = "applications"

    id = Column(String(64), primary_key=True, index=True)
    name = Column(String(128), nullable=False, index=True)
    package_name = Column(String(256), unique=True, nullable=False, index=True)
    version = Column(String(32), nullable=False, default="1.0.0")
    install_source = Column(String(64), nullable=False, default="Google Play")
    risk_score = Column(Integer, nullable=False, default=0)
    severity = Column(String(16), nullable=False, default="SAFE")
    first_seen = Column(DateTime, default=datetime.utcnow)
    last_seen = Column(DateTime, default=datetime.utcnow)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    # Relationships
    permissions = relationship("Permission", back_populates="application", cascade="all, delete-orphan")
    domains = relationship("Domain", back_populates="application", cascade="all, delete-orphan")
    threats = relationship("Threat", back_populates="application", cascade="all, delete-orphan")
    behaviour_events = relationship("BehaviourEvent", back_populates="application", cascade="all, delete-orphan")
    risk_assessments = relationship("RiskAssessment", back_populates="application", cascade="all, delete-orphan")
