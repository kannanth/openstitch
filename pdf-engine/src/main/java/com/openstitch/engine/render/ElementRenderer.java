package com.openstitch.engine.render;

import com.openstitch.engine.model.Element;
import com.openstitch.engine.parser.DataContext;

import java.io.IOException;

/**
 * Interface for rendering specific element types into a PDF.
 * Each element type (text, table, image, etc.) has a corresponding renderer implementation.
 *
 * @param <T> the specific Element subclass this renderer handles
 */
public interface ElementRenderer<T extends Element> {

    /**
     * Render the element at the current position in the render context.
     *
     * @param element     the element to render
     * @param context     the current rendering context (tracks page, cursor, etc.)
     * @param dataContext the data context for resolving expressions
     * @return the height consumed by this element (in points)
     * @throws IOException if a PDF writing error occurs
     */
    float render(T element, RenderContext context, DataContext dataContext) throws IOException;
}
