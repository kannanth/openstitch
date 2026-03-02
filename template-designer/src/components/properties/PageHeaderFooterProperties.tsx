import { useTemplateStore } from '../../store/templateStore';
import { Trash2 } from 'lucide-react';

interface Props {
  section: 'header' | 'footer';
}

export function PageHeaderFooterProperties({ section }: Props) {
  const template = useTemplateStore((s) => s.template);
  const updateSectionHeight = useTemplateStore((s) => s.updateSectionHeight);
  const updateHeaderConfig = useTemplateStore((s) => s.updateHeaderConfig);
  const updateFooterConfig = useTemplateStore((s) => s.updateFooterConfig);
  const disableSection = useTemplateStore((s) => s.disableSection);

  const sectionDef = section === 'header' ? template.header : template.footer;
  if (!sectionDef) return null;

  const label = section === 'header' ? 'Page Header' : 'Page Footer';

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-gray-700">{label} Properties</h3>
        <button
          className="p-1 rounded text-red-400 hover:bg-red-50 hover:text-red-600"
          title={`Remove ${label}`}
          onClick={() => disableSection(section)}
        >
          <Trash2 size={16} />
        </button>
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">
          Height (pt)
        </label>
        <input
          type="number"
          min={20}
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={sectionDef.height}
          onChange={(e) => {
            const v = Number(e.target.value);
            if (v >= 20) updateSectionHeight(section, v);
          }}
        />
      </div>

      <div className="flex items-center gap-2">
        <input
          type="checkbox"
          id={`${section}-firstPage`}
          checked={sectionDef.firstPageDifferent}
          onChange={(e) => {
            const fn = section === 'header' ? updateHeaderConfig : updateFooterConfig;
            fn({ firstPageDifferent: e.target.checked });
          }}
          className="rounded border-gray-300"
        />
        <label htmlFor={`${section}-firstPage`} className="text-xs text-gray-600">
          Different first page
        </label>
      </div>
      <div className="flex items-center gap-2">
        <input
          type="checkbox"
          id={`${section}-oddEven`}
          checked={sectionDef.oddEvenDifferent}
          onChange={(e) => {
            const fn = section === 'header' ? updateHeaderConfig : updateFooterConfig;
            fn({ oddEvenDifferent: e.target.checked });
          }}
          className="rounded border-gray-300"
        />
        <label htmlFor={`${section}-oddEven`} className="text-xs text-gray-600">
          Different odd/even pages
        </label>
      </div>
    </div>
  );
}
