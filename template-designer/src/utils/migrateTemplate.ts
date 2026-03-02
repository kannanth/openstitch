import { v4 as uuidv4 } from 'uuid';
import type { Template, Section, TemplateElement } from '../types/template';

interface LegacyTemplate {
  body?: {
    elements?: TemplateElement[];
    sections?: Section[];
  };
  subHeader?: { height: number; elements: TemplateElement[] };
  subFooter?: { height: number; elements: TemplateElement[] };
  [key: string]: unknown;
}

/**
 * Migrates a legacy template format (with body.elements, subHeader, subFooter)
 * to the new sections-based format.
 */
export function migrateTemplate(raw: LegacyTemplate): Template {
  const result = { ...raw } as Record<string, unknown>;

  // If body has elements but no sections, wrap them in a single section
  if (raw.body && raw.body.elements && !raw.body.sections) {
    const section: Section = {
      id: uuidv4(),
      name: 'Main',
      elements: raw.body.elements,
    };

    // Migrate subHeader into section's sectionHeader
    if (raw.subHeader) {
      section.sectionHeader = {
        height: raw.subHeader.height,
        elements: raw.subHeader.elements ?? [],
      };
    }

    // Migrate subFooter into section's sectionFooter
    if (raw.subFooter) {
      section.sectionFooter = {
        height: raw.subFooter.height,
        elements: raw.subFooter.elements ?? [],
      };
    }

    result.body = { sections: [section] };
  }

  // Remove legacy fields
  delete result.subHeader;
  delete result.subFooter;

  return result as unknown as Template;
}
