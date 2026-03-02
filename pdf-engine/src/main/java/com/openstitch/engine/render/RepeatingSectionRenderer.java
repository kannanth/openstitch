package com.openstitch.engine.render;

import com.openstitch.engine.model.Element;
import com.openstitch.engine.model.RepeatingSection;
import com.openstitch.engine.parser.DataContext;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Renders RepeatingSection elements by iterating over a resolved data list,
 * creating a child DataContext for each item, and rendering the child elements
 * within each iteration. Supports page breaks between items and optional separators.
 */
public class RepeatingSectionRenderer implements ElementRenderer<RepeatingSection> {

    private final PdfRenderer parentRenderer;

    /**
     * Creates a RepeatingSectionRenderer with a reference to the parent PdfRenderer
     * for delegating child element rendering.
     *
     * @param parentRenderer the PdfRenderer that orchestrates all element rendering
     */
    public RepeatingSectionRenderer(PdfRenderer parentRenderer) {
        this.parentRenderer = parentRenderer;
    }

    @Override
    public float render(RepeatingSection element, RenderContext context, DataContext dataContext) throws IOException {
        // Resolve the data list from the data source
        List<Map<String, Object>> dataList = dataContext.resolveList(element.getDataSource());

        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        List<Element> childElements = element.getElements();
        if (childElements == null || childElements.isEmpty()) {
            return 0;
        }

        float totalHeight = 0;

        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> itemData = dataList.get(i);
            boolean isLastItem = (i == dataList.size() - 1);

            // Create a child data context for this iteration item
            DataContext childContext = dataContext.createChildContext(itemData);

            // Render each child element in the section
            for (Element child : childElements) {
                float childHeight = parentRenderer.renderSingleElement(child, context, childContext);
                totalHeight += childHeight;
            }

            // Handle inter-item separator or page break (not after the last item)
            if (!isLastItem) {
                if (element.isPageBreakBetween()) {
                    // Force a new page between items
                    context.closeContentStream();
                    context.newPage();
                } else if (element.getSeparator() != null) {
                    // Draw a horizontal separator line
                    float separatorHeight = drawSeparator(element.getSeparator(), context);
                    totalHeight += separatorHeight;
                }
            }
        }

        return totalHeight;
    }

    /**
     * Draws a horizontal separator line at the current cursor position.
     *
     * @param separator the separator configuration (height and color)
     * @param context   the current render context
     * @return the height consumed by the separator
     * @throws IOException if a PDF writing error occurs
     */
    private float drawSeparator(RepeatingSection.Separator separator, RenderContext context) throws IOException {
        float lineHeight = separator.height() > 0 ? separator.height() : 1f;
        float totalSeparatorHeight = lineHeight + 4f; // 2pt padding above and below

        // Check for page break before drawing the separator
        if (context.needsPageBreak(totalSeparatorHeight)) {
            context.closeContentStream();
            context.newPage();
        }

        // Move down 2pt padding above the line
        context.moveCursorY(-2f);

        PDPageContentStream contentStream = context.getContentStream();

        // Parse separator color
        float[] color = parseHexColor(separator.color());
        contentStream.setStrokingColor(color[0], color[1], color[2]);
        contentStream.setLineWidth(lineHeight);

        // Draw a horizontal line across the content width
        float x1 = context.getLeftMargin();
        float x2 = context.getLeftMargin() + context.getContentWidth();
        float y = context.getCursorY() - (lineHeight / 2f);

        contentStream.moveTo(x1, y);
        contentStream.lineTo(x2, y);
        contentStream.stroke();

        // Move cursor down past the line and the bottom padding
        context.moveCursorY(-(lineHeight + 2f));

        return totalSeparatorHeight;
    }

    /**
     * Parses a hex color string (e.g., "#CCCCCC" or "CCCCCC") into RGB float components [0..1].
     *
     * @param hex the hex color string
     * @return a float array of [r, g, b] each in the range 0.0 to 1.0
     */
    private float[] parseHexColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new float[]{0.8f, 0.8f, 0.8f}; // default to light gray for separators
        }

        String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;

        if (cleaned.length() != 6) {
            return new float[]{0.8f, 0.8f, 0.8f}; // default to light gray for invalid input
        }

        try {
            int r = Integer.parseInt(cleaned.substring(0, 2), 16);
            int g = Integer.parseInt(cleaned.substring(2, 4), 16);
            int b = Integer.parseInt(cleaned.substring(4, 6), 16);
            return new float[]{r / 255f, g / 255f, b / 255f};
        } catch (NumberFormatException e) {
            return new float[]{0.8f, 0.8f, 0.8f}; // default to light gray for parse errors
        }
    }
}
