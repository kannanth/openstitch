package com.openstitch.engine.layout;

import com.openstitch.engine.render.RenderContext;

import java.io.IOException;

/**
 * Handles page break logic during PDF rendering.
 * Provides utility methods to check whether a page break is needed
 * and to perform the break by closing the current content stream
 * and creating a new page.
 */
public class PageBreakHandler {

    private PageBreakHandler() {
        // Utility class; prevent instantiation
    }

    /**
     * Handle a page break: close the current content stream, create a new page,
     * and return the new available height.
     *
     * @param context the render context
     * @return the available height on the new page in points
     * @throws IOException if closing the stream or creating a new page fails
     */
    public static float handlePageBreak(RenderContext context) throws IOException {
        context.closeContentStream();
        context.newPage();
        return context.getAvailableHeight();
    }

    /**
     * Check if a page break is needed for the given height requirement.
     * If needed, perform the page break and return true.
     *
     * @param context        the render context
     * @param requiredHeight the height in points that needs to fit on the current page
     * @return true if a page break was performed, false otherwise
     * @throws IOException if the page break operation fails
     */
    public static boolean breakIfNeeded(RenderContext context, float requiredHeight) throws IOException {
        if (context.needsPageBreak(requiredHeight)) {
            handlePageBreak(context);
            return true;
        }
        return false;
    }
}
