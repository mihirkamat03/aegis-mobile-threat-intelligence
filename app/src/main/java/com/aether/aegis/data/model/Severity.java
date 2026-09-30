package com.aether.aegis.data.model;

public enum Severity {
    SAFE("SAFE"),
    LOW("LOW"),
    SUSPICIOUS("SUSPICIOUS"),
    HIGH("HIGH"),
    CRITICAL("CRITICAL");

    private final String label;

    Severity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Severity fromScore(int score) {
        if (score >= 80) return CRITICAL;
        if (score >= 60) return HIGH;
        if (score >= 35) return SUSPICIOUS;
        if (score >= 10) return LOW;
        return SAFE;
    }
}
