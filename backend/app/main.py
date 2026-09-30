from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import settings
from app.core.database import engine, Base, SessionLocal
from app.models import Application
from app.seed.demo_data import seed_database
from app.api import api_router

@asynccontextmanager
async def lifespan(app: FastAPI):
    # 1. Ensure all database tables exist
    Base.metadata.create_all(bind=engine)
    
    # 2. Seed database if empty
    db = SessionLocal()
    try:
        app_count = db.query(Application).count()
        if app_count == 0:
            print("[AEGIS Backend] Seeding database with initial telemetry and threat intelligence...")
            seed_database(db)
            print("[AEGIS Backend] Database seeding complete.")
    finally:
        db.close()

    yield

app = FastAPI(
    title="AEGIS Mobile Threat Intelligence & Privacy Monitoring Backend",
    description="REST API and Correlation Engine for Mobile Threat Intelligence, Privacy Auditing, and Network Behaviour Analysis",
    version="2.0.0",
    lifespan=lifespan
)

# CORS configuration to allow local web dashboard and Android emulator (10.0.2.2) connections
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount API routers
app.include_router(api_router)

@app.get("/health", tags=["Health"])
def health_check():
    return {
        "status": "ok",
        "service": "AEGIS Backend",
        "version": "2.0.0",
        "environment": settings.ENVIRONMENT
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host=settings.API_HOST, port=settings.API_PORT, reload=True)
