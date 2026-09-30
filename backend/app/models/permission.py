from sqlalchemy import Column, String, Integer, ForeignKey
from sqlalchemy.orm import relationship
from app.core.database import Base

class Permission(Base):
    __tablename__ = "permissions"

    id = Column(String(64), primary_key=True, index=True)
    app_id = Column(String(64), ForeignKey("applications.id", ondelete="CASCADE"), nullable=False, index=True)
    permission_name = Column(String(128), nullable=False)
    permission_category = Column(String(64), nullable=False)
    sensitivity = Column(String(32), nullable=False, default="STANDARD")  # SENSITIVE, STANDARD, ANOMALY
    last_used = Column(String(64), nullable=True)
    usage_context = Column(String(256), nullable=True)

    application = relationship("Application", back_populates="permissions")
