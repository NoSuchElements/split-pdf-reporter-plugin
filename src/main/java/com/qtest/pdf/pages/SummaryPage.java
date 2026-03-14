package com.qtest.pdf.pages;

import com.qtest.cucumber.model.CucumberFeature;
import com.qtest.cucumber.model.CucumberScenario;
import com.qtest.pdf.PdfStyler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.util.List;

/**
 * Summary page (page 2) using simple PDFBox layout. We keep the same
 * information as the original iText version but draw a hand-crafted table.
 */
public class SummaryPage {

    private final PdfStyler styler;

    public SummaryPage(PdfStyler styler) {
        this.styler = styler;
    }

    public void build(PDDocument doc, PDPage page, CucumberFeature feature) throws IOException {
        PDRectangle box = page.getMediaBox();
        float margin = 40f;
        float y = box.getUpperRightY() - margin;

        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
            styler.drawText(doc, cs, "Scenario Summary", margin, y, styler.boldFont(), 16f);

            float tableTopY = y - 30f;
            drawHeaderRow(cs, margin, tableTopY, box.getWidth() - 2 * margin);

            float rowHeight = 16f;
            float currentY = tableTopY - rowHeight;

            List<CucumberScenario> scenarios = feature.getScenarios();
            if (scenarios != null) {
                for (CucumberScenario scenario : scenarios) {
                    drawScenarioRow(cs, margin, currentY, box.getWidth() - 2 * margin, scenario);
                    currentY -= rowHeight;
                }
            }

            // Totals footer
            currentY -= 10f;
            styler.drawText(doc, cs,
                    String.format("Totals - Steps Passed: %d, Failed: %d", feature.getPassedSteps(), feature.getFailedSteps()),
                    margin,
                    currentY,
                    styler.boldFont(),
                    10f);
        }
    }

    private void drawHeaderRow(PDPageContentStream cs, float x, float y, float width) throws IOException {
        float[] colWidths = columns(width);
        float colX = x;
        String[] headers = {"Scenario", "Status", "Passed", "Failed", "Duration"};
        for (int i = 0; i < headers.length; i++) {
            styler.drawText(null, cs, headers[i], colX + 2, y, styler.boldFont(), 10f);
            colX += colWidths[i];
        }
    }

    private void drawScenarioRow(PDPageContentStream cs, float x, float y, float width,
                                 CucumberScenario s) throws IOException {
        float[] colWidths = columns(width);
        float colX = x;

        String[] values = {
                s.getName(),
                s.getStatus(),
                String.valueOf(s.getPassedSteps()),
                String.valueOf(s.getFailedSteps()),
                s.formatDuration()
        };

        for (int i = 0; i < values.length; i++) {
            styler.drawText(null, cs, values[i], colX + 2, y, styler.regularFont(), 9f);
            colX += colWidths[i];
        }
    }

    private float[] columns(float totalWidth) {
        // Simple proportional columns: 40%, 15%, 15%, 15%, 15%
        return new float[]{
                totalWidth * 0.40f,
                totalWidth * 0.15f,
                totalWidth * 0.15f,
                totalWidth * 0.15f,
                totalWidth * 0.15f
        };
    }
}
