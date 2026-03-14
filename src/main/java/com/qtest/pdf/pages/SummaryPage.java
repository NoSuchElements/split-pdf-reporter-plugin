package com.qtest.pdf.pages;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.qtest.cucumber.model.CucumberFeature;
import com.qtest.cucumber.model.CucumberScenario;
import com.qtest.pdf.ColorScheme;
import com.qtest.pdf.PdfStyler;

import java.util.List;

/**
 * Summary Page - Displays all scenarios with status, duration, and totals.
 * Page 2 of feature PDF report.
 */
public class SummaryPage {

    private final PdfStyler styler;

    public SummaryPage(PdfStyler styler) {
        this.styler = styler;
    }

    /**
     * Build the summary page
     */
    public void build(Document document, CucumberFeature feature) {
        document.add(styler.createSectionTitle("Scenario Summary"));
        document.add(new Paragraph(" ").setMarginBottom(5));

        // Create scenario table
        Table scenarioTable = createScenarioTable(feature);
        document.add(scenarioTable);

        document.add(new Paragraph(" ").setMarginBottom(10));

        // Add page break
        document.add(new AreaBreak());
    }

    /**
     * Create table with all scenarios
     */
    private Table createScenarioTable(CucumberFeature feature) {
        Table table = new Table(5)
                .setWidth(100, com.itextpdf.layout.properties.UnitValue.PERCENT);

        // Header row
        table.addCell(styler.createHeaderCell("Scenario"));
        table.addCell(styler.createHeaderCell("Status"));
        table.addCell(styler.createHeaderCell("Passed"));
        table.addCell(styler.createHeaderCell("Failed"));
        table.addCell(styler.createHeaderCell("Duration"));

        // Data rows
        boolean alternate = false;
        List<CucumberScenario> scenarios = feature.getScenarios();
        if (scenarios != null) {
            for (CucumberScenario scenario : scenarios) {
                table.addCell(styler.createDataCell(scenario.getName(), alternate));
                table.addCell(styler.createStatusCell(scenario.getStatus()));
                table.addCell(styler.createDataCellCentered(String.valueOf(scenario.getPassedSteps()), alternate));
                table.addCell(styler.createDataCellCentered(String.valueOf(scenario.getFailedSteps()), alternate));
                table.addCell(styler.createDataCellCentered(scenario.formatDuration(), alternate));
                alternate = !alternate;
            }
        }

        // Footer row with totals
        Cell totalLabelCell = new Cell()
                .add(new Paragraph("TOTALS")
                        .setFont(styler.getBoldFont())
                        .setFontSize(11)
                        .setFontColor(ColorScheme.TEXT_WHITE))
                .setBackgroundColor(ColorScheme.BG_HEADER)
                .setPadding(8)
                .setTextAlignment(TextAlignment.LEFT)
                .setBorder(new SolidBorder(ColorScheme.BORDER, 0.5f));

        Cell emptyCell = new Cell()
                .add(new Paragraph(""))
                .setBackgroundColor(ColorScheme.BG_HEADER)
                .setPadding(8)
                .setBorder(new SolidBorder(ColorScheme.BORDER, 0.5f));

        Cell totalPassedCell = new Cell()
                .add(new Paragraph(String.valueOf(feature.getPassedSteps()))
                        .setFont(styler.getBoldFont())
                        .setFontSize(11)
                        .setFontColor(ColorScheme.TEXT_WHITE))
                .setBackgroundColor(ColorScheme.BG_HEADER)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8)
                .setBorder(new SolidBorder(ColorScheme.BORDER, 0.5f));

        Cell totalFailedCell = new Cell()
                .add(new Paragraph(String.valueOf(feature.getFailedSteps()))
                        .setFont(styler.getBoldFont())
                        .setFontSize(11)
                        .setFontColor(ColorScheme.TEXT_WHITE))
                .setBackgroundColor(ColorScheme.BG_HEADER)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8)
                .setBorder(new SolidBorder(ColorScheme.BORDER, 0.5f));

        Cell totalDurationCell = new Cell()
                .add(new Paragraph("-")
                        .setFont(styler.getBoldFont())
                        .setFontSize(11)
                        .setFontColor(ColorScheme.TEXT_WHITE))
                .setBackgroundColor(ColorScheme.BG_HEADER)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8)
                .setBorder(new SolidBorder(ColorScheme.BORDER, 0.5f));

        table.addCell(totalLabelCell);
        table.addCell(emptyCell);
        table.addCell(totalPassedCell);
        table.addCell(totalFailedCell);
        table.addCell(totalDurationCell);

        return table;
    }
}
