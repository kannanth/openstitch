import { create } from 'zustand';
import type { TemplateElement, SelectedArea } from '../types/template';

export interface DragData {
  type: 'palette' | 'element';
  paletteDefaults?: Omit<TemplateElement, 'id'>;
  elementId?: string;
  sourceArea?: SelectedArea;
}

interface UIState {
  zoom: number;
  showGrid: boolean;
  sidebarTab: 'elements' | 'templates' | 'settings';
  previewOpen: boolean;
  collapsedPageHeader: boolean;
  collapsedPageFooter: boolean;
  collapsedSections: Record<string, boolean>;
  dragData: DragData | null;
  setZoom: (zoom: number) => void;
  toggleGrid: () => void;
  setSidebarTab: (tab: 'elements' | 'templates' | 'settings') => void;
  setPreviewOpen: (open: boolean) => void;
  togglePageHeaderCollapsed: () => void;
  togglePageFooterCollapsed: () => void;
  toggleSectionCollapsed: (sectionId: string) => void;
  setDragData: (data: DragData) => void;
  clearDragData: () => void;
}

export const useUIStore = create<UIState>((set) => ({
  zoom: 1,
  showGrid: true,
  sidebarTab: 'elements',
  previewOpen: false,
  collapsedPageHeader: false,
  collapsedPageFooter: false,
  collapsedSections: {},
  dragData: null,
  setZoom: (zoom) => set({ zoom }),
  toggleGrid: () => set((s) => ({ showGrid: !s.showGrid })),
  setSidebarTab: (tab) => set({ sidebarTab: tab }),
  setPreviewOpen: (open) => set({ previewOpen: open }),
  togglePageHeaderCollapsed: () =>
    set((s) => ({ collapsedPageHeader: !s.collapsedPageHeader })),
  togglePageFooterCollapsed: () =>
    set((s) => ({ collapsedPageFooter: !s.collapsedPageFooter })),
  toggleSectionCollapsed: (sectionId) =>
    set((s) => ({
      collapsedSections: {
        ...s.collapsedSections,
        [sectionId]: !s.collapsedSections[sectionId],
      },
    })),
  setDragData: (data) => set({ dragData: data }),
  clearDragData: () => set({ dragData: null }),
}));
