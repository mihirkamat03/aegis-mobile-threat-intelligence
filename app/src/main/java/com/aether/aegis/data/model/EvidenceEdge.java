package com.aether.aegis.data.model;

import java.io.Serializable;

public class EvidenceEdge implements Serializable {
    private final String sourceId;
    private final String targetId;
    private final String label;
    private final boolean isThreatPath;

    public EvidenceEdge(String sourceId, String targetId, String label, boolean isThreatPath) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.label = label;
        this.isThreatPath = isThreatPath;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getLabel() {
        return label;
    }

    public boolean isThreatPath() {
        return isThreatPath;
    }
}
