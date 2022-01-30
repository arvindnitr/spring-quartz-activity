package com.arvind.quartz.scheduler.activity;

import lombok.Getter;

@Getter
public enum Operation {
    PRINT(ResourceType.TIME, "Print time", "Time {subject}, printing done by {user} at {time}"),
    PASSWORD_CHANGE(ResourceType.MT_DB, "MT DB password change", "MT DB password for pod {subject} of {participants} changed by {user} at {time}");

    private final ResourceType resourceType;
    private final String op;
    private String activitySummary;

    Operation(ResourceType resourceType, String op, String activitySummary) {
        this.resourceType = resourceType;
        this.op = op;
        this.activitySummary = activitySummary;
    }

    public static Operation safeValueOf(String literal) {
        try {
            return Operation.valueOf(literal);
        } catch (Exception ex) {
        }

        return null;
    }
}

