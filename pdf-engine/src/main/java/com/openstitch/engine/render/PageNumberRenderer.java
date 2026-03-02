package com.openstitch.engine.render;

import com.openstitch.engine.model.PageNumberFormat;
import com.openstitch.engine.model.PageNumbering;
import com.openstitch.engine.model.Style;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;

/**
 * Renders page numbers on each page during the second pass of rendering,
 * when the total number of pages is known.
 */
public class PageNumberRenderer {

    /**
     * Render page number on the given page.
     * Called during the second pass when totalPages is known.
     *
     * @param context    the render context with an active content stream
     * @param pageNumber the current page number (1-based)
     * @param totalPages the total number of pages in the document
     * @throws IOException if a PDF writing error occurs
     */
    public void renderPageNumber(RenderContext context, int pageNumber, int totalPages) throws IOException {
        PageNumbering numbering = context.getTemplate().getPageNumbering();
        if (numbering == null || !numbering.isEnabled()) {
            return;
        }

        String text = formatPageNumber(numbering, pageNumber, totalPages);

        Style style = numbering.getStyle() != null ? numbering.getStyle() : new Style();
        PDType1Font font = resolveFont(style.fontFamily(), style.bold(), style.italic());
        float fontSize = style.fontSize() > 0 ? style.fontSize() : 10f;

        float textWidth = font.getStringWidth(text) / 1000f * fontSize;

        // Position: use numbering position if specified, otherwise center bottom
        float x;
        float y;
        if (numbering.getPosition() != null) {
            x = numbering.getPosition().x();
            y = numbering.getPosition().y();
        } else {
            x = (context.getPageWidth() - textWidth) / 2f;
            y = context.getTemplate().getPageLayout().getMargins().bottom() / 2f;
        }

        PDPageContentStream cs = context.getContentStream();
        float[] color = parseHexColor(style.textColor());

        cs.beginText();
        cs.setFont(font, fontSize);
        cs.setNonStrokingColor(color[0], color[1], color[2]);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    /**
     * Format the page number string according to the numbering configuration.
     *
     * @param numbering  the page numbering configuration
     * @param pageNumber the current page number (1-based)
     * @param totalPages the total number of pages
     * @return the formatted page number string
     */
    private String formatPageNumber(PageNumbering numbering, int pageNumber, int totalPages) {
        int displayNumber = pageNumber + numbering.getStartFrom() - 1;
        int displayTotal = totalPages + numbering.getStartFrom() - 1;

        PageNumberFormat format = numbering.getFormat();
        if (format == null) {
            format = PageNumberFormat.PAGE_X_OF_Y;
        }

        return switch (format) {
            case PAGE_X_OF_Y -> String.format("Page %d of %d", displayNumber, displayTotal);
            case PAGE_X -> String.format("%d", displayNumber);
            case ROMAN -> toRoman(displayNumber);
            case CUSTOM -> {
                String fmt = numbering.getCustomFormat();
                if (fmt != null) {
                    yield fmt.replace("{page}", String.valueOf(displayNumber))
                             .replace("{total}", String.valueOf(displayTotal));
                }
                yield String.valueOf(displayNumber);
            }
        };
    }

    /**
     * Convert an integer to a Roman numeral string.
     * Supports numbers from 1 to 3999.
     *
     * @param number the number to convert
     * @return the Roman numeral representation, or the decimal string if out of range
     */
    private String toRoman(int number) {
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

        if (number <= 0 || number >= 4000) {
            return String.valueOf(number);
        }

        return thousands[number / 1000] + hundreds[(number % 1000) / 100]
                + tens[(number % 100) / 10] + ones[number % 10];
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
