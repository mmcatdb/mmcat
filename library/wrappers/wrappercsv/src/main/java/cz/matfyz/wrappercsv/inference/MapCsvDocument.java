package cz.matfyz.wrappercsv.inference;

import cz.matfyz.core.rsd.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.*;

/**
 * Abstract class representing a CSV document that maps keys and values to a
 * {@link RecordSchemaDescription} structure.
 */
public abstract class MapCsvDocument {

    private MapCsvDocument() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(MapCsvDocument.class);

    /**
     * Processes a given map of strings to produce a {@link RecordSchemaDescription}
     * that represents the schema of a CSV document.
     *
     * @param t the map of strings where each entry represents a key-value pair
     *          in the CSV document.
     * @return a {@link RecordSchemaDescription} representing the structure and schema
     *         inferred from the input map.
     */
    public static RecordSchemaDescription process(Map<String, String> t) {
        RecordSchemaDescription result = new RecordSchemaDescription();

        result.setName(RecordSchemaDescription.ROOT_SYMBOL);
        result.setUnique(Char.FALSE);
        //result.setShare(new Share());
        result.setId(Char.FALSE);
        result.setTypes(Type.MAP);
        result.setModels(Model.DOC);

        ObjectArrayList<RecordSchemaDescription> children = new ObjectArrayList<>();

        t.forEach((key, value) -> {
            Object typedValue = inferType(value);
            children.add(MapCsvRecord.process(key, typedValue, true, true));
        });

        result.setChildren(children);

        return result;
    }

    private static Object inferType(String value) {
        if (value == null || "\\N".equals(value)) {
            return null; // Handle null or missing values
        }
    
        // Check for arrays with brackets
        if (value.startsWith("[") && value.endsWith("]")) {
            // Remove brackets and split the inner content by commas
            String content = value.substring(1, value.length() - 1); // Remove brackets
            return Arrays.asList(content.split(","));
        }
    
        // Try parsing as number
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value); // Floating-point number
            }
            return Integer.parseInt(value); // Integer
        } catch (NumberFormatException ignored) {
        }
    
        // Try parsing as boolean
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
    
        return value;
    }
    
    
}
