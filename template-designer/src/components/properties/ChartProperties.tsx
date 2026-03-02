import type { TemplateElement } from '../../types/template';
import { useTemplateStore } from '../../store/templateStore';

interface Props {
  element: TemplateElement;
}

export function ChartProperties({ element }: Props) {
  const updateElement = useTemplateStore((s) => s.updateElement);

  const update = (updates: Partial<TemplateElement>) => {
    updateElement(element.id, updates);
  };

  const dim = element.dimension || { width: 400, height: 300 };
  const valueFields = element.valueFields || [];

  return (
    <div className="space-y-3">
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Chart Type
        </label>
        <select
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={element.chartType || 'BAR'}
          onChange={(e) =>
            update({ chartType: e.target.value as 'BAR' | 'PIE' | 'LINE' })
          }
        >
          <option value="BAR">Bar</option>
          <option value="PIE">Pie</option>
          <option value="LINE">Line</option>
        </select>
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Title
        </label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={element.title || ''}
          onChange={(e) => update({ title: e.target.value })}
          placeholder="Chart title"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Data Source
        </label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={element.dataSource || ''}
          onChange={(e) => update({ dataSource: e.target.value })}
          placeholder="e.g. chartData"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Category Field
        </label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={element.categoryField || ''}
          onChange={(e) => update({ categoryField: e.target.value })}
          placeholder="e.g. name"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Value Fields (comma-separated)
        </label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={valueFields.join(', ')}
          onChange={(e) =>
            update({
              valueFields: e.target.value
                .split(',')
                .map((s) => s.trim())
                .filter(Boolean),
            })
          }
          placeholder="e.g. value, count"
        />
      </div>

      <div className="flex items-center gap-2">
        <input
          type="checkbox"
          id="showLegend"
          checked={element.showLegend !== false}
          onChange={(e) => update({ showLegend: e.target.checked })}
          className="rounded border-gray-300"
        />
        <label htmlFor="showLegend" className="text-xs text-gray-600">
          Show Legend
        </label>
      </div>

      <div className="grid grid-cols-2 gap-2">
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">
            Width (px)
          </label>
          <input
            type="number"
            className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
            value={dim.width}
            min={1}
            onChange={(e) =>
              update({ dimension: { ...dim, width: Number(e.target.value) } })
            }
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">
            Height (px)
          </label>
          <input
            type="number"
            className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
            value={dim.height}
            min={1}
            onChange={(e) =>
              update({ dimension: { ...dim, height: Number(e.target.value) } })
            }
          />
        </div>
      </div>
    </div>
  );
}
