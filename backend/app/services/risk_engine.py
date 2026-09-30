from typing import List, Dict, Any, Tuple
from app.schemas.risk import RiskAssessmentResponse, RiskContributor
from app.core.config import settings

def classify_severity(score: int) -> str:
    if score >= 85:
        return "CRITICAL"
    if score >= 70:
        return "HIGH"
    if score >= 50:
        return "SUSPICIOUS"
    if score >= 30:
        return "LOW"
    return "SAFE"

class RiskEngine:
    """
    AEGIS Explainable Risk Engine (Prototype Heuristic Model)
    Calculates deterministic risk scores based on four contributing pillars:
      1. Permission Sensitivity & Anomalous Usage
      2. Network & Host Reputation
      3. Threat Intelligence Matches
      4. Behaviour Baseline Deviation

    Note: This is a prototype heuristic scoring model, not an established standard.
    Weights are loaded dynamically from application configuration.
    """

    @staticmethod
    def calculate_app_risk(
        app_id: str,
        permissions: List[Any],
        domains: List[Any],
        behaviour_events: List[Any],
        threats: List[Any]
    ) -> RiskAssessmentResponse:
        contributors: List[RiskContributor] = []
        perm_score = 0
        net_score = 0
        threat_score = 0
        behav_score = 0

        # 1. Evaluate Permissions
        for perm in permissions:
            sensitivity = getattr(perm, "sensitivity", "STANDARD")
            name = getattr(perm, "permission_name", "UNKNOWN_PERMISSION")
            if sensitivity == "ANOMALY":
                pts = settings.WEIGHT_PERMISSION_ANOMALY
                perm_score += pts
                contributors.append(RiskContributor(
                    type="permission",
                    name=name,
                    impact=pts,
                    reason=f"Anomalous background invocation: {getattr(perm, 'usage_context', 'No context')}"
                ))
            elif sensitivity == "SENSITIVE":
                pts = settings.WEIGHT_PERMISSION_SENSITIVE
                perm_score += pts
                contributors.append(RiskContributor(
                    type="permission",
                    name=name,
                    impact=pts,
                    reason=f"Sensitive device capability requested: {getattr(perm, 'permission_category', 'General')}"
                ))

        # 2. Evaluate Network Destinations
        for domain in domains:
            rep = getattr(domain, "reputation", "CLEAN")
            d_name = getattr(domain, "domain", "unknown.host")
            if rep in ["C2_INDICATOR", "C2 Host"]:
                pts = settings.WEIGHT_NETWORK_C2
                net_score += pts
                contributors.append(RiskContributor(
                    type="network",
                    name=d_name,
                    impact=pts,
                    reason="Matched confirmed Command & Control indicator"
                ))
            elif rep in ["SUSPICIOUS", "Dynamic DNS Host", "Aggressive Tracker"]:
                pts = settings.WEIGHT_NETWORK_SUSPICIOUS
                net_score += pts
                contributors.append(RiskContributor(
                    type="network",
                    name=d_name,
                    impact=pts,
                    reason="Unregistered dynamic DNS or tracking infrastructure"
                ))

        # 3. Evaluate External Threat Intelligence
        for threat in threats:
            status = getattr(threat, "status", "ACTIVE")
            t_sev = getattr(threat, "severity", "HIGH")
            if "RESOLVED" not in status:
                pts = settings.WEIGHT_THREAT_CRITICAL if t_sev in ["CRITICAL", "HIGH"] else settings.WEIGHT_THREAT_ELEVATED
                threat_score += pts
                contributors.append(RiskContributor(
                    type="threat_intel",
                    name=getattr(threat, "title", "Active Threat Match"),
                    impact=pts,
                    reason=getattr(threat, "description", "Threat intelligence correlation triggered")
                ))

        # 4. Evaluate Behavioural Deviation
        for event in behaviour_events:
            sev = getattr(event, "severity", "NORMAL")
            e_type = getattr(event, "event_type", "EVENT")
            dev = getattr(event, "deviation", 0.0)
            if sev == "ANOMALOUS" or dev > 150:
                pts = settings.WEIGHT_BEHAVIOUR_ANOMALY
                behav_score += pts
                contributors.append(RiskContributor(
                    type="behaviour",
                    name=e_type,
                    impact=pts,
                    reason=f"Anomalous temporal deviation: +{int(dev)}% vs historical baseline"
                ))
            elif sev == "ELEVATED" or dev > 50:
                pts = settings.WEIGHT_BEHAVIOUR_ELEVATED
                behav_score += pts
                contributors.append(RiskContributor(
                    type="behaviour",
                    name=e_type,
                    impact=pts,
                    reason=f"Elevated activity deviation: +{int(dev)}%"
                ))

        raw_total = perm_score + net_score + threat_score + behav_score
        clamped_score = max(0, min(100, raw_total))
        severity = classify_severity(clamped_score)

        # Generate Explainable Diagnostic Narrative
        if clamped_score >= 70:
            top_reasons = [c.name for c in contributors[:3]]
            explanation = (
                f"High risk flagged due to multi-signal correlation: "
                f"{', '.join(top_reasons)}. "
                f"Background capability invocation combined with suspicious network destination indicates potential spyware or exfiltration."
            )
        elif clamped_score >= 50:
            explanation = (
                f"Elevated risk observed. "
                f"The application exhibits behavioural drift or tracking characteristics requiring user review."
            )
        elif clamped_score >= 30:
            explanation = "Low risk. Minor background activity detected within tolerable operating parameters."
        else:
            explanation = "Optimal security posture. Permissions and network activity match expected baseline boundaries."

        return RiskAssessmentResponse(
            app_id=app_id,
            score=clamped_score,
            severity=severity,
            permission_score=perm_score,
            network_score=net_score,
            threat_intel_score=threat_score,
            behaviour_score=behav_score,
            contributors=contributors,
            explanation=explanation
        )

risk_engine = RiskEngine()
