import { GitBranch } from 'lucide-react';
import type { TemplateElement } from '../../types/template';
import { TextElementView } from './TextElement';
import { TableElementView } from './TableElement';
import { ImageElementView } from './ImageElement';
import { ChartElementView } from './ChartElement';

function MiniElementView({ element }: { element: TemplateElement }) {
  switch (element.type) {
    case 'TEXT':
      return <TextElementView element={element} />;
    case 'TABLE':
      return <TableElementView element={element} />;
    case 'IMAGE':
      return <ImageElementView element={element} />;
    case 'CHART':
      return <ChartElementView element={element} />;
    default:
      return <div className="text-xs text-gray-400">{element.type}</div>;
  }
}

export function ConditionalElementView({ element }: { element: TemplateElement }) {
  const thenElements = element.thenElements ?? [];
  const elseElements = element.elseElements ?? [];

  return (
    <div className="text-xs rounded overflow-hidden border border-purple-200">
      {/* Header */}
      <div className="bg-purple-600 text-white px-2 py-1 flex items-center gap-1.5 font-medium">
        <GitBranch size={12} />
        <span className="truncate">IF: {element.condition || '(no condition)'}</span>
      </div>

      {/* Branches */}
      <div className="flex divide-x divide-purple-200">
        {/* THEN */}
        <div className="flex-1 min-w-0">
          <div className="bg-green-100 text-green-700 px-2 py-0.5 text-[10px] font-semibold uppercase">
            Then
          </div>
          <div className="p-1.5 space-y-1 bg-green-50/50">
            {thenElements.length === 0 ? (
              <div className="text-gray-400 italic text-center py-1">(empty)</div>
            ) : (
              thenElements.map((child) => (
                <div key={child.id} className="border border-green-200 rounded p-1 bg-white">
                  <MiniElementView element={child} />
                </div>
              ))
            )}
          </div>
        </div>

        {/* ELSE */}
        <div className="flex-1 min-w-0">
          <div className="bg-orange-100 text-orange-700 px-2 py-0.5 text-[10px] font-semibold uppercase">
            Else
          </div>
          <div className="p-1.5 space-y-1 bg-orange-50/50">
            {elseElements.length === 0 ? (
              <div className="text-gray-400 italic text-center py-1">(empty)</div>
            ) : (
              elseElements.map((child) => (
                <div key={child.id} className="border border-orange-200 rounded p-1 bg-white">
                  <MiniElementView element={child} />
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
