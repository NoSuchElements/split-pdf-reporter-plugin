package com.qtest.cucumber.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a parsed Cucumber feature from JSON results.
 * Immutable data structure - no state management issues.
 */
public class CucumberFeature {
    private String name;
    private String description;
    private String uri;
    private List<String> tags;
    private List<CucumberScenario> scenarios;
    private String id;

    @SerializedName("keyword")
    private String keyword;

    // Calculated fields for reporting
    private transient int totalScenarios;
    private transient int passedScenarios;
    private transient int failedScenarios;
    private transient int skippedScenarios;
    private transient int totalSteps;
    private transient int passedSteps;
    private transient int failedSteps;
    private transient int skippedSteps;
    private transient String qtestCaseId;

    public CucumberFeature() {}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUri() {
        return uri;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<CucumberScenario> getScenarios() {
        return scenarios;
    }

    public String getId() {
        return id;
    }

    public String getKeyword() {
        return keyword;
    }

    public int getTotalScenarios() {
        return totalScenarios;
    }

    public void setTotalScenarios(int totalScenarios) {
        this.totalScenarios = totalScenarios;
    }

    public int getPassedScenarios() {
        return passedScenarios;
    }

    public void setPassedScenarios(int passedScenarios) {
        this.passedScenarios = passedScenarios;
    }

    public int getFailedScenarios() {
        return failedScenarios;
    }

    public void setFailedScenarios(int failedScenarios) {
        this.failedScenarios = failedScenarios;
    }

    public int getSkippedScenarios() {
        return skippedScenarios;
    }

    public void setSkippedScenarios(int skippedScenarios) {
        this.skippedScenarios = skippedScenarios;
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

    public String getQtestCaseId() {
        return qtestCaseId;
    }

    public void setQtestCaseId(String qtestCaseId) {
        this.qtestCaseId = qtestCaseId;
    }

    /**
     * Extract @QTEST_TC_XXXX tag from tags list
     */
    public String extractQtestTag() {
        if (tags != null) {
            for (String tag : tags) {
                if (tag.startsWith("@QTEST_TC_")) {
                    return tag.substring(1); // Remove leading @
                }
            }
        }
        return "UNKNOWN";
    }

    /**
     * Calculate overall status based on step results
     */
    public String getOverallStatus() {
        if (failedSteps > 0) return "FAILED";
        if (skippedSteps > 0) return "SKIPPED";
        return "PASSED";
    }
}
