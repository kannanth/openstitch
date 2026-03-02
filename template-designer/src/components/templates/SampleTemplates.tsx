import {
  DollarSign,
  FileSpreadsheet,
  FileText,
  Receipt,
  ClipboardList,
  BarChart3,
  Users,
} from 'lucide-react';
import { useTemplateStore } from '../../store/templateStore';
import type { Template } from '../../types/template';

interface SampleTemplate {
  name: string;
  description: string;
  icon: React.ReactNode;
  template: Template;
}

const sampleTemplates: SampleTemplate[] = [
  {
    name: 'Income Statement',
    description: 'Profit & loss report with current/prior period comparison and variance',
    icon: <DollarSign size={18} />,
    template: {
      metadata: { name: 'Income Statement', description: 'Profit & Loss Report', version: 1 },
      pageLayout: {
        pageSize: 'LETTER',
        orientation: 'PORTRAIT',
        margins: { top: 72, right: 56, bottom: 72, left: 56 },
      },
      header: {
        height: 60,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [
          {
            id: 'h1',
            type: 'TEXT',
            positioning: 'FLOW',
            content: '${company.name}',
            style: { fontSize: 16, bold: true, alignment: 'CENTER' },
          },
          {
            id: 'h2',
            type: 'TEXT',
            positioning: 'FLOW',
            content: 'Income Statement - Period Ending ${reportDate}',
            style: { fontSize: 10, alignment: 'CENTER', textColor: '#6b7280' },
          },
        ],
      },
      footer: {
        height: 30,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [
          {
            id: 'f1',
            type: 'TEXT',
            positioning: 'FLOW',
            content: 'Confidential - ${company.name}',
            style: { fontSize: 8, alignment: 'CENTER', textColor: '#9ca3af' },
          },
        ],
      },
      body: {
        sections: [
          {
            id: 'is-revenue',
            name: 'Revenue',
            sectionHeader: {
              height: 24,
              elements: [
                { id: 'sh1', type: 'TEXT', positioning: 'FLOW', content: 'Revenue', style: { fontSize: 12, bold: true, textColor: '#1e3a5f' } },
              ],
            },
            elements: [
              {
                id: 'b1',
                type: 'TABLE',
                positioning: 'FLOW',
                dataSource: 'revenueItems',
                showHeader: true,
                columns: [
                  { header: 'Description', field: 'description', alignment: 'LEFT', wrapText: true },
                  { header: 'Current Period', field: 'currentPeriod', alignment: 'RIGHT', formatPreset: 'ACCOUNTING', format: '#,##0.00;(#,##0.00)' },
                  { header: 'Prior Period', field: 'priorPeriod', alignment: 'RIGHT', formatPreset: 'ACCOUNTING', format: '#,##0.00;(#,##0.00)' },
                  {
                    header: 'Variance', field: 'variance', alignment: 'RIGHT', formatPreset: 'ACCOUNTING', format: '#,##0.00;(#,##0.00)',
                    conditionalFormats: [{ condition: 'value < 0', style: { textColor: '#dc2626' } }],
                  },
                ],
                footerCells: [
                  { content: 'Total Revenue', colSpan: 1 },
                  { content: '${sum(revenueItems, "currentPeriod")}', colSpan: 1 },
                  { content: '${sum(revenueItems, "priorPeriod")}', colSpan: 1 },
                  { content: '${sum(revenueItems, "variance")}', colSpan: 1 },
                ],
                alternateRowColor: '#f8fafc',
                headerStyle: { bold: true, backgroundColor: '#1e3a5f', textColor: '#ffffff', fontSize: 10 },
                rowStyle: { fontSize: 10 },
              },
            ],
          },
          {
            id: 'is-expenses',
            name: 'Expenses',
            sectionHeader: {
              height: 24,
              elements: [
                { id: 'sh2', type: 'TEXT', positioning: 'FLOW', content: 'Expenses', style: { fontSize: 12, bold: true, textColor: '#1e3a5f' }, marginTop: 12 },
              ],
            },
            elements: [
              {
                id: 'b2',
                type: 'TABLE',
                positioning: 'FLOW',
                dataSource: 'expenseItems',
                showHeader: true,
                columns: [
                  { header: 'Description', field: 'description', alignment: 'LEFT', wrapText: true },
                  { header: 'Current Period', field: 'currentPeriod', alignment: 'RIGHT', formatPreset: 'ACCOUNTING', format: '#,##0.00;(#,##0.00)' },
                  { header: 'Prior Period', field: 'priorPeriod', alignment: 'RIGHT', formatPreset: 'ACCOUNTING', format: '#,##0.00;(#,##0.00)' },
                  {
                    header: 'Variance', field: 'variance', alignment: 'RIGHT', formatPreset: 'ACCOUNTING', format: '#,##0.00;(#,##0.00)',
                    conditionalFormats: [{ condition: 'value < 0', style: { textColor: '#dc2626' } }],
                  },
                ],
                footerCells: [
                  { content: 'Total Expenses', colSpan: 1 },
                  { content: '${sum(expenseItems, "currentPeriod")}', colSpan: 1 },
                  { content: '${sum(expenseItems, "priorPeriod")}', colSpan: 1 },
                  { content: '${sum(expenseItems, "variance")}', colSpan: 1 },
                ],
                alternateRowColor: '#fef2f2',
                headerStyle: { bold: true, backgroundColor: '#7f1d1d', textColor: '#ffffff', fontSize: 10 },
                rowStyle: { fontSize: 10 },
              },
            ],
            sectionFooter: {
              height: 30,
              elements: [
                { id: 'sf2', type: 'TEXT', positioning: 'FLOW', content: 'Net Income = Total Revenue - Total Expenses', style: { fontSize: 10, bold: true, alignment: 'RIGHT', textColor: '#1e3a5f' }, marginTop: 8 },
              ],
            },
          },
        ],
      },
    },
  },
  {
    name: 'Balance Sheet',
    description: 'Assets, liabilities, and equity snapshot with account balances',
    icon: <FileSpreadsheet size={18} />,
    template: {
      metadata: { name: 'Balance Sheet', description: 'Statement of Financial Position', version: 1 },
      pageLayout: {
        pageSize: 'LETTER',
        orientation: 'PORTRAIT',
        margins: { top: 72, right: 56, bottom: 72, left: 56 },
      },
      header: {
        height: 60,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [
          {
            id: 'h1',
            type: 'TEXT',
            positioning: 'FLOW',
            content: '${company.name}',
            style: { fontSize: 16, bold: true, alignment: 'CENTER' },
          },
          {
            id: 'h2',
            type: 'TEXT',
            positioning: 'FLOW',
            content: 'Balance Sheet as of ${reportDate}',
            style: { fontSize: 10, alignment: 'CENTER', textColor: '#6b7280' },
          },
        ],
      },
      pageNumbering: {
        enabled: true,
        format: 'PAGE_X_OF_Y',
        startFrom: 1,
      },
      body: {
        sections: [
          {
            id: 'bs-assets',
            name: 'Assets',
            sectionHeader: {
              height: 24,
              elements: [
                { id: 'sh1', type: 'TEXT', positioning: 'FLOW', content: 'Assets', style: { fontSize: 13, bold: true, textColor: '#1e3a5f' } },
              ],
            },
            repeatHeaderOnPageBreak: true,
            elements: [
              {
                id: 'b2',
                type: 'TABLE',
                positioning: 'FLOW',
                dataSource: 'assets',
                showHeader: true,
                columns: [
                  { header: 'Account', field: 'account', alignment: 'LEFT', wrapText: true },
                  { header: 'Balance', field: 'balance', alignment: 'RIGHT', formatPreset: 'CURRENCY_USD', format: '$#,##0.00' },
                ],
                footerCells: [
                  { content: 'Total Assets', colSpan: 1 },
                  { content: '${sum(assets, "balance")}', colSpan: 1 },
                ],
                alternateRowColor: '#f8fafc',
                headerStyle: { bold: true, backgroundColor: '#1e3a5f', textColor: '#ffffff', fontSize: 10 },
                rowStyle: { fontSize: 10 },
              },
            ],
          },
          {
            id: 'bs-liabilities',
            name: 'Liabilities & Equity',
            sectionHeader: {
              height: 24,
              elements: [
                { id: 'sh2', type: 'TEXT', positioning: 'FLOW', content: 'Liabilities & Equity', style: { fontSize: 13, bold: true, textColor: '#374151' }, marginTop: 12 },
              ],
            },
            repeatHeaderOnPageBreak: true,
            elements: [
              {
                id: 'b4',
                type: 'TABLE',
                positioning: 'FLOW',
                dataSource: 'liabilities',
                showHeader: true,
                columns: [
                  { header: 'Account', field: 'account', alignment: 'LEFT', wrapText: true },
                  { header: 'Balance', field: 'balance', alignment: 'RIGHT', formatPreset: 'CURRENCY_USD', format: '$#,##0.00' },
                ],
                footerCells: [
                  { content: 'Total Liabilities & Equity', colSpan: 1 },
                  { content: '${sum(liabilities, "balance")}', colSpan: 1 },
                ],
                alternateRowColor: '#f8fafc',
                headerStyle: { bold: true, backgroundColor: '#374151', textColor: '#ffffff', fontSize: 10 },
                rowStyle: { fontSize: 10 },
              },
            ],
          },
        ],
      },
    },
  },
  {
    name: 'Invoice',
    description: 'Professional invoice with company details, line items, and totals',
    icon: <Receipt size={18} />,
    template: {
      metadata: { name: 'Invoice', description: 'Customer Invoice', version: 1 },
      pageLayout: {
        pageSize: 'LETTER',
        orientation: 'PORTRAIT',
        margins: { top: 56, right: 56, bottom: 56, left: 56 },
      },
      header: {
        height: 80,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [
          {
            id: 'h1',
            type: 'TEXT',
            positioning: 'FLOW',
            content: '${company.name}',
            style: { fontSize: 20, bold: true },
          },
          {
            id: 'h2',
            type: 'TEXT',
            positioning: 'FLOW',
            content: '${company.address}',
            style: { fontSize: 9, textColor: '#6b7280' },
          },
          {
            id: 'h3',
            type: 'TEXT',
            positioning: 'FLOW',
            content: 'INVOICE #${invoiceNumber}',
            style: { fontSize: 14, bold: true, alignment: 'RIGHT' },
          },
        ],
      },
      pageNumbering: {
        enabled: true,
        format: 'PAGE_X_OF_Y',
        startFrom: 1,
      },
      body: {
        sections: [
          {
            id: 'inv-details',
            name: 'Invoice Details',
            elements: [
              {
                id: 'b0',
                type: 'TEXT',
                positioning: 'FLOW',
                content: 'Bill To: ${customer.name}\n${customer.address}',
                style: { fontSize: 10 },
                marginTop: 8,
              },
              {
                id: 'b1',
                type: 'TEXT',
                positioning: 'FLOW',
                content: 'Invoice Date: ${invoiceDate}    Due Date: ${dueDate}',
                style: { fontSize: 9, textColor: '#6b7280' },
                marginTop: 8,
              },
            ],
          },
          {
            id: 'inv-items',
            name: 'Line Items',
            sectionHeader: {
              height: 20,
              elements: [
                { id: 'sh1', type: 'TEXT', positioning: 'FLOW', content: 'Items', style: { fontSize: 11, bold: true }, marginTop: 12 },
              ],
            },
            repeatHeaderOnPageBreak: true,
            elements: [
              {
                id: 'b2',
                type: 'TABLE',
                positioning: 'FLOW',
                dataSource: 'lineItems',
                showHeader: true,
                columns: [
                  { header: 'Description', field: 'description', alignment: 'LEFT', wrapText: true },
                  { header: 'Qty', field: 'quantity', alignment: 'CENTER', width: 50 },
                  { header: 'Unit Price', field: 'unitPrice', alignment: 'RIGHT', formatPreset: 'CURRENCY_USD', format: '$#,##0.00', width: 90 },
                  { header: 'Amount', field: 'amount', alignment: 'RIGHT', formatPreset: 'CURRENCY_USD', format: '$#,##0.00', width: 90 },
                ],
                footerCells: [
                  { content: 'Total', colSpan: 3 },
                  { content: '${sum(lineItems, "amount")}', colSpan: 1 },
                ],
                headerStyle: { bold: true, backgroundColor: '#111827', textColor: '#ffffff', fontSize: 10 },
                rowStyle: { fontSize: 10 },
                alternateRowColor: '#f9fafb',
              },
            ],
            sectionFooter: {
              height: 24,
              elements: [
                { id: 'sf1', type: 'TEXT', positioning: 'FLOW', content: 'Payment Terms: ${paymentTerms}', style: { fontSize: 9, textColor: '#6b7280' }, marginTop: 16 },
              ],
            },
          },
        ],
      },
      footer: {
        height: 25,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [
          {
            id: 'f1',
            type: 'TEXT',
            positioning: 'FLOW',
            content: 'Thank you for your business!',
            style: { fontSize: 9, alignment: 'CENTER', textColor: '#9ca3af', italic: true },
          },
        ],
      },
    },
  },
  {
    name: 'Sales Report',
    description: 'Monthly sales summary with bar chart and top performers table',
    icon: <BarChart3 size={18} />,
    template: {
      metadata: { name: 'Sales Report', description: 'Monthly Sales Summary', version: 1 },
      pageLayout: {
        pageSize: 'LETTER',
        orientation: 'LANDSCAPE',
        margins: { top: 56, right: 56, bottom: 56, left: 56 },
      },
      header: {
        height: 50,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [
          {
            id: 'h1',
            type: 'TEXT',
            positioning: 'FLOW',
            content: 'Monthly Sales Report - ${reportMonth}',
            style: { fontSize: 18, bold: true, alignment: 'CENTER' },
          },
        ],
      },
      body: {
        sections: [
          {
            id: 'sr-chart',
            name: 'Sales Chart',
            elements: [
              {
                id: 'b1',
                type: 'CHART',
                positioning: 'FLOW',
                chartType: 'BAR',
                dataSource: 'monthlySales',
                categoryField: 'month',
                valueFields: ['revenue', 'target'],
                title: 'Revenue vs Target',
                showLegend: true,
                dimension: { width: 680, height: 280 },
              },
            ],
          },
          {
            id: 'sr-performers',
            name: 'Top Performers',
            sectionHeader: {
              height: 24,
              elements: [
                { id: 'sh2', type: 'TEXT', positioning: 'FLOW', content: 'Top Performers', style: { fontSize: 13, bold: true, textColor: '#059669' }, marginTop: 16 },
              ],
            },
            elements: [
              {
                id: 'b3',
                type: 'TABLE',
                positioning: 'FLOW',
                dataSource: 'topPerformers',
                showHeader: true,
                columns: [
                  { header: 'Rank', field: 'rank', alignment: 'CENTER', width: 50 },
                  { header: 'Sales Rep', field: 'name', alignment: 'LEFT' },
                  { header: 'Revenue', field: 'revenue', alignment: 'RIGHT', formatPreset: 'CURRENCY_USD', format: '$#,##0.00' },
                  { header: '% of Target', field: 'targetPercent', alignment: 'RIGHT', formatPreset: 'PERCENTAGE', format: '#,##0.00%' },
                ],
                headerStyle: { bold: true, backgroundColor: '#059669', textColor: '#ffffff', fontSize: 10 },
                rowStyle: { fontSize: 10 },
                alternateRowColor: '#ecfdf5',
              },
            ],
          },
        ],
      },
    },
  },
  {
    name: 'Employee Directory',
    description: 'Paginated staff listing with department grouping',
    icon: <Users size={18} />,
    template: {
      metadata: { name: 'Employee Directory', description: 'Staff Listing', version: 1 },
      pageLayout: {
        pageSize: 'A4',
        orientation: 'PORTRAIT',
        margins: { top: 72, right: 56, bottom: 72, left: 56 },
      },
      header: {
        height: 45,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [
          {
            id: 'h1',
            type: 'TEXT',
            positioning: 'FLOW',
            content: '${company.name} - Employee Directory',
            style: { fontSize: 14, bold: true, alignment: 'CENTER' },
          },
        ],
      },
      pageNumbering: {
        enabled: true,
        format: 'PAGE_X_OF_Y',
        startFrom: 1,
      },
      body: {
        sections: [{
          id: 'ed-main',
          name: 'Main',
          elements: [
            {
              id: 'b1',
              type: 'TABLE',
              positioning: 'FLOW',
              dataSource: 'employees',
              showHeader: true,
              columns: [
                { header: 'Name', field: 'name', alignment: 'LEFT', wrapText: true },
                { header: 'Department', field: 'department', alignment: 'LEFT' },
                { header: 'Title', field: 'title', alignment: 'LEFT', wrapText: true },
                { header: 'Email', field: 'email', alignment: 'LEFT' },
                { header: 'Phone', field: 'phone', alignment: 'LEFT', width: 100 },
              ],
              headerStyle: { bold: true, backgroundColor: '#4f46e5', textColor: '#ffffff', fontSize: 10 },
              rowStyle: { fontSize: 9 },
              alternateRowColor: '#eef2ff',
            },
          ],
        }],
      },
      footer: {
        height: 25,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [
          {
            id: 'f1',
            type: 'TEXT',
            positioning: 'FLOW',
            content: 'Internal Use Only - Generated ${reportDate}',
            style: { fontSize: 8, alignment: 'CENTER', textColor: '#9ca3af' },
          },
        ],
      },
    },
  },
  {
    name: 'Expense Report',
    description: 'Employee expense report with categories and approval section',
    icon: <ClipboardList size={18} />,
    template: {
      metadata: { name: 'Expense Report', description: 'Employee Expense Report', version: 1 },
      pageLayout: {
        pageSize: 'LETTER',
        orientation: 'PORTRAIT',
        margins: { top: 56, right: 56, bottom: 56, left: 56 },
      },
      header: {
        height: 55,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [
          {
            id: 'h1',
            type: 'TEXT',
            positioning: 'FLOW',
            content: 'Expense Report',
            style: { fontSize: 18, bold: true, alignment: 'CENTER' },
          },
          {
            id: 'h2',
            type: 'TEXT',
            positioning: 'FLOW',
            content: '${employee.name} | ${employee.department} | Period: ${periodStart} - ${periodEnd}',
            style: { fontSize: 9, alignment: 'CENTER', textColor: '#6b7280' },
          },
        ],
      },
      body: {
        sections: [
          {
            id: 'er-expenses',
            name: 'Expenses',
            sectionHeader: {
              height: 20,
              elements: [
                { id: 'sh1', type: 'TEXT', positioning: 'FLOW', content: 'Expense Details', style: { fontSize: 11, bold: true, textColor: '#b91c1c' } },
              ],
            },
            repeatHeaderOnPageBreak: true,
            elements: [
              {
                id: 'b1',
                type: 'TABLE',
                positioning: 'FLOW',
                dataSource: 'expenses',
                showHeader: true,
                columns: [
                  { header: 'Date', field: 'date', alignment: 'LEFT', width: 80 },
                  { header: 'Category', field: 'category', alignment: 'LEFT', width: 100 },
                  { header: 'Description', field: 'description', alignment: 'LEFT', wrapText: true },
                  { header: 'Amount', field: 'amount', alignment: 'RIGHT', formatPreset: 'CURRENCY_USD', format: '$#,##0.00', width: 90 },
                ],
                footerCells: [
                  { content: 'Total Expenses', colSpan: 3 },
                  { content: '${sum(expenses, "amount")}', colSpan: 1 },
                ],
                headerStyle: { bold: true, backgroundColor: '#b91c1c', textColor: '#ffffff', fontSize: 10 },
                rowStyle: { fontSize: 10 },
                alternateRowColor: '#fef2f2',
              },
            ],
          },
          {
            id: 'er-approval',
            name: 'Approval',
            elements: [
              {
                id: 'b2',
                type: 'TEXT',
                positioning: 'FLOW',
                content: 'Approved by: ____________________    Date: ____________',
                style: { fontSize: 10 },
                marginTop: 32,
              },
            ],
          },
        ],
      },
    },
  },
  {
    name: 'Simple Report',
    description: 'Minimal report with a title, text body, and page numbers',
    icon: <FileText size={18} />,
    template: {
      metadata: { name: 'Simple Report', description: 'Basic Report Template', version: 1 },
      pageLayout: {
        pageSize: 'A4',
        orientation: 'PORTRAIT',
        margins: { top: 72, right: 72, bottom: 72, left: 72 },
      },
      header: {
        height: 40,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [
          {
            id: 'h1',
            type: 'TEXT',
            positioning: 'FLOW',
            content: '${reportTitle}',
            style: { fontSize: 16, bold: true, alignment: 'CENTER' },
          },
        ],
      },
      body: {
        sections: [{
          id: 'sr2-main',
          name: 'Main',
          elements: [
            {
              id: 'b1',
              type: 'TEXT',
              positioning: 'FLOW',
              content: '${reportContent}',
              style: { fontSize: 11, lineHeight: 1.5 },
            },
          ],
        }],
      },
      footer: {
        height: 25,
        firstPageDifferent: false,
        oddEvenDifferent: false,
        defaultElements: [
          {
            id: 'f1',
            type: 'TEXT',
            positioning: 'FLOW',
            content: 'Page ${pageNumber} of ${totalPages} | ${company.name}',
            style: { fontSize: 8, alignment: 'CENTER', textColor: '#9ca3af' },
          },
        ],
      },
      pageNumbering: {
        enabled: true,
        format: 'PAGE_X_OF_Y',
        startFrom: 1,
      },
    },
  },
];

export function SampleTemplates() {
  const setTemplate = useTemplateStore((s) => s.setTemplate);

  const handleSelect = (sample: SampleTemplate) => {
    setTemplate(structuredClone(sample.template));
  };

  return (
    <div>
      <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-3">
        Sample Templates
      </h3>
      <div className="space-y-2">
        {sampleTemplates.map((sample) => (
          <button
            key={sample.name}
            className="w-full flex items-start gap-2.5 p-3 rounded-lg border border-gray-200 hover:border-blue-400 hover:bg-blue-50 transition-colors text-left group"
            onClick={() => handleSelect(sample)}
          >
            <div className="mt-0.5 text-gray-400 group-hover:text-blue-600 shrink-0">
              {sample.icon}
            </div>
            <div className="min-w-0">
              <div className="text-xs font-semibold text-gray-700 group-hover:text-blue-700">
                {sample.name}
              </div>
              <div className="text-[10px] text-gray-400 leading-tight mt-0.5">
                {sample.description}
              </div>
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}
