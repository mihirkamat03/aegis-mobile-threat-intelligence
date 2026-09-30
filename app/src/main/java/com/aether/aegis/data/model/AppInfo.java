package com.aether.aegis.data.model;

import java.io.Serializable;
import java.util.List;

public class AppInfo implements Serializable {
    private final String id;
    private final String name;
    private final String packageName;
    private final String version;
    private final String installSource;
    private int riskScore;
    private Severity severity;
    private int threatScore;
    private int privacyScore;
    private String explanation;
    private final List<PermissionInfo> permissions;
    private final List<DomainInfo> domains;
    private final List<BehaviourEvent> behaviourEvents;
    private final List<Recommendation> recommendations;
    private final List<EvidenceNode> evidenceNodes;
    private final List<EvidenceEdge> evidenceEdges;
    private final String primaryRiskFactor;
    private final boolean isSideloaded;

    public AppInfo(String id, String name, String packageName, String version, String installSource,
                   int riskScore, Severity severity, int threatScore, int privacyScore,
                   String explanation, String primaryRiskFactor, boolean isSideloaded,
                   List<PermissionInfo> permissions, List<DomainInfo> domains,
                   List<BehaviourEvent> behaviourEvents, List<Recommendation> recommendations,
                   List<EvidenceNode> evidenceNodes, List<EvidenceEdge> evidenceEdges) {
        this.id = id;
        this.name = name;
        this.packageName = packageName;
        this.version = version;
        this.installSource = installSource;
        this.riskScore = riskScore;
        this.severity = severity;
        this.threatScore = threatScore;
        this.privacyScore = privacyScore;
        this.explanation = explanation;
        this.primaryRiskFactor = primaryRiskFactor;
        this.isSideloaded = isSideloaded;
        this.permissions = permissions;
        this.domains = domains;
        this.behaviourEvents = behaviourEvents;
        this.recommendations = recommendations;
        this.evidenceNodes = evidenceNodes;
        this.evidenceEdges = evidenceEdges;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVersion() {
        return version;
    }

    public String getInstallSource() {
        return installSource;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
        this.severity = Severity.fromScore(riskScore);
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public int getThreatScore() {
        return threatScore;
    }

    public void setThreatScore(int threatScore) {
        this.threatScore = threatScore;
    }

    public int getPrivacyScore() {
        return privacyScore;
    }

    public void setPrivacyScore(int privacyScore) {
        this.privacyScore = privacyScore;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getPrimaryRiskFactor() {
        return primaryRiskFactor;
    }

    public boolean isSideloaded() {
        return isSideloaded;
    }

    public List<PermissionInfo> getPermissions() {
        return permissions;
    }

    public List<DomainInfo> getDomains() {
        return domains;
    }

    public List<BehaviourEvent> getBehaviourEvents() {
        return behaviourEvents;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public List<EvidenceNode> getEvidenceNodes() {
        return evidenceNodes;
    }

    public List<EvidenceEdge> getEvidenceEdges() {
        return evidenceEdges;
    }
}
