package com.qtest.pdf;

import com.qtest.cucumber.model.CucumberFeature;
import com.qtest.cucumber.model.CucumberScenario;
import com.qtest.pdf.pages.DetailedPage;
import com.qtest.pdf.pages.FeaturePage;
import com.qtest.pdf.pages.SummaryPage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Main PDF generator for a single Cucumber feature.
 *
 * <p>PDFBox-based implementation. Each instance creates its own PDDocument and
 * styling helpers; no static fonts or shared state.</p>
 */
public class FeaturePdfGenerator {
    private static final Logger logger = LoggerFactory.getLogger(FeaturePdfGenerator.class);

    private final PdfStyler styler;
    private final SummaryPage summaryPage;
    private final FeaturePage featurePage;
    private final DetailedPage detailedPage;

    // Section toggles (allow future configuration while keeping sensible defaults)
    private final boolean includeSummaryPage;
    private final boolean includeFeaturePage;
    private final boolean includeDetailedPages;

    public FeaturePdfGenerator() throws IOException {
        this(true, true, true);
    }

    public FeaturePdfGenerator(boolean includeSummaryPage,
                               boolean includeFeaturePage,
                               boolean includeDetailedPages) throws IOException {
        this.styler = new PdfStyler();
        this.summaryPage = new SummaryPage(styler);
        this.featurePage = new FeaturePage(styler);
        this.detailedPage = new DetailedPage(styler);
        this.includeSummaryPage = includeSummaryPage;
        this.includeFeaturePage = includeFeaturePage;
        this.includeDetailedPages = includeDetailedPages;
    }

    /**
     * Generate a complete PDF for the given feature.
     */
    public void generateFeaturePdf(CucumberFeature feature, String outputFilePath) throws IOException {
        logger.info("Generating PDF (PDFBox) for feature: {} -> {}", feature.getName(), outputFilePath);

        File outFile = new File(outputFilePath);
        File parent = outFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create output directory: " + parent.getAbsolutePath());
        }

        try (PDDocument document = new PDDocument(); FileOutputStream ignored = new FileOutputStream(outFile)) {
            // Page 1: Summary
            if (includeSummaryPage) {
                PDPage summaryPagePd = new PDPage(PDRectangle.A4);
                document.addPage(summaryPagePd);
                summaryPage.build(document, summaryPagePd, feature);
            }

            // Page 2: Feature overview
            if (includeFeaturePage) {
                PDPage featurePagePd = new PDPage(PDRectangle.A4);
                document.addPage(featurePagePd);
                featurePage.build(document, featurePagePd, feature);
            }

            // Pages N+: Detailed scenarios
            if (includeDetailedPages) {
                if (feature.getScenarios() == null || feature.getScenarios().isEmpty()) {
                    // Single empty detailed page to state no scenarios
                    PDPage detailed = new PDPage(PDRectangle.A4);
                    document.addPage(detailed);
                    detailedPage.buildEmpty(document, detailed);
                } else {
                    for (CucumberScenario scenario : feature.getScenarios()) {
                        PDPage detailed = new PDPage(PDRectangle.A4);
                        document.addPage(detailed);
                        detailedPage.build(document, detailed, scenario);
                    }
                }
            }

            document.save(outFile);
        }

        logger.info("PDF generated successfully: {}", outputFilePath);
    }

    /**
     * Generate filename from feature with qTest tag.
     * Format: featurename@QTEST_TC_XXXX.pdf
     */
    public static String generateFilename(CucumberFeature feature, String qtestCaseId) {
        String featureName = feature.getName() != null ? feature.getName() : "Unknown";
        featureName = featureName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return featureName + "@" + qtestCaseId + ".pdf";
    }
}
