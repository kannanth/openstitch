import { useRef, useEffect } from 'react';
import {
  Undo2,
  Redo2,
  ZoomIn,
  ZoomOut,
  Eye,
  Grid3x3,
  FileDown,
  FileUp,
  FilePlus,
  PanelTop,
  PanelBottom,
  Plus,
} from 'lucide-react';
import { useTemplateStore, defaultTemplate } from '../../store/templateStore';
import { useTabStore } from '../../store/tabStore';
import { useUIStore } from '../../store/uiStore';
import { migrateTemplate } from '../../utils/migrateTemplate';

export function Toolbar() {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const template = useTemplateStore((s) => s.template);
  const undoStack = useTemplateStore((s) => s.undoStack);
  const redoStack = useTemplateStore((s) => s.redoStack);
  const undo = useTemplateStore((s) => s.undo);
  const createTab = useTabStore((s) => s.createTab);
  const redo = useTemplateStore((s) => s.redo);
  const enableSection = useTemplateStore((s) => s.enableSection);
  const disableSection = useTemplateStore((s) => s.disableSection);
  const addSection = useTemplateStore((s) => s.addSection);

  const zoom = useUIStore((s) => s.zoom);
  const showGrid = useUIStore((s) => s.showGrid);
  const setZoom = useUIStore((s) => s.setZoom);
  const toggleGrid = useUIStore((s) => s.toggleGrid);
  const setPreviewOpen = useUIStore((s) => s.setPreviewOpen);

  const btnClass = 'p-2 rounded hover:bg-gray-100 disabled:opacity-30';

  const handleExport = () => {
    const json = JSON.stringify(template, null, 2);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${template.metadata.name || 'template'}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleImport = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (ev) => {
      try {
        const parsed = JSON.parse(ev.target?.result as string);
        createTab(migrateTemplate(parsed));
      } catch {
        alert('Invalid JSON file');
      }
    };
    reader.readAsText(file);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  // Listen for Ctrl+S save event from keyboard shortcuts
  useEffect(() => {
    const handleSave = () => handleExport();
    document.addEventListener('template:save', handleSave);
    return () => document.removeEventListener('template:save', handleSave);
  });

  const hasHeader = !!template.header;
  const hasFooter = !!template.footer;

  const togglePageSection = (section: 'header' | 'footer', active: boolean) => {
    if (active) disableSection(section);
    else enableSection(section);
  };

  return (
    <div className="flex items-center gap-1 px-4 py-2 bg-white border-b shadow-sm">
      <button className={btnClass} onClick={() => createTab(structuredClone(defaultTemplate))} title="New Template">
        <FilePlus size={18} />
      </button>

      <input
        ref={fileInputRef}
        type="file"
        accept=".json"
        className="hidden"
        onChange={handleImport}
      />
      <button
        className={btnClass}
        onClick={() => fileInputRef.current?.click()}
        title="Import JSON"
      >
        <FileUp size={18} />
      </button>

      <button className={btnClass} onClick={handleExport} title="Export JSON (Ctrl+S)">
        <FileDown size={18} />
      </button>

      <div className="w-px h-6 bg-gray-300 mx-1" />

      <button
        className={btnClass}
        onClick={undo}
        disabled={undoStack.length === 0}
        title="Undo (Ctrl+Z)"
      >
        <Undo2 size={18} />
      </button>
      <button
        className={btnClass}
        onClick={redo}
        disabled={redoStack.length === 0}
        title="Redo (Ctrl+Shift+Z)"
      >
        <Redo2 size={18} />
      </button>

      <div className="w-px h-6 bg-gray-300 mx-1" />

      <button
        className={btnClass}
        onClick={() => setZoom(Math.max(0.25, zoom - 0.1))}
        title="Zoom Out"
      >
        <ZoomOut size={18} />
      </button>
      <span className="text-sm text-gray-600 min-w-[3rem] text-center select-none">
        {Math.round(zoom * 100)}%
      </span>
      <button
        className={btnClass}
        onClick={() => setZoom(Math.min(3, zoom + 0.1))}
        title="Zoom In"
      >
        <ZoomIn size={18} />
      </button>

      <div className="w-px h-6 bg-gray-300 mx-1" />

      <button
        className={`${btnClass} ${showGrid ? 'bg-blue-100 text-blue-600' : ''}`}
        onClick={toggleGrid}
        title="Toggle Grid"
      >
        <Grid3x3 size={18} />
      </button>

      <button
        className={btnClass}
        onClick={() => setPreviewOpen(true)}
        title="Preview (Ctrl+P)"
      >
        <Eye size={18} />
      </button>

      <div className="w-px h-6 bg-gray-300 mx-1" />

      {/* Section toggle buttons */}
      <button
        className={`${btnClass} text-xs ${hasHeader ? 'bg-blue-100 text-blue-600' : ''}`}
        onClick={() => togglePageSection('header', hasHeader)}
        title={hasHeader ? 'Remove Header' : 'Add Header'}
      >
        <PanelTop size={18} />
      </button>
      <button
        className={`${btnClass} text-xs ${hasFooter ? 'bg-orange-100 text-orange-600' : ''}`}
        onClick={() => togglePageSection('footer', hasFooter)}
        title={hasFooter ? 'Remove Footer' : 'Add Footer'}
      >
        <PanelBottom size={18} />
      </button>
      <button
        className={`${btnClass} text-xs`}
        onClick={() => addSection()}
        title="Add Section"
      >
        <Plus size={18} />
      </button>

      <div className="ml-auto text-sm text-gray-500 select-none">
        {template.metadata.name}
      </div>
    </div>
  );
}
