package com.qtest.pdf.pages;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.qtest.cucumber.model.CucumberFeature;
import com.qtest.pdf.ColorScheme;
import com.qtest.pdf.PdfChartGenerator;
import com.qtest.pdf.PdfStyler;

import java.io.IOException;

/**
 * Dashboard Page - Displays overall feature statistics with donut charts.
 * Page 1 of feature PDF report.
 */
public class DashboardPage {

    private final PdfStyler styler;
    private final PdfChartGenerator chartGenerator;

    public DashboardPage(PdfStyler styler, PdfChartGenerator chartGenerator) {
        this.styler = styler;
        this.chartGenerator = chartGenerator;
    }

    /**
     * Build the dashboard page
     */
    public void build(Document document, CucumberFeature feature, PdfDocument pdfDoc) throws IOException {
        // Title with status badge
        addHeaderWithBadge(document, feature);

        // Statistics boxes
        addStatisticsSection(document, feature);

        // Charts
        addChartsSection(document, feature, pdfDoc);

        // Add page break for next page
        document.add(new AreaBreak());
    }

    /**
     * Add header with feature name and overall status badge
     */
    private void addHeaderWithBadge(Document document, CucumberFeature feature) {
        Table headerTable = new Table(2)
                .setWidth(100, com.itextpdf.layout.properties.UnitValue.PERCENT)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

        // Feature name
        Cell titleCell = new Cell()
                .add(new Paragraph("Feature: " + (feature.getName() != null ? feature.getName() : "Unknown"))
                        .setFont(styler.getBoldFont())
                        .setFontSize(24))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        // Status badge
        Cell badgeCell = styler.createStatusCell(feature.getOverallStatus())
                .setWidth(100)
                .setHeight(40)
                .setFont(styler.getBoldFont())
                .setFontSize(14);

        headerTable.addCell(titleCell);
        headerTable.addCell(badgeCell);

        document.add(headerTable);
        document.add(new Paragraph(" ").setMarginBottom(10));
    }

    /**
     * Add statistics section with counts
     */
    private void addStatisticsSection(Document document, CucumberFeature feature) {
        Paragraph stats = new Paragraph()
                .setFont(styler.getRegularFont())
                .setFontSize(11)
                .setMarginBottom(15);

        stats.add(new com.itextpdf.layout.element.Text("Scenarios: ")
                .setFont(styler.getBoldFont()));
        stats.add(feature.getTotalScenarios() + " | ");

        stats.add(new com.itextpdf.layout.element.Text("Passed: ")
                .setFont(styler.getBoldFont())
                .setFontColor(ColorScheme.PASSED));
        stats.add(feature.getPassedScenarios() + " | ");

        stats.add(new com.itextpdf.layout.element.Text("Failed: ")
                .setFont(styler.getBoldFont())
                .setFontColor(ColorScheme.FAILED));
        stats.add(feature.getFailedScenarios() + " | ");

        stats.add(new com.itextpdf.layout.element.Text("Skipped: ")
                .setFont(styler.getBoldFont())
                .setFontColor(ColorScheme.SKIPPED));
        stats.add(String.valueOf(feature.getSkippedScenarios()));

        document.add(stats);

        // Step statistics
        Paragraph stepStats = new Paragraph()
                .setFont(styler.getRegularFont())
                .setFontSize(11)
                .setMarginBottom(15);

        stepStats.add(new com.itextpdf.layout.element.Text("Steps: ")
                .setFont(styler.getBoldFont()));
        stepStats.add(feature.getTotalSteps() + " | ");

        stepStats.add(new com.itextpdf.layout.element.Text("Passed: ")
                .setFont(styler.getBoldFont())
                .setFontColor(ColorScheme.PASSED));
        stepStats.add(feature.getPassedSteps() + " | ");

        stepStats.add(new com.itextpdf.layout.element.Text("Failed: ")
                .setFont(styler.getBoldFont())
                .setFontColor(ColorScheme.FAILED));
        stepStats.add(feature.getFailedSteps() + " | ");

        stepStats.add(new com.itextpdf.layout.element.Text("Skipped: ")
                .setFont(styler.getBoldFont())
                .setFontColor(ColorScheme.SKIPPED));
        stepStats.add(String.valueOf(feature.getSkippedSteps()));

        document.add(stepStats);
        document.add(new Paragraph(" ").setMarginBottom(10));
    }

    /**
     * Add charts section (scenario and step distribution)
     */
    private void addChartsSection(Document document, CucumberFeature feature, PdfDocument pdfDoc) throws IOException {
        document.add(styler.createSectionTitle("Test Distribution"));

        // Create a table for charts side by side
        Table chartTable = new Table(2)
                .setWidth(100, com.itextpdf.layout.properties.UnitValue.PERCENT);

        // Scenario chart cell
        Cell scenarioChartCell = new Cell()
                .add(new Paragraph("Scenarios")
                        .setFont(styler.getBoldFont())
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

        // Step chart cell
        Cell stepChartCell = new Cell()
                .add(new Paragraph("Steps")
                        .setFont(styler.getBoldFont())
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

        chartTable.addCell(scenarioChartCell);
        chartTable.addCell(stepChartCell);

        document.add(chartTable);

        // Charts will be drawn on canvas - placeholder for actual implementation
        document.add(new Paragraph(String.format(
                "Scenarios: %d Passed | %d Failed | %d Skipped\nSteps: %d Passed | %d Failed | %d Skipped",
                feature.getPassedScenarios(), feature.getFailedScenarios(), feature.getSkippedScenarios(),
                feature.getPassedSteps(), feature.getFailedSteps(), feature.getSkippedSteps()
        )).setFont(styler.getRegularFont()).setFontSize(10));
    }
}
