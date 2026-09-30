from typing import List, Dict, Any, Optional
from datetime import datetime, timezone, timedelta
from app.core.config import settings
from app.schemas.signal import NormalizedSignal, SignalType
from app.schemas.finding import CorrelatedFinding, EvidenceItem
from app.schemas.app import EvidenceNodeSchema, EvidenceEdgeSchema, EvidenceGraphResponse
from app.services.explanation import explainability_engine
from app.services.recommendation import recommendation_engine
from app.services.normalizer import normalizer

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

def to_naive_utc(dt: datetime) -> datetime:
    if dt.tzinfo is not None:
        return dt.astimezone(timezone.utc).replace(tzinfo=None)
    return dt

class CorrelationEngine:
    """
    AEGIS Correlation & Evidence Graph Engine (Phase 3 Intelligence Layer)
    Combines independent normalized signals across Permissions, Destination Domains,
    Behavioural Drift, and External Threat Intelligence into an explainable causality web.
    """

    @staticmethod
    def evaluate_temporal_correlation(
        signals: List[NormalizedSignal],
        window_minutes: Optional[int] = None
    ) -> bool:
        if window_minutes is None:
            window_minutes = settings.CORRELATION_WINDOW_MINUTES

        if len(signals) < 2:
            return False

        # Group signals by distinct types
        types_present = {s.type for s in signals}
        if len(types_present) < 2:
            return False

        sorted_signals = sorted(signals, key=lambda s: to_naive_utc(s.timestamp))
        max_delta = timedelta(minutes=window_minutes)

        for i in range(len(sorted_signals)):
            for j in range(i + 1, len(sorted_signals)):
                s1 = sorted_signals[i]
                s2 = sorted_signals[j]
                if s1.type != s2.type:
                    diff = abs(to_naive_utc(s2.timestamp) - to_naive_utc(s1.timestamp))
                    if diff <= max_delta:
                        return True
        return False

    @classmethod
    def correlate_app_signals(
        cls,
        app_id: str,
        app_name: str,
        permissions: List[Any],
        domains: List[Any],
        behaviour_events: List[Any],
        threats: List[Any],
        risk_score: int
    ) -> Dict[str, Any]:
        signals = []
        for p in permissions:
            signals.append(normalizer.normalize_permission(p, app_id))
        for d in domains:
            signals.append(normalizer.normalize_domain(d, app_id))
        for b in behaviour_events:
            signals.append(normalizer.normalize_behaviour(b, app_id))
        for t in threats:
            signals.append(normalizer.normalize_threat(t, app_id))

        finding = cls.correlate_signals(app_id, app_name, signals)
        return {
            "finding": finding.title,
            "confidence": finding.confidence,
            "evidence_count": finding.evidence_count,
            "severity": finding.severity,
            "contributing_signals": [e.title for e in finding.evidence]
        }

    @classmethod
    def correlate_signals(
        cls,
        app_id: str,
        app_name: str,
        signals: List[NormalizedSignal]
    ) -> CorrelatedFinding:
        has_sensitive_perm = any(
            s.type == SignalType.PERMISSION and s.severity in ["HIGH", "SUSPICIOUS", "ANOMALY"]
            for s in signals
        )
        has_suspicious_domain = any(
            s.type == SignalType.DOMAIN and s.severity in ["CRITICAL", "HIGH"]
            for s in signals
        )
        has_behaviour_anomaly = any(
            s.type == SignalType.BEHAVIOUR and (s.severity == "ANOMALOUS" or s.metadata.get("deviation", 0) >= settings.BEHAVIOUR_ANOMALOUS_THRESHOLD_PERCENT)
            for s in signals
        )
        has_threat_intel = any(
            s.type == SignalType.THREAT_INTELLIGENCE and "RESOLVED" not in s.metadata.get("status", "")
            for s in signals
        )

        is_temporally_correlated = cls.evaluate_temporal_correlation(signals)

        evidence_items: List[EvidenceItem] = []
        score = 0
        finding_id = f"finding_{app_id}_{int(datetime.now(timezone.utc).timestamp())}"

        # RULE A: Suspicious Domain
        if has_suspicious_domain:
            dom_sig = next(s for s in signals if s.type == SignalType.DOMAIN and s.severity in ["CRITICAL", "HIGH"])
            pts = settings.WEIGHT_CORRELATION_DOMAIN
            score += pts
            evidence_items.append(EvidenceItem(
                id=f"ev_dom_{app_id}",
                finding_id=finding_id,
                signal_type="DOMAIN",
                title=f"Untrusted Destination: {dom_sig.metadata.get('domain', 'C2 Host')}",
                description=dom_sig.description,
                contribution=pts,
                timestamp=dom_sig.timestamp,
                source="AEGIS Network Sentinel",
                confidence=dom_sig.confidence
            ))

        # RULE B: Behaviour Drift
        if has_behaviour_anomaly:
            behav_sig = next(s for s in signals if s.type == SignalType.BEHAVIOUR and (s.severity == "ANOMALOUS" or s.metadata.get("deviation", 0) >= settings.BEHAVIOUR_ANOMALOUS_THRESHOLD_PERCENT))
            pts = settings.WEIGHT_CORRELATION_BEHAVIOUR
            score += pts
            evidence_items.append(EvidenceItem(
                id=f"ev_behav_{app_id}",
                finding_id=finding_id,
                signal_type="BEHAVIOUR",
                title=f"Telemetry Anomaly: +{int(behav_sig.metadata.get('deviation', 0))}% Deviation",
                description=behav_sig.description,
                contribution=pts,
                timestamp=behav_sig.timestamp,
                source="AEGIS Behaviour Monitor",
                confidence=behav_sig.confidence
            ))

        # Sensitive Permissions (Evidence item, alone never critical)
        if has_sensitive_perm:
            perm_sig = next(s for s in signals if s.type == SignalType.PERMISSION and s.severity in ["HIGH", "SUSPICIOUS", "ANOMALY"])
            pts = settings.WEIGHT_CORRELATION_PERMISSION
            score += pts
            evidence_items.append(EvidenceItem(
                id=f"ev_perm_{app_id}",
                finding_id=finding_id,
                signal_type="PERMISSION",
                title=f"Privileged Access: {perm_sig.metadata.get('permission_name', 'Capability')}",
                description=perm_sig.description,
                contribution=pts,
                timestamp=perm_sig.timestamp,
                source="Android Capability Auditor",
                confidence=perm_sig.confidence
            ))

        # Threat Intelligence match
        if has_threat_intel:
            threat_sig = next(s for s in signals if s.type == SignalType.THREAT_INTELLIGENCE and "RESOLVED" not in s.metadata.get("status", ""))
            pts = settings.WEIGHT_CORRELATION_THREAT_INTEL
            score += pts
            evidence_items.append(EvidenceItem(
                id=f"ev_threat_{app_id}",
                finding_id=finding_id,
                signal_type="THREAT_INTELLIGENCE",
                title=threat_sig.metadata.get("title", "Threat Match"),
                description=threat_sig.description,
                contribution=pts,
                timestamp=threat_sig.timestamp,
                source="OSINT Intelligence Match",
                confidence=threat_sig.confidence
            ))

        # Temporal correlation bonus
        if is_temporally_correlated and len(evidence_items) >= 2:
            pts = settings.WEIGHT_CORRELATION_TEMPORAL_BONUS
            score += pts
            evidence_items.append(EvidenceItem(
                id=f"ev_tempo_{app_id}",
                finding_id=finding_id,
                signal_type="TEMPORAL",
                title="Clustered Execution Sequence",
                description=f"Signals executed within a {settings.CORRELATION_WINDOW_MINUTES}-minute temporal correlation window.",
                contribution=pts,
                timestamp=datetime.now(timezone.utc),
                source="AEGIS Correlation Engine",
                confidence=0.92
            ))

        # Baseline clamp to [0, 100]
        clamped_score = max(0, min(100, score))
        severity = classify_severity(clamped_score)

        # Dynamic Confidence Calculation
        active_signals_count = len(evidence_items)
        if active_signals_count >= 4:
            confidence = 0.94
            title = "Correlated Multi-Signal Spyware & Data Exfiltration"
        elif active_signals_count == 3:
            confidence = 0.86
            title = "Correlated Threat & Privacy Incursion"
        elif active_signals_count == 2:
            confidence = 0.74
            title = "Elevated Behavioral & Network Risk"
        elif active_signals_count == 1:
            confidence = 0.55
            title = "Isolated Security Event"
        else:
            confidence = 0.98
            title = "Baseline System Posture"
            severity = "SAFE"

        # Boost confidence if Rule C or D fired
        # Rule C: Permission + Behaviour
        if has_sensitive_perm and has_behaviour_anomaly and is_temporally_correlated:
            confidence = min(0.99, confidence + 0.05)
        # Rule D: Domain + Threat Intel
        if has_suspicious_domain and has_threat_intel:
            confidence = min(0.99, confidence + 0.08)

        # Generate Explainability & Recommendations
        explanation_result = explainability_engine.generate_explanation(
            app_name=app_name,
            signals=signals,
            score=clamped_score,
            severity=severity,
            confidence=confidence,
            is_temporally_correlated=is_temporally_correlated
        )

        rec = recommendation_engine.get_recommendation(
            severity=severity,
            has_sensitive_perm=has_sensitive_perm,
            has_suspicious_domain=has_suspicious_domain,
            has_behaviour_anomaly=has_behaviour_anomaly,
            has_threat_intel=has_threat_intel,
            app_name=app_name
        )

        now = datetime.now(timezone.utc)
        return CorrelatedFinding(
            id=finding_id,
            app_id=app_id,
            title=title,
            severity=severity,
            score=clamped_score,
            confidence=confidence,
            first_detected=now,
            last_updated=now,
            status="ACTIVE",
            explanation=explanation_result["explanation"],
            recommended_action=f"{rec['title']}: {rec['guidance']}",
            evidence_count=len(evidence_items),
            evidence=evidence_items
        )

    @classmethod
    def build_evidence_graph(
        cls,
        app_id: str,
        app_name: str,
        permissions: List[Any],
        domains: List[Any],
        threats: List[Any],
        behaviour_events: Optional[List[Any]] = None,
        risk_score: int = 50
    ) -> EvidenceGraphResponse:
        nodes: List[EvidenceNodeSchema] = []
        edges: List[EvidenceEdgeSchema] = []
        behaviour_events = behaviour_events or []

        # 1. Root APP Node
        app_severity = classify_severity(risk_score)
        app_node_id = f"app_{app_id}"
        nodes.append(EvidenceNodeSchema(
            id=app_node_id,
            type="APP",
            label=app_name,
            subtitle="Application Root",
            severity=app_severity,
            metadata={"app_id": app_id, "score": risk_score}
        ))

        # 2. PERMISSION Nodes & Edges (APP_HAS_PERMISSION)
        for idx, perm in enumerate(permissions[:4]):
            p_name = getattr(perm, "permission_name", f"PERM_{idx}")
            p_sens = getattr(perm, "sensitivity", "STANDARD")
            node_id = f"perm_{app_id}_{idx}"
            p_sev = "CRITICAL" if p_sens == "ANOMALY" else ("HIGH" if p_sens == "SENSITIVE" else "SAFE")

            nodes.append(EvidenceNodeSchema(
                id=node_id,
                type="PERMISSION",
                label=p_name,
                subtitle=getattr(perm, "usage_context", "Declared Permission"),
                severity=p_sev,
                metadata={"category": getattr(perm, "permission_category", "System")}
            ))
            edges.append(EvidenceEdgeSchema(
                source=app_node_id,
                target=node_id,
                relationship="APP_HAS_PERMISSION",
                strength=0.9 if p_sev in ["CRITICAL", "HIGH"] else 0.5,
                is_threat_path=(p_sev in ["CRITICAL", "HIGH"])
            ))

        # 3. DOMAIN Nodes & Edges (APP_CONTACTED_DOMAIN)
        for idx, dom in enumerate(domains[:4]):
            d_name = getattr(dom, "domain", f"domain_{idx}")
            d_rep = getattr(dom, "reputation", "CLEAN")
            d_conf = getattr(dom, "confidence", 0.0)
            node_id = f"domain_{app_id}_{idx}"
            d_sev = "CRITICAL" if d_rep in ["C2_INDICATOR", "C2 Host"] else ("HIGH" if d_rep == "SUSPICIOUS" else "SAFE")

            nodes.append(EvidenceNodeSchema(
                id=node_id,
                type="DOMAIN",
                label=d_name,
                subtitle=f"Reputation: {d_rep}",
                severity=d_sev,
                metadata={"confidence": d_conf, "threat_status": getattr(dom, "threat_status", "")}
            ))
            edges.append(EvidenceEdgeSchema(
                source=app_node_id,
                target=node_id,
                relationship="APP_CONTACTED_DOMAIN",
                strength=d_conf if d_conf > 0 else 0.6,
                is_threat_path=(d_sev in ["CRITICAL", "HIGH"])
            ))

            # Connect Domain to Threat Intel if malicious (DOMAIN_MATCHED_THREAT)
            if d_sev in ["CRITICAL", "HIGH"]:
                threat_node_id = f"threat_intel_{app_id}_{idx}"
                nodes.append(EvidenceNodeSchema(
                    id=threat_node_id,
                    type="THREAT_INTELLIGENCE",
                    label="AbuseIPDB / OTX Pulse",
                    subtitle="Matched C2 Host Infrastructure",
                    severity=d_sev,
                    metadata={"source": "OSINT Feeds", "confidence": d_conf}
                ))
                edges.append(EvidenceEdgeSchema(
                    source=node_id,
                    target=threat_node_id,
                    relationship="DOMAIN_MATCHED_THREAT",
                    strength=0.95,
                    is_threat_path=True
                ))

        # 4. BEHAVIOUR Nodes & Edges (APP_SHOWED_BEHAVIOUR)
        for idx, b_ev in enumerate(behaviour_events[:3]):
            e_type = getattr(b_ev, "event_type", "BEHAVIOUR")
            dev = getattr(b_ev, "deviation", 0.0)
            b_sev = getattr(b_ev, "severity", "NORMAL")
            b_node_id = f"behaviour_{app_id}_{idx}"

            nodes.append(EvidenceNodeSchema(
                id=b_node_id,
                type="BEHAVIOUR",
                label=e_type,
                subtitle=f"+{int(dev)}% Deviation",
                severity="CRITICAL" if dev >= 200 else ("HIGH" if dev >= 50 else "SAFE"),
                metadata={"deviation": dev, "baseline": getattr(b_ev, "baseline_value", 0.0)}
            ))
            edges.append(EvidenceEdgeSchema(
                source=app_node_id,
                target=b_node_id,
                relationship="APP_SHOWED_BEHAVIOUR",
                strength=0.85 if dev >= 50 else 0.4,
                is_threat_path=(dev >= 50)
            ))

        # 5. FINDING Node (SIGNAL_CONTRIBUTES_TO_FINDING)
        finding_node_id = f"finding_{app_id}"
        nodes.append(EvidenceNodeSchema(
            id=finding_node_id,
            type="FINDING",
            label="Correlated Threat Analysis",
            subtitle=f"{app_severity} Multi-Signal Synthesis",
            severity=app_severity,
            metadata={"confidence": 0.88, "score": risk_score}
        ))

        # Connect evidence nodes (permissions, domains, threat intel, behaviour) to finding
        threat_evidence_nodes = [
            n for n in nodes
            if n.id != app_node_id and n.id != finding_node_id and n.severity in ["CRITICAL", "HIGH"]
        ]
        if threat_evidence_nodes:
            for en in threat_evidence_nodes:
                edges.append(EvidenceEdgeSchema(
                    source=en.id,
                    target=finding_node_id,
                    relationship="SIGNAL_CONTRIBUTES_TO_FINDING",
                    strength=0.9,
                    is_threat_path=True
                ))
        else:
            edges.append(EvidenceEdgeSchema(
                source=app_node_id,
                target=finding_node_id,
                relationship="SIGNAL_CONTRIBUTES_TO_FINDING",
                strength=0.5,
                is_threat_path=False
            ))

        # 6. RISK Node (FINDING_HAS_RISK)
        risk_node_id = f"risk_{app_id}"
        nodes.append(EvidenceNodeSchema(
            id=risk_node_id,
            type="RISK",
            label=f"Risk Score {risk_score}",
            subtitle=f"{app_severity} Security Posture",
            severity=app_severity,
            metadata={"final_score": risk_score}
        ))
        edges.append(EvidenceEdgeSchema(
            source=finding_node_id,
            target=risk_node_id,
            relationship="FINDING_HAS_RISK",
            strength=1.0,
            is_threat_path=(app_severity in ["CRITICAL", "HIGH"])
        ))

        summary = (
            f"Evidence Graph connects {len(nodes)} correlated nodes and {len(edges)} causal relationships "
            f"across capabilities, destinations, threat feeds, and telemetry for {app_name}."
        )

        return EvidenceGraphResponse(
            app_id=app_id,
            app_name=app_name,
            nodes=nodes,
            edges=edges,
            summary=summary
        )

correlation_engine = CorrelationEngine()
