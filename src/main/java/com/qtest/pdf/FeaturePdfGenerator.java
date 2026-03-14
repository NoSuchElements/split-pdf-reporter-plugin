package com.qtest.pdf;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.qtest.cucumber.model.CucumberFeature;
import com.qtest.cucumber.model.CucumberScenario;
import com.qtest.pdf.pages.DashboardPage;
import com.qtest.pdf.pages.DetailedPage;
import com.qtest.pdf.pages.SummaryPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Main PDF generator for a single Cucumber feature.
 * Creates a complete per-feature PDF with Dashboard, Summary, and Detailed pages.
 * Thread-safe: Each instance creates its own fonts and resources.
 */
public class FeaturePdfGenerator {
    private static final Logger logger = LoggerFactory.getLogger(FeaturePdfGenerator.class);

    private final PdfStyler styler;
    private final PdfChartGenerator chartGenerator;
    private final DashboardPage dashboardPage;
    private final SummaryPage summaryPage;
    private final DetailedPage detailedPage;

    /**
     * Initialize PDF generator with all resources
     */
    public FeaturePdfGenerator() throws IOException {
        // Create fresh instances - no static state
        this.styler = new PdfStyler();
        this.chartGenerator = new PdfChartGenerator(styler);
        this.dashboardPage = new DashboardPage(styler, chartGenerator);
        this.summaryPage = new SummaryPage(styler);
        this.detailedPage = new DetailedPage(styler);
        logger.debug("PDF Generator initialized with fresh style resources");
    }

    /**
     * Generate complete PDF for a feature
     */
    public void generateFeaturePdf(CucumberFeature feature, String outputFilePath) throws IOException {
        logger.info("Generating PDF for feature: {} -> {}", feature.getName(), outputFilePath);

        // Create output directory if needed
        File outputFile = new File(outputFilePath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create output directory: " + parentDir.getAbsolutePath());
            }
        }

        try {
            // Create PDF document
            PdfWriter writer = new PdfWriter(outputFilePath);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);

            // Create layout document
            Document document = new Document(pdfDoc);
            document.setMargins(40, 40, 40, 40);

            // Build pages
            dashboardPage.build(document, feature, pdfDoc);
            summaryPage.build(document, feature);
            buildDetailedPages(document, feature);

            // Close document
            document.close();
            pdfDoc.close();
            writer.close();

            logger.info("PDF generated successfully: {}", outputFilePath);
        } catch (FileNotFoundException e) {
            logger.error("Failed to create PDF file: {}", outputFilePath, e);
            throw new IOException("Cannot create PDF file at " + outputFilePath, e);
        }
    }

    /**
     * Build detailed pages for each scenario
     */
    private void buildDetailedPages(Document document, CucumberFeature feature) throws IOException {
        if (feature.getScenarios() == null || feature.getScenarios().isEmpty()) {
            document.add(new Paragraph("No scenarios found in this feature")
                    .setFont(styler.getRegularFont())
                    .setFontSize(11));
            return;
        }

        for (CucumberScenario scenario : feature.getScenarios()) {
            detailedPage.build(document, scenario);
        }
    }

    /**
     * Generate filename from feature with qTest tag
     * Format: featurename@QTEST_TC_XXXX.pdf
     */
    public static String generateFilename(CucumberFeature feature, String qtestCaseId) {
        String featureName = feature.getName() != null ? feature.getName() : "Unknown";
        // Sanitize feature name (remove special chars, replace spaces with underscores)
        featureName = featureName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return String.format("%s@%s.pdf", featureName, qtestCaseId);
    }
}
