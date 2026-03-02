import { useTemplateStore } from '../../store/templateStore';
import { PageNumberingProperties } from './PageNumberingProperties';

export function PageLayoutProperties() {
  const template = useTemplateStore((s) => s.template);
  const updatePageLayout = useTemplateStore((s) => s.updatePageLayout);
  const updateMetadata = useTemplateStore((s) => s.updateMetadata);

  const { pageLayout, metadata } = template;
  const { margins } = pageLayout;

  return (
    <div className="space-y-4">
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Template Name
        </label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={metadata.name}
          onChange={(e) => updateMetadata({ name: e.target.value })}
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Description
        </label>
        <textarea
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm resize-y min-h-[40px] focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={metadata.description || ''}
          onChange={(e) => updateMetadata({ description: e.target.value })}
          placeholder="Template description..."
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Page Size
        </label>
        <select
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={pageLayout.pageSize}
          onChange={(e) =>
            updatePageLayout({
              pageSize: e.target.value as 'A4' | 'LETTER' | 'LEGAL' | 'CUSTOM',
            })
          }
        >
          <option value="A4">A4 (595 x 842)</option>
          <option value="LETTER">Letter (612 x 792)</option>
          <option value="LEGAL">Legal (612 x 1008)</option>
          <option value="CUSTOM">Custom</option>
        </select>
      </div>

      {pageLayout.pageSize === 'CUSTOM' && (
        <div className="grid grid-cols-2 gap-2">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Width (pt)
            </label>
            <input
              type="number"
              className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
              value={pageLayout.customWidth || 595}
              min={100}
              onChange={(e) =>
                updatePageLayout({ customWidth: Number(e.target.value) })
              }
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Height (pt)
            </label>
            <input
              type="number"
              className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
              value={pageLayout.customHeight || 842}
              min={100}
              onChange={(e) =>
                updatePageLayout({ customHeight: Number(e.target.value) })
              }
            />
          </div>
        </div>
      )}

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-2">
          Orientation
        </label>
        <div className="flex gap-1">
          <button
            className={`flex-1 px-3 py-1.5 text-xs rounded border ${
              pageLayout.orientation === 'PORTRAIT'
                ? 'bg-blue-100 border-blue-400 text-blue-700'
                : 'border-gray-200 text-gray-500 hover:bg-gray-50'
            }`}
            onClick={() => updatePageLayout({ orientation: 'PORTRAIT' })}
          >
            Portrait
          </button>
          <button
            className={`flex-1 px-3 py-1.5 text-xs rounded border ${
              pageLayout.orientation === 'LANDSCAPE'
                ? 'bg-blue-100 border-blue-400 text-blue-700'
                : 'border-gray-200 text-gray-500 hover:bg-gray-50'
            }`}
            onClick={() => updatePageLayout({ orientation: 'LANDSCAPE' })}
          >
            Landscape
          </button>
        </div>
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-2">
          Margins (pt)
        </label>
        <div className="grid grid-cols-2 gap-2">
          <div>
            <label className="block text-[10px] text-gray-400 mb-0.5">Top</label>
            <input
              type="number"
              className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
              value={margins.top}
              min={0}
              onChange={(e) =>
                updatePageLayout({
                  margins: { ...margins, top: Number(e.target.value) },
                })
              }
            />
          </div>
          <div>
            <label className="block text-[10px] text-gray-400 mb-0.5">Right</label>
            <input
              type="number"
              className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
              value={margins.right}
              min={0}
              onChange={(e) =>
                updatePageLayout({
                  margins: { ...margins, right: Number(e.target.value) },
                })
              }
            />
          </div>
          <div>
            <label className="block text-[10px] text-gray-400 mb-0.5">Bottom</label>
            <input
              type="number"
              className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
              value={margins.bottom}
              min={0}
              onChange={(e) =>
                updatePageLayout({
                  margins: { ...margins, bottom: Number(e.target.value) },
                })
              }
            />
          </div>
          <div>
            <label className="block text-[10px] text-gray-400 mb-0.5">Left</label>
            <input
              type="number"
              className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
              value={margins.left}
              min={0}
              onChange={(e) =>
                updatePageLayout({
                  margins: { ...margins, left: Number(e.target.value) },
                })
              }
            />
          </div>
        </div>
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Author
        </label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={metadata.author || ''}
          onChange={(e) => updateMetadata({ author: e.target.value })}
          placeholder="Author name"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Tags (comma-separated)
        </label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={(metadata.tags || []).join(', ')}
          onChange={(e) =>
            updateMetadata({
              tags: e.target.value
                .split(',')
                .map((s) => s.trim())
                .filter(Boolean),
            })
          }
          placeholder="invoice, report"
        />
      </div>

      <div className="border-t pt-4">
        <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-3">
          Page Numbering
        </h3>
        <PageNumberingProperties />
      </div>
    </div>
  );
}
