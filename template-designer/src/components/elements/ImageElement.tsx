import { ImageIcon } from 'lucide-react';
import type { TemplateElement } from '../../types/template';

export function ImageElementView({ element }: { element: TemplateElement }) {
  const dim = element.dimension;
  return (
    <div
      className="flex items-center justify-center bg-gray-50 border border-dashed border-gray-300 rounded"
      style={{
        width: dim?.width ? `${Math.min(dim.width, 400)}px` : '100%',
        height: dim?.height ? `${Math.min(dim.height, 200)}px` : '100px',
      }}
    >
      <div className="text-center text-gray-400">
        <ImageIcon className="mx-auto mb-1" size={24} />
        <div className="text-xs">{element.source || 'Image'}</div>
      </div>
    </div>
  );
}
