package com.openstitch.engine.expression;

import com.openstitch.engine.exception.ExpressionException;
import com.openstitch.engine.parser.DataContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates ${...} expressions in text strings by substituting values from a {@link DataContext}.
 * <p>
 * Supports:
 * <ul>
 *   <li>Simple variable references: {@code ${company.name}}</li>
 *   <li>Function calls: {@code ${format(amount, "#,##0.00")}}</li>
 *   <li>Arithmetic operators: +, -, *, /</li>
 *   <li>Comparison operators: ==, !=, &gt;, &lt;, &gt;=, &lt;=</li>
 *   <li>Logical operators: &amp;&amp;, ||</li>
 *   <li>String literals in single or double quotes</li>
 * </ul>
 */
public class ExpressionEvaluator {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern FUNCTION_CALL_PATTERN = Pattern.compile("^\\s*(\\w+)\\s*\\((.*)\\)\\s*$");

    private final FunctionRegistry functionRegistry;

    public ExpressionEvaluator() {
        this.functionRegistry = new FunctionRegistry();
        BuiltInFunctions.register(functionRegistry);
    }

    public ExpressionEvaluator(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }

    /**
     * Evaluate a string containing ${expression} placeholders.
     * Each placeholder is replaced with the resolved value from the DataContext.
     *
     * @param text    the text containing expressions
     * @param context the data context for resolving variables
     * @return the processed string with all expressions resolved
     */
    public String evaluate(String text, DataContext context) {
        if (text == null) {
            return "";
        }

        Matcher matcher = EXPRESSION_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String expression = matcher.group(1).trim();
            Object value = evaluateExpression(expression, context);
            String replacement = value == null ? "" : value.toString();
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Evaluate a single expression and return its value as an Object.
     * Used for conditionals where a boolean result may be needed.
     *
     * @param expression the expression string (without ${ } delimiters)
     * @param context    the data context
     * @return the evaluated value
     */
    public Object evaluateExpression(String expression, DataContext context) {
        if (expression == null || expression.isBlank()) {
            return null;
        }

        expression = expression.trim();

        // Check for logical operators first (lowest precedence)
        Object logicalResult = tryLogicalOperator(expression, context);
        if (logicalResult != null) {
            return logicalResult;
        }

        // Check for comparison operators
        Object comparisonResult = tryComparisonOperator(expression, context);
        if (comparisonResult != null) {
            return comparisonResult;
        }

        // Check for arithmetic operators (+ and - have lower precedence than * and /)
        Object arithmeticResult = tryArithmeticOperator(expression, context);
        if (arithmeticResult != null) {
            return arithmeticResult;
        }

        // Check for function call
        Matcher funcMatcher = FUNCTION_CALL_PATTERN.matcher(expression);
        if (funcMatcher.matches()) {
            String funcName = funcMatcher.group(1);
            if (functionRegistry.hasFunction(funcName)) {
                String argsStr = funcMatcher.group(2).trim();
                Object[] args = parseArguments(argsStr, context);
                return functionRegistry.call(funcName, args);
            }
        }

        // Check for string literal
        if ((expression.startsWith("\"") && expression.endsWith("\""))
                || (expression.startsWith("'") && expression.endsWith("'"))) {
            return expression.substring(1, expression.length() - 1);
        }

        // Check for numeric literal
        try {
            if (expression.contains(".")) {
                return Double.parseDouble(expression);
            }
            return Long.parseLong(expression);
        } catch (NumberFormatException ignored) {
            // not a number, fall through
        }

        // Check for boolean literals
        if ("true".equalsIgnoreCase(expression)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(expression)) {
            return Boolean.FALSE;
        }

        // Check for null literal
        if ("null".equalsIgnoreCase(expression)) {
            return null;
        }

        // Must be a variable reference - resolve from context
        return context.resolve(expression);
    }

    /**
     * Evaluate a condition expression and return a boolean result.
     *
     * @param expression the condition expression
     * @param context    the data context
     * @return the boolean result of the condition
     */
    public boolean evaluateCondition(String expression, DataContext context) {
        Object result = evaluateExpression(expression, context);
        return toBoolean(result);
    }

    /**
     * Check if the given text contains any expressions.
     *
     * @param text the text to check
     * @return true if the text contains at least one ${ } expression
     */
    public boolean hasExpressions(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return EXPRESSION_PATTERN.matcher(text).find();
    }

    // --- Operator handling ---

    /**
     * Try to evaluate logical operators (&amp;&amp; and ||).
     * Returns null if the expression does not contain a top-level logical operator.
     */
    private Object tryLogicalOperator(String expression, DataContext context) {
        int index = findOperatorOutsideQuotesAndParens(expression, "||");
        if (index >= 0) {
            String left = expression.substring(0, index).trim();
            String right = expression.substring(index + 2).trim();
            boolean leftVal = evaluateCondition(left, context);
            boolean rightVal = evaluateCondition(right, context);
            return leftVal || rightVal;
        }

        index = findOperatorOutsideQuotesAndParens(expression, "&&");
        if (index >= 0) {
            String left = expression.substring(0, index).trim();
            String right = expression.substring(index + 2).trim();
            boolean leftVal = evaluateCondition(left, context);
            boolean rightVal = evaluateCondition(right, context);
            return leftVal && rightVal;
        }

        return null;
    }

    /**
     * Try to evaluate comparison operators (==, !=, &gt;=, &lt;=, &gt;, &lt;).
     * Returns null if the expression does not contain a top-level comparison operator.
     */
    private Object tryComparisonOperator(String expression, DataContext context) {
        // Order matters: check two-char operators before single-char
        String[] operators = {"==", "!=", ">=", "<=", ">", "<"};

        for (String op : operators) {
            int index = findOperatorOutsideQuotesAndParens(expression, op);
            if (index >= 0) {
                String left = expression.substring(0, index).trim();
                String right = expression.substring(index + op.length()).trim();
                Object leftVal = evaluateExpression(left, context);
                Object rightVal = evaluateExpression(right, context);
                return compareValues(leftVal, rightVal, op);
            }
        }

        return null;
    }

    /**
     * Try to evaluate arithmetic operators (+, -, *, /).
     * Returns null if the expression does not contain a top-level arithmetic operator.
     */
    private Object tryArithmeticOperator(String expression, DataContext context) {
        // Lower precedence first: + and - (scan right-to-left for left-associativity)
        for (String op : new String[]{"+", "-"}) {
            int index = findLastOperatorOutsideQuotesAndParens(expression, op);
            if (index > 0) { // index > 0 to avoid treating unary minus as binary
                String left = expression.substring(0, index).trim();
                String right = expression.substring(index + 1).trim();
                if (!left.isEmpty() && !right.isEmpty()) {
                    Object leftVal = evaluateExpression(left, context);
                    Object rightVal = evaluateExpression(right, context);

                    // String concatenation with +
                    if ("+".equals(op) && (leftVal instanceof String || rightVal instanceof String)) {
                        String leftStr = leftVal == null ? "" : leftVal.toString();
                        String rightStr = rightVal == null ? "" : rightVal.toString();
                        return leftStr + rightStr;
                    }

                    return performArithmetic(leftVal, rightVal, op);
                }
            }
        }

        // Higher precedence: * and /
        for (String op : new String[]{"*", "/"}) {
            int index = findLastOperatorOutsideQuotesAndParens(expression, op);
            if (index > 0) {
                String left = expression.substring(0, index).trim();
                String right = expression.substring(index + 1).trim();
                if (!left.isEmpty() && !right.isEmpty()) {
                    Object leftVal = evaluateExpression(left, context);
                    Object rightVal = evaluateExpression(right, context);
                    return performArithmetic(leftVal, rightVal, op);
                }
            }
        }

        return null;
    }

    // --- Utility methods ---

    /**
     * Find the first occurrence of an operator that is not inside quotes or parentheses.
     */
    private int findOperatorOutsideQuotesAndParens(String expression, String operator) {
        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i <= expression.length() - operator.length(); i++) {
            char c = expression.charAt(i);

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (!inSingleQuote && !inDoubleQuote) {
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                } else if (depth == 0 && expression.startsWith(operator, i)) {
                    // Avoid confusing single-char > or < with two-char >= or <=
                    if (operator.length() == 1 && (operator.equals(">") || operator.equals("<"))) {
                        if (i + 1 < expression.length() && expression.charAt(i + 1) == '=') {
                            continue;
                        }
                    }
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Find the last occurrence of an operator that is not inside quotes or parentheses.
     * Used for left-to-right associativity of arithmetic operators.
     */
    private int findLastOperatorOutsideQuotesAndParens(String expression, String operator) {
        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int lastIndex = -1;

        for (int i = 0; i <= expression.length() - operator.length(); i++) {
            char c = expression.charAt(i);

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (!inSingleQuote && !inDoubleQuote) {
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                } else if (depth == 0 && expression.startsWith(operator, i)) {
                    lastIndex = i;
                }
            }
        }

        return lastIndex;
    }

    /**
     * Parse a comma-separated argument list, resolving each argument as an expression.
     */
    private Object[] parseArguments(String argsStr, DataContext context) {
        if (argsStr == null || argsStr.isBlank()) {
            return new Object[0];
        }

        List<String> argList = splitArguments(argsStr);
        Object[] args = new Object[argList.size()];

        for (int i = 0; i < argList.size(); i++) {
            args[i] = evaluateExpression(argList.get(i).trim(), context);
        }

        return args;
    }

    /**
     * Split a comma-separated argument string, respecting quotes and nested parentheses.
     */
    private List<String> splitArguments(String argsStr) {
        List<String> args = new ArrayList<>();
        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < argsStr.length(); i++) {
            char c = argsStr.charAt(i);

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                current.append(c);
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                current.append(c);
            } else if (!inSingleQuote && !inDoubleQuote) {
                if (c == '(') {
                    depth++;
                    current.append(c);
                } else if (c == ')') {
                    depth--;
                    current.append(c);
                } else if (c == ',' && depth == 0) {
                    args.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            args.add(current.toString());
        }

        return args;
    }

    /**
     * Compare two values with the given comparison operator.
     */
    private boolean compareValues(Object left, Object right, String operator) {
        // Handle null comparisons
        if (left == null && right == null) {
            return "==".equals(operator);
        }
        if (left == null || right == null) {
            return "!=".equals(operator);
        }

        // Try numeric comparison
        Double leftNum = toDouble(left);
        Double rightNum = toDouble(right);

        if (leftNum != null && rightNum != null) {
            return switch (operator) {
                case "==" -> leftNum.equals(rightNum);
                case "!=" -> !leftNum.equals(rightNum);
                case ">" -> leftNum > rightNum;
                case "<" -> leftNum < rightNum;
                case ">=" -> leftNum >= rightNum;
                case "<=" -> leftNum <= rightNum;
                default -> throw new ExpressionException("Unknown operator: " + operator);
            };
        }

        // String comparison
        String leftStr = left.toString();
        String rightStr = right.toString();
        int cmp = leftStr.compareTo(rightStr);

        return switch (operator) {
            case "==" -> cmp == 0;
            case "!=" -> cmp != 0;
            case ">" -> cmp > 0;
            case "<" -> cmp < 0;
            case ">=" -> cmp >= 0;
            case "<=" -> cmp <= 0;
            default -> throw new ExpressionException("Unknown operator: " + operator);
        };
    }

    /**
     * Perform an arithmetic operation on two values.
     */
    private Object performArithmetic(Object left, Object right, String operator) {
        Double leftNum = toDouble(left);
        Double rightNum = toDouble(right);

        if (leftNum == null || rightNum == null) {
            throw new ExpressionException(
                    "Cannot perform arithmetic '" + operator + "' on non-numeric values: "
                            + left + ", " + right);
        }

        return switch (operator) {
            case "+" -> leftNum + rightNum;
            case "-" -> leftNum - rightNum;
            case "*" -> leftNum * rightNum;
            case "/" -> {
                if (rightNum == 0.0) {
                    throw new ExpressionException("Division by zero");
                }
                yield leftNum / rightNum;
            }
            default -> throw new ExpressionException("Unknown arithmetic operator: " + operator);
        };
    }

    /**
     * Convert an object to a boolean.
     * null becomes false, Boolean as-is, Number != 0 is true, non-empty String is true.
     */
    private boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0.0;
        }
        if (value instanceof String s) {
            return !s.isEmpty() && !"false".equalsIgnoreCase(s);
        }
        return true;
    }

    /**
     * Try to convert an object to a Double. Returns null if not numeric.
     */
    private Double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
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
}
