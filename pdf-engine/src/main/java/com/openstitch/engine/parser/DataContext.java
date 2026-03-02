package com.openstitch.engine.parser;

import com.openstitch.engine.exception.DataParseException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wraps parsed data and provides dot-path resolution for accessing nested values.
 * Supports parent contexts for repeating sections and $root access.
 */
public class DataContext {

    private static final String ROOT_PREFIX = "$root.";
    private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile("^(.+?)\\[(\\d+)]$");

    private final Map<String, Object> data;
    private final DataContext parent;
    private Object currentItem;
    private Map<String, Object> systemVariables = new HashMap<>();

    /**
     * Creates a root data context.
     *
     * @param data the root data map
     */
    public DataContext(Map<String, Object> data) {
        this(data, null);
    }

    /**
     * Creates a child data context with a parent reference.
     *
     * @param data   the data map for this context
     * @param parent the parent context (for $root access)
     */
    public DataContext(Map<String, Object> data, DataContext parent) {
        this.data = data != null ? data : Collections.emptyMap();
        this.parent = parent;
    }

    /**
     * Resolves a dot-separated path to a value in the data hierarchy.
     * <p>
     * Supports:
     * <ul>
     *   <li>Simple paths: "name", "company.address.city"</li>
     *   <li>Root access: "$root.company.name" (always resolves from root context)</li>
     *   <li>Array indexing: "items[0].name", "matrix[1][2]"</li>
     * </ul>
     *
     * @param dotPath the dot-separated path
     * @return the resolved value, or null if not found
     */
    public Object resolve(String dotPath) {
        if (dotPath == null || dotPath.isBlank()) {
            return null;
        }

        // Check system variables first (e.g., pageNumber, totalPages)
        if (systemVariables.containsKey(dotPath)) {
            return systemVariables.get(dotPath);
        }

        // Handle $root prefix: delegate to the root context
        if (dotPath.startsWith(ROOT_PREFIX)) {
            DataContext root = getRootContext();
            return root.resolve(dotPath.substring(ROOT_PREFIX.length()));
        }

        String[] segments = dotPath.split("\\.");
        Object current = data;

        for (String segment : segments) {
            if (current == null) {
                return null;
            }
            current = resolveSegment(current, segment);
        }

        return current;
    }

    /**
     * Resolves a dot-path to a List of Maps, suitable for repeating sections and table data.
     *
     * @param dotPath the dot-separated path to a list
     * @return the resolved list of maps
     * @throws DataParseException if the resolved value is not a list
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> resolveList(String dotPath) throws DataParseException {
        Object value = resolve(dotPath);

        if (value == null) {
            return Collections.emptyList();
        }

        if (!(value instanceof List)) {
            throw new DataParseException(
                    "Expected a list at path '" + dotPath + "', but found: " + value.getClass().getSimpleName());
        }

        List<?> rawList = (List<?>) value;
        for (int i = 0; i < rawList.size(); i++) {
            Object item = rawList.get(i);
            if (item != null && !(item instanceof Map)) {
                throw new DataParseException(
                        "Expected map elements in list at path '" + dotPath
                                + "', but element at index " + i + " is: " + item.getClass().getSimpleName());
            }
        }

        return (List<Map<String, Object>>) value;
    }

    /**
     * Sets a system variable that takes priority over data resolution.
     * Used for built-in variables like pageNumber and totalPages.
     *
     * @param key   the variable name
     * @param value the variable value
     */
    public void setSystemVariable(String key, Object value) {
        systemVariables.put(key, value);
    }

    /**
     * Creates a child context with this context as the parent.
     * The child context uses the provided item data and can still access
     * the root data via $root. System variables are copied to the child.
     *
     * @param itemData the data map for the child context
     * @return a new child DataContext
     */
    public DataContext createChildContext(Map<String, Object> itemData) {
        DataContext child = new DataContext(itemData, this);
        child.systemVariables = new HashMap<>(this.systemVariables);
        return child;
    }

    /**
     * Gets the current item (used when iterating in repeating sections).
     *
     * @return the current item, or null
     */
    public Object getCurrentItem() {
        return currentItem;
    }

    /**
     * Sets the current item for iteration in repeating sections.
     *
     * @param item the current item
     */
    public void setCurrentItem(Object item) {
        this.currentItem = item;
    }

    /**
     * Gets the parent context.
     *
     * @return the parent context, or null if this is the root
     */
    public DataContext getParent() {
        return parent;
    }

    /**
     * Gets the underlying data map.
     *
     * @return the data map
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Navigates to the root context by following parent references.
     */
    private DataContext getRootContext() {
        DataContext ctx = this;
        while (ctx.parent != null) {
            ctx = ctx.parent;
        }
        return ctx;
    }

    /**
     * Resolves a single path segment against a current object.
     * Handles both plain keys and array-indexed keys like "items[0]".
     */
    @SuppressWarnings("unchecked")
    private Object resolveSegment(Object current, String segment) {
        // Check for array indexing: segment might be "items[0]" or "[0]"
        // There can be multiple indices like "matrix[1][2]"
        Matcher matcher = ARRAY_INDEX_PATTERN.matcher(segment);

        if (matcher.matches()) {
            // Has array index — first resolve the key, then index into the array
            String key = matcher.group(1);
            Object value = lookupKey(current, key);
            // Now resolve any trailing indices (there may be chained like [1][2])
            return resolveIndices(value, segment.substring(key.length()));
        }

        // Check if the entire segment is just array indices like "[0]"
        if (segment.startsWith("[")) {
            return resolveIndices(current, segment);
        }

        return lookupKey(current, segment);
    }

    /**
     * Looks up a key in a Map.
     */
    @SuppressWarnings("unchecked")
    private Object lookupKey(Object current, String key) {
        if (current instanceof Map) {
            return ((Map<String, Object>) current).get(key);
        }
        return null;
    }

    /**
     * Resolves chained array indices like "[0]", "[1][2]".
     */
    private Object resolveIndices(Object value, String indexPart) {
        if (value == null || indexPart.isEmpty()) {
            return value;
        }

        Pattern indexPattern = Pattern.compile("\\[(\\d+)]");
        Matcher indexMatcher = indexPattern.matcher(indexPart);
        Object current = value;

        while (indexMatcher.find()) {
            if (!(current instanceof List)) {
                return null;
            }
            int index = Integer.parseInt(indexMatcher.group(1));
            List<?> list = (List<?>) current;
            if (index < 0 || index >= list.size()) {
                return null;
            }
            current = list.get(index);
        }

        return current;
    }
}
