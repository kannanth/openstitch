import { useTemplateStore } from '../../store/templateStore';
import type { PageNumbering } from '../../types/template';

export function PageNumberingProperties() {
  const pageNumbering = useTemplateStore((s) => s.template.pageNumbering);
  const togglePageNumbering = useTemplateStore((s) => s.togglePageNumbering);
  const updatePageNumbering = useTemplateStore((s) => s.updatePageNumbering);

  return (
    <div>
      <label className="flex items-center gap-2 text-xs font-medium text-gray-600 cursor-pointer">
        <input
          type="checkbox"
          checked={!!pageNumbering}
          onChange={togglePageNumbering}
          className="rounded border-gray-300"
        />
        Show page numbers
      </label>

      {pageNumbering && (
        <div className="mt-3 space-y-3">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Format
            </label>
            <select
              className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
              value={pageNumbering.format}
              onChange={(e) =>
                updatePageNumbering({
                  format: e.target.value as PageNumbering['format'],
                })
              }
            >
              <option value="PAGE_X_OF_Y">Page X of Y</option>
              <option value="PAGE_X">Page X</option>
              <option value="ROMAN">Roman Numerals</option>
              <option value="CUSTOM">Custom</option>
            </select>
          </div>

          {pageNumbering.format === 'CUSTOM' && (
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">
                Custom Format
              </label>
              <input
                type="text"
                className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
                value={pageNumbering.customFormat || ''}
                onChange={(e) =>
                  updatePageNumbering({ customFormat: e.target.value })
                }
                placeholder="{page} / {total}"
              />
            </div>
          )}

          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Start From
            </label>
            <input
              type="number"
              className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
              value={pageNumbering.startFrom}
              min={0}
              onChange={(e) =>
                updatePageNumbering({ startFrom: Number(e.target.value) })
              }
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Font Size
            </label>
            <input
              type="number"
              className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
              value={pageNumbering.style?.fontSize || 10}
              min={6}
              max={24}
              onChange={(e) =>
                updatePageNumbering({
                  style: { ...pageNumbering.style, fontSize: Number(e.target.value) },
                })
              }
            />
          </div>
        </div>
      )}
    </div>
  );
}
