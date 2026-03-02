import { BarChart3, PieChart, TrendingUp } from 'lucide-react';
import type { TemplateElement } from '../../types/template';

export function ChartElementView({ element }: { element: TemplateElement }) {
  const Icon =
    element.chartType === 'PIE'
      ? PieChart
      : element.chartType === 'LINE'
        ? TrendingUp
        : BarChart3;

  return (
    <div className="flex items-center justify-center bg-blue-50 border border-dashed border-blue-300 rounded p-4">
      <div className="text-center text-blue-500">
        <Icon className="mx-auto mb-1" size={32} />
        <div className="text-xs font-medium">
          {element.chartType || 'BAR'} Chart
        </div>
        {element.title && (
          <div className="text-xs text-gray-500">{element.title}</div>
        )}
      </div>
    </div>
  );
}
