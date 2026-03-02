package com.openstitch.engine.render;

import com.openstitch.engine.model.ConditionalBlock;
import com.openstitch.engine.model.Element;
import com.openstitch.engine.parser.DataContext;

import java.io.IOException;
import java.util.List;

/**
 * Renders ConditionalBlock elements by evaluating the condition expression
 * and rendering either the "then" elements or the "else" elements accordingly.
 * Delegates child element rendering to the parent PdfRenderer.
 */
public class ConditionalRenderer implements ElementRenderer<ConditionalBlock> {

    private final PdfRenderer parentRenderer;

    /**
     * Creates a ConditionalRenderer with a reference to the parent PdfRenderer
     * for delegating child element rendering.
     *
     * @param parentRenderer the PdfRenderer that orchestrates all element rendering
     */
    public ConditionalRenderer(PdfRenderer parentRenderer) {
        this.parentRenderer = parentRenderer;
    }

    @Override
    public float render(ConditionalBlock element, RenderContext context, DataContext dataContext) throws IOException {
        // Evaluate the condition expression
        String condition = element.getCondition();
        boolean conditionResult = context.getExpressionEvaluator()
                .evaluateCondition(condition, dataContext);

        float totalHeight = 0;

        if (conditionResult) {
            // Condition is true: render thenElements
            List<Element> thenElements = element.getThenElements();
            if (thenElements != null) {
                totalHeight = renderChildElements(thenElements, context, dataContext);
            }
        } else {
            // Condition is false: render elseElements (if present)
            List<Element> elseElements = element.getElseElements();
            if (elseElements != null) {
                totalHeight = renderChildElements(elseElements, context, dataContext);
            }
        }

        return totalHeight;
    }

    /**
     * Renders a list of child elements by delegating to the parent PdfRenderer.
     *
     * @param elements    the child elements to render
     * @param context     the current render context
     * @param dataContext the data context for expression resolution
     * @return the total height consumed by all child elements
     * @throws IOException if a PDF writing error occurs
     */
    private float renderChildElements(List<Element> elements, RenderContext context,
                                      DataContext dataContext) throws IOException {
        float totalHeight = 0;
        for (Element child : elements) {
            float childHeight = parentRenderer.renderSingleElement(child, context, dataContext);
            totalHeight += childHeight;
        }
        return totalHeight;
    }
}
