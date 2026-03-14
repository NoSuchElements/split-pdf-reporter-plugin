package com.qtest.pdf;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.kernel.colors.Color;

import java.io.IOException;

/**
 * Styling utilities for PDF elements.
 * Creates fresh instances per use - no static state for thread safety.
 */
public class PdfStyler {

    private final PdfFont regularFont;
    private final PdfFont boldFont;
    private final PdfFont italicFont;
    private final PdfFont monoFont;

    /**
     * Initialize fonts for this PDF generation
     */
    public PdfStyler() throws IOException {
        this.regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        this.boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        this.italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
        this.monoFont = PdfFontFactory.createFont(StandardFonts.COURIER);
    }

    public PdfFont getRegularFont() {
        return regularFont;
    }

    public PdfFont getBoldFont() {
        return boldFont;
    }

    public PdfFont getItalicFont() {
        return italicFont;
    }

    public PdfFont getMonoFont() {
        return monoFont;
    }

    /**
     * Create header paragraph
     */
    public Paragraph createHeaderParagraph(String text) {
        return new Paragraph(text)
                .setFont(boldFont)
                .setFontSize(24)
                .setMarginBottom(10)
                .setTextAlignment(TextAlignment.CENTER);
    }

    /**
     * Create section title
     */
    public Paragraph createSectionTitle(String text) {
        return new Paragraph(text)
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(10)
                .setTextAlignment(TextAlignment.LEFT);
    }

    /**
     * Create body text
     */
    public Paragraph createBodyText(String text) {
        return new Paragraph(text)
                .setFont(regularFont)
                .setFontSize(11)
                .setMarginBottom(5);
    }

    /**
     * Create status badge cell
     */
    public Cell createStatusCell(String status) {
        String displayStatus = status != null ? status.toUpperCase() : "UNKNOWN";
        Cell cell = new Cell()
                .add(new Paragraph(displayStatus)
                        .setFont(boldFont)
                        .setFontSize(10)
                        .setFontColor(ColorScheme.TEXT_WHITE))
                .setBackgroundColor(ColorScheme.getStatusColor(status))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(5)
                .setBorder(Border.NO_BORDER);
        return cell;
    }

    /**
     * Create table header cell
     */
    public Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text)
                        .setFont(boldFont)
                        .setFontSize(11)
                        .setFontColor(ColorScheme.TEXT_WHITE))
                .setBackgroundColor(ColorScheme.BG_HEADER)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8)
                .setBorder(new SolidBorder(ColorScheme.BORDER, 0.5f));
    }

    /**
     * Create table data cell
     */
    public Cell createDataCell(String text, boolean alternate) {
        Cell cell = new Cell()
                .add(new Paragraph(text != null ? text : "")
                        .setFont(regularFont)
                        .setFontSize(10))
                .setTextAlignment(TextAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(6);

        if (alternate) {
            cell.setBackgroundColor(ColorScheme.BG_ROW_ALT);
        }
        cell.setBorder(new SolidBorder(ColorScheme.BORDER, 0.5f));
        return cell;
    }

    /**
     * Create data cell with center alignment
     */
    public Cell createDataCellCentered(String text, boolean alternate) {
        Cell cell = createDataCell(text, alternate);
        cell.setTextAlignment(TextAlignment.CENTER);
        return cell;
    }

    /**
     * Create error message paragraph (for failures)
     */
    public Paragraph createErrorParagraph(String errorMessage) {
        return new Paragraph(errorMessage)
                .setFont(monoFont)
                .setFontSize(9)
                .setFontColor(ColorScheme.FAILED)
                .setMarginTop(5)
                .setMarginBottom(5)
                .setMarginLeft(10)
                .setMarginRight(10);
    }

    /**
     * Create step line paragraph (keyword + name)
     */
    public Paragraph createStepParagraph(String keyword, String stepName) {
        Paragraph p = new Paragraph()
                .setFont(regularFont)
                .setFontSize(11)
                .setMarginBottom(3);

        if (keyword != null && !keyword.isEmpty()) {
            p.add(new com.itextpdf.layout.element.Text(keyword.trim() + " ")
                    .setFont(boldFont)
                    .setFontColor(ColorScheme.TEXT_SECONDARY));
        }

        if (stepName != null) {
            p.add(new com.itextpdf.layout.element.Text(stepName)
                    .setFont(regularFont)
                    .setFontColor(ColorScheme.TEXT_PRIMARY));
        }

        return p;
    }

    /**
     * Create meta information cell (smaller font)
     */
    public Paragraph createMetaText(String text) {
        return new Paragraph(text)
                .setFont(regularFont)
                .setFontSize(9)
                .setFontColor(ColorScheme.TEXT_SECONDARY)
                .setMarginBottom(3);
    }
}
