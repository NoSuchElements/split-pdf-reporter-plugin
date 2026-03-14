package com.qtest.cucumber.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents the result of a single step (PASSED, FAILED, SKIPPED, PENDING).
 */
public class CucumberStepResult {
    private String status;
    private long duration;

    @SerializedName("error_message")
    private String errorMessage;

    private List<CucumberEmbedding> embeddings;

    public CucumberStepResult() {}

    public String getStatus() {
        return status;
    }

    public long getDuration() {
        return duration;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<CucumberEmbedding> getEmbeddings() {
        return embeddings;
    }

    /**
     * Check if step failed
     */
    public boolean isFailed() {
        return "failed".equalsIgnoreCase(status);
    }

    /**
     * Check if step passed
     */
    public boolean isPassed() {
        return "passed".equalsIgnoreCase(status);
    }

    /**
     * Check if step was skipped
     */
    public boolean isSkipped() {
        return "skipped".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status);
    }
}
