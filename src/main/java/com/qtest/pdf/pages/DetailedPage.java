package com.qtest.pdf.pages;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.qtest.cucumber.model.CucumberScenario;
import com.qtest.cucumber.model.CucumberStep;
import com.qtest.cucumber.model.CucumberDataTable;
import com.qtest.cucumber.model.CucumberTableRow;
import com.qtest.pdf.ColorScheme;
import com.qtest.pdf.PdfStyler;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * Detailed Page - Shows every step with keywords, names, durations, statuses, errors, and screenshots.
 * Pages 3+ of feature PDF report.
 */
public class DetailedPage {

    private final PdfStyler styler;

    public DetailedPage(PdfStyler styler) {
        this.styler = styler;
    }

    /**
     * Build detailed pages (one per scenario)
     */
    public void build(Document document, CucumberScenario scenario) throws IOException {
        // Scenario header
        addScenarioHeader(document, scenario);

        // Steps
        addStepsSection(document, scenario);

        // Page break for next scenario
        document.add(new AreaBreak());
    }

    /**
     * Add scenario header with name and tags
     */
    private void addScenarioHeader(Document document, CucumberScenario scenario) {
        Paragraph header = new Paragraph("Scenario: " + (scenario.getName() != null ? scenario.getName() : "Unknown"))
                .setFont(styler.getBoldFont())
                .setFontSize(16)
                .setMarginBottom(5);
        document.add(header);

        // Tags and metadata
        Paragraph meta = new Paragraph()
                .setFont(styler.getRegularFont())
                .setFontSize(9)
                .setFontColor(ColorScheme.TEXT_SECONDARY)
                .setMarginBottom(10);

        if (scenario.getTags() != null && !scenario.getTags().isEmpty()) {
            meta.add(new com.itextpdf.layout.element.Text("Tags: "));
            meta.add(String.join(", ", scenario.getTags()));
            meta.add("\n");
        }

        meta.add(new com.itextpdf.layout.element.Text("Duration: "))
                .add(scenario.formatDuration())
                .add(" | ")
                .add(new com.itextpdf.layout.element.Text("Status: ")
                        .setFont(styler.getBoldFont())
                        .setFontColor(ColorScheme.getStatusColor(scenario.getStatus())))
                .add(scenario.getStatus().toUpperCase());

        document.add(meta);
        document.add(new Paragraph(" ").setMarginBottom(5));
    }

    /**
     * Add steps section with all step details
     */
    private void addStepsSection(Document document, CucumberScenario scenario) throws IOException {
        document.add(styler.createSectionTitle("Steps"));

        List<CucumberStep> steps = scenario.getSteps();
        if (steps == null || steps.isEmpty()) {
            document.add(new Paragraph("No steps found")
                    .setFont(styler.getRegularFont())
                    .setFontSize(10));
            return;
        }

        for (CucumberStep step : steps) {
            addStepDetail(document, step);
        }
    }

    /**
     * Add individual step details
     */
    private void addStepDetail(Document document, CucumberStep step) throws IOException {
        // Step line with keyword and name
        Paragraph stepLine = styler.createStepParagraph(step.getKeyword(), step.getName())
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(stepLine);

        // Status dot + duration
        Paragraph statusLine = new Paragraph()
                .setFont(styler.getRegularFont())
                .setFontSize(10)
                .setMarginBottom(5)
                .setMarginLeft(20);

        statusLine.add("● ")
                .setFontColor(ColorScheme.getStatusColor(step.getStatus()));
        statusLine.add(step.getStatus().toUpperCase() + " | ");
        statusLine.add(String.valueOf(step.getDurationMillis()) + "ms");

        document.add(statusLine);

        // Error message if failed
        if ("FAILED".equalsIgnoreCase(step.getStatus()) && !step.getErrorMessage().isEmpty()) {
            addErrorSection(document, step.getErrorMessage());
        }

        // DataTable if present
        if (step.getDataTable() != null && !step.getDataTable().isEmpty()) {
            addDataTableSection(document, step.getDataTable());
        }

        // DocString if present
        if (step.getDocString() != null && step.getDocString().getContent() != null) {
            addDocStringSection(document, step.getDocString().getContent());
        }

        // Embedded screenshot if present
        String screenshot = step.getScreenshotBase64();
        if (screenshot != null && !screenshot.isEmpty()) {
            addScreenshotSection(document, screenshot);
        }
    }

    /**
     * Add error section (red text, first 3 lines + overflow count)
     */
    private void addErrorSection(Document document, String errorMessage) {
        String[] lines = errorMessage.split("\n");
        StringBuilder displayError = new StringBuilder();
        int displayLines = Math.min(lines.length, 3);

        for (int i = 0; i < displayLines; i++) {
            displayError.append(lines[i]).append("\n");
        }

        if (lines.length > 3) {
            displayError.append("... + ").append(lines.length - 3).append(" more lines");
        }

        Paragraph errorPara = styler.createErrorParagraph(displayError.toString());
        errorPara.setMarginLeft(20);
        document.add(errorPara);
    }

    /**
     * Add DataTable section
     */
    private void addDataTableSection(Document document, List<CucumberDataTable> dataTables) {
        for (CucumberDataTable dataTable : dataTables) {
            List<CucumberTableRow> rows = dataTable.getRows();
            if (rows == null || rows.isEmpty()) continue;

            int columnCount = rows.get(0).getCells().size();
            Table table = new Table(columnCount)
                    .setWidth(90, com.itextpdf.layout.properties.UnitValue.PERCENT)
                    .setMarginLeft(20)
                    .setMarginTop(5)
                    .setMarginBottom(5);

            // Header row (first row of DataTable)
            CucumberTableRow headerRow = rows.get(0);
            for (String cell : headerRow.getCells()) {
                table.addCell(styler.createHeaderCell(cell));
            }

            // Data rows
            boolean alternate = false;
            for (int i = 1; i < rows.size(); i++) {
                CucumberTableRow row = rows.get(i);
                for (String cell : row.getCells()) {
                    table.addCell(styler.createDataCell(cell, alternate));
                }
                alternate = !alternate;
            }

            document.add(table);
        }
    }

    /**
     * Add DocString section (code block-like)
     */
    private void addDocStringSection(Document document, String content) {
        Paragraph docStringPara = new Paragraph(content)
                .setFont(styler.getMonoFont())
                .setFontSize(9)
                .setBackgroundColor(ColorScheme.BG_ROW_ALT)
                .setPadding(8)
                .setMarginLeft(20)
                .setMarginTop(5)
                .setMarginBottom(5);
        document.add(docStringPara);
    }

    /**
     * Add embedded screenshot (base64 encoded image)
     */
    private void addScreenshotSection(Document document, String base64Image) throws IOException {
        try {
            byte[] decodedImage = Base64.getDecoder().decode(base64Image);
            Image image = new Image(ImageDataFactory.create(decodedImage))
                    .setWidth(400)
                    .setMarginLeft(20)
                    .setMarginTop(8)
                    .setMarginBottom(8);
            document.add(image);
        } catch (Exception e) {
            // If image decode fails, add error note
            Paragraph errorNote = new Paragraph("[Screenshot image failed to decode]")
                    .setFont(styler.getRegularFont())
                    .setFontSize(9)
                    .setFontColor(ColorScheme.TEXT_SECONDARY)
                    .setMarginLeft(20);
            document.add(errorNote);
        }
    }
}
