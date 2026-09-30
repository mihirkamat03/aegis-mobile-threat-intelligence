package com.aether.aegis.data.model;

import java.io.Serializable;

public class PermissionInfo implements Serializable {
    private final String name;
    private final String group;
    private final boolean isSensitive;
    private final String usageContext;
    private final boolean isAnomaly;
    private final String lastUsed;

    public PermissionInfo(String name, String group, boolean isSensitive, String usageContext, boolean isAnomaly, String lastUsed) {
        this.name = name;
        this.group = group;
        this.isSensitive = isSensitive;
        this.usageContext = usageContext;
        this.isAnomaly = isAnomaly;
        this.lastUsed = lastUsed;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public boolean isSensitive() {
        return isSensitive;
    }

    public String getUsageContext() {
        return usageContext;
    }

    public boolean isAnomaly() {
        return isAnomaly;
    }

    public String getLastUsed() {
        return lastUsed;
    }
}
