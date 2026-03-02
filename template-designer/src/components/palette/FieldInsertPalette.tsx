import { useState } from 'react';
import {
  Calendar,
  Clock,
  Hash,
  ChevronDown,
  ChevronRight,
} from 'lucide-react';
import { useTemplateStore } from '../../store/templateStore';
import { useUIStore } from '../../store/uiStore';
import type { TemplateElement } from '../../types/template';

interface FieldItem {
  label: string;
  expression: string;
  example: string;
}

interface FieldGroup {
  name: string;
  icon: React.ReactNode;
  fields: FieldItem[];
}

const fieldGroups: FieldGroup[] = [
  {
    name: 'Date',
    icon: <Calendar size={14} />,
    fields: [
      { label: 'Date (US)', expression: "${dateFormat(now(), 'MM/dd/yyyy')}", example: '02/28/2026' },
      { label: 'Date (EU)', expression: "${dateFormat(now(), 'dd/MM/yyyy')}", example: '28/02/2026' },
      { label: 'Date (ISO)', expression: "${dateFormat(now(), 'yyyy-MM-dd')}", example: '2026-02-28' },
      { label: 'Date (Long)', expression: "${dateFormat(now(), 'MMMM d, yyyy')}", example: 'February 28, 2026' },
      { label: 'Date (Short)', expression: "${dateFormat(now(), 'MMM d, yyyy')}", example: 'Feb 28, 2026' },
    ],
  },
  {
    name: 'Time',
    icon: <Clock size={14} />,
    fields: [
      { label: 'Time (24h)', expression: "${timeFormat(now(), 'HH:mm')}", example: '14:30' },
      { label: 'Time (12h)', expression: "${timeFormat(now(), 'hh:mm a')}", example: '02:30 PM' },
      { label: 'Time (Full)', expression: "${timeFormat(now(), 'HH:mm:ss')}", example: '14:30:45' },
    ],
  },
  {
    name: 'Page',
    icon: <Hash size={14} />,
    fields: [
      { label: 'Page Number', expression: '${pageNumber}', example: '1, 2, 3...' },
      { label: 'Total Pages', expression: '${totalPages}', example: 'Total count' },
      { label: 'Page X of Y', expression: 'Page ${pageNumber} of ${totalPages}', example: 'Page 1 of 5' },
    ],
  },
  {
    name: 'Date + Time',
    icon: <Calendar size={14} />,
    fields: [
      {
        label: 'Date + Time',
        expression: "${dateFormat(now(), 'MM/dd/yyyy')} ${timeFormat(now(), 'hh:mm a')}",
        example: '02/28/2026 02:30 PM',
      },
    ],
  },
];

export function FieldInsertPalette() {
  const [expanded, setExpanded] = useState(false);
  const addElementToArea = useTemplateStore((s) => s.addElementToArea);
  const selectedArea = useTemplateStore((s) => s.selectedArea);
  const setDragData = useUIStore((s) => s.setDragData);
  const clearDragData = useUIStore((s) => s.clearDragData);

  const handleInsert = (expression: string) => {
    const element: Omit<TemplateElement, 'id'> = {
      type: 'TEXT',
      positioning: 'FLOW',
      content: expression,
      style: { fontSize: 10 },
    };
    addElementToArea(selectedArea, element);
  };

  const makeFieldDefaults = (expression: string): Omit<TemplateElement, 'id'> => ({
    type: 'TEXT',
    positioning: 'FLOW',
    content: expression,
    style: { fontSize: 10 },
  });

  return (
    <div className="mt-4">
      <button
        className="flex items-center gap-1 text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2 hover:text-gray-700 w-full"
        onClick={() => setExpanded(!expanded)}
      >
        {expanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
        Insert Field
      </button>

      {expanded && (
        <div className="space-y-3">
          {fieldGroups.map((group) => (
            <div key={group.name}>
              <div className="flex items-center gap-1 text-[10px] font-semibold text-gray-400 uppercase mb-1">
                {group.icon}
                {group.name}
              </div>
              <div className="space-y-1">
                {group.fields.map((field) => (
                  <button
                    key={field.label}
                    className="w-full flex items-center justify-between px-2 py-1.5 rounded border border-gray-200 hover:border-blue-400 hover:bg-blue-50 transition-colors text-left group cursor-grab active:cursor-grabbing"
                    draggable
                    onDragStart={(e) => {
                      setDragData({ type: 'palette', paletteDefaults: makeFieldDefaults(field.expression) });
                      e.dataTransfer.effectAllowed = 'copy';
                    }}
                    onDragEnd={() => clearDragData()}
                    onClick={() => handleInsert(field.expression)}
                    title={field.expression}
                  >
                    <span className="text-xs text-gray-600 group-hover:text-blue-600">
                      {field.label}
                    </span>
                    <span className="text-[10px] text-gray-400">
                      {field.example}
                    </span>
                  </button>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
