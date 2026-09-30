from typing import List, Dict, Any

class RecommendationEngine:
    """
    AEGIS Action Recommendation Service.
    Maps evidence signals and correlated finding severities to structured, non-destructive recommendations.
    Adheres strictly to capability honesty: recommendations provide user guidance and do NOT alter system settings.
    """

    ACTION_REVIEW_PERMISSION = "REVIEW_PERMISSION"
    ACTION_INVESTIGATE_DOMAIN = "INVESTIGATE_DOMAIN"
    ACTION_MONITOR_APPLICATION = "MONITOR_APPLICATION"
    ACTION_REVIEW_BEHAVIOUR = "REVIEW_BEHAVIOUR"
    ACTION_CONSIDER_REMOVING_APP = "CONSIDER_REMOVING_APP"

    @classmethod
    def get_recommendation(
        cls,
        severity: str,
        has_sensitive_perm: bool,
        has_suspicious_domain: bool,
        has_behaviour_anomaly: bool,
        has_threat_intel: bool,
        app_name: str = "this application"
    ) -> Dict[str, str]:
        if severity == "CRITICAL":
            return {
                "action": cls.ACTION_CONSIDER_REMOVING_APP,
                "title": f"Consider Uninstalling {app_name}",
                "guidance": (
                    f"Confirmed multi-signal correlation indicates targeted spyware or unauthorized exfiltration. "
                    f"It is strongly recommended to isolate or uninstall {app_name} and audit recent account activity."
                )
            }
        elif severity == "HIGH":
            if has_suspicious_domain and has_sensitive_perm:
                return {
                    "action": cls.ACTION_CONSIDER_REMOVING_APP,
                    "title": f"Revoke Permissions or Remove {app_name}",
                    "guidance": (
                        f"{app_name} transmits sensitive data to untrusted network destinations. "
                        f"Immediately revoke background permissions via Android Settings, or consider removing the app."
                    )
                }
            elif has_suspicious_domain:
                return {
                    "action": cls.ACTION_INVESTIGATE_DOMAIN,
                    "title": "Investigate Network Destinations",
                    "guidance": (
                        f"Unregistered dynamic DNS or tracker traffic detected. "
                        f"Audit destination hosts in the Evidence Graph and restrict background data usage."
                    )
                }
            else:
                return {
                    "action": cls.ACTION_MONITOR_APPLICATION,
                    "title": f"Closely Monitor {app_name}",
                    "guidance": (
                        f"Elevated risk signals detected. Keep {app_name} under active monitoring "
                        f"and verify whether recent background invocations match your actual usage."
                    )
                }
        elif severity == "SUSPICIOUS":
            if has_behaviour_anomaly:
                return {
                    "action": cls.ACTION_REVIEW_BEHAVIOUR,
                    "title": "Review Background Behaviour",
                    "guidance": (
                        f"Recent telemetry shows unusual frequency spikes. "
                        f"Check if background sync is intended or restrict battery optimization for {app_name}."
                    )
                }
            else:
                return {
                    "action": cls.ACTION_REVIEW_PERMISSION,
                    "title": "Review Declared Capabilities",
                    "guidance": (
                        f"Permissions declared by {app_name} exceed baseline privacy norms. "
                        f"Review and revoke non-essential permissions in device privacy settings."
                    )
                }
        elif severity == "LOW":
            return {
                "action": cls.ACTION_MONITOR_APPLICATION,
                "title": "Routine Monitoring",
                "guidance": f"Minor background signals observed within normal operating parameters. No immediate action required."
            }
        else:
            return {
                "action": cls.ACTION_MONITOR_APPLICATION,
                "title": "Application Posture Secure",
                "guidance": f"Application behavior conforms to baseline privacy and security guidelines."
            }

recommendation_engine = RecommendationEngine()
