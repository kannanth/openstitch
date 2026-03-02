package com.openstitch.engine.render;

import com.openstitch.engine.expression.ExpressionEvaluator;
import com.openstitch.engine.model.Orientation;
import com.openstitch.engine.model.PageLayout;
import com.openstitch.engine.model.PageSize;
import com.openstitch.engine.model.Template;
import com.openstitch.engine.parser.DataContext;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;

/**
 * Maintains the rendering state during PDF generation.
 * Tracks the current page, cursor position, page dimensions, and content boundaries.
 */
public class RenderContext {

    private final PDDocument document;
    private final Template template;
    private final ExpressionEvaluator expressionEvaluator;

    private PDPage currentPage;
    private PDPageContentStream contentStream;

    private float cursorX;        // current X position
    private float cursorY;        // current Y position (top of page = pageHeight, decreases downward)
    private float pageWidth;
    private float pageHeight;
    private float contentStartY;  // top of content area (after top margin)
    private float contentEndY;    // bottom of content area (before bottom margin)
    private float contentWidth;   // page width minus left and right margins
    private float leftMargin;

    private int currentPageNumber;
    private int totalPages;       // set after first pass if multi-pass rendering is used
    private boolean isFirstPass;
    private boolean pageBreakEnabled = true;
    private DataContext dataContext;
    private PageBreakHandler pageBreakHandler;

    /**
     * Constructor that initializes rendering dimensions from the template's PageLayout.
     *
     * @param document            the PDDocument being built
     * @param template            the template definition
     * @param expressionEvaluator the evaluator for resolving expressions
     */
    public RenderContext(PDDocument document, Template template, ExpressionEvaluator expressionEvaluator) {
        this.document = document;
        this.template = template;
        this.expressionEvaluator = expressionEvaluator;
        this.currentPageNumber = 0;
        this.isFirstPass = true;

        PageLayout layout = template.getPageLayout();

        // Calculate effective page dimensions based on page size and orientation
        PDRectangle pageRect = getPageRectangle(layout);
        this.pageWidth = pageRect.getWidth();
        this.pageHeight = pageRect.getHeight();
        this.leftMargin = layout.getMargins().left();
        this.contentWidth = pageWidth - layout.getMargins().left() - layout.getMargins().right();

        // Content area boundaries derived from template header/footer definitions
        float headerHeight = template.getHeader() != null ? template.getHeader().getHeight() : 0;
        float footerHeight = template.getFooter() != null ? template.getFooter().getHeight() : 0;

        this.contentStartY = pageHeight - layout.getMargins().top() - headerHeight;
        this.contentEndY = layout.getMargins().bottom() + footerHeight;
    }

    /**
     * Create a new page and set up a fresh content stream.
     * Closes any existing content stream before creating the new page.
     *
     * @throws IOException if closing the old stream or creating the new one fails
     */
    public void newPage() throws IOException {
        closeContentStream();

        currentPageNumber++;

        PDRectangle pageRect = getPageRectangle(template.getPageLayout());
        currentPage = new PDPage(pageRect);
        document.addPage(currentPage);

        contentStream = new PDPageContentStream(document, currentPage);

        // Reset cursor to top-left of content area
        cursorX = leftMargin;
        cursorY = contentStartY;

        // Auto-inject pageNumber into data context for expression resolution
        if (dataContext != null) {
            dataContext.setSystemVariable("pageNumber", currentPageNumber);
        }
    }

    /**
     * Close the current content stream if one is open.
     *
     * @throws IOException if closing the stream fails
     */
    public void closeContentStream() throws IOException {
        if (contentStream != null) {
            contentStream.close();
            contentStream = null;
        }
    }

    /**
     * Check if a page break is needed to fit the required height.
     *
     * @param requiredHeight the height in points that needs to fit
     * @return true if the remaining space on the current page is insufficient
     */
    public boolean needsPageBreak(float requiredHeight) {
        if (!pageBreakEnabled) return false;
        return (cursorY - requiredHeight) < contentEndY;
    }

    public void setPageBreakEnabled(boolean enabled) {
        this.pageBreakEnabled = enabled;
    }

    /**
     * Move the cursor Y position by the given delta.
     * Negative values move the cursor downward (toward the bottom of the page).
     *
     * @param delta the amount to move (negative = down, positive = up)
     */
    public void moveCursorY(float delta) {
        this.cursorY += delta;
    }

    // --- Getters and Setters ---

    public PDDocument getDocument() {
        return document;
    }

    public Template getTemplate() {
        return template;
    }

    public ExpressionEvaluator getExpressionEvaluator() {
        return expressionEvaluator;
    }

    public PDPage getCurrentPage() {
        return currentPage;
    }

    public PDPageContentStream getContentStream() {
        return contentStream;
    }

    public void setContentStream(PDPageContentStream contentStream) {
        this.contentStream = contentStream;
    }

    public float getCursorX() {
        return cursorX;
    }

    public void setCursorX(float cursorX) {
        this.cursorX = cursorX;
    }

    public float getCursorY() {
        return cursorY;
    }

    public void setCursorY(float cursorY) {
        this.cursorY = cursorY;
    }

    public float getPageWidth() {
        return pageWidth;
    }

    public float getPageHeight() {
        return pageHeight;
    }

    public float getContentStartY() {
        return contentStartY;
    }

    public float getContentEndY() {
        return contentEndY;
    }

    public float getContentWidth() {
        return contentWidth;
    }

    public float getLeftMargin() {
        return leftMargin;
    }

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirstPass() {
        return isFirstPass;
    }

    public void setFirstPass(boolean firstPass) {
        isFirstPass = firstPass;
    }

    /**
     * Returns the available vertical space remaining on the current page.
     *
     * @return the available height in points
     */
    public float getAvailableHeight() {
        return cursorY - contentEndY;
    }

    public DataContext getDataContext() {
        return dataContext;
    }

    public void setDataContext(DataContext dataContext) {
        this.dataContext = dataContext;
    }

    public void setPageBreakHandler(PageBreakHandler handler) {
        this.pageBreakHandler = handler;
    }

    /**
     * Triggers a page break through the registered handler, which ensures
     * footer/header rendering around the page break. Falls back to a plain
     * newPage() if no handler is registered.
     *
     * @throws IOException if page break handling fails
     */
    public void triggerPageBreak() throws IOException {
        if (pageBreakHandler != null) {
            pageBreakHandler.handlePageBreak(this);
        } else {
            closeContentStream();
            newPage();
        }
    }

    /**
     * Functional interface for handling page breaks with footer/header rendering.
     */
    @FunctionalInterface
    public interface PageBreakHandler {
        void handlePageBreak(RenderContext ctx) throws IOException;
    }

    // --- Private helpers ---

    /**
     * Maps the template's PageLayout to a PDRectangle, handling page size and orientation.
     * For LANDSCAPE orientation, width and height are swapped.
     *
     * @param layout the page layout configuration
     * @return the PDRectangle representing the page dimensions
     */
    private PDRectangle getPageRectangle(PageLayout layout) {
        float width;
        float height;

        if (layout.getPageSize() == PageSize.CUSTOM) {
            width = layout.getCustomWidth() != null ? layout.getCustomWidth() : 0;
            height = layout.getCustomHeight() != null ? layout.getCustomHeight() : 0;
        } else {
            PDRectangle base = mapPageSize(layout.getPageSize());
            width = base.getWidth();
            height = base.getHeight();
        }

        // Swap dimensions for landscape orientation
        if (layout.getOrientation() == Orientation.LANDSCAPE) {
            float temp = width;
            width = height;
            height = temp;
        }

        return new PDRectangle(width, height);
    }

    /**
     * Maps a PageSize enum to the corresponding PDRectangle constant.
     *
     * @param pageSize the page size enum value
     * @return the corresponding PDRectangle
     */
    private PDRectangle mapPageSize(PageSize pageSize) {
        return switch (pageSize) {
            case A4 -> PDRectangle.A4;
            case LETTER -> PDRectangle.LETTER;
            case LEGAL -> PDRectangle.LEGAL;
            case CUSTOM -> new PDRectangle(0, 0);
        };
    }

}
