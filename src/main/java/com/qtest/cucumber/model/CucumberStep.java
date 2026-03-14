package com.qtest.cucumber.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single Cucumber step with result and optional error.
 */
public class CucumberStep {
    private String keyword;
    private String name;
    private int line;
    private CucumberStepResult result;
    private List<CucumberDataTable> dataTable;
    private CucumberDocString docString;

    @SerializedName("match")
    private CucumberMatch matchInfo;

    public CucumberStep() {}

    public String getKeyword() {
        return keyword;
    }

    public String getName() {
        return name;
    }

    public int getLine() {
        return line;
    }

    public CucumberStepResult getResult() {
        return result;
    }

    public List<CucumberDataTable> getDataTable() {
        return dataTable;
    }

    public CucumberDocString getDocString() {
        return docString;
    }

    public CucumberMatch getMatchInfo() {
        return matchInfo;
    }

    /**
     * Get step status
     */
    public String getStatus() {
        if (result != null) {
            return result.getStatus();
        }
        return "UNKNOWN";
    }

    /**
     * Get step duration in milliseconds
     */
    public long getDurationMillis() {
        if (result != null && result.getDuration() > 0) {
            return result.getDuration() / 1_000_000; // Convert nanoseconds to ms
        }
        return 0;
    }

    /**
     * Get error message if step failed
     */
    public String getErrorMessage() {
        if (result != null && result.getErrorMessage() != null) {
            return result.getErrorMessage();
        }
        return "";
    }

    /**
     * Format step display as: Given/When/Then step name
     */
    public String getDisplayText() {
        return (keyword != null ? keyword.trim() : "") + " " + (name != null ? name : "");
    }

    /**
     * Get all embedded screenshots for this step (zero, one, or many).
     */
    public List<String> getScreenshotBase64List() {
        List<String> screenshots = new ArrayList<>();
        if (result != null && result.getEmbeddings() != null) {
            for (CucumberEmbedding embedding : result.getEmbeddings()) {
                if (embedding != null && embedding.isScreenshot()
                        && embedding.getData() != null && !embedding.getData().isEmpty()) {
                    screenshots.add(embedding.getData());
                }
            }
        }
        return screenshots;
    }

    /**
     * Get the first embedded screenshot for this step, if any.
     * Existing callers rely on a single image; this remains for compatibility.
     */
    public String getScreenshotBase64() {
        List<String> screenshots = getScreenshotBase64List();
        return screenshots.isEmpty() ? null : screenshots.get(0);
    }
}
