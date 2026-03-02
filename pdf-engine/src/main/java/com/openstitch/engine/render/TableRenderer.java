package com.openstitch.engine.render;

import com.openstitch.engine.expression.ExpressionEvaluator;
import com.openstitch.engine.model.Border;
import com.openstitch.engine.model.ConditionalFormat;
import com.openstitch.engine.model.Style;
import com.openstitch.engine.model.TableColumn;
import com.openstitch.engine.model.TableElement;
import com.openstitch.engine.model.TableFooterCell;
import com.openstitch.engine.parser.DataContext;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renders TableElement instances into the PDF.
 * Supports data-bound tables with configurable columns, headers, footers,
 * borders, alternating row colors, automatic page break handling
 * with header repetition on new pages, dynamic row heights via word wrap,
 * and conditional formatting.
 */
public class TableRenderer implements ElementRenderer<TableElement> {

    private static final float DEFAULT_CELL_PADDING = 4f;

    @Override
    public float render(TableElement element, RenderContext context, DataContext dataContext) throws IOException {
        List<TableColumn> columns = element.getColumns();
        if (columns == null || columns.isEmpty()) {
            return 0;
        }

        // Resolve the data rows from the data source
        List<Map<String, Object>> rows = dataContext.resolveList(element.getDataSource());

        // Calculate column widths
        float contentWidth = context.getContentWidth();
        float[] columnWidths = calculateColumnWidths(columns, contentWidth);

        // Determine styles
        Style headerStyle = element.getHeaderStyle() != null ? element.getHeaderStyle() : new Style();
        Style rowStyle = element.getRowStyle() != null ? element.getRowStyle() : new Style();
        Border borderStyle = element.getBorderStyle() != null ? element.getBorderStyle() : new Border();

        // Calculate row height based on font size + padding
        float headerFontSize = headerStyle.fontSize() > 0 ? headerStyle.fontSize() : 12f;
        float rowFontSize = rowStyle.fontSize() > 0 ? rowStyle.fontSize() : 12f;
        float headerRowHeight = headerFontSize + DEFAULT_CELL_PADDING * 2;

        // Apply margin top
        context.moveCursorY(-element.getMarginTop());

        float totalHeightConsumed = 0;
        float startX = context.getLeftMargin();

        // Resolve fonts for data rows (needed for dynamic height calculation)
        PDType1Font rowFont = resolveFont(rowStyle.fontFamily(), rowStyle.bold(), rowStyle.italic());

        // Draw the header row if requested
        if (element.isShowHeader()) {
            if (context.needsPageBreak(headerRowHeight)) {
                context.closeContentStream();
                context.newPage();
            }
            drawHeaderRow(context, columns, columnWidths, headerStyle, borderStyle, startX, headerRowHeight);
            context.moveCursorY(-headerRowHeight);
            totalHeightConsumed += headerRowHeight;
        }

        // Draw data rows
        ExpressionEvaluator evaluator = context.getExpressionEvaluator();

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Map<String, Object> rowData = rows.get(rowIndex);
            DataContext childContext = dataContext.createChildContext(rowData);

            // Calculate dynamic row height for this row
            float dataRowHeight = calculateDynamicRowHeight(columns, columnWidths, rowFont, rowFontSize, childContext, evaluator);

            // Check for page break before each data row
            if (context.needsPageBreak(dataRowHeight)) {
                context.closeContentStream();
                context.newPage();

                // Repeat header on new page
                if (element.isShowHeader()) {
                    drawHeaderRow(context, columns, columnWidths, headerStyle, borderStyle, startX, headerRowHeight);
                    context.moveCursorY(-headerRowHeight);
                    totalHeightConsumed += headerRowHeight;
                }
            }

            // Determine background color for alternating rows
            String bgColor = null;
            if (rowIndex % 2 == 1 && element.getAlternateRowColor() != null) {
                bgColor = element.getAlternateRowColor();
            }

            drawDataRow(context, columns, columnWidths, rowStyle, borderStyle,
                    startX, dataRowHeight, childContext, bgColor, evaluator);
            context.moveCursorY(-dataRowHeight);
            totalHeightConsumed += dataRowHeight;
        }

        // Draw footer row if defined
        if (element.getFooterCells() != null && !element.getFooterCells().isEmpty()) {
            float footerRowHeight = rowFontSize + DEFAULT_CELL_PADDING * 2;

            if (context.needsPageBreak(footerRowHeight)) {
                context.closeContentStream();
                context.newPage();
            }

            drawFooterRow(context, element.getFooterCells(), columns, columnWidths,
                    headerStyle, borderStyle, startX, footerRowHeight, dataContext, evaluator);
            context.moveCursorY(-footerRowHeight);
            totalHeightConsumed += footerRowHeight;
        }

        return totalHeightConsumed;
    }

    // -------------------------------------------------------------------------
    // Dynamic row height calculation
    // -------------------------------------------------------------------------

    /**
     * Calculates the dynamic row height for a data row by checking each column
     * that has wrapText enabled. For wrap-enabled columns, the text is word-wrapped
     * and the required height is computed. The maximum height across all columns
     * is used as the row height.
     *
     * @param columns      the column definitions
     * @param widths       the calculated column widths
     * @param font         the font for data rows
     * @param fontSize     the font size for data rows
     * @param childContext the data context for resolving field values
     * @param evaluator    the expression evaluator
     * @return the dynamic row height in points
     */
    private float calculateDynamicRowHeight(List<TableColumn> columns, float[] widths,
                                            PDType1Font font, float fontSize,
                                            DataContext childContext,
                                            ExpressionEvaluator evaluator) throws IOException {
        float minHeight = fontSize + DEFAULT_CELL_PADDING * 2;
        float maxHeight = minHeight;

        for (int i = 0; i < columns.size(); i++) {
            TableColumn col = columns.get(i);
            if (!col.isWrapText()) {
                continue;
            }

            Object value = childContext.resolve(col.field());
            String cellText = formatValue(value, col.format());
            if (cellText == null || cellText.isEmpty()) {
                continue;
            }

            float cellWidth = widths[i] - DEFAULT_CELL_PADDING * 2;
            List<String> lines = wrapCellText(cellText, font, fontSize, cellWidth);
            float lineHeight = fontSize * 1.2f;
            float requiredHeight = lines.size() * lineHeight + DEFAULT_CELL_PADDING * 2;

            if (requiredHeight > maxHeight) {
                maxHeight = requiredHeight;
            }
        }

        return maxHeight;
    }

    /**
     * Word-wraps text to fit within the given cell width.
     * Mirrors the logic from TextRenderer.wrapText.
     *
     * @param text      the text to wrap
     * @param font      the font for measurement
     * @param fontSize  the font size in points
     * @param cellWidth the available width inside the cell (minus padding)
     * @return a list of wrapped lines
     */
    private List<String> wrapCellText(String text, PDType1Font font, float fontSize, float cellWidth)
            throws IOException {
        List<String> lines = new ArrayList<>();

        String[] paragraphs = text.split("\\n");

        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }

            String[] words = paragraph.split("\\s+");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if (word.isEmpty()) {
                    continue;
                }

                float wordWidth = font.getStringWidth(word) / 1000f * fontSize;

                if (currentLine.isEmpty()) {
                    if (wordWidth > cellWidth) {
                        lines.add(word);
                    } else {
                        currentLine.append(word);
                    }
                } else {
                    String candidate = currentLine + " " + word;
                    float candidateWidth = font.getStringWidth(candidate) / 1000f * fontSize;

                    if (candidateWidth <= cellWidth) {
                        currentLine.append(" ").append(word);
                    } else {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    }
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }
        }

        if (lines.isEmpty()) {
            lines.add("");
        }

        return lines;
    }

    // -------------------------------------------------------------------------
    // Column width calculation
    // -------------------------------------------------------------------------

    /**
     * Calculates the absolute width for each column in points.
     * Columns with an explicit width use that value directly.
     * Columns without a width share the remaining space equally.
     *
     * @param columns      the column definitions
     * @param contentWidth the total available content width
     * @return an array of widths, one per column
     */
    private float[] calculateColumnWidths(List<TableColumn> columns, float contentWidth) {
        float[] widths = new float[columns.size()];
        float usedWidth = 0;
        int autoCount = 0;

        for (int i = 0; i < columns.size(); i++) {
            Float specified = columns.get(i).width();
            if (specified != null) {
                widths[i] = specified;
                usedWidth += specified;
            } else {
                autoCount++;
            }
        }

        float autoWidth = autoCount > 0 ? (contentWidth - usedWidth) / autoCount : 0;

        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).width() == null) {
                widths[i] = autoWidth;
            }
        }

        return widths;
    }

    // -------------------------------------------------------------------------
    // Header row
    // -------------------------------------------------------------------------

    /**
     * Draws the header row with background, text, and borders.
     */
    private void drawHeaderRow(RenderContext context, List<TableColumn> columns,
                               float[] columnWidths, Style headerStyle, Border borderStyle,
                               float startX, float rowHeight) throws IOException {
        PDPageContentStream cs = context.getContentStream();
        float cursorY = context.getCursorY();
        float cellX = startX;

        PDType1Font font = resolveFont(headerStyle.fontFamily(), headerStyle.bold(), headerStyle.italic());
        float fontSize = headerStyle.fontSize() > 0 ? headerStyle.fontSize() : 12f;
        float[] textColor = parseHexColor(headerStyle.textColor());

        // Draw background for the entire header row
        if (headerStyle.backgroundColor() != null) {
            float[] bgColor = parseHexColor(headerStyle.backgroundColor());
            cs.setNonStrokingColor(bgColor[0], bgColor[1], bgColor[2]);
            cs.addRect(startX, cursorY - rowHeight, totalWidth(columnWidths), rowHeight);
            cs.fill();
        }

        // Draw each header cell
        for (int i = 0; i < columns.size(); i++) {
            String headerText = columns.get(i).header() != null ? columns.get(i).header() : "";
            Style.Alignment alignment = columns.get(i).alignment() != null
                    ? columns.get(i).alignment() : Style.Alignment.CENTER;

            drawCell(cs, headerText, cellX, cursorY, columnWidths[i], rowHeight,
                    font, fontSize, textColor, alignment);

            cellX += columnWidths[i];
        }

        // Draw borders for header row
        cellX = startX;
        for (int i = 0; i < columns.size(); i++) {
            drawBorders(cs, cellX, cursorY, columnWidths[i], rowHeight, borderStyle);
            cellX += columnWidths[i];
        }
    }

    // -------------------------------------------------------------------------
    // Data rows
    // -------------------------------------------------------------------------

    /**
     * Draws a single data row with optional background color, text, and borders.
     * Supports per-column word wrapping and conditional formatting.
     */
    private void drawDataRow(RenderContext context, List<TableColumn> columns,
                             float[] columnWidths, Style rowStyle, Border borderStyle,
                             float startX, float rowHeight, DataContext childContext,
                             String bgColor, ExpressionEvaluator evaluator) throws IOException {
        PDPageContentStream cs = context.getContentStream();
        float cursorY = context.getCursorY();
        float cellX = startX;

        PDType1Font font = resolveFont(rowStyle.fontFamily(), rowStyle.bold(), rowStyle.italic());
        float fontSize = rowStyle.fontSize() > 0 ? rowStyle.fontSize() : 12f;
        float[] defaultTextColor = parseHexColor(rowStyle.textColor());

        // Draw alternating row background
        if (bgColor != null) {
            float[] bg = parseHexColor(bgColor);
            cs.setNonStrokingColor(bg[0], bg[1], bg[2]);
            cs.addRect(startX, cursorY - rowHeight, totalWidth(columnWidths), rowHeight);
            cs.fill();
        }

        // Draw each data cell
        for (int i = 0; i < columns.size(); i++) {
            TableColumn col = columns.get(i);
            String fieldPath = col.field();
            Object value = childContext.resolve(fieldPath);

            String cellText = formatValue(value, col.format());

            Style.Alignment alignment = col.alignment() != null ? col.alignment() : Style.Alignment.LEFT;

            // Determine text color (conditional formatting may override)
            float[] cellTextColor = defaultTextColor;
            if (col.conditionalFormats() != null) {
                for (ConditionalFormat cf : col.conditionalFormats()) {
                    if (cf.condition() != null && evaluator.evaluateCondition(cf.condition(), childContext)) {
                        if (cf.style() != null && cf.style().textColor() != null) {
                            cellTextColor = parseHexColor(cf.style().textColor());
                        }
                        break; // first match wins
                    }
                }
            }

            if (col.isWrapText() && cellText != null && !cellText.isEmpty()) {
                float cellWidth = columnWidths[i] - DEFAULT_CELL_PADDING * 2;
                List<String> lines = wrapCellText(cellText, font, fontSize, cellWidth);
                drawCellMultiLine(cs, lines, cellX, cursorY, columnWidths[i], rowHeight,
                        font, fontSize, cellTextColor, alignment);
            } else {
                drawCell(cs, cellText, cellX, cursorY, columnWidths[i], rowHeight,
                        font, fontSize, cellTextColor, alignment);
            }

            cellX += columnWidths[i];
        }

        // Draw borders for data row
        cellX = startX;
        for (int i = 0; i < columns.size(); i++) {
            drawBorders(cs, cellX, cursorY, columnWidths[i], rowHeight, borderStyle);
            cellX += columnWidths[i];
        }
    }

    // -------------------------------------------------------------------------
    // Footer row
    // -------------------------------------------------------------------------

    /**
     * Draws the footer row. Footer cells may span multiple columns and contain
     * expressions that are evaluated against the data context.
     */
    private void drawFooterRow(RenderContext context, List<TableFooterCell> footerCells,
                               List<TableColumn> columns, float[] columnWidths,
                               Style footerStyle, Border borderStyle, float startX,
                               float rowHeight, DataContext dataContext,
                               ExpressionEvaluator evaluator) throws IOException {
        PDPageContentStream cs = context.getContentStream();
        float cursorY = context.getCursorY();
        float cellX = startX;

        PDType1Font font = resolveFont(footerStyle.fontFamily(), footerStyle.bold(), footerStyle.italic());
        float fontSize = footerStyle.fontSize() > 0 ? footerStyle.fontSize() : 12f;
        float[] textColor = parseHexColor(footerStyle.textColor());

        // Draw background for the entire footer row
        if (footerStyle.backgroundColor() != null) {
            float[] bgColor = parseHexColor(footerStyle.backgroundColor());
            cs.setNonStrokingColor(bgColor[0], bgColor[1], bgColor[2]);
            cs.addRect(startX, cursorY - rowHeight, totalWidth(columnWidths), rowHeight);
            cs.fill();
        }

        int colIndex = 0;
        for (TableFooterCell footerCell : footerCells) {
            int span = Math.max(1, footerCell.colSpan());

            // Calculate the total width for this spanned cell
            float cellWidth = 0;
            for (int s = 0; s < span && (colIndex + s) < columnWidths.length; s++) {
                cellWidth += columnWidths[colIndex + s];
            }

            // Evaluate expressions in footer content
            String content = evaluator.evaluate(footerCell.content(), dataContext);

            drawCell(cs, content, cellX, cursorY, cellWidth, rowHeight,
                    font, fontSize, textColor, Style.Alignment.RIGHT);

            // Draw border around the spanned cell
            drawBorders(cs, cellX, cursorY, cellWidth, rowHeight, borderStyle);

            cellX += cellWidth;
            colIndex += span;
        }
    }

    // -------------------------------------------------------------------------
    // Cell rendering
    // -------------------------------------------------------------------------

    /**
     * Draws text within a cell with padding and alignment (single line).
     */
    private void drawCell(PDPageContentStream cs, String text, float cellX, float cellTopY,
                          float cellWidth, float cellHeight, PDType1Font font, float fontSize,
                          float[] textColor, Style.Alignment alignment) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }

        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        float innerWidth = cellWidth - DEFAULT_CELL_PADDING * 2;

        // Calculate X position within the cell based on alignment
        float textX;
        switch (alignment) {
            case CENTER:
                textX = cellX + DEFAULT_CELL_PADDING + (innerWidth - textWidth) / 2f;
                break;
            case RIGHT:
                textX = cellX + DEFAULT_CELL_PADDING + innerWidth - textWidth;
                break;
            case LEFT:
            case JUSTIFIED:
            default:
                textX = cellX + DEFAULT_CELL_PADDING;
                break;
        }

        // Vertically center the text within the cell
        float textY = cellTopY - cellHeight / 2f - fontSize / 3f;

        cs.beginText();
        cs.setFont(font, fontSize);
        cs.setNonStrokingColor(textColor[0], textColor[1], textColor[2]);
        cs.newLineAtOffset(textX, textY);
        cs.showText(text);
        cs.endText();
    }

    /**
     * Renders multiple text lines within cell bounds, top-aligned.
     *
     * @param cs         the content stream
     * @param lines      the wrapped text lines
     * @param cellX      the left X of the cell
     * @param cellTopY   the top Y of the cell
     * @param cellWidth  the width of the cell
     * @param cellHeight the height of the cell
     * @param font       the font to use
     * @param fontSize   the font size
     * @param textColor  the RGB text color (0-1 range)
     * @param alignment  the horizontal alignment
     */
    private void drawCellMultiLine(PDPageContentStream cs, List<String> lines,
                                   float cellX, float cellTopY, float cellWidth, float cellHeight,
                                   PDType1Font font, float fontSize, float[] textColor,
                                   Style.Alignment alignment) throws IOException {
        float innerWidth = cellWidth - DEFAULT_CELL_PADDING * 2;
        float lineHeight = fontSize * 1.2f;
        float startY = cellTopY - DEFAULT_CELL_PADDING - fontSize;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.isEmpty()) {
                continue;
            }

            float lineWidth = font.getStringWidth(line) / 1000f * fontSize;

            float textX;
            switch (alignment) {
                case CENTER:
                    textX = cellX + DEFAULT_CELL_PADDING + (innerWidth - lineWidth) / 2f;
                    break;
                case RIGHT:
                    textX = cellX + DEFAULT_CELL_PADDING + innerWidth - lineWidth;
                    break;
                case LEFT:
                case JUSTIFIED:
                default:
                    textX = cellX + DEFAULT_CELL_PADDING;
                    break;
            }

            float textY = startY - i * lineHeight;

            cs.beginText();
            cs.setFont(font, fontSize);
            cs.setNonStrokingColor(textColor[0], textColor[1], textColor[2]);
            cs.newLineAtOffset(textX, textY);
            cs.showText(line);
            cs.endText();
        }
    }

    // -------------------------------------------------------------------------
    // Border rendering
    // -------------------------------------------------------------------------

    /**
     * Draws the borders around a single cell according to the border style.
     * Supports SOLID, DASHED, DOTTED, and NONE border styles.
     */
    private void drawBorders(PDPageContentStream cs, float cellX, float cellTopY,
                             float cellWidth, float cellHeight, Border border) throws IOException {
        Border.BorderStyle borderStyle = border.style() != null ? border.style() : Border.BorderStyle.SOLID;
        if (borderStyle == Border.BorderStyle.NONE) {
            return;
        }

        float[] borderColor = parseHexColor(border.color());
        cs.setStrokingColor(borderColor[0], borderColor[1], borderColor[2]);
        cs.setLineWidth(border.width());

        // Set dash pattern based on border style
        switch (borderStyle) {
            case DASHED:
                cs.setLineDashPattern(new float[]{6, 3}, 0);
                break;
            case DOTTED:
                cs.setLineDashPattern(new float[]{1, 2}, 0);
                break;
            case SOLID:
            default:
                cs.setLineDashPattern(new float[]{}, 0);
                break;
        }

        float bottomY = cellTopY - cellHeight;

        // Top border
        cs.moveTo(cellX, cellTopY);
        cs.lineTo(cellX + cellWidth, cellTopY);
        cs.stroke();

        // Bottom border
        cs.moveTo(cellX, bottomY);
        cs.lineTo(cellX + cellWidth, bottomY);
        cs.stroke();

        // Left border
        cs.moveTo(cellX, cellTopY);
        cs.lineTo(cellX, bottomY);
        cs.stroke();

        // Right border
        cs.moveTo(cellX + cellWidth, cellTopY);
        cs.lineTo(cellX + cellWidth, bottomY);
        cs.stroke();

        // Reset dash pattern to solid for subsequent drawing operations
        if (borderStyle != Border.BorderStyle.SOLID) {
            cs.setLineDashPattern(new float[]{}, 0);
        }
    }

    // -------------------------------------------------------------------------
    // Value formatting
    // -------------------------------------------------------------------------

    /**
     * Formats a resolved field value using the optional format string.
     * For numeric values, the format string is interpreted as a DecimalFormat pattern.
     */
    private String formatValue(Object value, String format) {
        if (value == null) {
            return "";
        }

        if (format != null && !format.isEmpty()) {
            if (value instanceof Number) {
                try {
                    DecimalFormat df = new DecimalFormat(format);
                    return df.format(value);
                } catch (IllegalArgumentException e) {
                    // If the format is invalid, fall through to toString
                }
            }
            // For non-numeric values with a format string, use String.format if applicable
            try {
                return String.format(format, value);
            } catch (Exception e) {
                // Fall through to toString
            }
        }

        return value.toString();
    }

    // -------------------------------------------------------------------------
    // Font resolution
    // -------------------------------------------------------------------------

    /**
     * Resolves a PDType1Font from the font family name, bold, and italic flags.
     * Maps common family names to PDFBox Standard 14 fonts.
     */
    private PDType1Font resolveFont(String family, boolean bold, boolean italic) {
        String normalizedFamily = (family != null) ? family.toLowerCase().trim() : "helvetica";

        Standard14Fonts.FontName fontName = switch (normalizedFamily) {
            case "times", "times-roman", "times new roman", "serif" -> {
                if (bold && italic) {
                    yield Standard14Fonts.FontName.TIMES_BOLD_ITALIC;
                } else if (bold) {
                    yield Standard14Fonts.FontName.TIMES_BOLD;
                } else if (italic) {
                    yield Standard14Fonts.FontName.TIMES_ITALIC;
                } else {
                    yield Standard14Fonts.FontName.TIMES_ROMAN;
                }
            }
            case "courier", "monospace", "mono" -> {
                if (bold && italic) {
                    yield Standard14Fonts.FontName.COURIER_BOLD_OBLIQUE;
                } else if (bold) {
                    yield Standard14Fonts.FontName.COURIER_BOLD;
                } else if (italic) {
                    yield Standard14Fonts.FontName.COURIER_OBLIQUE;
                } else {
                    yield Standard14Fonts.FontName.COURIER;
                }
            }
            default -> {
                if (bold && italic) {
                    yield Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE;
                } else if (bold) {
                    yield Standard14Fonts.FontName.HELVETICA_BOLD;
                } else if (italic) {
                    yield Standard14Fonts.FontName.HELVETICA_OBLIQUE;
                } else {
                    yield Standard14Fonts.FontName.HELVETICA;
                }
            }
        };

        return new PDType1Font(fontName);
    }

    // -------------------------------------------------------------------------
    // Color parsing
    // -------------------------------------------------------------------------

    /**
     * Parses a hex color string (e.g., "#FF0000" or "FF0000") into RGB float components [0..1].
     */
    private float[] parseHexColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new float[]{0f, 0f, 0f};
        }

        String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;

        if (cleaned.length() != 6) {
            return new float[]{0f, 0f, 0f};
        }

        try {
            int r = Integer.parseInt(cleaned.substring(0, 2), 16);
            int g = Integer.parseInt(cleaned.substring(2, 4), 16);
            int b = Integer.parseInt(cleaned.substring(4, 6), 16);
            return new float[]{r / 255f, g / 255f, b / 255f};
        } catch (NumberFormatException e) {
            return new float[]{0f, 0f, 0f};
        }
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Calculates the total width from an array of column widths.
     */
    private float totalWidth(float[] columnWidths) {
        float total = 0;
        for (float w : columnWidths) {
            total += w;
        }
        return total;
    }
}
