import { useState, useMemo } from 'react';
import { X } from 'lucide-react';
import { useUIStore } from '../../store/uiStore';
import { useTemplateStore } from '../../store/templateStore';
import { generateApi } from '../../api/templateApi';
import type { Template, TemplateElement } from '../../types/template';

/**
 * Builds context-aware sample data by scanning the template for
 * data sources, expression variables, and table column fields.
 */
function buildSampleData(template: Template): object {
  const data: Record<string, unknown> = {
    company: { name: 'Acme Corp', address: '123 Main St, Springfield, IL 62701' },
    reportDate: '2026-01-15',
    reportTitle: 'Sample Report',
    reportContent: 'This is sample content for the report body.',
    reportMonth: 'January 2026',
    invoiceNumber: 'INV-1001',
    invoiceDate: '2026-01-15',
    dueDate: '2026-02-14',
    paymentTerms: 'Net 30',
    customer: { name: 'Jane Smith', address: '456 Oak Ave, Chicago, IL 60601' },
    employee: { name: 'John Doe', department: 'Engineering' },
    periodStart: '2026-01-01',
    periodEnd: '2026-01-31',
  };

  // Collect all elements across all sections
  const allElements: TemplateElement[] = [
    ...(template.header?.defaultElements ?? []),
    ...(template.footer?.defaultElements ?? []),
  ];

  // Collect elements from each body section
  for (const section of template.body.sections) {
    if (section.sectionHeader) {
      allElements.push(...section.sectionHeader.elements);
    }
    allElements.push(...section.elements);
    if (section.sectionFooter) {
      allElements.push(...section.sectionFooter.elements);
    }
  }

  // For each TABLE or REPEATING_SECTION element, generate sample rows
  for (const el of allElements) {
    if ((el.type === 'TABLE' || el.type === 'REPEATING_SECTION') && el.dataSource) {
      const ds = el.dataSource;
      if (data[ds] !== undefined) continue; // already provided

      if (el.columns && el.columns.length > 0) {
        const cols = el.columns;
        const rows: Record<string, unknown>[] = [];
        for (let r = 0; r < 3; r++) {
          const row: Record<string, unknown> = {};
          for (let c = 0; c < cols.length; c++) {
            const field = cols[c].field;
            if (!field) continue;
            const align = cols[c].alignment ?? 'LEFT';
            if (align === 'RIGHT' || cols[c].formatPreset) {
              // Numeric field
              row[field] = Math.round((r + 1) * 1000 + Math.random() * 500);
            } else {
              row[field] = `${cols[c].header ?? field} ${r + 1}`;
            }
          }
          rows.push(row);
        }
        data[ds] = rows;
      } else {
        data[ds] = [
          { field1: 'Value 1', name: 'Item A', value: 100 },
          { field1: 'Value 2', name: 'Item B', value: 200 },
          { field1: 'Value 3', name: 'Item C', value: 150 },
        ];
      }
    }

    // CHART elements
    if (el.type === 'CHART' && el.dataSource && data[el.dataSource] === undefined) {
      const catField = el.categoryField ?? 'name';
      const valFields = el.valueFields ?? ['value'];
      const rows: Record<string, unknown>[] = [];
      for (let r = 0; r < 4; r++) {
        const row: Record<string, unknown> = { [catField]: `Category ${r + 1}` };
        for (const vf of valFields) {
          row[vf] = Math.round(Math.random() * 1000 + 200);
        }
        rows.push(row);
      }
      data[el.dataSource] = rows;
    }
  }

  // Also generate data for section-level data sources
  for (const section of template.body.sections) {
    if (section.dataSource && data[section.dataSource] === undefined) {
      data[section.dataSource] = [
        { name: 'Item 1', value: 100 },
        { name: 'Item 2', value: 200 },
        { name: 'Item 3', value: 300 },
      ];
    }
  }

  return data;
}

export function PreviewModal() {
  const setPreviewOpen = useUIStore((s) => s.setPreviewOpen);
  const template = useTemplateStore((s) => s.template);

  const initialData = useMemo(
    () => JSON.stringify(buildSampleData(template), null, 2),
    [template]
  );

  const [sampleData, setSampleData] = useState(initialData);
  const [pdfUrl, setPdfUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleGenerate = async () => {
    setLoading(true);
    setError(null);
    setPdfUrl(null);

    try {
      const data = JSON.parse(sampleData);
      const response = await generateApi.inline(template, data);
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = URL.createObjectURL(blob);
      setPdfUrl(url);
    } catch (err: unknown) {
      if (err instanceof SyntaxError) {
        setError('Invalid JSON in sample data');
      } else if (err instanceof Error) {
        setError(`Generation failed: ${err.message}`);
      } else {
        setError('An unknown error occurred');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (pdfUrl) URL.revokeObjectURL(pdfUrl);
    setPreviewOpen(false);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-white rounded-lg shadow-2xl w-[90vw] h-[85vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b">
          <h2 className="text-lg font-semibold text-gray-800">
            PDF Preview
          </h2>
          <button
            className="p-1 rounded hover:bg-gray-100 text-gray-500"
            onClick={handleClose}
            title="Close"
          >
            <X size={20} />
          </button>
        </div>

        {/* Body */}
        <div className="flex flex-1 overflow-hidden">
          {/* Left: Sample data editor */}
          <div className="w-1/3 border-r flex flex-col p-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Sample Data (JSON)
            </label>
            <textarea
              className="flex-1 w-full border border-gray-300 rounded px-3 py-2 text-sm font-mono resize-none focus:outline-none focus:ring-1 focus:ring-blue-400"
              value={sampleData}
              onChange={(e) => setSampleData(e.target.value)}
              spellCheck={false}
            />

            <div className="mt-3 flex gap-2">
              <button
                className="flex-1 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                onClick={handleGenerate}
                disabled={loading}
              >
                {loading ? 'Generating...' : 'Generate Preview'}
              </button>
            </div>

            {error && (
              <div className="mt-2 text-sm text-red-600 bg-red-50 rounded p-2">
                {error}
              </div>
            )}
          </div>

          {/* Right: PDF preview */}
          <div className="flex-1 bg-gray-100 flex items-center justify-center p-4">
            {pdfUrl ? (
              <iframe
                src={pdfUrl}
                className="w-full h-full rounded border border-gray-300"
                title="PDF Preview"
              />
            ) : (
              <div className="text-center text-gray-400">
                <p className="text-lg font-medium">No preview yet</p>
                <p className="text-sm mt-1">
                  Enter sample data and click "Generate Preview" to see the
                  PDF output.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
