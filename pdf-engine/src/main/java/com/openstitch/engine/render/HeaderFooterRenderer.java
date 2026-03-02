package com.openstitch.engine.render;

import com.openstitch.engine.model.Element;
import com.openstitch.engine.model.FooterDefinition;
import com.openstitch.engine.model.HeaderDefinition;
import com.openstitch.engine.model.Template;
import com.openstitch.engine.parser.DataContext;

import java.io.IOException;
import java.util.List;

/**
 * Renders headers and footers on PDF pages.
 * Called by PdfRenderer after creating each page (for headers)
 * and before closing each page (for footers).
 */
public class HeaderFooterRenderer {

    private final PdfRenderer parentRenderer;

    public HeaderFooterRenderer(PdfRenderer parentRenderer) {
        this.parentRenderer = parentRenderer;
    }

    /**
     * Render header for the current page.
     * Called after each new page is created.
     *
     * @param context     the current render context
     * @param dataContext the data context for expression evaluation
     * @throws IOException if a PDF writing error occurs
     */
    public void renderHeader(RenderContext context, DataContext dataContext) throws IOException {
        Template template = context.getTemplate();

        // Render main header
        HeaderDefinition header = template.getHeader();
        if (header != null) {
            float headerY = context.getPageHeight() - template.getPageLayout().getMargins().top();
            float savedCursorY = context.getCursorY();
            context.setCursorY(headerY);
            context.setPageBreakEnabled(false);

            List<Element> elements = header.getElementsForPage(
                    context.getCurrentPageNumber(), context.getTotalPages());
            if (elements != null) {
                for (Element element : elements) {
                    parentRenderer.renderSingleElement(element, context, dataContext);
                }
            }

            context.setPageBreakEnabled(true);
            context.setCursorY(savedCursorY);
        }
    }

    /**
     * Render footer for the current page.
     * Called before closing each page.
     *
     * @param context     the current render context
     * @param dataContext the data context for expression evaluation
     * @throws IOException if a PDF writing error occurs
     */
    public void renderFooter(RenderContext context, DataContext dataContext) throws IOException {
        Template template = context.getTemplate();

        // Render main footer
        FooterDefinition footer = template.getFooter();
        if (footer != null) {
            float footerY = template.getPageLayout().getMargins().bottom() + footer.getHeight();
            float savedCursorY = context.getCursorY();
            context.setCursorY(footerY);
            context.setPageBreakEnabled(false);

            List<Element> elements = footer.getElementsForPage(
                    context.getCurrentPageNumber(), context.getTotalPages());
            if (elements != null) {
                for (Element element : elements) {
                    parentRenderer.renderSingleElement(element, context, dataContext);
                }
            }

            context.setPageBreakEnabled(true);
            context.setCursorY(savedCursorY);
        }
    }
}
