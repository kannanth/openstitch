import type { TemplateElement, TableColumn, TableFooterCell } from '../../types/template';
import { useTemplateStore } from '../../store/templateStore';
import { Plus, Trash2 } from 'lucide-react';
import { TableColumnProperties } from './TableColumnProperties';

interface Props {
  element: TemplateElement;
}

export function TableProperties({ element }: Props) {
  const updateElement = useTemplateStore((s) => s.updateElement);
  const columns = element.columns || [];
  const footerCells = element.footerCells ?? [];

  const update = (updates: Partial<TemplateElement>) => {
    updateElement(element.id, updates);
  };

  const updateColumn = (index: number, updates: Partial<TableColumn>) => {
    const newColumns = columns.map((col, i) =>
      i === index ? { ...col, ...updates } : col
    );
    update({ columns: newColumns });
  };

  const addColumn = () => {
    const newColumns = [
      ...columns,
      { header: `Column ${columns.length + 1}`, field: `field${columns.length + 1}` },
    ];
    update({ columns: newColumns });
  };

  const removeColumn = (index: number) => {
    const newColumns = columns.filter((_, i) => i !== index);
    update({ columns: newColumns });
  };

  const addFooterCell = () => {
    update({ footerCells: [...footerCells, { content: '', colSpan: 1 }] });
  };

  const updateFooterCell = (index: number, updates: Partial<TableFooterCell>) => {
    const updated = footerCells.map((cell, i) =>
      i === index ? { ...cell, ...updates } : cell
    );
    update({ footerCells: updated });
  };

  const removeFooterCell = (index: number) => {
    update({ footerCells: footerCells.filter((_, i) => i !== index) });
  };

  return (
    <div className="space-y-3">
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Data Source
        </label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={element.dataSource || ''}
          onChange={(e) => update({ dataSource: e.target.value })}
          placeholder="e.g. items, data.rows"
        />
      </div>

      <div className="flex items-center gap-2">
        <input
          type="checkbox"
          id="showHeader"
          checked={element.showHeader !== false}
          onChange={(e) => update({ showHeader: e.target.checked })}
          className="rounded border-gray-300"
        />
        <label htmlFor="showHeader" className="text-xs text-gray-600">
          Show Header Row
        </label>
      </div>

      <div>
        <div className="flex items-center justify-between mb-2">
          <label className="text-xs font-medium text-gray-600">Columns</label>
          <button
            className="flex items-center gap-1 text-xs text-blue-600 hover:text-blue-800"
            onClick={addColumn}
          >
            <Plus size={12} />
            Add
          </button>
        </div>

        <div className="space-y-2">
          {columns.map((col, i) => (
            <TableColumnProperties
              key={i}
              column={col}
              index={i}
              onUpdate={updateColumn}
              onRemove={removeColumn}
            />
          ))}
        </div>
      </div>

      {/* Footer Cells */}
      <div>
        <div className="flex items-center justify-between mb-2">
          <label className="text-xs font-medium text-gray-600">Footer Cells</label>
          <button
            className="flex items-center gap-1 text-xs text-blue-600 hover:text-blue-800"
            onClick={addFooterCell}
          >
            <Plus size={12} />
            Add
          </button>
        </div>

        <div className="space-y-1">
          {footerCells.map((cell, i) => (
            <div key={i} className="flex items-center gap-1">
              <input
                type="text"
                className="flex-1 border border-gray-300 rounded px-2 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
                value={cell.content}
                onChange={(e) => updateFooterCell(i, { content: e.target.value })}
                placeholder="Content / expression"
              />
              <input
                type="number"
                className="w-12 border border-gray-300 rounded px-1 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
                value={cell.colSpan}
                min={1}
                onChange={(e) => updateFooterCell(i, { colSpan: Math.max(1, Number(e.target.value)) })}
                title="Column span"
              />
              <button
                className="text-red-400 hover:text-red-600 p-0.5"
                onClick={() => removeFooterCell(i)}
              >
                <Trash2 size={12} />
              </button>
            </div>
          ))}
        </div>
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Alternate Row Color
        </label>
        <div className="flex items-center gap-2">
          <input
            type="color"
            className="w-8 h-8 border border-gray-300 rounded cursor-pointer"
            value={element.alternateRowColor || '#f9fafb'}
            onChange={(e) => update({ alternateRowColor: e.target.value })}
          />
          <input
            type="text"
            className="flex-1 border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
            value={element.alternateRowColor || ''}
            placeholder="none"
            onChange={(e) => update({ alternateRowColor: e.target.value || undefined })}
          />
        </div>
      </div>
    </div>
  );
}
