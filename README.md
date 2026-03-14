# Split PDF Reporter Plugin

A Maven plugin for generating **per-feature PDF reports** from Cucumber JSON test results. Each feature gets its own PDF file, eliminating the font corruption bugs of grasshopper7 through a complete architectural rewrite using **Apache PDFBox** (Apache-2.0, fully compatible with MIT).

## Problem Solved

The grasshopper7 `cucumber-pdf-report` library has a **fundamental design flaw**: its `ReportFont` class uses static fields for font management. This breaks multi-PDF generation in a single JVM:

- **Static Font State**: Once a font's InputStream is consumed, it cannot be reused
- **Stream Exhaustion**: After first PDF, subsequent PDFs fail with corrupted fonts
- **Unfixable**: Reflection hacks don't work - the stream is already closed

**This plugin solves it completely** by:
- Using **Apache PDFBox** with built-in Type1 fonts (no external font streams)
- Creating **fresh resource instances per PDF** (no static state)
- Generating **one PDF per feature file** (isolated resources)
- Running **reliably across 100+ PDFs** in a single JVM

## Features

✅ **Per-Feature PDFs** - One PDF file per Cucumber feature  
✅ **qTest Integration** - Filename includes @QTEST_TC_XXXX tag  
✅ **No Font Corruption** - Stateless design, no shared fonts/streams  
✅ **Rich Content** - Dashboard, summary, detailed steps with screenshots  
✅ **Embedded Screenshots** - Base64 images rendered inline  
✅ **Error Details** - Failed steps show first 3 lines + overflow count  
✅ **DataTables & DocStrings** - Rendered as monospaced text blocks  
✅ **Thread-Safe** - Each PDF gets its own resource instances  
✅ **Maven Integration** - Post-integration-test phase hook  
✅ **Permissive Stack** - Apache PDFBox + Gson + SLF4J (MIT-friendly)  

## Installation

### Add to pom.xml

```xml
<plugin>
    <groupId>com.qtest</groupId>
    <artifactId>split-pdf-reporter-plugin</artifactId>
    <version>1.1.0</version>
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
| `cucumberJsonPattern` | String | `null` | Optional Ant-style glob for multiple JSON files |
| `reportOutputDir` | File | `${project.build.directory}/cucumber-reports` | Output directory for PDFs |
| `skipSplitPdfReporter` | Boolean | `false` | Skip plugin execution |
| `failOnNoFeatures` | Boolean | `true` | Fail build when no features found |
| `verbose` | Boolean | `false` | Enable verbose logging |

### Command-Line Override

```bash
mvn verify \
  -DcucumberJson=/path/to/results.json \
  -DreportOutputDir=/path/to/pdfs \
  -Dverbose=true
```

### Skip Plugin

```bash
mvn verify -DskipSplitPdfReporter=true
```

## PDF Format

Each feature PDF contains:

### Page 1: Dashboard
- Feature name with overall status badge (PASSED/FAILED/SKIPPED)
- Scenario & step statistics (counts)
- Textual distribution summary for scenarios and steps

### Page 2: Summary
- Table-like listing of all scenarios
- Scenario name, status, passed/failed step counts, duration
- Totals line with aggregate step statistics

### Pages 3+: Detailed
- **One page per scenario** showing:
  - Scenario name, duration, status
  - Every step with:
    - Keyword (Given/When/Then) + step name
    - Status + duration in milliseconds
    - Error message (first 3 lines + overflow count)
    - DataTables (rows rendered as `val1 | val2 | ...`)
    - DocStrings (monospace, one line per row)
    - **Embedded screenshots** (base64 decoded & drawn into the page)

> Note: The PDFBox layout is more "hand-crafted" than the original iText table-based layout,
> but the structure and information content closely match the grasshopper style.

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

1. Feature tags are searched for `@QTEST_TC_XXXX` (or `QTEST_TC_XXXX`)  
2. If not found at feature level, all scenarios are scanned for the same tag  
3. If still not found: uses `UNKNOWN`

## Architecture

### No Static State

Unlike grasshopper7, this plugin **never stores fonts or resources statically**:

```java
// ✅ CORRECT - Fresh instance per PDF
for (CucumberFeature feature : features) {
    FeaturePdfGenerator generator = new FeaturePdfGenerator();
    generator.generateFeaturePdf(feature, outputPath);
}
```

All fonts and drawing primitives are scoped to the current `PDDocument` and
`PDPageContentStream`. After `document.save()` and `document.close()`, the
resources are eligible for garbage collection.

### Component Design

```
SplitPdfReporterMojo (Maven plugin entry point)
    ↓
CucumberJsonParser (Parse JSON, calculate stats, extract qTest tag)
    ↓
FeaturePdfGenerator (Coordinate PDF generation)
    ├─ PdfStyler (PDFBox fonts + drawing primitives)
    ├─ PdfChartGenerator (summary text for distributions)
    ├─ DashboardPage (Page 1)
    ├─ SummaryPage (Page 2)
    └─ DetailedPage (Pages 3+)
        ├─ Step rendering with keywords
        ├─ Error message display
        ├─ DataTable rendering
        ├─ DocString rendering
        └─ Screenshot embedding (base64 → PDImageXObject)
```

### Why Apache PDFBox?

| Aspect | grasshopper7 | iText 7 (old impl) | PDFBox (current) |
|--------|--------------|--------------------|------------------|
| Font State | Static, broken | Stateless | Stateless |
| Multi-PDF | Fails after 1st | Works | Works |
| License | Unknown | AGPL/commercial | Apache-2.0 ✓ |
| MIT Compatibility | No | Risky | Yes ✓ |
| Dependency Size | Heavy | Moderate | Moderate |
| Custom Layout | Limited | Powerful | Manual but flexible |

PDFBox gives us a **permissive license** while still providing enough power
to implement the required pages and embedded screenshots.

## Logging

The plugin logs to SLF4J with messages such as:

```
[INFO] ========================================
[INFO]  Split PDF Reporter Plugin v1.1.0
[INFO]  Per-Feature Cucumber PDF Generation (PDFBox)
[INFO] ========================================
[INFO]  cucumberJson        : /path/to/target/cucumber.json
[INFO]  cucumberJsonPattern : (none)
[INFO]  outputDirectory     : /path/to/target/cucumber-reports
[INFO]  failOnNoFeatures    : true
[INFO]  verbose             : false
[INFO] Parsed 3 feature(s) from cucumber.json
[INFO] ✓ Generated : Feature1@QTEST_TC_123.pdf
[INFO] ✓ Generated : Feature2@QTEST_TC_124.pdf
[INFO] ✓ Generated : Feature3@QTEST_TC_125.pdf
[INFO] ==============================
[INFO]  Generation Summary
[INFO]  Output : /path/to/target/cucumber-reports
[INFO]  ✓ Success : 3
[INFO]  ✗ Failures: 0
[INFO] ==============================
```

Enable verbose logging:

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
- Check `target/cucumber.json` exists or that `cucumberJsonPattern` matches files
- Check plugin phase timing (post-integration-test)
- Check logs for errors (enable `-Dverbose=true`)

### Screenshots not showing
- Ensure Cucumber JSON includes base64 embeddings
- Check mime type is `image/png` or `image/jpeg`

### Out of Memory
- Large test suites with many screenshots may need heap increase:
  ```bash
  mvn clean verify -Xmx2g
  ```

## Contributing

Contributions welcome! Please:
1. Fork repository
2. Create feature branch
3. Add tests
4. Submit pull request

## License

MIT License - See LICENSE file for details.  
Internal PDF generation now uses **Apache PDFBox (Apache-2.0)** to avoid any
AGPL/commercial licensing conflicts.

## Acknowledgments

Originally built to solve the limitations of grasshopper7 and provide
reliable per-feature PDF generation for Cucumber test automation, now
updated to use a fully permissive PDF stack.

## Support

For issues, questions, or suggestions:
- Open GitHub issue
- Check existing documentation
- Review test cases for examples
