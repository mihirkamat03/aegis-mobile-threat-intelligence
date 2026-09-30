package com.aether.aegis.data.model;

import java.io.Serializable;

public class BehaviourEvent implements Serializable {
    private final String id;
    private final String timestamp;
    private final String title;
    private final String description;
    private final Severity severity;
    private final String associatedApp;
    private final int scoreShiftFrom;
    private final int scoreShiftTo;

    public BehaviourEvent(String id, String timestamp, String title, String description,
                          Severity severity, String associatedApp, int scoreShiftFrom, int scoreShiftTo) {
        this.id = id;
        this.timestamp = timestamp;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.associatedApp = associatedApp;
        this.scoreShiftFrom = scoreShiftFrom;
        this.scoreShiftTo = scoreShiftTo;
    }

    public String getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getAssociatedApp() {
        return associatedApp;
    }

    public int getScoreShiftFrom() {
        return scoreShiftFrom;
    }

    public int getScoreShiftTo() {
        return scoreShiftTo;
    }

    public boolean hasScoreShift() {
        return scoreShiftTo > scoreShiftFrom && scoreShiftFrom > 0;
    }
}
