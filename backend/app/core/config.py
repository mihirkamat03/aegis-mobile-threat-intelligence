import os
from typing import Optional
from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    PROJECT_NAME: str = "AEGIS Mobile Threat Intelligence & Privacy API"
    VERSION: str = "1.0.0"
    API_V1_STR: str = "/api/v1"
    DEBUG: bool = True
    ENVIRONMENT: str = "development"
    API_HOST: str = "0.0.0.0"
    API_PORT: int = 8000

    POSTGRES_SERVER: str = os.getenv("POSTGRES_SERVER", "localhost")
    POSTGRES_PORT: int = int(os.getenv("POSTGRES_PORT", "5432"))
    POSTGRES_USER: str = os.getenv("POSTGRES_USER", "postgres")
    POSTGRES_PASSWORD: str = os.getenv("POSTGRES_PASSWORD", "")
    POSTGRES_DB: str = os.getenv("POSTGRES_DB", "aegis_db")

    DATABASE_URL: Optional[str] = None

    # Risk Engine Heuristic Weights (Prototype Model)
    WEIGHT_PERMISSION_ANOMALY: int = 25
    WEIGHT_PERMISSION_SENSITIVE: int = 15
    WEIGHT_NETWORK_C2: int = 35
    WEIGHT_NETWORK_SUSPICIOUS: int = 20
    WEIGHT_THREAT_CRITICAL: int = 30
    WEIGHT_THREAT_ELEVATED: int = 15
    WEIGHT_BEHAVIOUR_ANOMALY: int = 25
    WEIGHT_BEHAVIOUR_ELEVATED: int = 15

    # Phase 3: Multi-Signal Correlation & Temporal Window (Configurable)
    CORRELATION_WINDOW_MINUTES: int = 15
    WEIGHT_CORRELATION_PERMISSION: int = 15
    WEIGHT_CORRELATION_DOMAIN: int = 20
    WEIGHT_CORRELATION_BEHAVIOUR: int = 25
    WEIGHT_CORRELATION_THREAT_INTEL: int = 30
    WEIGHT_CORRELATION_TEMPORAL_BONUS: int = 10
    BEHAVIOUR_ANOMALOUS_THRESHOLD_PERCENT: float = 150.0
    BEHAVIOUR_ELEVATED_THRESHOLD_PERCENT: float = 50.0

    model_config = SettingsConfigDict(env_file=".env", extra="allow")

    def get_database_url(self) -> str:
        if self.DATABASE_URL:
            return self.DATABASE_URL
        if self.POSTGRES_PASSWORD:
            return f"postgresql+psycopg2://{self.POSTGRES_USER}:{self.POSTGRES_PASSWORD}@{self.POSTGRES_SERVER}:{self.POSTGRES_PORT}/{self.POSTGRES_DB}"
        return f"postgresql+psycopg2://{self.POSTGRES_USER}@{self.POSTGRES_SERVER}:{self.POSTGRES_PORT}/{self.POSTGRES_DB}"

settings = Settings()
