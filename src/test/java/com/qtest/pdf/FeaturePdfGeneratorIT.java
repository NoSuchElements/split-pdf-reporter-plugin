package com.qtest.pdf;

import com.qtest.cucumber.CucumberJsonParser;
import com.qtest.cucumber.model.CucumberFeature;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * End-to-end test: parse a small Cucumber JSON, generate PDFs, and verify
 * that expected text content appears in the output documents.
 */
public class FeaturePdfGeneratorIT {

    private static final String SAMPLE_JSON = "[" +
            "{" +
            "\"line\":2," +
            "\"elements\":[{" +
            "  \"line\":4," +
            "  \"name\":\"Simple scenario\"," +
            "  \"id\":\"simple-feature;simple-scenario\"," +
            "  \"type\":\"scenario\"," +
            "  \"keyword\":\"Scenario\"," +
            "  \"steps\":[{" +
            "    \"result\":{\"status\":\"passed\",\"duration\":1000000}," +
            "    \"line\":5," +
            "    \"name\":\"a passing step\"," +
            "    \"keyword\":\"Given \"" +
            "  }]," +
            "  \"tags\":[{\"name\":\"@QTEST_TC_9999\"}]" +
            "}]," +
            "\"name\":\"Simple feature\"," +
            "\"id\":\"simple-feature\"," +
            "\"keyword\":\"Feature\"," +
            "\"uri\":\"file:src/test/resources/features/simple.feature\"," +
            "\"tags\":[{\"name\":\"@QTEST_TC_9999\"}]" +
            "}" +
            "]";

    @Test
    public void generatesPdfWithExpectedContent() throws Exception {
        // 1) Write JSON to a temp file
        File jsonFile = Files.createTempFile("cucumber-sample", ".json").toFile();
        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(SAMPLE_JSON);
        }

        // 2) Parse features
        CucumberJsonParser parser = new CucumberJsonParser(false);
        List<CucumberFeature> features = parser.parseJsonFile(jsonFile.getAbsolutePath());
        assertFalse("No features parsed from sample JSON", features.isEmpty());

        CucumberFeature feature = features.get(0);
        assertFalse("Feature should have at least one scenario", feature.getScenarios() == null || feature.getScenarios().isEmpty());

        // 3) Generate PDF
        File pdfFile = Files.createTempFile("simple-feature", ".pdf").toFile();
        FeaturePdfGenerator generator = new FeaturePdfGenerator();
        generator.generateFeaturePdf(feature, pdfFile.getAbsolutePath());

        // 4) Extract text and verify key content
        try (PDDocument doc = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            assertTrue("PDF should contain feature name", text.contains("Simple feature"));
            assertTrue("PDF should contain scenario name", text.contains("Simple scenario"));
            assertTrue("PDF should contain step description", text.contains("a passing step"));
            assertTrue("PDF should contain status", text.toLowerCase().contains("passed"));
        }
    }
}
