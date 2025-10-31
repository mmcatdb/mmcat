package cz.matfyz.wrappercsv.inference;

import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.DataType;
import cz.matfyz.core.rsd.RecordSchemaDescription;

import java.util.*;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MapCsvDocument {

    // TODO This is kinda strange. Why would css needed to process map properties?
    // Maybe for something like csv with nested structures? But that's not really csv anymore ... we should then parse it as JSON.

    private static final Logger LOGGER = LoggerFactory.getLogger(MapCsvDocument.class);

    private MapCsvDocument() {}

    public static RecordSchemaDescription process(Map<String, String> document) {
        final var result = new RecordSchemaDescription(
            RecordSchemaDescription.ROOT_SYMBOL,
            Char.FALSE,
            Char.FALSE,
            0,
            0
        );

        result.setTypes(DataType.MAP);

        final var children = new ObjectArrayList<RecordSchemaDescription>();

        document.forEach((key, value) -> {
            final Object parsedValue = parseValue(value);
            children.add(processChild(key, parsedValue, true));
        });

        result.setChildren(children);

        return result;
    }

    private static RecordSchemaDescription processChild(String key, Object value, boolean isFirstOccurrence) {
        final var result = new RecordSchemaDescription(
            key,
            Char.UNKNOWN,
            Char.UNKNOWN,
            1,
            isFirstOccurrence ? 1 : 0
        );

        final var type = getType(value);
        result.setTypes(DataType.OBJECT | type);

        if (type == DataType.MAP)
            result.setChildren(processMapChildren((Map<String, Object>) value));
        else if (type == DataType.ARRAY)
            result.setChildren(processArrayChildren((List<Object>) value));

        return result;
    }

    private static ObjectArrayList<RecordSchemaDescription> processMapChildren(Map<String, Object> map) {
        final var children = new ObjectArrayList<RecordSchemaDescription>();

        map.forEach((key, value) -> {
            children.add(processChild(key, value, true));
        });

        Collections.sort(children);

        return children;
    }

    private static ObjectArrayList<RecordSchemaDescription> processArrayChildren(List<Object> items) {
        final var children = new ObjectArrayList<RecordSchemaDescription>();
        RecordSchemaDescription firstElement = null;

        for (final Object item : items) {
            final var currentElement = processChild(RecordSchemaDescription.ROOT_SYMBOL, item, firstElement == null);

            if (firstElement == null) {
                firstElement = currentElement;
                children.add(currentElement);
            }
            else if (firstElement.compareTo(currentElement) != 0) {
                children.add(currentElement);
            }
        }

        return children;
    }

    private static Object parseValue(String value) {
        if (value == null || "\\N".equals(value))
            return null; // Handle null or missing values

        // Check for arrays with brackets
        if (value.startsWith("[") && value.endsWith("]")) {
            // Remove brackets and split the inner content by commas
            final String content = value.substring(1, value.length() - 1);
            return Arrays.asList(content.split(","));
        }

        // Try parsing as number
        try {
            if (value.contains("."))
                return Double.parseDouble(value);

            return Integer.parseInt(value);
        }
        catch (NumberFormatException ignored) {}

        // Try parsing as boolean
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))
            return Boolean.parseBoolean(value);

        return value;
    }

    private static int getType(Object value) {
        return switch (value) {
            case null -> DataType.UNKNOWN;
            case Number number -> DataType.NUMBER;
            case Boolean bool -> DataType.BOOLEAN;
            case String string -> DataType.STRING;
            case Map<?, ?> map -> DataType.MAP;
            case List<?> list -> DataType.ARRAY;
            default -> {
                LOGGER.error("Invalid data type");
                yield DataType.UNKNOWN;
            }
        };
    }

}
