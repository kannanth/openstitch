import { useUIStore } from '../../store/uiStore';
import { ElementPalette } from '../palette/ElementPalette';
import { PageLayoutProperties } from '../properties/PageLayoutProperties';
import { SampleTemplates } from '../templates/SampleTemplates';

const tabs = [
  { key: 'elements' as const, label: 'Elements' },
  { key: 'templates' as const, label: 'Templates' },
  { key: 'settings' as const, label: 'Settings' },
];

export function Sidebar() {
  const sidebarTab = useUIStore((s) => s.sidebarTab);
  const setSidebarTab = useUIStore((s) => s.setSidebarTab);

  return (
    <div className="w-64 bg-white border-r flex flex-col">
      <div className="flex border-b">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            className={`flex-1 px-2 py-2 text-sm font-medium transition-colors ${
              sidebarTab === tab.key
                ? 'text-blue-600 border-b-2 border-blue-600'
                : 'text-gray-500 hover:text-gray-700'
            }`}
            onClick={() => setSidebarTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="flex-1 overflow-y-auto p-3">
        {sidebarTab === 'elements' && <ElementPalette />}

        {sidebarTab === 'templates' && (
          <div className="space-y-6">
            <div className="text-sm text-gray-400 text-center">
              <p>Saved templates will appear here.</p>
              <p className="mt-1 text-xs">Connect to the API to browse and load templates.</p>
            </div>
            <div className="border-t pt-4">
              <SampleTemplates />
            </div>
          </div>
        )}

        {sidebarTab === 'settings' && <PageLayoutProperties />}
      </div>
    </div>
  );
}
