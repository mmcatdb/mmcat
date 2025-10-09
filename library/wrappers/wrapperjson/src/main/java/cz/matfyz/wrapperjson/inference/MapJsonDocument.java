package cz.matfyz.wrapperjson.inference;

import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.DataType;
import cz.matfyz.core.rsd.RecordSchemaDescription;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class MapJsonDocument {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapJsonDocument.class);

    private MapJsonDocument() {}

    public static RecordSchemaDescription process(ObjectNode object) {
        final var result = new RecordSchemaDescription(
            RecordSchemaDescription.ROOT_SYMBOL,
            Char.FALSE,
            Char.FALSE,
            0,
            0
        );

        result.setTypes(DataType.MAP);
        result.setChildren(processMapChildren(object));

        return result;
    }

    public static RecordSchemaDescription processChild(String key, JsonNode value, boolean isFirstOccurrence) {
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
            result.setChildren(processMapChildren((ObjectNode) value));
        else if (type == DataType.ARRAY)
            result.setChildren(processArrayChildren((ArrayNode) value));

        return result;
    }

    private static ObjectArrayList<RecordSchemaDescription> processMapChildren(ObjectNode object) {
        final var children = new ObjectArrayList<RecordSchemaDescription>();

        object.properties().forEach(entry -> {
            children.add(processChild(entry.getKey(), entry.getValue(), true));
        });

        Collections.sort(children);

        return children;
    }

    private static ObjectArrayList<RecordSchemaDescription> processArrayChildren(ArrayNode items) {
        final var children = new ObjectArrayList<RecordSchemaDescription>();
        RecordSchemaDescription firstElement = null;

        for (final var item : items) {
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

    private static int getType(JsonNode value) {
        return switch (value.getNodeType()) {
            case NULL -> DataType.UNKNOWN;
            case NUMBER -> DataType.NUMBER;
            case BOOLEAN -> DataType.BOOLEAN;
            case BINARY, STRING -> DataType.STRING;
            case OBJECT -> DataType.MAP;
            case ARRAY -> DataType.ARRAY;
            default -> {
                LOGGER.error("Invalid data type");
                yield DataType.UNKNOWN;
            }
        };
    }

}
