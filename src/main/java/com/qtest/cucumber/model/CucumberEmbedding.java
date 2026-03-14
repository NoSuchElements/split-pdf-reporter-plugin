package com.qtest.cucumber.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents embedded content (screenshots, logs, etc.) in test results.
 */
public class CucumberEmbedding {
    @SerializedName("mime_type")
    private String mimeType;

    private String data; // base64 encoded

    public CucumberEmbedding() {}

    public String getMimeType() {
        return mimeType;
    }

    public String getData() {
        return data;
    }

    /**
     * Check if this is a screenshot (image/png or image/jpeg)
     */
    public boolean isScreenshot() {
        return mimeType != null && (mimeType.contains("image/png") || mimeType.contains("image/jpeg"));
    }

    /**
     * Get image format from mime type
     */
    public String getImageFormat() {
        if (mimeType == null) return "png";
        if (mimeType.contains("png")) return "png";
        if (mimeType.contains("jpeg") || mimeType.contains("jpg")) return "jpg";
        return "png";
    }
}
