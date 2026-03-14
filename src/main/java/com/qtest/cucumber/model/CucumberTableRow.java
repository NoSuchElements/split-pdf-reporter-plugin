package com.qtest.cucumber.model;

import java.util.List;

/**
 * Represents a single row in a DataTable.
 */
public class CucumberTableRow {
    private List<String> cells;

    public CucumberTableRow() {}

    public List<String> getCells() {
        return cells;
    }
}
