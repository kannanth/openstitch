# OpenStitch

**Dynamic Report Generation System**

OpenStitch is an open-source report generation platform that pairs a Java PDF engine with a visual template designer. Developers define data-driven layouts as JSON with expressions and logic; teams refine them visually via drag-and-drop — bridging code and design for automated, pixel-perfect documents at scale.

---

## Features

- **JSON-based templates** -- define page layout, headers, footers, and body content as structured JSON
- **Dynamic data binding** -- bind templates to JSON, XML, or CSV data using `${expression}` syntax
- **Rich element types** -- text, tables, images, charts (bar, pie, line), conditional blocks, repeating sections
- **Expression engine** -- arithmetic, comparison, logical operators, and built-in functions (`format`, `sum`, `upper`, `dateFormat`, etc.)
- **Automatic page management** -- flow layout with automatic page breaks, headers/footers on every page
- **Page numbering** -- configurable format including "Page X of Y", Roman numerals, and custom patterns
- **Header/footer variants** -- first-page-different and odd/even-different configurations
- **Table features** -- column alignment, number formatting, alternating row colors, borders, footer summary rows
- **Template storage** -- save and manage templates via filesystem or PostgreSQL
- **REST API** -- generate PDFs and manage templates over HTTP with Spring Boot
- **Extensible** -- register custom element renderers, expression functions, and storage providers
- **Docker support** -- production-ready Docker Compose setup with multi-stage builds

---

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- Node.js 22+ (for the template designer)
- Docker and Docker Compose (optional)

### Option 1: Docker Compose (recommended)

```bash
cd docker
docker compose up --build
```

This starts:
- **API server** at `http://localhost:8080`
- **Template designer** at `http://localhost:3000`

To also start PostgreSQL for database-backed template storage:

```bash
docker compose --profile database up --build
```

### Option 2: Build from Source

**Build the Java backend:**

```bash
mvn clean package
```

**Run the API server:**

```bash
java -jar pdf-api/target/pdf-api-1.0.0-SNAPSHOT.jar
```

The API starts at `http://localhost:8080`.

**Run the template designer (separate terminal):**

```bash
cd template-designer
npm install
npm run dev
```

The designer starts at `http://localhost:5173` (or the port shown in the terminal).

---

## Architecture Overview

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  Template         │     │  PDF API          │     │  PDF Engine       │
│  Designer         │────>│  (Spring Boot)    │────>│  (Java + PDFBox)  │
│  (Browser)        │     │                   │     │                   │
└──────────────────┘     └──────────────────┘     └──────────────────┘
                               │                         │
                               v                         v
                         ┌───────────┐           ┌──────────────┐
                         │ Storage   │           │ Generated    │
                         │ (FS/DB)   │           │ PDF          │
                         └───────────┘           └──────────────┘
```

| Module | Description |
|---|---|
| `pdf-engine` | Core library. Parses templates, evaluates expressions, renders PDFs with Apache PDFBox. |
| `pdf-api` | REST API. Manages templates (CRUD) and exposes PDF generation endpoints. |
| `template-designer` | Visual editor for designing template JSON (served as static files). |

For detailed architecture documentation, see [docs/architecture.md](docs/architecture.md).

---

## API Examples

### Check service health

```bash
curl http://localhost:8080/api/v1/health
```

```json
{"status":"UP","version":"1.0.0"}
```

### Generate a PDF (inline template)

```bash
curl -X POST http://localhost:8080/api/v1/generate/inline \
  -H "Content-Type: application/json" \
  -d '{
    "template": {
      "pageLayout": {"pageSize": "A4"},
      "body": {
        "elements": [
          {
            "type": "TEXT",
            "content": "Hello, ${name}!",
            "style": {"fontSize": 24, "bold": true, "alignment": "CENTER"}
          },
          {
            "type": "TEXT",
            "content": "Generated on ${now()}",
            "style": {"fontSize": 12, "italic": true, "alignment": "CENTER"},
            "marginTop": 20
          }
        ]
      }
    },
    "data": {"name": "World"}
  }' \
  -o hello.pdf
```

### Save a template

```bash
curl -X POST http://localhost:8080/api/v1/templates \
  -H "Content-Type: application/json" \
  -d '{
    "template": {
      "metadata": {
        "name": "Invoice",
        "tags": ["invoice", "finance"]
      },
      "body": {
        "elements": [
          {
            "type": "TEXT",
            "content": "Invoice #${invoice.number}",
            "style": {"fontSize": 20, "bold": true}
          },
          {
            "type": "TABLE",
            "dataSource": "invoice.items",
            "columns": [
              {"header": "Item", "field": "description", "width": 250},
              {"header": "Qty", "field": "quantity", "width": 60, "alignment": "CENTER"},
              {"header": "Amount", "field": "amount", "width": 100, "alignment": "RIGHT", "format": "#,##0.00"}
            ],
            "headerStyle": {"bold": true, "backgroundColor": "#4472C4", "textColor": "#FFFFFF"},
            "alternateRowColor": "#F2F2F2"
          }
        ]
      }
    }
  }'
```

### Generate PDF from a stored template

```bash
curl -X POST http://localhost:8080/api/v1/generate \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": "<id-from-save-response>",
    "data": {
      "invoice": {
        "number": "INV-2025-001",
        "items": [
          {"description": "Widget A", "quantity": 5, "amount": 49.95},
          {"description": "Widget B", "quantity": 3, "amount": 29.97}
        ]
      }
    }
  }' \
  -o invoice.pdf
```

### List all templates

```bash
curl http://localhost:8080/api/v1/templates
```

### List templates by tag

```bash
curl "http://localhost:8080/api/v1/templates?tag=invoice"
```

### Delete a template

```bash
curl -X DELETE http://localhost:8080/api/v1/templates/<id>
```

For the full API reference, see [docs/api-reference.md](docs/api-reference.md).

---

## Development Setup

### Project Structure

```
OpenStitch/
  pom.xml                    # Maven parent POM
  pdf-engine/                # Core PDF generation library
    pom.xml
    src/main/java/com/openstitch/engine/
      OpenStitchEngine.java  # Main facade
      model/                 # Template domain model
      parser/                # Template and data parsers
      expression/            # Expression evaluator and functions
      render/                # PDF rendering pipeline
      layout/                # Layout engine and page breaks
      storage/               # Storage abstraction
  pdf-api/                   # Spring Boot REST API
    pom.xml
    src/main/java/com/openstitch/api/
      controller/            # REST controllers
      service/               # Business services
      dto/                   # Request/response DTOs
      config/                # Configuration
      exception/             # Exception handling
  template-designer/         # Visual template editor
  docker/                    # Docker and deployment files
    Dockerfile.api
    Dockerfile.designer
    docker-compose.yml
    nginx.conf
  docs/                      # Documentation
    architecture.md
    template-schema.md
    api-reference.md
```

### Running Tests

```bash
mvn test
```

### Using the Engine as a Library

The `pdf-engine` module can be used standalone in any Java application without Spring:

```java
OpenStitchEngine engine = new OpenStitchEngine();

String templateJson = """
  {
    "body": {
      "elements": [
        {"type": "TEXT", "content": "Hello, ${name}!"}
      ]
    }
  }
  """;

String dataJson = """
  {"name": "World"}
  """;

byte[] pdf = engine.generatePdf(templateJson, dataJson);
Files.write(Path.of("output.pdf"), pdf);
```

### Extending the Engine

**Custom renderer:**

```java
engine.getRenderer().registerRenderer(MyElement.class, new MyRenderer());
```

**Custom expression function:**

```java
engine.getRenderer().getExpressionEvaluator()
    .getFunctionRegistry()
    .register("myFunc", args -> { /* ... */ });
```

---

## Documentation

| Document | Description |
|---|---|
| [Architecture](docs/architecture.md) | System design, module descriptions, data flow, and extension points |
| [Template Schema](docs/template-schema.md) | Complete JSON template reference with all element types and examples |
| [API Reference](docs/api-reference.md) | REST endpoint documentation with request/response examples |

---

## License

This project is licensed under the terms specified in the LICENSE file.
