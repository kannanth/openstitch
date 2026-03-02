export type SectionType = 'header' | 'body' | 'footer';

export type SectionPartType = 'sectionHeader' | 'sectionBody' | 'sectionFooter';

export interface SelectedArea {
  type: 'pageHeader' | 'pageFooter' | 'section';
  sectionIndex?: number;
  part?: SectionPartType;
}

export type NumberFormatPreset =
  | 'CURRENCY_USD'
  | 'CURRENCY_EUR'
  | 'CURRENCY_GBP'
  | 'PERCENTAGE'
  | 'ACCOUNTING'
  | 'NUMBER_2DP'
  | 'NUMBER_0DP'
  | 'CUSTOM';

export interface TableCellStyle {
  borderTop?: { width: number; color: string };
  borderRight?: { width: number; color: string };
  borderBottom?: { width: number; color: string };
  borderLeft?: { width: number; color: string };
  padding?: number;
}

export interface ConditionalFormat {
  condition: string;
  style: Partial<StyleDef>;
}

export interface TableFooterCell {
  content: string;
  colSpan: number;
}

export interface SectionBand {
  height: number;
  elements: TemplateElement[];
}

export interface Section {
  id: string;
  name: string;
  sectionHeader?: SectionBand;
  elements: TemplateElement[];
  sectionFooter?: SectionBand;
  repeatHeaderOnPageBreak?: boolean;
  repeatFooterOnPageBreak?: boolean;
  dataSource?: string;
}

export interface Template {
  metadata: TemplateMetadata;
  pageLayout: PageLayout;
  header?: HeaderDefinition;
  footer?: FooterDefinition;
  pageNumbering?: PageNumbering;
  body: BodyDefinition;
}

export interface TemplateMetadata {
  id?: string;
  name: string;
  description?: string;
  author?: string;
  tags?: string[];
  createdAt?: string;
  updatedAt?: string;
  version?: number;
}

export interface PageLayout {
  pageSize: 'A4' | 'LETTER' | 'LEGAL' | 'CUSTOM';
  orientation: 'PORTRAIT' | 'LANDSCAPE';
  margins: { top: number; right: number; bottom: number; left: number };
  customWidth?: number;
  customHeight?: number;
}

export interface HeaderDefinition {
  height: number;
  firstPageDifferent: boolean;
  oddEvenDifferent: boolean;
  defaultElements: TemplateElement[];
  firstPageElements?: TemplateElement[];
  oddPageElements?: TemplateElement[];
  evenPageElements?: TemplateElement[];
}

export type FooterDefinition = HeaderDefinition;

export interface PageNumbering {
  enabled: boolean;
  format: 'PAGE_X_OF_Y' | 'PAGE_X' | 'ROMAN' | 'CUSTOM';
  position?: { x: number; y: number };
  startFrom: number;
  style?: StyleDef;
  customFormat?: string;
}

export interface BodyDefinition {
  sections: Section[];
}

export type ElementType = 'TEXT' | 'TABLE' | 'IMAGE' | 'CHART' | 'CONDITIONAL' | 'REPEATING_SECTION';

export interface TemplateElement {
  id: string;
  type: ElementType;
  position?: { x: number; y: number };
  dimension?: { width: number; height: number };
  positioning: 'FLOW' | 'ABSOLUTE';
  style?: StyleDef;
  marginTop?: number;
  marginBottom?: number;
  content?: string;
  dataSource?: string;
  columns?: TableColumn[];
  headerStyle?: StyleDef;
  rowStyle?: StyleDef;
  alternateRowColor?: string;
  borderStyle?: BorderDef;
  showHeader?: boolean;
  footerCells?: TableFooterCell[];
  source?: 'STATIC' | 'DATA_FIELD' | 'URL';
  data?: string;
  fit?: 'CONTAIN' | 'COVER' | 'STRETCH';
  chartType?: 'BAR' | 'PIE' | 'LINE';
  categoryField?: string;
  valueFields?: string[];
  title?: string;
  showLegend?: boolean;
  condition?: string;
  thenElements?: TemplateElement[];
  elseElements?: TemplateElement[];
  elements?: TemplateElement[];
  pageBreakBetween?: boolean;
}

export interface TableColumn {
  header: string;
  field: string;
  width?: number;
  alignment?: 'LEFT' | 'CENTER' | 'RIGHT';
  format?: string;
  formatPreset?: NumberFormatPreset;
  cellStyle?: TableCellStyle;
  conditionalFormats?: ConditionalFormat[];
  wrapText?: boolean;
}

export interface StyleDef {
  fontFamily?: string;
  fontSize?: number;
  bold?: boolean;
  italic?: boolean;
  underline?: boolean;
  textColor?: string;
  backgroundColor?: string;
  alignment?: 'LEFT' | 'CENTER' | 'RIGHT' | 'JUSTIFIED';
  lineHeight?: number;
}

export interface BorderDef {
  width: number;
  color: string;
  style: 'SOLID' | 'DASHED' | 'DOTTED' | 'NONE';
}
