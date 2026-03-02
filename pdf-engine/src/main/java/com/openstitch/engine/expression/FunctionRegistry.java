package com.openstitch.engine.expression;

import com.openstitch.engine.exception.ExpressionException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Registry for template expression functions.
 * Functions are stored by lowercase name and invoked with an array of arguments.
 */
public class FunctionRegistry {

    private final Map<String, Function<Object[], Object>> functions = new HashMap<>();

    /**
     * Register a function with the given name.
     *
     * @param name     the function name (case-insensitive)
     * @param function the function implementation
     */
    public void register(String name, Function<Object[], Object> function) {
        functions.put(name.toLowerCase(), function);
    }

    /**
     * Call a registered function by name with the given arguments.
     *
     * @param name the function name (case-insensitive)
     * @param args the arguments to pass to the function
     * @return the function result
     * @throws ExpressionException if no function is registered with the given name
     */
    public Object call(String name, Object[] args) {
        Function<Object[], Object> fn = functions.get(name.toLowerCase());
        if (fn == null) {
            throw new ExpressionException("Unknown function: " + name);
        }
        return fn.apply(args);
    }

    /**
     * Check if a function with the given name is registered.
     *
     * @param name the function name (case-insensitive)
     * @return true if the function exists
     */
    public boolean hasFunction(String name) {
        return functions.containsKey(name.toLowerCase());
    }
}
