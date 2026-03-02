# OpenStitch Template Schema Reference

This document describes the complete JSON schema for OpenStitch templates.

---

## Top-Level Structure

```json
{
  "metadata": { ... },
  "pageLayout": { ... },
  "header": { ... },
  "subHeader": { ... },
  "body": { ... },
  "subFooter": { ... },
  "footer": { ... },
  "pageNumbering": { ... }
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `metadata` | object | No | Template identification and versioning information |
| `pageLayout` | object | No | Page size, orientation, and margins (defaults to A4 portrait) |
| `header` | object | No | Page header definition with support for first-page and odd/even variants |
| `subHeader` | object | No | Sub-header rendered below the main header (first page only) |
| `body` | object | Yes | The main content area containing the element list |
| `subFooter` | object | No | Sub-footer rendered above the main footer |
| `footer` | object | No | Page footer definition with support for first-page and odd/even variants |
| `pageNumbering` | object | No | Automatic page number rendering configuration |

---

## Metadata

```json
{
  "metadata": {
    "id": "invoice-template-001",
    "name": "Invoice Template",
    "description": "Standard customer invoice",
    "author": "engineering",
    "tags": ["invoice", "finance"],
    "version": 1,
    "createdAt": "2025-01-15T10:30:00Z",
    "updatedAt": "2025-01-15T10:30:00Z"
  }
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `id` | string | auto-generated | Unique template identifier |
| `name` | string | -- | Human-readable template name |
| `description` | string | -- | Template description |
| `author` | string | -- | Template author |
| `tags` | string[] | -- | Tags for filtering and categorization |
| `version` | integer | `1` | Template version number (auto-incremented on update) |
| `createdAt` | ISO-8601 | auto | Creation timestamp |
| `updatedAt` | ISO-8601 | auto | Last update timestamp |

---

## Page Layout

```json
{
  "pageLayout": {
    "pageSize": "A4",
    "orientation": "PORTRAIT",
    "margins": {
      "top": 72,
      "right": 72,
      "bottom": 72,
      "left": 72
    }
  }
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `pageSize` | enum | `A4` | One of: `A4`, `LETTER`, `LEGAL`, `CUSTOM` |
| `orientation` | enum | `PORTRAIT` | One of: `PORTRAIT`, `LANDSCAPE` |
| `margins` | object | 72pt all sides | Page margins in points (72 points = 1 inch) |
| `customWidth` | number | -- | Width in points (only used when `pageSize` is `CUSTOM`) |
| `customHeight` | number | -- | Height in points (only used when `pageSize` is `CUSTOM`) |

### Standard Page Sizes (in points)

| Size | Width | Height |
|---|---|---|
| A4 | 595.28 | 841.89 |
| LETTER | 612 | 792 |
| LEGAL | 612 | 1008 |

---

## Header and Footer

Headers and footers support three modes: default, first-page-different, and odd/even-different.

```json
{
  "header": {
    "height": 60,
    "firstPageDifferent": true,
    "oddEvenDifferent": false,
    "defaultElements": [ ... ],
    "firstPageElements": [ ... ],
    "oddPageElements": [ ... ],
    "evenPageElements": [ ... ]
  }
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `height` | number | `50` | Height reserved for the header/footer in points |
| `firstPageDifferent` | boolean | `false` | Use `firstPageElements` on the first page |
| `oddEvenDifferent` | boolean | `false` | Use `oddPageElements`/`evenPageElements` based on page number |
| `defaultElements` | Element[] | -- | Elements rendered on all pages (unless overridden) |
| `firstPageElements` | Element[] | -- | Elements rendered on the first page only |
| `oddPageElements` | Element[] | -- | Elements rendered on odd-numbered pages |
| `evenPageElements` | Element[] | -- | Elements rendered on even-numbered pages |

Footer has the same structure and fields as header.

---

## Sub-Header and Sub-Footer

Simple section definitions with a fixed height and element list.

```json
{
  "subHeader": {
    "height": 30,
    "elements": [ ... ]
  }
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `height` | number | `30` | Height in points |
| `elements` | Element[] | -- | Elements to render in this section |

---

## Body

```json
{
  "body": {
    "elements": [ ... ]
  }
}
```

The body contains the main content as an ordered list of elements. Elements are rendered top-to-bottom in flow layout. Page breaks are inserted automatically when content exceeds the available space.

---

## Element Types

Every element shares a common base structure:

```json
{
  "id": "element-1",
  "type": "TEXT",
  "positioning": "FLOW",
  "position": { "x": 0, "y": 0 },
  "dimension": { "width": 200, "height": 50 },
  "style": { ... },
  "marginTop": 10,
  "marginBottom": 5
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `id` | string | -- | Optional element identifier |
| `type` | enum | -- | **Required.** One of: `TEXT`, `TABLE`, `IMAGE`, `CHART`, `CONDITIONAL`, `REPEATING_SECTION` |
| `positioning` | enum | `FLOW` | `FLOW` (sequential layout) or `ABSOLUTE` (fixed x/y coordinates) |
| `position` | object | -- | Position for absolute positioning (`x`, `y` in points) |
| `dimension` | object | -- | Explicit width and height in points |
| `style` | object | -- | Visual styling (see Style Properties below) |
| `marginTop` | number | `0` | Top margin in points |
| `marginBottom` | number | `0` | Bottom margin in points |

---

### TEXT

Renders a text block with expression support.

```json
{
  "type": "TEXT",
  "content": "Invoice for ${company.name}",
  "style": {
    "fontSize": 18,
    "bold": true,
    "textColor": "#333333",
    "alignment": "CENTER"
  }
}
```

| Field | Type | Description |
|---|---|---|
| `content` | string | Text content. Supports `${...}` expressions. |

---

### TABLE

Renders a data-bound table with headers, rows, optional alternating row colors, borders, and footer cells.

```json
{
  "type": "TABLE",
  "dataSource": "invoice.items",
  "showHeader": true,
  "columns": [
    { "header": "Description", "field": "description", "width": 200, "alignment": "LEFT" },
    { "header": "Qty", "field": "quantity", "width": 60, "alignment": "CENTER" },
    { "header": "Unit Price", "field": "unitPrice", "width": 100, "alignment": "RIGHT", "format": "#,##0.00" },
    { "header": "Amount", "field": "amount", "width": 100, "alignment": "RIGHT", "format": "#,##0.00" }
  ],
  "headerStyle": {
    "bold": true,
    "backgroundColor": "#4472C4",
    "textColor": "#FFFFFF",
    "fontSize": 10
  },
  "rowStyle": {
    "fontSize": 10,
    "padding": { "top": 4, "right": 6, "bottom": 4, "left": 6 }
  },
  "alternateRowColor": "#F2F2F2",
  "borderStyle": {
    "width": 0.5,
    "color": "#CCCCCC",
    "style": "SOLID"
  },
  "footerCells": [
    { "content": "", "colSpan": 2 },
    { "content": "Total:", "colSpan": 1 },
    { "content": "${format(sum(invoice.items, 'amount'), '#,##0.00')}", "colSpan": 1 }
  ]
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `dataSource` | string | -- | Dot-notation path to the array in the data context |
| `columns` | TableColumn[] | -- | Column definitions (see below) |
| `showHeader` | boolean | `true` | Whether to render the header row |
| `headerStyle` | Style | -- | Style applied to the header row |
| `rowStyle` | Style | -- | Style applied to data rows |
| `alternateRowColor` | string | -- | Background color for even rows (hex, e.g., `#F2F2F2`) |
| `borderStyle` | Border | -- | Table border configuration |
| `footerCells` | TableFooterCell[] | -- | Optional footer row cells |

#### TableColumn

| Field | Type | Default | Description |
|---|---|---|---|
| `header` | string | -- | Column header text |
| `field` | string | -- | Dot-notation field path within each row object |
| `width` | number | auto | Column width in points |
| `alignment` | enum | `LEFT` | `LEFT`, `CENTER`, `RIGHT`, `JUSTIFIED` |
| `format` | string | -- | `DecimalFormat` pattern for numeric values |

#### TableFooterCell

| Field | Type | Default | Description |
|---|---|---|---|
| `content` | string | -- | Cell content (supports expressions) |
| `colSpan` | integer | `1` | Number of columns this cell spans |

---

### IMAGE

Renders an image from a static base64 string, a data field reference, or a URL.

```json
{
  "type": "IMAGE",
  "source": "STATIC",
  "data": "iVBORw0KGgoAAAANSUhEUg...",
  "fit": "CONTAIN",
  "dimension": { "width": 150, "height": 50 }
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `source` | enum | -- | `STATIC` (base64 in `data`), `DATA_FIELD` (expression path in `data`), `URL` (URL in `data`) |
| `data` | string | -- | The image data, field path, or URL depending on `source` |
| `fit` | enum | `CONTAIN` | `CONTAIN` (scale to fit), `COVER` (fill area, may crop), `STRETCH` (distort to exact size) |

---

### CHART

Renders a chart from data using JFreeChart.

```json
{
  "type": "CHART",
  "chartType": "BAR",
  "dataSource": "salesData",
  "categoryField": "month",
  "valueFields": ["revenue", "profit"],
  "title": "Monthly Sales",
  "showLegend": true,
  "axisLabels": {
    "x": "Month",
    "y": "Amount ($)"
  },
  "colors": ["#4472C4", "#ED7D31"],
  "dimension": { "width": 450, "height": 300 }
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `chartType` | enum | -- | `BAR`, `PIE`, `LINE` |
| `dataSource` | string | -- | Dot-notation path to the data array |
| `categoryField` | string | -- | Field name for category axis / pie labels |
| `valueFields` | string[] | -- | Field names for value axis / pie values |
| `title` | string | -- | Chart title |
| `showLegend` | boolean | `true` | Whether to show the legend |
| `axisLabels` | map | -- | Axis labels (`x` and `y` keys) |
| `colors` | string[] | -- | Custom colors for data series (hex values) |

---

### CONDITIONAL

Renders one set of elements if a condition is true, and optionally another set if false.

```json
{
  "type": "CONDITIONAL",
  "condition": "customer.isVip == true",
  "thenElements": [
    {
      "type": "TEXT",
      "content": "VIP Customer - 10% discount applied",
      "style": { "bold": true, "textColor": "#006600" }
    }
  ],
  "elseElements": [
    {
      "type": "TEXT",
      "content": "Standard pricing applied"
    }
  ]
}
```

| Field | Type | Description |
|---|---|---|
| `condition` | string | Expression to evaluate as a boolean |
| `thenElements` | Element[] | Elements rendered when condition is true |
| `elseElements` | Element[] | Elements rendered when condition is false (optional) |

---

### REPEATING_SECTION

Iterates over a data array and renders its child elements once per item.

```json
{
  "type": "REPEATING_SECTION",
  "dataSource": "departments",
  "pageBreakBetween": true,
  "separator": {
    "height": 1,
    "color": "#CCCCCC"
  },
  "elements": [
    {
      "type": "TEXT",
      "content": "Department: ${name}",
      "style": { "fontSize": 16, "bold": true }
    },
    {
      "type": "TABLE",
      "dataSource": "employees",
      "columns": [
        { "header": "Name", "field": "name" },
        { "header": "Role", "field": "role" }
      ]
    }
  ]
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `dataSource` | string | -- | Dot-notation path to the array to iterate |
| `elements` | Element[] | -- | Elements rendered for each item (data context is scoped to the current item) |
| `pageBreakBetween` | boolean | `false` | Insert a page break between iterations |
| `separator` | object | -- | Optional visual separator between iterations |

#### Separator

| Field | Type | Description |
|---|---|---|
| `height` | number | Separator line height in points |
| `color` | string | Separator line color (hex) |

---

## Page Numbering

```json
{
  "pageNumbering": {
    "enabled": true,
    "format": "PAGE_X_OF_Y",
    "position": { "x": 500, "y": 30 },
    "startFrom": 1,
    "style": {
      "fontSize": 9,
      "textColor": "#999999"
    },
    "customFormat": "Page %d of %d"
  }
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `enabled` | boolean | `false` | Enable automatic page numbering |
| `format` | enum | -- | `PAGE_X_OF_Y`, `PAGE_X`, `ROMAN`, `CUSTOM` |
| `position` | object | -- | Position on the page (`x`, `y` in points) |
| `startFrom` | integer | `1` | Starting page number |
| `style` | Style | -- | Text style for the page number |
| `customFormat` | string | -- | Custom `String.format` pattern (used when `format` is `CUSTOM`) |

---

## Style Properties

Style objects can be applied to most elements, headers, footers, and table rows.

```json
{
  "fontFamily": "Helvetica",
  "fontSize": 12,
  "bold": false,
  "italic": false,
  "underline": false,
  "textColor": "#000000",
  "backgroundColor": "#FFFFFF",
  "alignment": "LEFT",
  "lineHeight": 1.2,
  "padding": {
    "top": 4,
    "right": 6,
    "bottom": 4,
    "left": 6
  }
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `fontFamily` | string | `Helvetica` | Font family name |
| `fontSize` | number | `12` | Font size in points |
| `bold` | boolean | `false` | Bold text |
| `italic` | boolean | `false` | Italic text |
| `underline` | boolean | `false` | Underlined text |
| `textColor` | string | `#000000` | Text color (hex) |
| `backgroundColor` | string | `null` | Background fill color (hex) |
| `alignment` | enum | `LEFT` | `LEFT`, `CENTER`, `RIGHT`, `JUSTIFIED` |
| `lineHeight` | number | `1.2` | Line height multiplier |
| `padding` | object | `0` all | Inner padding (`top`, `right`, `bottom`, `left` in points) |

---

## Border Properties

```json
{
  "width": 1.0,
  "color": "#000000",
  "style": "SOLID"
}
```

| Field | Type | Default | Description |
|---|---|---|---|
| `width` | number | `1.0` | Border width in points |
| `color` | string | `#000000` | Border color (hex) |
| `style` | enum | `SOLID` | `SOLID`, `DASHED`, `DOTTED`, `NONE` |

---

## Expression Syntax

Expressions are enclosed in `${...}` and can appear in any text content field.

### Variable References

```
${company.name}           -- simple dot-notation path
${items[0].description}   -- array index access
${address.city}            -- nested object traversal
```

### Function Calls

```
${format(amount, "#,##0.00")}      -- number formatting
${upper(customer.name)}             -- string transformation
${sum(invoice.items, "amount")}     -- aggregation
${count(items)}                      -- list counting
${now()}                             -- current timestamp
${dateFormat("2025-01-15", "MM/dd/yyyy")}  -- date formatting
```

### Operators

```
${price * quantity}                  -- arithmetic
${subtotal + tax}                    -- addition
${total > 1000}                      -- comparison (returns boolean)
${status == "active"}                -- equality
${hasDiscount && total > 100}        -- logical AND
${isVip || total > 500}              -- logical OR
```

---

## Example Templates

### Simple Text Document

```json
{
  "pageLayout": {
    "pageSize": "LETTER",
    "orientation": "PORTRAIT",
    "margins": { "top": 72, "right": 72, "bottom": 72, "left": 72 }
  },
  "body": {
    "elements": [
      {
        "type": "TEXT",
        "content": "Hello, ${name}!",
        "style": { "fontSize": 24, "bold": true, "alignment": "CENTER" }
      },
      {
        "type": "TEXT",
        "content": "Welcome to OpenStitch. This document was generated on ${now()}.",
        "style": { "fontSize": 12 },
        "marginTop": 20
      }
    ]
  }
}
```

### Table Report

```json
{
  "pageLayout": { "pageSize": "A4" },
  "header": {
    "height": 40,
    "defaultElements": [
      {
        "type": "TEXT",
        "content": "Employee Report",
        "style": { "fontSize": 14, "bold": true }
      }
    ]
  },
  "body": {
    "elements": [
      {
        "type": "TABLE",
        "dataSource": "employees",
        "columns": [
          { "header": "Name", "field": "name", "width": 200 },
          { "header": "Department", "field": "department", "width": 150 },
          { "header": "Salary", "field": "salary", "width": 100, "alignment": "RIGHT", "format": "#,##0.00" }
        ],
        "headerStyle": {
          "bold": true,
          "backgroundColor": "#2E75B6",
          "textColor": "#FFFFFF"
        },
        "alternateRowColor": "#D6E4F0",
        "borderStyle": { "width": 0.5, "color": "#999999", "style": "SOLID" }
      }
    ]
  },
  "pageNumbering": {
    "enabled": true,
    "format": "PAGE_X_OF_Y",
    "position": { "x": 480, "y": 30 },
    "style": { "fontSize": 9, "textColor": "#666666" }
  }
}
```

### Full Invoice

```json
{
  "metadata": {
    "name": "Standard Invoice",
    "tags": ["invoice", "finance"]
  },
  "pageLayout": {
    "pageSize": "A4",
    "orientation": "PORTRAIT",
    "margins": { "top": 50, "right": 50, "bottom": 60, "left": 50 }
  },
  "header": {
    "height": 80,
    "defaultElements": [
      {
        "type": "TEXT",
        "content": "${company.name}",
        "style": { "fontSize": 22, "bold": true, "textColor": "#2E75B6" }
      },
      {
        "type": "TEXT",
        "content": "${company.address}",
        "style": { "fontSize": 9, "textColor": "#666666" },
        "marginTop": 4
      }
    ]
  },
  "body": {
    "elements": [
      {
        "type": "TEXT",
        "content": "INVOICE",
        "style": { "fontSize": 28, "bold": true, "alignment": "RIGHT", "textColor": "#333333" },
        "marginBottom": 20
      },
      {
        "type": "TEXT",
        "content": "Invoice #: ${invoice.number}",
        "style": { "fontSize": 11 }
      },
      {
        "type": "TEXT",
        "content": "Date: ${dateFormat(invoice.date, 'MMMM dd, yyyy')}",
        "style": { "fontSize": 11 },
        "marginBottom": 15
      },
      {
        "type": "TEXT",
        "content": "Bill To: ${customer.name}",
        "style": { "fontSize": 12, "bold": true },
        "marginBottom": 5
      },
      {
        "type": "TEXT",
        "content": "${customer.address}",
        "style": { "fontSize": 10, "textColor": "#555555" },
        "marginBottom": 20
      },
      {
        "type": "TABLE",
        "dataSource": "invoice.items",
        "columns": [
          { "header": "Description", "field": "description", "width": 220 },
          { "header": "Qty", "field": "quantity", "width": 50, "alignment": "CENTER" },
          { "header": "Unit Price", "field": "unitPrice", "width": 100, "alignment": "RIGHT", "format": "#,##0.00" },
          { "header": "Amount", "field": "amount", "width": 100, "alignment": "RIGHT", "format": "#,##0.00" }
        ],
        "headerStyle": {
          "bold": true,
          "backgroundColor": "#2E75B6",
          "textColor": "#FFFFFF",
          "fontSize": 10,
          "padding": { "top": 6, "right": 8, "bottom": 6, "left": 8 }
        },
        "rowStyle": {
          "fontSize": 10,
          "padding": { "top": 5, "right": 8, "bottom": 5, "left": 8 }
        },
        "alternateRowColor": "#F2F7FB",
        "borderStyle": { "width": 0.5, "color": "#CCCCCC", "style": "SOLID" },
        "footerCells": [
          { "content": "", "colSpan": 2 },
          { "content": "Subtotal:", "colSpan": 1 },
          { "content": "${format(invoice.subtotal, '#,##0.00')}", "colSpan": 1 }
        ],
        "marginBottom": 10
      },
      {
        "type": "CONDITIONAL",
        "condition": "invoice.discount > 0",
        "thenElements": [
          {
            "type": "TEXT",
            "content": "Discount: -${format(invoice.discount, '#,##0.00')}",
            "style": { "fontSize": 11, "alignment": "RIGHT", "textColor": "#CC0000" }
          }
        ]
      },
      {
        "type": "TEXT",
        "content": "Tax (${invoice.taxRate}%): ${format(invoice.tax, '#,##0.00')}",
        "style": { "fontSize": 11, "alignment": "RIGHT" },
        "marginTop": 5
      },
      {
        "type": "TEXT",
        "content": "Total: ${format(invoice.total, '#,##0.00')}",
        "style": { "fontSize": 16, "bold": true, "alignment": "RIGHT", "textColor": "#2E75B6" },
        "marginTop": 10,
        "marginBottom": 30
      },
      {
        "type": "TEXT",
        "content": "${invoice.notes}",
        "style": { "fontSize": 9, "italic": true, "textColor": "#888888" }
      }
    ]
  },
  "footer": {
    "height": 40,
    "defaultElements": [
      {
        "type": "TEXT",
        "content": "Thank you for your business!",
        "style": { "fontSize": 10, "italic": true, "alignment": "CENTER", "textColor": "#999999" }
      }
    ]
  },
  "pageNumbering": {
    "enabled": true,
    "format": "PAGE_X_OF_Y",
    "position": { "x": 480, "y": 25 },
    "style": { "fontSize": 8, "textColor": "#AAAAAA" }
  }
}
```
