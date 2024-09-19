package org.mf.langchain.prompt;

public enum MigrationPreferences {
    PREFER_PERFORMANCE("Optimize for performance (most efficient queries possible)"),
    PREFER_CONSISTENCY("Optimize for consistency / less redundant data");

    private final String description;

    MigrationPreferences(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
