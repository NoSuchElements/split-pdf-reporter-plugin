package com.qtest.cucumber.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents an argument in step match.
 */
public class CucumberArgument {
    @SerializedName("value")
    private String value;

    @SerializedName("offset")
    private int offset;

    public CucumberArgument() {}

    public String getValue() {
        return value;
    }

    public int getOffset() {
        return offset;
    }
}
