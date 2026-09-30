package com.aether.aegis.data.model;

import java.io.Serializable;

public class DomainInfo implements Serializable {
    private final String domain;
    private final String ip;
    private final String reputation;
    private final String associatedApp;
    private final String lastObserved;
    private final String threatIndicator;
    private final String confidence;
    private boolean isBlocked;

    public DomainInfo(String domain, String ip, String reputation, String associatedApp,
                      String lastObserved, String threatIndicator, String confidence, boolean isBlocked) {
        this.domain = domain;
        this.ip = ip;
        this.reputation = reputation;
        this.associatedApp = associatedApp;
        this.lastObserved = lastObserved;
        this.threatIndicator = threatIndicator;
        this.confidence = confidence;
        this.isBlocked = isBlocked;
    }

    public String getDomain() {
        return domain;
    }

    public String getIp() {
        return ip;
    }

    public String getReputation() {
        return reputation;
    }

    public String getAssociatedApp() {
        return associatedApp;
    }

    public String getLastObserved() {
        return lastObserved;
    }

    public String getThreatIndicator() {
        return threatIndicator;
    }

    public String getConfidence() {
        return confidence;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
