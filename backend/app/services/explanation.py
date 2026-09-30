from typing import List, Dict, Any
from app.schemas.signal import NormalizedSignal, SignalType

class ExplainabilityEngine:
    """
    AEGIS Explainability Engine
    Transforms raw evidence items and correlated signals into human-understandable security findings.
    Provides structured breakdowns: Summary, Contributing Signals, Evidence, Risk Impact, Confidence, and Action.
    """

    @staticmethod
    def generate_explanation(
        app_name: str,
        signals: List[NormalizedSignal],
        score: int,
        severity: str,
        confidence: float,
        is_temporally_correlated: bool = False
    ) -> Dict[str, Any]:
        perm_signals = [s for s in signals if s.type == SignalType.PERMISSION]
        dom_signals = [s for s in signals if s.type == SignalType.DOMAIN]
        threat_signals = [s for s in signals if s.type == SignalType.THREAT_INTELLIGENCE]
        behav_signals = [s for s in signals if s.type == SignalType.BEHAVIOUR]

        # 1. Contributing Signals (names and descriptions)
        contributing_signals = [s.description for s in signals]

        # 2. Concrete Evidence Statements
        evidence_statements = []
        for s in signals:
            if s.type == SignalType.PERMISSION:
                p_name = s.metadata.get("permission_name", "Permission")
                usage = s.metadata.get("usage_context", "Invoked in background")
                evidence_statements.append(f"Permission {p_name} was invoked: {usage}.")
            elif s.type == SignalType.DOMAIN:
                dom = s.metadata.get("domain", "Host")
                rep = s.metadata.get("reputation", "CLEAN")
                status = s.metadata.get("threat_status", "")
                evidence_statements.append(f"Network destination {dom} flagged with reputation '{rep}': {status}.")
            elif s.type == SignalType.THREAT_INTELLIGENCE:
                title = s.metadata.get("title", "Threat Alert")
                evidence_statements.append(f"Threat Intelligence pulse matched: {title} ({s.description}).")
            elif s.type == SignalType.BEHAVIOUR:
                dev = s.metadata.get("deviation", 0.0)
                evidence_statements.append(f"Temporal telemetry recorded a {int(dev)}% deviation from historical baseline.")

        # 3. Dynamic Summary Construction
        if len(signals) == 0:
            summary = f"{app_name} is operating within normal baseline limits with no active risk indicators."
        elif len(signals) >= 3:
            key_elements = []
            if perm_signals:
                key_elements.append(f"sensitive permission ({perm_signals[0].metadata.get('permission_name', 'System')})")
            if dom_signals:
                key_elements.append(f"suspicious destination ({dom_signals[0].metadata.get('domain', 'network')})")
            if behav_signals:
                key_elements.append("unusual behavioral traffic spike")
            if threat_signals:
                key_elements.append("threat intelligence pulse match")

            temporal_suffix = " within a narrow 15-minute execution window" if is_temporally_correlated else ""
            summary = (
                f"Multi-signal correlation detected for {app_name}: combination of {', '.join(key_elements)}"
                f"{temporal_suffix} strongly indicates potential data exfiltration or spyware behavior."
            )
        elif dom_signals and threat_signals:
            dom = dom_signals[0].metadata.get("domain", "target host")
            summary = f"Network telemetry matched active threat intelligence: outbound traffic to {dom} aligns with known C2 infrastructure."
        elif perm_signals and behav_signals:
            perm = perm_signals[0].metadata.get("permission_name", "capability")
            summary = f"Anomalous background capability: {app_name} invoked {perm} accompanied by unexpected telemetry deviation."
        elif dom_signals:
            dom = dom_signals[0].metadata.get("domain", "host")
            summary = f"Untrusted network destination: {app_name} initiated communication with unverified host {dom}."
        elif perm_signals:
            perm = perm_signals[0].metadata.get("permission_name", "permission")
            summary = f"Elevated permission declared: {app_name} holds {perm}, but no malicious telemetry has been observed."
        elif behav_signals:
            summary = f"Telemetry drift: {app_name} showed temporary deviation from baseline usage patterns."
        else:
            summary = f"Security signals observed for {app_name} requiring user awareness."

        # 4. Risk Impact Breakdown
        risk_impact = {
            "permission_contribution": len(perm_signals) * 15,
            "network_contribution": len(dom_signals) * 20,
            "behaviour_contribution": len(behav_signals) * 25,
            "threat_intel_contribution": len(threat_signals) * 30,
            "temporal_bonus": 10 if is_temporally_correlated else 0,
            "total_score": score
        }

        # 5. Narrative Explanation
        explanation_narrative = (
            f"{summary}\n\n"
            f"Observed Evidence:\n" + "\n".join(f"• {e}" for e in evidence_statements)
        )

        return {
            "summary": summary,
            "contributing_signals": contributing_signals,
            "evidence": evidence_statements,
            "risk_impact": risk_impact,
            "confidence": round(confidence, 2),
            "explanation": explanation_narrative
        }

explainability_engine = ExplainabilityEngine()
