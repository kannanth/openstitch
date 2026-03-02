# OpenStitch Architecture

## System Overview

OpenStitch is a dynamic report generation system that transforms JSON template definitions and data into pixel-perfect PDF documents. The system is composed of three modules:

| Module | Technology | Purpose |
|---|---|---|
| **pdf-engine** | Java 21, Apache PDFBox 3.x | Core rendering engine -- parses templates, evaluates expressions, and generates PDFs |
| **pdf-api** | Spring Boot 3.4 | REST API layer -- exposes PDF generation and template management over HTTP |
| **template-designer** | Node.js / Browser | Visual template design tool (future full integration) |

All modules are managed together in a single monorepo with a Maven parent POM (`openstitch-parent`) coordinating the Java build.

---

## Architecture Decisions

### Monorepo

All modules live in a single repository. The Maven parent POM declares `pdf-engine` and `pdf-api` as child modules, allowing a single `mvn package` command to build the entire backend. The `template-designer` is built separately with npm but deployed alongside via Docker Compose.

### Renderer Registry Pattern

The `PdfRenderer` maintains a `Map<Class<? extends Element>, ElementRenderer<?>>` that maps each element subclass to its renderer. This decouples the rendering pipeline from specific element types and allows custom renderers to be registered at runtime:

```java
pdfRenderer.registerRenderer(MyCustomElement.class, new MyCustomRenderer());
```

Built-in renderers:

| Element Class | Renderer |
|---|---|
| `TextElement` | `TextRenderer` |
| `TableElement` | `TableRenderer` |
| `ImageElement` | `ImageRenderer` |
| `ChartElement` | `ChartRenderer` |
| `ConditionalBlock` | `ConditionalRenderer` |
| `RepeatingSection` | `RepeatingSectionRenderer` |

### Two-Pass Rendering

Headers and footers require knowledge of total page count (for "Page X of Y" numbering). The engine uses a two-pass approach:

1. **First pass** -- render body content, creating pages as needed. Headers are rendered immediately on each new page. Footers and page numbers are deferred.
2. **Second pass** -- once total page count is known, render footers and page numbers on every page.

### Storage Abstraction

Template persistence is abstracted behind the `StorageProvider` interface:

```java
public interface StorageProvider {
    Template save(Template template);
    Optional<Template> findById(String id);
    List<TemplateMetadata> findAll();
    List<TemplateMetadata> findByTag(String tag);
    Template update(String id, Template template);
    void delete(String id);
    boolean exists(String id);
}
```

Two implementations ship out of the box:

- **`FileSystemStorageProvider`** -- stores templates as JSON files on disk. Default for local development.
- **`DatabaseStorageProvider`** -- stores templates in PostgreSQL using Spring JDBC and Flyway migrations.

The active provider is selected by the `OPENSTITCH_STORAGE_TYPE` environment variable (`filesystem` or `database`).

---

## Module Descriptions

### pdf-engine

The engine is a pure Java library with zero Spring dependencies. It can be used standalone in any JVM application.

**Key packages:**

| Package | Responsibility |
|---|---|
| `com.openstitch.engine` | `OpenStitchEngine` facade -- single entry point for template parsing and PDF generation |
| `com.openstitch.engine.model` | Template domain model -- `Template`, `Element` subclasses, `Style`, `PageLayout`, etc. |
| `com.openstitch.engine.parser` | Template and data parsers -- JSON, XML, and CSV input formats |
| `com.openstitch.engine.expression` | Expression evaluator and function registry |
| `com.openstitch.engine.render` | PDF rendering pipeline -- `PdfRenderer`, element renderers, layout engine |
| `com.openstitch.engine.layout` | Layout engine and page break handling |
| `com.openstitch.engine.storage` | Storage abstraction and implementations |
| `com.openstitch.engine.exception` | Domain-specific exception hierarchy |

**Dependencies:**

- Apache PDFBox 3.0.4 -- low-level PDF creation
- JFreeChart 1.5.5 -- chart rendering (bar, pie, line)
- Jackson 2.18.x -- JSON/XML/CSV parsing
- SLF4J + Logback -- logging

### pdf-api

A Spring Boot 3.4 application that wraps the engine and exposes it as a REST API.

**Key packages:**

| Package | Responsibility |
|---|---|
| `com.openstitch.api.controller` | REST controllers -- `GenerateController`, `TemplateController`, `HealthController` |
| `com.openstitch.api.service` | Business services -- `PdfGenerationService`, `TemplateService`, `DataConversionService` |
| `com.openstitch.api.dto` | Request/response DTOs |
| `com.openstitch.api.config` | CORS, storage, and OpenAPI configuration |
| `com.openstitch.api.exception` | Global exception handler |

**Additional dependencies:**

- Spring Boot Starter Web, Validation, Actuator, JDBC
- SpringDoc OpenAPI (Swagger UI at `/swagger-ui.html`)
- Flyway -- database migrations
- PostgreSQL driver (runtime scope)

### template-designer

A browser-based visual editor for designing OpenStitch template JSON. Deployed as static files served by nginx.

---

## Data Flow

```
Template JSON ──> TemplateParser ──> Template (model)
                                          │
Data (JSON/XML/CSV) ──> DataParser ──> DataContext
                                          │
                                          v
                                    PdfRenderer
                                          │
                                   ┌──────┴──────┐
                                   │  For each    │
                                   │  Element:    │
                                   │              │
                                   │  Renderer    │
                                   │  Registry    │
                                   │  Lookup      │
                                   │              │
                                   │  Expression  │
                                   │  Evaluation  │
                                   │              │
                                   │  PDFBox      │
                                   │  Drawing     │
                                   └──────┬──────┘
                                          │
                                          v
                                    PDF byte[]
```

### Step-by-step

1. **Parse template** -- `TemplateParser` deserializes the JSON string into a `Template` object graph using Jackson. Polymorphic deserialization maps the `type` field to the correct `Element` subclass via `@JsonSubTypes`.

2. **Parse data** -- The input data (JSON, XML, or CSV) is parsed into a `Map<String, Object>` and wrapped in a `DataContext`. The `DataContext` supports dot-notation path resolution (e.g., `company.address.city`), list indexing, and scoped child contexts for repeating sections.

3. **Render** -- `PdfRenderer` creates a `PDDocument`, sets up page geometry from `PageLayout`, and iterates over body elements. For each element:
   - The renderer registry resolves the correct `ElementRenderer` by element class.
   - The renderer evaluates any `${...}` expressions in the element properties against the `DataContext`.
   - Content is drawn to the current `PDPageContentStream` via PDFBox.
   - If the content exceeds the available page space, the layout engine triggers a page break.

4. **Headers and footers** -- `HeaderFooterRenderer` renders the header on each new page immediately and renders the footer on each page during the finalization pass. Headers and footers support first-page-different and odd/even-different configurations.

5. **Output** -- The completed `PDDocument` is serialized to a `byte[]`.

---

## Storage Layer

### Filesystem Storage

Templates are stored as individual JSON files in a configurable base directory. The file name is the template ID with a `.json` extension. Metadata (name, tags, timestamps) is embedded in the template JSON itself.

Configuration:

```properties
openstitch.storage.type=filesystem
openstitch.storage.filesystem.base-dir=/data/templates
```

### Database Storage

Templates are stored in PostgreSQL. Schema migrations are managed by Flyway. The template JSON is stored in a `jsonb` column for efficient querying.

Configuration:

```properties
openstitch.storage.type=database
spring.datasource.url=jdbc:postgresql://localhost:5432/openstitch
spring.datasource.username=openstitch
spring.datasource.password=openstitch
```

---

## Expression Evaluation System

The expression engine resolves `${...}` placeholders in template text, conditions, and data source paths.

### Supported syntax

| Category | Examples |
|---|---|
| Variable references | `${company.name}`, `${items[0].price}` |
| Function calls | `${format(total, "#,##0.00")}`, `${upper(name)}` |
| Arithmetic | `${price * quantity}`, `${subtotal + tax}` |
| Comparison | `${total > 1000}`, `${status == "active"}` |
| Logical | `${hasDiscount && total > 100}`, `${isVip \|\| total > 500}` |
| String literals | `${"Hello"}`, `${'world'}` |
| Numeric literals | `${42}`, `${3.14}` |
| Boolean/null | `${true}`, `${false}`, `${null}` |

### Built-in functions

| Function | Signature | Description |
|---|---|---|
| `format` | `format(number, pattern)` | Format a number using `DecimalFormat` patterns |
| `upper` | `upper(string)` | Convert string to uppercase |
| `lower` | `lower(string)` | Convert string to lowercase |
| `sum` | `sum(list, field)` | Sum a numeric field across a list of maps |
| `count` | `count(list)` | Count items in a list or collection |
| `now` | `now()` | Current date/time as ISO-8601 string |
| `dateFormat` | `dateFormat(dateString, pattern)` | Format a date string with a `DateTimeFormatter` pattern |

### Custom functions

Register custom functions via the `FunctionRegistry`:

```java
OpenStitchEngine engine = new OpenStitchEngine();
FunctionRegistry registry = engine.getRenderer().getExpressionEvaluator().getFunctionRegistry();
registry.register("capitalize", args -> {
    String s = args[0].toString();
    return s.substring(0, 1).toUpperCase() + s.substring(1);
});
```

---

## Extension Points

### Custom Element Renderers

Create a new `Element` subclass and a corresponding `ElementRenderer` implementation, then register it:

```java
public class BarcodeElement extends Element {
    private String barcodeData;
    // ...
}

public class BarcodeRenderer implements ElementRenderer<BarcodeElement> {
    @Override
    public float render(BarcodeElement element, RenderContext context, DataContext dataContext)
            throws IOException {
        // Draw barcode using PDFBox
        return renderedHeight;
    }
}

// Registration
engine.getRenderer().registerRenderer(BarcodeElement.class, new BarcodeRenderer());
```

### Custom Data Parsers

Implement the `DataParser` interface to support additional input formats (YAML, protocol buffers, etc.) and pass the resulting `Map<String, Object>` to the engine via `DataContext`.

### Custom Storage Providers

Implement the `StorageProvider` interface to store templates in S3, MongoDB, or any other backing store.

### Custom Expression Functions

Register functions in the `FunctionRegistry` as shown above. Functions receive an `Object[]` of evaluated arguments and return an `Object`.
