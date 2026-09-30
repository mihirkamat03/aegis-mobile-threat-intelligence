from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.services.demo_engine import demo_engine

router = APIRouter(prefix="/demo", tags=["Demonstration Engine"])

@router.get("/status")
def get_demo_status(db: Session = Depends(get_db)):
    """Returns the current step, risk score, active findings, and progression timeline."""
    return demo_engine.get_status(db)

@router.post("/start")
def start_demo_scenario(db: Session = Depends(get_db)):
    """Starts the deterministic demonstration scenario from Step 1."""
    return demo_engine.start_demo(db)

@router.post("/step")
def advance_demo_scenario(db: Session = Depends(get_db)):
    """Advances the demonstration scenario by one step."""
    return demo_engine.advance_step(db)

@router.post("/reset")
def reset_demo_scenario(db: Session = Depends(get_db)):
    """Resets QuickPDF Reader back to baseline (Risk 28, LOW) and clears demo artifacts."""
    return demo_engine.reset_demo(db)
