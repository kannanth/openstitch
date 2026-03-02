import { useState, useCallback, useRef } from 'react';
import type { SelectedArea, TemplateElement } from '../../types/template';
import { useTemplateStore } from '../../store/templateStore';
import { useUIStore } from '../../store/uiStore';
import { ElementFactory } from '../elements/ElementFactory';
import { ChevronDown, ChevronRight, GripHorizontal } from 'lucide-react';

interface Props {
  area: SelectedArea;
  elements: TemplateElement[];
  height: number | 'auto';
  contentWidth: number;
  label: string;
  color: string;
  ringColor: string;
  activeColor: string;
  borderColor: string;
  collapsed: boolean;
  onToggleCollapsed: () => void;
  onResize?: (height: number) => void;
}

const MIN_HEIGHT = 20;

function areasMatch(a: SelectedArea, b: SelectedArea): boolean {
  return (
    a.type === b.type &&
    a.sectionIndex === b.sectionIndex &&
    a.part === b.part
  );
}

export function SectionZone({
  area,
  elements,
  height,
  contentWidth,
  label,
  color,
  ringColor,
  activeColor,
  borderColor,
  collapsed,
  onToggleCollapsed,
  onResize,
}: Props) {
  const selectedArea = useTemplateStore((s) => s.selectedArea);
  const selectedElementId = useTemplateStore((s) => s.selectedElementId);
  const selectElement = useTemplateStore((s) => s.selectElement);
  const setSelectedArea = useTemplateStore((s) => s.setSelectedArea);
  const addElementToArea = useTemplateStore((s) => s.addElementToArea);
  const insertElementToArea = useTemplateStore((s) => s.insertElementToArea);
  const moveElementInArea = useTemplateStore((s) => s.moveElementInArea);
  const moveElementBetweenAreas = useTemplateStore((s) => s.moveElementBetweenAreas);

  const dragData = useUIStore((s) => s.dragData);
  const clearDragData = useUIStore((s) => s.clearDragData);

  const isActive = areasMatch(selectedArea, area);
  const isResizable = onResize != null && height !== 'auto';

  const [resizing, setResizing] = useState(false);
  const [dropIndex, setDropIndex] = useState<number | null>(null);
  const contentRef = useRef<HTMLDivElement>(null);

  const handleLabelClick = useCallback(() => {
    setSelectedArea(area);
    selectElement(null);
  }, [area, setSelectedArea, selectElement]);

  const handleElementSelect = useCallback(
    (id: string) => {
      setSelectedArea(area);
      selectElement(id);
    },
    [area, setSelectedArea, selectElement]
  );

  const handleResizeStart = useCallback(
    (e: React.MouseEvent) => {
      if (!isResizable || !onResize) return;
      e.preventDefault();
      setResizing(true);
      const startY = e.clientY;
      const startHeight = typeof height === 'number' ? height : 60;

      const handleMouseMove = (moveEvent: MouseEvent) => {
        const delta = moveEvent.clientY - startY;
        const newHeight = Math.max(MIN_HEIGHT, startHeight + delta);
        onResize(Math.round(newHeight));
      };

      const handleMouseUp = () => {
        setResizing(false);
        document.removeEventListener('mousemove', handleMouseMove);
        document.removeEventListener('mouseup', handleMouseUp);
      };

      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
    },
    [isResizable, height, onResize]
  );

  const calcDropIndex = useCallback(
    (clientY: number): number => {
      if (!contentRef.current) return elements.length;
      const children = Array.from(contentRef.current.children).filter(
        (el) => !(el as HTMLElement).dataset.dropIndicator
      );
      for (let i = 0; i < children.length; i++) {
        const rect = children[i].getBoundingClientRect();
        const midY = rect.top + rect.height / 2;
        if (clientY < midY) return i;
      }
      return elements.length;
    },
    [elements.length]
  );

  const handleDragOver = useCallback(
    (e: React.DragEvent) => {
      if (!dragData) return;
      e.preventDefault();
      e.dataTransfer.dropEffect = dragData.type === 'palette' ? 'copy' : 'move';
      setDropIndex(calcDropIndex(e.clientY));
    },
    [dragData, calcDropIndex]
  );

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    // Only clear if actually leaving the container, not entering a child
    if (!contentRef.current?.contains(e.relatedTarget as Node)) {
      setDropIndex(null);
    }
  }, []);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      if (!dragData) return;
      const idx = dropIndex ?? elements.length;

      if (dragData.type === 'palette' && dragData.paletteDefaults) {
        if (idx < elements.length) {
          insertElementToArea(area, dragData.paletteDefaults, idx);
        } else {
          addElementToArea(area, dragData.paletteDefaults);
        }
      } else if (dragData.type === 'element' && dragData.elementId && dragData.sourceArea) {
        if (areasMatch(dragData.sourceArea, area)) {
          moveElementInArea(area, dragData.elementId, idx);
        } else {
          moveElementBetweenAreas(dragData.sourceArea, area, dragData.elementId, idx);
        }
      }

      setDropIndex(null);
      clearDragData();
    },
    [
      dragData,
      dropIndex,
      elements.length,
      area,
      addElementToArea,
      insertElementToArea,
      moveElementInArea,
      moveElementBetweenAreas,
      clearDragData,
    ]
  );

  const numericHeight = typeof height === 'number' ? height : undefined;
  const isDragOver = dropIndex !== null;

  return (
    <div
      className={`relative transition-all duration-150 ${color} ${
        isActive
          ? `ring-2 ${ringColor} border-l-4 ${borderColor}`
          : 'hover:ring-1 hover:ring-dashed hover:ring-gray-300'
      } ${isDragOver ? 'ring-2 ring-blue-400 ring-dashed bg-blue-50/30' : ''}`}
      style={{
        width: `${contentWidth}px`,
        minHeight: collapsed ? undefined : numericHeight ? `${numericHeight}px` : undefined,
      }}
    >
      {/* Label bar */}
      <div
        className={`flex items-center gap-1 px-2 py-0.5 border-b cursor-pointer select-none text-[10px] transition-all duration-150 ${
          isActive
            ? `${activeColor} text-white border-transparent`
            : 'bg-gray-100/80 text-gray-600 border-gray-200'
        }`}
        onClick={handleLabelClick}
      >
        <button
          className={`p-0.5 rounded ${
            isActive ? 'hover:bg-white/20' : 'hover:bg-gray-200'
          }`}
          onClick={(e) => {
            e.stopPropagation();
            onToggleCollapsed();
          }}
        >
          {collapsed ? <ChevronRight size={10} /> : <ChevronDown size={10} />}
        </button>
        <span className="font-semibold">{label}</span>
        {numericHeight != null && (
          <span className={`ml-auto ${isActive ? 'text-white/70' : 'text-gray-400'}`}>
            {numericHeight}pt
          </span>
        )}
        {height === 'auto' && (
          <span className={`ml-auto ${isActive ? 'text-white/70' : 'text-gray-400'}`}>
            auto
          </span>
        )}
      </div>

      {/* Content area */}
      {!collapsed && (
        <div
          ref={contentRef}
          className="relative"
          style={{ minHeight: '20px' }}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
        >
          {elements.length === 0 && dropIndex === null ? (
            <div className="flex items-center justify-center py-4 text-[10px] text-gray-400 italic">
              Drop elements here
            </div>
          ) : (
            elements.map((element, i) => (
              <div key={element.id}>
                {dropIndex === i && (
                  <div
                    data-drop-indicator="true"
                    className="h-0.5 bg-blue-500 rounded-full mx-2 my-0.5"
                  />
                )}
                <ElementFactory
                  element={element}
                  area={area}
                  index={i}
                  isSelected={element.id === selectedElementId}
                  onSelect={() => handleElementSelect(element.id)}
                />
              </div>
            ))
          )}
          {dropIndex === elements.length && (
            <div
              data-drop-indicator="true"
              className="h-0.5 bg-blue-500 rounded-full mx-2 my-0.5"
            />
          )}
          {elements.length === 0 && dropIndex === 0 && (
            <div
              data-drop-indicator="true"
              className="h-0.5 bg-blue-500 rounded-full mx-2 my-0.5"
            />
          )}
        </div>
      )}

      {/* Resize handle */}
      {isResizable && !collapsed && (
        <div
          className={`absolute bottom-0 left-0 right-0 h-1.5 cursor-row-resize flex items-center justify-center hover:bg-blue-200/50 ${
            resizing ? 'bg-blue-300/50' : ''
          }`}
          onMouseDown={handleResizeStart}
        >
          <GripHorizontal size={10} className="text-gray-400" />
        </div>
      )}
    </div>
  );
}
