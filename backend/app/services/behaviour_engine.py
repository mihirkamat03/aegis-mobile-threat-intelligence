from app.schemas.behaviour import BehaviourComparison

class BehaviourEngine:
    """
    AEGIS Behaviour Baseline Comparison Engine (Prototype)
    Compares recent application telemetry against established historical baselines.
    """

    @staticmethod
    def compare_metric(metric_name: str, baseline: float, observed: float) -> BehaviourComparison:
        if baseline <= 0:
            deviation_percent = 100.0 if observed > 0 else 0.0
        else:
            deviation_percent = ((observed - baseline) / baseline) * 100.0

        if deviation_percent >= 150.0:
            classification = "ANOMALOUS"
            explanation = (
                f"Significant behavioural anomaly detected for {metric_name}: "
                f"spiked from {baseline:.1f} to {observed:.1f} (+{deviation_percent:.0f}% deviation)."
            )
        elif deviation_percent >= 50.0:
            classification = "ELEVATED"
            explanation = (
                f"Elevated activity observed for {metric_name}: "
                f"increased by {deviation_percent:.0f}% over normal baseline."
            )
        else:
            classification = "NORMAL"
            explanation = f"{metric_name} operating within normal baseline limits ({baseline:.1f} vs {observed:.1f})."

        return BehaviourComparison(
            metric_name=metric_name,
            baseline=baseline,
            observed=observed,
            deviation_percent=round(deviation_percent, 1),
            classification=classification,
            explanation=explanation
        )

behaviour_engine = BehaviourEngine()
