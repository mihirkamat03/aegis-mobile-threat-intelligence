from typing import Dict, Any, List, Optional
from datetime import datetime, timezone, timedelta
from sqlalchemy.orm import Session

from app.models.app import Application
from app.models.permission import Permission
from app.models.domain import Domain
from app.models.threat import Threat
from app.models.behaviour import BehaviourEvent
from app.models.risk import RiskAssessment

class DemoEngine:
    """
    AEGIS Hackathon Demo Engine (Phase 3)
    Orchestrates the deterministic multi-signal attack scenario on QuickPDF Reader:
      Initial (Step 0): Risk = 28 (LOW)
      Step 1: Sensitive microphone invocation -> Risk = 43 (LOW)
      Step 2: Network destination spike (3 -> 14 destinations/hr) -> Risk = 61 (SUSPICIOUS)
      Step 3: Suspicious domain detected (sync-audio-cdn.hopto.org) -> Risk = 72 (HIGH)
      Step 4: Threat Intelligence match (AbuseIPDB C2 pulse) -> Risk = 84 (HIGH)
      Step 5: Full Multi-Signal Correlation completes -> Confirmed Correlated Spyware
    """

    def __init__(self):
        self.current_step = 0
        self.total_steps = 5
        self.timeline_log: List[Dict[str, Any]] = []

    def get_status(self, db: Session) -> Dict[str, Any]:
        app = db.query(Application).filter(Application.id == "app_quickpdf").first()
        current_risk = app.risk_score if app else 28
        current_sev = app.severity if app else "LOW"

        step_descriptions = [
            "Baseline Secure / Low Risk Posture",
            "Step 1: Sensitive Microphone Access in Background",
            "Step 2: Telemetry Destination Spike (3 -> 14/hr)",
            "Step 3: Untrusted Dynamic DNS Destination Contacted",
            "Step 4: External Threat Intelligence Pulse Corroboration",
            "Step 5: Multi-Signal Correlation Synthesis Complete"
        ]

        return {
            "current_step": self.current_step,
            "total_steps": self.total_steps,
            "step_title": step_descriptions[min(self.current_step, 5)],
            "current_risk": current_risk,
            "severity": current_sev,
            "current_finding": self._get_current_finding_title(self.current_step),
            "timeline": self.timeline_log
        }

    def _get_current_finding_title(self, step: int) -> str:
        if step == 0:
            return "Baseline Normal Monitoring"
        elif step == 1:
            return "Isolated Capability Access: RECORD_AUDIO"
        elif step == 2:
            return "Behavioral Anomaly: Destination Spikes"
        elif step == 3:
            return "Untrusted Network Destination Contacted"
        elif step == 4:
            return "External C2 Threat Match Corroborated"
        else:
            return "Correlated Privacy & Network Risk (Confirmed Multi-Signal Spyware)"

    def start_demo(self, db: Session) -> Dict[str, Any]:
        """Resets to Step 0 then advances to Step 1."""
        self.reset_demo(db)
        return self.advance_step(db)

    def advance_step(self, db: Session) -> Dict[str, Any]:
        """Advances demo to the next step (1 through 5)."""
        if self.current_step >= self.total_steps:
            return self.get_status(db)

        self.current_step += 1
        now = datetime.now(timezone.utc).replace(tzinfo=None)
        app = db.query(Application).filter(Application.id == "app_quickpdf").first()
        if not app:
            return self.get_status(db)

        if self.current_step == 1:
            # Step 1: Microphone access -> Risk 43
            app.risk_score = 43
            app.severity = "LOW"
            db.query(RiskAssessment).filter(RiskAssessment.app_id == "app_quickpdf").delete()
            ra = RiskAssessment(
                id=f"ra_demo_step1_{int(now.timestamp())}",
                app_id="app_quickpdf",
                permission_score=25,
                network_score=0,
                threat_intel_score=0,
                behaviour_score=0,
                total_score=43,
                severity="LOW",
                explanation="Isolated background capability invocation: RECORD_AUDIO accessed while screen locked. No active network destination yet confirmed.",
                created_at=now
            )
            db.add(ra)

            # Prepend timeline event
            ev = BehaviourEvent(
                id=f"b_demo_1_{int(now.timestamp())}",
                app_id="app_quickpdf",
                event_type="PERMISSION_USAGE",
                description="DEMO: RECORD_AUDIO invoked without foreground UI activity",
                timestamp=now,
                baseline_value=0.0,
                observed_value=1.0,
                deviation=100.0,
                severity="ELEVATED"
            )
            db.add(ev)
            self.timeline_log.append({
                "step": 1,
                "timestamp": now.isoformat(),
                "title": "Microphone Access Detected",
                "risk": 43,
                "note": "RECORD_AUDIO invoked while screen off"
            })

        elif self.current_step == 2:
            # Step 2: Destination spike (3 -> 14 destinations/hr) -> Risk 61
            app.risk_score = 61
            app.severity = "SUSPICIOUS"
            ra = RiskAssessment(
                id=f"ra_demo_step2_{int(now.timestamp())}",
                app_id="app_quickpdf",
                permission_score=25,
                network_score=0,
                threat_intel_score=0,
                behaviour_score=25,
                total_score=61,
                severity="SUSPICIOUS",
                explanation="Elevated behavioral deviation: Network destinations spiked from 3/hr baseline to 14/hr immediately following background audio invocation.",
                created_at=now
            )
            db.add(ra)

            ev = BehaviourEvent(
                id=f"b_demo_2_{int(now.timestamp())}",
                app_id="app_quickpdf",
                event_type="NETWORK_DESTINATION_CHANGE",
                description="DEMO: Destination spike from 3/hr baseline to 14/hr during standby",
                timestamp=now,
                baseline_value=3.0,
                observed_value=14.0,
                deviation=366.7,
                severity="ANOMALOUS"
            )
            db.add(ev)
            self.timeline_log.append({
                "step": 2,
                "timestamp": now.isoformat(),
                "title": "Behavioral Anomaly: Destination Spike",
                "risk": 61,
                "note": "Traffic destinations spiked to 14/hr (+367% deviation)"
            })

        elif self.current_step == 3:
            # Step 3: Suspicious domain detected -> Risk 72
            app.risk_score = 72
            app.severity = "HIGH"
            ra = RiskAssessment(
                id=f"ra_demo_step3_{int(now.timestamp())}",
                app_id="app_quickpdf",
                permission_score=25,
                network_score=20,
                threat_intel_score=0,
                behaviour_score=25,
                total_score=72,
                severity="HIGH",
                explanation="Suspicious dynamic DNS host contacted: sync-audio-cdn.hopto.org observed receiving periodic telemetry beacons.",
                created_at=now
            )
            db.add(ra)

            self.timeline_log.append({
                "step": 3,
                "timestamp": now.isoformat(),
                "title": "Suspicious Host Contacted",
                "risk": 72,
                "note": "Untrusted dynamic DNS host sync-audio-cdn.hopto.org"
            })

        elif self.current_step == 4:
            # Step 4: Threat Intelligence match -> Risk 84
            app.risk_score = 84
            app.severity = "HIGH"
            ra = RiskAssessment(
                id=f"ra_demo_step4_{int(now.timestamp())}",
                app_id="app_quickpdf",
                permission_score=25,
                network_score=20,
                threat_intel_score=30,
                behaviour_score=25,
                total_score=84,
                severity="HIGH",
                explanation="Threat Intelligence corroboration: sync-audio-cdn.hopto.org matched known AbuseIPDB C2 pulse (Confidence: 96%).",
                created_at=now
            )
            db.add(ra)

            self.timeline_log.append({
                "step": 4,
                "timestamp": now.isoformat(),
                "title": "Threat Intelligence Match",
                "risk": 84,
                "note": "Corroborated AbuseIPDB C2 indicator match"
            })

        elif self.current_step == 5:
            # Step 5: Correlation completes -> Risk 84/HIGH or CRITICAL
            app.risk_score = 84
            app.severity = "HIGH"
            ra = RiskAssessment(
                id=f"ra_demo_step5_{int(now.timestamp())}",
                app_id="app_quickpdf",
                permission_score=25,
                network_score=35,
                threat_intel_score=30,
                behaviour_score=25,
                total_score=84,
                severity="HIGH",
                explanation=(
                    "DEMO CORRELATION COMPLETE: High-confidence multi-signal correlation. "
                    "Background RECORD_AUDIO, 367% network destination spike, untrusted dynamic DNS host, "
                    "and 96% AbuseIPDB threat pulse confirmed malicious audio exfiltration."
                ),
                created_at=now
            )
            db.add(ra)

            self.timeline_log.append({
                "step": 5,
                "timestamp": now.isoformat(),
                "title": "Correlated Multi-Signal Synthesis",
                "risk": 84,
                "note": "Confirmed Correlated Privacy & Network Risk (Spyware)"
            })

        db.commit()
        return self.get_status(db)

    def reset_demo(self, db: Session) -> Dict[str, Any]:
        """Restores QuickPDF Reader to baseline Risk=28 (LOW) and clears demo state."""
        self.current_step = 0
        self.timeline_log.clear()

        app = db.query(Application).filter(Application.id == "app_quickpdf").first()
        if app:
            app.risk_score = 28
            app.severity = "LOW"

            # Remove demo events
            db.query(BehaviourEvent).filter(
                BehaviourEvent.app_id == "app_quickpdf",
                BehaviourEvent.description.ilike("%DEMO%")
            ).delete()

            # Set baseline clean risk assessment
            db.query(RiskAssessment).filter(RiskAssessment.app_id == "app_quickpdf").delete()
            baseline_ra = RiskAssessment(
                id="ra_demo_baseline",
                app_id="app_quickpdf",
                permission_score=15,
                network_score=0,
                threat_intel_score=0,
                behaviour_score=0,
                total_score=28,
                severity="LOW",
                explanation="Initial baseline state. Document reader operating within acceptable baseline limits with standard permissions.",
                created_at=datetime.now(timezone.utc).replace(tzinfo=None)
            )
            db.add(baseline_ra)
            db.commit()

        return self.get_status(db)

demo_engine = DemoEngine()
