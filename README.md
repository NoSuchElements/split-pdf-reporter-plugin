# Split PDF Reporter Plugin

A Maven plugin for generating **per-feature PDF reports** from Cucumber JSON test results. Each feature gets its own PDF file, eliminating the font corruption bugs of grasshopper7 through complete architectural rewrite using **iText 7**.

## Problem Solved

The grasshopper7 `cucumber-pdf-report` library has a **fundamental design flaw**: its `ReportFont` class uses static fields for font management. This breaks multi-PDF generation in a single JVM:

- **Static Font State**: Once a font's InputStream is consumed, it cannot be reused
- **Stream Exhaustion**: After first PDF, subsequent PDFs fail with corrupted fonts
- **Unfixable**: Reflection hacks don't work - the stream is already closed

**This plugin solves it completely** by:
- Using **iText 7** (industry standard, stateless design)
- Creating **fresh font instances per PDF** (no static state)
- Generating **one PDF per feature file** (isolated resources)
- Running **reliably across 100+ PDFs** in single JVM

## Features

✅ **Per-Feature PDFs** - One PDF file per Cucumber feature  
✅ **qTest Integration** - Filename includes @QTEST_TC_XXXX tag  
✅ **No Font Corruption** - Complete iText 7 rewrite, stateless design  
✅ **Rich Content** - Dashboard, summary tables, detailed steps with screenshots  
✅ **Embedded Screenshots** - Base64 images rendered inline  
✅ **Error Details** - Failed steps show first 3 lines + overflow count  
✅ **DataTables & DocStrings** - Formatted appropriately in PDF  
✅ **Thread-Safe** - Each PDF gets its own resource instances  
✅ **Maven Integration** - Post-integration-test phase hook  
✅ **Zero Dependencies** - Only iText 7, Gson, SLF4J  

## Installation

### Add to pom.xml

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
        <verbose>false</verbose>
    </configuration>
</plugin>
```

## Usage

### Basic

```bash
mvn clean verify
```

PDFs are generated automatically in `target/cucumber-reports/` after tests complete.

### Configuration

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `cucumberJson` | File | `${project.build.directory}/cucumber.json` | Location of Cucumber JSON file |
| `reportOutputDir` | File | `${project.build.directory}/cucumber-reports` | Output directory for PDFs |
| `skip` | Boolean | `false` | Skip plugin execution |
| `verbose` | Boolean | `false` | Enable verbose logging |

### Command-Line Override

```bash
mvn verify -DcucumberJson=/path/to/results.json -DreportOutputDir=/path/to/pdfs
```

### Skip Plugin

```bash
mvn verify -DskipSplitPdfReporter=true
```

## PDF Format

Each feature PDF contains:

### Page 1: Dashboard
- Feature name with overall status badge (PASSED/FAILED/SKIPPED)
- Scenario & step statistics
- Distribution charts (passed/failed/skipped percentages)

### Page 2: Summary
- Table of all scenarios
- Scenario name, status badge, passed/failed step counts, duration
- Totals footer with aggregate statistics

### Pages 3+: Detailed
- **One page per scenario** showing:
  - Scenario name, tags, duration, status
  - Every step with:
    - Keyword (Given/When/Then) + step name
    - Colored status dot (● PASSED/FAILED/SKIPPED)
    - Duration in milliseconds
    - Error message (red text, first 3 lines + overflow count)
    - DataTables (formatted with headers and alternating row colors)
    - DocStrings (mono-spaced code blocks)
    - **Embedded screenshots** (base64 decoded & rendered inline)

## Filename Format

PDFs are named using the feature name + qTest case ID:

```
featurename@QTEST_TC_12345.pdf
```

**Example**: If your feature is tagged with `@QTEST_TC_99999`, the PDF is:

```
Login_Functionality@QTEST_TC_99999.pdf
```

### How qTest Tag is Extracted

1. Feature tags are searched for `@QTEST_TC_XXXX` pattern
2. If found: PDF filename includes it
3. If not found: Uses `UNKNOWN`

**Example Feature File**:

```gherkin
@QTEST_TC_12345
@Critical
Feature: User Login
  Scenario: Valid credentials
    Given user is on login page
    When user enters valid credentials
    Then user should see dashboard
```

**Generated PDF**: `User_Login@QTEST_TC_12345.pdf`

## Architecture

### No Static State

Unlike grasshopper7, this plugin **never stores fonts or resources statically**:

```java
// ✅ CORRECT - Fresh instance per PDF
for (Feature feature : features) {
    FeaturePdfGenerator generator = new FeaturePdfGenerator(); // Fresh fonts
    generator.generateFeaturePdf(feature, outputPath);
}

// ❌ WRONG (grasshopper7 approach)
public class ReportFont {
    private static PdfFont font; // ← PROBLEM: Static state!
}
```

### Component Design

```
SplitPdfReporterMojo (Maven plugin entry point)
    ↓
CucumberJsonParser (Parse JSON, calculate stats)
    ↓
FeaturePdfGenerator (Coordinate PDF generation)
    ├─ PdfStyler (Fresh fonts + styling per PDF)
    ├─ DashboardPage (Page 1)
    ├─ SummaryPage (Page 2)
    └─ DetailedPage (Pages 3+)
        ├─ Step rendering with keywords
        ├─ Error message display
        ├─ DataTable formatting
        ├─ DocString rendering
        └─ Screenshot embedding (base64)
```

## Why iText 7?

| Aspect | grasshopper7 | iText 7 |
|--------|-------------|--------|
| Font State | Static (broken) | Stateless ✓ |
| Multi-PDF | Fails after 1st | Works perfectly ✓ |
| Thread Safety | No | Yes ✓ |
| Active Development | Abandoned | Maintained ✓ |
| Industry Standard | No | Yes ✓ |
| License | Unknown | AGPL-3.0 + Commercial ✓ |

## Logging

Plugin logs to SLF4J with:

```
[INFO] ========================================
[INFO]    Split PDF Reporter Plugin v1.0.0
[INFO]    Per-Feature Cucumber PDF Generation
[INFO] ========================================
[INFO] Input: /path/to/cucumber.json
[INFO] Output: /path/to/cucumber-reports
[INFO] Parsed 3 features
[INFO] ✓ Generated: Feature1@QTEST_TC_123.pdf
[INFO] ✓ Generated: Feature2@QTEST_TC_124.pdf
[INFO] ✓ Generated: Feature3@QTEST_TC_125.pdf
[INFO] ========================================
[INFO]    Generation Summary
[INFO]    Success: 3
[INFO]    Failures: 0
[INFO] ========================================
```

Enable debug logging:

```xml
<configuration>
    <verbose>true</verbose>
</configuration>
```

## Requirements

- Java 11+
- Maven 3.6.0+
- Cucumber JSON format (from cucumber-java, cucumber-testng, etc.)

## Troubleshooting

### PDF not generated
- Check `target/cucumber.json` exists
- Check plugin phase timing (post-integration-test)
- Check logs for errors

### Font corruption (if using old version)
- Update to version 1.0.0+
- Verify using iText 7 (check pom.xml dependency)

### Out of Memory
- Large test suites with many screenshots may need heap increase:
  ```bash
  mvn clean verify -Xmx2g
  ```

### Screenshots not showing
- Ensure Cucumber JSON includes base64 embeddings
- Check mime type is `image/png` or `image/jpeg`
- Verify step has correct `@Attachment("screenshot")` annotation

## Contributing

Contributions welcome! Please:
1. Fork repository
2. Create feature branch
3. Add tests
4. Submit pull request

## License

MIT License - See LICENSE file for details

## Acknowledgments

Built to solve the limitations of grasshopper7 and provide reliable per-feature PDF generation for Cucumber test automation.

## Support

For issues, questions, or suggestions:
- Open GitHub issue
- Check existing documentation
- Review test cases for examples
