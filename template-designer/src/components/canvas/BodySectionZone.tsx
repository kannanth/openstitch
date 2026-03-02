import type { Section } from '../../types/template';
import { useTemplateStore } from '../../store/templateStore';
import { useUIStore } from '../../store/uiStore';
import { getElementsForArea } from '../../store/templateStore';
import { SectionZone } from './SectionZone';
import { ChevronDown, ChevronRight, Database } from 'lucide-react';

interface Props {
  section: Section;
  sectionIndex: number;
  contentWidth: number;
}

export function BodySectionZone({ section, sectionIndex, contentWidth }: Props) {
  const template = useTemplateStore((s) => s.template);
  const updateSectionBandHeight = useTemplateStore((s) => s.updateSectionBandHeight);
  const collapsed = useUIStore((s) => s.collapsedSections[section.id]);
  const toggleCollapsed = useUIStore((s) => s.toggleSectionCollapsed);

  return (
    <div className="border-t border-gray-200 first:border-t-0">
      {/* Section label bar */}
      <div
        className="flex items-center gap-1 px-2 py-0.5 bg-gray-200/80 border-b border-gray-300 cursor-pointer select-none text-[10px]"
        onClick={() => toggleCollapsed(section.id)}
      >
        <button className="p-0.5 hover:bg-gray-300 rounded">
          {collapsed ? <ChevronRight size={10} /> : <ChevronDown size={10} />}
        </button>
        <span className="font-semibold text-gray-700">{section.name}</span>
        {section.dataSource && (
          <span className="flex items-center gap-0.5 text-purple-500 ml-1">
            <Database size={8} />
            <span>{section.dataSource}</span>
          </span>
        )}
      </div>

      {!collapsed && (
        <>
          {/* Section Header band */}
          {section.sectionHeader && (
            <SectionZone
              area={{ type: 'section', sectionIndex, part: 'sectionHeader' }}
              elements={getElementsForArea(template, { type: 'section', sectionIndex, part: 'sectionHeader' })}
              height={section.sectionHeader.height}
              contentWidth={contentWidth}
              label="Section Header"
              color="bg-indigo-50"
              ringColor="ring-indigo-300"
              activeColor="bg-indigo-500"
              borderColor="border-l-indigo-500"
              collapsed={false}
              onToggleCollapsed={() => {}}
              onResize={(h) => updateSectionBandHeight(sectionIndex, 'sectionHeader', h)}
            />
          )}

          {/* Section Body */}
          <SectionZone
            area={{ type: 'section', sectionIndex, part: 'sectionBody' }}
            elements={getElementsForArea(template, { type: 'section', sectionIndex, part: 'sectionBody' })}
            height="auto"
            contentWidth={contentWidth}
            label="Elements"
            color="bg-white"
            ringColor="ring-gray-300"
            activeColor="bg-gray-600"
            borderColor="border-l-gray-600"
            collapsed={false}
            onToggleCollapsed={() => {}}
          />

          {/* Section Footer band */}
          {section.sectionFooter && (
            <SectionZone
              area={{ type: 'section', sectionIndex, part: 'sectionFooter' }}
              elements={getElementsForArea(template, { type: 'section', sectionIndex, part: 'sectionFooter' })}
              height={section.sectionFooter.height}
              contentWidth={contentWidth}
              label="Section Footer"
              color="bg-slate-50"
              ringColor="ring-slate-300"
              activeColor="bg-slate-500"
              borderColor="border-l-slate-500"
              collapsed={false}
              onToggleCollapsed={() => {}}
              onResize={(h) => updateSectionBandHeight(sectionIndex, 'sectionFooter', h)}
            />
          )}
        </>
      )}
    </div>
  );
}
