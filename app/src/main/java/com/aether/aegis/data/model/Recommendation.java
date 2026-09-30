package com.aether.aegis.data.model;

import java.io.Serializable;

public class Recommendation implements Serializable {
    private final String id;
    private final String title;
    private final String description;
    private final String actionType; // "ANDROID_SETTINGS", "DNS_BLOCK", "QUARANTINE"
    private final String intentTarget;
    private boolean isCompleted;

    public Recommendation(String id, String title, String description, String actionType, String intentTarget, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.actionType = actionType;
        this.intentTarget = intentTarget;
        this.isCompleted = isCompleted;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getActionType() {
        return actionType;
    }

    public String getIntentTarget() {
        return intentTarget;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
