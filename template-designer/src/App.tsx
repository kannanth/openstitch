import { Toolbar } from './components/layout/Toolbar';
import { Sidebar } from './components/layout/Sidebar';
import { DesignCanvas } from './components/canvas/DesignCanvas';
import { PropertyPanel } from './components/layout/PropertyPanel';
import { PreviewModal } from './components/preview/PreviewModal';
import { useUIStore } from './store/uiStore';
import { useKeyboardShortcuts } from './hooks/useKeyboardShortcuts';

function App() {
  const previewOpen = useUIStore((s) => s.previewOpen);
  useKeyboardShortcuts();

  return (
    <div className="h-screen flex flex-col bg-gray-100">
      <Toolbar />
      <div className="flex flex-1 overflow-hidden">
        <Sidebar />
        <DesignCanvas />
        <PropertyPanel />
      </div>
      {previewOpen && <PreviewModal />}
    </div>
  );
}

export default App;
