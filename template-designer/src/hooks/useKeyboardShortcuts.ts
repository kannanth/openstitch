import { useEffect } from 'react';
import { useTemplateStore } from '../store/templateStore';
import { useUIStore } from '../store/uiStore';

export function useKeyboardShortcuts() {
  const undo = useTemplateStore((s) => s.undo);
  const redo = useTemplateStore((s) => s.redo);
  const removeElement = useTemplateStore((s) => s.removeElement);
  const selectElement = useTemplateStore((s) => s.selectElement);
  const copyElement = useTemplateStore((s) => s.copyElement);
  const pasteElement = useTemplateStore((s) => s.pasteElement);
  const cutElement = useTemplateStore((s) => s.cutElement);
  const duplicateElement = useTemplateStore((s) => s.duplicateElement);
  const nudgeElement = useTemplateStore((s) => s.nudgeElement);
  const selectedElementId = useTemplateStore((s) => s.selectedElementId);
  const setPreviewOpen = useUIStore((s) => s.setPreviewOpen);

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      const target = e.target as HTMLElement;
      const tag = target.tagName;
      if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT' || target.isContentEditable) {
        return;
      }

      const mod = e.metaKey || e.ctrlKey;

      // Ctrl/Cmd+Z - Undo
      if (mod && !e.shiftKey && e.key === 'z') {
        e.preventDefault();
        undo();
        return;
      }

      // Ctrl/Cmd+Shift+Z or Ctrl/Cmd+Y - Redo
      if ((mod && e.shiftKey && e.key === 'z') || (mod && e.key === 'y')) {
        e.preventDefault();
        redo();
        return;
      }

      // Delete / Backspace - Remove selected
      if ((e.key === 'Delete' || e.key === 'Backspace') && selectedElementId) {
        e.preventDefault();
        removeElement(selectedElementId);
        return;
      }

      // Ctrl/Cmd+C - Copy
      if (mod && e.key === 'c') {
        e.preventDefault();
        copyElement();
        return;
      }

      // Ctrl/Cmd+V - Paste
      if (mod && e.key === 'v') {
        e.preventDefault();
        pasteElement();
        return;
      }

      // Ctrl/Cmd+X - Cut
      if (mod && e.key === 'x') {
        e.preventDefault();
        cutElement();
        return;
      }

      // Ctrl/Cmd+D - Duplicate
      if (mod && e.key === 'd') {
        e.preventDefault();
        duplicateElement();
        return;
      }

      // Escape - Deselect
      if (e.key === 'Escape') {
        e.preventDefault();
        selectElement(null);
        return;
      }

      // Arrow keys - Nudge
      if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key) && selectedElementId) {
        e.preventDefault();
        const step = e.shiftKey ? 10 : 1;
        const dx = e.key === 'ArrowLeft' ? -step : e.key === 'ArrowRight' ? step : 0;
        const dy = e.key === 'ArrowUp' ? -step : e.key === 'ArrowDown' ? step : 0;
        nudgeElement(dx, dy);
        return;
      }

      // Ctrl/Cmd+S - Save
      if (mod && e.key === 's') {
        e.preventDefault();
        document.dispatchEvent(new CustomEvent('template:save'));
        return;
      }

      // Ctrl/Cmd+P - Preview
      if (mod && e.key === 'p') {
        e.preventDefault();
        setPreviewOpen(true);
        return;
      }
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [
    undo, redo, removeElement, selectElement, copyElement, pasteElement,
    cutElement, duplicateElement, nudgeElement, selectedElementId, setPreviewOpen,
  ]);
}
