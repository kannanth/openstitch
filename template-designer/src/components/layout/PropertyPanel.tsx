import { useTemplateStore, getElementsForArea } from '../../store/templateStore';
import { TextProperties } from '../properties/TextProperties';
import { TableProperties } from '../properties/TableProperties';
import { ImageProperties } from '../properties/ImageProperties';
import { ChartProperties } from '../properties/ChartProperties';
import { CommonProperties } from '../properties/CommonProperties';
import { SectionProperties } from '../properties/SectionProperties';
import { PageHeaderFooterProperties } from '../properties/PageHeaderFooterProperties';
import { Trash2 } from 'lucide-react';

export function PropertyPanel() {
  const template = useTemplateStore((s) => s.template);
  const selectedElementId = useTemplateStore((s) => s.selectedElementId);
  const selectedArea = useTemplateStore((s) => s.selectedArea);
  const removeAreaElement = useTemplateStore((s) => s.removeAreaElement);
  const selectElement = useTemplateStore((s) => s.selectElement);

  const areaElements = getElementsForArea(template, selectedArea);
  const selectedElement = selectedElementId
    ? areaElements.find((el) => el.id === selectedElementId)
    : null;

  // Show section properties when no element selected
  if (!selectedElement) {
    if (selectedArea.type === 'pageHeader' && template.header) {
      return (
        <div className="w-72 bg-white border-l overflow-y-auto p-4">
          <PageHeaderFooterProperties section="header" />
        </div>
      );
    }
    if (selectedArea.type === 'pageFooter' && template.footer) {
      return (
        <div className="w-72 bg-white border-l overflow-y-auto p-4">
          <PageHeaderFooterProperties section="footer" />
        </div>
      );
    }
    if (selectedArea.type === 'section') {
      return (
        <div className="w-72 bg-white border-l overflow-y-auto p-4">
          <SectionProperties sectionIndex={selectedArea.sectionIndex ?? 0} />
        </div>
      );
    }
  }

  return (
    <div className="w-72 bg-white border-l overflow-y-auto p-4">
      {!selectedElement ? (
        <div className="text-sm text-gray-400 text-center mt-8">
          Select an element to edit its properties
        </div>
      ) : (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-gray-700">
              {selectedElement.type} Properties
            </h3>
            <button
              className="p-1 rounded text-red-400 hover:bg-red-50 hover:text-red-600"
              title="Delete element"
              onClick={() => {
                removeAreaElement(selectedArea, selectedElement.id);
                selectElement(null);
              }}
            >
              <Trash2 size={16} />
            </button>
          </div>

          <div className="border-t pt-3">
            {selectedElement.type === 'TEXT' && (
              <TextProperties element={selectedElement} />
            )}
            {selectedElement.type === 'TABLE' && (
              <TableProperties element={selectedElement} />
            )}
            {selectedElement.type === 'IMAGE' && (
              <ImageProperties element={selectedElement} />
            )}
            {selectedElement.type === 'CHART' && (
              <ChartProperties element={selectedElement} />
            )}
            {selectedElement.type === 'CONDITIONAL' && (
              <div className="text-sm text-gray-500">
                <label className="block text-xs font-medium text-gray-600 mb-1">
                  Condition Expression
                </label>
                <pre className="bg-gray-50 p-2 rounded text-xs overflow-auto">
                  {selectedElement.condition}
                </pre>
              </div>
            )}
            {selectedElement.type === 'REPEATING_SECTION' && (
              <div className="text-sm text-gray-500">
                <label className="block text-xs font-medium text-gray-600 mb-1">
                  Data Source
                </label>
                <pre className="bg-gray-50 p-2 rounded text-xs overflow-auto">
                  {selectedElement.dataSource}
                </pre>
              </div>
            )}
          </div>

          <div className="border-t pt-3">
            <CommonProperties element={selectedElement} />
          </div>
        </div>
      )}
    </div>
  );
}
