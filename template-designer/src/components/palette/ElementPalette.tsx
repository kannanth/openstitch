import {
  Type,
  Table,
  ImageIcon,
  BarChart3,
  GitBranch,
  Repeat,
} from 'lucide-react';
import { useTemplateStore } from '../../store/templateStore';
import { useUIStore } from '../../store/uiStore';
import type { TemplateElement, SelectedArea } from '../../types/template';
import { FieldInsertPalette } from './FieldInsertPalette';

interface PaletteItem {
  label: string;
  icon: React.ReactNode;
  defaults: Omit<TemplateElement, 'id'>;
}

const paletteItems: PaletteItem[] = [
  {
    label: 'Text',
    icon: <Type size={20} />,
    defaults: {
      type: 'TEXT',
      positioning: 'FLOW',
      content: 'Enter text here',
      style: { fontSize: 12 },
    },
  },
  {
    label: 'Table',
    icon: <Table size={20} />,
    defaults: {
      type: 'TABLE',
      positioning: 'FLOW',
      dataSource: 'items',
      columns: [{ header: 'Column 1', field: 'field1' }],
      showHeader: true,
    },
  },
  {
    label: 'Image',
    icon: <ImageIcon size={20} />,
    defaults: {
      type: 'IMAGE',
      positioning: 'FLOW',
      source: 'STATIC',
      fit: 'CONTAIN',
      dimension: { width: 200, height: 150 },
    },
  },
  {
    label: 'Chart',
    icon: <BarChart3 size={20} />,
    defaults: {
      type: 'CHART',
      positioning: 'FLOW',
      chartType: 'BAR',
      dataSource: 'data',
      categoryField: 'name',
      valueFields: ['value'],
      dimension: { width: 400, height: 300 },
    },
  },
  {
    label: 'Conditional',
    icon: <GitBranch size={20} />,
    defaults: {
      type: 'CONDITIONAL',
      positioning: 'FLOW',
      condition: 'true',
      thenElements: [],
      elseElements: [],
    },
  },
  {
    label: 'Repeating',
    icon: <Repeat size={20} />,
    defaults: {
      type: 'REPEATING_SECTION',
      positioning: 'FLOW',
      dataSource: 'items',
      elements: [],
    },
  },
];

function getAreaLabel(area: SelectedArea, template: { body: { sections: { name: string }[] } }): string {
  switch (area.type) {
    case 'pageHeader':
      return 'Page Header';
    case 'pageFooter':
      return 'Page Footer';
    case 'section': {
      const section = template.body.sections[area.sectionIndex ?? 0];
      const name = section?.name ?? 'Section';
      switch (area.part) {
        case 'sectionHeader':
          return `${name} - Header`;
        case 'sectionFooter':
          return `${name} - Footer`;
        default:
          return name;
      }
    }
  }
}

export function ElementPalette() {
  const addElementToArea = useTemplateStore((s) => s.addElementToArea);
  const selectedArea = useTemplateStore((s) => s.selectedArea);
  const template = useTemplateStore((s) => s.template);
  const setDragData = useUIStore((s) => s.setDragData);
  const clearDragData = useUIStore((s) => s.clearDragData);

  return (
    <div>
      <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-3">
        Add Element
      </h3>
      <div className="text-[10px] text-gray-400 mb-2">
        Adding to: <span className="font-semibold text-gray-600">{getAreaLabel(selectedArea, template)}</span>
      </div>
      <div className="grid grid-cols-2 gap-2">
        {paletteItems.map((item) => (
          <button
            key={item.label}
            className="flex flex-col items-center gap-1 p-3 rounded-lg border border-gray-200 hover:border-blue-400 hover:bg-blue-50 transition-colors text-gray-600 hover:text-blue-600 cursor-grab active:cursor-grabbing"
            draggable
            onDragStart={(e) => {
              setDragData({ type: 'palette', paletteDefaults: item.defaults });
              e.dataTransfer.effectAllowed = 'copy';
            }}
            onDragEnd={() => clearDragData()}
            onClick={() => addElementToArea(selectedArea, item.defaults)}
          >
            {item.icon}
            <span className="text-xs font-medium">{item.label}</span>
          </button>
        ))}
      </div>
      <FieldInsertPalette />
    </div>
  );
}
