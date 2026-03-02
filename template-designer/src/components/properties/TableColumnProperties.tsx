import type { TableColumn, ConditionalFormat, NumberFormatPreset } from '../../types/template';
import { Plus, Trash2 } from 'lucide-react';

interface Props {
  column: TableColumn;
  index: number;
  onUpdate: (index: number, updates: Partial<TableColumn>) => void;
  onRemove: (index: number) => void;
}

const FORMAT_PRESETS: { value: NumberFormatPreset; label: string }[] = [
  { value: 'CURRENCY_USD', label: 'Currency (USD)' },
  { value: 'CURRENCY_EUR', label: 'Currency (EUR)' },
  { value: 'CURRENCY_GBP', label: 'Currency (GBP)' },
  { value: 'PERCENTAGE', label: 'Percentage' },
  { value: 'ACCOUNTING', label: 'Accounting' },
  { value: 'NUMBER_2DP', label: 'Number (2 dp)' },
  { value: 'NUMBER_0DP', label: 'Number (0 dp)' },
  { value: 'CUSTOM', label: 'Custom' },
];

function presetToFormat(preset: NumberFormatPreset): string {
  switch (preset) {
    case 'CURRENCY_USD': return '$#,##0.00';
    case 'CURRENCY_EUR': return '€#,##0.00';
    case 'CURRENCY_GBP': return '£#,##0.00';
    case 'PERCENTAGE': return '#,##0.00%';
    case 'ACCOUNTING': return '#,##0.00;(#,##0.00)';
    case 'NUMBER_2DP': return '#,##0.00';
    case 'NUMBER_0DP': return '#,##0';
    case 'CUSTOM': return '';
  }
}

export function TableColumnProperties({ column, index, onUpdate, onRemove }: Props) {
  const conditionalFormats = column.conditionalFormats ?? [];

  const addConditionalFormat = () => {
    const newFormat: ConditionalFormat = { condition: 'value < 0', style: { textColor: '#ff0000' } };
    onUpdate(index, {
      conditionalFormats: [...conditionalFormats, newFormat],
    });
  };

  const updateConditionalFormat = (cfIndex: number, updates: Partial<ConditionalFormat>) => {
    const updated = conditionalFormats.map((cf, i) =>
      i === cfIndex ? { ...cf, ...updates } : cf
    );
    onUpdate(index, { conditionalFormats: updated });
  };

  const removeConditionalFormat = (cfIndex: number) => {
    onUpdate(index, {
      conditionalFormats: conditionalFormats.filter((_, i) => i !== cfIndex),
    });
  };

  return (
    <div className="border border-gray-200 rounded p-2 bg-gray-50">
      <div className="flex items-center justify-between mb-1">
        <span className="text-xs font-medium text-gray-500">Column {index + 1}</span>
        <button
          className="text-red-400 hover:text-red-600 p-0.5"
          onClick={() => onRemove(index)}
          title="Remove column"
        >
          <Trash2 size={12} />
        </button>
      </div>

      <div className="space-y-1">
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={column.header}
          onChange={(e) => onUpdate(index, { header: e.target.value })}
          placeholder="Header"
        />
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={column.field}
          onChange={(e) => onUpdate(index, { field: e.target.value })}
          placeholder="Field name"
        />
        <div className="flex gap-1">
          <input
            type="number"
            className="flex-1 border border-gray-300 rounded px-2 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
            value={column.width || ''}
            placeholder="Width"
            onChange={(e) =>
              onUpdate(index, {
                width: e.target.value ? Number(e.target.value) : undefined,
              })
            }
          />
          <select
            className="flex-1 border border-gray-300 rounded px-1 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
            value={column.alignment || 'LEFT'}
            onChange={(e) =>
              onUpdate(index, {
                alignment: e.target.value as 'LEFT' | 'CENTER' | 'RIGHT',
              })
            }
          >
            <option value="LEFT">Left</option>
            <option value="CENTER">Center</option>
            <option value="RIGHT">Right</option>
          </select>
        </div>

        {/* Format Preset */}
        <div>
          <label className="block text-[10px] font-medium text-gray-500 mt-1">Format</label>
          <select
            className="w-full border border-gray-300 rounded px-1 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
            value={column.formatPreset || ''}
            onChange={(e) => {
              const preset = (e.target.value || undefined) as NumberFormatPreset | undefined;
              const format = preset ? presetToFormat(preset) : undefined;
              onUpdate(index, { formatPreset: preset, format: preset === 'CUSTOM' ? column.format : format });
            }}
          >
            <option value="">None</option>
            {FORMAT_PRESETS.map((p) => (
              <option key={p.value} value={p.value}>{p.label}</option>
            ))}
          </select>
        </div>

        {/* Custom format string */}
        {column.formatPreset === 'CUSTOM' && (
          <input
            type="text"
            className="w-full border border-gray-300 rounded px-2 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
            value={column.format || ''}
            onChange={(e) => onUpdate(index, { format: e.target.value })}
            placeholder="e.g. #,##0.00"
          />
        )}

        {/* Wrap text toggle */}
        <div className="flex items-center gap-1 mt-1">
          <input
            type="checkbox"
            id={`wrapText-${index}`}
            checked={column.wrapText ?? false}
            onChange={(e) => onUpdate(index, { wrapText: e.target.checked })}
            className="rounded border-gray-300"
          />
          <label htmlFor={`wrapText-${index}`} className="text-[10px] text-gray-600">
            Wrap text
          </label>
        </div>

        {/* Conditional Formatting */}
        <div className="mt-1">
          <div className="flex items-center justify-between">
            <label className="text-[10px] font-medium text-gray-500">
              Conditional Formats
            </label>
            <button
              className="flex items-center gap-0.5 text-[10px] text-blue-600 hover:text-blue-800"
              onClick={addConditionalFormat}
            >
              <Plus size={10} />
              Add
            </button>
          </div>
          {conditionalFormats.map((cf, cfIdx) => (
            <div key={cfIdx} className="flex items-center gap-1 mt-0.5">
              <input
                type="text"
                className="flex-1 border border-gray-300 rounded px-1 py-0.5 text-[10px] focus:outline-none focus:ring-1 focus:ring-blue-400"
                value={cf.condition}
                onChange={(e) => updateConditionalFormat(cfIdx, { condition: e.target.value })}
                placeholder="value < 0"
              />
              <input
                type="color"
                className="w-5 h-5 border border-gray-300 rounded cursor-pointer"
                value={cf.style.textColor || '#ff0000'}
                onChange={(e) =>
                  updateConditionalFormat(cfIdx, { style: { ...cf.style, textColor: e.target.value } })
                }
              />
              <button
                className="text-red-400 hover:text-red-600 p-0.5"
                onClick={() => removeConditionalFormat(cfIdx)}
              >
                <Trash2 size={10} />
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
