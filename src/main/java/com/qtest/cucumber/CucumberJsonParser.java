package com.qtest.cucumber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.qtest.cucumber.model.CucumberFeature;
import com.qtest.cucumber.model.CucumberScenario;
import com.qtest.cucumber.model.CucumberStep;
import com.qtest.cucumber.model.CucumberStepResult;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses a Cucumber JSON report file into a list of {@link CucumberFeature} objects.
 *
 * <p>Stateless — safe to call {@link #parseJsonFile} multiple times on the same
 * instance (e.g. when the glob pattern matched several JSON files).</p>
 */
public class CucumberJsonParser {

    private static final String QTEST_TAG_PREFIX = "@QTEST_TC_";

    private final Gson gson;
    private final boolean verbose;

    public CucumberJsonParser() {
        this(false);
    }

    public CucumberJsonParser(boolean verbose) {
        this.verbose  = verbose;
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .create();
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Parse a Cucumber JSON file.
     *
     * @param jsonFilePath absolute or relative path to the JSON file
     * @return non-null, possibly-empty list of features
     * @throws IOException if the file cannot be read
     */
    public List<CucumberFeature> parseJsonFile(String jsonFilePath) throws IOException {
        if (verbose) {
            System.out.println("[SplitPdfReporter] Parsing: " + jsonFilePath);
        }

        String content = readFile(jsonFilePath);

        if (content == null || content.isBlank()) {
            System.out.println("[SplitPdfReporter] WARN  : JSON file is empty — " + jsonFilePath);
            return new ArrayList<>();
        }

        List<CucumberFeature> features;
        try {
            Type type = new TypeToken<List<CucumberFeature>>() {}.getType();
            features  = gson.fromJson(content, type);
        } catch (JsonSyntaxException e) {
            throw new IOException("Malformed Cucumber JSON in '" + jsonFilePath + "': " + e.getMessage(), e);
        }

        // Gson can return null for a literal JSON null
        if (features == null) {
            System.out.println("[SplitPdfReporter] WARN  : JSON parsed to null — " + jsonFilePath);
            return new ArrayList<>();
        }

        for (CucumberFeature feature : features) {
            // Null-safe statistics and tag extraction are handled downstream
            calculateFeatureStatistics(feature);
        }

        if (verbose) {
            System.out.println("[SplitPdfReporter] Parsed " + features.size() + " feature(s)");
        }
        return features;
    }

    /**
     * Extract the qTest case ID ({@code QTEST_TC_XXXX}) from a feature.
     *
     * <p>Search order:
     * <ol>
     *   <li>Feature-level tags</li>
     *   <li>Scenario-level tags</li>
     * </ol>
     * Returns {@code "UNKNOWN"} when no tag is found.
     * </p>
     */
    public String extractQtestCaseId(CucumberFeature feature) {
        // 1. Feature-level tags
        String id = findQtestTag(feature.getTags());
        if (id != null) return id;

        // 2. Scenario-level tags (fallback)
        List<CucumberScenario> scenarios = feature.getScenarios();
        if (scenarios != null) {
            for (CucumberScenario scenario : scenarios) {
                id = findQtestTag(scenario.getTags());
                if (id != null) return id;
            }
        }
        return "UNKNOWN";
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /** Returns the tag value (without leading @) or null if not found. */
    private String findQtestTag(List<String> tags) {
        if (tags == null) return null;
        for (String tag : tags) {
            // Tags may arrive as "@QTEST_TC_1234" or "QTEST_TC_1234"
            String normalised = tag.startsWith("@") ? tag.substring(1) : tag;
            if (normalised.startsWith("QTEST_TC_")) {
                return normalised;
            }
        }
        return null;
    }

    private String readFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString().trim();
    }

    private void calculateFeatureStatistics(CucumberFeature feature) {
        int totalScenarios   = 0;
        int passedScenarios  = 0;
        int failedScenarios  = 0;
        int skippedScenarios = 0;
        int totalSteps   = 0;
        int passedSteps  = 0;
        int failedSteps  = 0;
        int skippedSteps = 0;

        List<CucumberScenario> scenarios = feature.getScenarios();
        if (scenarios == null) {
            scenarios = Collections.emptyList();
        }

        for (CucumberScenario scenario : scenarios) {
            totalScenarios++;
            int sPass = 0, sFail = 0, sSkip = 0;

            for (CucumberStep step : scenario.getSteps()) {
                totalSteps++;
                CucumberStepResult result = step.getResult();
                if (result == null) continue;
                if (result.isFailed())  { failedSteps++;  sFail++; }
                else if (result.isSkipped()) { skippedSteps++; sSkip++; }
                else if (result.isPassed())  { passedSteps++;  sPass++; }
            }

            scenario.setTotalSteps(scenario.getSteps().size());
            scenario.setPassedSteps(sPass);
            scenario.setFailedSteps(sFail);
            scenario.setSkippedSteps(sSkip);

            if (sFail > 0)       failedScenarios++;
            else if (sSkip > 0)  skippedScenarios++;
            else                 passedScenarios++;
        }

        feature.setTotalScenarios(totalScenarios);
        feature.setPassedScenarios(passedScenarios);
        feature.setFailedScenarios(failedScenarios);
        feature.setSkippedScenarios(skippedScenarios);
        feature.setTotalSteps(totalSteps);
        feature.setPassedSteps(passedSteps);
        feature.setFailedSteps(failedSteps);
        feature.setSkippedSteps(skippedSteps);
    }
}
