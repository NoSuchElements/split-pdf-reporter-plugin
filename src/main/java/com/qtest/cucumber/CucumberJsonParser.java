package com.qtest.cucumber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.qtest.cucumber.model.CucumberFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Parser for Cucumber JSON test results.
 * Immutable - creates fresh Gson instance per parse.
 */
public class CucumberJsonParser {
    private static final Logger logger = LoggerFactory.getLogger(CucumberJsonParser.class);

    private final Gson gson;

    public CucumberJsonParser() {
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .create();
    }

    /**
     * Parse Cucumber JSON file
     */
    public List<CucumberFeature> parseJsonFile(String jsonFilePath) throws IOException {
        logger.info("Parsing Cucumber JSON: {}", jsonFilePath);

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }

        Type featureListType = new TypeToken<List<CucumberFeature>>(){}.getType();
        List<CucumberFeature> features = gson.fromJson(content.toString(), featureListType);

        // Calculate statistics
        for (CucumberFeature feature : features) {
            calculateFeatureStatistics(feature);
        }

        logger.info("Parsed {} features", features.size());
        return features;
    }

    /**
     * Calculate statistics for a feature based on its scenarios and steps
     */
    private void calculateFeatureStatistics(CucumberFeature feature) {
        if (feature.getScenarios() == null) {
            feature.setScenarios(new ArrayList<>());
        }

        int totalScenarios = feature.getScenarios().size();
        int totalSteps = 0;
        int passedScenarios = 0;
        int failedScenarios = 0;
        int skippedScenarios = 0;
        int passedSteps = 0;
        int failedSteps = 0;
        int skippedSteps = 0;

        for (CucumberFeature.CucumberScenarioWrapper scenario : feature.getScenarios()) {
            CucumberScenario innerScenario = scenario; // Unwrap if needed
            // Calculate scenario stats
            int scenarioFailed = 0;
            int scenarioSkipped = 0;
            int scenarioPassed = 0;

            if (innerScenario.getSteps() != null) {
                for (CucumberStep step : innerScenario.getSteps()) {
                    totalSteps++;
                    if (step.getResult() != null) {
                        if (step.getResult().isFailed()) {
                            failedSteps++;
                            scenarioFailed++;
                        } else if (step.getResult().isSkipped()) {
                            skippedSteps++;
                            scenarioSkipped++;
                        } else if (step.getResult().isPassed()) {
                            passedSteps++;
                            scenarioPassed++;
                        }
                    }
                }
            }

            // Determine scenario status based on steps
            if (scenarioFailed > 0) {
                failedScenarios++;
            } else if (scenarioSkipped > 0) {
                skippedScenarios++;
            } else {
                passedScenarios++;
            }

            // Set scenario stats
            innerScenario.setTotalSteps(innerScenario.getSteps() != null ? innerScenario.getSteps().size() : 0);
            innerScenario.setPassedSteps(scenarioPassed);
            innerScenario.setFailedSteps(scenarioFailed);
            innerScenario.setSkippedSteps(scenarioSkipped);
        }

        feature.setTotalScenarios(totalScenarios);
        feature.setPassedScenarios(passedScenarios);
        feature.setFailedScenarios(failedScenarios);
        feature.setSkippedScenarios(skippedScenarios);
        feature.setTotalSteps(totalSteps);
        feature.setPassedSteps(passedSteps);
        feature.setFailedSteps(failedSteps);
        feature.setSkippedSteps(skippedSteps);

        logger.debug("Feature {}: Scenarios={}/{}/{} Steps={}/{}/{}",
                feature.getName(), passedScenarios, failedScenarios, skippedScenarios,
                passedSteps, failedSteps, skippedSteps);
    }

    /**
     * Extract qTest case ID from feature tags
     */
    public String extractQtestCaseId(CucumberFeature feature) {
        if (feature.getTags() != null) {
            for (String tag : feature.getTags()) {
                if (tag.startsWith("@QTEST_TC_")) {
                    return tag.substring(1); // Remove @
                }
            }
        }
        return "UNKNOWN";
    }
}
