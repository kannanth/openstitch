package com.openstitch.engine.layout;

import com.openstitch.engine.model.PageLayout;
import com.openstitch.engine.model.Template;

/**
 * A utility that helps with page-level layout calculations.
 * Computes the usable content area dimensions after accounting for
 * margins, headers, and footers.
 */
public class LayoutEngine {

    private LayoutEngine() {
        // Utility class; prevent instantiation
    }

    /**
     * Calculate the content area height for a given template.
     * Content area is the page height minus margins, header, and footer.
     *
     * @param template the template definition
     * @return the available content height in points
     */
    public static float getContentHeight(Template template) {
        PageLayout layout = template.getPageLayout();
        float pageHeight = layout.getEffectiveHeight();

        float usedHeight = layout.getMargins().top() + layout.getMargins().bottom();

        if (template.getHeader() != null) {
            usedHeight += template.getHeader().getHeight();
        }
        if (template.getFooter() != null) {
            usedHeight += template.getFooter().getHeight();
        }

        return pageHeight - usedHeight;
    }

    /**
     * Calculate the content area width for a given template.
     * Content area width is the page width minus left and right margins.
     *
     * @param template the template definition
     * @return the available content width in points
     */
    public static float getContentWidth(Template template) {
        PageLayout layout = template.getPageLayout();
        return layout.getEffectiveWidth() - layout.getMargins().left() - layout.getMargins().right();
    }
}
