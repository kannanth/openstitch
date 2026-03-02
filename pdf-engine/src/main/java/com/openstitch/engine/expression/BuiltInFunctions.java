package com.openstitch.engine.expression;

import com.openstitch.engine.exception.ExpressionException;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Registers built-in functions in a {@link FunctionRegistry}.
 * <p>
 * Available functions:
 * <ul>
 *   <li>{@code format(number, pattern)} - format a number using {@link DecimalFormat}</li>
 *   <li>{@code upper(string)} - convert to uppercase</li>
 *   <li>{@code lower(string)} - convert to lowercase</li>
 *   <li>{@code sum(list, field)} - sum a numeric field across a list of maps</li>
 *   <li>{@code count(list)} - count items in a list or collection</li>
 *   <li>{@code now()} - current date/time as ISO string</li>
 *   <li>{@code dateFormat(dateString, pattern)} - format a date string with the given pattern</li>
 *   <li>{@code today()} - current date as ISO date string</li>
 *   <li>{@code time()} - current time as HH:mm:ss string</li>
 *   <li>{@code timeFormat(input, pattern)} - format a time/datetime string with a pattern</li>
 *   <li>{@code dateFormatLocale(date, pattern, locale)} - locale-aware date formatting</li>
 * </ul>
 */
public final class BuiltInFunctions {

    private BuiltInFunctions() {
        // utility class
    }

    /**
     * Register all built-in functions into the given registry.
     *
     * @param registry the function registry
     */
    public static void register(FunctionRegistry registry) {
        registry.register("format", BuiltInFunctions::formatNumber);
        registry.register("upper", BuiltInFunctions::upper);
        registry.register("lower", BuiltInFunctions::lower);
        registry.register("sum", BuiltInFunctions::sum);
        registry.register("count", BuiltInFunctions::count);
        registry.register("now", BuiltInFunctions::now);
        registry.register("dateFormat", BuiltInFunctions::dateFormat);
        registry.register("today", BuiltInFunctions::today);
        registry.register("time", BuiltInFunctions::time);
        registry.register("timeFormat", BuiltInFunctions::timeFormat);
        registry.register("dateFormatLocale", BuiltInFunctions::dateFormatLocale);
    }

    /**
     * format(number, pattern) - Formats a number using DecimalFormat.
     * Example: format(1234.5, "#,##0.00") -> "1,234.50"
     */
    private static Object formatNumber(Object[] args) {
        requireArgs("format", args, 2);
        Number number = toNumber(args[0], "format");
        String pattern = toString(args[1]);

        try {
            DecimalFormat df = new DecimalFormat(pattern);
            return df.format(number.doubleValue());
        } catch (IllegalArgumentException e) {
            throw new ExpressionException("Invalid format pattern: " + pattern, e);
        }
    }

    /**
     * upper(string) - Converts a string to uppercase.
     */
    private static Object upper(Object[] args) {
        requireArgs("upper", args, 1);
        return toString(args[0]).toUpperCase();
    }

    /**
     * lower(string) - Converts a string to lowercase.
     */
    private static Object lower(Object[] args) {
        requireArgs("lower", args, 1);
        return toString(args[0]).toLowerCase();
    }

    /**
     * sum(list, field) - Sums a numeric field across a list of maps.
     * Example: sum(items, "amount") where items is a List of Maps.
     */
    @SuppressWarnings("unchecked")
    private static Object sum(Object[] args) {
        requireArgs("sum", args, 2);

        if (args[0] == null) {
            return 0.0;
        }

        if (!(args[0] instanceof List)) {
            throw new ExpressionException("sum() first argument must be a list, got: "
                    + args[0].getClass().getSimpleName());
        }

        List<?> list = (List<?>) args[0];
        String field = toString(args[1]);
        double total = 0.0;

        for (Object item : list) {
            if (item instanceof Map) {
                Object value = ((Map<String, Object>) item).get(field);
                if (value instanceof Number) {
                    total += ((Number) value).doubleValue();
                }
            }
        }

        return total;
    }

    /**
     * count(list) - Counts items in a list or collection.
     */
    private static Object count(Object[] args) {
        requireArgs("count", args, 1);

        if (args[0] == null) {
            return 0;
        }
        if (args[0] instanceof Collection) {
            return ((Collection<?>) args[0]).size();
        }

        throw new ExpressionException("count() argument must be a list or collection, got: "
                + args[0].getClass().getSimpleName());
    }

    /**
     * now() - Returns the current date/time as an ISO-8601 string.
     */
    private static Object now(Object[] args) {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * dateFormat(dateString, pattern) - Formats a date string with the given pattern.
     * Accepts ISO date or date-time strings.
     * Example: dateFormat("2024-01-15", "MM/dd/yyyy") -> "01/15/2024"
     */
    private static Object dateFormat(Object[] args) {
        requireArgs("dateFormat", args, 2);
        String dateString = toString(args[0]);
        String pattern = toString(args[1]);

        try {
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(pattern);

            // Try parsing as LocalDateTime first, then fall back to LocalDate
            try {
                LocalDateTime dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return dateTime.format(outputFormatter);
            } catch (DateTimeParseException e) {
                LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
                return date.format(outputFormatter);
            }
        } catch (DateTimeParseException e) {
            throw new ExpressionException("Failed to parse date: " + dateString, e);
        } catch (IllegalArgumentException e) {
            throw new ExpressionException("Invalid date format pattern: " + pattern, e);
        }
    }

    /**
     * today() - Returns the current date as an ISO-8601 date string (yyyy-MM-dd).
     */
    private static Object today(Object[] args) {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * time() - Returns the current time as HH:mm:ss.
     */
    private static Object time(Object[] args) {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * timeFormat(input, pattern) - Formats a time or datetime string with a DateTimeFormatter pattern.
     * Accepts ISO date-time strings or HH:mm:ss time strings.
     * Example: timeFormat("2026-02-28T14:30:00", "hh:mm a") -> "02:30 PM"
     */
    private static Object timeFormat(Object[] args) {
        requireArgs("timeFormat", args, 2);
        String input = toString(args[0]);
        String pattern = toString(args[1]);

        try {
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(pattern);

            // Try parsing as LocalDateTime first
            try {
                LocalDateTime dateTime = LocalDateTime.parse(input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return dateTime.format(outputFormatter);
            } catch (DateTimeParseException e) {
                // Try as LocalTime
                LocalTime time = LocalTime.parse(input);
                return time.format(outputFormatter);
            }
        } catch (DateTimeParseException e) {
            throw new ExpressionException("Failed to parse time: " + input, e);
        } catch (IllegalArgumentException e) {
            throw new ExpressionException("Invalid time format pattern: " + pattern, e);
        }
    }

    /**
     * dateFormatLocale(dateString, pattern, locale) - Locale-aware date formatting.
     * Locale is a BCP-47 tag like "en-US", "fr-FR", "de-DE".
     * Example: dateFormatLocale("2026-02-28", "MMMM d, yyyy", "fr-FR") -> "février 28, 2026"
     */
    private static Object dateFormatLocale(Object[] args) {
        requireArgs("dateFormatLocale", args, 3);
        String dateString = toString(args[0]);
        String pattern = toString(args[1]);
        String localeTag = toString(args[2]);

        try {
            Locale locale = Locale.forLanguageTag(localeTag);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(pattern, locale);

            // Try parsing as LocalDateTime first, then fall back to LocalDate
            try {
                LocalDateTime dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return dateTime.format(outputFormatter);
            } catch (DateTimeParseException e) {
                LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
                return date.format(outputFormatter);
            }
        } catch (DateTimeParseException e) {
            throw new ExpressionException("Failed to parse date: " + dateString, e);
        } catch (IllegalArgumentException e) {
            throw new ExpressionException("Invalid date format pattern: " + pattern, e);
        }
    }

    // --- Helper methods ---

    private static void requireArgs(String functionName, Object[] args, int required) {
        if (args == null || args.length < required) {
            throw new ExpressionException(
                    functionName + "() requires at least " + required + " argument(s), got: "
                            + (args == null ? 0 : args.length));
        }
    }

    private static String toString(Object value) {
        return value == null ? "" : value.toString();
    }

    private static Number toNumber(Object value, String functionName) {
        if (value instanceof Number) {
            return (Number) value;
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw new ExpressionException(
                        functionName + "() requires a numeric argument, got: \"" + value + "\"", e);
            }
        }
        throw new ExpressionException(
                functionName + "() requires a numeric argument, got: "
                        + (value == null ? "null" : value.getClass().getSimpleName()));
    }
}
