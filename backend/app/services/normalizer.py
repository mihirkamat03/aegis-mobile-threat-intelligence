from typing import List, Any, Optional
from datetime import datetime, timezone
from app.schemas.signal import NormalizedSignal, SignalType

def to_naive_utc(dt: Optional[datetime]) -> datetime:
    if dt is None:
        return datetime.now(timezone.utc).replace(tzinfo=None)
    if dt.tzinfo is not None:
        return dt.astimezone(timezone.utc).replace(tzinfo=None)
    return dt

HIGH_SENSITIVITY_PERMISSIONS = {
    "RECORD_AUDIO", "CAMERA", "ACCESS_FINE_LOCATION", "ACCESS_BACKGROUND_LOCATION",
    "READ_CONTACTS", "READ_PHONE_STATE", "READ_SMS", "RECEIVE_SMS", "WRITE_EXTERNAL_STORAGE"
}

MEDIUM_SENSITIVITY_PERMISSIONS = {
    "ACCESS_COARSE_LOCATION", "READ_EXTERNAL_STORAGE", "GET_ACCOUNTS", "USE_BIOMETRIC"
}

class SignalNormalizer:
    """
    Normalizes raw entities (database models or incoming telemetry) into the Unified Signal Model.
    """

    @staticmethod
    def normalize_permission(perm: Any, app_id: str) -> NormalizedSignal:
        p_name = getattr(perm, "permission_name", "UNKNOWN_PERMISSION")
        p_sens = getattr(perm, "sensitivity", "STANDARD")
        usage_ctx = getattr(perm, "usage_context", "Declared permission")
        
        # Categorize permission sensitivity
        if p_name in HIGH_SENSITIVITY_PERMISSIONS or p_sens == "ANOMALY":
            category = "HIGH"
            severity = "HIGH" if p_sens == "ANOMALY" else "SUSPICIOUS"
            confidence = 0.90 if p_sens == "ANOMALY" else 0.75
        elif p_name in MEDIUM_SENSITIVITY_PERMISSIONS or p_sens == "SENSITIVE":
            category = "MEDIUM"
            severity = "LOW"
            confidence = 0.65
        else:
            category = "LOW"
            severity = "SAFE"
            confidence = 0.50

        # Preserve or generate timestamp
        ts = to_naive_utc(getattr(perm, "timestamp", None))

        return NormalizedSignal(
            id=getattr(perm, "id", f"sig_perm_{p_name}"),
            type=SignalType.PERMISSION,
            app_id=app_id,
            timestamp=ts,
            severity=severity,
            confidence=confidence,
            description=f"Permission {p_name} ({category} sensitivity): {usage_ctx}",
            metadata={
                "permission_name": p_name,
                "category": category,
                "declared_category": getattr(perm, "permission_category", "System"),
                "usage_context": usage_ctx,
                "sensitivity": p_sens
            }
        )

    @staticmethod
    def normalize_domain(domain: Any, app_id: str) -> NormalizedSignal:
        d_name = getattr(domain, "domain", "unknown.host")
        rep = getattr(domain, "reputation", "CLEAN")
        conf = getattr(domain, "confidence", 0.5)
        threat_status = getattr(domain, "threat_status", "Observed network destination")

        if rep in ["C2_INDICATOR", "C2 Host"]:
            severity = "CRITICAL"
        elif rep in ["SUSPICIOUS", "Dynamic DNS Host", "Aggressive Tracker"]:
            severity = "HIGH"
        else:
            severity = "SAFE"

        ts = to_naive_utc(getattr(domain, "last_seen", None))

        return NormalizedSignal(
            id=getattr(domain, "id", f"sig_dom_{d_name}"),
            type=SignalType.DOMAIN,
            app_id=app_id,
            timestamp=ts,
            severity=severity,
            confidence=conf if conf > 0 else 0.7,
            description=f"Outbound host {d_name} ({rep}): {threat_status}",
            metadata={
                "domain": d_name,
                "reputation": rep,
                "threat_status": threat_status,
                "novelty": "RECENT" if "dyn" in d_name or "hopto" in d_name else "ESTABLISHED"
            }
        )

    @staticmethod
    def normalize_threat(threat: Any, app_id: str) -> NormalizedSignal:
        title = getattr(threat, "title", "Active Threat Alert")
        t_sev = getattr(threat, "severity", "HIGH")
        desc = getattr(threat, "description", "")
        ts = to_naive_utc(getattr(threat, "detected_at", None))

        return NormalizedSignal(
            id=getattr(threat, "id", f"sig_threat_{app_id}"),
            type=SignalType.THREAT_INTELLIGENCE,
            app_id=app_id,
            timestamp=ts,
            severity=t_sev,
            confidence=0.92 if t_sev in ["CRITICAL", "HIGH"] else 0.75,
            description=f"Threat Intel match '{title}': {desc}",
            metadata={
                "title": title,
                "status": getattr(threat, "status", "ACTIVE"),
                "domain_id": getattr(threat, "domain_id", None)
            }
        )

    @staticmethod
    def normalize_behaviour(event: Any, app_id: str) -> NormalizedSignal:
        e_type = getattr(event, "event_type", "TELEMETRY_EVENT")
        desc = getattr(event, "description", "")
        dev = getattr(event, "deviation", 0.0)
        sev = getattr(event, "severity", "NORMAL")
        ts = to_naive_utc(getattr(event, "timestamp", None))

        return NormalizedSignal(
            id=getattr(event, "id", f"sig_behav_{app_id}"),
            type=SignalType.BEHAVIOUR,
            app_id=app_id,
            timestamp=ts,
            severity=sev,
            confidence=0.88 if sev == "ANOMALOUS" else 0.70,
            description=f"Behavioural drift {e_type}: {desc} (+{int(dev)}% deviation)",
            metadata={
                "event_type": e_type,
                "baseline_value": getattr(event, "baseline_value", 0.0),
                "observed_value": getattr(event, "observed_value", 0.0),
                "deviation": dev
            }
        )

    @staticmethod
    def normalize_app_metadata(app: Any) -> NormalizedSignal:
        app_id = getattr(app, "id", "unknown_app")
        install_source = getattr(app, "install_source", "Google Play")
        is_sideloaded = "sideload" in install_source.lower() or "apk" in install_source.lower()
        
        return NormalizedSignal(
            id=f"sig_meta_{app_id}",
            type=SignalType.APP_METADATA,
            app_id=app_id,
            timestamp=to_naive_utc(getattr(app, "first_seen", None)),
            severity="SUSPICIOUS" if is_sideloaded else "SAFE",
            confidence=0.85,
            description=f"Application packaging: installed via {install_source}",
            metadata={
                "install_source": install_source,
                "is_sideloaded": is_sideloaded,
                "version": getattr(app, "version", "1.0.0"),
                "package_name": getattr(app, "package_name", "")
            }
        )

normalizer = SignalNormalizer()
