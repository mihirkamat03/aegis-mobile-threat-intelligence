package com.aether.aegis.data.model;

import java.io.Serializable;

public class Threat implements Serializable {
    private final String id;
    private final String title;
    private final Severity severity;
    private final String appName;
    private final String packageName;
    private final String detectionTime;
    private final int evidenceCount;
    private String status; // "ACTIVE", "RESOLVED", "INVESTIGATING"
    private final String description;
    private final String recommendedAction;

    public Threat(String id, String title, Severity severity, String appName, String packageName,
                  String detectionTime, int evidenceCount, String status, String description, String recommendedAction) {
        this.id = id;
        this.title = title;
        this.severity = severity;
        this.appName = appName;
        this.packageName = packageName;
        this.detectionTime = detectionTime;
        this.evidenceCount = evidenceCount;
        this.status = status;
        this.description = description;
        this.recommendedAction = recommendedAction;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getDetectionTime() {
        return detectionTime;
    }

    public int getEvidenceCount() {
        return evidenceCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }
}
