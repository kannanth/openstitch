package com.openstitch.engine.render;

import com.openstitch.engine.model.Dimension;
import com.openstitch.engine.model.Element;
import com.openstitch.engine.model.ImageElement;
import com.openstitch.engine.model.ImageFit;
import com.openstitch.engine.model.ImageSource;
import com.openstitch.engine.model.Position;
import com.openstitch.engine.parser.DataContext;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;

/**
 * Renders ImageElement instances into the PDF.
 * Supports static base64 images, data-field resolved images, and URL-based images.
 * Handles CONTAIN, COVER, and STRETCH fit modes, as well as FLOW and ABSOLUTE positioning.
 */
public class ImageRenderer implements ElementRenderer<ImageElement> {

    private static final Logger LOG = LoggerFactory.getLogger(ImageRenderer.class);

    @Override
    public float render(ImageElement element, RenderContext context, DataContext dataContext) throws IOException {
        // Resolve image bytes from the configured source
        byte[] imageBytes;
        try {
            imageBytes = resolveImageBytes(element, dataContext);
        } catch (Exception e) {
            LOG.warn("Failed to load image (source={}, data={}): {}",
                    element.getSource(), element.getData(), e.getMessage());
            return 0;
        }

        if (imageBytes == null || imageBytes.length == 0) {
            LOG.warn("Image data is empty (source={}, data={})", element.getSource(), element.getData());
            return 0;
        }

        // Create the PDFBox image object
        PDImageXObject image;
        try {
            image = PDImageXObject.createFromByteArray(context.getDocument(), imageBytes, "image");
        } catch (Exception e) {
            LOG.warn("Failed to create PDF image object: {}", e.getMessage());
            return 0;
        }

        // Calculate rendering dimensions based on fit mode
        float[] renderDims = calculateRenderDimensions(element, image, context.getContentWidth());
        float renderWidth = renderDims[0];
        float renderHeight = renderDims[1];

        // Apply margin top
        context.moveCursorY(-element.getMarginTop());

        float heightConsumed = 0;

        if (element.getPositioning() == Element.Positioning.ABSOLUTE) {
            // Absolute positioning: place at the specified coordinates
            Position pos = element.getPosition();
            float x = pos != null ? pos.x() : 0;
            // PDFBox y-axis goes up from bottom; the position y is the top of the image,
            // so the bottom-left y for drawImage is (y - renderHeight)
            float y = pos != null ? pos.y() - renderHeight : 0;

            PDPageContentStream contentStream = context.getContentStream();
            contentStream.drawImage(image, x, y, renderWidth, renderHeight);

            // Absolute elements do not consume flow height or move the cursor
            heightConsumed = 0;
        } else {
            // FLOW positioning: place at current cursor position
            // Check if we need a page break
            if (context.needsPageBreak(renderHeight)) {
                context.closeContentStream();
                context.newPage();
            }

            float x = context.getCursorX();
            // cursorY is the top of the current line; PDFBox drawImage needs the bottom-left y
            float y = context.getCursorY() - renderHeight;

            PDPageContentStream contentStream = context.getContentStream();
            contentStream.drawImage(image, x, y, renderWidth, renderHeight);

            // Move cursor down by the image height
            context.moveCursorY(-renderHeight);
            heightConsumed = renderHeight;
        }

        return heightConsumed;
    }

    /**
     * Resolves the raw image bytes based on the image source type.
     *
     * @param element     the image element with source configuration
     * @param dataContext the data context for resolving data field paths
     * @return the image bytes
     * @throws IOException if reading the image fails
     */
    private byte[] resolveImageBytes(ImageElement element, DataContext dataContext) throws IOException {
        ImageSource source = element.getSource();
        String data = element.getData();

        if (source == null || data == null || data.isBlank()) {
            return null;
        }

        return switch (source) {
            case STATIC -> Base64.getDecoder().decode(data);
            case DATA_FIELD -> {
                Object resolved = dataContext.resolve(data);
                if (resolved == null) {
                    LOG.warn("Data field '{}' resolved to null", data);
                    yield null;
                }
                String base64String = resolved.toString();
                yield Base64.getDecoder().decode(base64String);
            }
            case URL -> downloadImageFromUrl(data);
        };
    }

    /**
     * Downloads image bytes from a URL.
     *
     * @param urlString the URL string to download from
     * @return the downloaded image bytes
     * @throws IOException if the download fails
     */
    private byte[] downloadImageFromUrl(String urlString) throws IOException {
        URI uri = URI.create(urlString);
        try (InputStream inputStream = uri.toURL().openStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
            return buffer.toByteArray();
        }
    }

    /**
     * Calculates the rendering width and height for the image based on the element's
     * dimension specification and the chosen fit mode.
     *
     * @param element      the image element with dimension and fit configuration
     * @param image        the PDFBox image object (for natural dimensions)
     * @param contentWidth the available content width on the page
     * @return a float array of [width, height] in points
     */
    private float[] calculateRenderDimensions(ImageElement element, PDImageXObject image, float contentWidth) {
        float naturalWidth = image.getWidth();
        float naturalHeight = image.getHeight();
        Dimension dimension = element.getDimension();

        if (dimension == null || (dimension.width() <= 0 && dimension.height() <= 0)) {
            // No dimension specified: use natural size, but cap width at content width
            if (naturalWidth > contentWidth) {
                float scale = contentWidth / naturalWidth;
                return new float[]{contentWidth, naturalHeight * scale};
            }
            return new float[]{naturalWidth, naturalHeight};
        }

        float targetWidth = dimension.width() > 0 ? dimension.width() : 0;
        float targetHeight = dimension.height() > 0 ? dimension.height() : 0;

        ImageFit fit = element.getFit() != null ? element.getFit() : ImageFit.CONTAIN;

        return switch (fit) {
            case STRETCH -> {
                // Use exact specified dimensions; fill in missing dimension from natural aspect ratio
                float w = targetWidth > 0 ? targetWidth : naturalWidth;
                float h = targetHeight > 0 ? targetHeight : naturalHeight;
                yield new float[]{w, h};
            }
            case CONTAIN -> calculateContainDimensions(naturalWidth, naturalHeight, targetWidth, targetHeight);
            case COVER -> calculateCoverDimensions(naturalWidth, naturalHeight, targetWidth, targetHeight);
        };
    }

    /**
     * Calculates dimensions for CONTAIN fit mode.
     * Scales the image to fit entirely within the target dimensions while maintaining aspect ratio.
     * The image may be smaller than the target area (letterboxed).
     *
     * @param naturalWidth  the image's natural width
     * @param naturalHeight the image's natural height
     * @param targetWidth   the target width (0 means unconstrained)
     * @param targetHeight  the target height (0 means unconstrained)
     * @return a float array of [width, height]
     */
    private float[] calculateContainDimensions(float naturalWidth, float naturalHeight,
                                               float targetWidth, float targetHeight) {
        float aspectRatio = naturalWidth / naturalHeight;

        if (targetWidth > 0 && targetHeight > 0) {
            // Both dimensions specified: fit within both, maintaining aspect ratio
            float scaleByWidth = targetWidth / naturalWidth;
            float scaleByHeight = targetHeight / naturalHeight;
            float scale = Math.min(scaleByWidth, scaleByHeight);
            return new float[]{naturalWidth * scale, naturalHeight * scale};
        } else if (targetWidth > 0) {
            // Only width specified: scale height proportionally
            return new float[]{targetWidth, targetWidth / aspectRatio};
        } else {
            // Only height specified: scale width proportionally
            return new float[]{targetHeight * aspectRatio, targetHeight};
        }
    }

    /**
     * Calculates dimensions for COVER fit mode.
     * Scales the image to completely cover the target dimensions while maintaining aspect ratio.
     * The image may extend beyond the target area (cropped in effect).
     *
     * @param naturalWidth  the image's natural width
     * @param naturalHeight the image's natural height
     * @param targetWidth   the target width (0 means unconstrained)
     * @param targetHeight  the target height (0 means unconstrained)
     * @return a float array of [width, height]
     */
    private float[] calculateCoverDimensions(float naturalWidth, float naturalHeight,
                                             float targetWidth, float targetHeight) {
        float aspectRatio = naturalWidth / naturalHeight;

        if (targetWidth > 0 && targetHeight > 0) {
            // Both dimensions specified: cover both, maintaining aspect ratio
            float scaleByWidth = targetWidth / naturalWidth;
            float scaleByHeight = targetHeight / naturalHeight;
            float scale = Math.max(scaleByWidth, scaleByHeight);
            return new float[]{naturalWidth * scale, naturalHeight * scale};
        } else if (targetWidth > 0) {
            // Only width specified: scale height proportionally
            return new float[]{targetWidth, targetWidth / aspectRatio};
        } else {
            // Only height specified: scale width proportionally
            return new float[]{targetHeight * aspectRatio, targetHeight};
        }
    }
}
