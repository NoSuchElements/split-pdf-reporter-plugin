package com.qtest.maven;

import com.qtest.cucumber.CucumberJsonParser;
import com.qtest.cucumber.model.CucumberFeature;
import com.qtest.pdf.FeaturePdfGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Maven Mojo for generating per-feature PDFs from Cucumber JSON results.
 * Execution: post-integration-test phase
 * Binds to: split-pdf-reporter:generate-pdfs
 */
@Mojo(name = "generate-pdfs", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class SplitPdfReporterMojo extends AbstractMojo {
    private static final Logger logger = LoggerFactory.getLogger(SplitPdfReporterMojo.class);

    /**
     * Location of Cucumber JSON results (default: target/cucumber.json)
     */
    @Parameter(
            property = "cucumber.json",
            defaultValue = "${project.build.directory}/cucumber.json",
            required = true
    )
    private File cucumberJson;

    /**
     * Output directory for PDF reports (default: target/cucumber-reports)
     */
    @Parameter(
            property = "reportOutputDir",
            defaultValue = "${project.build.directory}/cucumber-reports",
            required = true
    )
    private File outputDirectory;

    /**
     * Skip plugin execution
     */
    @Parameter(
            property = "skipSplitPdfReporter",
            defaultValue = "false"
    )
    private boolean skip;

    /**
     * Verbose logging
     */
    @Parameter(
            property = "verbose",
            defaultValue = "false"
    )
    private boolean verbose;

    /**
     * Execute the Mojo
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping Split PDF Reporter generation (skipSplitPdfReporter=true)");
            return;
        }

        try {
            getLog().info("========================================");
            getLog().info("   Split PDF Reporter Plugin v1.0.0");
            getLog().info("   Per-Feature Cucumber PDF Generation");
            getLog().info("========================================");

            // Validate input
            if (!cucumberJson.exists()) {
                getLog().warn("Cucumber JSON file not found: " + cucumberJson.getAbsolutePath());
                getLog().warn("Skipping PDF generation");
                return;
            }

            getLog().info("Input: " + cucumberJson.getAbsolutePath());
            getLog().info("Output: " + outputDirectory.getAbsolutePath());

            // Create output directory
            if (!outputDirectory.exists()) {
                if (!outputDirectory.mkdirs()) {
                    throw new MojoExecutionException("Failed to create output directory: " + outputDirectory.getAbsolutePath());
                }
            }

            // Parse Cucumber JSON
            CucumberJsonParser parser = new CucumberJsonParser();
            List<CucumberFeature> features = parser.parseJsonFile(cucumberJson.getAbsolutePath());
            getLog().info("Parsed " + features.size() + " features");

            if (features.isEmpty()) {
                getLog().warn("No features found in Cucumber JSON");
                return;
            }

            // Generate PDF for each feature
            int successCount = 0;
            int failureCount = 0;

            for (CucumberFeature feature : features) {
                try {
                    // Extract qTest case ID from @QTEST_TC_XXXX tag
                    String qtestCaseId = parser.extractQtestCaseId(feature);
                    String filename = FeaturePdfGenerator.generateFilename(feature, qtestCaseId);
                    String outputPath = outputDirectory.getAbsolutePath() + File.separator + filename;

                    // Generate PDF
                    FeaturePdfGenerator generator = new FeaturePdfGenerator();
                    generator.generateFeaturePdf(feature, outputPath);

                    getLog().info("✓ Generated: " + filename);
                    successCount++;
                } catch (IOException e) {
                    getLog().error("✗ Failed to generate PDF for feature: " + (feature.getName() != null ? feature.getName() : "Unknown"), e);
                    failureCount++;
                }
            }

            // Summary
            getLog().info("");
            getLog().info("========================================");
            getLog().info("   Generation Summary");
            getLog().info("   Success: " + successCount);
            getLog().info("   Failures: " + failureCount);
            getLog().info("========================================");

            if (failureCount > 0) {
                throw new MojoFailureException("PDF generation completed with " + failureCount + " failure(s)");
            }

        } catch (IOException e) {
            throw new MojoExecutionException("Error processing Cucumber JSON: " + e.getMessage(), e);
        }
    }
}
