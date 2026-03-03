import { create } from 'zustand';
import { v4 as uuidv4 } from 'uuid';
import type { Template, TemplateElement, SelectedArea } from '../types/template';
import { useTemplateStore, defaultTemplate, defaultArea } from './templateStore';

interface TabSnapshot {
  template: Template;
  selectedElementId: string | null;
  selectedArea: SelectedArea;
  clipboard: TemplateElement | null;
  undoStack: Template[];
  redoStack: Template[];
}

interface Tab {
  id: string;
  name: string;
  snapshot: TabSnapshot;
}

interface TabState {
  tabs: Tab[];
  activeTabId: string;
  createTab: (template: Template) => void;
  switchTab: (tabId: string) => void;
  closeTab: (tabId: string) => void;
}

function snapshotFromTemplateStore(): TabSnapshot {
  const s = useTemplateStore.getState();
  return {
    template: s.template,
    selectedElementId: s.selectedElementId,
    selectedArea: s.selectedArea,
    clipboard: s.clipboard,
    undoStack: s.undoStack,
    redoStack: s.redoStack,
  };
}

const initialTabId = uuidv4();

export const useTabStore = create<TabState>((set, get) => ({
  tabs: [
    {
      id: initialTabId,
      name: defaultTemplate.metadata.name,
      snapshot: {
        template: defaultTemplate,
        selectedElementId: null,
        selectedArea: defaultArea,
        clipboard: null,
        undoStack: [],
        redoStack: [],
      },
    },
  ],
  activeTabId: initialTabId,

  createTab: (template) => {
    const state = get();
    const currentSnapshot = snapshotFromTemplateStore();

    const tabs = state.tabs.map((tab) =>
      tab.id === state.activeTabId
        ? { ...tab, name: currentSnapshot.template.metadata.name, snapshot: currentSnapshot }
        : tab
    );

    const newId = uuidv4();
    const newTab: Tab = {
      id: newId,
      name: template.metadata.name,
      snapshot: {
        template,
        selectedElementId: null,
        selectedArea: defaultArea,
        clipboard: null,
        undoStack: [],
        redoStack: [],
      },
    };

    set({ tabs: [...tabs, newTab], activeTabId: newId });
    useTemplateStore.getState().loadState(newTab.snapshot);
  },

  switchTab: (tabId) => {
    const state = get();
    if (tabId === state.activeTabId) return;

    const currentSnapshot = snapshotFromTemplateStore();
    const tabs = state.tabs.map((tab) =>
      tab.id === state.activeTabId
        ? { ...tab, name: currentSnapshot.template.metadata.name, snapshot: currentSnapshot }
        : tab
    );

    const target = tabs.find((t) => t.id === tabId);
    if (!target) return;

    set({ tabs, activeTabId: tabId });
    useTemplateStore.getState().loadState(target.snapshot);
  },

  closeTab: (tabId) => {
    const state = get();
    if (state.tabs.length <= 1) return;

    const idx = state.tabs.findIndex((t) => t.id === tabId);
    if (idx === -1) return;

    const newTabs = state.tabs.filter((t) => t.id !== tabId);

    if (tabId === state.activeTabId) {
      const newActiveIdx = Math.min(idx, newTabs.length - 1);
      const newActive = newTabs[newActiveIdx];
      set({ tabs: newTabs, activeTabId: newActive.id });
      useTemplateStore.getState().loadState(newActive.snapshot);
    } else {
      set({ tabs: newTabs });
    }
  },
}));
