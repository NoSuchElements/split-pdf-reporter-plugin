package com.qtest.pdf;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.IOException;

/**
 * Generates donut charts for dashboard statistics.
 * Uses low-level PDF canvas operations - no external chart library dependency.
 */
public class PdfChartGenerator {

    private final PdfStyler styler;

    public PdfChartGenerator(PdfStyler styler) {
        this.styler = styler;
    }

    /**
     * Draw a donut chart on the PDF
     *
     * @param document PDF document
     * @param pageIndex page number
     * @param x X coordinate
     * @param y Y coordinate
     * @param radius radius of the donut
     * @param passed count of passed items
     * @param failed count of failed items
     * @param skipped count of skipped items
     * @param label chart title/label
     */
    public void drawDonutChart(PdfDocument document, int pageIndex,
                                float x, float y, float radius,
                                int passed, int failed, int skipped, String label) throws IOException {

        int total = passed + failed + skipped;
        if (total == 0) total = 1; // Avoid division by zero

        PdfCanvas canvas = new PdfCanvas(document.getPage(pageIndex));

        // Calculate angles
        float passedAngle = (passed * 360f) / total;
        float failedAngle = (failed * 360f) / total;
        float skippedAngle = (skipped * 360f) / total;

        // Draw donut segments
        drawDonutSegment(canvas, x, y, radius, 0, passedAngle, ColorScheme.PASSED);
        drawDonutSegment(canvas, x, y, radius, passedAngle, passedAngle + failedAngle, ColorScheme.FAILED);
        drawDonutSegment(canvas, x, y, radius, passedAngle + failedAngle, 360, ColorScheme.SKIPPED);

        // Draw legend
        float legendX = x + radius + 30;
        float legendY = y + 20;
        drawLegendItem(canvas, legendX, legendY, "Passed: " + passed, ColorScheme.PASSED);
        drawLegendItem(canvas, legendX, legendY - 20, "Failed: " + failed, ColorScheme.FAILED);
        drawLegendItem(canvas, legendX, legendY - 40, "Skipped: " + skipped, ColorScheme.SKIPPED);

        // Draw label in center
        Canvas layoutCanvas = new Canvas(canvas, new Rectangle(x - 20, y - 20, 40, 40));
        layoutCanvas.add(new Paragraph(label)
                .setFont(styler.getBoldFont())
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER));
    }

    /**
     * Draw a single segment of the donut
     */
    private void drawDonutSegment(PdfCanvas canvas, float cx, float cy, float radius,
                                   float startAngle, float endAngle, Color color) {
        // Convert angles to radians
        double start = Math.toRadians(startAngle);
        double end = Math.toRadians(endAngle);

        // Calculate points
        float x1 = (float) (cx + radius * Math.cos(start));
        float y1 = (float) (cy + radius * Math.sin(start));
        float x2 = (float) (cx + radius * Math.cos(end));
        float y2 = (float) (cy + radius * Math.sin(end));

        // Inner radius (for donut effect)
        float innerRadius = radius * 0.6f;
        float x3 = (float) (cx + innerRadius * Math.cos(end));
        float y3 = (float) (cy + innerRadius * Math.sin(end));
        float x4 = (float) (cx + innerRadius * Math.cos(start));
        float y4 = (float) (cy + innerRadius * Math.sin(start));

        canvas.setFillColor(color);
        canvas.setLineWidth(0);

        // Draw filled path
        canvas.moveTo(x1, y1);
        boolean largeArc = (endAngle - startAngle) > 180;
        canvas.arcTo(radius, radius, 0, largeArc ? 1 : 0, 1, x2, y2);
        canvas.lineTo(x3, y3);
        canvas.arcTo(innerRadius, innerRadius, 0, largeArc ? 1 : 0, 0, x4, y4);
        canvas.closePath();
        canvas.fill();
    }

    /**
     * Draw a legend item (colored square + text)
     */
    private void drawLegendItem(PdfCanvas canvas, float x, float y, String text, Color color) {
        // Draw colored square
        canvas.setFillColor(color);
        canvas.rectangle(x, y - 8, 10, 10);
        canvas.fill();

        // Draw text (would need Layout Canvas for proper text rendering)
        // This is a simplified version - for production use Canvas with Paragraph
    }
}
