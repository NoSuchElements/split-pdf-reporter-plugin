package com.qtest.cucumber.model;

import java.util.List;

/**
 * Represents DataTable in Cucumber steps.
 */
public class CucumberDataTable {
    private List<CucumberTableRow> rows;

    public CucumberDataTable() {}

    public List<CucumberTableRow> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rows != null ? rows.size() : 0;
    }

    public int getColumnCount() {
        if (rows != null && !rows.isEmpty()) {
            return rows.get(0).getCells().size();
        }
        return 0;
    }
}
