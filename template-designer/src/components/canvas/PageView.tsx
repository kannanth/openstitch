import { useTemplateStore, getElementsForArea } from '../../store/templateStore';
import { useUIStore } from '../../store/uiStore';
import { SectionZone } from './SectionZone';
import { BodySectionZone } from './BodySectionZone';
import { Plus } from 'lucide-react';

const PAGE_SIZES: Record<string, { width: number; height: number }> = {
  A4: { width: 595.28, height: 841.89 },
  LETTER: { width: 612, height: 792 },
  LEGAL: { width: 612, height: 1008 },
};

export function PageView() {
  const template = useTemplateStore((s) => s.template);
  const selectElement = useTemplateStore((s) => s.selectElement);
  const addSection = useTemplateStore((s) => s.addSection);
  const updateSectionHeight = useTemplateStore((s) => s.updateSectionHeight);
  const showGrid = useUIStore((s) => s.showGrid);
  const collapsedPageHeader = useUIStore((s) => s.collapsedPageHeader);
  const collapsedPageFooter = useUIStore((s) => s.collapsedPageFooter);
  const togglePageHeaderCollapsed = useUIStore((s) => s.togglePageHeaderCollapsed);
  const togglePageFooterCollapsed = useUIStore((s) => s.togglePageFooterCollapsed);

  const { pageLayout } = template;
  const { pageSize, orientation, margins } = pageLayout;

  let size = PAGE_SIZES[pageSize] || PAGE_SIZES.A4;
  if (pageSize === 'CUSTOM' && pageLayout.customWidth && pageLayout.customHeight) {
    size = { width: pageLayout.customWidth, height: pageLayout.customHeight };
  }

  const pageWidth = orientation === 'LANDSCAPE' ? size.height : size.width;
  const pageHeight = orientation === 'LANDSCAPE' ? size.width : size.height;
  const contentWidth = pageWidth - margins.left - margins.right;

  return (
    <div
      className="relative bg-white shadow-lg"
      style={{ width: `${pageWidth}px`, height: `${pageHeight}px` }}
      onClick={(e) => {
        if (e.target === e.currentTarget) selectElement(null);
      }}
    >
      {/* Grid pattern */}
      {showGrid && (
        <svg
          className="absolute inset-0 pointer-events-none"
          width={pageWidth}
          height={pageHeight}
        >
          <defs>
            <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
              <path
                d="M 20 0 L 0 0 0 20"
                fill="none"
                stroke="#e5e7eb"
                strokeWidth="0.5"
              />
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#grid)" />
        </svg>
      )}

      {/* Margin guidelines */}
      <div
        className="absolute border border-dashed border-blue-200 pointer-events-none"
        style={{
          top: `${margins.top}px`,
          left: `${margins.left}px`,
          right: `${margins.right}px`,
          bottom: `${margins.bottom}px`,
        }}
      />

      {/* Content area with section zones */}
      <div
        className="absolute overflow-hidden"
        style={{
          top: `${margins.top}px`,
          left: `${margins.left}px`,
          width: `${contentWidth}px`,
          height: `${pageHeight - margins.top - margins.bottom}px`,
        }}
      >
        {template.header && (
          <SectionZone
            area={{ type: 'pageHeader' }}
            elements={getElementsForArea(template, { type: 'pageHeader' })}
            height={template.header.height}
            contentWidth={contentWidth}
            label="Page Header"
            color="bg-blue-50"
            ringColor="ring-blue-300"
            activeColor="bg-blue-500"
            borderColor="border-l-blue-500"
            collapsed={collapsedPageHeader}
            onToggleCollapsed={togglePageHeaderCollapsed}
            onResize={(h) => updateSectionHeight('header', h)}
          />
        )}

        {/* Body sections */}
        {template.body.sections.map((section, index) => (
          <BodySectionZone
            key={section.id}
            section={section}
            sectionIndex={index}
            contentWidth={contentWidth}
          />
        ))}

        {/* Add Section button */}
        <div className="flex justify-center py-1">
          <button
            className="flex items-center gap-1 px-2 py-0.5 text-[10px] text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors"
            onClick={() => addSection()}
          >
            <Plus size={10} />
            Add Section
          </button>
        </div>

        {template.footer && (
          <SectionZone
            area={{ type: 'pageFooter' }}
            elements={getElementsForArea(template, { type: 'pageFooter' })}
            height={template.footer.height}
            contentWidth={contentWidth}
            label="Page Footer"
            color="bg-orange-50"
            ringColor="ring-orange-300"
            activeColor="bg-orange-500"
            borderColor="border-l-orange-500"
            collapsed={collapsedPageFooter}
            onToggleCollapsed={togglePageFooterCollapsed}
            onResize={(h) => updateSectionHeight('footer', h)}
          />
        )}
      </div>
    </div>
  );
}
