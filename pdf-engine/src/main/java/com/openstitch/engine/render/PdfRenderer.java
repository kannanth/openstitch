package com.openstitch.engine.render;

import com.openstitch.engine.exception.RenderException;
import com.openstitch.engine.expression.ExpressionEvaluator;
import com.openstitch.engine.model.ChartElement;
import com.openstitch.engine.model.ConditionalBlock;
import com.openstitch.engine.model.Element;
import com.openstitch.engine.model.ImageElement;
import com.openstitch.engine.model.PageNumbering;
import com.openstitch.engine.model.RepeatingSection;
import com.openstitch.engine.model.SectionDefinition;
import com.openstitch.engine.model.TableElement;
import com.openstitch.engine.model.Template;
import com.openstitch.engine.model.TextElement;
import com.openstitch.engine.parser.DataContext;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The main orchestrator that renders a complete Template into a PDF document.
 * Manages element renderers and coordinates the rendering pipeline.
 */
public class PdfRenderer {

    private final Map<Class<? extends Element>, ElementRenderer<?>> renderers = new HashMap<>();
    private final ExpressionEvaluator expressionEvaluator;
    private final HeaderFooterRenderer headerFooterRenderer;
    private final SectionRenderer sectionRenderer;
    private final PageNumberRenderer pageNumberRenderer;

    public PdfRenderer() {
        this.expressionEvaluator = new ExpressionEvaluator();
        this.headerFooterRenderer = new HeaderFooterRenderer(this);
        this.sectionRenderer = new SectionRenderer(this);
        this.pageNumberRenderer = new PageNumberRenderer();
        registerDefaultRenderers();
    }

    /**
     * Registers the built-in renderers for supported element types.
     */
    private void registerDefaultRenderers() {
        renderers.put(TextElement.class, new TextRenderer());
        renderers.put(ImageElement.class, new ImageRenderer());
        renderers.put(TableElement.class, new TableRenderer());
        renderers.put(ChartElement.class, new ChartRenderer());
        renderers.put(ConditionalBlock.class, new ConditionalRenderer(this));
        renderers.put(RepeatingSection.class, new RepeatingSectionRenderer(this));
    }

    /**
     * Register a custom renderer for a specific element type.
     *
     * @param elementClass the element class to associate the renderer with
     * @param renderer     the renderer implementation
     * @param <T>          the element type
     */
    public <T extends Element> void registerRenderer(Class<T> elementClass, ElementRenderer<T> renderer) {
        renderers.put(elementClass, renderer);
    }

    /**
     * Render a template with data into a PDF byte array.
     * Uses two-pass rendering when page numbering is enabled.
     *
     * @param template    the parsed template definition
     * @param dataContext the data context for expression evaluation
     * @return the generated PDF as a byte array
     * @throws RenderException if rendering fails
     */
    public byte[] render(Template template, DataContext dataContext) throws RenderException {
        PageNumbering numbering = template.getPageNumbering();
        boolean twoPass = numbering != null && numbering.isEnabled();

        if (twoPass) {
            return renderTwoPass(template, dataContext);
        } else {
            return renderSinglePass(template, dataContext);
        }
    }

    /**
     * Single-pass rendering (no page numbering overlay).
     */
    private byte[] renderSinglePass(Template template, DataContext dataContext) throws RenderException {
        try (PDDocument document = new PDDocument()) {
            renderDocument(document, template, dataContext);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RenderException("Failed to render PDF", e);
        }
    }

    /**
     * Two-pass rendering: first pass counts pages, second pass renders with
     * totalPages known, then overlays page numbers on each page.
     */
    private byte[] renderTwoPass(Template template, DataContext dataContext) throws RenderException {
        int totalPages;

        // Pass 1: render to count pages (result is discarded)
        try (PDDocument pass1Doc = new PDDocument()) {
            RenderContext pass1Ctx = renderDocument(pass1Doc, template, dataContext);
            totalPages = pass1Ctx.getCurrentPageNumber();
        } catch (IOException e) {
            throw new RenderException("Failed during first rendering pass", e);
        }

        // Pass 2: render with totalPages injected
        try (PDDocument document = new PDDocument()) {
            dataContext.setSystemVariable("totalPages", totalPages);
            RenderContext context = renderDocument(document, template, dataContext);
            context.setTotalPages(totalPages);

            // Overlay page numbers on each page
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PDPage page = document.getPage(i);
                try (PDPageContentStream cs = new PDPageContentStream(
                        document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    context.setContentStream(cs);
                    pageNumberRenderer.renderPageNumber(context, i + 1, totalPages);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RenderException("Failed during second rendering pass", e);
        }
    }

    /**
     * Core rendering pipeline: creates pages, renders headers/footers/sections.
     * Returns the RenderContext so callers can inspect page count.
     */
    private RenderContext renderDocument(PDDocument document, Template template, DataContext dataContext) throws IOException {
        RenderContext context = new RenderContext(document, template, expressionEvaluator);
        context.setDataContext(dataContext);

        // Set page break handler for proper footer/header rendering on breaks
        context.setPageBreakHandler(ctx -> {
            headerFooterRenderer.renderFooter(ctx, dataContext);
            handleNewPage(ctx, dataContext);
        });

        // Create the first page and render its header
        handleNewPage(context, dataContext);

        // Render body sections
        if (template.getBody() != null && template.getBody().getSections() != null) {
            for (SectionDefinition section : template.getBody().getSections()) {
                sectionRenderer.renderSection(section, context, dataContext);
            }
        }

        // Render footer on the last page before closing
        headerFooterRenderer.renderFooter(context, dataContext);

        // Close the last content stream
        context.closeContentStream();

        return context;
    }

    /**
     * Render a single element using the registered renderer for its type.
     * Package-private so that HeaderFooterRenderer, ConditionalRenderer,
     * RepeatingSectionRenderer, and SectionRenderer can delegate element
     * rendering back to PdfRenderer.
     *
     * @param element     the element to render
     * @param context     the current render context
     * @param dataContext the data context for expression resolution
     * @return the height consumed by the element in points
     * @throws IOException if a PDF writing error occurs
     */
    @SuppressWarnings("unchecked")
    float renderSingleElement(Element element, RenderContext context, DataContext dataContext) throws IOException {
        ElementRenderer renderer = renderers.get(element.getClass());
        if (renderer == null) {
            throw new RenderException("No renderer registered for element type: " + element.getClass().getSimpleName());
        }

        // Render the element (the renderer returns the height consumed)
        float elementHeight = renderer.render(element, context, dataContext);

        // Add margin bottom
        context.moveCursorY(-element.getMarginBottom());

        return elementHeight + element.getMarginBottom();
    }

    /**
     * Handle creation of a new page with header rendering.
     * Wraps context.newPage() so that headers are automatically rendered
     * on every new page.
     *
     * @param context     the render context
     * @param dataContext the data context for expression evaluation
     * @throws IOException if page creation or header rendering fails
     */
    void handleNewPage(RenderContext context, DataContext dataContext) throws IOException {
        context.newPage();
        headerFooterRenderer.renderHeader(context, dataContext);
    }

    /**
     * Gets the header/footer renderer used by this renderer.
     *
     * @return the header/footer renderer
     */
    HeaderFooterRenderer getHeaderFooterRenderer() {
        return headerFooterRenderer;
    }

    /**
     * Gets the expression evaluator used by this renderer.
     *
     * @return the expression evaluator
     */
    public ExpressionEvaluator getExpressionEvaluator() {
        return expressionEvaluator;
    }
}
