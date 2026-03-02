import type { TemplateElement } from '../../types/template';
import { useTemplateStore } from '../../store/templateStore';

interface Props {
  element: TemplateElement;
}

export function ImageProperties({ element }: Props) {
  const updateElement = useTemplateStore((s) => s.updateElement);

  const update = (updates: Partial<TemplateElement>) => {
    updateElement(element.id, updates);
  };

  const dim = element.dimension || { width: 200, height: 150 };

  return (
    <div className="space-y-3">
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Source Type
        </label>
        <select
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={element.source || 'STATIC'}
          onChange={(e) =>
            update({ source: e.target.value as 'STATIC' | 'DATA_FIELD' | 'URL' })
          }
        >
          <option value="STATIC">Static</option>
          <option value="DATA_FIELD">Data Field</option>
          <option value="URL">URL</option>
        </select>
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          {element.source === 'DATA_FIELD'
            ? 'Field Name'
            : element.source === 'URL'
              ? 'Image URL'
              : 'Image Data (base64)'}
        </label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={element.data || ''}
          onChange={(e) => update({ data: e.target.value })}
          placeholder={
            element.source === 'DATA_FIELD'
              ? 'e.g. logo'
              : element.source === 'URL'
                ? 'https://...'
                : 'base64 data'
          }
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Fit Mode
        </label>
        <select
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={element.fit || 'CONTAIN'}
          onChange={(e) =>
            update({ fit: e.target.value as 'CONTAIN' | 'COVER' | 'STRETCH' })
          }
        >
          <option value="CONTAIN">Contain</option>
          <option value="COVER">Cover</option>
          <option value="STRETCH">Stretch</option>
        </select>
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
              update({
                dimension: { ...dim, width: Number(e.target.value) },
              })
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
              update({
                dimension: { ...dim, height: Number(e.target.value) },
              })
            }
          />
        </div>
      </div>
    </div>
  );
}
