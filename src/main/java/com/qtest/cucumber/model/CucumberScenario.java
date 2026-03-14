package com.qtest.cucumber.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Cucumber scenario with steps.
 */
public class CucumberScenario {
    private String name;
    private String description;
    private String type;
    private String id;
    @SerializedName("tags")
    private List<CucumberTag> tagObjects;
    private List<CucumberStep> steps;
    private String keyword;

    @SerializedName("start_timestamp")
    private String startTimestamp;

    // Calculated fields
    private transient long durationMillis;
    private transient int totalSteps;
    private transient int passedSteps;
    private transient int failedSteps;
    private transient int skippedSteps;

    public CucumberScenario() {}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    /**
     * Return tag names for this scenario.
     */
    public List<String> getTags() {
        List<String> names = new ArrayList<>();
        if (tagObjects != null) {
            for (CucumberTag tag : tagObjects) {
                if (tag != null && tag.getName() != null) {
                    names.add(tag.getName());
                }
            }
        }
        return names;
    }

    public List<CucumberStep> getSteps() {
        return steps != null ? steps : java.util.Collections.emptyList();
    }

    public String getKeyword() {
        return keyword;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public int getPassedSteps() {
        return passedSteps;
    }

    public void setPassedSteps(int passedSteps) {
        this.passedSteps = passedSteps;
    }

    public int getFailedSteps() {
        return failedSteps;
    }

    public void setFailedSteps(int failedSteps) {
        this.failedSteps = failedSteps;
    }

    public int getSkippedSteps() {
        return skippedSteps;
    }

    public void setSkippedSteps(int skippedSteps) {
        this.skippedSteps = skippedSteps;
    }

    /**
     * Get overall status of scenario based on its steps
     */
    public String getStatus() {
        if (failedSteps > 0) return "FAILED";
        if (skippedSteps > 0) return "SKIPPED";
        return "PASSED";
    }

    /**
     * Format duration in human-readable form
     */
    public String formatDuration() {
        if (durationMillis < 1000) {
            return durationMillis + "ms";
        }
        return String.format("%.2fs", durationMillis / 1000.0);
    }
}
