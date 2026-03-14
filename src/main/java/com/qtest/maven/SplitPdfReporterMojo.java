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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Maven Mojo for generating per-feature PDFs from Cucumber JSON results.
 *
 * <p>Bound to the {@code post-integration-test} phase by default.
 * All parameters are configurable via {@code <configuration>} block or
 * {@code -D} system properties on the command line.</p>
 *
 * <h3>Minimal configuration (single JSON)</h3>
 * <pre>{@code
 * <configuration>
 *   <cucumberJson>${project.build.directory}/cucumber.json</cucumberJson>
 *   <outputDirectory>${project.build.directory}/cucumber-reports</outputDirectory>
 * </configuration>
 * }</pre>
 *
 * <h3>Multi-module / glob pattern</h3>
 * <pre>{@code
 * <configuration>
 *   <cucumberJsonPattern>**&#47;cucumber*.json</cucumberJsonPattern>
 *   <outputDirectory>${project.build.directory}/cucumber-reports</outputDirectory>
 * </configuration>
 * }</pre>
 *
 * <h3>CLI overrides</h3>
 * <pre>
 *   mvn verify -DcucumberJson=path/to/results.json
 *   mvn verify -DcucumberJsonPattern=target/**&#47;*.json
 *   mvn verify -DreportOutputDir=target/my-pdfs
 *   mvn verify -DskipSplitPdfReporter=true
 * </pre>
 */
@Mojo(name = "generate-pdfs", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, threadSafe = true)
public class SplitPdfReporterMojo extends AbstractMojo {

    // -----------------------------------------------------------------------
    // Configurable parameters
    // -----------------------------------------------------------------------

    /**
     * Path to a single Cucumber JSON results file.
     * <p>Mutually exclusive with {@link #cucumberJsonPattern}. When both are
     * set, {@code cucumberJsonPattern} wins and this value is ignored.</p>
     *
     * <p>Override via CLI: {@code -DcucumberJson=target/my-results.json}</p>
     */
    @Parameter(
            property = "cucumberJson",
            defaultValue = "${project.build.directory}/cucumber.json"
    )
    private File cucumberJson;

    /**
     * Ant-style glob pattern relative to {@code ${project.basedir}} for
     * locating one or more Cucumber JSON files.
     * <p>Example: {@code target/**&#47;cucumber*.json}</p>
     *
     * <p>When set, overrides {@link #cucumberJson}.</p>
     *
     * <p>Override via CLI: {@code -DcucumberJsonPattern=target/**&#47;*.json}</p>
     */
    @Parameter(property = "cucumberJsonPattern")
    private String cucumberJsonPattern;

    /**
     * Directory where the generated PDF files are written.
     *
     * <p>Override via CLI: {@code -DreportOutputDir=/tmp/pdfs}</p>
     */
    @Parameter(
            property = "reportOutputDir",
            defaultValue = "${project.build.directory}/cucumber-reports"
    )
    private File outputDirectory;

    /**
     * Base directory used when resolving {@link #cucumberJsonPattern}.
     * Defaults to the Maven project base directory.
     */
    @Parameter(
            property = "project.basedir",
            defaultValue = "${project.basedir}",
            readonly = true
    )
    private File baseDir;

    /**
     * Skip plugin execution entirely.
     *
     * <p>Override via CLI: {@code -DskipSplitPdfReporter=true}</p>
     */
    @Parameter(property = "skipSplitPdfReporter", defaultValue = "false")
    private boolean skip;

    /**
     * Fail the build when no Cucumber JSON input files are found.
     * Set to {@code false} to treat missing JSON as a warning (useful in
     * optional-module setups where some modules may not run integration tests).
     *
     * <p>Override via CLI: {@code -DfailOnNoFeatures=false}</p>
     */
    @Parameter(property = "failOnNoFeatures", defaultValue = "true")
    private boolean failOnNoFeatures;

    /**
     * Emit extra debug information about each feature and scenario.
     *
     * <p>Override via CLI: {@code -Dverbose=true}</p>
     */
    @Parameter(property = "verbose", defaultValue = "false")
    private boolean verbose;

    // -----------------------------------------------------------------------
    // Execution
    // -----------------------------------------------------------------------

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping Split PDF Reporter (skipSplitPdfReporter=true)");
            return;
        }

        banner();

        // 1. Resolve input JSON file(s)
        List<File> jsonFiles = resolveInputFiles();

        if (jsonFiles.isEmpty()) {
            String msg = "No Cucumber JSON file(s) found. "
                    + (cucumberJsonPattern != null
                    ? "Pattern: " + cucumberJsonPattern
                    : "File: " + cucumberJson.getAbsolutePath());
            if (failOnNoFeatures) {
                throw new MojoExecutionException(msg + "  Set failOnNoFeatures=false to downgrade to a warning.");
            }
            getLog().warn(msg);
            getLog().warn("Skipping PDF generation.");
            return;
        }

        // 2. Ensure output directory exists
        ensureOutputDirectory();

        // 3. Parse + generate
        CucumberJsonParser parser = new CucumberJsonParser(verbose);
        int successCount = 0;
        int failureCount = 0;

        for (File jsonFile : jsonFiles) {
            getLog().info("Input : " + jsonFile.getAbsolutePath());

            List<CucumberFeature> features;
            try {
                features = parser.parseJsonFile(jsonFile.getAbsolutePath());
            } catch (IOException e) {
                getLog().error("Failed to parse " + jsonFile.getName() + ": " + e.getMessage(), e);
                failureCount++;
                continue;
            }

            if (features == null || features.isEmpty()) {
                getLog().warn("No features found in: " + jsonFile.getName());
                continue;
            }

            getLog().info("Parsed " + features.size() + " feature(s) from " + jsonFile.getName());

            for (CucumberFeature feature : features) {
                try {
                    String qtestCaseId = parser.extractQtestCaseId(feature);
                    String filename    = FeaturePdfGenerator.generateFilename(feature, qtestCaseId);
                    String outputPath  = outputDirectory.getAbsolutePath() + File.separator + filename;

                    if (verbose) {
                        getLog().info("  → Feature  : " + feature.getName());
                        getLog().info("  → qTest ID : " + qtestCaseId);
                        getLog().info("  → File     : " + filename);
                    }

                    FeaturePdfGenerator generator = new FeaturePdfGenerator();
                    generator.generateFeaturePdf(feature, outputPath);

                    getLog().info("✓ Generated : " + filename);
                    successCount++;
                } catch (IOException e) {
                    String name = feature.getName() != null ? feature.getName() : "<unnamed>";
                    getLog().error("✗ Failed    : " + name + " — " + e.getMessage(), e);
                    failureCount++;
                }
            }
        }

        // 4. Summary
        getLog().info("");
        getLog().info("==============================");
        getLog().info("  Generation Summary");
        getLog().info("  Output : " + outputDirectory.getAbsolutePath());
        getLog().info("  ✓ Success : " + successCount);
        getLog().info("  ✗ Failures: " + failureCount);
        getLog().info("==============================");

        if (failureCount > 0) {
            throw new MojoFailureException(
                    "PDF generation completed with " + failureCount + " failure(s). See log above.");
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Resolves the list of JSON files to process.
     * Priority: cucumberJsonPattern > cucumberJson (single file).
     */
    private List<File> resolveInputFiles() throws MojoExecutionException {
        if (cucumberJsonPattern != null && !cucumberJsonPattern.isBlank()) {
            return resolveGlob(cucumberJsonPattern);
        }
        // Single-file mode
        if (cucumberJson != null && cucumberJson.exists() && cucumberJson.isFile()) {
            return List.of(cucumberJson);
        }
        return List.of();
    }

    /**
     * Walks {@code baseDir} collecting files that match the given Ant-style glob.
     */
    private List<File> resolveGlob(String pattern) throws MojoExecutionException {
        Path base = baseDir.toPath();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        List<File> found = new ArrayList<>();
        try {
            Files.walkFileTree(base, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    Path rel = base.relativize(file);
                    if (matcher.matches(rel)) {
                        found.add(file.toFile());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new MojoExecutionException("Error scanning for Cucumber JSON files: " + e.getMessage(), e);
        }
        if (verbose) {
            getLog().info("Glob '" + pattern + "' matched " + found.size() + " file(s) under " + base);
        }
        return found;
    }

    private void ensureOutputDirectory() throws MojoExecutionException {
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new MojoExecutionException(
                    "Failed to create output directory: " + outputDirectory.getAbsolutePath());
        }
    }

    private void banner() {
        getLog().info("========================================");
        getLog().info("  Split PDF Reporter Plugin v1.0.0");
        getLog().info("  Per-Feature Cucumber PDF Generation");
        getLog().info("========================================");
        getLog().info("  cucumberJson        : " + (cucumberJson != null ? cucumberJson.getAbsolutePath() : "(none)"));
        getLog().info("  cucumberJsonPattern : " + (cucumberJsonPattern != null ? cucumberJsonPattern : "(none)"));
        getLog().info("  outputDirectory     : " + outputDirectory.getAbsolutePath());
        getLog().info("  failOnNoFeatures    : " + failOnNoFeatures);
        getLog().info("  verbose             : " + verbose);
        getLog().info("========================================");
    }
}
