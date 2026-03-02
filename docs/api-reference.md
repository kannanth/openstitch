# OpenStitch REST API Reference

Base URL: `http://localhost:8080/api/v1`

All request and response bodies use `application/json` unless otherwise noted. PDF generation endpoints return `application/pdf`.

---

## Table of Contents

- [Health and Version](#health-and-version)
- [Template Management](#template-management)
- [PDF Generation](#pdf-generation)
- [Error Responses](#error-responses)
- [CORS Configuration](#cors-configuration)
- [Authentication](#authentication)
- [OpenAPI / Swagger UI](#openapi--swagger-ui)

---

## Health and Version

### GET /api/v1/health

Returns the service health status.

**Response: 200 OK**

```json
{
  "status": "UP",
  "version": "1.0.0"
}
```

---

### GET /api/v1/version

Returns the service version information.

**Response: 200 OK**

```json
{
  "version": "1.0.0",
  "name": "OpenStitch PDF API"
}
```

---

## Template Management

### POST /api/v1/templates

Create a new template.

**Request Body:**

```json
{
  "template": {
    "metadata": {
      "name": "Invoice Template",
      "description": "Standard customer invoice",
      "tags": ["invoice", "finance"]
    },
    "pageLayout": {
      "pageSize": "A4",
      "orientation": "PORTRAIT"
    },
    "body": {
      "elements": [
        {
          "type": "TEXT",
          "content": "Hello, ${name}!",
          "style": { "fontSize": 18, "bold": true }
        }
      ]
    }
  }
}
```

**Response: 201 Created**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Invoice Template",
  "description": "Standard customer invoice",
  "author": null,
  "tags": ["invoice", "finance"],
  "template": { ... },
  "version": 1,
  "createdAt": "2025-01-15T10:30:00Z",
  "updatedAt": "2025-01-15T10:30:00Z"
}
```

---

### GET /api/v1/templates

List all templates. Optionally filter by tag.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `tag` | string | No | Filter templates by tag |

**Example:** `GET /api/v1/templates?tag=invoice`

**Response: 200 OK**

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Invoice Template",
    "description": "Standard customer invoice",
    "author": null,
    "tags": ["invoice", "finance"],
    "template": { ... },
    "version": 1,
    "createdAt": "2025-01-15T10:30:00Z",
    "updatedAt": "2025-01-15T10:30:00Z"
  }
]
```

---

### GET /api/v1/templates/{id}

Retrieve a template by ID.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | string | Template ID |

**Response: 200 OK**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Invoice Template",
  "description": "Standard customer invoice",
  "author": null,
  "tags": ["invoice", "finance"],
  "template": { ... },
  "version": 1,
  "createdAt": "2025-01-15T10:30:00Z",
  "updatedAt": "2025-01-15T10:30:00Z"
}
```

**Response: 404 Not Found**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Template not found: a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

---

### PUT /api/v1/templates/{id}

Update an existing template.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | string | Template ID |

**Request Body:**

```json
{
  "template": {
    "metadata": {
      "name": "Updated Invoice Template"
    },
    "body": {
      "elements": [ ... ]
    }
  }
}
```

**Response: 200 OK**

Returns the updated `TemplateResponse` with an incremented `version`.

---

### DELETE /api/v1/templates/{id}

Delete a template.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | string | Template ID |

**Response: 204 No Content**

No response body.

---

### POST /api/v1/templates/{id}/clone

Clone an existing template. Creates a new template with a new ID and appended " (Copy)" to the name.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `id` | string | Source template ID |

**Response: 201 Created**

Returns the cloned `TemplateResponse` with a new ID, version 1, and new timestamps.

---

## PDF Generation

### POST /api/v1/generate/inline

Generate a PDF from an inline template and data payload. The template definition is provided directly in the request body.

**Request Body:**

```json
{
  "template": {
    "pageLayout": { "pageSize": "A4" },
    "body": {
      "elements": [
        {
          "type": "TEXT",
          "content": "Hello, ${name}!",
          "style": { "fontSize": 24, "bold": true }
        }
      ]
    }
  },
  "data": {
    "name": "World"
  }
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `template` | object | Yes | The full template definition (JSON object) |
| `data` | object | No | Data object for expression binding (JSON) |
| `dataString` | string | No | Raw data string (for XML or CSV input) |
| `dataFormat` | string | No | Data format: `JSON` (default), `XML`, `CSV` |

**Response: 200 OK**

- Content-Type: `application/pdf`
- Content-Disposition: `attachment; filename="report.pdf"`
- Body: PDF binary

**curl example:**

```bash
curl -X POST http://localhost:8080/api/v1/generate/inline \
  -H "Content-Type: application/json" \
  -d '{
    "template": {
      "body": {
        "elements": [
          {"type": "TEXT", "content": "Hello, ${name}!", "style": {"fontSize": 24}}
        ]
      }
    },
    "data": {"name": "World"}
  }' \
  -o report.pdf
```

---

### POST /api/v1/generate/inline (with XML data)

Generate a PDF using XML data instead of JSON.

**Request Body:**

```json
{
  "template": {
    "body": {
      "elements": [
        { "type": "TEXT", "content": "Customer: ${customer.name}" }
      ]
    }
  },
  "dataFormat": "XML",
  "dataString": "<root><customer><name>Acme Corp</name></customer></root>"
}
```

---

### POST /api/v1/generate/inline (with CSV data)

Generate a PDF using CSV data.

**Request Body:**

```json
{
  "template": {
    "body": {
      "elements": [
        {
          "type": "TABLE",
          "dataSource": "rows",
          "columns": [
            { "header": "Name", "field": "name" },
            { "header": "Age", "field": "age" }
          ]
        }
      ]
    }
  },
  "dataFormat": "CSV",
  "dataString": "name,age\nAlice,30\nBob,25"
}
```

---

### POST /api/v1/generate

Generate a PDF from a stored template by ID.

**Request Body:**

```json
{
  "templateId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "data": {
    "company": { "name": "Acme Corp" },
    "invoice": {
      "number": "INV-2025-001",
      "items": [
        { "description": "Widget", "quantity": 10, "unitPrice": 9.99, "amount": 99.90 }
      ]
    }
  }
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `templateId` | string | Yes | ID of a previously stored template |
| `data` | object | No | Data object for expression binding |

**Response: 200 OK**

- Content-Type: `application/pdf`
- Content-Disposition: `attachment; filename="report.pdf"`
- Body: PDF binary

**curl example:**

```bash
curl -X POST http://localhost:8080/api/v1/generate \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "data": {"name": "World"}
  }' \
  -o report.pdf
```

---

## Error Responses

All error responses follow a consistent format:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Descriptive error message",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

### Error Codes

| HTTP Status | Error | Cause |
|---|---|---|
| 400 | Bad Request | Invalid template JSON (`TemplateParseException`) |
| 400 | Bad Request | Invalid data payload (`DataParseException`) |
| 400 | Bad Request | Validation failure (missing required fields) |
| 404 | Not Found | Template ID not found (`ResourceNotFoundException`) |
| 500 | Internal Server Error | PDF rendering failure (`RenderException`) |
| 500 | Internal Server Error | Storage I/O error (`StorageException`) |
| 500 | Internal Server Error | Unexpected server error |

### Example Error Responses

**Invalid template:**

```bash
curl -X POST http://localhost:8080/api/v1/generate/inline \
  -H "Content-Type: application/json" \
  -d '{"template": "not-valid"}'
```

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid template: Failed to parse template JSON",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

**Template not found:**

```bash
curl http://localhost:8080/api/v1/templates/nonexistent-id
```

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Template not found: nonexistent-id",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

**Missing required field:**

```bash
curl -X POST http://localhost:8080/api/v1/generate \
  -H "Content-Type: application/json" \
  -d '{}'
```

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "templateId: must not be blank",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

---

## CORS Configuration

The API allows cross-origin requests with the following configuration:

| Setting | Value |
|---|---|
| Allowed Origins | `*` (all origins) |
| Allowed Methods | `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS` |
| Allowed Headers | `*` (all headers) |
| Path Pattern | `/api/**` |

For production deployments, restrict `allowedOrigins` to your application domain(s) by configuring the `WebConfig` class or using environment variables.

---

## Authentication

Authentication is not yet implemented. All endpoints are publicly accessible.

Future versions will support:

- API key authentication via `X-API-Key` header
- OAuth 2.0 / JWT bearer token authentication
- Role-based access control for template management vs. PDF generation

---

## OpenAPI / Swagger UI

The API includes auto-generated OpenAPI documentation via SpringDoc.

| Resource | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| OpenAPI YAML | `http://localhost:8080/v3/api-docs.yaml` |

The Swagger UI provides an interactive interface for exploring and testing all API endpoints.
