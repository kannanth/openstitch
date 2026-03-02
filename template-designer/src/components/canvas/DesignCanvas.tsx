import { useUIStore } from '../../store/uiStore';
import { useTemplateStore } from '../../store/templateStore';
import { PageView } from './PageView';

export function DesignCanvas() {
  const zoom = useUIStore((s) => s.zoom);
  const selectElement = useTemplateStore((s) => s.selectElement);

  return (
    <div
      className="flex-1 overflow-auto bg-gray-200 flex items-start justify-center p-8"
      onClick={(e) => {
        if (e.target === e.currentTarget) selectElement(null);
      }}
    >
      <div style={{ transform: `scale(${zoom})`, transformOrigin: 'top center' }}>
        <PageView />
      </div>
    </div>
  );
}
