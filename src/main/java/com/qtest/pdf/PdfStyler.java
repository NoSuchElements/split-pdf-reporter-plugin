package com.qtest.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;

/**
 * Styling utilities for PDF elements using Apache PDFBox.
 *
 * <p>This is intentionally simpler than the iText-based version: instead of
 * high-level layout elements (Paragraph, Cell, Table), we draw text and
 * simple boxes directly on the content stream. The higher-level page
 * builders (DashboardPage, SummaryPage, DetailedPage) orchestrate layout
 * using these primitives.</p>
 */
public class PdfStyler {

    // Fonts (built-in Type1 fonts, no external TTF, Apache-2.0 compatible)
    public PDType1Font regularFont() {
        return PDType1Font.HELVETICA;
    }

    public PDType1Font boldFont() {
        return PDType1Font.HELVETICA_BOLD;
    }

    public PDType1Font italicFont() {
        return PDType1Font.HELVETICA_OBLIQUE;
    }

    public PDType1Font monoFont() {
        return PDType1Font.COURIER;
    }

    /**
     * Draw a text string at (x, y) with the given font and size.
     *
     * <p>Sanitises control characters (\r, \n and other non-printables) to
     * avoid PDFBox WinAnsiEncoding errors when rendering stack traces or
     * platform-specific newlines.</p>
     */
    public void drawText(PDDocument doc, PDPageContentStream cs,
                         String text, float x, float y,
                         PDType1Font font, float fontSize) throws IOException {
        if (text == null) return;

        // Replace CR/LF and other control chars with spaces so they are
        // representable in the WinAnsiEncoding used by Type1 fonts.
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\r' || ch == '\n' || ch < 0x20) {
                sb.append(' ');
            } else {
                sb.append(ch);
            }
        }
        String safe = sb.toString();
        if (safe.isEmpty()) return;

        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(safe);
        cs.endText();
    }

    /**
     * Draw a colored rectangle (used for status badges, table headers, etc.).
     */
    public void drawFilledRect(PDPageContentStream cs,
                               float x, float y, float width, float height,
                               java.awt.Color awtColor) throws IOException {
        cs.setNonStrokingColor(awtColor);
        cs.addRect(x, y, width, height);
        cs.fill();
    }

    /**
     * Draw a status badge with white text on a colored background.
     */
    public void drawStatusBadge(PDPageContentStream cs,
                                String status,
                                float x, float y, float width, float height) throws IOException {
        String display = status != null ? status.toUpperCase() : "UNKNOWN";
        java.awt.Color bg = ColorScheme.getStatusColorAwt(status);
        drawFilledRect(cs, x, y, width, height, bg);

        // Centered text inside the badge
        cs.setNonStrokingColor(java.awt.Color.WHITE);
        float textX = x + 6;
        float textY = y + height / 2 - 4;
        drawText(null, cs, display, textX, textY, boldFont(), 10f);
        cs.setNonStrokingColor(java.awt.Color.BLACK);
    }

    /**
     * Draw a simple label/value pair on a single line.
     */
    public void drawLabelValue(PDPageContentStream cs,
                               String label, String value,
                               float x, float y) throws IOException {
        if (label != null) {
            drawText(null, cs, label, x, y, boldFont(), 10f);
        }
        if (value != null) {
            float valueX = x + 60;
            drawText(null, cs, value, valueX, y, regularFont(), 10f);
        }
    }
}
