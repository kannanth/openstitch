import type { TemplateElement } from '../../types/template';
import { useTemplateStore } from '../../store/templateStore';
import { Bold, Italic, Underline, AlignLeft, AlignCenter, AlignRight, AlignJustify } from 'lucide-react';

interface Props {
  element: TemplateElement;
}

const fontFamilies = [
  'sans-serif',
  'serif',
  'monospace',
  'Arial',
  'Helvetica',
  'Times New Roman',
  'Courier New',
  'Georgia',
  'Verdana',
];

export function TextProperties({ element }: Props) {
  const updateElement = useTemplateStore((s) => s.updateElement);
  const style = element.style || {};

  const update = (updates: Partial<TemplateElement>) => {
    updateElement(element.id, updates);
  };

  const updateStyle = (styleUpdates: Partial<typeof style>) => {
    update({ style: { ...style, ...styleUpdates } });
  };

  const toggleBtn = (active: boolean) =>
    `p-1.5 rounded border ${active ? 'bg-blue-100 border-blue-400 text-blue-700' : 'border-gray-200 text-gray-500 hover:bg-gray-50'}`;

  return (
    <div className="space-y-3">
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Content
        </label>
        <textarea
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm resize-y min-h-[60px] focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={element.content || ''}
          onChange={(e) => update({ content: e.target.value })}
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Font Family
        </label>
        <select
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={style.fontFamily || 'sans-serif'}
          onChange={(e) => updateStyle({ fontFamily: e.target.value })}
        >
          {fontFamilies.map((f) => (
            <option key={f} value={f}>
              {f}
            </option>
          ))}
        </select>
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Font Size (px)
        </label>
        <input
          type="number"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={style.fontSize || 12}
          min={1}
          max={200}
          onChange={(e) => updateStyle({ fontSize: Number(e.target.value) })}
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Style
        </label>
        <div className="flex gap-1">
          <button
            className={toggleBtn(!!style.bold)}
            onClick={() => updateStyle({ bold: !style.bold })}
            title="Bold"
          >
            <Bold size={14} />
          </button>
          <button
            className={toggleBtn(!!style.italic)}
            onClick={() => updateStyle({ italic: !style.italic })}
            title="Italic"
          >
            <Italic size={14} />
          </button>
          <button
            className={toggleBtn(!!style.underline)}
            onClick={() => updateStyle({ underline: !style.underline })}
            title="Underline"
          >
            <Underline size={14} />
          </button>
        </div>
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Text Color
        </label>
        <div className="flex items-center gap-2">
          <input
            type="color"
            className="w-8 h-8 border border-gray-300 rounded cursor-pointer"
            value={style.textColor || '#000000'}
            onChange={(e) => updateStyle({ textColor: e.target.value })}
          />
          <input
            type="text"
            className="flex-1 border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
            value={style.textColor || '#000000'}
            onChange={(e) => updateStyle({ textColor: e.target.value })}
          />
        </div>
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Background Color
        </label>
        <div className="flex items-center gap-2">
          <input
            type="color"
            className="w-8 h-8 border border-gray-300 rounded cursor-pointer"
            value={style.backgroundColor || '#ffffff'}
            onChange={(e) => updateStyle({ backgroundColor: e.target.value })}
          />
          <input
            type="text"
            className="flex-1 border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
            value={style.backgroundColor || ''}
            placeholder="none"
            onChange={(e) => updateStyle({ backgroundColor: e.target.value || undefined })}
          />
        </div>
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Alignment
        </label>
        <div className="flex gap-1">
          <button
            className={toggleBtn(style.alignment === 'LEFT' || !style.alignment)}
            onClick={() => updateStyle({ alignment: 'LEFT' })}
            title="Left"
          >
            <AlignLeft size={14} />
          </button>
          <button
            className={toggleBtn(style.alignment === 'CENTER')}
            onClick={() => updateStyle({ alignment: 'CENTER' })}
            title="Center"
          >
            <AlignCenter size={14} />
          </button>
          <button
            className={toggleBtn(style.alignment === 'RIGHT')}
            onClick={() => updateStyle({ alignment: 'RIGHT' })}
            title="Right"
          >
            <AlignRight size={14} />
          </button>
          <button
            className={toggleBtn(style.alignment === 'JUSTIFIED')}
            onClick={() => updateStyle({ alignment: 'JUSTIFIED' })}
            title="Justified"
          >
            <AlignJustify size={14} />
          </button>
        </div>
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Line Height
        </label>
        <input
          type="number"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={style.lineHeight || ''}
          placeholder="1.5"
          step={0.1}
          min={0.5}
          max={5}
          onChange={(e) =>
            updateStyle({
              lineHeight: e.target.value ? Number(e.target.value) : undefined,
            })
          }
        />
      </div>
    </div>
  );
}
