import type { TemplateElement } from '../../types/template';

export function TextElementView({ element }: { element: TemplateElement }) {
  const style = element.style || {};
  return (
    <div
      style={{
        fontFamily: style.fontFamily || 'sans-serif',
        fontSize: style.fontSize ? `${style.fontSize}px` : '12px',
        fontWeight: style.bold ? 'bold' : 'normal',
        fontStyle: style.italic ? 'italic' : 'normal',
        textDecoration: style.underline ? 'underline' : 'none',
        color: style.textColor || '#000',
        textAlign: (style.alignment?.toLowerCase() as 'left' | 'center' | 'right' | 'justify') || 'left',
        lineHeight: style.lineHeight ? `${style.lineHeight}` : undefined,
        backgroundColor: style.backgroundColor || undefined,
      }}
    >
      {element.content || 'Empty text'}
    </div>
  );
}
