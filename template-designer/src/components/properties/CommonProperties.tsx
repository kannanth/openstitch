import type { TemplateElement } from '../../types/template';
import { useTemplateStore } from '../../store/templateStore';

interface Props {
  element: TemplateElement;
}

export function CommonProperties({ element }: Props) {
  const updateElement = useTemplateStore((s) => s.updateElement);

  const update = (updates: Partial<TemplateElement>) => {
    updateElement(element.id, updates);
  };

  return (
    <div className="space-y-3">
      <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
        Layout
      </h4>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Positioning
        </label>
        <select
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={element.positioning}
          onChange={(e) => {
            const val = e.target.value as 'FLOW' | 'ABSOLUTE';
            if (val === 'ABSOLUTE' && !element.position) {
              update({ positioning: val, position: { x: 0, y: 0 } });
            } else {
              update({ positioning: val });
            }
          }}
        >
          <option value="FLOW">Flow</option>
          <option value="ABSOLUTE">Absolute</option>
        </select>
      </div>

      {element.positioning === 'ABSOLUTE' && (
        <>
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">
                X (pt)
              </label>
              <input
                type="number"
                className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
                value={element.position?.x ?? 0}
                onChange={(e) =>
                  update({
                    position: {
                      x: Number(e.target.value),
                      y: element.position?.y ?? 0,
                    },
                  })
                }
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">
                Y (pt)
              </label>
              <input
                type="number"
                className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
                value={element.position?.y ?? 0}
                onChange={(e) =>
                  update({
                    position: {
                      x: element.position?.x ?? 0,
                      y: Number(e.target.value),
                    },
                  })
                }
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">
                Width (pt)
              </label>
              <input
                type="number"
                className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
                value={element.dimension?.width ?? ''}
                placeholder="Auto"
                min={0}
                onChange={(e) =>
                  update({
                    dimension: {
                      width: e.target.value ? Number(e.target.value) : 0,
                      height: element.dimension?.height ?? 0,
                    },
                  })
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
                value={element.dimension?.height ?? ''}
                placeholder="Auto"
                min={0}
                onChange={(e) =>
                  update({
                    dimension: {
                      width: element.dimension?.width ?? 0,
                      height: e.target.value ? Number(e.target.value) : 0,
                    },
                  })
                }
              />
            </div>
          </div>
        </>
      )}

      {element.positioning === 'FLOW' && (
        <div className="grid grid-cols-2 gap-2">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Margin Top (pt)
            </label>
            <input
              type="number"
              className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
              value={element.marginTop || 0}
              min={0}
              onChange={(e) => update({ marginTop: Number(e.target.value) })}
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Margin Bottom (pt)
            </label>
            <input
              type="number"
              className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
              value={element.marginBottom || 0}
              min={0}
              onChange={(e) => update({ marginBottom: Number(e.target.value) })}
            />
          </div>
        </div>
      )}

      <div className="pt-2 border-t">
        <label className="block text-xs font-medium text-gray-500 mb-1">
          Element ID
        </label>
        <div className="text-xs text-gray-400 font-mono bg-gray-50 px-2 py-1 rounded select-all">
          {element.id}
        </div>
      </div>
    </div>
  );
}
