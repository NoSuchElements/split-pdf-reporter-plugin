# Split PDF Reporter - Architecture Documentation (PDFBox Edition)

## Problem Statement

The grasshopper7 `cucumber-pdf-report` library fails when generating multiple PDFs in a single JVM process. The root cause is **static font state management**:

```java
// grasshopper7 ReportFont class (BROKEN)
public class ReportFont {
    private static PdfFont regularFont;    // ❌ Static!
    private static PdfFont boldFont;       // ❌ Static!
    private static InputStream fontStream; // ❌ Stream exhausted after first PDF!
}
```

The earlier 1.0.0 version of this plugin used iText 7 to fix grasshopper7's
static state bug, but iText Core is AGPL/commercial, which is not ideal for
an MIT-licensed plugin.

## Solution: Stateless Architecture + Permissive PDF Library

This plugin uses a **stateless architecture** with **Apache PDFBox** as the
PDF engine:

- No static fonts or shared InputStreams
- Fresh `PDDocument` and `PDPageContentStream` per feature
- Built-in Type1 fonts (HELVETICA, COURIER, etc.), no external font files
- Apache-2.0 license (compatible with MIT)

### Core Principle

**Each feature PDF gets completely independent resources - no shared state**:

```java
for (CucumberFeature feature : features) {
    FeaturePdfGenerator generator = new FeaturePdfGenerator();
    String qtestId  = parser.extractQtestCaseId(feature);
    String filename = FeaturePdfGenerator.generateFilename(feature, qtestId);
    String output   = outputDir + File.separator + filename;

    generator.generateFeaturePdf(feature, output);
}
```

## Technology Stack

### Apache PDFBox
- PDF engine (Apache-2.0)
- Uses `PDDocument`, `PDPage`, `PDPageContentStream`
- Built-in Type1 fonts (no TTF/OTF packaging)

### Gson (JSON Parsing)
- Lightweight, no external state
- Deserializes Cucumber JSON into model objects

### SLF4J (Logging)
- Pluggable logging facade

## Component Architecture

```
┌─────────────────────────────────────────┐
│      SplitPdfReporterMojo               │ Maven plugin entry point
│  (post-integration-test phase)          │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│    CucumberJsonParser                   │ Parses JSON + calculates stats
│  • Parse features/scenarios/steps       │
│  • Calculate pass/fail/skip counts      │
│  • Extract qTest tags                   │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  FeaturePdfGenerator                    │ Main coordination
│  (Fresh instance per feature)           │
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ PdfStyler                           │ │ PDFBox drawing helpers
│ │ • Fonts: HELVETICA, HELVETICA_BOLD  │ │
│ │ • drawText()                        │ │
│ │ • drawStatusBadge()                 │ │
│ │ • drawLabelValue()                  │ │
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ PdfChartGenerator                   │ │
│ │ • drawScenarioAndStepSummary()      │ │ Textual dist. for now
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ DashboardPage (Page 1)              │ │
│ │ • Feature name + status badge       │ │
│ │ • Scenario & step statistics        │ │
│ │ • Distribution summary text         │ │
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ SummaryPage (Page 2)                │ │
│ │ • Scenario listing (name/status)    │ │
│ │ • Passed/failed counts              │ │
│ │ • Duration                          │ │
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ DetailedPage (Pages 3+)             │ │
│ │ • Step keyword + name               │ │
│ │ • Status + duration                 │ │
│ │ • Error messages (first 3 lines)    │ │
│ │ • DataTables (CSV-style)            │ │
│ │ • DocStrings (monospace lines)      │ │
│ │ • Screenshots (base64 → image)      │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## Data Flow

```
Cucumber JSON Input
        │
        ▼
┌──────────────────────┐
│  JSON Deserialization│  Gson parses into:
│  CucumberFeature[]   │  - CucumberFeature
└──────────┬───────────┘  - CucumberScenario[]
           │              - CucumberStep[]
           ▼
┌──────────────────────┐
│ Statistics Calculation│  For each feature:
│ (Pass/Fail/Skip)     │  - Scenario counts
└──────────┬───────────┘  - Step counts
           │
           ▼
┌──────────────────────┐
│ PDF Generation Loop  │  For each feature:
│ FeaturePdfGenerator  │  1. new PDDocument
└──────────┬───────────┘  2. Add pages (Dashboard/Summary/Detailed)
           │              3. Save and close document
           ▼
        Output PDFs
    ├─ Feature1@TC_1.pdf
    ├─ Feature2@TC_2.pdf
    └─ Feature3@TC_3.pdf
```

## Font & Resource Lifecycle

### PDFBox Font Handling

We use PDFBox built-in **Type1 fonts**:

```java
public class PdfStyler {
    public PDType1Font regularFont() { return PDType1Font.HELVETICA; }
    public PDType1Font boldFont()    { return PDType1Font.HELVETICA_BOLD; }
    public PDType1Font italicFont()  { return PDType1Font.HELVETICA_OBLIQUE; }
    public PDType1Font monoFont()    { return PDType1Font.COURIER; }
}
```

These fonts are provided by PDFBox and do **not** rely on external
InputStreams. There is no risk of stream exhaustion.

### Resource Lifecycle

```
1. FeaturePdfGenerator.generateFeaturePdf()
   ├─ new PDDocument()
   ├─ new PDPage(A4) for Dashboard
   ├─ new PDPage(A4) for Summary
   ├─ new PDPage(A4) per scenario
   ├─ Each page:
   │   ├─ new PDPageContentStream(..., AppendMode.APPEND, true)
   │   ├─ PdfStyler.drawText/drawStatusBadge/etc.
   │   └─ contentStream.close()
   ├─ document.save(file)
   └─ document.close()  ← releases resources

2. Generator instance goes out of scope
   └─ Eligible for garbage collection
```

## Thread Safety Considerations

The design remains parallel-friendly:

```java
features.parallelStream().forEach(feature -> {
    try {
        FeaturePdfGenerator generator = new FeaturePdfGenerator();
        String qtestId  = parser.extractQtestCaseId(feature);
        String filename = FeaturePdfGenerator.generateFilename(feature, qtestId);
        generator.generateFeaturePdf(feature, outputDir + "/" + filename);
    } catch (IOException e) {
        getLog().error("Failed: " + feature.getName(), e);
    }
});
```

Each thread gets its own `FeaturePdfGenerator` + `PDDocument`; PDFBox is
thread-safe as long as documents are not shared across threads.

## Performance Considerations

- **Per-feature overhead**: One PDDocument with a few pages
- **Screenshots**: Embedded as compressed images via `PDImageXObject`
- **100+ features**: Works reliably without font corruption

Further optimizations (if needed):
- Parallel generation (as above)
- Reducing screenshot resolution before embedding
- Skipping PDFs for features with no scenarios

## Licensing

- Plugin: **MIT License**
- PDF engine: **Apache PDFBox (Apache-2.0)**

This combination avoids AGPL/commercial constraints that would come from
using iText Core while preserving the stateless, per-feature PDF
architecture originally designed to fix grasshopper7.
