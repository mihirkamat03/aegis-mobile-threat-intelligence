package com.aether.aegis.data.model;

import java.io.Serializable;

public class EvidenceNode implements Serializable {
    public enum NodeType {
        APP,
        PERMISSION,
        DOMAIN,
        THREAT_INTEL,
        RISK
    }

    private final String id;
    private final NodeType type;
    private final String label;
    private final String subtitle;
    private final Severity severity;
    private boolean isHighlighted;

    // Canvas rendering coordinates (normalized 0.0 to 1.0 or pixel coordinates)
    public float x;
    public float y;

    public EvidenceNode(String id, NodeType type, String label, String subtitle, Severity severity) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.subtitle = subtitle;
        this.severity = severity;
        this.isHighlighted = false;
    }

    public String getId() {
        return id;
    }

    public NodeType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Severity getSeverity() {
        return severity;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }
}
