package com.qtest.pdf.pages;

import com.qtest.cucumber.model.CucumberDataTable;
import com.qtest.cucumber.model.CucumberScenario;
import com.qtest.cucumber.model.CucumberStep;
import com.qtest.cucumber.model.CucumberTableRow;
import com.qtest.pdf.PdfStyler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * Detailed scenario pages using PDFBox. Layout is simpler than the iText
 * table-based version but preserves the same information hierarchy.
 */
public class DetailedPage {

    private final PdfStyler styler;

    public DetailedPage(PdfStyler styler) {
        this.styler = styler;
    }

    public void build(PDDocument doc, PDPage page, CucumberScenario scenario) throws IOException {
        PDRectangle box = page.getMediaBox();
        float margin = 40f;
        float y = box.getUpperRightY() - margin;

        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
            // Scenario header
            String scenarioName = scenario.getName() != null ? scenario.getName() : "Unknown";
            styler.drawText(doc, cs, "Scenario: " + scenarioName, margin, y, styler.boldFont(), 14f);
            y -= 20f;

            // Meta: status + duration
            styler.drawText(doc, cs,
                    String.format("Status: %s  |  Duration: %s", scenario.getStatus(), scenario.formatDuration()),
                    margin,
                    y,
                    styler.regularFont(),
                    10f);
            y -= 20f;

            // Steps
            List<CucumberStep> steps = scenario.getSteps();
            if (steps == null || steps.isEmpty()) {
                styler.drawText(doc, cs, "No steps found", margin, y, styler.regularFont(), 10f);
                return;
            }
        }

        float currentY = box.getUpperRightY() - margin - 60f;
        for (CucumberStep step : scenario.getSteps()) {
            currentY = addStep(doc, page, step, margin, currentY);
            currentY -= 10f;
        }
    }

    public void buildEmpty(PDDocument doc, PDPage page) throws IOException {
        PDRectangle box = page.getMediaBox();
        float margin = 40f;
        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
            styler.drawText(doc, cs, "No scenarios found in this feature", margin,
                    box.getUpperRightY() - margin, styler.regularFont(), 12f);
        }
    }

    private float addStep(PDDocument doc, PDPage page, CucumberStep step,
                          float margin, float y) throws IOException {
        PDRectangle box = page.getMediaBox();
        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
            // Step keyword + name
            String line = (step.getKeyword() != null ? step.getKeyword().trim() + " " : "") +
                    (step.getName() != null ? step.getName() : "");
            styler.drawText(doc, cs, line, margin, y, styler.regularFont(), 11f);
            y -= 14f;

            // Status + duration
            String statusLine = String.format("%s  |  %d ms",
                    step.getStatus().toUpperCase(), step.getDurationMillis());
            styler.drawText(doc, cs, statusLine, margin + 10f, y, styler.regularFont(), 9f);
            y -= 12f;

            // Error message (first 3 lines + overflow count)
            if ("FAILED".equalsIgnoreCase(step.getStatus()) && step.getErrorMessage() != null && !step.getErrorMessage().isEmpty()) {
                y = addError(doc, cs, step.getErrorMessage(), margin + 10f, y);
            }

            // Data tables
            if (step.getDataTable() != null && !step.getDataTable().isEmpty()) {
                y = addDataTables(doc, cs, step.getDataTable(), margin + 10f, y, box.getWidth() - 2 * margin - 10f);
            }

            // DocString
            if (step.getDocString() != null && step.getDocString().getContent() != null) {
                y = addDocString(doc, cs, step.getDocString().getContent(), margin + 10f, y, box.getWidth() - 2 * margin - 10f);
            }
        }

        // Screenshots: render zero, one, or many images under the textual step details
        List<String> screenshots = step.getScreenshotBase64List();
        if (!screenshots.isEmpty()) {
            for (String base64 : screenshots) {
                if (base64 == null || base64.isEmpty()) {
                    continue;
                }
                y = addScreenshot(doc, page, base64, margin + 10f, y - 4f, 400f);
                y -= 4f; // small spacer between multiple images
            }
        }

        return y;
    }

    private float addError(PDDocument doc, PDPageContentStream cs, String error,
                           float x, float y) throws IOException {
        String[] lines = error.split("\n");
        int displayLines = Math.min(lines.length, 3);
        for (int i = 0; i < displayLines; i++) {
            styler.drawText(doc, cs, lines[i], x, y, styler.monoFont(), 8f);
            y -= 10f;
        }
        if (lines.length > 3) {
            String more = "... + " + (lines.length - 3) + " more lines";
            styler.drawText(doc, cs, more, x, y, styler.monoFont(), 8f);
            y -= 10f;
        }
        return y;
    }

    private float addDataTables(PDDocument doc, PDPageContentStream cs,
                                List<CucumberDataTable> tables,
                                float x, float y, float maxWidth) throws IOException {
        for (CucumberDataTable t : tables) {
            List<CucumberTableRow> rows = t.getRows();
            if (rows == null || rows.isEmpty()) continue;
            // Draw simple CSV-style rows
            for (CucumberTableRow row : rows) {
                String joined = String.join(" | ", row.getCells());
                styler.drawText(doc, cs, joined, x, y, styler.monoFont(), 8f);
                y -= 10f;
            }
            y -= 6f;
        }
        return y;
    }

    private float addDocString(PDDocument doc, PDPageContentStream cs,
                               String content, float x, float y, float maxWidth) throws IOException {
        String[] lines = content.split("\r?\n");
        for (String line : lines) {
            styler.drawText(doc, cs, line, x, y, styler.monoFont(), 8f);
            y -= 10f;
        }
        y -= 6f;
        return y;
    }

    private float addScreenshot(PDDocument doc, PDPage page, String base64,
                                float x, float y, float targetWidth) throws IOException {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            PDImageXObject img = PDImageXObject.createFromByteArray(doc, bytes, "screenshot");
            float scale = targetWidth / img.getWidth();
            float height = img.getHeight() * scale;
            try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
                cs.drawImage(img, x, y - height, targetWidth, height);
            }
            return y - height - 6f;
        } catch (Exception e) {
            try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
                styler.drawText(doc, cs, "[Screenshot image failed to decode]", x, y, styler.regularFont(), 8f);
            }
            return y - 12f;
        }
    }
}
