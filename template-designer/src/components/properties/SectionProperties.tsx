import { useTemplateStore } from '../../store/templateStore';
import { Trash2 } from 'lucide-react';

interface Props {
  sectionIndex: number;
}

export function SectionProperties({ sectionIndex }: Props) {
  const template = useTemplateStore((s) => s.template);
  const updateSectionName = useTemplateStore((s) => s.updateSectionName);
  const updateSectionDataSource = useTemplateStore((s) => s.updateSectionDataSource);
  const toggleSectionHeader = useTemplateStore((s) => s.toggleSectionHeader);
  const toggleSectionFooter = useTemplateStore((s) => s.toggleSectionFooter);
  const updateSectionBandHeight = useTemplateStore((s) => s.updateSectionBandHeight);
  const toggleRepeatHeaderOnPageBreak = useTemplateStore((s) => s.toggleRepeatHeaderOnPageBreak);
  const toggleRepeatFooterOnPageBreak = useTemplateStore((s) => s.toggleRepeatFooterOnPageBreak);
  const removeSection = useTemplateStore((s) => s.removeSection);

  const section = template.body.sections[sectionIndex];
  if (!section) return null;

  const canDelete = template.body.sections.length > 1;

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-gray-700">Section Properties</h3>
        {canDelete && (
          <button
            className="p-1 rounded text-red-400 hover:bg-red-50 hover:text-red-600"
            title="Delete section"
            onClick={() => removeSection(sectionIndex)}
          >
            <Trash2 size={16} />
          </button>
        )}
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Name</label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={section.name}
          onChange={(e) => updateSectionName(sectionIndex, e.target.value)}
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-gray-600 mb-1">Data Source</label>
        <input
          type="text"
          className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
          value={section.dataSource ?? ''}
          placeholder="e.g., items"
          onChange={(e) => updateSectionDataSource(sectionIndex, e.target.value)}
        />
        <p className="text-[10px] text-gray-400 mt-0.5">
          Bind to a data list to repeat this section per item
        </p>
      </div>

      {/* Section Header toggle */}
      <div className="border-t pt-3 space-y-2">
        <div className="flex items-center gap-2">
          <input
            type="checkbox"
            id="section-header-toggle"
            checked={!!section.sectionHeader}
            onChange={() => toggleSectionHeader(sectionIndex)}
            className="rounded border-gray-300"
          />
          <label htmlFor="section-header-toggle" className="text-xs text-gray-600">
            Section Header
          </label>
        </div>
        {section.sectionHeader && (
          <>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">
                Header Height (pt)
              </label>
              <input
                type="number"
                min={20}
                className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
                value={section.sectionHeader.height}
                onChange={(e) => {
                  const v = Number(e.target.value);
                  if (v >= 20) updateSectionBandHeight(sectionIndex, 'sectionHeader', v);
                }}
              />
            </div>
            <div className="flex items-center gap-2">
              <input
                type="checkbox"
                id="repeat-header"
                checked={!!section.repeatHeaderOnPageBreak}
                onChange={() => toggleRepeatHeaderOnPageBreak(sectionIndex)}
                className="rounded border-gray-300"
              />
              <label htmlFor="repeat-header" className="text-xs text-gray-600">
                Repeat on page break
              </label>
            </div>
          </>
        )}
      </div>

      {/* Section Footer toggle */}
      <div className="border-t pt-3 space-y-2">
        <div className="flex items-center gap-2">
          <input
            type="checkbox"
            id="section-footer-toggle"
            checked={!!section.sectionFooter}
            onChange={() => toggleSectionFooter(sectionIndex)}
            className="rounded border-gray-300"
          />
          <label htmlFor="section-footer-toggle" className="text-xs text-gray-600">
            Section Footer
          </label>
        </div>
        {section.sectionFooter && (
          <>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">
                Footer Height (pt)
              </label>
              <input
                type="number"
                min={20}
                className="w-full border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
                value={section.sectionFooter.height}
                onChange={(e) => {
                  const v = Number(e.target.value);
                  if (v >= 20) updateSectionBandHeight(sectionIndex, 'sectionFooter', v);
                }}
              />
            </div>
            <div className="flex items-center gap-2">
              <input
                type="checkbox"
                id="repeat-footer"
                checked={!!section.repeatFooterOnPageBreak}
                onChange={() => toggleRepeatFooterOnPageBreak(sectionIndex)}
                className="rounded border-gray-300"
              />
              <label htmlFor="repeat-footer" className="text-xs text-gray-600">
                Repeat on page break
              </label>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
