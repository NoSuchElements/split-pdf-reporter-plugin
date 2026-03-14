package com.qtest.cucumber.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents DocString in Cucumber steps (multi-line text).
 */
public class CucumberDocString {
    @SerializedName("content")
    private String content;

    @SerializedName("line")
    private int line;

    public CucumberDocString() {}

    public String getContent() {
        return content;
    }

    public int getLine() {
        return line;
    }
}
