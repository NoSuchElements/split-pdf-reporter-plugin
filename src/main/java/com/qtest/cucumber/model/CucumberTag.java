package com.qtest.cucumber.model;

/**
 * Represents a tag object in Cucumber JSON (name, type, location, etc.).
 * We only care about the {@code name} field for reporting.
 */
public class CucumberTag {
    private String name;

    public CucumberTag() {}

    public String getName() {
        return name;
    }
}
