import { create } from 'zustand';
import type {
  Template,
  TemplateElement,
  PageLayout,
  HeaderDefinition,
  FooterDefinition,
  PageNumbering,
  SelectedArea,
  Section,
} from '../types/template';
import { v4 as uuidv4 } from 'uuid';

// --- Area helpers ---

function getElementsForArea(template: Template, area: SelectedArea): TemplateElement[] {
  switch (area.type) {
    case 'pageHeader':
      return template.header?.defaultElements ?? [];
    case 'pageFooter':
      return template.footer?.defaultElements ?? [];
    case 'section': {
      const section = template.body.sections[area.sectionIndex ?? 0];
      if (!section) return [];
      switch (area.part) {
        case 'sectionHeader':
          return section.sectionHeader?.elements ?? [];
        case 'sectionFooter':
          return section.sectionFooter?.elements ?? [];
        case 'sectionBody':
        default:
          return section.elements;
      }
    }
  }
}

function setElementsForArea(
  template: Template,
  area: SelectedArea,
  elements: TemplateElement[]
): Template {
  switch (area.type) {
    case 'pageHeader':
      return template.header
        ? { ...template, header: { ...template.header, defaultElements: elements } }
        : template;
    case 'pageFooter':
      return template.footer
        ? { ...template, footer: { ...template.footer, defaultElements: elements } }
        : template;
    case 'section': {
      const idx = area.sectionIndex ?? 0;
      const section = template.body.sections[idx];
      if (!section) return template;
      let updatedSection: Section;
      switch (area.part) {
        case 'sectionHeader':
          updatedSection = section.sectionHeader
            ? { ...section, sectionHeader: { ...section.sectionHeader, elements } }
            : section;
          break;
        case 'sectionFooter':
          updatedSection = section.sectionFooter
            ? { ...section, sectionFooter: { ...section.sectionFooter, elements } }
            : section;
          break;
        case 'sectionBody':
        default:
          updatedSection = { ...section, elements };
          break;
      }
      const newSections = [...template.body.sections];
      newSections[idx] = updatedSection;
      return { ...template, body: { ...template.body, sections: newSections } };
    }
  }
}

export { getElementsForArea };

// --- Backward compatibility alias ---
export function getElementsForSection(template: Template, area: SelectedArea): TemplateElement[] {
  return getElementsForArea(template, area);
}

// --- Store interface ---

interface TemplateState {
  template: Template;
  selectedElementId: string | null;
  selectedArea: SelectedArea;
  clipboard: TemplateElement | null;
  undoStack: Template[];
  redoStack: Template[];

  setTemplate: (template: Template) => void;
  updatePageLayout: (layout: Partial<PageLayout>) => void;
  updateMetadata: (updates: Partial<Template['metadata']>) => void;
  selectElement: (id: string | null) => void;
  setSelectedArea: (area: SelectedArea) => void;

  // Area-aware element CRUD
  addElementToArea: (area: SelectedArea, element: Omit<TemplateElement, 'id'>) => void;
  updateAreaElement: (area: SelectedArea, id: string, updates: Partial<TemplateElement>) => void;
  removeAreaElement: (area: SelectedArea, id: string) => void;

  // Cross-area operations
  moveElementBetweenAreas: (fromArea: SelectedArea, toArea: SelectedArea, elementId: string, insertIndex?: number) => void;
  insertElementToArea: (area: SelectedArea, element: Omit<TemplateElement, 'id'>, index: number) => void;
  moveElementInArea: (area: SelectedArea, elementId: string, newIndex: number) => void;

  // Delegating methods (use current selectedArea)
  addElement: (element: Omit<TemplateElement, 'id'>) => void;
  updateElement: (id: string, updates: Partial<TemplateElement>) => void;
  removeElement: (id: string) => void;
  moveElement: (id: string, newIndex: number) => void;

  // Page header/footer management
  updateSectionHeight: (section: 'header' | 'footer', height: number) => void;
  enableSection: (section: 'header' | 'footer') => void;
  disableSection: (section: 'header' | 'footer') => void;
  updateHeaderConfig: (updates: Partial<Pick<HeaderDefinition, 'firstPageDifferent' | 'oddEvenDifferent'>>) => void;
  updateFooterConfig: (updates: Partial<Pick<FooterDefinition, 'firstPageDifferent' | 'oddEvenDifferent'>>) => void;

  // Section management
  addSection: (name?: string) => void;
  removeSection: (sectionIndex: number) => void;
  updateSectionName: (sectionIndex: number, name: string) => void;
  updateSectionDataSource: (sectionIndex: number, dataSource: string) => void;
  toggleSectionHeader: (sectionIndex: number) => void;
  toggleSectionFooter: (sectionIndex: number) => void;
  updateSectionBandHeight: (sectionIndex: number, band: 'sectionHeader' | 'sectionFooter', height: number) => void;
  toggleRepeatHeaderOnPageBreak: (sectionIndex: number) => void;
  toggleRepeatFooterOnPageBreak: (sectionIndex: number) => void;

  // Clipboard
  copyElement: () => void;
  pasteElement: () => void;
  cutElement: () => void;
  duplicateElement: () => void;

  // Page numbering
  togglePageNumbering: () => void;
  updatePageNumbering: (updates: Partial<PageNumbering>) => void;

  // Nudge
  nudgeElement: (dx: number, dy: number) => void;

  undo: () => void;
  redo: () => void;
  resetTemplate: () => void;
}

const defaultTemplate: Template = {
  metadata: { name: 'Untitled Template', version: 1 },
  pageLayout: {
    pageSize: 'A4',
    orientation: 'PORTRAIT',
    margins: { top: 72, right: 72, bottom: 72, left: 72 }
  },
  body: { sections: [{ id: uuidv4(), name: 'Main', elements: [] }] }
};

const defaultArea: SelectedArea = { type: 'section', sectionIndex: 0, part: 'sectionBody' };

export const useTemplateStore = create<TemplateState>((set, get) => ({
  template: defaultTemplate,
  selectedElementId: null,
  selectedArea: defaultArea,
  clipboard: null,
  undoStack: [],
  redoStack: [],

  setTemplate: (template) => set({ template, selectedElementId: null }),

  updatePageLayout: (layoutUpdates) => {
    const state = get();
    const newTemplate = {
      ...state.template,
      pageLayout: { ...state.template.pageLayout, ...layoutUpdates }
    };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: []
    });
  },

  updateMetadata: (updates) => {
    const state = get();
    const newTemplate = {
      ...state.template,
      metadata: { ...state.template.metadata, ...updates }
    };
    set({ template: newTemplate });
  },

  selectElement: (id) => set({ selectedElementId: id }),
  setSelectedArea: (area) => set({ selectedArea: area }),

  // Area-aware CRUD
  addElementToArea: (area, elementData) => {
    const state = get();
    const element: TemplateElement = { ...elementData, id: uuidv4() };
    const currentElements = getElementsForArea(state.template, area);
    const newTemplate = setElementsForArea(state.template, area, [...currentElements, element]);
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
      selectedElementId: element.id,
      selectedArea: area,
    });
  },

  updateAreaElement: (area, id, updates) => {
    const state = get();
    const currentElements = getElementsForArea(state.template, area);
    const newElements = currentElements.map(el => el.id === id ? { ...el, ...updates } : el);
    const newTemplate = setElementsForArea(state.template, area, newElements);
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: []
    });
  },

  removeAreaElement: (area, id) => {
    const state = get();
    const currentElements = getElementsForArea(state.template, area);
    const newElements = currentElements.filter(el => el.id !== id);
    const newTemplate = setElementsForArea(state.template, area, newElements);
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
      selectedElementId: null
    });
  },

  // Cross-area operations
  moveElementBetweenAreas: (fromArea, toArea, elementId, insertIndex) => {
    const state = get();
    const fromElements = [...getElementsForArea(state.template, fromArea)];
    const elIndex = fromElements.findIndex(el => el.id === elementId);
    if (elIndex === -1) return;
    const [moved] = fromElements.splice(elIndex, 1);
    let t = setElementsForArea(state.template, fromArea, fromElements);
    const toElements = [...getElementsForArea(t, toArea)];
    if (insertIndex != null && insertIndex >= 0) {
      toElements.splice(insertIndex, 0, moved);
    } else {
      toElements.push(moved);
    }
    t = setElementsForArea(t, toArea, toElements);
    set({
      template: t,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
      selectedElementId: moved.id,
      selectedArea: toArea,
    });
  },

  insertElementToArea: (area, elementData, index) => {
    const state = get();
    const element: TemplateElement = { ...elementData, id: uuidv4() };
    const currentElements = [...getElementsForArea(state.template, area)];
    if (index >= 0 && index <= currentElements.length) {
      currentElements.splice(index, 0, element);
    } else {
      currentElements.push(element);
    }
    const newTemplate = setElementsForArea(state.template, area, currentElements);
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
      selectedElementId: element.id,
      selectedArea: area,
    });
  },

  moveElementInArea: (area, elementId, newIndex) => {
    const state = get();
    const elements = [...getElementsForArea(state.template, area)];
    const oldIndex = elements.findIndex(el => el.id === elementId);
    if (oldIndex === -1) return;
    const [moved] = elements.splice(oldIndex, 1);
    elements.splice(newIndex, 0, moved);
    const newTemplate = setElementsForArea(state.template, area, elements);
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
    });
  },

  // Delegating methods
  addElement: (elementData) => {
    get().addElementToArea(get().selectedArea, elementData);
  },

  updateElement: (id, updates) => {
    get().updateAreaElement(get().selectedArea, id, updates);
  },

  removeElement: (id) => {
    get().removeAreaElement(get().selectedArea, id);
  },

  moveElement: (id, newIndex) => {
    const state = get();
    const area = state.selectedArea;
    const elements = [...getElementsForArea(state.template, area)];
    const oldIndex = elements.findIndex(el => el.id === id);
    if (oldIndex === -1) return;
    const [moved] = elements.splice(oldIndex, 1);
    elements.splice(newIndex, 0, moved);
    const newTemplate = setElementsForArea(state.template, area, elements);
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: []
    });
  },

  // Page header/footer management
  updateSectionHeight: (section, height) => {
    const state = get();
    let newTemplate = { ...state.template };
    if (section === 'header' && newTemplate.header) {
      newTemplate = { ...newTemplate, header: { ...newTemplate.header, height } };
    } else if (section === 'footer' && newTemplate.footer) {
      newTemplate = { ...newTemplate, footer: { ...newTemplate.footer, height } };
    }
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: []
    });
  },

  enableSection: (section) => {
    const state = get();
    let newTemplate = { ...state.template };
    if (section === 'header' && !newTemplate.header) {
      newTemplate.header = {
        height: 60,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [],
      };
    } else if (section === 'footer' && !newTemplate.footer) {
      newTemplate.footer = {
        height: 60,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [],
      };
    }
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: []
    });
  },

  disableSection: (section) => {
    const state = get();
    let newTemplate = { ...state.template };
    if (section === 'header') newTemplate = { ...newTemplate, header: undefined };
    else if (section === 'footer') newTemplate = { ...newTemplate, footer: undefined };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
      selectedArea: defaultArea,
    });
  },

  updateHeaderConfig: (updates) => {
    const state = get();
    if (!state.template.header) return;
    const newTemplate = {
      ...state.template,
      header: { ...state.template.header, ...updates }
    };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: []
    });
  },

  updateFooterConfig: (updates) => {
    const state = get();
    if (!state.template.footer) return;
    const newTemplate = {
      ...state.template,
      footer: { ...state.template.footer, ...updates }
    };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: []
    });
  },

  // Section management
  addSection: (name?) => {
    const state = get();
    const sections = state.template.body.sections;
    const newSection: Section = {
      id: uuidv4(),
      name: name ?? `Section ${sections.length + 1}`,
      elements: [],
    };
    const newTemplate = {
      ...state.template,
      body: { ...state.template.body, sections: [...sections, newSection] },
    };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
    });
  },

  removeSection: (sectionIndex) => {
    const state = get();
    const sections = state.template.body.sections;
    if (sections.length <= 1) return; // Must keep at least 1
    const newSections = sections.filter((_, i) => i !== sectionIndex);
    const newTemplate = {
      ...state.template,
      body: { ...state.template.body, sections: newSections },
    };
    const area = state.selectedArea;
    const newArea = area.type === 'section' && (area.sectionIndex ?? 0) >= newSections.length
      ? { ...area, sectionIndex: newSections.length - 1 }
      : area;
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
      selectedArea: newArea,
      selectedElementId: null,
    });
  },

  updateSectionName: (sectionIndex, name) => {
    const state = get();
    const sections = [...state.template.body.sections];
    if (!sections[sectionIndex]) return;
    sections[sectionIndex] = { ...sections[sectionIndex], name };
    const newTemplate = { ...state.template, body: { ...state.template.body, sections } };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
    });
  },

  updateSectionDataSource: (sectionIndex, dataSource) => {
    const state = get();
    const sections = [...state.template.body.sections];
    if (!sections[sectionIndex]) return;
    sections[sectionIndex] = { ...sections[sectionIndex], dataSource: dataSource || undefined };
    const newTemplate = { ...state.template, body: { ...state.template.body, sections } };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
    });
  },

  toggleSectionHeader: (sectionIndex) => {
    const state = get();
    const sections = [...state.template.body.sections];
    const section = sections[sectionIndex];
    if (!section) return;
    sections[sectionIndex] = section.sectionHeader
      ? { ...section, sectionHeader: undefined }
      : { ...section, sectionHeader: { height: 30, elements: [] } };
    const newTemplate = { ...state.template, body: { ...state.template.body, sections } };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
    });
  },

  toggleSectionFooter: (sectionIndex) => {
    const state = get();
    const sections = [...state.template.body.sections];
    const section = sections[sectionIndex];
    if (!section) return;
    sections[sectionIndex] = section.sectionFooter
      ? { ...section, sectionFooter: undefined }
      : { ...section, sectionFooter: { height: 30, elements: [] } };
    const newTemplate = { ...state.template, body: { ...state.template.body, sections } };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
    });
  },

  updateSectionBandHeight: (sectionIndex, band, height) => {
    const state = get();
    const sections = [...state.template.body.sections];
    const section = sections[sectionIndex];
    if (!section) return;
    if (band === 'sectionHeader' && section.sectionHeader) {
      sections[sectionIndex] = { ...section, sectionHeader: { ...section.sectionHeader, height } };
    } else if (band === 'sectionFooter' && section.sectionFooter) {
      sections[sectionIndex] = { ...section, sectionFooter: { ...section.sectionFooter, height } };
    }
    const newTemplate = { ...state.template, body: { ...state.template.body, sections } };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
    });
  },

  toggleRepeatHeaderOnPageBreak: (sectionIndex) => {
    const state = get();
    const sections = [...state.template.body.sections];
    const section = sections[sectionIndex];
    if (!section) return;
    sections[sectionIndex] = { ...section, repeatHeaderOnPageBreak: !section.repeatHeaderOnPageBreak };
    const newTemplate = { ...state.template, body: { ...state.template.body, sections } };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
    });
  },

  toggleRepeatFooterOnPageBreak: (sectionIndex) => {
    const state = get();
    const sections = [...state.template.body.sections];
    const section = sections[sectionIndex];
    if (!section) return;
    sections[sectionIndex] = { ...section, repeatFooterOnPageBreak: !section.repeatFooterOnPageBreak };
    const newTemplate = { ...state.template, body: { ...state.template.body, sections } };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
    });
  },

  // Clipboard operations
  copyElement: () => {
    const state = get();
    if (!state.selectedElementId) return;
    const elements = getElementsForArea(state.template, state.selectedArea);
    const el = elements.find(e => e.id === state.selectedElementId);
    if (el) {
      set({ clipboard: structuredClone(el) });
    }
  },

  pasteElement: () => {
    const state = get();
    if (!state.clipboard) return;
    const pasted: TemplateElement = {
      ...structuredClone(state.clipboard),
      id: uuidv4(),
      position: state.clipboard.position
        ? { x: state.clipboard.position.x + 10, y: state.clipboard.position.y + 10 }
        : undefined,
    };
    const elements = getElementsForArea(state.template, state.selectedArea);
    const newTemplate = setElementsForArea(state.template, state.selectedArea, [...elements, pasted]);
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
      selectedElementId: pasted.id,
    });
  },

  cutElement: () => {
    const state = get();
    if (!state.selectedElementId) return;
    const elements = getElementsForArea(state.template, state.selectedArea);
    const el = elements.find(e => e.id === state.selectedElementId);
    if (!el) return;
    const clipboard = structuredClone(el);
    const newElements = elements.filter(e => e.id !== state.selectedElementId);
    const newTemplate = setElementsForArea(state.template, state.selectedArea, newElements);
    set({
      clipboard,
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
      selectedElementId: null,
    });
  },

  duplicateElement: () => {
    const state = get();
    if (!state.selectedElementId) return;
    const elements = getElementsForArea(state.template, state.selectedArea);
    const el = elements.find(e => e.id === state.selectedElementId);
    if (!el) return;
    const dup: TemplateElement = {
      ...structuredClone(el),
      id: uuidv4(),
      position: el.position
        ? { x: el.position.x + 10, y: el.position.y + 10 }
        : undefined,
    };
    const newTemplate = setElementsForArea(state.template, state.selectedArea, [...elements, dup]);
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
      selectedElementId: dup.id,
    });
  },

  // Page numbering
  togglePageNumbering: () => {
    const state = get();
    const newTemplate = state.template.pageNumbering
      ? { ...state.template, pageNumbering: undefined }
      : {
          ...state.template,
          pageNumbering: {
            enabled: true,
            format: 'PAGE_X_OF_Y' as const,
            startFrom: 1,
          },
        };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
    });
  },

  updatePageNumbering: (updates) => {
    const state = get();
    if (!state.template.pageNumbering) return;
    const newTemplate = {
      ...state.template,
      pageNumbering: { ...state.template.pageNumbering, ...updates },
    };
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: [],
    });
  },

  // Nudge
  nudgeElement: (dx, dy) => {
    const state = get();
    if (!state.selectedElementId) return;
    const elements = getElementsForArea(state.template, state.selectedArea);
    const el = elements.find(e => e.id === state.selectedElementId);
    if (!el) return;

    let updates: Partial<TemplateElement>;
    if (el.positioning === 'ABSOLUTE' && el.position) {
      updates = {
        position: {
          x: Math.max(0, el.position.x + dx),
          y: Math.max(0, el.position.y + dy),
        },
      };
    } else {
      updates = {
        marginTop: Math.max(0, (el.marginTop ?? 0) + dy),
      };
    }

    const newElements = elements.map(e => e.id === state.selectedElementId ? { ...e, ...updates } : e);
    const newTemplate = setElementsForArea(state.template, state.selectedArea, newElements);
    set({
      template: newTemplate,
      undoStack: [...state.undoStack, state.template],
      redoStack: []
    });
  },

  undo: () => {
    const state = get();
    if (state.undoStack.length === 0) return;
    const previous = state.undoStack[state.undoStack.length - 1];
    set({
      template: previous,
      undoStack: state.undoStack.slice(0, -1),
      redoStack: [...state.redoStack, state.template],
      selectedElementId: null
    });
  },

  redo: () => {
    const state = get();
    if (state.redoStack.length === 0) return;
    const next = state.redoStack[state.redoStack.length - 1];
    set({
      template: next,
      undoStack: [...state.undoStack, state.template],
      redoStack: state.redoStack.slice(0, -1),
      selectedElementId: null
    });
  },

  resetTemplate: () => set({
    template: defaultTemplate,
    undoStack: [],
    redoStack: [],
    selectedElementId: null,
    selectedArea: defaultArea,
    clipboard: null,
  })
}));
