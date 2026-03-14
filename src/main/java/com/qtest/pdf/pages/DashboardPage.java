package com.qtest.pdf.pages;

import com.qtest.cucumber.model.CucumberFeature;
import com.qtest.pdf.ColorScheme;
import com.qtest.pdf.PdfChartGenerator;
import com.qtest.pdf.PdfStyler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;

/**
 * Dashboard page (page 1) for a feature PDF using PDFBox.
 */
public class DashboardPage {

    private final PdfStyler styler;
    private final PdfChartGenerator chartGenerator;

    public DashboardPage(PdfStyler styler, PdfChartGenerator chartGenerator) {
        this.styler = styler;
        this.chartGenerator = chartGenerator;
    }

    public void build(PDDocument doc, PDPage page, CucumberFeature feature) throws IOException {
        PDRectangle box = page.getMediaBox();
        float margin = 40f;
        float y = box.getUpperRightY() - margin;

        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
            // Header: Feature name
            String featureName = feature.getName() != null ? feature.getName() : "Unknown";
            styler.drawText(doc, cs, "Feature: " + featureName, margin, y, styler.boldFont(), 18f);

            // Status badge on the right
            float badgeWidth = 90f;
            float badgeHeight = 20f;
            float badgeX = box.getUpperRightX() - margin - badgeWidth;
            float badgeY = y - 4f;
            styler.drawStatusBadge(cs, feature.getOverallStatus(), badgeX, badgeY - badgeHeight + 4, badgeWidth, badgeHeight);

            // Scenario stats
            float statsY = y - 40f;
            styler.drawLabelValue(cs, "Scenarios Total:", String.valueOf(feature.getTotalScenarios()), margin, statsY);
            styler.drawLabelValue(cs, "Passed:", String.valueOf(feature.getPassedScenarios()), margin, statsY - 14);
            styler.drawLabelValue(cs, "Failed:", String.valueOf(feature.getFailedScenarios()), margin, statsY - 28);
            styler.drawLabelValue(cs, "Skipped:", String.valueOf(feature.getSkippedScenarios()), margin, statsY - 42);

            // Step stats
            float stepsY = statsY - 70f;
            styler.drawLabelValue(cs, "Steps Total:", String.valueOf(feature.getTotalSteps()), margin, stepsY);
            styler.drawLabelValue(cs, "Passed:", String.valueOf(feature.getPassedSteps()), margin, stepsY - 14);
            styler.drawLabelValue(cs, "Failed:", String.valueOf(feature.getFailedSteps()), margin, stepsY - 28);
            styler.drawLabelValue(cs, "Skipped:", String.valueOf(feature.getSkippedSteps()), margin, stepsY - 42);
        }

        // Simple textual chart summary placeholder (bottom area)
        chartGenerator.drawScenarioAndStepSummary(
                doc,
                page,
                feature.getPassedScenarios(), feature.getFailedScenarios(), feature.getSkippedScenarios(),
                feature.getPassedSteps(), feature.getFailedSteps(), feature.getSkippedSteps(),
                margin, box.getLowerLeftY() + 80f
        );
    }
}
