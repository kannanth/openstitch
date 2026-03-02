import type { TemplateElement, NumberFormatPreset } from '../../types/template';

function getPlaceholderValue(preset?: NumberFormatPreset): string {
  switch (preset) {
    case 'CURRENCY_USD': return '$0.00';
    case 'CURRENCY_EUR': return '€0.00';
    case 'CURRENCY_GBP': return '£0.00';
    case 'PERCENTAGE': return '0.00%';
    case 'ACCOUNTING': return '0.00';
    case 'NUMBER_2DP': return '0.00';
    case 'NUMBER_0DP': return '0';
    default: return '';
  }
}

export function TableElementView({ element }: { element: TemplateElement }) {
  const columns = element.columns || [];
  return (
    <div className="text-xs">
      <table className="w-full border-collapse border border-gray-300">
        {element.showHeader !== false && (
          <thead>
            <tr className="bg-gray-100">
              {columns.map((col, i) => (
                <th
                  key={i}
                  className="border border-gray-300 px-2 py-1 text-left"
                >
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
        )}
        <tbody>
          <tr>
            {columns.map((col, i) => {
              const placeholder = col.formatPreset
                ? getPlaceholderValue(col.formatPreset)
                : `\${${col.field}}`;
              const hasConditionalFormats = (col.conditionalFormats?.length ?? 0) > 0;
              return (
                <td
                  key={i}
                  className="border border-gray-300 px-2 py-1 text-gray-400"
                  style={{
                    whiteSpace: col.wrapText ? 'normal' : 'nowrap',
                    wordWrap: col.wrapText ? 'break-word' : undefined,
                    textAlign: col.alignment === 'RIGHT' ? 'right' : col.alignment === 'CENTER' ? 'center' : 'left',
                  }}
                >
                  {hasConditionalFormats && (
                    <span
                      className="inline-block w-1.5 h-1.5 rounded-full mr-1"
                      style={{ backgroundColor: col.conditionalFormats![0].style.textColor || '#ef4444' }}
                      title="Has conditional formatting"
                    />
                  )}
                  {placeholder}
                </td>
              );
            })}
          </tr>
        </tbody>
      </table>
      <div className="text-gray-400 mt-1">Data: {element.dataSource}</div>
    </div>
  );
}
