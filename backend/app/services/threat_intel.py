from typing import Dict, Any

class ThreatIntelService:
    """
    AEGIS Simulated Threat Intelligence Lookup Service
    Maps observed endpoints against simulated OSINT threat feeds (AbuseIPDB, OTX, Quad9).
    """

    KNOWN_C2_DOMAINS = {
        "sync-audio-cdn.hopto.org": {
            "feed": "AbuseIPDB",
            "indicator_type": "C2_ENDPOINT",
            "confidence": 0.96,
            "threat_actor": "APT-MobileSpy",
            "severity": "CRITICAL"
        },
        "telemetry-collector.dynv6.net": {
            "feed": "AlienVault OTX",
            "indicator_type": "DYNAMIC_DNS_EXFIL",
            "confidence": 0.88,
            "threat_actor": "Generic Adware Dropper",
            "severity": "HIGH"
        },
        "ad-track-metric.biz": {
            "feed": "EasyList / Quad9",
            "indicator_type": "AGGRESSIVE_TRACKER",
            "confidence": 0.72,
            "threat_actor": "Commercial Profile Harvester",
            "severity": "SUSPICIOUS"
        }
    }

    @classmethod
    def lookup_domain(cls, domain: str) -> Dict[str, Any]:
        match = cls.KNOWN_C2_DOMAINS.get(domain.lower())
        if match:
            return {
                "matched": True,
                "domain": domain,
                "reputation": match["severity"],
                "confidence": match["confidence"],
                "feed": match["feed"],
                "description": f"Matched {match['feed']} {match['indicator_type']} (Confidence: {int(match['confidence']*100)}%)"
            }
        return {
            "matched": False,
            "domain": domain,
            "reputation": "CLEAN",
            "confidence": 0.0,
            "feed": "Quad9/Clean",
            "description": "No external malicious threat indicators identified."
        }

threat_intel_service = ThreatIntelService()
