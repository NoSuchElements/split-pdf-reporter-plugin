# Quick Start Guide

## Installation (5 minutes)

### 1. Add Plugin to pom.xml

Add this to your test project's `pom.xml` (inside `<build><plugins>`)

```xml
<plugin>
    <groupId>com.qtest</groupId>
    <artifactId>split-pdf-reporter-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <phase>post-integration-test</phase>
            <goals>
                <goal>generate-pdfs</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <cucumberJson>${project.build.directory}/cucumber.json</cucumberJson>
        <reportOutputDir>${project.build.directory}/cucumber-reports</reportOutputDir>
    </configuration>
</plugin>
```

### 2. Generate Cucumber JSON

Make sure your tests output JSON results. Example with Cucumber 7:

```java
@SpringBootTest
@CucumberContextConfiguration
public class CucumberTest {}
```

In `pom.xml` for Cucumber:

```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>7.x.x</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit</artifactId>
    <version>7.x.x</version>
    <scope>test</scope>
</dependency>
```

In your feature file folder `src/test/resources/features/`:

```gherkin
@QTEST_TC_12345
@Login
Feature: User Authentication
  
  Scenario: Successful login
    Given user opens login page
    When user enters valid credentials
    Then user sees dashboard
```

### 3. Run Tests

```bash
mvn clean verify
```

### 4. Find PDFs

Generated PDFs are in:

```
target/cucumber-reports/
├── User_Authentication@QTEST_TC_12345.pdf
└── [other features...]
```

## First Test Run

### Step 1: Create Feature File

`src/test/resources/features/example.feature`:

```gherkin
@QTEST_TC_99999
@Smoke
Feature: Sample Feature
  
  Scenario: First scenario
    Given a sample step
    When another action
    Then verify result
```

### Step 2: Create Step Definitions

`src/test/java/steps/SampleSteps.java`:

```java
import io.cucumber.java.en.*;
import static org.junit.Assert.*;

public class SampleSteps {
    
    @Given("a sample step")
    public void sampleStep() {
        System.out.println("Sample step executing");
    }
    
    @When("another action")
    public void anotherAction() {
        System.out.println("Another action executing");
    }
    
    @Then("verify result")
    public void verifyResult() {
        assertTrue(true);
    }
}
```

### Step 3: Create Test Runner

`src/test/java/runners/CucumberRunner.java`:

```java
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "steps",
    plugin = {"json:target/cucumber.json"},
    tags = "not @skip"
)
public class CucumberRunner {}
```

### Step 4: Run and Check

```bash
mvn clean verify
```

Expected output:

```
[INFO] ========================================
[INFO]    Split PDF Reporter Plugin v1.0.0
[INFO]    Per-Feature Cucumber PDF Generation
[INFO] ========================================
[INFO] Input: /path/to/target/cucumber.json
[INFO] Output: /path/to/target/cucumber-reports
[INFO] Parsed 1 features
[INFO] ✓ Generated: Sample_Feature@QTEST_TC_99999.pdf
[INFO] ========================================
[INFO]    Generation Summary
[INFO]    Success: 1
[INFO]    Failures: 0
[INFO] ========================================
```

Find your PDF at:

```
target/cucumber-reports/Sample_Feature@QTEST_TC_99999.pdf
```

## Using with Screenshots

To embed screenshots in PDFs:

### 1. Add Screenshot Attachment

```java
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class Hooks {
    private WebDriver driver;
    
    @After
    public void takeScreenshot(Scenario scenario) {
        if (scenario.isFailed() && driver != null) {
            byte[] screenshot = ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "Screenshot");
        }
    }
}
```

### 2. Screenshots Appear In PDF

Screenshots automatically embed in the detailed pages under each failed step.

## Configuration Examples

### Skip Plugin

```bash
mvn verify -DskipSplitPdfReporter=true
```

### Custom Output Directory

```bash
mvn verify -DreportOutputDir=/tmp/my-reports
```

### Custom JSON Location

```bash
mvn verify -DcucumberJson=/path/to/results.json
```

### Verbose Logging

```xml
<configuration>
    <verbose>true</verbose>
</configuration>
```

Then:

```bash
mvn verify
```

## Understanding PDF Pages

### Page 1: Dashboard

- Feature name with status badge
- Scenario statistics (total, passed, failed, skipped)
- Step statistics
- Distribution charts

### Page 2: Summary

- Table of all scenarios
- Status badge per scenario
- Passed/failed step counts
- Duration for each scenario
- Totals footer

### Pages 3+: Detailed

One page per scenario:

- Scenario name
- Each step with:
  - Keyword (Given/When/Then)
  - Step text
  - Status dot + duration
  - Error message (if failed)
  - DataTables (if present)
  - DocStrings (if present)
  - Screenshots (if embedded)

## Troubleshooting

### "Cucumber JSON file not found"

**Cause**: Test runner not configured to generate JSON

**Solution**: Add `plugin` to `@CucumberOptions`:

```java
@CucumberOptions(
    plugin = {"json:target/cucumber.json"}
)
```

### "No features found in Cucumber JSON"

**Cause**: JSON file is empty or tests didn't run

**Solution**: 
1. Check tests actually executed
2. Check `target/cucumber.json` exists and has content
3. Run with `-X` flag for debug info

### PDF has no content

**Cause**: Test parsing error

**Solution**:
1. Check JSON format matches Cucumber output
2. Enable verbose logging
3. Check feature has @QTEST_TC_XXXX tag

### Out of Memory

**Cause**: Too many large screenshots

**Solution**: Increase heap:

```bash
mvn clean verify -Xmx2g
```

## Next Steps

1. **Customize PDF styling** - Edit `ColorScheme.java` for colors
2. **Add new pages** - Create `NewPage.java` extending page builders
3. **Integrate with CI/CD** - Add PDF generation to pipeline
4. **Upload to qTest** - Use qTest API with PDF paths

## Example Project Structure

```
my-test-project/
├─ pom.xml                          (Maven config + plugin)
├─ src/
│  ├─ test/
│  │  ├─ java/
│  │  │  ├─ steps/
│  │  │  │  └─ SampleSteps.java
│  │  │  ├─ runners/
│  │  │  │  └─ CucumberRunner.java
│  │  │  └─ hooks/
│  │  │     └─ Hooks.java
│  │  └─ resources/
│  │     └─ features/
│  │        └─ example.feature
└─ target/
   ├─ cucumber.json         (Generated by test runner)
   └─ cucumber-reports/    (Generated PDFs)
      └─ Example@QTEST_TC_99999.pdf
```

## Getting Help

- Check [README.md](README.md) for full documentation
- Check [ARCHITECTURE.md](ARCHITECTURE.md) for technical details
- Review test cases in `src/test/` for examples
- Open GitHub issues for bugs/features

## Success!

If you see your PDF in `target/cucumber-reports/`, you're all set! ✅
