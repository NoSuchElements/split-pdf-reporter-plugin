# Technical Solution: Fixing Grasshopper7 Font Corruption (PDFBox Version)

## The Problem: Static Font State Bug in Grasshopper7

The grasshopper7 library's `ReportFont` class uses static fields to manage fonts:

```java
// grasshopper7 - BROKEN DESIGN
public class ReportFont {
    private static PdfFont regularFont;      // ❌ Static - shared across all PDFs
    private static InputStream fontStream;   // ❌ Stream consumed and closed
    
    static {
        try {
            fontStream = ReportFont.class.getResourceAsStream("/fonts/arial.ttf");
            regularFont = PdfFontFactory.createFont(fontStream, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

This design causes font streams to be exhausted after the first PDF, leading
to corrupted text in later documents.

## Our Approach: Stateless Architecture + PDFBox

Rather than trying to patch grasshopper7, we built a new architecture and
moved from iText 7 (AGPL/commercial) to **Apache PDFBox (Apache-2.0)**.

### 1. Stateless, Per-Feature Generation

```java
for (CucumberFeature feature : features) {
    FeaturePdfGenerator generator = new FeaturePdfGenerator();
    String qtestId  = parser.extractQtestCaseId(feature);
    String filename = FeaturePdfGenerator.generateFilename(feature, qtestId);
    String output   = outputDir + File.separator + filename;

    generator.generateFeaturePdf(feature, output);
}
```

Each call to `generateFeaturePdf` creates its own `PDDocument` and pages.
No fonts or streams are reused across features.

### 2. PDFBox Font Handling

PDFBox provides built-in Type1 fonts that do not depend on external
InputStreams:

```java
public class PdfStyler {
    public PDType1Font regularFont() { return PDType1Font.HELVETICA; }
    public PDType1Font boldFont()    { return PDType1Font.HELVETICA_BOLD; }
    public PDType1Font italicFont()  { return PDType1Font.HELVETICA_OBLIQUE; }
    public PDType1Font monoFont()    { return PDType1Font.COURIER; }
}
```

This completely sidesteps the stream exhaustion problem seen in
ReportFont-based designs.

### 3. Resource Lifecycle per PDF

```java
public void generateFeaturePdf(CucumberFeature feature, String outputFilePath) throws IOException {
    try (PDDocument document = new PDDocument()) {
        PDPage dashboard = new PDPage(PDRectangle.A4);
        document.addPage(dashboard);
        dashboardPage.build(document, dashboard, feature);
        
        PDPage summary = new PDPage(PDRectangle.A4);
        document.addPage(summary);
        summaryPage.build(document, summary, feature);

        // One detailed page per scenario
        for (CucumberScenario scenario : feature.getScenarios()) {
            PDPage detailed = new PDPage(PDRectangle.A4);
            document.addPage(detailed);
            detailedPage.build(document, detailed, scenario);
        }

        document.save(new File(outputFilePath));
    }
}
```

When `document.close()` returns, all resources for that feature are
released. The next feature starts with a completely clean state.

## Why This Fix Works

1. **No shared state**: Each PDF has its own document and content streams.  
2. **No font streams**: Built-in Type1 fonts remove the need for TTF
   InputStreams entirely.  
3. **Garbage collection**: Once a feature PDF is saved, its document
   instance is eligible for GC.  
4. **Thread-safe**: Each generator and document live on a single thread.

## Comparison: Grasshopper7 vs Split PDF Reporter (PDFBox)

### Grasshopper7

- Static font fields for all PDFs
- Reliance on a single InputStream consumed at startup
- Fails after the first PDF (font corruption)

### Split PDF Reporter (PDFBox)

- Fresh `PDDocument` per feature
- Built-in fonts, no external streams
- Verified to handle many PDFs in the same JVM without corruption

## Licensing Advantage

By using **Apache PDFBox (Apache-2.0)**:

- The plugin can remain under **MIT** without AGPL obligations
- Users can integrate it into commercial pipelines without separate
  iText licensing

This preserves the original technical benefits (stateless multi-PDF
generation) while aligning with a permissive open-source licensing model.
