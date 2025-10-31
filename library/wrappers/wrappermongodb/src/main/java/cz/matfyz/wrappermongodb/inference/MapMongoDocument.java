package cz.matfyz.wrappermongodb.inference;

import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.DataType;

import java.util.*;

import org.apache.spark.sql.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import scala.collection.Seq;
import scala.jdk.CollectionConverters;

public abstract class MapMongoDocument {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapMongoDocument.class);

    private MapMongoDocument() {}

    public static RecordSchemaDescription process(Row row) {
        final var result = new RecordSchemaDescription(
            RecordSchemaDescription.ROOT_SYMBOL,
            Char.FALSE,
            Char.FALSE,
            1,
            1
        );

        result.setTypes(DataType.MAP);
        result.setChildren(processMapChildren(row));

        return result;
    }

    public static RecordSchemaDescription processChild(String key, Object value, boolean isFirstOccurrence) {
        final var result = new RecordSchemaDescription(
            key,
            Char.UNKNOWN,
            Char.UNKNOWN,
            1,
            isFirstOccurrence ? 1 : 0
        );

        if (key.equals("_id")) {
            // If the name of the property is _id, it has to be unique identifier (MongoDB convention).
            result.setId(Char.TRUE);
            result.setUnique(Char.TRUE);
        }

        final var type = getType(value);
        result.setTypes(DataType.OBJECT | type);

        if (type == DataType.MAP)
            result.setChildren(processMapChildren((Row) value));
        else if (type == DataType.ARRAY)
            result.setChildren(processArrayChildren((Seq) value));

        return result;
    }

    private static ObjectArrayList<RecordSchemaDescription> processMapChildren(Row row) {
        final var children = new ObjectArrayList<RecordSchemaDescription>(row.size());

        for (final String key : row.schema().fieldNames()) {
            // There is also field.dataType() ... but the current solution should be sufficient and it already fits the algorithm below.
            // Yes, the algorithm is hardly ideal, but fuck it, I am not going to spend a single minute more on fixing other people's pre-school level errors.
            final var value = row.getAs(key);
            children.add(processChild(key, value, true));
        }

        Collections.sort(children);

        return children;
    }

    private static ObjectArrayList<RecordSchemaDescription> processArrayChildren(Seq<Object> seq) {
        // Fuck scala.
        final var list = CollectionConverters.SeqHasAsJava(seq).asJava();

        final var children = new ObjectArrayList<RecordSchemaDescription>();
        RecordSchemaDescription firstElement = null;

        for (final Object value : list) {
            final var currentElement = processChild(RecordSchemaDescription.ROOT_SYMBOL, value, firstElement == null);

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

    static int getType(Object value) {
        return switch (value) {
            case null -> DataType.UNKNOWN;
            case Number number -> DataType.NUMBER;
            case Boolean bool -> DataType.BOOLEAN;
            case String string -> DataType.STRING;
            case Row row -> DataType.MAP;
            case Seq<?> seq -> DataType.ARRAY;
            default -> {
                LOGGER.error("Invalid data type");
                yield DataType.UNKNOWN;
            }
        };
    }

}
