import { useState, useCallback, useRef } from 'react';
import type { TemplateElement, SelectedArea } from '../../types/template';
import { useTemplateStore } from '../../store/templateStore';
import { useUIStore } from '../../store/uiStore';
import { TextElementView } from './TextElement';
import { TableElementView } from './TableElement';
import { ImageElementView } from './ImageElement';
import { ChartElementView } from './ChartElement';
import { GripVertical } from 'lucide-react';

interface Props {
  element: TemplateElement;
  area: SelectedArea;
  index: number;
  isSelected: boolean;
  onSelect: () => void;
}

export function ElementFactory({ element, area, index: _index, isSelected, onSelect }: Props) {
  const updateAreaElement = useTemplateStore((s) => s.updateAreaElement);
  const setDragData = useUIStore((s) => s.setDragData);
  const clearDragData = useUIStore((s) => s.clearDragData);

  const isAbsolute = element.positioning === 'ABSOLUTE';
  const [isDragging, setIsDragging] = useState(false);
  const [isHovered, setIsHovered] = useState(false);

  // --- ABSOLUTE mouse-based repositioning ---
  const dragRef = useRef<{
    startX: number;
    startY: number;
    origX: number;
    origY: number;
  } | null>(null);

  const handleAbsoluteMouseDown = useCallback(
    (e: React.MouseEvent) => {
      if (!isAbsolute || !isSelected) return;
      e.preventDefault();
      e.stopPropagation();
      dragRef.current = {
        startX: e.clientX,
        startY: e.clientY,
        origX: element.position?.x ?? 0,
        origY: element.position?.y ?? 0,
      };

      const handleMouseMove = (moveEvent: MouseEvent) => {
        if (!dragRef.current) return;
        const dx = moveEvent.clientX - dragRef.current.startX;
        const dy = moveEvent.clientY - dragRef.current.startY;
        updateAreaElement(area, element.id, {
          position: {
            x: Math.max(0, dragRef.current.origX + dx),
            y: Math.max(0, dragRef.current.origY + dy),
          },
        });
      };

      const handleMouseUp = () => {
        dragRef.current = null;
        document.removeEventListener('mousemove', handleMouseMove);
        document.removeEventListener('mouseup', handleMouseUp);
      };

      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
    },
    [isAbsolute, isSelected, element.id, element.position, area, updateAreaElement]
  );

  // --- FLOW HTML drag-and-drop ---
  const handleDragStart = useCallback(
    (e: React.DragEvent) => {
      setDragData({ type: 'element', elementId: element.id, sourceArea: area });
      e.dataTransfer.effectAllowed = 'move';
      setIsDragging(true);
    },
    [element.id, area, setDragData]
  );

  const handleDragEnd = useCallback(() => {
    clearDragData();
    setIsDragging(false);
  }, [clearDragData]);

  const baseClass = `cursor-pointer border-2 ${
    isSelected ? 'border-blue-500' : 'border-transparent'
  } hover:border-blue-300 rounded p-2${isAbsolute ? '' : ' relative mb-2'} ${
    isDragging ? 'opacity-50' : ''
  }`;

  const style: React.CSSProperties = isAbsolute
    ? {
        position: 'absolute',
        left: `${element.position?.x ?? 0}px`,
        top: `${element.position?.y ?? 0}px`,
        width: element.dimension?.width ? `${element.dimension.width}px` : undefined,
        height: element.dimension?.height ? `${element.dimension.height}px` : undefined,
        zIndex: isSelected ? 10 : 1,
        cursor: isSelected ? 'move' : undefined,
      }
    : {
        marginTop: element.marginTop ? `${element.marginTop}px` : undefined,
        marginBottom: element.marginBottom ? `${element.marginBottom}px` : undefined,
      };

  const content = (() => {
    switch (element.type) {
      case 'TEXT':
        return <TextElementView element={element} />;
      case 'TABLE':
        return <TableElementView element={element} />;
      case 'IMAGE':
        return <ImageElementView element={element} />;
      case 'CHART':
        return <ChartElementView element={element} />;
      case 'CONDITIONAL':
        return (
          <div className="text-xs text-purple-600 bg-purple-50 p-2 rounded">
            Conditional: {element.condition}
          </div>
        );
      case 'REPEATING_SECTION':
        return (
          <div className="text-xs text-green-600 bg-green-50 p-2 rounded">
            Repeating: {element.dataSource}
          </div>
        );
      default:
        return <div>Unknown element</div>;
    }
  })();

  return (
    <div
      className={baseClass}
      style={style}
      draggable={!isAbsolute}
      onDragStart={!isAbsolute ? handleDragStart : undefined}
      onDragEnd={!isAbsolute ? handleDragEnd : undefined}
      onMouseDown={isAbsolute ? handleAbsoluteMouseDown : undefined}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onClick={(e) => {
        e.stopPropagation();
        onSelect();
      }}
    >
      {/* Drag handle for FLOW elements */}
      {!isAbsolute && isHovered && (
        <div className="absolute top-0 left-0 p-0.5 text-gray-400 cursor-grab active:cursor-grabbing z-10">
          <GripVertical size={12} />
        </div>
      )}
      {content}
    </div>
  );
}
