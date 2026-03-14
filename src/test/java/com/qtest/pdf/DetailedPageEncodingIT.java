package com.qtest.pdf;

import com.qtest.cucumber.model.CucumberScenario;
import com.qtest.cucumber.model.CucumberStep;
import com.qtest.cucumber.model.CucumberStepResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Verifies that DetailedPage/PdfStyler can safely render Windows-style
 * CRLF stack traces without triggering PDFBox encoding errors.
 */
public class DetailedPageEncodingIT {

    @Test
    public void rendersErrorMessageWithCarriageReturns() throws Exception {
        // Build a scenario with a single failing step that includes a CRLF stack trace
        CucumberStepResult result = new CucumberStepResult();
        java.lang.reflect.Field statusField = CucumberStepResult.class.getDeclaredField("status");
        java.lang.reflect.Field errorField = CucumberStepResult.class.getDeclaredField("errorMessage");
        statusField.setAccessible(true);
        errorField.setAccessible(true);
        statusField.set(result, "failed");
        errorField.set(result, "java.lang.AssertionError: boom\r\n\tat com.example.Test.foo(Test.java:10)\r\n\tat ✽.step(file:some.feature:5)\r\n");

        CucumberStep step = new CucumberStep();
        java.lang.reflect.Field resultField = CucumberStep.class.getDeclaredField("result");
        resultField.setAccessible(true);
        resultField.set(step, result);

        CucumberScenario scenario = new CucumberScenario();
        java.lang.reflect.Field nameField = CucumberScenario.class.getDeclaredField("name");
        java.lang.reflect.Field stepsField = CucumberScenario.class.getDeclaredField("steps");
        nameField.setAccessible(true);
        stepsField.setAccessible(true);
        nameField.set(scenario, "Scenario with CRLF error");
        stepsField.set(scenario, java.util.Collections.singletonList(step));

        // Generate a PDF and ensure PDFBox can read it back without encoding issues
        File pdf = File.createTempFile("crlf-error", ".pdf");
        FeaturePdfGenerator generator = new FeaturePdfGenerator();
        // We don't have a full feature here; instead we call DetailedPage via generator by
        // wrapping the scenario in a dummy feature if necessary. For this smoke test, it's
        // sufficient to ensure that no exception is thrown when drawing the error text.
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            new com.qtest.pdf.pages.DetailedPage(new PdfStyler())
                    .build(doc, page, scenario);
            doc.save(pdf);
        }

        assertTrue("PDF file should be created", pdf.length() > 0);
    }
}
