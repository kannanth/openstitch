package com.openstitch.engine.render;

import com.openstitch.engine.model.Element;
import com.openstitch.engine.model.SectionBand;
import com.openstitch.engine.model.SectionDefinition;
import com.openstitch.engine.parser.DataContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Renders sections within the body of a template.
 * Handles data-bound sections (repeating per data item) and
 * section headers/footers with optional repeat-on-page-break.
 */
public class SectionRenderer {

    private final PdfRenderer parentRenderer;

    public SectionRenderer(PdfRenderer parentRenderer) {
        this.parentRenderer = parentRenderer;
    }

    /**
     * Render a section definition. If a dataSource is specified, resolves
     * the data list and renders the section once per item.
     *
     * @param section     the section definition to render
     * @param context     the current render context
     * @param dataContext the data context for expression evaluation
     * @throws IOException if a PDF writing error occurs
     */
    public void renderSection(SectionDefinition section, RenderContext context, DataContext dataContext) throws IOException {
        String ds = section.getDataSource();
        if (ds != null && !ds.isBlank()) {
            List<Map<String, Object>> items = dataContext.resolveList(ds);
            for (Map<String, Object> item : items) {
                DataContext childContext = dataContext.createChildContext(item);
                renderSingleInstance(section, context, childContext);
            }
        } else {
            renderSingleInstance(section, context, dataContext);
        }
    }

    /**
     * Render a single instance of a section: header band, body elements, footer band.
     * Handles page breaks with optional re-rendering of section header/footer.
     */
    private void renderSingleInstance(SectionDefinition section, RenderContext context, DataContext dataContext) throws IOException {
        // Render section header band
        renderBand(section.getSectionHeader(), context, dataContext);

        // Render body elements
        if (section.getElements() != null) {
            for (Element element : section.getElements()) {
                // Check if page break is needed
                if (context.needsPageBreak(element.getMarginTop() + 20)) {
                    // Render section footer before page break if configured
                    if (section.isRepeatFooterOnPageBreak() && section.getSectionFooter() != null) {
                        renderBand(section.getSectionFooter(), context, dataContext);
                    }

                    // Page break
                    parentRenderer.getHeaderFooterRenderer().renderFooter(context, dataContext);
                    parentRenderer.handleNewPage(context, dataContext);

                    // Re-render section header after page break if configured
                    if (section.isRepeatHeaderOnPageBreak() && section.getSectionHeader() != null) {
                        renderBand(section.getSectionHeader(), context, dataContext);
                    }
                }

                parentRenderer.renderSingleElement(element, context, dataContext);
            }
        }

        // Render section footer band
        renderBand(section.getSectionFooter(), context, dataContext);
    }

    /**
     * Render a section band (header or footer) if present.
     */
    private void renderBand(SectionBand band, RenderContext context, DataContext dataContext) throws IOException {
        if (band == null || band.getElements() == null) {
            return;
        }
        for (Element element : band.getElements()) {
            parentRenderer.renderSingleElement(element, context, dataContext);
        }
    }
}
