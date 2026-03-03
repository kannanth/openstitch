import { X } from 'lucide-react';
import { useTabStore } from '../../store/tabStore';
import { useTemplateStore } from '../../store/templateStore';

export function TabBar() {
  const tabs = useTabStore((s) => s.tabs);
  const activeTabId = useTabStore((s) => s.activeTabId);
  const switchTab = useTabStore((s) => s.switchTab);
  const closeTab = useTabStore((s) => s.closeTab);

  const activeTemplateName = useTemplateStore((s) => s.template.metadata.name);

  return (
    <div className="flex items-end gap-px bg-gray-300 px-2 pt-1 overflow-x-auto">
      {tabs.map((tab) => {
        const isActive = tab.id === activeTabId;
        const name = isActive ? activeTemplateName : tab.name;

        return (
          <button
            key={tab.id}
            className={`group flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium max-w-[180px] rounded-t-md transition-colors shrink-0 ${
              isActive
                ? 'bg-gray-200 text-gray-800 border-t-2 border-blue-500'
                : 'text-gray-500 hover:bg-gray-200/60 hover:text-gray-700 border-t-2 border-transparent'
            }`}
            onClick={() => switchTab(tab.id)}
          >
            <span className="truncate">{name || 'Untitled'}</span>
            {tabs.length > 1 && (
              <span
                className={`shrink-0 rounded p-0.5 hover:bg-gray-200 ${
                  isActive ? 'opacity-60 hover:opacity-100' : 'opacity-0 group-hover:opacity-60 hover:!opacity-100'
                }`}
                onClick={(e) => {
                  e.stopPropagation();
                  closeTab(tab.id);
                }}
              >
                <X size={12} />
              </span>
            )}
          </button>
        );
      })}
    </div>
  );
}
