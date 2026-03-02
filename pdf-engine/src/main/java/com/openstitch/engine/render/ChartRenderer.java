package com.openstitch.engine.render;

import com.openstitch.engine.model.ChartElement;
import com.openstitch.engine.model.ChartType;
import com.openstitch.engine.model.Dimension;
import com.openstitch.engine.model.Element;
import com.openstitch.engine.model.Position;
import com.openstitch.engine.parser.DataContext;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Renders ChartElement instances into the PDF.
 * Uses JFreeChart to generate chart images (BAR, PIE, LINE) and embeds them
 * as PNG images in the PDF document via PDFBox.
 */
public class ChartRenderer implements ElementRenderer<ChartElement> {

    private static final Logger LOG = LoggerFactory.getLogger(ChartRenderer.class);

    private static final float DEFAULT_CHART_WIDTH = 400f;
    private static final float DEFAULT_CHART_HEIGHT = 300f;

    @Override
    public float render(ChartElement element, RenderContext context, DataContext dataContext) throws IOException {
        // Resolve data rows from the data source
        List<Map<String, Object>> dataRows = dataContext.resolveList(element.getDataSource());

        if (dataRows == null || dataRows.isEmpty()) {
            LOG.warn("Chart data source '{}' resolved to empty list", element.getDataSource());
            return 0;
        }

        // Create the JFreeChart based on chart type
        JFreeChart chart = createChart(element, dataRows);
        if (chart == null) {
            LOG.warn("Failed to create chart of type {}", element.getChartType());
            return 0;
        }

        // Apply styling
        applyChartStyling(chart, element);

        // Determine chart dimensions
        float chartWidth = DEFAULT_CHART_WIDTH;
        float chartHeight = DEFAULT_CHART_HEIGHT;
        Dimension dimension = element.getDimension();
        if (dimension != null) {
            if (dimension.width() > 0) {
                chartWidth = dimension.width();
            }
            if (dimension.height() > 0) {
                chartHeight = dimension.height();
            }
        }

        // Render chart to a BufferedImage and convert to PNG bytes
        int imageWidth = Math.round(chartWidth * 2); // render at 2x for better quality
        int imageHeight = Math.round(chartHeight * 2);
        BufferedImage bufferedImage = chart.createBufferedImage(imageWidth, imageHeight);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();

        // Create PDFBox image object
        PDImageXObject pdfImage = PDImageXObject.createFromByteArray(
                context.getDocument(), imageBytes, "chart");

        // Apply margin top
        context.moveCursorY(-element.getMarginTop());

        float heightConsumed = 0;

        if (element.getPositioning() == Element.Positioning.ABSOLUTE) {
            // Absolute positioning: place at the specified coordinates
            Position pos = element.getPosition();
            float x = pos != null ? pos.x() : 0;
            float y = pos != null ? pos.y() - chartHeight : 0;

            PDPageContentStream contentStream = context.getContentStream();
            contentStream.drawImage(pdfImage, x, y, chartWidth, chartHeight);

            // Absolute elements do not consume flow height
            heightConsumed = 0;
        } else {
            // FLOW positioning: place at current cursor position
            if (context.needsPageBreak(chartHeight)) {
                context.closeContentStream();
                context.newPage();
            }

            float x = context.getCursorX();
            float y = context.getCursorY() - chartHeight;

            PDPageContentStream contentStream = context.getContentStream();
            contentStream.drawImage(pdfImage, x, y, chartWidth, chartHeight);

            context.moveCursorY(-chartHeight);
            heightConsumed = chartHeight;
        }

        return heightConsumed;
    }

    /**
     * Creates a JFreeChart based on the chart type specified in the element.
     *
     * @param element  the chart element configuration
     * @param dataRows the resolved data rows
     * @return the created JFreeChart, or null if the chart type is unsupported
     */
    private JFreeChart createChart(ChartElement element, List<Map<String, Object>> dataRows) {
        ChartType chartType = element.getChartType();
        if (chartType == null) {
            chartType = ChartType.BAR;
        }

        String title = element.getTitle() != null ? element.getTitle() : "";
        String categoryLabel = "";
        String valueLabel = "";

        Map<String, String> axisLabels = element.getAxisLabels();
        if (axisLabels != null) {
            categoryLabel = axisLabels.getOrDefault("category", "");
            valueLabel = axisLabels.getOrDefault("value", "");
        }

        return switch (chartType) {
            case BAR -> createBarChart(element, dataRows, title, categoryLabel, valueLabel);
            case LINE -> createLineChart(element, dataRows, title, categoryLabel, valueLabel);
            case PIE -> createPieChart(element, dataRows, title);
        };
    }

    /**
     * Creates a bar chart using DefaultCategoryDataset.
     */
    private JFreeChart createBarChart(ChartElement element, List<Map<String, Object>> dataRows,
                                      String title, String categoryLabel, String valueLabel) {
        DefaultCategoryDataset dataset = buildCategoryDataset(element, dataRows);
        return ChartFactory.createBarChart(title, categoryLabel, valueLabel, dataset);
    }

    /**
     * Creates a line chart using DefaultCategoryDataset.
     */
    private JFreeChart createLineChart(ChartElement element, List<Map<String, Object>> dataRows,
                                       String title, String categoryLabel, String valueLabel) {
        DefaultCategoryDataset dataset = buildCategoryDataset(element, dataRows);
        return ChartFactory.createLineChart(title, categoryLabel, valueLabel, dataset);
    }

    /**
     * Creates a pie chart using DefaultPieDataset.
     * Uses the categoryField as the key and the first valueField as the value.
     */
    @SuppressWarnings("unchecked")
    private JFreeChart createPieChart(ChartElement element, List<Map<String, Object>> dataRows,
                                      String title) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        String categoryField = element.getCategoryField();
        List<String> valueFields = element.getValueFields();
        String valueField = (valueFields != null && !valueFields.isEmpty()) ? valueFields.get(0) : null;

        for (Map<String, Object> row : dataRows) {
            String category = resolveFieldAsString(row, categoryField);
            if (category != null && valueField != null) {
                Number value = resolveFieldAsNumber(row, valueField);
                if (value != null) {
                    dataset.setValue(category, value);
                }
            }
        }

        return ChartFactory.createPieChart(title, dataset, element.isShowLegend(), true, false);
    }

    /**
     * Builds a DefaultCategoryDataset from the data rows.
     * Iterates data rows; for each row, gets the categoryField value and adds values for each valueField.
     */
    private DefaultCategoryDataset buildCategoryDataset(ChartElement element, List<Map<String, Object>> dataRows) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String categoryField = element.getCategoryField();
        List<String> valueFields = element.getValueFields();

        if (valueFields == null || valueFields.isEmpty()) {
            return dataset;
        }

        for (Map<String, Object> row : dataRows) {
            String category = resolveFieldAsString(row, categoryField);
            if (category == null) {
                continue;
            }

            for (String valueField : valueFields) {
                Number value = resolveFieldAsNumber(row, valueField);
                if (value != null) {
                    dataset.addValue(value, valueField, category);
                }
            }
        }

        return dataset;
    }

    /**
     * Applies styling options to the chart: colors and legend visibility.
     *
     * @param chart   the JFreeChart to style
     * @param element the chart element with styling configuration
     */
    @SuppressWarnings("unchecked")
    private void applyChartStyling(JFreeChart chart, ChartElement element) {
        // Apply legend visibility
        if (!element.isShowLegend() && chart.getLegend() != null) {
            chart.removeLegend();
        }

        // Apply custom colors if specified
        List<String> colors = element.getColors();
        if (colors != null && !colors.isEmpty()) {
            Plot plot = chart.getPlot();

            if (plot instanceof CategoryPlot categoryPlot) {
                for (int i = 0; i < colors.size(); i++) {
                    Color color = parseHexToAwtColor(colors.get(i));
                    if (color != null) {
                        categoryPlot.getRenderer().setSeriesPaint(i, color);
                    }
                }
            } else if (plot instanceof PiePlot piePlot) {
                // For pie charts, colors are applied to individual sections
                DefaultPieDataset<String> dataset = (DefaultPieDataset<String>) piePlot.getDataset();
                if (dataset != null) {
                    for (int i = 0; i < Math.min(colors.size(), dataset.getItemCount()); i++) {
                        Color color = parseHexToAwtColor(colors.get(i));
                        if (color != null) {
                            piePlot.setSectionPaint(dataset.getKey(i), color);
                        }
                    }
                }
            }
        }
    }

    /**
     * Resolves a field value from a data row as a String.
     *
     * @param row   the data row map
     * @param field the field name to resolve
     * @return the field value as a String, or null if not found
     */
    private String resolveFieldAsString(Map<String, Object> row, String field) {
        if (field == null || row == null) {
            return null;
        }
        Object value = row.get(field);
        return value != null ? value.toString() : null;
    }

    /**
     * Resolves a field value from a data row as a Number.
     *
     * @param row   the data row map
     * @param field the field name to resolve
     * @return the field value as a Number, or null if not numeric
     */
    private Number resolveFieldAsNumber(Map<String, Object> row, String field) {
        if (field == null || row == null) {
            return null;
        }
        Object value = row.get(field);
        if (value instanceof Number) {
            return (Number) value;
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Parses a hex color string (e.g., "#FF0000" or "FF0000") into a java.awt.Color.
     *
     * @param hex the hex color string
     * @return the parsed Color, or null if the input is invalid
     */
    private Color parseHexToAwtColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }

        String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;

        if (cleaned.length() != 6) {
            return null;
        }

        try {
            int r = Integer.parseInt(cleaned.substring(0, 2), 16);
            int g = Integer.parseInt(cleaned.substring(2, 4), 16);
            int b = Integer.parseInt(cleaned.substring(4, 6), 16);
            return new Color(r, g, b);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
