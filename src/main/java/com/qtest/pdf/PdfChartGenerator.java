package com.qtest.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

/**
 * Minimal chart helper for PDFBox. For now, we render textual summaries and
 * leave room for future donut charts if desired.
 */
public class PdfChartGenerator {

    private final PdfStyler styler;

    public PdfChartGenerator(PdfStyler styler) {
        this.styler = styler;
    }

    /**
     * Draw a simple textual representation of scenario/step distribution.
     * Can be replaced by real donut charts later without changing callers.
     */
    public void drawScenarioAndStepSummary(PDDocument doc, PDPage page,
                                           int passedScenarios, int failedScenarios, int skippedScenarios,
                                           int passedSteps, int failedSteps, int skippedSteps,
                                           float x, float y) throws IOException {
        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
            String line1 = String.format("Scenarios - Passed: %d, Failed: %d, Skipped: %d",
                    passedScenarios, failedScenarios, skippedScenarios);
            String line2 = String.format("Steps    - Passed: %d, Failed: %d, Skipped: %d",
                    passedSteps, failedSteps, skippedSteps);

            styler.drawText(doc, cs, line1, x, y, styler.regularFont(), 10f);
            styler.drawText(doc, cs, line2, x, y - 14, styler.regularFont(), 10f);
        }
    }
}
