package com.openstitch.engine.render;

import com.openstitch.engine.expression.ExpressionEvaluator;
import com.openstitch.engine.model.Style;
import com.openstitch.engine.model.TextElement;
import com.openstitch.engine.parser.DataContext;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders TextElement instances into the PDF.
 * Supports font selection, text color, alignment, word wrapping,
 * expression evaluation, and page break handling for long text.
 */
public class TextRenderer implements ElementRenderer<TextElement> {

    @Override
    public float render(TextElement element, RenderContext context, DataContext dataContext) throws IOException {
        // Resolve expressions in the text content
        ExpressionEvaluator evaluator = context.getExpressionEvaluator();
        String rawContent = element.getContent();
        String text = evaluator.evaluate(rawContent, dataContext);

        if (text == null || text.isEmpty()) {
            return 0;
        }

        // Determine style (use element style or defaults)
        Style style = element.getStyle() != null ? element.getStyle() : new Style();

        // Resolve font
        PDType1Font font = resolveFont(style.fontFamily(), style.bold(), style.italic());
        float fontSize = style.fontSize() > 0 ? style.fontSize() : 12f;
        float lineHeight = style.lineHeight() > 0 ? style.lineHeight() : 1.2f;
        float leading = fontSize * lineHeight;

        // Determine available width
        float availableWidth = context.getContentWidth();

        // Word-wrap the text into lines
        List<String> lines = wrapText(text, font, fontSize, availableWidth);

        // Calculate total height needed
        float totalHeight = lines.size() * leading;

        // Apply margin top
        context.moveCursorY(-element.getMarginTop());

        // Parse text color
        float[] color = parseHexColor(style.textColor());

        // Render lines, handling page breaks as needed
        float heightConsumed = 0;

        for (int i = 0; i < lines.size(); i++) {
            // Check if we need a page break before rendering this line
            if (context.needsPageBreak(leading)) {
                context.triggerPageBreak();
            }

            PDPageContentStream contentStream = context.getContentStream();

            // Calculate X position based on alignment
            float lineWidth = getStringWidth(lines.get(i), font, fontSize);
            float x = calculateAlignedX(context, lineWidth, style.alignment());

            // Draw the text line
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.setNonStrokingColor(color[0], color[1], color[2]);
            contentStream.newLineAtOffset(x, context.getCursorY() - fontSize);
            contentStream.showText(lines.get(i));
            contentStream.endText();

            // Move cursor down by leading
            context.moveCursorY(-leading);
            heightConsumed += leading;
        }

        return heightConsumed;
    }

    /**
     * Wraps text into lines that fit within the given width.
     * Splits on whitespace and measures word widths using the font metrics.
     *
     * @param text           the text to wrap
     * @param font           the font to use for measurement
     * @param fontSize       the font size in points
     * @param availableWidth the maximum line width in points
     * @return a list of lines that each fit within the available width
     * @throws IOException if font metric retrieval fails
     */
    private List<String> wrapText(String text, PDType1Font font, float fontSize, float availableWidth)
            throws IOException {
        List<String> lines = new ArrayList<>();

        // Split on newlines first to preserve explicit line breaks
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

                if (currentLine.isEmpty()) {
                    // First word on the line
                    float wordWidth = getStringWidth(word, font, fontSize);
                    if (wordWidth > availableWidth) {
                        // Single word exceeds available width; add it on its own line
                        lines.add(word);
                    } else {
                        currentLine.append(word);
                    }
                } else {
                    // Check if adding this word (with a space) still fits
                    String candidate = currentLine + " " + word;
                    float candidateWidth = getStringWidth(candidate, font, fontSize);

                    if (candidateWidth <= availableWidth) {
                        currentLine.append(" ").append(word);
                    } else {
                        // Flush the current line and start a new one
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    }
                }
            }

            // Add the last line of this paragraph
            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }
        }

        // Ensure at least one line if text was non-empty
        if (lines.isEmpty()) {
            lines.add("");
        }

        return lines;
    }

    /**
     * Calculates the width of a string in the given font and size.
     *
     * @param text     the text to measure
     * @param font     the font
     * @param fontSize the font size in points
     * @return the width of the string in points
     * @throws IOException if font metric retrieval fails
     */
    private float getStringWidth(String text, PDType1Font font, float fontSize) throws IOException {
        return font.getStringWidth(text) / 1000f * fontSize;
    }

    /**
     * Calculates the X position for a line based on alignment.
     *
     * @param context   the render context
     * @param lineWidth the measured width of the text line
     * @param alignment the text alignment
     * @return the X coordinate where the text should begin
     */
    private float calculateAlignedX(RenderContext context, float lineWidth, Style.Alignment alignment) {
        float leftMargin = context.getLeftMargin();
        float contentWidth = context.getContentWidth();
        Style.Alignment effectiveAlignment = alignment != null ? alignment : Style.Alignment.LEFT;

        return switch (effectiveAlignment) {
            case CENTER -> leftMargin + (contentWidth - lineWidth) / 2f;
            case RIGHT -> leftMargin + contentWidth - lineWidth;
            case LEFT, JUSTIFIED -> leftMargin;
        };
    }

    /**
     * Resolves a PDType1Font from the font family name, bold, and italic flags.
     * Maps common family names to PDFBox Standard 14 fonts.
     *
     * @param family the font family name (e.g., "Helvetica", "Times-Roman", "Courier")
     * @param bold   whether the font should be bold
     * @param italic whether the font should be italic
     * @return the resolved PDType1Font
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
                // Default to Helvetica family
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

    /**
     * Parses a hex color string (e.g., "#FF0000" or "FF0000") into RGB float components [0..1].
     *
     * @param hex the hex color string
     * @return a float array of [r, g, b] each in the range 0.0 to 1.0
     */
    private float[] parseHexColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new float[]{0f, 0f, 0f}; // default to black
        }

        String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;

        if (cleaned.length() != 6) {
            return new float[]{0f, 0f, 0f}; // default to black for invalid input
        }

        try {
            int r = Integer.parseInt(cleaned.substring(0, 2), 16);
            int g = Integer.parseInt(cleaned.substring(2, 4), 16);
            int b = Integer.parseInt(cleaned.substring(4, 6), 16);
            return new float[]{r / 255f, g / 255f, b / 255f};
        } catch (NumberFormatException e) {
            return new float[]{0f, 0f, 0f}; // default to black for parse errors
        }
    }
}
