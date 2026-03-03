import { useState } from 'react';
import type { TemplateElement, ElementType } from '../../types/template';
import { useTemplateStore } from '../../store/templateStore';
import { v4 as uuidv4 } from 'uuid';
import { Trash2, ArrowUp, ArrowDown, ChevronDown, ChevronRight, Plus } from 'lucide-react';

interface Props {
  element: TemplateElement;
}

const NESTABLE_TYPES: { type: ElementType; label: string }[] = [
  { type: 'TEXT', label: 'Text' },
  { type: 'TABLE', label: 'Table' },
  { type: 'IMAGE', label: 'Image' },
  { type: 'CHART', label: 'Chart' },
];

function createDefaultElement(type: ElementType): TemplateElement {
  const base: TemplateElement = { id: uuidv4(), type, positioning: 'FLOW' };
  switch (type) {
    case 'TEXT':
      return { ...base, content: 'New text' };
    case 'TABLE':
      return { ...base, dataSource: '', columns: [], showHeader: true };
    case 'IMAGE':
      return { ...base, source: 'STATIC', data: '' };
    case 'CHART':
      return { ...base, chartType: 'BAR', dataSource: '' };
    default:
      return base;
  }
}

// --- Inline nested element editors ---

function NestedTextEditor({
  el,
  onChange,
}: {
  el: TemplateElement;
  onChange: (updates: Partial<TemplateElement>) => void;
}) {
  return (
    <div className="space-y-2">
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Content</label>
        <textarea
          className="w-full border border-gray-300 rounded px-2 py-1 text-xs resize-y min-h-[40px] focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={el.content || ''}
          onChange={(e) => onChange({ content: e.target.value })}
        />
      </div>
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Font Size (px)</label>
        <input
          type="number"
          className="w-full border border-gray-300 rounded px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={el.style?.fontSize || 12}
          min={1}
          max={200}
          onChange={(e) =>
            onChange({ style: { ...el.style, fontSize: Number(e.target.value) } })
          }
        />
      </div>
    </div>
  );
}

function NestedTableEditor({
  el,
  onChange,
}: {
  el: TemplateElement;
  onChange: (updates: Partial<TemplateElement>) => void;
}) {
  return (
    <div>
      <label className="block text-xs font-medium text-gray-600 mb-1">Data Source</label>
      <input
        type="text"
        className="w-full border border-gray-300 rounded px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
        value={el.dataSource || ''}
        placeholder="${data.items}"
        onChange={(e) => onChange({ dataSource: e.target.value })}
      />
    </div>
  );
}

function NestedImageEditor({
  el,
  onChange,
}: {
  el: TemplateElement;
  onChange: (updates: Partial<TemplateElement>) => void;
}) {
  return (
    <div className="space-y-2">
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Source</label>
        <select
          className="w-full border border-gray-300 rounded px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={el.source || 'STATIC'}
          onChange={(e) =>
            onChange({ source: e.target.value as 'STATIC' | 'DATA_FIELD' | 'URL' })
          }
        >
          <option value="STATIC">Static</option>
          <option value="DATA_FIELD">Data Field</option>
          <option value="URL">URL</option>
        </select>
      </div>
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Data</label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={el.data || ''}
          onChange={(e) => onChange({ data: e.target.value })}
        />
      </div>
    </div>
  );
}

function NestedChartEditor({
  el,
  onChange,
}: {
  el: TemplateElement;
  onChange: (updates: Partial<TemplateElement>) => void;
}) {
  return (
    <div className="space-y-2">
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Chart Type</label>
        <select
          className="w-full border border-gray-300 rounded px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={el.chartType || 'BAR'}
          onChange={(e) =>
            onChange({ chartType: e.target.value as 'BAR' | 'PIE' | 'LINE' })
          }
        >
          <option value="BAR">Bar</option>
          <option value="PIE">Pie</option>
          <option value="LINE">Line</option>
        </select>
      </div>
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Data Source</label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={el.dataSource || ''}
          onChange={(e) => onChange({ dataSource: e.target.value })}
        />
      </div>
    </div>
  );
}

function NestedElementEditor({
  el,
  onChange,
}: {
  el: TemplateElement;
  onChange: (updates: Partial<TemplateElement>) => void;
}) {
  switch (el.type) {
    case 'TEXT':
      return <NestedTextEditor el={el} onChange={onChange} />;
    case 'TABLE':
      return <NestedTableEditor el={el} onChange={onChange} />;
    case 'IMAGE':
      return <NestedImageEditor el={el} onChange={onChange} />;
    case 'CHART':
      return <NestedChartEditor el={el} onChange={onChange} />;
    default:
      return null;
  }
}

// --- Branch editor (reused for then & else) ---

function BranchEditor({
  label,
  color,
  elements,
  onUpdate,
}: {
  label: string;
  color: 'green' | 'orange';
  elements: TemplateElement[];
  onUpdate: (elements: TemplateElement[]) => void;
}) {
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [showAddMenu, setShowAddMenu] = useState(false);

  const colorClasses = {
    green: {
      header: 'bg-green-100 text-green-800',
      border: 'border-green-200',
      badge: 'bg-green-200 text-green-700',
    },
    orange: {
      header: 'bg-orange-100 text-orange-800',
      border: 'border-orange-200',
      badge: 'bg-orange-200 text-orange-700',
    },
  }[color];

  const addElement = (type: ElementType) => {
    onUpdate([...elements, createDefaultElement(type)]);
    setShowAddMenu(false);
  };

  const removeElement = (id: string) => {
    onUpdate(elements.filter((el) => el.id !== id));
    if (expandedId === id) setExpandedId(null);
  };

  const moveElement = (index: number, direction: -1 | 1) => {
    const newIndex = index + direction;
    if (newIndex < 0 || newIndex >= elements.length) return;
    const arr = [...elements];
    [arr[index], arr[newIndex]] = [arr[newIndex], arr[index]];
    onUpdate(arr);
  };

  const updateNestedElement = (id: string, updates: Partial<TemplateElement>) => {
    onUpdate(elements.map((el) => (el.id === id ? { ...el, ...updates } : el)));
  };

  return (
    <div className={`border rounded ${colorClasses.border}`}>
      <div className={`px-2 py-1.5 flex items-center justify-between ${colorClasses.header}`}>
        <span className="text-xs font-semibold">{label}</span>
        <span className={`text-[10px] px-1.5 py-0.5 rounded-full ${colorClasses.badge}`}>
          {elements.length}
        </span>
      </div>

      <div className="p-2 space-y-1.5">
        {elements.length === 0 && (
          <div className="text-xs text-gray-400 italic text-center py-2">No elements</div>
        )}

        {elements.map((el, index) => {
          const isExpanded = expandedId === el.id;
          return (
            <div key={el.id} className="border border-gray-200 rounded bg-white">
              {/* Row header */}
              <div className="flex items-center gap-1 px-1.5 py-1">
                <button
                  className="p-0.5 text-gray-400 hover:text-gray-600"
                  onClick={() => setExpandedId(isExpanded ? null : el.id)}
                >
                  {isExpanded ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
                </button>
                <span className="text-xs font-medium text-gray-700 flex-1 truncate">
                  {el.type}
                  {el.type === 'TEXT' && el.content ? (
                    <span className="font-normal text-gray-400 ml-1">
                      — {el.content.slice(0, 20)}
                      {el.content.length > 20 ? '...' : ''}
                    </span>
                  ) : null}
                </span>
                <button
                  className="p-0.5 text-gray-400 hover:text-gray-600 disabled:opacity-30"
                  disabled={index === 0}
                  onClick={() => moveElement(index, -1)}
                  title="Move up"
                >
                  <ArrowUp size={12} />
                </button>
                <button
                  className="p-0.5 text-gray-400 hover:text-gray-600 disabled:opacity-30"
                  disabled={index === elements.length - 1}
                  onClick={() => moveElement(index, 1)}
                  title="Move down"
                >
                  <ArrowDown size={12} />
                </button>
                <button
                  className="p-0.5 text-red-400 hover:text-red-600"
                  onClick={() => removeElement(el.id)}
                  title="Delete"
                >
                  <Trash2 size={12} />
                </button>
              </div>

              {/* Expanded editor */}
              {isExpanded && (
                <div className="px-2 pb-2 border-t border-gray-100 pt-2">
                  <NestedElementEditor
                    el={el}
                    onChange={(updates) => updateNestedElement(el.id, updates)}
                  />
                </div>
              )}
            </div>
          );
        })}

        {/* Add element button */}
        <div className="relative">
          <button
            className="w-full flex items-center justify-center gap-1 text-xs text-gray-500 hover:text-gray-700 border border-dashed border-gray-300 rounded py-1.5 hover:bg-gray-50"
            onClick={() => setShowAddMenu(!showAddMenu)}
          >
            <Plus size={12} />
            Add Element
          </button>
          {showAddMenu && (
            <div className="absolute z-10 left-0 right-0 mt-1 bg-white border border-gray-200 rounded shadow-lg py-1">
              {NESTABLE_TYPES.map(({ type, label: typeLabel }) => (
                <button
                  key={type}
                  className="w-full text-left px-3 py-1.5 text-xs hover:bg-gray-100 text-gray-700"
                  onClick={() => addElement(type)}
                >
                  {typeLabel}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// --- Main component ---

export function ConditionalProperties({ element }: Props) {
  const updateElement = useTemplateStore((s) => s.updateElement);

  const thenElements = element.thenElements ?? [];
  const elseElements = element.elseElements ?? [];

  return (
    <div className="space-y-4">
      {/* Condition expression */}
      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Condition Expression
        </label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-purple-400"
          value={element.condition || ''}
          placeholder="customer.isVip == true"
          onChange={(e) => updateElement(element.id, { condition: e.target.value })}
        />
      </div>

      {/* Then branch */}
      <BranchEditor
        label="Then Branch"
        color="green"
        elements={thenElements}
        onUpdate={(arr) => updateElement(element.id, { thenElements: arr })}
      />

      {/* Else branch */}
      <BranchEditor
        label="Else Branch"
        color="orange"
        elements={elseElements}
        onUpdate={(arr) => updateElement(element.id, { elseElements: arr })}
      />
    </div>
  );
}
