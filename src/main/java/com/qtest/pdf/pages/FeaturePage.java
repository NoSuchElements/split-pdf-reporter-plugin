package com.qtest.pdf.pages;

import com.qtest.cucumber.model.CucumberFeature;
import com.qtest.cucumber.model.CucumberScenario;
import com.qtest.pdf.ColorScheme;
import com.qtest.pdf.PdfStyler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.util.List;

/**
 * Feature overview page.
 *
 * <p>Shows a feature "card" with URI and qTest tag, followed by per-scenario
 * mini progress bars (pass/fail/skip ratios). Kept intentionally simple so it
 * can be rendered reliably across many PDFs.</p>
 */
public class FeaturePage {

    private final PdfStyler styler;

    public FeaturePage(PdfStyler styler) {
        this.styler = styler;
    }

    public void build(PDDocument doc, PDPage page, CucumberFeature feature) throws IOException {
        PDRectangle box = page.getMediaBox();
        float margin = 40f;
        float y = box.getUpperRightY() - margin;

        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
            // Feature title
            String featureName = feature.getName() != null ? feature.getName() : "Unknown";
            styler.drawText(doc, cs, "Feature Overview", margin, y, styler.boldFont(), 16f);
            y -= 24f;

            // Feature card background
            float cardHeight = 80f;
            float cardWidth = box.getWidth() - 2 * margin;
            float cardY = y - cardHeight;
            styler.drawFilledRect(cs, margin, cardY, cardWidth, cardHeight, ColorScheme.BG_ROW_ALT);

            float textX = margin + 10f;
            float textY = y - 16f;

            // Name
            styler.drawText(doc, cs, featureName, textX, textY, styler.boldFont(), 12f);
            textY -= 14f;

            // URI
            if (feature.getUri() != null) {
                styler.drawText(doc, cs, "URI: " + feature.getUri(), textX, textY, styler.regularFont(), 9f);
                textY -= 12f;
            }

            // qTest tag (from feature tags)
            String qtestTag = feature.extractQtestTag();
            if (qtestTag != null && !"UNKNOWN".equals(qtestTag)) {
                styler.drawText(doc, cs, "qTest: " + qtestTag, textX, textY, styler.regularFont(), 9f);
                textY -= 12f;
            }

            // Other tags
            List<String> tags = feature.getTags();
            if (tags != null && !tags.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String t : tags) {
                    if (t == null) continue;
                    if (sb.length() > 0) sb.append(' ');
                    sb.append(t);
                }
                styler.drawText(doc, cs, "Tags: " + sb, textX, textY, styler.italicFont(), 8f);
            }

            // Overall step progress bar under the card
            float barY = cardY - 20f;
            drawFeatureProgressBar(cs, margin, barY, cardWidth, feature);

            // Per-scenario mini bars
            float scenariosTopY = barY - 30f;
            drawScenarioMiniBars(cs, margin, scenariosTopY, cardWidth, feature);
        }
    }

    private void drawFeatureProgressBar(PDPageContentStream cs,
                                        float x, float y, float width,
                                        CucumberFeature feature) throws IOException {
        int total = Math.max(1, feature.getTotalSteps());
        float passedRatio = feature.getPassedSteps() / (float) total;
        float failedRatio = feature.getFailedSteps() / (float) total;
        float skippedRatio = feature.getSkippedSteps() / (float) total;

        float passedWidth = width * passedRatio;
        float failedWidth = width * failedRatio;
        float skippedWidth = width * skippedRatio;

        float currentX = x;
        float height = 10f;

        if (passedWidth > 0) {
            styler.drawFilledRect(cs, currentX, y, passedWidth, height, ColorScheme.PASSED);
            currentX += passedWidth;
        }
        if (failedWidth > 0) {
            styler.drawFilledRect(cs, currentX, y, failedWidth, height, ColorScheme.FAILED);
            currentX += failedWidth;
        }
        if (skippedWidth > 0) {
            styler.drawFilledRect(cs, currentX, y, skippedWidth, height, ColorScheme.SKIPPED);
        }
    }

    private void drawScenarioMiniBars(PDPageContentStream cs,
                                      float x, float startY, float width,
                                      CucumberFeature feature) throws IOException {
        List<CucumberScenario> scenarios = feature.getScenarios();
        if (scenarios == null || scenarios.isEmpty()) return;

        float rowHeight = 12f;
        float barWidth = width * 0.5f;
        float nameWidth = width * 0.45f;
        float y = startY;

        for (CucumberScenario s : scenarios) {
            if (y < 60f) {
                // Avoid drawing off the bottom; this is a simple single-page overview
                break;
            }
            String name = s.getName() != null ? s.getName() : "Scenario";
            styler.drawText(null, cs, name, x, y, styler.regularFont(), 9f);

            int total = Math.max(1, s.getTotalSteps());
            float passedRatio = s.getPassedSteps() / (float) total;
            float failedRatio = s.getFailedSteps() / (float) total;
            float skippedRatio = s.getSkippedSteps() / (float) total;

            float barX = x + nameWidth;
            float barY = y - 4f;

            float passedWidth = barWidth * passedRatio;
            float failedWidth = barWidth * failedRatio;
            float skippedWidth = barWidth * skippedRatio;

            float currentX = barX;
            if (passedWidth > 0) {
                styler.drawFilledRect(cs, currentX, barY, passedWidth, 6f, ColorScheme.PASSED);
                currentX += passedWidth;
            }
            if (failedWidth > 0) {
                styler.drawFilledRect(cs, currentX, barY, failedWidth, 6f, ColorScheme.FAILED);
                currentX += failedWidth;
            }
            if (skippedWidth > 0) {
                styler.drawFilledRect(cs, currentX, barY, skippedWidth, 6f, ColorScheme.SKIPPED);
            }

            y -= rowHeight;
        }
    }
}
