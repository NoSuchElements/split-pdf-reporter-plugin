package com.qtest.cucumber.model;

import java.util.List;

/**
 * Represents step match information.
 */
public class CucumberMatch {
    private String location;
    private List<CucumberArgument> arguments;

    public CucumberMatch() {}

    public String getLocation() {
        return location;
    }

    public List<CucumberArgument> getArguments() {
        return arguments;
    }
}
